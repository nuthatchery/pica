package org.magnolialang.resources.internal;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.magnolialang.eclipse.MagnoliaPlugin;
import org.magnolialang.resources.IManagedResource;
import org.magnolialang.resources.IResourceManager;

public abstract class AbstractManagedResource implements IManagedResource {
	protected final IResource			resource;
	protected final IResourceManager	manager;


	protected AbstractManagedResource(IResourceManager manager, IResource resource) {
		this.resource = resource;
		this.manager = manager;
	}


	@Override
	public URI getURI() {
		IProject project = resource.getProject();
		IPath path = resource.getProjectRelativePath();
		return MagnoliaPlugin.constructProjectURI(project, path);
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


	@Override
	public long getModificationStamp() {
		return resource.getModificationStamp();
	}

}
