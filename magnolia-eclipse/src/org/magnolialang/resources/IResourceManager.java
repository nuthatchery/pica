package org.magnolialang.resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.magnolialang.nullness.Nullable;
import org.magnolialang.util.depgraph.IDepGraph;
import org.rascalmpl.interpreter.IRascalMonitor;

public interface IResourceManager extends IManagedContainer {

	@Nullable
	IManagedPackage findPackage(URI uri);


	@Nullable
	IManagedResource findResource(URI uri);


	/**
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
	Collection<? extends IManagedPackage> allPackages(ILanguage language);


	Collection<? extends IManagedResource> allFiles();


	/**
	 * @return Return the underlying resource manager
	 */
	IWorkspaceManager getResourceManager();


	/**
	 * Force refresh/reinitialization of the manager, discarding all cached
	 * data.
	 */
	void refresh();


	void addMarker(String message, ISourceLocation loc, String markerType, int severity);


	void addMarker(String message, ISourceLocation loc, String markerType);


	void addMarker(String message, ISourceLocation loc, int severity);


	void addMarker(String message, ISourceLocation loc);


	/**
	 * @return The *project-relative* path to the src folder
	 */
	IPath getSrcFolder();


	/**
	 * @return The *project-relative* path to the output folder
	 */
	IPath getOutFolder();


	IManagedResource findResource(String path);


	/**
	 * Obtain a dependency graph for all packages in the given language.
	 * 
	 * The returned graph is a snapshot which will not change after the method
	 * returns.
	 * 
	 * @param lang
	 *            The language
	 * @param rm
	 *            A monitor, or null
	 * @return A dependency graph
	 */
	IDepGraph<IManagedPackage> getPackageDependencyGraph(ILanguage lang, IRascalMonitor rm);


	void dispose();


	boolean hasURI(URI uri);


	URI getURI(String path) throws URISyntaxException;
}
