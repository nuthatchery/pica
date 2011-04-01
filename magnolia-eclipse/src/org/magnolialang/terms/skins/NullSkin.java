package org.magnolialang.terms.skins;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IValue;

public class NullSkin implements ILanguageSkin {

	@Override
	public boolean isVertical(String cons, int arity, IValue context) {
		return false;
	}

	@Override
	public IList getConcrete(String cons, int arity, IValue context) {
		return null;
	}

	@Override
	public boolean isVertical(IConstructor cons, IValue context) {
		return false;
	}

	@Override
	public IList getConcrete(IConstructor cons, IValue context) {
		return null;
	}

	@Override
	public IConstructor getListSep(String sort, IValue context) {
		return null;
	}

}
