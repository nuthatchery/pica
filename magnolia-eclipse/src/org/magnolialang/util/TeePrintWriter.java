/**************************************************************************
 * Copyright (c) 2011-2012 Anya Helene Bagge
 * Copyright (c) 2011-2012 University of Bergen
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. See http://www.gnu.org/licenses/
 * 
 * 
 * See the file COPYRIGHT for more information.
 * 
 * Contributors:
 * * Anya Helene Bagge
 * 
 *************************************************************************/
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
