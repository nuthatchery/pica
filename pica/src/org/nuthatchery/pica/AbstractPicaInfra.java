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
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.imp.pdb.facts.IValue;
import org.nuthatchery.pica.errors.Severity;
import org.nuthatchery.pica.rascal.IEvaluatorPool;
import org.nuthatchery.pica.resources.IWorkspaceConfig;
import org.rascalmpl.interpreter.Evaluator;

public abstract class AbstractPicaInfra implements IPica, IEvaluatorFactory {
	private final Map<List<String>, IEvaluatorPool> pools = new HashMap<List<String>, IEvaluatorPool>();
	protected final IWorkspaceConfig config;


	public AbstractPicaInfra(IWorkspaceConfig config) {
		this.config = config;
		Pica.set(this);
	}


	@Override
	public IWorkspaceConfig getConfig() {
		return config;
	}


	@Override
	public IEvaluatorFactory getEvaluatorFactory() {
		return this;
	}


	@Override
	public IEvaluatorPool getEvaluatorPool(String name, List<String> imports) {
		IEvaluatorPool pool = pools.get(imports);
		if(pool == null) {
			pool = makeEvaluatorPool(name, imports);
			pools.put(new ArrayList<String>(imports), pool);
		}
		return pool;
	}


	public String getRascalClassPath() {
		String path = "";
		URL rascalPath = Evaluator.class.getResource("/");
		System.err.println("rascalPath: " + rascalPath);
		if(rascalPath != null) {
			path = rascalPath.toString();
		}

		URL valuesPath = IValue.class.getResource("/");
		System.err.println("valuesPath: " + valuesPath);
		if(valuesPath != null) {
			path += File.pathSeparator + valuesPath.toString();
		}

		String property = System.getProperty("java.class.path");
		if(property != null)
			path += File.pathSeparator + property;

		System.err.println("rascalClassPath: " + path);
		return path;
	}


	@Override
	public void logMessage(String msg) {
		logMessage(msg, Severity.DEFAULT);
	}


	@Override
	public Evaluator makeEvaluator() {
		return makeEvaluator(new PrintWriter(System.out), new PrintWriter(System.err));
	}


	@Override
	public void refresh() {
		for(IEvaluatorPool pool : pools.values()) {
			pool.reload();
		}
	}

}
