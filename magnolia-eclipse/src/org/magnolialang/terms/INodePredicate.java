package org.magnolialang.terms;

import org.eclipse.imp.pdb.facts.IConstructor;

public interface INodePredicate {
	public boolean match(IConstructor tree);
}
