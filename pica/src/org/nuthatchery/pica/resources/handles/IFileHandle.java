package org.nuthatchery.pica.resources.handles;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

import org.nuthatchery.pica.tasks.ITaskMonitor;

public interface IFileHandle extends IResourceHandle {
	char[] getContentsCharArray(ITaskMonitor tm) throws IOException;


	String getContentsString(ITaskMonitor tm) throws IOException;


	long getLength(ITaskMonitor tm) throws IOException;


	byte[] getRawContentsByteArray(ITaskMonitor tm) throws IOException;


	InputStream getRawContentsStream(ITaskMonitor tm) throws IOException;


	/**
	 * Set contents of this file.
	 *
	 * Relevant standard open options include
	 * {@link StandardOpenOption#CREATE} (default; resource is
	 * created if it does not exist, no action taken if it already exists)
	 * {@link StandardOpenOption#CREATE_NEW} (fail if resource
	 * already exists),
	 * {@link StandardOpenOption#TRUNCATE_EXISTING} (default; truncate file
	 * if it exists), {@link StandardOpenOption#APPEND} (content is appended to
	 * existing file),
	 *
	 * @param contents
	 * @param tm
	 * @param flags
	 * @return
	 * @throws IOException
	 */
	boolean setContents(String contents, ITaskMonitor tm, OpenOption... flags) throws IOException;
}
