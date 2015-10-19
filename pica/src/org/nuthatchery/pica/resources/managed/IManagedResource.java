/**************************************************************************
 * Copyright (c) 2011-2013 Anya Helene Bagge
 * Copyright (c) 2011-2013 University of Bergen
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
package org.nuthatchery.pica.resources.managed;

import java.io.IOException;
import java.net.URI;

import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.resources.handles.IResourceHandle;

public interface IManagedResource {


	/**
	 * Get the length of this resource
	 *
	 * @return The length of this resource
	 * @throws UnsupportedOperationException
	 *             if !isFile() or !isFragment()
	 * @throws IOException
	 *             if resource is a file, and checking its length fails
	 */
	long getLength() throws UnsupportedOperationException, IOException;


	/**
	 * Get the file-relative offset of this resource
	 *
	 * @return A zero-based inclusive offset within a file
	 * @throws UnsupportedOperationException
	 *             if !isFile() or !isFragment()
	 */
	long getOffset() throws UnsupportedOperationException;


	/**
	 * @return The parent, or null if getKind() == PROJECT
	 * @axiom getParent().isContainer() is always true
	 */
	@Nullable
	IManagedContainer getParent();


	/**
	 * Get a handle to the underlying resource this managed resource is
	 * associated with.
	 *
	 * @return A resource handle
	 */
	IResourceHandle getResource();


	URI getURI();


	/**
	 * A code unit may also be a file and/or a container.
	 *
	 * @return true if this resourec is a
	 */
	boolean isCodeUnit();


	/**
	 * A part of a file.
	 *
	 * Implies !isFile()
	 *
	 * @return true if this resource is a strict sub-part of a file
	 */
	boolean isFragment();


	/**
	 * Check if resource is at the root of the resource hierarchy
	 *
	 * @return true if resource is root
	 */
	boolean isRoot();


	void onResourceChanged();
}
