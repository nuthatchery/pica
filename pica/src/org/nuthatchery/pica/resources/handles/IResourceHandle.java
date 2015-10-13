package org.nuthatchery.pica.resources.handles;

import java.io.IOException;
import java.net.URI;

import org.eclipse.jdt.annotation.Nullable;

public interface IResourceHandle {
	public static final int FLAG_NONE = 0x00;
	/**
	 * When creating a resource, indicate that any already existing resource
	 * should be removed/truncated and overwritten.
	 */
	public static final int FLAG_OVERWRITE = 0x10;
	/**
	 * Indicate that a file should be hidden, if the underlying filesystem
	 * supports this.
	 */
	public static final int FLAG_HIDDEN = 0x02;
	/**
	 * Indicate that a resource is derived from some other resource.
	 */
	public static final int FLAG_DERIVED = 0x04;

	public static final int FLAG_READ_ONLY = 0x01;
	/**
	 * When creating a resource, also create the parent and ancestors, if
	 * necessary.
	 */
	public static final int FLAG_CREATE_PARENT = 0x20;


	void clearFlags(int flags);


	/**
	 * Create this resource.
	 *
	 * @throws IOException
	 */
	void create(int flags) throws IOException;


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
	IFolderHandle getParent();


	/**
	 * Get the URI corresponding to this resource.
	 *
	 * This is the physical URI, corresponding to how the resource is accessed
	 * from the system – typically a file://, http:// or jar:// URI.
	 *
	 * @return Physical URI of resource
	 */
	URI getURI();


	boolean isDerived();


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


	boolean isHidden();


	boolean isReadable();


	boolean isWritable();


	void setFlags(int flags);


}
