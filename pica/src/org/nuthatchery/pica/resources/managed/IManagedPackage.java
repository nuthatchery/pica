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
package org.nuthatchery.pica.resources.managed;

import java.util.Collection;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.errors.Severity;
import org.nuthatchery.pica.util.ISignature;
import org.rascalmpl.debug.IRascalMonitor;

public interface IManagedPackage extends IManagedContainer, IManagedCodeUnit {

	/**
	 * Find a given child of a package
	 * 
	 * @param childId
	 *            The fully-qualified ID of the child
	 * @param rm
	 *            A monitor
	 * @return The child, or null if not found
	 */
	@Nullable
	IManagedCodeUnit getChild(IConstructor childId, IRascalMonitor rm);


	/* (non-Javadoc)
	 * @see org.nuthatchery.pica.resources.IManagedContainer#getChildren(org.rascalmpl.debug.IRascalMonitor)
	 */
	@Override
	Collection<? extends IManagedCodeUnit> getChildren(IRascalMonitor rm);


	@Override
	Collection<? extends IManagedCodeUnit> getDepends(IRascalMonitor rm);


	/**
	 * Get a byte array containing a hash that identifies the current source
	 * code version of this package and all its dependencies.
	 * 
	 * The return value must not be modified.
	 * 
	 * @param rm
	 *            A monitor
	 * @return A hash of the package and its dependencies
	 */
	ISignature getFullSignature(IRascalMonitor rm);

}
