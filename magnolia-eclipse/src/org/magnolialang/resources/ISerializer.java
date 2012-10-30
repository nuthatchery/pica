package org.magnolialang.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ISerializer<T> {
	T read(InputStream stream) throws IOException;


	void write(T data, OutputStream stream) throws IOException;
}
