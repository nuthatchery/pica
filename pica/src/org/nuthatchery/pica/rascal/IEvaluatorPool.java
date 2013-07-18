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

import org.eclipse.imp.pdb.facts.IValue;
import org.rascalmpl.interpreter.IRascalMonitor;

public interface IEvaluatorPool {

	/**
	 * Call the given Rascal function.
	 * 
	 * @param rm
	 * @param funName
	 * @param args
	 * @return Result of the function call
	 */
	IValue call(IRascalMonitor rm, String funName, IValue... args);


	/**
	 * Call the given Rascal function, with no/default monitor
	 * 
	 * @param funName
	 * @param args
	 * @return Result of the function call
	 */
	IValue call(String string, IValue... args);


	/**
	 * Ensures that evaluator is fully loaded when method returns.
	 * 
	 * May take a long time to complete.
	 */
	void ensureInit();


	/**
	 * Reload all evaluators.
	 * 
	 * May complete asynchroniously.
	 */
	void reload();

}
