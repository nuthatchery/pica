package org.nuthatchery.pica.resources;

public class ResourceRegion implements IResourceRegion {

	private final int length;
	private final int offset;
	private final IManagedResource resource;


	public ResourceRegion(IManagedResource resource, int offset, int length) {
		super();
		if(offset < 0 || length < 0) {
			throw new IllegalArgumentException("Offset and length must not be negative");
		}
		this.resource = resource;
		this.offset = offset;
		this.length = length;
	}


	public boolean contains(IResourceRegion other) {
		if(resource.equals(other.getResource())) {
			return offset <= other.getOffset() && offset + length >= other.getOffset() + other.getLength();
		}
		else {
			return false;
		}
	}


	@Override
	public int getLength() {
		return length;
	}


	@Override
	public int getOffset() {
		return offset;
	}


	@Override
	public IManagedResource getResource() {
		return resource;
	}
}
