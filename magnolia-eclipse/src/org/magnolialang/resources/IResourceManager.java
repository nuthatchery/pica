/**************************************************************************
 * Copyright (c) 2011-2012 Anya Helene Bagge
 * Copyright (c) 2011-2012 University of Bergen
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
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
package org.magnolialang.resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.magnolialang.errors.Severity;
import org.magnolialang.nullness.Nullable;
import org.magnolialang.resources.storage.IStorage;
import org.magnolialang.util.depgraph.IDepGraph;
import org.rascalmpl.interpreter.IRascalMonitor;

public interface IResourceManager extends IManagedContainer {

	/**
	 * Add a marker with default severity and type (error and compilation
	 * problem, respectively).
	 * 
	 * @param message
	 *            The message
	 * @param loc
	 *            A source location
	 * @see org.magnolialang.errors.ErrorMarkers
	 */
	void addMarker(String message, ISourceLocation loc);


	/**
	 * Add a marker with default type (compilation problem).
	 * 
	 * @param message
	 *            The message
	 * @param loc
	 *            A source location
	 * @param severity
	 *            The severity
	 * @see org.magnolialang.errors.ErrorMarkers
	 */
	void addMarker(String message, ISourceLocation loc, Severity severity);


	/**
	 * Add a marker with default severity (error).
	 * 
	 * @param message
	 *            The message
	 * @param loc
	 *            A source location
	 * @param markerType
	 *            The marker type
	 * @see org.magnolialang.errors.ErrorMarkers
	 */
	void addMarker(String message, ISourceLocation loc, String markerType);


	/**
	 * Add a marker.
	 * 
	 * @param message
	 *            The message
	 * @param loc
	 *            A source location
	 * @param markerType
	 *            The marker type
	 * @param severity
	 *            The severity
	 * @see org.magnolialang.errors.ErrorMarkers
	 */
	void addMarker(String message, ISourceLocation loc, String markerType, Severity severity);


	/**
	 * This method returns a snapshot of all files managed by the resource
	 * manager at the time the method was called.
	 * 
	 * @return All resources managed by the resource manager.
	 */
	Iterable<IManagedResource> allFiles();


	/*
	 * @param moduleName
	 *            A language-specific module identifier (AST)
	 * @return The module with that name, or null
	 * @throws IllegalArgumentException
	 *             if moduleId is not a valid name
	 * @Nullable
	 * 
	 *           This one can't work if modules can have same ID in different
	 *           languages
	 * 
	 *           IManagedResource findModule(IValue moduleId);
	 */

	/**
	 * This method returns a snapshot of all packages in the given language at
	 * the time the method was called.
	 * 
	 * @param language
	 *            A language
	 * @return A collection of packages in the given language
	 */
	Iterable<IManagedPackage> allPackages(ILanguage language);


	/**
	 * Remove all resources associated with this resource manager.
	 */
	void dispose();


	/**
	 * Find a package by id.
	 * 
	 * @param language
	 *            A language
	 * @param moduleId
	 *            A language-specific module ID
	 * @return The module with that name, or null.
	 * @throws IllegalArgumentException
	 *             if moduleName is not a valid name
	 */
	@Nullable
	IManagedPackage findPackage(ILanguage language, IConstructor moduleId);


	/**
	 * Find a package by name.
	 * 
	 * @param language
	 *            A language
	 * @param moduleName
	 *            A language-specific module name string
	 * @return The module with that name, or null.
	 * @throws IllegalArgumentException
	 *             if moduleName is not a valid name
	 */
	@Nullable
	IManagedPackage findPackage(ILanguage language, String moduleName);


	/**
	 * Find a package by URI.
	 * 
	 * @param uri
	 *            URI of the package
	 * @return The package associated with the URI, or null.
	 */
	@Nullable
	IManagedPackage findPackage(URI uri);


	/**
	 * Find a resource by path name.
	 * 
	 * @param path
	 *            Path name, relative to the project.
	 * @return The resource, or null if not found.
	 */
	@Nullable
	IManagedResource findResource(String path);


	/**
	 * Find a resource by URI.
	 * 
	 * @param uri
	 *            URI of the resource
	 * @return The resource associated with the URI, or null.
	 */
	@Nullable
	IManagedResource findResource(URI uri);


	/**
	 * @return The *project-relative* path to the output folder
	 */
	IPath getOutFolder();


	/**
	 * Obtain a dependency graph for all packages.
	 * 
	 * The returned graph is a snapshot which will not change after the method
	 * returns.
	 * 
	 * @param rm
	 *            A monitor, or null
	 * @return A dependency graph
	 */
	IDepGraph<IManagedPackage> getPackageDependencyGraph(IRascalMonitor rm);


	Set<IManagedPackage> getPackageTransitiveDependents(IManagedPackage pkg, IRascalMonitor rm);


	/**
	 * @return Return the underlying pkg manager
	 */
	IWorkspaceManager getResourceManager();


	/**
	 * @return The *project-relative* path to the src folder
	 */
	IPath getSrcFolder();


	IStorage getStorage(URI uri);


	/**
	 * Get the URI associated with the path. The path is interpreted relative to
	 * the manager's project.
	 * 
	 * @param path
	 * @return A URI corresponding to the path.
	 * @throws URISyntaxException
	 */
	URI getURI(String path) throws URISyntaxException;


	boolean processChanges(IRascalMonitor rm);


	/**
	 * Force refresh/reinitialization of the manager, discarding all cached
	 * data.
	 */
	void refresh();


	/**
	 * Stop any running jobs and prepare for shutdown.
	 */
	void stop();
}
