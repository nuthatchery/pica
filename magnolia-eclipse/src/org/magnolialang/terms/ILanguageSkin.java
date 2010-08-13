package org.magnolialang.terms;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IValue;

public interface ILanguageSkin {
	public abstract boolean isVertical(String cons, int arity, IValue context);

	public abstract IList getConcrete(String cons, int arity, IValue context);

	public abstract boolean isVertical(IConstructor cons, IValue context);

	public abstract IList getConcrete(IConstructor cons, IValue context);

	public abstract IConstructor getListSep(String sort, IValue context);
}
