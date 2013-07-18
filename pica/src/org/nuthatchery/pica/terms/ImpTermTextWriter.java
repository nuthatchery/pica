/**************************************************************************
 * Copyright (c) 2011-2012 Anya Helene Bagge
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
 * + Tero Hasu
 * 
 *************************************************************************/
package org.nuthatchery.pica.terms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;

// Contains some convenience methods to accompany the visitor class.
public class ImpTermTextWriter {
	private IValueVisitor<IValue, IOException> visitor;


	public ImpTermTextWriter(IValueVisitor<IValue, IOException> visitor) {
		this.visitor = visitor;
	}


	public ImpTermTextWriter(OutputStream stream) {
		this(new ImpTermTextWriterVisitor(stream, false, 2));
	}


	public IValueVisitor<IValue, IOException> getVisitor() {
		return visitor;
	}


	public void setVisitor(IValueVisitor<IValue, IOException> visitor) {
		this.visitor = visitor;
	}


	public void write(IValue value) throws IOException {
		value.accept(visitor);
	}


	// Two inefficiencies here.
	// (1) We are dealing with OutputStreams when we really want a character
	// String.
	// (2) We recreate the OutputStream with every invocation of this method,
	// and the same goes for the visitor,
	// when more typically one probably generates more text at once.
	public static String termPatternToString(IValue value) {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			ImpTermTextWriterVisitor visitor = new ImpTermTextWriterVisitor(stream, false, 2) {
				@Override
				public IValue visitConstructor(IConstructor o) throws IOException {
					if(TermAdapter.isVar(o)) {
						IString is = (IString) o.get("name");
						append(is.getValue());
						return o;
					}
					else {
						return visitNode(o);
					}
				}
			};
			new ImpTermTextWriter(visitor).write(value);
			return stream.toString();
		}
		catch(IOException ioex) { // NOPMD by anya on 1/5/12 3:43 AM
			// this never happens
		}
		return null;
	}


	public static String termToString(IValue value) {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			new ImpTermTextWriter(stream).write(value);
			return stream.toString();
		}
		catch(IOException ioex) { // NOPMD by anya on 1/5/12 3:43 AM
			// this never happens
		}
		return null;
	}

}
