package org.nuthatchery.pica.resources.handles;

import java.net.URI;

import org.eclipse.jdt.annotation.Nullable;

public interface IResourceHandle {


	/**
	 * @return True if this resource exists
	 */
	boolean exists();


	/**
	 * @return Last modification time of the resource
	 */
	long getModificationStamp();


	/**
	 * @return The parent, or null if getKind() == PROJECT
	 * @axiom getParent().isContainer() is always true
	 */
	@Nullable
	IResourceHandle getParent();


	/**
	 * Get the URI corresponding to this resource.
	 *
	 * This is the physical URI, corresponding to how the resource is accessed
	 * from the system – typically a file://, http:// or jar:// URI.
	 *
	 * @return Physical URI of resource
	 */
	URI getURI();


	/**
	 * A file or a package.
	 *
	 * @return true if the pkg is a file or a package
	 */
	boolean isFile();


	/**
	 * A file system folder or a package.
	 *
	 * @return true if the pkg can have children
	 */
	boolean isFolder();


}
