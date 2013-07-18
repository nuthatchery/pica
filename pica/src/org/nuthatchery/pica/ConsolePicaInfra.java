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
package org.nuthatchery.pica;

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.nuthatchery.pica.errors.Severity;
import org.nuthatchery.pica.rascal.ConsoleEvaluatorPool;
import org.nuthatchery.pica.rascal.IEvaluatorPool;
import org.nuthatchery.pica.resources.ILanguage;
import org.nuthatchery.pica.resources.IWorkspaceConfig;
import org.nuthatchery.pica.resources.IWorkspaceManager;
import org.nuthatchery.pica.terms.TermFactory;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.IRascalMonitor;
import org.rascalmpl.interpreter.NullRascalMonitor;
import org.rascalmpl.interpreter.env.GlobalEnvironment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.load.RascalURIResolver;
import org.rascalmpl.uri.URIResolverRegistry;

/**
 * For use in a static setting, where the input files do not change.
 * Any user feedback is output to the console.
 */
public final class ConsolePicaInfra extends AbstractPicaInfra {
	private static List<String> magnoliaSearchPath = Collections.emptyList();

	private final IRascalMonitor rm = new NullRascalMonitor();

	protected static final String parserPackageName = "org.rascalmpl.java.parser.object";


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


	@Override
	public IWorkspaceManager getWorkspaceManager() {
		return null;
	}


	@Override
	public void logException(String msg, Throwable t) {
		System.err.println("error: " + (t != null ? t.toString() : "no exception") + " (" + (msg != null ? msg : "no details") + ")");
		if(t != null) {
			t.printStackTrace();
		}
	}


	// cache of generated parsers
//	private final Map<String, Class<IGTD<IConstructor, IConstructor, ISourceLocation>>>	parserClasses		= new HashMap<String, Class<IGTD<IConstructor, IConstructor, ISourceLocation>>>();

	@Override
	public void logMessage(String msg, Severity severity) {
		System.err.println(severity.toString() + ": " + msg);
	}


	@Override
	public Evaluator makeEvaluator(PrintWriter out, PrintWriter err) {
		GlobalEnvironment heap = new GlobalEnvironment();
		ModuleEnvironment root = heap.addModule(new ModuleEnvironment("***magnolia***", heap));

		//List<ClassLoader> loaders = Arrays.asList(getClass().getClassLoader(), Evaluator.class.getClassLoader(), RascalScriptInterpreter.class.getClassLoader());
		List<ClassLoader> loaders = Arrays.asList(getClass().getClassLoader(), Evaluator.class.getClassLoader());
		URIResolverRegistry registry = new URIResolverRegistry();
		RascalURIResolver resolver = new RascalURIResolver(registry);
		//ClassResourceInputOutput eclipseResolver = new ClassResourceInputOutput(registry, "eclipse-std", RascalScriptInterpreter.class, "/org/rascalmpl/eclipse/library");
		//registry.registerInput(eclipseResolver);
		Evaluator eval = new Evaluator(TermFactory.vf, err, out, root, heap, loaders, resolver); // URIResolverRegistry
		// specifies one possible mapping to a rascal:// URI
		eval.addRascalSearchPath(new File(".", "src").toURI());
		for(URI uri : config.moreRascalSearchPath()) {
			eval.addRascalSearchPath(uri);
		}
		String property = config.moreRascalClassPath();
		if(property != null) {
			eval.getConfiguration().setRascalJavaClassPathProperty(property);
		}

		return eval;
	}


	@Override
	public IEvaluatorPool makeEvaluatorPool(String name, List<String> imports) {
		return new ConsoleEvaluatorPool(name, imports);
	}


	/** Returns null if not found from the search path. */
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

}
