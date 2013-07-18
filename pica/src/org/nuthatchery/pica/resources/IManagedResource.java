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
package org.nuthatchery.pica.resources;

import java.net.URI;

import org.eclipse.imp.pdb.facts.IExternalValue;
import org.eclipse.imp.pdb.facts.type.ExternalType;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;

public interface IManagedResource extends IExternalValue {
	public static final Type ResourceType = new ExternalType() {

		@Override
		protected Type glbWithExternal(Type type) {
			if(type == this)
				return this;
			else
				return TypeFactory.getInstance().voidType();
		}


		@Override
		protected boolean isSubtypeOfExternal(Type type) {
			return false;
		}


		@Override
		protected Type lubWithExternal(Type type) {
			if(type == this)
				return this;
			else
				return TypeFactory.getInstance().valueType();
		}

	};


	long getModificationStamp();


	/**
	 * @return The parent, or null if getKind() == PROJECT
	 * @axiom getParent().isContainer() is always true
	 */
	IManagedResource getParent();


	URI getURI();


	/**
	 * A code unit may also be a file and/or a container.
	 * 
	 * @return true if the pkg is a package
	 */
	boolean isCodeUnit();


	/**
	 * A file system folder or a package.
	 * 
	 * @return true if the pkg can have children
	 */
	boolean isContainer();


	/**
	 * A file or a package.
	 * 
	 * @return true if the pkg is a file or a package
	 */
	boolean isFile();


	/**
	 * isProject() implies isContainer()
	 * 
	 * @return true if the pkg is a project
	 */
	boolean isProject();


	void onResourceChanged();
}
