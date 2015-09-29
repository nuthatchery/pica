package org.nuthatchery.pica.resources.eclipse;

import java.io.IOException;
import java.net.URI;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.resources.internal.AbstractManagedResource;
import org.nuthatchery.pica.resources.managed.IManagedResource;

public abstract class ManagedEclipseResource extends AbstractManagedResource {
	protected final IResource resource;
	protected final EclipseProjectManager manager;


	public ManagedEclipseResource(URI uri, IResource resource, EclipseProjectManager manager) {
		super(uri);
		this.resource = resource;
		this.manager = manager;
	}


	@Override
	public IManagedResource getContainingFile() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}


	public IResource getEclipseResource() {
		return resource;
	}


	@Override
	public int getLength() throws UnsupportedOperationException, IOException {
		throw new UnsupportedOperationException();
	}


	@Override
	public long getModificationStamp() {
		return resource.getModificationStamp();
	}


	@Override
	public int getOffset() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}


	@Override
	@Nullable
	public IManagedResource getParent() {
		return null;
	}


	@Override
	public void onResourceChanged() {
	}

}
