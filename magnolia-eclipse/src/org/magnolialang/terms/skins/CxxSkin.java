package org.magnolialang.terms.skins;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IValue;

public class CxxSkin extends MagnoliaSkin implements ILanguageSkin {
	private final SkinTable table = new SkinTable("MagnoliaCxxSkinTable.pbf");

	@Override
	public IList getConcrete(String cons, int arity, IValue context) {
		IList concrete = table.getConcrete(cons + "/" + arity);
		if(concrete != null)
			return concrete;
		else
			return super.getConcrete(cons, arity, context);
	}

	@Override
	public IList getConcrete(IConstructor cons, IValue context) {
		IList concrete = table.getConcrete(cons.getName() + "/" + cons.arity());
		if(concrete != null)
			return concrete;
		else
			return super.getConcrete(cons, context);
	}

}
