/**************************************************************************
 * Copyright (c) 2012 Anya Helene Bagge
 * Copyright (c) 2012 University of Bergen
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. See http://www.gnu.org/licenses/
 *
 *
 * See the file COPYRIGHT for more information.
 *
 * Contributors:
 * * Anya Helene Bagge
 *
 *************************************************************************/
package org.nuthatchery.pica.rascal;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import io.usethesource.vallang.IValue;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.errors.CancelledException;
import org.nuthatchery.pica.errors.ImplementationError;
import org.nuthatchery.pica.rascal.errors.EvaluatorLoadError;
import org.nuthatchery.pica.terms.TermFactory;
import org.nuthatchery.pica.util.NullnessHelper;
import org.rascalmpl.interpreter.Evaluator;
import org.nuthatchery.pica.tasks.ITaskMonitor;
import org.nuthatchery.pica.tasks.TaskId;
import org.nuthatchery.pica.tasks.rascal.RascalTaskMonitor;

/**
 * This pool keeps a number of Rascal evaluators available, with modules
 * preloaded.
 *
 */
public abstract class AbstractEvaluatorPool implements IEvaluatorPool {

	class EvaluatorHandle {
		@Nullable
		Evaluator evaluator;
		int count = 0;
		@Nullable
		RuntimeException failCause;
		final int number;


		EvaluatorHandle(int i) {
			number = i;
		}


		IValue call(ITaskMonitor rm, String funName, IValue... args) {
			Evaluator eval = evaluator;
			if(eval != null)
				return NullnessHelper.checkNonNull(eval.call(RascalTaskMonitor.makeRascalMonitor(rm, 100), funName, args));
			else {
				if(failCause != null)
					throw new EvaluatorLoadError("Evaluator for '" + jobName + "' not loaded", failCause);
				else
					throw new EvaluatorLoadError("Evaluator for '" + jobName + "' not loaded");
			}
		}


		IValue call(String funName, IValue... args) {
			Evaluator eval = evaluator;
			if(eval != null)
				return NullnessHelper.checkNonNull(eval.call(funName, args));
			else {
				if(failCause != null)
					throw new EvaluatorLoadError("Evaluator for '" + jobName + "' not loaded", failCause);
				else
					throw new EvaluatorLoadError("Evaluator for '" + jobName + "' not loaded");
			}
		}


		synchronized void initialize(ITaskMonitor rm) {
			try {
				rm.begin(new TaskId("AbstractEvaluatorPool.initialize", "Loading " + jobName, jobName));
				rm.setWorkTodo(1 + imports.size() * 10);
				if(number > 0) {
					rm.subTask("Waiting...");
					try {
						wait(number * 10000);
					}
					catch(InterruptedException e) {
					}
				}
				PrintWriter stderr = new PrintWriter(System.err);
				Evaluator eval = factory.makeEvaluator(stderr, stderr);
				rm.done(1);
				eval.getCurrentEnvt().getStore().importStore(TermFactory.ts);
				for(String imp : imports) {
//					System.err.println("Mg: Importing: " + imp);
					rm.subTask("Importing " + imp);
					eval.doImport(RascalTaskMonitor.makeRascalMonitor(rm, 10), imp);
				}
				System.err.println("Mg: finished making evaluator");
				rm.done();
				evaluator = eval;
			}
			catch(RuntimeException e) {
				failCause = e; // suppress for now since we're probably async
			}
			availableEvaluators.add(this);
		}
	}

	protected final List<String> imports;
	protected final String jobName;
	protected final IEvaluatorFactory factory;
	protected int minEvaluators;

	protected int startedEvaluators = 0;

	/**
	 * The evaluators that are currently in use, organised by thread. The
	 * assumtion is that a single thread can call the same evaluator multiple
	 * times (since the new call will be nested entirely within the stack
	 * activation of the previous calls), for cases where control is transferred
	 * multiple times between Java and Rascal. The same is not true across
	 * thread, hence each thread needs its own evaluator.
	 */
	protected Map<Thread, EvaluatorHandle> inUseEvaluators = new IdentityHashMap<Thread, EvaluatorHandle>();


	/**
	 * A queue of available evaluators. The queue ensures that potential callers
	 * will wait until an evaluator is available. If evaluator loading has
	 * failed for some reason, the returned evaluator handles will throw
	 * exceptions for every operation.
	 */
	protected BlockingQueue<EvaluatorHandle> availableEvaluators = new LinkedBlockingQueue<>();


	/**
	 * Construct a new pool. Always call {@link #initialize()} before using the
	 * pool.
	 *
	 * @param factory
	 * @param jobName
	 * @param imports
	 * @param minEvaluators
	 */
	public AbstractEvaluatorPool(IEvaluatorFactory factory, String jobName, List<String> imports, int minEvaluators) {
		super();
		this.factory = factory;
		this.jobName = jobName;
		this.imports = NullnessHelper.assertNonNull(Collections.unmodifiableList(new ArrayList<String>(imports)));
		this.minEvaluators = minEvaluators;
	}


