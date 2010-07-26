package org.magnolialang.terms;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.imp.pdb.facts.*;

import static org.magnolialang.terms.TermFactory.vf;
import static org.magnolialang.terms.TermFactory.ts;
import org.eclipse.imp.pdb.facts.io.PBFReader;
import org.magnolialang.eclipse.MagnoliaPlugin;
import org.magnolialang.errors.ImplementationError;

public class SkinTable {
	private final String tableName;
	private IMap table;
	
	public SkinTable(String tableName) {
		this.tableName = tableName;
		loadTable();
	}
	
	private void loadTable() {
		PBFReader reader = new PBFReader();
		InputStream stream = null;
		IPath path =  new Path("src/" + tableName);
		try {
			stream = FileLocator.openStream(MagnoliaPlugin.metaxaBundle, path, false);
			table = (IMap)reader.read(vf, ts, null, stream);
		}
		catch(IOException e) {
			throw new ImplementationError("Unable to read pretty print table " + tableName + " from bundle://MetaXa/" + path, e);
		}
		finally {
			if(stream != null)
				try {
					stream.close();
				}
				catch(IOException e) {
				}
		}
	}
	
	public IList getConcrete(String consname) {
		ITuple entry = getEntry(consname);
		if(entry != null)
			return (IList) entry.get(0);
		else
			return null;
	}

	public String getSyntax(String consname) {
		ITuple entry = getEntry(consname);
		if(entry != null)
			return ((IString) entry.get(1)).getValue();
		else
			return null;
	}

	public String getSort(String consname) {
		ITuple entry = getEntry(consname);
		if(entry != null)
			return ((IString) entry.get(2)).getValue();
		else
			return null;
	}

	public ITuple getEntry(String consname) {
		IValue entry = table.get(vf.string(consname));
		if(entry != null && entry instanceof ITuple) {
			return (ITuple)entry;
		}
		else
			return null;
		
	}
}
