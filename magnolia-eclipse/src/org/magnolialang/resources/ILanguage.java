package org.magnolialang.resources;

import java.util.Collection;

import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.magnolialang.compiler.ICompiler;
import org.magnolialang.load.ModuleParser;

public interface ILanguage {
	/**
	 * @return User-visible language name
	 */
	public String getName();

	/**
	 * @return Identifying language name
	 */
	public String getId();

	/**
	 * @return The main/preferred file name extension, not including the dot
	 */
	public String getPreferredExtension();

	/**
	 * @return A collection of valid extensions for the language, not including
	 *         the dot
	 */
	public Collection<String> getExtensions();

	/**
	 * @param ext
	 *            filename extension, with or without dot
	 * @return True if 'ext' is a valid filename extension for this language
	 */
	public boolean hasExtension(String ext);

	/**
	 * @return A parser for the language
	 */
	public ModuleParser getParser();

	/**
	 * @param path
	 *            A path relative to a source folder. The source file need not
	 *            exist
	 * @return The canonical module name corresponding to the path, or null if
	 *         none
	 */
	public String getModuleName(IPath path);

	/**
	 * @param moduleName
	 *            A module name; module need not exist
	 * @return A source-folder relative path to the module, including preferred
	 *         extension
	 */
	public IPath getModulePath(String moduleName);

	/**
	 * @param name
	 *            A string representation of a name
	 * @return The AST representation of the same name
	 */
	public IConstructor getNameAST(String name);

	/**
	 * @param nameAST
	 *            An AST representation of a name
	 * @return The string representation of the same name
	 */
	public String getNameString(IConstructor nameAST);

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object o);

	ICompiler makeCompiler(IModuleManager manager);

}