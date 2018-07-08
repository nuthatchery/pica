package org.nuthatchery.pica.resources.handles;

/**
 *
 * Base class for implemention resource handles. Provides
 * {@link #equals(Object)} and {@link #hashCode()} by delegation to a resouce
 * representation object (i.e a Java File or Eclipse IFile).
 *
 * @author anya
 *
 * @param <ResourceRepresentation>
 */
public abstract class AbstractResourceHandle<ResourceRepresentation> implements IResourceHandle {
	protected final ResourceRepresentation resource;


	protected AbstractResourceHandle(ResourceRepresentation resource) {
		super();
		this.resource = resource;
	}


	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(getClass() != obj.getClass()) {
			return false;
		}
		AbstractResourceHandle<?> other = (AbstractResourceHandle<?>) obj;
		if(resource == null) {
			if(other.resource != null) {
				return false;
			}
		}
		else if(!resource.equals(other.resource)) {
			return false;
		}
		return true;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (resource == null ? 0 : resource.hashCode());
		return result;
	}
}
