package org.nuthatchery.pica;

import java.io.File;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.errors.ImplementationError;
import org.nuthatchery.pica.resources.IProjectManager;
import org.nuthatchery.pica.resources.IWorkspaceManager;

public class Pica {
	@Nullable
	private static IPica platform;


	static {
//		final FileURIResolver files = new FileURIResolver();
//		URI_RESOLVER_REGISTRY.registerInput(files);
//		URI_RESOLVER_REGISTRY.registerOutput(files);
//
//		final HttpURIResolver http = new HttpURIResolver();
//		URI_RESOLVER_REGISTRY.registerInput(http);
//
//		final CWDURIResolver cwd = new CWDURIResolver();
//		URI_RESOLVER_REGISTRY.registerInput(cwd);
//		URI_RESOLVER_REGISTRY.registerOutput(cwd);

		// TODO: which library is this?
		// final ClassResourceInputOutput library = new
		// ClassResourceInputOutput(
		// uriResolverRegistry, "stdlib", Config.class, "");
		// uriResolverRegistry.registerInput(library);

	}


	@Nullable
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
		IPica p = platform;
		if(p == null) {
			throw new ImplementationError("Pica not initialised");
		}
		return p;
	}


	/**
	 * Get the manager for a project
	 *
	 * @param project
	 *            The project name (in Eclipse) or directory name
	 * @return A project resource manager, null if the project does not exist
	 * @see {@link IWorkspaceManager#getManager(String)}
	 */
	@Nullable
	public static IProjectManager getResourceManager(String project) {
		return get().getWorkspaceManager().getManager(project);
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
		return get().getWorkspaceManager();
	}


	/**
	 * Print a message to standard error, with a copy to the current log file
	 * (if any)
	 *
	 * @param msg
	 */
	public static void println(String msg) {
		get().println(msg);
	}


	public static void set(IPica platform) {
		Pica.platform = platform;
	}
}
