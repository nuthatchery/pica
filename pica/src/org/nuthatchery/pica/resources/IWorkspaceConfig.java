package org.nuthatchery.pica.resources;

import java.util.Collection;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.resources.handles.IFileHandle;
import org.nuthatchery.pica.resources.managed.IManagedCodeUnit;
import org.nuthatchery.pica.resources.storage.IStorage;

public interface IWorkspaceConfig {

	Collection<String> getActiveNatures();


	void initCompiler();


	IManagedCodeUnit makePackage(IProjectManager manager, IFileHandle res, @Nullable IStorage storage, IConstructor id, ILanguage lang);
}
