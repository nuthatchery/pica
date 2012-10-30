package org.magnolialang.resources;

import java.net.URI;
import java.net.URISyntaxException;

public interface IWorkspaceManager {

	void dispose();


	/**
	 * Get the manager for a project
	 * 
	 * @param project
	 *            The project name (in Eclipse) or directory name
	 * @return A project resource manager
	 */
	IResourceManager getManager(String project);


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


	/**
	 * Stop any running jobs associated with this resource manager
	 */
	void stop();
}
