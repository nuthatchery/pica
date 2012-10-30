package org.magnolialang.resources.internal;

import java.net.URI;

import org.magnolialang.resources.IManagedPackage;
import org.magnolialang.resources.IManagedResource;

public interface IWritableResources extends IResources {
	void addPackage(URI uri, String name, IManagedPackage pkg);


	void addResource(URI uri, IManagedResource resource);


	IManagedResource removeResource(URI uri);

}
