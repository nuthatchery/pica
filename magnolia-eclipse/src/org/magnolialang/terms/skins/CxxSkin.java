package org.magnolialang.terms.skins;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IValue;

public class CxxSkin extends MagnoliaSkin {
	private final SkinTable	table	= new SkinTable("MagnoliaCxxSkinTable.pbf");


	@Override
	public IList getConcrete(IConstructor cons, IValue context) {
		IList concrete = table.getConcrete(cons.getName() + "/" + cons.arity());
		if(concrete == null)
			return super.getConcrete(cons, context);
		else
			return concrete;
	}


	@Override
	public IList getConcrete(String cons, int arity, IValue context) {
		IList concrete = table.getConcrete(cons + "/" + arity);
		if(concrete == null)
			return super.getConcrete(cons, arity, context);
		else
			return concrete;
	}

}
