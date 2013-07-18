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
 * it under the terms of the GNU General Public License as published by
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

import org.nuthatchery.pica.errors.Severity;
import org.nuthatchery.pica.rascal.IEvaluatorPool;
import org.nuthatchery.pica.resources.IWorkspaceConfig;
import org.nuthatchery.pica.resources.IWorkspaceManager;

/**
 * This interface abstracts away from underlying platform/infrastructure
 * differences.
 * It is a mixed bag of all sorts of platform dependent functionality.
 * (This is necessary as Java does not support #ifdefs, which we might use
 * instead in C code. We also cannot use runtime checks, as we cannot expect all
 * of the imported classes to be available.)
 * 
 */
public interface IPica {

	/**
	 * Log an exception
	 * 
	 * @param msg
	 *            A log message
	 * @param t
	 *            The exception
	 */
	public void logException(String msg, Throwable t);


	/**
	 * Write a message to the log
	 * 
	 * @param msg
	 *            A log message
	 */
	public void logMessage(String msg);


	/**
	 * Write a message to the log
	 * 
	 * @param msg
	 *            A log message
	 * @param severity
	 *            The severity
	 */
	public void logMessage(String msg, Severity severity);


	/**
	 * @return true if ModuleResource facts are preloaded, and false if produced
	 *         on demand
	 */
	boolean areModuleFactsPreloaded();


	IWorkspaceConfig getConfig();


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


	/**
	 * Get the workspace manager.
	 * 
	 * The workspace manager provides an entry point to all projects and files
	 * in the workspace.
	 * 
	 * @return The workspace manager
	 */
	IWorkspaceManager getWorkspaceManager();


	/**
	 * Reload code and clear cached data
	 */
	void refresh();
}