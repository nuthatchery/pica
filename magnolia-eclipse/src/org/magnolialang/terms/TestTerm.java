package org.magnolialang.terms;

import static org.junit.Assert.*;

import java.util.Random;
import org.eclipse.imp.pdb.facts.*;
import org.junit.Test;

@Deprecated
public class TestTerm {
/*	public static void main(String[] args) {
		ITuple result = TermFactory.randomTreeWithVars(new Random(), 20);
		IConstructor tree = (IConstructor) result.get(0);
		IConstructor vartree = (IConstructor) result.get(1);
		IMap env = (IMap) result.get(2);
		
		System.out.println(tree);
		System.out.println(vartree);
		System.out.println(env);
		System.out.println(result.get(3));
		
	}
	
	@Test
	public void randomMatch() {
		for(int i = 0; i < 1000; i++) {
			ITuple result = TermFactory.randomTreeWithVars(new Random(), 20);
			IConstructor tree = (IConstructor) result.get(0);
			IConstructor pattern = (IConstructor) result.get(1);
			IMap env = (IMap) result.get(2);
			
			IMap matchEnv = TermAdapter.match(pattern, tree);
			assertNotNull(matchEnv);
			assertTrue(env.isEqual(matchEnv));
		}
	}
	
	public void matchEqualAxiom(IConstructor pattern, IConstructor tree) {
		if(pattern.isEqual(tree))
			assertNotNull(TermAdapter.match(pattern, tree));
	}
*/
	}
