package org.magnolialang.terms;

import org.eclipse.imp.pdb.facts.*;

public interface ILanguageSkin {
	public abstract boolean isVertical(String cons, String sort, int arity, IValue context);
	public abstract IList getConcrete(String cons, String sort, int arity, IValue context);
	public abstract IConstructor getListSep(String sort, IValue context);
}
