package org.magnolialang.terms.skins;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IValue;
import org.magnolialang.nullness.Nullable;

public interface ILanguageSkin {
	boolean isVertical(String cons, int arity, @Nullable IValue context);


	@Nullable
	IList getConcrete(String cons, int arity, @Nullable IValue context);


	boolean isVertical(IConstructor cons, @Nullable IValue context);


	@Nullable
	IList getConcrete(IConstructor cons, @Nullable IValue context);


	@Nullable
	IConstructor getListSep(String sort, @Nullable IValue context);
}
