package org.magnolialang.resources;

import java.util.Collection;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.magnolialang.util.ISignature;
import org.rascalmpl.interpreter.IRascalMonitor;

public interface IManagedPackage extends IManagedContainer, IManagedFile, IManagedCodeUnit {

	/* (non-Javadoc)
	 * @see org.magnolialang.resources.IManagedContainer#getChildren(org.rascalmpl.interpreter.IRascalMonitor)
	 */
	@Override
	Collection<? extends IManagedCodeUnit> getChildren(IRascalMonitor rm);


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
	 * @see org.magnolialang.errors.ErrorMarkers
	 */
	void addMarker(String message, ISourceLocation loc, String markerType, int severity);


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


	@Override
	Collection<? extends IManagedPackage> getDepends(IRascalMonitor rm);

}
