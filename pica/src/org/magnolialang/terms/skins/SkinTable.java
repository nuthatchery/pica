/**************************************************************************
 * Copyright (c) 2010-2013 Anya Helene Bagge
 * Copyright (c) 2010-2013 Tero Hasu
 * Copyright (c) 2010-2013 University of Bergen
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
package org.magnolialang.terms.skins;

import static org.magnolialang.terms.TermFactory.*;

import java.io.InputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IMapWriter;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.io.BinaryValueReader;
import org.magnolialang.errors.ImplementationError;
import org.magnolialang.infra.Infra;
import org.magnolialang.terms.TermFactory;

public class SkinTable {
	private final String tableName;
	private IMap table;


	public SkinTable(final String tableName) {
		this.tableName = tableName;
		loadTable();
	}


	public IList getConcrete(final String consname) {
		final ITuple entry = getEntry(consname);
		if(entry == null)
			return null;
		else
			return (IList) entry.get(0);
	}


	public ITuple getEntry(final String consname) {
		final IValue entry = table.get(vf.string(consname));
		if(entry instanceof ITuple)
			return (ITuple) entry;
		else
			return null;

	}


	public String getSort(final String consname) {
		final ITuple entry = getEntry(consname);
		if(entry == null)
			return null;
		else
			return ((IString) entry.get(2)).getValue();
	}


	public String getSyntax(final String consname) {
		final ITuple entry = getEntry(consname);
		if(entry == null)
			return null;
		else
			return ((IString) entry.get(1)).getValue();
	}


	private void loadTable() {
		final BinaryValueReader reader = new BinaryValueReader();
		final IPath path = new Path("org/magnolialang/syntax/" + tableName);
		try {
			InputStream stream = Infra.get().openStream(path.toString());
			try {
				ISet rel = (ISet) reader.read(vf, ts, null, stream);
				IMapWriter pp = vf.mapWriter(TermFactory.tf.stringType(), TermFactory.tf.listType(TermFactory.Type_XaToken));
				for(IValue x : rel) {
					ITuple prod = (ITuple) x;
					pp.put(vf.string(((IString) prod.get(0)).getValue() + "/" + ((IInteger) prod.get(1)).intValue()), vf.tuple(prod.get(2), vf.string(prod.get(4).toString()), prod.get(3)));
				}
				table = pp.done();
			}
			finally {
				stream.close();
			}

		}
		catch(Exception e) {
			throw new ImplementationError("Unable to read pretty print table " + tableName + " from lang/" + path, e);
		}
	}
}
