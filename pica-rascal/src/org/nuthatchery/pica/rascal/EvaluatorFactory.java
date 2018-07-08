package org.nuthatchery.pica.rascal;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.usethesource.vallang.IValue;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.terms.TermFactory;
// import org.rascalmpl.eclipse.nature.Nature;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.env.GlobalEnvironment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.load.RascalSearchPath;
import org.rascalmpl.interpreter.load.StandardLibraryContributor;
import org.rascalmpl.uri.ISourceLocationInput;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.libraries.ClassResourceInput;

/**
 * This static helper class provides access to an appropriate (system-dependent)
 * factory for producing Rascal evaluators.
 *
 */
public class EvaluatorFactory implements IEvaluatorFactory {
	static class MyClassResourceInput extends ClassResourceInput {

		public MyClassResourceInput(String scheme, Class<?> clazz, String prefix) {
			super(scheme, clazz, prefix);
		}
	}


	public static void addClassInputSearchPath(Evaluator eval, String scheme, Class<?> clazz, String prefix) {
		ClassResourceInput resolver = new MyClassResourceInput(scheme, clazz, prefix);
		URIResolverRegistry registry = URIResolverRegistry.getInstance();
		Method method;
		try {
			method = registry.getClass().getDeclaredMethod("registerInput", ISourceLocationInput.class);
			method.setAccessible(true);
			method.invoke(registry, resolver);
		}
		catch(NoSuchMethodException e) {
			e.printStackTrace();
		}
		catch(SecurityException e) {
			e.printStackTrace();
		}
		catch(IllegalAccessException e) {
			e.printStackTrace();
		}
		catch(IllegalArgumentException e) {
			e.printStackTrace();
		}
		catch(InvocationTargetException e) {
			e.printStackTrace();
		}
		// TODO: if registerInput is made public, we should do this instead:
		//registry.registerInput(resolver);
		eval.addRascalSearchPath(TermFactory.vf.sourceLocation(URI.create(resolver.scheme() + ":///")));
	}


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
	public synchronized IEvaluatorPool getEvaluatorPool(String name, List<String> imports) {
		return getEvaluatorPool(name, imports, IEvaluatorFactory.DEFAULT_MIN_EVALUTORS);
	}


	@Override
	public synchronized IEvaluatorPool getEvaluatorPool(String name, List<String> imports, int minEvaluators) {
		IEvaluatorPool pool = pools.get(imports);
		if(pool == null) {
			pool = makeEvaluatorPool(name, imports, minEvaluators);
			pools.put(new ArrayList<String>(imports), pool);
		}
		return pool;
	}


	private String getRascalClassPath() {
		String path = "";
		URL rascalPath = Evaluator.class.getResource("/");
//		System.err.println("rascalPath: " + rascalPath);
		if(rascalPath != null) {
			path = rascalPath.toString();
		}

		URL valuesPath = IValue.class.getResource("/");
		//	System.err.println("valuesPath: " + valuesPath);
		if(valuesPath != null) {
			path += File.pathSeparator + valuesPath.toString();
		}

		String property = System.getProperty("java.class.path");
		if(property != null)
			path += File.pathSeparator + property;

		// System.err.println("rascalClassPath: " + path);
		return path;
	}


	@Override
	public Evaluator makeEvaluator() {
		return makeEvaluator(new PrintWriter(System.out), new PrintWriter(System.err));
	}


	@Override
	public Evaluator makeEvaluator(PrintWriter out, PrintWriter err) {
		GlobalEnvironment heap = new GlobalEnvironment();
		ModuleEnvironment root = heap.addModule(new ModuleEnvironment("***magnolia***", heap));

		// TODO: Eclipse?
		List<ClassLoader> loaders = new ArrayList<ClassLoader>(Arrays.asList(getClass().getClassLoader(), Evaluator.class.getClassLoader()//, Nature.class.getClassLoader()
		));

		if(pathProvider != null)
			loaders.addAll(pathProvider.additionalClassLoaders());
		URIResolverRegistry registry = URIResolverRegistry.getInstance();
		// TODO: RascalURIResolver resolver = new RascalURIResolver(registry);
		// registry.registerInput(new BundleURIResolver(registry));
		Evaluator eval = new Evaluator(TermFactory.vf, out, err, root, heap, loaders, new RascalSearchPath());// URIResolverRegistry
		eval.addRascalSearchPathContributor(StandardLibraryContributor.getInstance());

		//TODO:
		// addClassInputSearchPath(eval, "eclipse-std", Nature.class, "/org/rascalmpl/eclipse/library");

		addClassInputSearchPath(eval, "pica-std", getClass(), "/");

		if(pathProvider != null)
			pathProvider.addRascalSearchPaths(eval);

		String property = getRascalClassPath();
		if(!property.equals("")) {
			eval.getConfiguration().setRascalJavaClassPathProperty(property);
		}

		return eval;
	}


	/**
	 *
	 * @param name
	 * @param imports
	 * @param minEvaluators
	 * @return A newly created and initialized pool.
	 */
	protected IEvaluatorPool makeEvaluatorPool(String name, List<String> imports, int minEvaluators) {
		ConsoleEvaluatorPool pool = new ConsoleEvaluatorPool(this, name, imports, minEvaluators);
		pool.initialize();
		return pool;
	}


	/**
	 * Refresh the evaluator pools. Call getEvaluatorPool again to get a fresh,
	 * reloaded pool.
	 */
	@Override
	public synchronized void refresh() {
		for(Entry<List<String>, IEvaluatorPool> entry : pools.entrySet()) {
			List<String> imports = entry.getKey();
			assert imports != null;
			IEvaluatorPool pool = entry.getValue();
			pool = makeEvaluatorPool(pool.getName(), imports, pool.getMinEvaluators());
			entry.setValue(pool);
		}
	}


	/**
	 * Refresh/reinitialise the given evaluator pool
	 *
	 * @param pool
	 *            The pool
	 * @return A (possibly new) pool with all imports reloaded
	 */
	@Override
	public IEvaluatorPool refresh(IEvaluatorPool pool) {
		return refresh(pool, pool.getMinEvaluators());
	}


	/**
	 * Refresh/reinitialise the given evaluator pool
	 *
	 * @param pool
	 *            The pool
	 * @param minEvaluators
	 *            Minimum number of evaluators in the refreshed pool
	 * @return A (possibly new) pool with all imports reloaded
	 */
	@Override
	public synchronized IEvaluatorPool refresh(IEvaluatorPool pool, int minEvaluators) {
		List<String> imports = pool.getImports();
		IEvaluatorPool newPool = makeEvaluatorPool(pool.getName(), imports, pool.getMinEvaluators());
		pools.put(imports, newPool);
		return newPool;
	}

}
