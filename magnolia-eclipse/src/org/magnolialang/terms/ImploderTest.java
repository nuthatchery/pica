package org.magnolialang.terms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;
import org.magnolialang.rascal.RascalInterpreter;
import org.rascalmpl.values.uptr.TreeAdapter;

public class ImploderTest {

	void implodeUnparseAxiom(final IConstructor tree) throws VisitorException {
		final IValue implodedJ = TermImploder.implodeTree(tree);

		// TODO: not really...
		assertEquals(TreeAdapter.yield(tree), implodedJ.toString());
	}


	void isSameAsRascalAxiom(final IConstructor tree) throws VisitorException {
		final IValue implodedJ = TermImploder.implodeTree(tree);
		final IValue implodedR = RascalInterpreter.getInstance().call("implodeTree", "import XaTree;", tree);

		assertTrue(implodedJ.isEqual(implodedR));
	}
}
