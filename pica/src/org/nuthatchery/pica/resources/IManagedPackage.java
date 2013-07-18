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
package org.nuthatchery.pica.resources;

import java.util.Collection;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.nuthatchery.pica.errors.Severity;
import org.nuthatchery.pica.resources.storage.IStorage;
import org.nuthatchery.pica.util.ISignature;
import org.rascalmpl.interpreter.IRascalMonitor;

public interface IManagedPackage extends IManagedContainer, IManagedFile, IManagedCodeUnit {

	/**
	 * Add a marker to the pkg.
	 * 
	 * The location must to refer to the given pkg, or the behaviour will
	 * be undefined.
	 * 
	 * @param message
	 *            A message for the marker
	 * @param loc
	 *            The marker location, must refer to the given pkg
	 * @param markerType
	 *            A marker type
	 * @param severity
	 *            A severity
	 * @see org.nuthatchery.pica.errors.ErrorMarkers
	 */
	void addMarker(String message, ISourceLocation loc, String markerType, Severity severity);


	/**
	 * Find a given child of a package
	 * 
	 * @param childId
	 *            The fully-qualified ID of the child
	 * @param rm
	 *            A monitor
	 * @return The child, or null if not found
	 */
	IManagedCodeUnit getChild(IConstructor childId, IRascalMonitor rm);


	/* (non-Javadoc)
	 * @see org.nuthatchery.pica.resources.IManagedContainer#getChildren(org.rascalmpl.interpreter.IRascalMonitor)
	 */
	@Override
	Collection<? extends IManagedCodeUnit> getChildren(IRascalMonitor rm);


	@Override
	Collection<? extends IManagedPackage> getDepends(IRascalMonitor rm);


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


	/**
	 * Get a byte array containing a hash that identifies the current source
	 * code of this package.
	 * 
	 * The return value must not be modified.
	 * 
	 * @param rm
	 *            A monitor
	 * @return A hash of the source code
	 */
	ISignature getSourceSignature(IRascalMonitor rm);


	IStorage getStorage();

}
