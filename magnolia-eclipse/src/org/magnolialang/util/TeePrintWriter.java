package org.magnolialang.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * This class wraps a PrintWriter, and stores its output in a string buffer.
 * 
 * @author anya
 * 
 */
public class TeePrintWriter extends PrintWriter {
	private final Writer writer;
	private final StringWriter buffer;
	private final ITeeReceiver receiver;


	public TeePrintWriter(Writer writer, ITeeReceiver receiver) {
		super(new StringWriter(), true);
		this.buffer = (StringWriter) out;
		this.writer = writer;
		this.receiver = receiver;
	}


	@Override
	public void flush() {
		String s = null;
		synchronized(lock) {
			super.flush();
			s = buffer.toString();
			try {
				writer.write(s);
				writer.flush();
			}
			catch(IOException e) {
				setError();
			}
			buffer.getBuffer().setLength(0);
		}
		if(s != null && !s.equals("")) {
			receiver.receive(s);
		}
	}


	@Override
	public void println() {
		super.println();
		flush();
	}
}
