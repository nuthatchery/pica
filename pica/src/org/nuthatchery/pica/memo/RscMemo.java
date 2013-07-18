/**************************************************************************
 * Copyright (c) 2012-2013 Anya Helene Bagge
 * Copyright (c) 2012-2013 University of Bergen
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
package org.nuthatchery.pica.memo;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.interpreter.result.ICallableValue;
import org.rascalmpl.interpreter.staticErrors.NonWellformedType;
import org.rascalmpl.values.ValueFactoryFactory;

public class RscMemo {
	private final IValueFactory vf;


	public RscMemo() {
		this(ValueFactoryFactory.getValueFactory());
	}


	public RscMemo(IValueFactory vf) {
		this.vf = vf;
	}


	public IValue memo(IValue fun, IEvaluatorContext ctx) {
		if(true)
			return fun;
		if(fun instanceof ICallableValue) {
			ICallableValue callable = (ICallableValue) fun;
			callable.getEval().getStdErr().println(fun.getClass().getCanonicalName());
			ICallableValue memoCallable = new CallableMemo(callable, new MemoContext());
			return memoCallable;
		}
		else
			throw new NonWellformedType("Expected callable argument", ctx.getCurrentAST());
	}
}
