package org.magnolialang.resources.internal;

import java.net.URI;
import java.util.Collection;

import org.magnolialang.resources.IManagedPackage;
import org.magnolialang.resources.IManagedResource;
import org.magnolialang.util.depgraph.IDepGraph;

public interface IResources {

	IManagedResource getResource(URI uri);


	IManagedPackage getPackage(String name);


	Collection<IManagedResource> allResources();


	Collection<URI> allURIs();


	IWritableResources createNewVersion();


	int getVersion();


	IDepGraph<IManagedPackage> getDepGraph();


	void setDepGraph(IDepGraph<IManagedPackage> graph);


	boolean hasDepGraph();


	int numResources();


	int numPackages();
}
