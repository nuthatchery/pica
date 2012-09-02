package org.magnolialang.resources;

import java.util.Collection;

import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.magnolialang.compiler.ICompiler;
import org.magnolialang.nullness.Nullable;

public interface IResourceManager extends IWorkspaceManager, IManagedContainer {
	/**
	 * Get the compiler for the given language.
	 */
	ICompiler getCompiler(ILanguage language);


	/**
	 * Get a compiler suitable for the sourceFile. The path need not exist, only
	 * the file name extension is considered.
	 */
	ICompiler getCompiler(IPath sourceFile);


	@Nullable
	IManagedPackage findPackage(IPath path);


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

	Collection<IPath> allPackages(ILanguage language);


	Collection<IPath> allFiles();


	/**
	 * Get module id for a path. Resource handle operation; path does not have
	 * to exist.
	 * 
	 * @param path
	 *            A path, either absolute or project-relative
	 * @return The language-specific module id for a module with that path.
	 */
	IConstructor getPackageId(IPath path);


	/**
	 * Get module name for a path. Resource handle operation; path does not have
	 * to exist.
	 * 
	 * @param path
	 *            A path, either absolute or project-relative
	 * @return The language-specific module name string for a module with that
	 *         path.
	 */
	String getPackageName(IPath path);


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

}
