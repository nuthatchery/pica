package org.nuthatchery.pica.resources.handles;

import java.net.URI;

import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.resources.managed.IManagedResource;

public interface IResourceHandle {

	/**
	 * @return This handle as an IManagedResource
	 * @throws UnsupportedOperationException
	 *             if {@link #isManaged()} is not true
	 */
	IManagedResource asManagedResource() throws UnsupportedOperationException;


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
	 * A file system folder or a package.
	 *
	 * @return true if the pkg can have children
	 */
	boolean isContainer();


	/**
	 * A file or a package.
	 *
	 * @return true if the pkg is a file or a package
	 */
	boolean isFile();


	/**
	 * @return True if this handle is also an IManagedResource
	 */
	boolean isManaged();
}
