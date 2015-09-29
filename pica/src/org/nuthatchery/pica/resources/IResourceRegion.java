package org.nuthatchery.pica.resources;

import org.nuthatchery.pica.resources.managed.IManagedResource;

public interface IResourceRegion {
	int getLength();


	int getOffset();


	IManagedResource getResource();
}
