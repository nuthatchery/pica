package org.magnolialang.resources;

import java.util.Collection;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.magnolialang.compiler.ICompiler;
import org.magnolialang.load.ModuleParser;
import org.magnolialang.nullness.Nullable;

public interface ILanguage {
	@Override
	boolean equals(@Nullable Object o);


	ICompiler getCompiler();


	/**
	 * @return A collection of valid extensions for the language, not including
	 *         the dot
	 */
	Collection<String> getExtensions();


	/**
	 * @return Identifying language name
	 */
	String getId();


	/**
	 * @param path
	 *            A path relative to a source folder. The source file need not
	 *            exist
	 * @return The canonical module name corresponding to the path, or null if
	 *         none
	 *         TODO: should this really be nullable?
	 */
	@Nullable
	String getModuleName(String fileName);


	/**
	 * @return User-visible language name
	 */
	String getName();


	/**
	 * @param name
	 *            A string representation of a name
	 * @return The AST representation of the same name
	 * @throws IllegalArgumentException
	 *             if argument is not a syntactically valid name
	 */
	IConstructor getNameAST(String name);


	/**
	 * @param nameAST
	 *            An AST representation of a name
	 * @return The string representation of the same name
	 * @throws IllegalArgumentException
	 *             if argument is not a name
	 */
	String getNameString(IConstructor nameAST);


	/**
	 * @return A parser for the language
	 */
	ModuleParser getParser();


	/**
	 * @return The main/preferred file name extension, not including the dot
	 */
	String getPreferredExtension();


	/**
	 * @return The file name extension used when storing compiled files / fact
	 *         files for this language. Null if this isn't supported or
	 *         shouldn't be done for this language.
	 */
	String getStoreExtenstion();


	/**
	 * @param ext
	 *            filename extension, with or without dot
	 * @return True if 'ext' is a valid filename extension for this language
	 */
	boolean hasExtension(String ext);


	@Override
	int hashCode();

}
