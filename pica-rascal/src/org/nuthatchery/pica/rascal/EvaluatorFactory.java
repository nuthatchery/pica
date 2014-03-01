package org.nuthatchery.pica.rascal;

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.terms.TermFactory;
import org.rascalmpl.eclipse.console.RascalScriptInterpreter;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.env.GlobalEnvironment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.load.RascalURIResolver;
import org.rascalmpl.interpreter.load.StandardLibraryContributor;
import org.rascalmpl.uri.ClassResourceInput;
import org.rascalmpl.uri.URIResolverRegistry;

/**
 * This static helper class provides access to an appropriate (system-dependent)
 * factory for producing Rascal evaluators.
 * 
 */
public class EvaluatorFactory implements IEvaluatorFactory {
	private final Map<List<String>, IEvaluatorPool> pools = new HashMap<List<String>, IEvaluatorPool>();
	@Nullable
	private final ISearchPathProvider pathProvider;


	public EvaluatorFactory() {
		this.pathProvider = null;
	}


	public EvaluatorFactory(ISearchPathProvider pathProvider) {
		this.pathProvider = pathProvider;
	}


	@Override
	public IEvaluatorPool getEvaluatorPool(String name, List<String> imports) {
		IEvaluatorPool pool = pools.get(imports);
		if(pool == null) {
			pool = makeEvaluatorPool(name, imports);
			pools.put(new ArrayList<String>(imports), pool);
		}
		return pool;
	}


	@Override
	public Evaluator makeEvaluator() {
		return makeEvaluator(new PrintWriter(System.out), new PrintWriter(System.err));
	}


	@Override
	public Evaluator makeEvaluator(PrintWriter out, PrintWriter err) {
		GlobalEnvironment heap = new GlobalEnvironment();
		ModuleEnvironment root = heap.addModule(new ModuleEnvironment("***magnolia***", heap));

		List<ClassLoader> loaders = new ArrayList<ClassLoader>(Arrays.asList(getClass().getClassLoader(), Evaluator.class.getClassLoader(), RascalScriptInterpreter.class.getClassLoader()));

		if(pathProvider != null)
			loaders.addAll(pathProvider.additionalClassLoaders());
		URIResolverRegistry registry = new URIResolverRegistry();
		RascalURIResolver resolver = new RascalURIResolver(registry);
		// registry.registerInput(new BundleURIResolver(registry));
		Evaluator eval = new Evaluator(TermFactory.vf, out, err, root, heap, loaders, resolver); // URIResolverRegistry
		eval.addRascalSearchPathContributor(StandardLibraryContributor.getInstance());

		ClassResourceInput eclipseResolver = new ClassResourceInput(registry, "eclipse-std", RascalScriptInterpreter.class, "/org/rascalmpl/eclipse/library");
		registry.registerInput(eclipseResolver);
		eval.addRascalSearchPath(URI.create(eclipseResolver.scheme() + ":///"));

		ClassResourceInput picaResolver = new ClassResourceInput(registry, "pica-std", getClass(), "/");
		registry.registerInput(picaResolver);
		eval.addRascalSearchPath(URI.create(picaResolver.scheme() + ":///"));

		if(pathProvider != null)
			pathProvider.addRascalSearchPaths(eval);

		String property = getRascalClassPath();
		if(!property.equals("")) {
			eval.getConfiguration().setRascalJavaClassPathProperty(property);
		}

		return eval;
	}


	public void refresh() {
		for(IEvaluatorPool pool : pools.values()) {
			pool.reload();
		}
	}


	private String getRascalClassPath() {
		String path = "";
		URL rascalPath = Evaluator.class.getResource("/");
		System.err.println("rascalPath: " + rascalPath);
		if(rascalPath != null) {
			path = rascalPath.toString();
		}

		URL valuesPath = IValue.class.getResource("/");
		System.err.println("valuesPath: " + valuesPath);
		if(valuesPath != null) {
			path += File.pathSeparator + valuesPath.toString();
		}

		String property = System.getProperty("java.class.path");
		if(property != null)
			path += File.pathSeparator + property;

		System.err.println("rascalClassPath: " + path);
		return path;
	}


	protected IEvaluatorPool makeEvaluatorPool(String name, List<String> imports) {
		return new ConsoleEvaluatorPool(this, name, imports);
	}

}
