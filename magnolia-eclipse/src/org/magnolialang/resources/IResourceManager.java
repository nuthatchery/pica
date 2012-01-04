package org.magnolialang.resources;

import static org.magnolialang.terms.TermFactory.tf;
import static org.magnolialang.terms.TermFactory.ts;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.pdb.facts.type.Type;
import org.magnolialang.nullness.Nullable;
import org.rascalmpl.tasks.Transaction;

public interface IResourceManager {
	public static final Type	Type_ModuleResource	= tf.abstractDataType(ts, "ModuleResource");
	public static final Type	Cons_ModuleResource	= tf.constructor(ts, Type_ModuleResource, "ModuleResource", tf.stringType(), "val");
	public static final Type	Type_FileResource	= tf.abstractDataType(ts, "FileResource");
	public static final Type	Cons_FileResource	= tf.constructor(ts, Type_ModuleResource, "FileResource", tf.stringType(), "val");


	Transaction getTransaction();


	@Nullable
	IManagedResource find(IPath path);


	@Nullable
	IManagedResource find(IProject project, IPath path);


	void addListener(IManagedResourceListener listener);


	void removeListener(IManagedResourceListener listener);


	void dispose();


	/**
	 * @param uri
	 *            An URI
	 * @return An appropriate path for subsequent calls to the manager
	 * @throws IllegalArgumentException
	 *             if URI points outside workspace
	 */
	IPath getPath(URI uri);


	/**
	 * Relative paths are resolved relative to the current project (if any).
	 * 
	 * Path needs not exist in the file system.
	 * 
	 * @param path
	 *            A string representation of a path
	 * @return An appropriate path for subsequent calls to the manager
	 */
	IPath getPath(String path);
}
