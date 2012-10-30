package org.magnolialang.resources.internal;

import java.net.URI;
import java.util.Collection;

import org.magnolialang.resources.IManagedPackage;
import org.magnolialang.resources.IManagedResource;
import org.magnolialang.util.depgraph.IDepGraph;

public interface IResources {

	Collection<IManagedResource> allResources();


	Collection<URI> allURIs();


	IWritableResources createNewVersion();


	IDepGraph<IManagedPackage> getDepGraph();


	IManagedPackage getPackage(String name);


	IManagedResource getResource(URI uri);


	int getVersion();


	boolean hasDepGraph();


	int numPackages();


	int numResources();


	void setDepGraph(IDepGraph<IManagedPackage> graph);
}
