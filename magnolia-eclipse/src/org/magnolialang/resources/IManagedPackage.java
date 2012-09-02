package org.magnolialang.resources;

import java.util.Collection;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.rascalmpl.interpreter.IRascalMonitor;

public interface IManagedPackage extends IManagedContainer, IManagedFile {
	ILanguage getLanguage();


	Collection<IManagedPackage> getDepends(IRascalMonitor rm);


	Collection<IManagedPackage> getTransitiveDepends(IRascalMonitor rm);


	IConstructor getId();


	String getName();


	IConstructor getAST(IRascalMonitor rm);


	void addMarker(String message, ISourceLocation loc, String markerType, int severity);


	IConstructor getDefInfo(IRascalMonitor rm);

}
