package org.magnolialang.resources;

import java.util.Collection;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.rascalmpl.interpreter.IRascalMonitor;

public interface IManagedPackage extends IManagedContainer, IManagedFile, IManagedCodeUnit {

	/* (non-Javadoc)
	 * @see org.magnolialang.resources.IManagedContainer#getChildren(org.rascalmpl.interpreter.IRascalMonitor)
	 */
	@Override
	Collection<IManagedCodeUnit> getChildren(IRascalMonitor rm);


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
	 * Add a marker to the resource.
	 * 
	 * The location must to refer to the given resource, or the behaviour will
	 * be undefined.
	 * 
	 * @param message
	 *            A message for the marker
	 * @param loc
	 *            The marker location, must refer to the given resource
	 * @param markerType
	 *            A marker type
	 * @param severity
	 *            A severity
	 * @see org.magnolialang.errors.ErrorMarkers
	 */
	void addMarker(String message, ISourceLocation loc, String markerType, int severity);

}
