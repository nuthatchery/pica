package org.magnolialang.resources;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.magnolialang.resources.storage.IStorage;

public interface IWorkspaceConfig {

	Collection<String> getActiveNatures();


	ClassLoader getParserClassLoader();


	void initCompiler();


	IManagedPackage makePackage(IResourceManager manager, IFile resource, IStorage storage, IConstructor id, ILanguage lang);


	String moreRascalClassPath();


	List<URI> moreRascalSearchPath();
}
