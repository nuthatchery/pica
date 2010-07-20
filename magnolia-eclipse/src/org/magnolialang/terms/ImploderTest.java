package org.magnolialang.terms;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;
import org.magnolialang.eclipse.rascal.RascalInterpreter;
import org.rascalmpl.values.uptr.TreeAdapter;

import static org.junit.Assert.*;

public class ImploderTest {

	void isSameAsRascalAxiom(IConstructor tree) throws VisitorException {
		IValue implodedJ = TermImploder.implodeTree(tree);
		IValue implodedR = RascalInterpreter.getInstance().call("implodeTree", "import XaTree;", tree);

		assertTrue(implodedJ.isEqual(implodedR));
	}
	
	void implodeUnparseAxiom(IConstructor tree) throws VisitorException {
		IValue implodedJ = TermImploder.implodeTree(tree);
		
		// TODO: not really...
		assertEquals(TreeAdapter.yield(tree), implodedJ.toString());
	}
}
