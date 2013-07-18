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
 * it under the terms of the GNU General Public License as published by
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
package org.magnolialang.pica;

import java.io.PrintWriter;
import java.util.List;

import org.magnolialang.rascal.IEvaluatorPool;
import org.rascalmpl.interpreter.Evaluator;

public interface IEvaluatorFactory {
	/**
	 * @return Rascal evaluator, with default stdout and stderr
	 */
	Evaluator makeEvaluator();


	/**
	 * Note that the arguments are reversed compared to Evaluator constructor.
	 * 
	 * @param out
	 *            writer to use as Rascal stdout
	 * @param err
	 *            writer to use as Rascal stderr
	 * @return Rascal evaluator
	 */
	Evaluator makeEvaluator(PrintWriter out, PrintWriter err);


	/**
	 * Construct a new evaluator pool.
	 * 
	 * @param name
	 *            Name of the pool, e.g. "Magnolia loader"
	 * @param imports
	 *            A list of Rascal modules to import
	 * @return The new pool
	 */
	IEvaluatorPool makeEvaluatorPool(String name, List<String> imports);

}
