package org.magnolialang.terms;

import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IValue;

public class CxxSkin extends MagnoliaSkin implements ILanguageSkin {
	private SkinTable table = new SkinTable("MagnoliaCxxSkinTable.pbf");

	@Override
	public IList getConcrete(final String cons, final String sort,
			final int arity, final IValue context) {
		final IList concrete = table.getConcrete(cons + "/" + arity + ":"
				+ sort);
		if(concrete != null)
			return concrete;
		else
			return super.getConcrete(cons, sort, arity, context);
	}

}
