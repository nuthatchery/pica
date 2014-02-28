/**************************************************************************
 * Copyright (c) 2012 Anya Helene Bagge
 * Copyright (c) 2012 Tero Hasu
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
 * * Tero Hasu
 * 
 *************************************************************************/
package org.nuthatchery.pica;

import java.util.List;

import org.nuthatchery.pica.rascal.IEvaluatorFactory;
import org.nuthatchery.pica.rascal.IEvaluatorPool;

/**
 * This interface abstracts away from underlying platform/infrastructure
 * differences.
 * It is a mixed bag of all sorts of platform dependent functionality.
 * (This is necessary as Java does not support #ifdefs, which we might use
 * instead in C code. We also cannot use runtime checks, as we cannot expect all
 * of the imported classes to be available.)
 * 
 */
public interface IPicaRascal extends IPica {
	/**
	 * The newEvaluator() method is hidden away behind the IEvaluatorFactory
	 * method to encourage the use of evaluator pools instead.
	 * 
	 * @return An evaluator factory
	 */
	IEvaluatorFactory getEvaluatorFactory();


	/**
	 * Get an evaluator pool, with the given list of modules imported.
	 * 
	 * This may return a previously generated pool. If you really need a new
	 * pool, use getEvaluatorFactory().makeEvaluatorPool().
	 * 
	 * @param name
	 *            Name of the pool, e.g. "Magnolia loader"
	 * @param imports
	 *            A list of Rascal modules to import
	 * @return The new pool
	 */
	IEvaluatorPool getEvaluatorPool(String name, List<String> imports);
}
