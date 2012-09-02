package org.magnolialang.resources;

import org.eclipse.imp.pdb.facts.ISourceLocation;

public interface IManagedPackage extends IManagedContainer, IManagedFile, IManagedCodeUnit {

	void addMarker(String message, ISourceLocation loc, String markerType, int severity);

}
