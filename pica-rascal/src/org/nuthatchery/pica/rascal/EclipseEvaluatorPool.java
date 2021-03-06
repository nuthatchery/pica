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
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.tasks.eclipse.EclipseTaskMonitor;

public class EclipseEvaluatorPool extends AbstractEvaluatorPool {
	static class InitJob extends Job {

		private final EvaluatorHandle handle;


		public InitJob(String name, EvaluatorHandle handle) {
			super("Loading " + name);
			this.handle = handle;
		}


		@Override
		public IStatus run(@Nullable IProgressMonitor monitor) {
			SubMonitor m = SubMonitor.convert(monitor);
			long time = System.currentTimeMillis();

			handle.initialize(new EclipseTaskMonitor(m));

			System.err.println(getName() + ": " + (System.currentTimeMillis() - time) + " ms");
			m.done();
			return Status.OK_STATUS;
		}
	}


	/**
	 * Don't call this constructor directly, use
	 * {@link org.magnolialang.IPica.IInfra#makeEvaluatorPool(String, List)}
	 *
	 * {@link #initialize()} must be called on the newly constructed pool.
	 *
	 * @param jobName
	 * @param imports
	 * @param minEvaluators
	 */
	public EclipseEvaluatorPool(IEvaluatorFactory factory, String jobName, List<String> imports, int minEvaluators) {
		super(factory, jobName, imports, minEvaluators);
	}


	@Override
	protected void startEvaluatorInit(EvaluatorHandle handle) {
		InitJob initJob = new InitJob(jobName, handle);
		IJobManager jobManager = Job.getJobManager();
		initJob.schedule();
//		if(!jobManager.isSuspended()) {
//			initJob.schedule();
//		}
//		else {
//			initJob.run(null);
//		}
	}
}
