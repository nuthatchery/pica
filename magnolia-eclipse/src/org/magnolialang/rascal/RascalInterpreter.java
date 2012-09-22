// based on org.rascalmpl.checker.StaticChecker
package org.magnolialang.rascal;

import java.io.PrintWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.pdb.facts.IValue;
import org.magnolialang.errors.ImplementationError;
import org.magnolialang.infra.Infra;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.NullRascalMonitor;
import org.rascalmpl.interpreter.staticErrors.StaticError;
import org.rascalmpl.interpreter.staticErrors.SyntaxError;

public class RascalInterpreter {

	public static RascalInterpreter getInstance() {
		return InstanceKeeper.INSTANCE;
	}


	// private final CommandEvaluator eval;
	private final Map<String, Evaluator>	evals	= new HashMap<String, Evaluator>();


	public IValue call(String fun, Evaluator eval, IValue... args) {
		try {
			return eval.call(new NullRascalMonitor(), fun, args);
		}
		catch(StaticError e) { // NOPMD by anya on 1/5/12 4:18 AM
			throw e;
		}
		catch(Exception e) {
			throw new ImplementationError("Error in Rascal command evaluation: '" + fun + "'", e);
		}
	}


	public IValue call(String fun, String prelude, IValue... args) {
		return call(fun, getEvaluator(prelude), args);
	}


	public IValue eval(String cmd) {
		return eval(cmd, "");
	}


	public IValue eval(String cmd, Evaluator eval) {
		try {
			return eval.eval(new NullRascalMonitor(), cmd, URI.create("stdin:///")).getValue();
		}
		catch(SyntaxError se) { // NOPMD by anya on 1/5/12 4:18 AM
			throw se;
			// throw new ImplementationError(
			// "syntax error in static checker modules: '" + cmd + "'", se);
		}
		catch(StaticError e) { // NOPMD by anya on 1/5/12 4:18 AM
			throw e; // new ImplementationError("static error: '" + cmd + "'",
			// e);
		}
		catch(Exception e) {
			throw new ImplementationError("Error in Rascal command evaluation: '" + cmd + "'", e);
		}
	}


	public IValue eval(String cmd, String prelude) {
		return eval(cmd, getEvaluator(prelude));
	}


	public synchronized Evaluator getEvaluator(String prelude) {
		if(evals.containsKey(prelude))
			return evals.get(prelude);

		Evaluator eval = newEvaluator(new PrintWriter(System.out), new PrintWriter(System.err));

		if(!prelude.equals("")) {
			String[] cmds = prelude.split(";");
			for(String cmd : cmds)
				eval(cmd + ";", eval);
		}
		evals.put(prelude, eval);
		return eval;

	}


	public Evaluator newEvaluator() {
		return newEvaluator(new PrintWriter(System.out), new PrintWriter(System.err));
	}


	public Evaluator newEvaluator(PrintWriter out, PrintWriter err) {
		return Infra.get().newEvaluator(out, err);
	}


	public void refresh() {
		evals.clear();
	}


	private static final class InstanceKeeper {
		public static final RascalInterpreter	INSTANCE	= new RascalInterpreter();


		private InstanceKeeper() {
		}
	}

}
