package org.magnolialang.resources.internal;

import java.net.URI;

import org.magnolialang.resources.IManagedPackage;
import org.magnolialang.resources.IManagedResource;

public interface IWritableResources extends IResources {
	IManagedResource removeResource(URI uri);


	void addResource(URI uri, IManagedResource resource);


	void addPackage(URI uri, String name, IManagedPackage pkg);

}
