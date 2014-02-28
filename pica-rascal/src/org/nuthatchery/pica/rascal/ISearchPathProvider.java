package org.nuthatchery.pica.rascal;

import java.util.Collection;

import org.rascalmpl.interpreter.Evaluator;

public interface ISearchPathProvider {
	Collection<ClassLoader> additionalClassLoaders();


	/**
	 * 
	 * For example, to add a path based on a class resource:
	 * 
	 * <code>
	 * 	URIResolverRegistry registry = evaluator.getResolverRegistry();
	 * 	ClassResourceInputOutput resolver = new ClassResourceInputOutput(registry, "myclass", MyClass.class, "/");
	 * 	registry.registerInput(resolver);
	 * 	evaluator.addRascalSearchPath(URI.create(resolver.scheme() + ":///"));
	 * </code>
	 * 
	 * @param evaluator
	 *            An evaluator to which search paths should be added
	 */
	void addRascalSearchPaths(Evaluator evaluator);
}
