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
package org.nuthatchery.pica.resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.errors.Severity;
import org.nuthatchery.pica.resources.handles.IFileHandle;
import org.nuthatchery.pica.resources.handles.IResourceHandle;
import org.nuthatchery.pica.resources.managed.IManagedCodeUnit;
import org.nuthatchery.pica.resources.managed.IManagedResource;
import org.nuthatchery.pica.resources.marks.IMark;
import org.nuthatchery.pica.resources.marks.IMarkPattern;
import org.nuthatchery.pica.resources.regions.ICodeRegion;
import org.nuthatchery.pica.resources.storage.IStorage;
import org.nuthatchery.pica.tasks.ITaskMonitor;
import org.nuthatchery.pica.util.depgraph.IDepGraph;

public interface IProjectManager {

	/**
	 * Add a mark.
	 *
	 * The mark is queued, and will be processed on the next call to
	 * {@link #commitMarks()}.
	 *
	 * @param mark
	 * @see {@link org.nuthatchery.pica.resources.marks.MarkBuilder#done()}
	 */
	void addMark(IMark mark);


	/**
	 * Add a mark.
	 *
	 * The mark is queued, and will be processed on the next call to
	 * {@link #commitMarks()}.
	 *
	 * @param message
	 *            The message
	 * @param loc
	 *            A source location
	 * @param severity
	 *            The severity
	 * @param markerSource
	 *            The mark source, indicating the part of the system that
	 *            generated the mark (e.g., the fully qualified name of the
	 *            class/module that produced the mark)
	 * @param markerContext
	 *            The URI of the resource that was being processed when the
	 *            mark was created
	 * @see org.nuthatchery.pica.errors.ErrorMarkers
	 */
	void addMark(String message, ICodeRegion<URI> loc, Severity severity, String markerSource, @Nullable URI markerContext);


	/**
	 * This method returns a snapshot of all files managed by the resource
	 * manager at the time the method was called.
	 *
	 * @return All resources managed by the resource manager.
	 */
	Iterable<? extends IManagedResource> allFiles();


	/**
	 * This method returns a snapshot of all packages in the given language at
	 * the time the method was called.
	 *
	 * @param language
	 *            A language
	 * @return A collection of packages in the given language
	 */
	Iterable<? extends IManagedCodeUnit> allPackages(ILanguage language);


	/**
	 * Clear all the given marks.
	 *
	 * The cleared marks are queued, and will be processed on the next call to
	 * {@link #commitMarks()}.
	 *
	 * Marks that don't exist are ignored.
	 *
	 * @param marks
	 *            A list of marks to be cleared.
	 *
	 */
	void clearMarks(IMark... marks);


	/**
	 * Clear all markers coming from a particular source / cause.
	 *
	 * The cleared marks are queued, and will be processed on the next call to
	 * {@link #commitMarks()}.
	 *
	 * @param markSource
	 *            The mark source, indicating the part of the system that
	 *            generated the mark (e.g., the fully qualified name of the
	 *            class/module that produced the mark)
	 * @param context
	 *            The URI of the resource that was being processed when the
	 *            mark was created
	 */
	void clearMarks(String markSource, @Nullable URI context);


	/**
	 * Process all queued mark changes.
	 */
	void commitMarks();


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
	IManagedCodeUnit findCodeUnit(ILanguage language, Object moduleId);


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
	IManagedCodeUnit findCodeUnit(ILanguage language, String moduleName);


	/**
	 * Find a package by URI.
	 *
	 * @param uri
	 *            URI of the package
	 * @return The package associated with the URI, or null.
	 */
	@Nullable
	IManagedCodeUnit findCodeUnit(URI uri);


	/**
	 * Find all marks associated with the resource.
	 *
	 * Note that the marks' locations will be relative to the file containing
	 * the resource.
	 *
	 * Queued marks (that are not yet commited) will not be returned.
	 *
	 * @param resource
	 *            A resource
	 * @return All marks associated with the resource.
	 */
	Collection<IMark> findMarks(IManagedResource resource);


	/**
	 * Find all the marks that match the given search criteria.
	 *
	 * @param searchCriteria
	 *            A pattern to search for
	 * @return All matching marks
	 * @see {@link org.nuthatchery.pica.resources.marks.MarkBuilder#toPattern()}
	 */
	Collection<IMark> findMarks(IMarkPattern searchCriteria);


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


	Collection<IManagedResource> getChildren(ITaskMonitor rm);


	/**
	 * Get the code unit corresponding to a file resource
	 *
	 * Returns null if the argument is not associated with a code unit.
	 *
	 * If the resource is a
	 * file, the returned code unit resource should span the entire file.
	 *
	 * @param resource
	 *            A resource
	 * @return A code unit
	 */
	@Nullable
	IManagedCodeUnit getCodeUnit(IResourceHandle resource);


	/**
	 * @return The *project-relative* path to the output folder
	 */
	Path getOutFolder();


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
	IDepGraph<IManagedCodeUnit> getPackageDependencyGraph(@Nullable ITaskMonitor rm);


	Set<IManagedCodeUnit> getPackageTransitiveDependents(IManagedCodeUnit pkg, ITaskMonitor rm);


	/**
	 * @return Name of the managed project
	 */
	String getProjectName();


	/**
	 * @return Return the underlying pkg manager
	 */
	IWorkspaceManager getResourceManager();


	/**
	 * @return The *workspace-relative* path to the src folder
	 */
	Path getSrcFolder();


	IStorage getStorage(URI uri);


	URI getURI();


	/**
	 * Get the URI associated with the path. The path is interpreted relative to
	 * the manager's project.
	 *
	 * @param path
	 * @return A URI corresponding to the path.
	 * @throws URISyntaxException
	 */
	URI getURI(String path) throws URISyntaxException;


	boolean processChanges(ITaskMonitor rm);


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
