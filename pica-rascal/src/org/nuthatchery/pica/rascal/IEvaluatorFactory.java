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
import java.util.List;

import org.rascalmpl.interpreter.Evaluator;

public interface IEvaluatorFactory {
	static final int DEFAULT_MIN_EVALUTORS = 1;


	/**
	 * Construct or retrieve an evaluator pool.
	 * 
	 * A minimum of {@value #DEFAULT_MIN_EVALUTORS} evaluators will be created.
	 * 
	 * @param name
	 *            Name of the pool, e.g. "Magnolia loader"
	 * @param imports
	 *            A list of Rascal modules to import
	 * @return A pool with the given modules imported
	 */
	IEvaluatorPool getEvaluatorPool(String name, List<String> imports);


	/**
	 * Construct or retrieve an evaluator pool.
	 * 
	 * @param name
	 *            Name of the pool, e.g. "Magnolia loader"
	 * @param imports
	 *            A list of Rascal modules to import
	 * @param minEvaluators
	 *            The minimum number of evaluators that should be created for
	 *            the pool, if a new pool is created
	 * @return A pool with the given modules imported
	 */
	IEvaluatorPool getEvaluatorPool(String name, List<String> imports, int minEvaluators);


	/**
	 * Make a new Rascal evaluator.
	 * 
	 * Use {@link #makeEvaluatorPool(String, List)} instead if possible.
	 * 
	 * @return Rascal evaluator, with default stdout and stderr
	 * 
	 */
	Evaluator makeEvaluator();


	/**
	 * Make a new Rascal evaluator.
	 * 
	 * Note that the arguments are reversed compared to Evaluator constructor.
	 * 
	 * Use {@link #makeEvaluatorPool(String, List)} instead if possible.
	 * 
	 * @param out
	 *            writer to use as Rascal stdout
	 * @param err
	 *            writer to use as Rascal stderr
	 * @return Rascal evaluator
	 */
	Evaluator makeEvaluator(PrintWriter out, PrintWriter err);


	/**
	 * Refresh the evaluator pools. Call getEvaluatorPool again to get a fresh,
	 * reloaded pool.
	 */
	void refresh();


	/**
	 * Refresh/reinitialise the given evaluator pool
	 * 
	 * @param pool
	 *            The pool
	 * @return A (possibly new) pool with all imports reloaded
	 */
	IEvaluatorPool refresh(IEvaluatorPool pool);


	/**
	 * Refresh/reinitialise the given evaluator pool
	 * 
	 * @param pool
	 *            The pool
	 * @param minEvaluators
	 *            Minimum number of evaluators in the refreshed pool
	 * @return A (possibly new) pool with all imports reloaded
	 */
	IEvaluatorPool refresh(IEvaluatorPool pool, int minEvaluators);

}
