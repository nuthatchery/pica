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

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.tasks.NullTaskMonitor;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.NullRascalMonitor;

/**
 * A version of the EvaluatorPool that does not depend on Eclipse.
 *
 * TODO: use threads to load stuff asynchroniously
 */

public class ConsoleEvaluatorPool extends AbstractEvaluatorPool {

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
	public ConsoleEvaluatorPool(IEvaluatorFactory factory, String jobName, List<String> imports, int minEvaluators) {
		super(factory, jobName, imports, minEvaluators);
	}


	@Override
	protected void startEvaluatorInit(EvaluatorHandle handle) {
		long time = System.currentTimeMillis();

		handle.initialize(new NullTaskMonitor());
		System.err.println(jobName + ": " + (System.currentTimeMillis() - time) + " ms");
	}
}
