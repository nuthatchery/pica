package org.magnolialang.terms.skins;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IValue;

import checkers.nullness.quals.Nullable;

public interface ILanguageSkin {
	public abstract boolean isVertical(String cons, int arity, @Nullable IValue context);


	@Nullable
	public abstract IList getConcrete(String cons, int arity, @Nullable IValue context);


	public abstract boolean isVertical(IConstructor cons, @Nullable IValue context);


	@Nullable
	public abstract IList getConcrete(IConstructor cons, @Nullable IValue context);


	@Nullable
	public abstract IConstructor getListSep(String sort, @Nullable IValue context);
}
