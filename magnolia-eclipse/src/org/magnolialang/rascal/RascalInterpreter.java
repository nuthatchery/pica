// based on org.rascalmpl.checker.StaticChecker
package org.magnolialang.rascal;

import java.io.PrintWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.imp.pdb.facts.IValue;
import org.magnolialang.eclipse.MagnoliaPlugin;
import org.magnolialang.errors.ImplementationError;
import org.magnolialang.terms.TermFactory;
import org.rascalmpl.eclipse.console.RascalScriptInterpreter;
import org.rascalmpl.interpreter.Configuration;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.NullRascalMonitor;
import org.rascalmpl.interpreter.env.GlobalEnvironment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.load.RascalURIResolver;
import org.rascalmpl.interpreter.staticErrors.StaticError;
import org.rascalmpl.interpreter.staticErrors.SyntaxError;
import org.rascalmpl.uri.ClassResourceInputOutput;
import org.rascalmpl.uri.URIResolverRegistry;

public class RascalInterpreter {

	// private final CommandEvaluator eval;
	private final Map<String, Evaluator>	evals	= new HashMap<String, Evaluator>();


	private static final class InstanceKeeper {
		public static final RascalInterpreter	INSTANCE	= new RascalInterpreter();


		private InstanceKeeper() {
		}
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
		GlobalEnvironment heap = new GlobalEnvironment();
		ModuleEnvironment root = heap.addModule(new ModuleEnvironment("***magnolia***", heap));

		List<ClassLoader> loaders = Arrays.asList(getClass().getClassLoader(), Evaluator.class.getClassLoader(), RascalScriptInterpreter.class.getClassLoader());
		URIResolverRegistry registry = new URIResolverRegistry();
		RascalURIResolver resolver = new RascalURIResolver(registry);
		ClassResourceInputOutput eclipseResolver = new ClassResourceInputOutput(registry, "eclipse-std", RascalScriptInterpreter.class, "/org/rascalmpl/eclipse/library");
		registry.registerInput(eclipseResolver);
		Evaluator eval = new Evaluator(TermFactory.vf, out, err, root, heap, loaders, resolver); // URIResolverRegistry
		eval.addRascalSearchPath(URI.create(eclipseResolver.scheme() + ":///"));
		URI uri = MagnoliaPlugin.getFileURI(MagnoliaPlugin.MAGNOLIA_BUNDLE, "");
		eval.addRascalSearchPath(uri);
		eval.addRascalSearchPath(uri.resolve("src"));

		System.out.println("Rascal Classpath: " + Configuration.getRascalJavaClassPathProperty());
		System.out.println("Magnolia Classpath: " + System.getProperty("magnolia.java.classpath"));
		Configuration.setRascalJavaClassPathProperty(System.getProperty("magnolia.java.classpath"));

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
			return eval.call(new NullRascalMonitor(), fun, args);
		}
		catch(StaticError e) { // NOPMD by anya on 1/5/12 4:18 AM
			throw e;
		}
		catch(Exception e) {
			throw new ImplementationError("Error in Rascal command evaluation: '" + fun + "'", e);
		}
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


	public void refresh() {
		evals.clear();
	}

}
