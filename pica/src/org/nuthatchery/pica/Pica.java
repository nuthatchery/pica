package org.nuthatchery.pica;

import java.io.File;
import java.util.List;

import org.nuthatchery.pica.resources.IResourceManager;
import org.nuthatchery.pica.resources.IWorkspaceManager;
import org.rascalmpl.uri.CWDURIResolver;
import org.rascalmpl.uri.FileURIResolver;
import org.rascalmpl.uri.HttpURIResolver;
import org.rascalmpl.uri.URIResolverRegistry;

public class Pica {
	private static IPica platform;
	public static final URIResolverRegistry URI_RESOLVER_REGISTRY = new URIResolverRegistry();

	static {
		final FileURIResolver files = new FileURIResolver();
		URI_RESOLVER_REGISTRY.registerInput(files);
		URI_RESOLVER_REGISTRY.registerOutput(files);

		final HttpURIResolver http = new HttpURIResolver();
		URI_RESOLVER_REGISTRY.registerInput(http);

		final CWDURIResolver cwd = new CWDURIResolver();
		URI_RESOLVER_REGISTRY.registerInput(cwd);
		URI_RESOLVER_REGISTRY.registerOutput(cwd);

		// TODO: which library is this?
		// final ClassResourceInputOutput library = new
		// ClassResourceInputOutput(
		// uriResolverRegistry, "stdlib", Config.class, "");
		// uriResolverRegistry.registerInput(library);

	}


	public static String findFileInPath(final String path, final List<String> searchPath) {
		String file = null;
		for(final String dir : searchPath) {
			file = dir + "/" + path;
			if(new File(file).canRead()) {
				break;
			}
		}
		return file;
	}


	public static IPica get() {
		return platform;
	}


	public static URIResolverRegistry getResolverRegistry() {
		return URI_RESOLVER_REGISTRY;
	}


	/**
	 * Get the manager for a project
	 * 
	 * @param project
	 *            The project name (in Eclipse) or directory name
	 * @return A project resource manager
	 * @see {@link IWorkspaceManager#getManager(String)}
	 */
	public static IResourceManager getResourceManager(String project) {
		return platform.getWorkspaceManager().getManager(project);
	}


	/**
	 * Get the workspace manager.
	 * 
	 * The workspace manager provides an entry point to all projects and files
	 * in the workspace.
	 * 
	 * @return The workspace manager
	 * @see {@link IPica#getWorkspaceManager()}
	 */
	public static IWorkspaceManager getWorkspaceManager() {
		return platform.getWorkspaceManager();
	}


	public static void set(IPica platform) {
		Pica.platform = platform;
	}
}
