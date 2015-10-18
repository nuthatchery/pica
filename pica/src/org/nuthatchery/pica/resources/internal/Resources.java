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
import org.nuthatchery.pica.resources.handles.IResourceHandle;
import org.nuthatchery.pica.resources.managed.IManagedCodeUnit;
import org.nuthatchery.pica.resources.managed.IManagedResource;
import org.nuthatchery.pica.util.depgraph.IDepGraph;

public class Resources implements IWritableResources {
	private final Map<URI, IManagedResource> resources = new HashMap<>();
	private final Map<String, IManagedCodeUnit> unitsByName = new HashMap<>();
	private final Map<URI, String> unitNamesByURI = new HashMap<>();
	private final Map<IResourceHandle, IManagedCodeUnit> unitsByFile = new HashMap<>();
	private final int version;
	@Nullable
	private IDepGraph<IManagedCodeUnit> depGraph;


	public Resources() {
		version = 0;
	}


	public Resources(int version) {
		this.version = version;
	}


	private Resources(Resources old) {
		version = old.version + 1;
		resources.putAll(old.resources);
		unitsByFile.putAll(old.unitsByFile);
		unitsByName.putAll(old.unitsByName);
		unitNamesByURI.putAll(old.unitNamesByURI);
		if(unitsByFile.size() != unitsByName.size()) {
			System.err.println("Something is terribly wrong here...");
		}
	}


	@Override
	public void addPackage(URI uri, String name, IManagedCodeUnit pkg, IResourceHandle res) {
		if(!(resources.get(uri).equals(res))) {
			throw new IllegalArgumentException();
		}
		unitsByFile.put(res, pkg);
		unitsByName.put(name, pkg);
		unitNamesByURI.put(uri, name);
		if(unitsByFile.size() != unitsByName.size()) {
			System.err.println("Something is terribly wrong here...");
		}
	}


	@Override
	public void addResource(URI uri, IManagedResource resource) {
		resources.put(uri, resource);
	}


	@Override
	public Collection<IManagedCodeUnit> allCodeUnits() {
		return unitsByName.values();
	}


	@Override
	public Collection<IManagedResource> allResources() {
		return resources.values();
	}


	@Override
	public Collection<URI> allURIs() {
		Set<URI> set = new HashSet<URI>(resources.keySet());
		set.addAll(unitNamesByURI.keySet());
		return set;
	}


	@Override
	public IWritableResources createNewVersion() {
		return new Resources(this);
	}


	@Override
	@Nullable
	public IDepGraph<IManagedCodeUnit> getDepGraph() {
		return depGraph;
	}


	@Override
	@Nullable
	public IManagedCodeUnit getPackage(IManagedResource res) {
		if(unitsByFile.size() != unitsByName.size()) {
			System.err.println("Something is terribly wrong here...");
		}
		return unitsByFile.get(res);
	}


	@Override
	@Nullable
	public IManagedCodeUnit getPackage(URI uri) {
		String name = unitNamesByURI.get(uri);
		if(name != null) {
			return unitsByName.get(name);
		}
		else {
			return null;
		}
	}


	@Override
	@Nullable
	public IManagedResource getResource(URI uri) {
		return resources.get(uri);
	}


	@Override
	@Nullable
	public IManagedCodeUnit getUnit(String name) {
		return unitsByName.get(name);
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
		return unitsByName.size();
	}


	@Override
	public int numResources() {
		return resources.size();
	}


	@Override
	@Nullable
	public IManagedResource removeResource(URI uri) {
		IManagedResource removed = resources.remove(uri);
		String name = unitNamesByURI.remove(uri);
		if(name != null) {
			unitsByName.remove(name);
		}
		if(removed != null) {
			unitsByFile.remove(removed);
		}
		if(unitsByFile.size() != unitsByName.size()) {
			System.err.println("Something is terribly wrong here...");
		}
		return removed;
	}


	@Override
	public void setDepGraph(IDepGraph<IManagedCodeUnit> graph) {
		depGraph = graph;
	}

}
