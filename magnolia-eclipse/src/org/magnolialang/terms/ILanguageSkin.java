package org.magnolialang.terms;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IValue;

public interface ILanguageSkin {
	boolean isVertical(String cons, String sort, int arity,
			IValue context);

	IList getConcrete(String cons, String sort, int arity,
			IValue context);

	IConstructor getListSep(String sort, IValue context);
}
