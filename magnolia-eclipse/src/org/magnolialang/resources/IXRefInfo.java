package org.magnolialang.resources;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;

public interface IXRefInfo {

	IConstructor getDefInfo();


	IConstructor getId();


	ISourceLocation getSrcLoc();


	boolean hasDefInfo();
}
