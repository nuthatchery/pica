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

import java.net.URI;
import java.nio.file.Path;

import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.errors.Severity;
import org.nuthatchery.pica.resources.IWorkspaceConfig;
import org.nuthatchery.pica.resources.IWorkspaceManager;
import org.nuthatchery.pica.resources.handles.IResourceHandle;

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
	 * @return true if ModuleResource facts are preloaded, and false if produced
	 *         on demand
	 */
	boolean areModuleFactsPreloaded();


	/**
	 * Make a project:// URI from the project name and path
	 *
	 * @param project
	 * @param path
	 * @return
	 */
	URI constructProjectURI(String project, Path path);


	URI constructProjectURI(String project, String path);


	IWorkspaceConfig getConfig();


	IResourceHandle getResourceHandle(URI uri);


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
	 * Log an exception
	 *
	 * @param msg
	 *            A log message (optional)
	 * @param t
	 *            The exception (optional)
	 */
	public void logException(@Nullable String msg, @Nullable Throwable t);


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
	 * Print a message to standard error, with a copy to the current log file
	 * (if any)
	 *
	 * @param msg
	 */
	void println(String msg);

}
