package org.magnolialang.resources;

import java.util.Collection;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.rascalmpl.interpreter.IRascalMonitor;

public interface IManagedCodeUnit extends IManagedResource {

	ILanguage getLanguage();


	Collection<IManagedPackage> getDepends(IRascalMonitor rm);


	Collection<IManagedPackage> getTransitiveDepends(IRascalMonitor rm);


	IConstructor getId();


	String getName();


	IConstructor getAST(IRascalMonitor rm);


	IConstructor getDefInfo(IRascalMonitor rm);

}
