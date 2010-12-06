// based on org.rascalmpl.checker.StaticChecker
package org.magnolialang.rascal;

import java.io.PrintWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.pdb.facts.IValue;
import org.magnolialang.errors.ImplementationError;
import org.magnolialang.terms.TermFactory;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.env.GlobalEnvironment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.staticErrors.StaticError;
import org.rascalmpl.interpreter.staticErrors.SyntaxError;
import org.rascalmpl.parser.IRascalParser;
import org.rascalmpl.parser.LegacyRascalParser;

public class RascalInterpreter {

	// private final CommandEvaluator eval;
	private final Map<String, Evaluator> evals = new HashMap<String, Evaluator>();
	private final IRascalParser parser = new LegacyRascalParser();

	private static final class InstanceKeeper {
		public static final RascalInterpreter INSTANCE = new RascalInterpreter();

		private InstanceKeeper() {
		}
	}

	public RascalInterpreter() {
		super();
	}

	public Evaluator getEvaluator(String prelude) {
		if(evals.containsKey(prelude))
			return evals.get(prelude);

		GlobalEnvironment heap = new GlobalEnvironment();
		ModuleEnvironment root = heap.addModule(new ModuleEnvironment(
				"***magnolia***"));
		PrintWriter stderr = new PrintWriter(System.err);
		PrintWriter stdout = new PrintWriter(System.out);

		Evaluator eval = new Evaluator(TermFactory.vf, stderr, stdout, parser,
				root, heap);

		eval.addClassLoader(getClass().getClassLoader());
		if(!prelude.equals("")) {
			String[] cmds = prelude.split(";");
			for(String cmd : cmds)
				eval(cmd + ";", eval);
		}
		evals.put(prelude, eval);
		return eval;

	}

	public static RascalInterpreter getInstance() {
		return InstanceKeeper.INSTANCE;
	}

	public IValue eval(String cmd) {
		return eval(cmd, "");
	}

	public IValue eval(String cmd, String prelude) {
		return eval(cmd, getEvaluator(prelude));
	}

	public IValue call(String fun, String prelude, IValue... args) {
		return call(fun, getEvaluator(prelude), args);
	}

	public IValue call(String fun, Evaluator eval, IValue... args) {
		try {
			return eval.call(fun, args);
		}
		catch(StaticError e) {
			throw e;
		}
		catch(Exception e) {
			throw new ImplementationError(
					"Error in Rascal command evaluation: '" + fun + "'", e);
		}
	}

	public IValue eval(String cmd, Evaluator eval) {
		try {
			return eval.eval(cmd, URI.create("stdin:///")).getValue();
		}
		catch(SyntaxError se) {
			throw se;
			// throw new ImplementationError(
			// "syntax error in static checker modules: '" + cmd + "'", se);
		}
		catch(StaticError e) {
			throw e; // new ImplementationError("static error: '" + cmd + "'",
			// e);
		}
		catch(Exception e) {
			throw new ImplementationError(
					"Error in Rascal command evaluation: '" + cmd + "'", e);
		}
	}

	public void refresh() {
		evals.clear();
	}

}
