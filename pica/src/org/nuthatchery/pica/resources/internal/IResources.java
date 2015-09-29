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

import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.resources.managed.IManagedCodeUnit;
import org.nuthatchery.pica.resources.managed.IManagedPackage;
import org.nuthatchery.pica.resources.managed.IManagedResource;
import org.nuthatchery.pica.util.depgraph.IDepGraph;

public interface IResources<R extends IManagedResource> {

	Collection<IManagedCodeUnit> allCodeUnits();


	Collection<R> allResources();


	Collection<URI> allURIs();


	IWritableResources<R> createNewVersion();


	@Nullable
	IDepGraph<IManagedCodeUnit> getDepGraph();


	@Nullable
	IManagedCodeUnit getPackage(R res);


	@Nullable
	IManagedCodeUnit getPackage(String name);


	@Nullable
	IManagedCodeUnit getPackage(URI uri);


	@Nullable
	R getResource(URI uri);


	int getVersion();


	boolean hasDepGraph();


	int numPackages();


	int numResources();


	void setDepGraph(IDepGraph<IManagedCodeUnit> graph);

}
