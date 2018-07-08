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

import io.usethesource.vallang.IValue;
import org.nuthatchery.pica.errors.CancelledException;
import org.nuthatchery.pica.rascal.errors.EvaluatorLoadError;
import org.nuthatchery.pica.tasks.ITaskMonitor;

public interface IEvaluatorPool {

	/**
	 * Call the given Rascal function.
	 * 
	 * @param rm
	 * @param funName
	 * @param args
	 * @return Result of the function call
	 * @throws CancelledException
	 *             If the thread was interrupted while waiting for an evaluator,
	 *             or cancellation requested by the user
	 * @throws EvaluatorLoadError
	 *             If the evaluator has failed to load for some reason
	 */
	IValue call(ITaskMonitor rm, String funName, IValue... args) throws CancelledException, EvaluatorLoadError;


	/**
	 * Call the given Rascal function, with no/default monitor
	 * 
	 * @param funName
	 * @param args
	 * @return Result of the function call
	 * @throws CancelledException
	 *             If the thread was interrupted while waiting for an evaluator,
	 *             or cancellation requested by the user
	 * @throws EvaluatorLoadError
	 *             If the evaluator has failed to load for some reason
	 */
	IValue call(String string, IValue... args) throws CancelledException, EvaluatorLoadError;


	/**
	 * Ensures that at least one evaluator is fully loaded when method returns.
	 * 
	 * May take a long time to complete.
	 * 
	 * Calling this method is unnecessary, but may be desirable in order to
	 * check that loading has succeeded before calling functions.
	 * 
	 * @throws CancelledException
	 *             If the thread was interrupted while waiting for an evaluator,
	 *             or cancellation requested by the user
	 * @throws EvaluatorLoadError
	 *             If the evaluator has failed to load for some reason
	 */
	void ensureInit() throws CancelledException, EvaluatorLoadError;


	/**
	 * @return An (unmodifiable) list of this pool's imports
	 */
	List<String> getImports();


	/**
	 * @return The minimum number of evaluators this pool should have
	 */
	int getMinEvaluators();


	/**
	 * @return The name of this pool
	 */
	String getName();


	/**
	 * Reload all evaluators.
	 * 
	 * May complete asynchroniously.
	 */
	// void reload();

	/**
	 * Start creating the evaluators for this pool. This method may be called
	 * multiple times, and will do nothing if all evaluators have already been
	 * started. Must be called prior to the other methods in the interface.
	 */
	void initialize();


	/**
	 * Set the minimum number of evaluators this pool should have.
	 * 
	 * Call {@link #initialize()} afterwards to actually create the evaluators.
	 */
	void setMinEvaluators(int minEvaluators);

}
