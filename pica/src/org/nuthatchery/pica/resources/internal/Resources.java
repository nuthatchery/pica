/**************************************************************************
 * Copyright (c) 2012 Anya Helene Bagge
 * Copyright (c) 2012 University of Bergen
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. See http://www.gnu.org/licenses/
 * 
 * 
 * See the file COPYRIGHT for more information.
 * 
 * Contributors:
 * * Anya Helene Bagge
 * 
 *************************************************************************/
package org.nuthatchery.pica.resources.internal;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.resources.IManagedCodeUnit;
import org.nuthatchery.pica.resources.IManagedPackage;
import org.nuthatchery.pica.resources.IManagedResource;
import org.nuthatchery.pica.util.depgraph.IDepGraph;

public class Resources<R extends IManagedResource> implements IWritableResources<R> {
	private final Map<URI, R> resources = new HashMap<URI, R>();
	private final Map<String, IManagedCodeUnit> packagesByName = new HashMap<String, IManagedCodeUnit>();
	private final Map<URI, String> packageNamesByURI = new HashMap<URI, String>();
	private final Map<R, IManagedCodeUnit> packagesByFile = new IdentityHashMap<R, IManagedCodeUnit>();
	private final int version;
	@Nullable
	private IDepGraph<IManagedCodeUnit> depGraph;


	public Resources() {
		version = 0;
	}


	public Resources(int version) {
		this.version = version;
	}


	private Resources(Resources<R> old) {
		version = old.version + 1;
		resources.putAll(old.resources);
		packagesByFile.putAll(old.packagesByFile);
		packagesByName.putAll(old.packagesByName);
		packageNamesByURI.putAll(old.packageNamesByURI);
		if(packagesByFile.size() != packagesByName.size()) {
			System.err.println("Something is terribly wrong here...");
		}
	}


	@Override
	public void addPackage(URI uri, String name, IManagedCodeUnit pkg, R res) {
		if(!(resources.get(uri) == res)) {
			throw new IllegalArgumentException();
		}
		packagesByFile.put(res, pkg);
		packagesByName.put(name, pkg);
		packageNamesByURI.put(uri, name);
		if(packagesByFile.size() != packagesByName.size()) {
			System.err.println("Something is terribly wrong here...");
		}
	}


	@Override
	public void addResource(URI uri, R resource) {
		resources.put(uri, resource);
	}


	@Override
	public Collection<IManagedCodeUnit> allCodeUnits() {
		return packagesByName.values();
	}


	@Override
	public Collection<R> allResources() {
		return resources.values();
	}


	@Override
	public Collection<URI> allURIs() {
		Set<URI> set = new HashSet<URI>(resources.keySet());
		set.addAll(packageNamesByURI.keySet());
		return set;
	}


	@Override
	public IWritableResources<R> createNewVersion() {
		return new Resources<R>(this);
	}


	@Override
	@Nullable
	public IDepGraph<IManagedCodeUnit> getDepGraph() {
		return depGraph;
	}


	@Override
	@Nullable
	public IManagedCodeUnit getPackage(R res) {
		if(packagesByFile.size() != packagesByName.size()) {
			System.err.println("Something is terribly wrong here...");
		}
		return packagesByFile.get(res);
	}


	@Override
	@Nullable
	public IManagedCodeUnit getPackage(String name) {
		return packagesByName.get(name);
	}


	@Override
	@Nullable
	public IManagedCodeUnit getPackage(URI uri) {
		String name = packageNamesByURI.get(uri);
		if(name != null) {
			return packagesByName.get(name);
		}
		else {
			return null;
		}
	}


	@Override
	@Nullable
	public R getResource(URI uri) {
		return resources.get(uri);
	}


	@Override
	public int getVersion() {
		return version;
	}


	@Override
	public boolean hasDepGraph() {
		return depGraph != null;
	}


	@Override
	public int numPackages() {
		return packagesByName.size();
	}


	@Override
	public int numResources() {
		return resources.size();
	}


	@Override
	@Nullable
	public IManagedResource removeResource(URI uri) {
		IManagedResource removed = resources.remove(uri);
		String name = packageNamesByURI.remove(uri);
		if(name != null) {
			packagesByName.remove(name);
		}
		if(removed != null) {
			packagesByFile.remove(removed);
		}
		if(packagesByFile.size() != packagesByName.size()) {
			System.err.println("Something is terribly wrong here...");
		}
		return removed;
	}


	@Override
	public void setDepGraph(IDepGraph<IManagedCodeUnit> graph) {
		depGraph = graph;
	}

}
