package org.magnolialang.resources.internal;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.pdb.facts.IValue;
import org.magnolialang.eclipse.MagnoliaPlugin;
import org.magnolialang.resources.ILanguage;
import org.magnolialang.resources.IManagedResource;
import org.magnolialang.resources.ResourceManager;
import org.rascalmpl.tasks.facts.AbstractFact;

public abstract class ManagedResource extends AbstractFact<IValue> implements IManagedResource {
	protected final IResource	resource;


	protected ManagedResource(@SuppressWarnings("unused") ResourceManager manager, IResource resource) {
		super(null, resource.getLocationURI().toString(), null);
		this.resource = resource;
	}


	@Override
	public URI getURI() {
		IProject project = resource.getProject();
		IPath path = resource.getProjectRelativePath();
		return MagnoliaPlugin.constructProjectURI(project, path);
	}


	@Override
	public ILanguage getLanguage() {
		return null; // TODO: maybe make a NullLanguage instead, to avoid
						// nullpointers?
	}


	@Override
	public IPath getPath() {
		return resource.getProjectRelativePath();
	}


	@Override
	public IPath getFullPath() {
		return resource.getFullPath();
	}


	@Override
	public IProject getProject() {
		return resource.getProject();
	}

}
