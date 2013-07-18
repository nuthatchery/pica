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

import org.eclipse.imp.pdb.facts.IBool;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.INode;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.type.TypeFactory;

import org.rascalmpl.interpreter.utils.RuntimeExceptionFactory;

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
		return TermImploder.implodeTree(tree);
	}


	public INode setChild(INode n, IInteger i, IValue v) {
		return n.set(i.intValue(), v);
	}


	public void termPrintln(IList l) {
		PrintStream currentOutStream = System.out;

		synchronized(currentOutStream) {
			try {
				Iterator<IValue> valueIterator = l.iterator();
				while(valueIterator.hasNext()) {
					IValue arg = valueIterator.next();

					if(arg.getType().isString()) {
						currentOutStream.print(((IString) arg).getValue());
					}
					else {
						currentOutStream.print(TermAdapter.yieldTerm(arg, false));
					}
				}
				currentOutStream.println();
			}
			finally {
				currentOutStream.flush();
			}
		}
	}


	public IString unparse(IConstructor tree, IString skin, IBool fallback) {
		// System.err.println(TermAdapter.yieldTerm(tree, false));
		if(skin.getValue().equals(""))
			return vf.string(TermAdapter.yield(tree));
		else
			throw RuntimeExceptionFactory.illegalArgument(skin, null, null);
	}


	public IString yieldTerm(IValue tree, IBool withAnnos) {
		return vf.string(TermAdapter.yieldTerm(tree, withAnnos.getValue()));
	}


	public IString yieldTermPattern(IValue tree) {
		return vf.string(ImpTermTextWriter.termPatternToString(tree));
	}
}
