package org.magnolialang.terms;

import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IValue;

public class CxxSkin extends MagnoliaSkin implements ILanguageSkin {
	SkinTable table = new SkinTable("MagnoliaCxxSkinTable.pbf");

	public IList getConcrete(String cons, String sort, int arity, IValue context) {
		 IList concrete = table.getConcrete(cons + "/" + arity + ":" + sort);
		 if(concrete != null)
			 return concrete;
		 else
			 return super.getConcrete(cons, sort, arity, context);
	}


}