	@Override
	public IValue call(ITaskMonitor rm, String funName, IValue... args) throws CancelledException {
		if(startedEvaluators < minEvaluators)
			throw new IllegalStateException("Not initialized");
		if(rm.isCanceled())
			throw new CancelledException("Cancelled requested by user");
		try {
//			if(jobName.equals("Magnolia backend") || funName.equals("flatten"))
//				System.err.println("Evaluator pool stats for '" + jobName + "': " + inUseEvaluators.size() + " in use, " + availableEvaluators.size() + "/" + minEvaluators + " available (calling " + funName + ")");
			EvaluatorHandle handle = obtainEvaluator();
			try {
				return handle.call(rm, funName, args);
			}
			finally {
				releaseEvaluator();
			}
		}
		catch(InterruptedException e) {
			throw new CancelledException("Interrupted while waiting", e);
		}
	}


	@Override
	public IValue call(String funName, IValue... args) throws CancelledException {
		if(startedEvaluators < minEvaluators)
			throw new IllegalStateException("Not initialized");
		try {
//			if(jobName.equals("Magnolia backend") || funName.equals("flatten"))
//				System.err.println("Evaluator pool stats for '" + jobName + "': " + inUseEvaluators.size() + " in use, " + availableEvaluators.size() + "/" + minEvaluators + " available (calling " + funName + ")");
			EvaluatorHandle handle = obtainEvaluator();
			try {
				return handle.call(funName, args);
			}
			finally {
				releaseEvaluator();
			}
		}
		catch(InterruptedException e) {
			throw new CancelledException("Interrupted while waiting", e);
		}
	}


	/**
	 * Ensures that evaluator is fully loaded when method returns
	 *
	 * @throws CancelledException
	 */
	@Override
	public void ensureInit() throws CancelledException {
		if(startedEvaluators < minEvaluators)
			throw new IllegalStateException("Not initialized");
		try {
			EvaluatorHandle handle = obtainEvaluator();
			try {
				RuntimeException cause = handle.failCause;
				if(cause != null)
					throw new EvaluatorLoadError("Loading evaluator for '" + jobName + "' failed", cause);
			}
			finally {
				releaseEvaluator();
			}
		}
		catch(InterruptedException e) {
			throw new CancelledException("Interrupted while waiting", e);
		}
	}


	@Override
	public List<String> getImports() {
		return imports;
	}


	@Override
	public int getMinEvaluators() {
		return minEvaluators;
	}


	@Override
	public String getName() {
		return jobName;
	}


	@Override
	public void initialize() {
		synchronized(this) {
			for(; startedEvaluators < minEvaluators; startedEvaluators++) {
				EvaluatorHandle handle = new EvaluatorHandle(startedEvaluators);
				startEvaluatorInit(handle);
			}
		}
	}


	/**
	 * Get an evaluator handle for this thread. Every call *must* be paired with
	 * a call to {@link #releaseEvaluator()} from the same thread.
	 *
	 * @return
	 * @throws InterruptedException
	 */
	private EvaluatorHandle obtainEvaluator() throws InterruptedException {
		synchronized(this) {
			EvaluatorHandle handle = inUseEvaluators.get(Thread.currentThread());
			if(handle != null) {
				handle.count++;
				System.err.println("Reuse evaluator for '" + jobName + "': " + handle.count);
				return handle;
			}
		}

		EvaluatorHandle handle = availableEvaluators.take();
		synchronized(this) {
			handle.count++;
			// Can't be there already, since we're in same thread as above
			inUseEvaluators.put(Thread.currentThread(), handle);
			return handle;
		}
	}


	/**
	 * Release the evaluator handle for this thread. If the handle has been used
	 * multiple times, it will only get returned to the pool once all
	 * outstanding uses are complete.
	 */
	private void releaseEvaluator() {
		synchronized(this) {
			EvaluatorHandle handle = inUseEvaluators.get(Thread.currentThread());
			if(handle != null) {
				handle.count--;
				if(handle.count < 1) {
					inUseEvaluators.remove(Thread.currentThread());
					availableEvaluators.add(handle);
				}
			}
			else
				throw new ImplementationError("Thread release evaluator handle without first obtaining it");
		}
	}


	@Override
	public void setMinEvaluators(int minEvaluators) {
		this.minEvaluators = minEvaluators;
	}


	/**
	 * Schedule initialisation of an evaluator. Abstract, so that the Eclipse
	 * implementation can do this using the Eclipse Jobs API.
	 *
	 * @param handle
	 */
	protected abstract void startEvaluatorInit(EvaluatorHandle handle);
}
