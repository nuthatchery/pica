package org.magnolialang.resources.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IStorableValue {
	void writeValue(OutputStream stream) throws IOException;


	void readValue(InputStream stream) throws IOException;
}
