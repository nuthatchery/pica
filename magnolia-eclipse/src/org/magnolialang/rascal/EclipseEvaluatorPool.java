package org.magnolialang.rascal;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.magnolialang.eclipse.MagnoliaPlugin;
import org.magnolialang.errors.ImplementationError;
import org.magnolialang.errors.RascalErrors;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.eclipse.nature.WarningsToMarkers;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.control_exceptions.Throw;

public class EclipseEvaluatorPool extends AbstractEvaluatorPool {

	private volatile Evaluator evaluator = null;
	private final Job initJob;
	private volatile boolean initialized = false;


	/**
	 * Don't call this constructor directly, use
	 * {@link org.magnolialang.infra.IInfra#makeEvaluatorPool(String, List)}
	 * 
	 * @param jobName
	 * @param imports
	 */
	public EclipseEvaluatorPool(String jobName, List<String> imports) {
		super(jobName, imports);
		this.initJob = new InitJob(jobName);
	}


	/**
	 * Ensures that evaluator is fully loaded when method returns
	 */
	@Override
	public synchronized void ensureInit() {
		if(!initialized || evaluator == null) {
			waitForInit();
		}
	}


	@Override
	public synchronized void reload() {
		initialized = false;
		System.err.println(jobName + ": scheduling job");
		initJob.schedule();
	}


	/**
	 * @return an Evaluator with all the compiler code loaded
	 */
	@Override
	protected Evaluator getEvaluator() {
		if(!initialized || evaluator == null) {
			waitForInit();
		}
		return evaluator;
	}


	protected void waitForInit() {
		try {
			initJob.schedule();
			initJob.join();
			if(evaluator == null)
				throw new ImplementationError("Loading compiler failed");
		}
		catch(InterruptedException e) {
			throw new ImplementationError("Loading compiler failed", e);
		}
	}


	class InitJob extends Job {

		public InitJob(String name) {
			super("Loading " + name);
		}


		@Override
		public boolean belongsTo(Object obj) {
			return obj == MagnoliaPlugin.JOB_FAMILY_MAGNOLIA;
		}


		@Override
		public boolean shouldRun() {
			return !initialized;
		}


		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if(initialized) {
				monitor.done();
				return Status.OK_STATUS;
			}
			long time = System.currentTimeMillis();
			try {
				evaluator = makeEvaluator(new RascalMonitor(monitor, new WarningsToMarkers()));
			}
			catch(Throw t) {
				throw RascalErrors.decodeRascalError(t);
			}
			initialized = true;
			System.err.println(getName() + ": " + (System.currentTimeMillis() - time) + " ms");
			return Status.OK_STATUS;
		}
	}
}
