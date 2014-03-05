package org.nuthatchery.pica.resources;

import java.util.Collection;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.resources.storage.IStorage;

public interface IWorkspaceConfig {

	Collection<String> getActiveNatures();


	void initCompiler();


	IManagedPackage makePackage(IResourceManager manager, IManagedFile res, @Nullable IStorage storage, IConstructor id, ILanguage lang);
}
