package org.magnolialang.resources;

import java.io.IOException;
import java.io.InputStream;

public interface IManagedFile extends IManagedResource {
	char[] getContentsCharArray() throws IOException;


	InputStream getContentsStream() throws IOException;


	String getContentsString() throws IOException;


	boolean setContents(String contents) throws IOException;

}
