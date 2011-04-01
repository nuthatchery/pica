package org.magnolialang.terms;

import static org.magnolialang.terms.TermFactory.ts;
import static org.magnolialang.terms.TermFactory.vf;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.imp.pdb.facts.*;
import org.eclipse.imp.pdb.facts.io.PBFReader;
import org.magnolialang.eclipse.MagnoliaFileLocator;
import org.magnolialang.errors.ImplementationError;

public class SkinTable {
	private final String tableName;
	private IMap table;

	public SkinTable(final String tableName) {
		this.tableName = tableName;
		loadTable();
	}

	private void loadTable() {
		final PBFReader reader = new PBFReader();
		InputStream stream = null;
		final IPath path = new Path("src/org/magnolialang/syntax/" + tableName);
		try {
			stream = MagnoliaFileLocator.openStream(path);
			ISet rel = (ISet) reader.read(vf, ts, null, stream);
			IMapWriter pp = vf.mapWriter(TermFactory.tf.stringType(),
					TermFactory.tf.listType(TermFactory.Type_XaToken));
			for(IValue x : rel) {
				ITuple prod = (ITuple) x;
				pp.put((vf.string(((IString) prod.get(0)).getValue() + "/"
						+ ((IInteger) prod.get(1)).intValue())), vf.tuple(
						prod.get(2), vf.string(prod.get(4).toString()),
						prod.get(3)));
			}
			table = pp.done();

		}
		catch(final IOException e) {
			throw new ImplementationError("Unable to read pretty print table "
					+ tableName + " from lang/" + path, e);
		}
		finally {
			if(stream != null)
				try {
					stream.close();
				}
				catch(final IOException e) {
				}
		}
	}

	public IList getConcrete(final String consname) {
		final ITuple entry = getEntry(consname);
		if(entry != null)
			return (IList) entry.get(0);
		else
			return null;
	}

	public String getSyntax(final String consname) {
		final ITuple entry = getEntry(consname);
		if(entry != null)
			return ((IString) entry.get(1)).getValue();
		else
			return null;
	}

	public String getSort(final String consname) {
		final ITuple entry = getEntry(consname);
		if(entry != null)
			return ((IString) entry.get(2)).getValue();
		else
			return null;
	}

	public ITuple getEntry(final String consname) {
		final IValue entry = table.get(vf.string(consname));
		if(entry != null && entry instanceof ITuple)
			return (ITuple) entry;
		else
			return null;

	}
}
