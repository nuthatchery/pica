package org.nuthatchery.pica.resources;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.nuthatchery.pica.resources.storage.IStorage;

public interface IWorkspaceConfig {

	Collection<String> getActiveNatures();


	void initCompiler();


	IManagedPackage makePackage(IResourceManager manager, IFile resource, IStorage storage, IConstructor id, ILanguage lang);
}
