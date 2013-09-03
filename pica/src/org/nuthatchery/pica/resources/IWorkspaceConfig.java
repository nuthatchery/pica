package org.nuthatchery.pica.resources;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.nuthatchery.pica.resources.storage.IStorage;
import org.rascalmpl.interpreter.Evaluator;

public interface IWorkspaceConfig {

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


	Collection<String> getActiveNatures();


	ClassLoader getParserClassLoader();


	void initCompiler();


	IManagedPackage makePackage(IResourceManager manager, IFile resource, IStorage storage, IConstructor id, ILanguage lang);
}
