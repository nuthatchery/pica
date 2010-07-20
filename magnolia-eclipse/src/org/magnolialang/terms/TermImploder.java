package org.magnolialang.terms;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;
import org.magnolialang.errors.ImplementationError;

public class TermImploder {

	/**
	 * Implode an AsFix tree to an XaTree.
	 * @param tree A tree in AsFix format
	 * @return An imploded XaTree
	 */
	public static IValue implodeTree(IValue tree) {
		try {
			return tree.accept(new TermImplodeVisitor());
		} catch (VisitorException e) {
			throw new ImplementationError("Failed to implode parse tree", e);
		}	
	}
}
