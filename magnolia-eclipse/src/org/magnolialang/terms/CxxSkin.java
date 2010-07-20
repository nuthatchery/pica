package org.magnolialang.terms;

import java.util.Map;

import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IValue;

public class CxxSkin extends MagnoliaSkin implements ILanguageSkin {
	private static final Map<String,IList> table = MagnoliaCxxSkinTable.getMap();

	public IList getConcrete(String cons, String sort, int arity, IValue context) {
		 IList concrete = table.get(cons + "/" + arity + ":" + sort);
		 if(concrete != null)
			 return concrete;
		 else
			 return super.getConcrete(cons, sort, arity, context);
	}


}
