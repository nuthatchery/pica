package org.magnolialang.terms;

import static org.magnolialang.terms.TermFactory.token;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IValue;

public class MagnoliaSkin implements ILanguageSkin {
	private SkinTable table = new SkinTable("MagnoliaSkinTable.pbf");
	private static final Set<String> VERTICALS = new HashSet<String>();
	static {
		VERTICALS.add("Stat");
		VERTICALS.add("Decl");
		VERTICALS.add("DeclNS");
		VERTICALS.add("TopDecl");
		VERTICALS.add("ModuleHead");
		VERTICALS.add("StatDefBodyS");
		VERTICALS.add("StatDefBodyNS");
	}

	public boolean isVertical(final String cons, String sort, final int arity,
			final IValue context) {
		if(sort == null)
			sort = table.getSort(cons + "/" + arity);
		return VERTICALS.contains(sort);
	}

	public IList getConcrete(final String cons, final String sort,
			final int arity, final IValue context) {
		return table.getConcrete(cons + "/" + arity);
	}

	public IConstructor getListSep(final String sort, final IValue context) {
		if(sort != null && (sort.equals("Expr") || sort.equals("Type")))
			return token(", ");
		return null;
	}

}
