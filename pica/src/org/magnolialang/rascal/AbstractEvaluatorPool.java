/**************************************************************************
 * Copyright (c) 2012 Anya Helene Bagge
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
 * 
 *************************************************************************/
package org.magnolialang.rascal;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.imp.pdb.facts.IValue;
import org.magnolialang.infra.Infra;
import org.magnolialang.terms.TermFactory;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.IRascalMonitor;

public abstract class AbstractEvaluatorPool implements IEvaluatorPool {

	private final List<String> imports;
	protected final String jobName;


	public AbstractEvaluatorPool(String jobName, List<String> imports) {
		super();
		this.jobName = jobName;
		this.imports = Collections.unmodifiableList(new ArrayList<String>(imports));

	}


	@Override
	public synchronized IValue call(IRascalMonitor rm, String funName, IValue... args) {
		return getEvaluator().call(rm, funName, args);
	}


	@Override
	public synchronized IValue call(String funName, IValue... args) {
		return getEvaluator().call(funName, args);
	}


	protected abstract Evaluator getEvaluator();


	protected Evaluator makeEvaluator(IRascalMonitor rm) {
		rm.startJob("Loading " + jobName, 10 + imports.size() * 10);
		PrintWriter stderr = new PrintWriter(System.err);
		rm.event(5);
		Evaluator evaluator = Infra.get().getEvaluatorFactory().makeEvaluator(stderr, stderr);
		rm.event(5);
		evaluator.getCurrentEnvt().getStore().importStore(TermFactory.ts);
		for(String imp : imports) {
			rm.event("Importing " + imp, 10);
			evaluator.doImport(rm, imp);
		}
		rm.endJob(true);

		return evaluator;
	}
}
