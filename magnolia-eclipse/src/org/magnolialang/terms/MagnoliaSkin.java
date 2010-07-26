package org.magnolialang.terms;

import java.util.Map;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IValue;

import static org.magnolialang.terms.TermFactory.*;
public class MagnoliaSkin implements ILanguageSkin {
	private static final Map<String,IList> table = MagnoliaSkinTable.getMap();
	
	public boolean isVertical(String cons, String sort, int arity, IValue context) {
		if(sort != null && (sort.equals("Stat") || sort.equals("Decl")))
			return true;
		return false;
	}

	public IList getConcrete(String cons, String sort, int arity, IValue context) {
		return table.get(cons + "/" + arity);
	}

	public IConstructor getListSep(String sort, IValue context) {
		if(sort != null && (sort.equals("Expr") || sort.equals("Type")))
			return token(", ");
		return null;
	}

}
