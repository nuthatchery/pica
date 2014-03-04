package org.nuthatchery.pica.resources;

import java.util.Collection;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.nuthatchery.pica.resources.storage.IStorage;

public interface IWorkspaceConfig {

	Collection<String> getActiveNatures();


	void initCompiler();


	IManagedPackage makePackage(IResourceManager manager, IManagedFile res, IStorage storage, IConstructor id, ILanguage lang);
}
