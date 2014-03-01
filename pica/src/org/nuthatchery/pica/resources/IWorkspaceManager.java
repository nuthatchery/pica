/**************************************************************************
 * Copyright (c) 2011-2012 Anya Helene Bagge
 * Copyright (c) 2011-2012 Tero Hasu
 * Copyright (c) 2011-2012 University of Bergen
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
package org.nuthatchery.pica.resources;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.errors.ProjectNotFoundError;

public interface IWorkspaceManager {

	void dispose();


	/**
	 * Get the manager for a project
	 * 
	 * @param project
	 *            The project name (in Eclipse) or directory name
	 * @return A project resource manager
	 */
	IResourceManager getManager(String project) throws ProjectNotFoundError;


	/**
	 * Return an URI for the given path.
	 * 
	 * For the workspace manager, the path is interpreted relative to the
	 * workspace root. For project managers, the path is interpreted relative to
	 * the project root. The path may be either absolute or not.
	 * 
	 * Path needs not exist in the file system.
	 * 
	 * @param path
	 *            A string representation of a path
	 * @return An appropriate uri for subsequent calls to the manager
	 * @throws URISyntaxException
	 *             if the path is malformed
	 */
	URI getURI(String path) throws URISyntaxException;


	/**
	 * @param uri
	 *            A URI
	 * @return true if URI points inside workspace / project
	 */
	boolean hasURI(URI uri);


	/**
	 * Stop any running jobs associated with this resource manager
	 */
	void stop();
}
