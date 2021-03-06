/**************************************************************************
 * Copyright (c) 2010-2013 Anya Helene Bagge
 * Copyright (c) 2010-2013 University of Bergen
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
 *
 *************************************************************************/
package org.nuthatchery.pica.terms;

import java.io.PrintStream;
import java.util.Iterator;

import io.usethesource.vallang.IBool;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IInteger;
import io.usethesource.vallang.IList;
import io.usethesource.vallang.INode;
import io.usethesource.vallang.IString;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;
import io.usethesource.vallang.type.TypeFactory;
import org.nuthatchery.pica.IPica;
import org.nuthatchery.pica.Pica;
import org.rascalmpl.interpreter.utils.RuntimeExceptionFactory;
import org.rascalmpl.values.uptr.ITree;

public class Terms {
	@SuppressWarnings("unused")
	private static final TypeFactory types = TypeFactory.getInstance();
	private final IValueFactory vf;


	public Terms(IValueFactory values) {
		super();

		this.vf = values;
	}


	public IValue fromValue(IValue v, @SuppressWarnings("unused") IValue t) {
		return v;
	}


	public IConstructor implode(IConstructor tree) {
		if(tree instanceof ITree)
			return TermImploder.implodeTree((ITree) tree);
		else
			throw new UnsupportedOperationException();
	}


	public INode setChild(INode n, IInteger i, IValue v) {
		return n.set(i.intValue(), v);
	}


	public void termPrintln(IList l) {
		StringBuilder b = new StringBuilder();
		Iterator<IValue> valueIterator = l.iterator();
		while(valueIterator.hasNext()) {
			IValue arg = valueIterator.next();

			if(arg.getType().isString()) {
				b.append(((IString) arg).getValue());
			}
			else {
				b.append(TermAdapter.yieldTerm(arg, false));
			}
		}

		Pica.get().println(b.toString());
	}


	public IString unparse(IConstructor tree) {
		return vf.string(TermAdapter.yield(tree));
	}


	public IString yieldTerm(IValue tree, IBool withAnnos) {
		return vf.string(TermAdapter.yieldTerm(tree, withAnnos.getValue()));
	}


	public IString yieldTermPattern(IValue tree) {
		return vf.string(ImpTermTextWriter.termPatternToString(tree));
	}
}
