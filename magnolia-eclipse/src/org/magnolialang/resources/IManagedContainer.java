package org.magnolialang.resources;

import java.util.Collection;

public interface IManagedContainer extends IManagedResource {
	Collection<IManagedResource> getContents();
}
