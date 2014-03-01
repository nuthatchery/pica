/**************************************************************************
 * Copyright (c) 2012-2013 Anya Helene Bagge
 * Copyright (c) 2012-2013 University of Bergen
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

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.errors.ImplementationError;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.eclipse.nature.WarningsToMarkers;
import org.rascalmpl.interpreter.Evaluator;

public class EclipseEvaluatorPool extends AbstractEvaluatorPool {

	protected volatile Evaluator evaluator = null;
	private final Job initJob;
	protected volatile boolean initialized = false;


	/**
	 * Don't call this constructor directly, use
	 * {@link org.magnolialang.IPica.IInfra#makeEvaluatorPool(String, List)}
	 * 
	 * @param jobName
	 * @param imports
	 */
	public EclipseEvaluatorPool(IEvaluatorFactory factory, String jobName, List<String> imports) {
		super(factory, jobName, imports);
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
		try {
			initJob.join();
		}
		catch(InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		public boolean shouldRun() {
			return !initialized;
		}


		@Override
		protected IStatus run(@Nullable IProgressMonitor monitor) {
			assert monitor != null;
			if(initialized) {
				monitor.done();
				return Status.OK_STATUS;
			}
			long time = System.currentTimeMillis();

			evaluator = makeEvaluator(new RascalMonitor(monitor, new WarningsToMarkers()));

			initialized = true;
			System.err.println(getName() + ": " + (System.currentTimeMillis() - time) + " ms");
			return Status.OK_STATUS;
		}
	}
}
