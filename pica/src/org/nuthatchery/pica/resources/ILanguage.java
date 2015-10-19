/**************************************************************************
 * Copyright (c) 2010-2012 Anya Helene Bagge
 * Copyright (c) 2010-2012 Tero Hasu
 * Copyright (c) 2010-2012 University of Bergen
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
 * * Tero Hasu
 *
 *************************************************************************/
package org.nuthatchery.pica.resources;

import java.util.Collection;

import org.eclipse.jdt.annotation.Nullable;

public interface ILanguage {


	/**
	 * @return A collection of valid extensions for the language, not including
	 *         the dot
	 */
	Collection<String> getExtensions();


	/** The opposite of 'getModuleName'. */
	String getFileName(String modName);


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
	Object getNameAST(String name);


	/**
	 * @param nameAST
	 *            An AST representation of a name
	 * @return The string representation of the same name
	 * @throws IllegalArgumentException
	 *             if argument is not a name
	 */
	String getNameString(Object nameAST);


	/**
	 * @return The main/preferred file name extension, not including the dot
	 */
	String getPreferredExtension();


	/**
	 * @return The file name extension used when storing compiled files / fact
	 *         files for this language. Null if this isn't supported or
	 *         shouldn't be done for this language.
	 */
	@Nullable
	String getStoreExtension();


	/**
	 * @param ext
	 *            filename extension, with or without dot
	 * @return True if 'ext' is a valid filename extension for this language
	 */
	boolean hasExtension(String ext);
}
