package org.magnolialang.resources;

import java.util.Collection;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;

public interface IManagedPackage extends IManagedContainer, IManagedFile {
	ILanguage getLanguage();


	Collection<IManagedResource> getDepends();


	Collection<IManagedResource> getTransitiveDepends();


	IConstructor getId();


	void addMarker(String message, ISourceLocation loc, String markerType, int severity);

}
