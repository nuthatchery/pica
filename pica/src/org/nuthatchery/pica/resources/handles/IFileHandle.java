package org.nuthatchery.pica.resources.handles;

import java.io.IOException;
import java.io.InputStream;

public interface IFileHandle extends IResourceHandle {
	char[] getContentsCharArray() throws IOException;


	InputStream getContentsStream() throws IOException;


	String getContentsString() throws IOException;


	int getLength() throws IOException;


	boolean setContents(String contents) throws IOException;

}
