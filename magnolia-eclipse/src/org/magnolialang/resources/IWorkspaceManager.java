package org.magnolialang.resources;

import static org.magnolialang.terms.TermFactory.tf;
import static org.magnolialang.terms.TermFactory.ts;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.imp.pdb.facts.type.Type;
import org.magnolialang.terms.TermFactory;

public interface IWorkspaceManager {
	Type	Type_FileResource	= tf.abstractDataType(ts, "FileResource");
	Type	Cons_FileResource	= tf.constructor(ts, Type_FileResource, "FileResource", tf.stringType(), "val");
	Type	Type_ModuleResource	= tf.abstractDataType(ts, "ModuleResource");
	Type	Cons_ModuleResource	= tf.constructor(ts, Type_ModuleResource, "ModuleResource", TermFactory.Type_AST, "val");


	void addListener(IManagedResourceListener listener);


	void removeListener(IManagedResourceListener listener);


	void dispose();


	/**
	 * Return an URI for the given path.
	 * 
	 * For the workspace manager, the path is interpreted relative to the
	 * workspace root. For project managers, the path is interpreted relative to
	 * the project root. The path may be either absolute or not.
	 * 
	 * Path needs not exist in the file system.
	 * 
	 * @param path
	 *            A string representation of a path
	 * @return An appropriate uri for subsequent calls to the manager
	 * @throws URISyntaxException
	 *             if the path is malformed
	 */
	URI getURI(String path) throws URISyntaxException;


	/**
	 * @param uri
	 *            A URI
	 * @return true if URI points inside workspace / project
	 */
	boolean hasURI(URI uri);
}
