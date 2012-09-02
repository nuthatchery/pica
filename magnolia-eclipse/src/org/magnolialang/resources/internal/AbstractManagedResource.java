package org.magnolialang.resources.internal;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;
import org.magnolialang.eclipse.MagnoliaPlugin;
import org.magnolialang.resources.IManagedResource;
import org.magnolialang.resources.IResourceManager;

public abstract class AbstractManagedResource implements IManagedResource {
	protected final IResource			resource;
	protected final IResourceManager	manager;
	protected final URI					uri;


	protected AbstractManagedResource(IResourceManager manager, IResource resource) {
		this.resource = resource;
		this.manager = manager;
		IProject project = resource.getProject();
		IPath path = resource.getProjectRelativePath();
		uri = MagnoliaPlugin.constructProjectURI(project, path);

	}


	@Override
	public URI getURI() {
		return uri;
	}


	@Override
	public long getModificationStamp() {
		return resource.getModificationStamp();
	}


	@Override
	public Type getType() {
		return IManagedResource.ResourceType;
	}


	@Override
	public <T> T accept(IValueVisitor<T> v) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean isEqual(IValue other) {
		return this == other;
	}


	@Override
	public String toString() {
		return uri.toString();
	}
}
