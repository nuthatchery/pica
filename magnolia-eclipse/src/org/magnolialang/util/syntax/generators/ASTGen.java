package org.magnolialang.util.syntax.generators;

import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.IString;
import org.magnolialang.rascal.RascalInterpreter;

public class ASTGen {

	public static String generateASTTypes(ISet prods) {
		ISet ast = (ISet) RascalInterpreter
				.getInstance()
				.call("grammar2asts",
						"import org::magnolialang::util::syntax::generators::TermASTGen;",
						prods);
		String astDef = ((IString) RascalInterpreter
				.getInstance()
				.call("asts2rascal",
						"import org::magnolialang::util::syntax::generators::TermASTGen;",
						ast)).getValue();

		return astDef;
	}

	public static ISet generateGrammarInfo(ISet prods) {
		ISet info = (ISet) RascalInterpreter
				.getInstance()
				.call("grammar2info",
						"import org::magnolialang::util::syntax::generators::TermASTGen;",
						prods);
		return info;
	}
}
