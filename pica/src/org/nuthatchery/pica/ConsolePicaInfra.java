/**************************************************************************
 * Copyright (c) 2012 Anya Helene Bagge
 * Copyright (c) 2012 Tero Hasu
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
 * * Tero Hasu
 *
 *************************************************************************/
package org.nuthatchery.pica;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.errors.Severity;
import org.nuthatchery.pica.resources.ILanguage;
import org.nuthatchery.pica.resources.IWorkspaceConfig;
import org.nuthatchery.pica.resources.IWorkspaceManager;
import org.nuthatchery.pica.resources.handles.IResourceHandle;
import org.rascalmpl.debug.IRascalMonitor;
import org.rascalmpl.interpreter.NullRascalMonitor;
import org.rascalmpl.uri.UnsupportedSchemeException;

/**
 * For use in a static setting, where the input files do not change.
 * Any user feedback is output to the console.
 */
public final class ConsolePicaInfra extends AbstractPicaInfra {
	private static List<String> magnoliaSearchPath = Collections.emptyList();


	/** Returns null if not found from the search path. */
	@Nullable
	public static File findModuleFile(IConstructor nameAst, ILanguage lang) {
		String modName = lang.getNameString(nameAst);
		//System.err.println("---modName is " + modName);
		for(String loadPath : magnoliaSearchPath) {
			//System.err.println("---checking load path " + loadPath);
			String relPath = lang.getFileName(modName);
			//System.err.println("---relPath is " + relPath);
			File file = new File(loadPath, relPath);
			//System.err.println("---looking for file " + file);
			if(file.exists()) {
				//System.err.println("produce ModuleResource " + file + " for " + modName);
				return file;
			}
		}
		return null;
	}


	public static ConsolePicaInfra getInfra() {
		return (ConsolePicaInfra) Pica.get();
	}


	public static List<String> getMagnoliaSearchPath() {
		return magnoliaSearchPath;
	}


	/**
	 * This always sets a fresh instance, even if already set.
	 * This is to help get the system in a known state, e.g.
	 * for repeatable testing (cf. test fixture).
	 * (A caveat is that there is a lot of mutable static final state in the
	 * rest of the system which is not easily reset.)
	 */
	public static void setInfra(IWorkspaceConfig config) {
		Pica.set(new ConsolePicaInfra(config));
	}


	public static void setMagnoliaSearchPath(List<String> magnoliaSearchPath) {
		ConsolePicaInfra.magnoliaSearchPath = magnoliaSearchPath;
	}


	private final IRascalMonitor rm = new NullRascalMonitor();


	// cache of generated parsers
//	private final Map<String, Class<IGTD<IConstructor, IConstructor, ISourceLocation>>>	parserClasses		= new HashMap<String, Class<IGTD<IConstructor, IConstructor, ISourceLocation>>>();


	public ConsolePicaInfra(IWorkspaceConfig config) {
		super(config);
	}


	@Override
	public boolean areModuleFactsPreloaded() {
		return false;
	}


	public IRascalMonitor getMonitor() {
		return rm;
	}


	/**
	 * @param uri
	 *            The URI of the desired file
	 * @return An IFile representing the URI
	 */
	@Override
	@Nullable
	public IResourceHandle getResourceHandle(URI uri) {
//		IPath path = null;
//		try {
		throw new UnsupportedOperationException();
//path = new Path(new File(Pica.getResolverRegistry().getResourceURI(uri)).getAbsolutePath());
//		}
//		catch(UnsupportedSchemeException e) {
//			Pica.get().logException(e.getMessage(), e);
//			e.printStackTrace();
//			return null;
//		}
//		catch(IOException e) {
//			Pica.get().logException(e.getMessage(), e);
//			e.printStackTrace();
//			return null;
//		}
//		return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
	}


	@Override
	public IWorkspaceManager getWorkspaceManager() {
		throw new UnsupportedOperationException();
	}

}
