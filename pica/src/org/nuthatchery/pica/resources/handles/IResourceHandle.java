package org.nuthatchery.pica.resources.handles;

import java.io.IOException;
import java.net.URI;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.tasks.ITaskMonitor;

public interface IResourceHandle {
	/**
	 * Create this resource.
	 *
	 * Relevant standard open options include
	 * {@link StandardOpenOption#CREATE} (default; resource is
	 * created if it does not exist, no action taken if it already exists)
	 * {@link StandardOpenOption#CREATE_NEW} (fail if resource
	 * already exists),
	 * {@link StandardOpenOption#TRUNCATE_EXISTING} (truncate file
	 * if it exists), {@link StandardOpenOption#SYNC} (write synchronously,
	 * returns only after data is written to disk).
	 *
	 * Relevant Pica-specific open options include
	 * {@link PicaOpenOption#DERIVED} (resource is derived),
	 * {@link PicaOpenOption#HIDDEN} (resource should be hidden),
	 * {@link PicaOpenOption#CREATE_PARENTS} (parent/ancestor folders should be
	 * created automatically).
	 *
	 * @param flags
	 * @throws IOException
	 */
	void create(ITaskMonitor tm, OpenOption... flags) throws IOException;


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


	/**
	 * @param derived
	 * @return True on success
	 * @throws IOException
	 */
	boolean setDerived(boolean derived) throws IOException;


	/**
	 * Attempt to set the hidden status of a resource.
	 *
	 * Not all file systems support hiding, and not all kinds of resources may
	 * be hidden.
	 *
	 * Note that on Unix systems, hiding is based on the file name (any name
	 * starting with a dot is hidden), hence it will not be possible to change
	 * status after a creation time.
	 *
	 * @param hidden
	 *            True if resource should be hidden
	 * @return True on success
	 * @throws IOException
	 */
	boolean setHidden(boolean hidden) throws IOException;


	/**
	 * @param readable
	 *            True if resource should be readable
	 * @return True on success
	 * @throws IOException
	 */
	boolean setReadable(boolean readable) throws IOException;


	/**
	 * @param writable
	 *            True if resource should be writable
	 * @return True on success
	 * @throws IOException
	 */
	boolean setWritable(boolean writable) throws IOException;
}
