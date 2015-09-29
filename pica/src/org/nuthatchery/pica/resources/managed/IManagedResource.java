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

import org.eclipse.imp.pdb.facts.IExternalValue;
import org.eclipse.imp.pdb.facts.type.ExternalType;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.resources.handles.IResourceHandle;

public interface IManagedResource extends IResourceHandle, IExternalValue {
	public static final Type ResourceType = new ExternalType() {

		@Override
		protected Type glbWithExternal(@Nullable Type type) {
			if(type == this) {
				return this;
			}
			else {
				return TypeFactory.getInstance().voidType();
			}
		}


		@Override
		protected boolean isSubtypeOfExternal(@Nullable Type type) {
			return false;
		}


		@Override
		protected Type lubWithExternal(@Nullable Type type) {
			if(type == this) {
				return this;
			}
			else {
				return TypeFactory.getInstance().valueType();
			}
		}

	};


	/**
	 * Get the file resource this fragment is part of
	 *
	 * @return A resource corresponding to a file
	 * @throws UnsupportedOperationException
	 *             if !isFragment()
	 */
	IManagedResource getContainingFile() throws UnsupportedOperationException;


	/**
	 * Get the file-relative length of this resource
	 *
	 * @return The length of this resource within a file
	 * @throws UnsupportedOperationException
	 *             if !isFile() or !isFragment()
	 * @throws IOException
	 *             if resource is a file, and checking its length fails
	 */
	int getLength() throws UnsupportedOperationException, IOException;


	/**
	 * Get the logical URI corresponding to this resource.
	 *
	 * This is the physical URI, corresponding to how the resource is addressed
	 * internally, possible through a search path based scheme that is mappable
	 * to a physical URI.
	 * *
	 * 
	 * @return Logical URI of resource
	 */
	URI getLogicalURI();


	/**
	 * Get the file-relative offset of this resource
	 *
	 * @return A zero-based inclusive offset within a file
	 * @throws UnsupportedOperationException
	 *             if !isFile() or !isFragment()
	 */
	int getOffset() throws UnsupportedOperationException;


	/**
	 * @return The parent, or null if getKind() == PROJECT
	 * @axiom getParent().isContainer() is always true
	 */
	@Nullable
	IManagedResource getParent();


	/**
	 * A code unit may also be a file and/or a container.
	 *
	 * @return true if the pkg is a package
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
	 * isProject() implies isContainer()
	 *
	 * @return true if the pkg is a project
	 */
	boolean isProject();


	void onResourceChanged();
}
