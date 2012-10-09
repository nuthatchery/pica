package org.magnolialang.resources.internal;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.magnolialang.resources.IManagedPackage;
import org.magnolialang.resources.IManagedResource;
import org.magnolialang.util.depgraph.IDepGraph;

public class Resources implements IWritableResources {
	private final Map<URI, IManagedResource>	resources			= new HashMap<URI, IManagedResource>();
	private final Map<String, IManagedPackage>	packagesByName		= new HashMap<String, IManagedPackage>();
	private final Map<URI, String>				packageNamesByURI	= new HashMap<URI, String>();
	private final int							version;
	private IDepGraph<IManagedPackage>			depGraph;


	public Resources() {
		version = 0;
	}


	public Resources(int version) {
		this.version = version;
	}


	private Resources(Resources old) {
		version = old.version + 1;
		resources.putAll(old.resources);
		packagesByName.putAll(old.packagesByName);
		packageNamesByURI.putAll(old.packageNamesByURI);
	}


	@Override
	public void addResource(URI uri, IManagedResource resource) {
		resources.put(uri, resource);
	}


	@Override
	public void addPackage(URI uri, String name, IManagedPackage pkg) {
		resources.put(uri, pkg);
		packagesByName.put(name, pkg);
		packageNamesByURI.put(uri, name);
	}


	@Override
	public IManagedResource getResource(URI uri) {
		return resources.get(uri);
	}


	@Override
	public IManagedPackage getPackage(String name) {
		return packagesByName.get(name);
	}


	@Override
	public Collection<IManagedResource> allResources() {
		return resources.values();
	}


	@Override
	public Collection<URI> allURIs() {
		return resources.keySet();
	}


	@Override
	public IManagedResource removeResource(URI uri) {
		IManagedResource removed = resources.remove(uri);
		String name = packageNamesByURI.remove(uri);
		if(name != null) {
			packagesByName.remove(name);
		}
		return removed;
	}


	@Override
	public IWritableResources createNewVersion() {
		return new Resources(this);
	}


	@Override
	public int getVersion() {
		return version;
	}


	@Override
	public IDepGraph<IManagedPackage> getDepGraph() {
		return depGraph;
	}


	@Override
	public void setDepGraph(IDepGraph<IManagedPackage> graph) {
		depGraph = graph;
	}


	@Override
	public boolean hasDepGraph() {
		return depGraph != null;
	}


	@Override
	public int numResources() {
		return resources.size();
	}


	@Override
	public int numPackages() {
		return packagesByName.size();
	}
}