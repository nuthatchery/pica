/**************************************************************************
 * Copyright (c) 2011-2012 Anya Helene Bagge
 * Copyright (c) 2011-2012 Tero Hasu
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
 * * Tero Hasu
 * 
 *************************************************************************/
package org.nuthatchery.pica.rascal;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.nuthatchery.pica.Pica;
import org.nuthatchery.pica.errors.ParserLoadError;
import org.nuthatchery.pica.errors.ParserNotFoundError;
import org.nuthatchery.pica.parsergen.GenerateParser;
import org.rascalmpl.parser.gtd.IGTD;

/**
 * This class provides utility methods for accessing the Rascal parser
 * generator.
 * 
 * The class keeps a cache of loaded parsers, and will load parsers on
 * the fly if the parser file is updated.
 * 
 * @author anya
 * 
 */
public final class RascalParserLoader {
	private static final Map<String, ParserEntry> modules = new HashMap<>();
	private boolean checkTimeStamps;
	private URLClassLoader loader = null;


	/**
	 * Instantiate the loader.
	 * 
	 * @param watchForUpdates
	 *            True if the loader should watch the parser files and reload on
	 *            updates
	 */
	public RascalParserLoader(boolean watchForUpdates) {
		Pica.get().getEvaluatorFactory().makeEvaluator();
		checkTimeStamps = watchForUpdates;
	}


	/**
	 * Return a parser for the given grammar. The generated parser should
	 * be somewhere in the class path.
	 * 
	 * 
	 * @param moduleName
	 *            Rascal module name for the grammar
	 * @return A new parser instance
	 * @throws ParserLoadError
	 *             if the parser could not be loaded
	 * @throws ParserNotFoundError
	 *             if the parser could not be found
	 */
	public synchronized IGTD<IConstructor, IConstructor, ISourceLocation> getParser(String moduleName) {
		ParserEntry entry = modules.get(moduleName);

		if(entry == null) {
			entry = new ParserEntry();
			modules.put(moduleName, entry);

			URL url = getParserURL(moduleName, ".jar");
			if(url != null) {
				entry.url = url;
				try {
					entry.parserClass = loadParser(url, moduleName);
					entry.lastModified = getLastModified(url);
				}
				catch(ClassNotFoundException | IOException e) {
					throw new ParserLoadError(moduleName, e);
				}
			}
		}

		if(checkTimeStamps && entry.url != null) {
			if(getLastModified(entry.url) > entry.lastModified) {
				try {
					System.err.println("Reloading: " + moduleName);
					Class<IGTD<IConstructor, IConstructor, ISourceLocation>> parser = loadParser(entry.url, moduleName);
					entry.parserClass = parser;
					entry.lastModified = getLastModified(entry.url);
				}
				catch(ClassNotFoundException | IOException e) {
					throw new ParserLoadError(moduleName, e);
				}
			}
		}

		if(entry.parserClass != null) {
			try {
				return entry.parserClass.newInstance();
			}
			catch(InstantiationException e) {
				throw new ParserLoadError("Failed to instantiate " + moduleName, e);
			}
			catch(IllegalAccessException e) {
				throw new ParserLoadError("Failed to instantiate " + moduleName, e);
			}
		}

		if(entry.url == null) {
			throw new ParserNotFoundError(moduleName);
		}
		else {
			throw new ParserLoadError(moduleName);
		}
	}


	private long getLastModified(URL url) {
		try {
			URLConnection openConnection = url.openConnection();
			long lastModified = openConnection.getLastModified();
			return lastModified;
		}
		catch(IOException e) {
		}
		return 0L;
	}


	private URL getParserURL(String moduleName, String suffix) {
		String fileName = moduleName.replace("::", "/") + suffix;

		return Pica.get().getConfig().getParserClassLoader().getResource(fileName);
	}


	/**
	 * Load a parser from disk.
	 * 
	 * @param jarFileURL
	 *            url of jar fil containing parser
	 * @param moduleName
	 *            name of Rascal grammar module
	 * @return A parser class
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private Class<IGTD<IConstructor, IConstructor, ISourceLocation>> loadParser(final URL jarFileURL, String moduleName) throws ClassNotFoundException, IOException {
		String normName = moduleName.replaceAll("::", "_").replaceAll("\\\\", "_");
		String clsName = GenerateParser.parserPackageName + "." + normName;

		final URL[] urls = new URL[] { jarFileURL };
		if(loader != null) {
			loader.close();
		}
		loader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
			@Override
			public URLClassLoader run() {
				return new URLClassLoader(urls, Pica.get().getConfig().getParserClassLoader());
			}
		});

		Class<IGTD<IConstructor, IConstructor, ISourceLocation>> parserClass = (Class<IGTD<IConstructor, IConstructor, ISourceLocation>>) loader.loadClass(clsName);
		return parserClass;
	}


	/**
	 * Force a reload of a particular parser.
	 * 
	 * 
	 * @param moduleName
	 *            Rascal module name for the grammar
	 */
	public static void clearParser(String moduleName) {
		modules.remove(moduleName);
	}


	/**
	 * Force a reload of all parsers.
	 */
	public static void refresh() {
		modules.clear();
	}


	static class ParserEntry {
		Class<IGTD<IConstructor, IConstructor, ISourceLocation>> parserClass = null;
		long lastModified = 0L;
		URL url = null;
	}
}
