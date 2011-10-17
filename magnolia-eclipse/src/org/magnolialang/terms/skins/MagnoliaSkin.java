package org.magnolialang.terms.skins;

import static org.magnolialang.terms.TermFactory.token;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IValue;

public class MagnoliaSkin implements ILanguageSkin {
	private final SkinTable table = new SkinTable("MagnoliaInfo.pbf");
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.magnolialang.terms.ILanguageSkin#isVertical(java.lang.String,
	 * int, org.eclipse.imp.pdb.facts.IValue)
	 */
	@Override
	public boolean isVertical(String cons, int arity, IValue context) {
		String sort = table.getSort(cons + "/" + arity);
		return VERTICALS.contains(sort);
	}

	@Override
	public IList getConcrete(String cons, int arity, IValue context) {
		return table.getConcrete(cons + "/" + arity);
	}

	@Override
	public IConstructor getListSep(final String sort, final IValue context) {
		if(sort != null && (sort.equals("Expr") || sort.equals("Type")))
			return token(", ");
		return null;
	}

	@Override
	public IList getConcrete(IConstructor cons, IValue context) {
		return table.getConcrete(cons.getName() + "/" + cons.arity());
	}

	@Override
	public boolean isVertical(IConstructor cons, IValue context) {
		return VERTICALS.contains(table.getSort(cons.getName() + "/"
				+ cons.arity()));
	}

}