package org.magnolialang.terms;

import org.eclipse.imp.pdb.facts.IConstructor;

public interface INodePredicate {
	boolean match(IConstructor tree);
}
