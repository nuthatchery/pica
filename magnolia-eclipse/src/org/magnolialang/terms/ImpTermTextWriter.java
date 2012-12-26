package org.magnolialang.terms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;

// Contains some convenience methods to accompany the visitor class.
public class ImpTermTextWriter {
	// Two inefficiencies here.
	// (1) We are dealing with OutputStreams when we really want a character
	// String.
	// (2) We recreate the OutputStream with every invocation of this method,
	// and the same goes for the visitor,
	// when more typically one probably generates more text at once.
	public static String termPatternToString(IValue value) {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			ImpTermTextWriterVisitor visitor = new ImpTermTextWriterVisitor(stream, false, 2) {
				@Override
				public IValue visitConstructor(IConstructor o) throws VisitorException {
					if(TermAdapter.isVar(o)) {
						IString is = (IString) o.get("name");
						append(is.getValue());
						return o;
					}
					else {
						return visitNode(o);
					}
				}
			};
			new ImpTermTextWriter(visitor).write(value);
			return stream.toString();
		}
		catch(IOException ioex) { // NOPMD by anya on 1/5/12 3:43 AM
			// this never happens
		}
		return null;
	}


	public static String termToString(IValue value) {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			new ImpTermTextWriter(stream).write(value);
			return stream.toString();
		}
		catch(IOException ioex) { // NOPMD by anya on 1/5/12 3:43 AM
			// this never happens
		}
		return null;
	}

	private IValueVisitor<IValue> visitor;


	public ImpTermTextWriter(IValueVisitor<IValue> visitor) {
		this.visitor = visitor;
	}


	public ImpTermTextWriter(OutputStream stream) {
		this(new ImpTermTextWriterVisitor(stream, false, 2));
	}


	public IValueVisitor<IValue> getVisitor() {
		return visitor;
	}


	public void setVisitor(IValueVisitor<IValue> visitor) {
		this.visitor = visitor;
	}


	public void write(IValue value) throws IOException {
		try {
			value.accept(visitor);
		}
		catch(VisitorException e) {
			throw (IOException) e.getCause();
		}
	}

}
