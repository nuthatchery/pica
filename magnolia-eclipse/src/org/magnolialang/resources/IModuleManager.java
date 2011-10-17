package org.magnolialang.resources;

import java.util.Collection;

import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.magnolialang.compiler.ICompiler;

public interface IModuleManager extends IResourceManager {
	/**
	 * Get the compiler for the given language.
	 */
	ICompiler getCompiler(ILanguage language);


	/**
	 * Get a compiler suitable for the sourceFile. The path need not exist, only
	 * the file name extension is considered.
	 */
	ICompiler getCompiler(IPath sourceFile);


	/**
	 * @param language
	 *            A language
	 * @param moduleName
	 *            A language-specific module name string
	 * @return The module with that name, or null.
	 */
	IManagedResource findModule(ILanguage language, String moduleName);


	/**
	 * @param moduleName
	 *            A language-specific module identifier (AST)
	 * @return The module with that name, or null
	 */
	IManagedResource findModule(IValue moduleId);


	Collection<IPath> allModules(ILanguage language);


	Collection<IPath> allFiles();


	/**
	 * @param path
	 *            A path, either absolute or project-relative
	 * @return The language-specific module id for a module with that path.
	 */
	IConstructor getModuleId(IPath path);


	/**
	 * @param path
	 *            A path, either absolute or project-relative
	 * @return The language-specific module name string for a module with that
	 *         path.
	 */
	String getModuleName(IPath path);


	/**
	 * @return Return the underlying resource manager
	 */
	IResourceManager getResourceManager();


	/**
	 * Force refresh/reinitialization of the manager, discarding all cached
	 * data.
	 */
	void refresh();


	void addMarker(String message, ISourceLocation loc, String markerType, int severity);


	void addMarker(String message, ISourceLocation loc, String markerType);


	void addMarker(String message, ISourceLocation loc, int severity);


	void addMarker(String message, ISourceLocation loc);

}