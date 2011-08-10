package org.magnolialang.resources;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.pdb.facts.IValue;
import org.rascalmpl.tasks.IFact;

public interface IManagedResource extends IFact<IValue> {
	URI getURI();

	boolean isFile();

	boolean isFolder();

	ILanguage getLanguage();

	IPath getPath();

	IPath getFullPath();

	IProject getProject();
}
