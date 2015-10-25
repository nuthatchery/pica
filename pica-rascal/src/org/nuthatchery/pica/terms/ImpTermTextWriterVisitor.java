/**************************************************************************
 * Copyright (c) 2009 CWI
 * Copyright (c) 2011-2013 Anya Helene Bagge
 * Copyright (c) 2011-2013 University of Bergen
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * 
 * See the file COPYRIGHT for more information.
 * 
 * Contributors:
 * * Anya Helene Bagge
 * + Jurgen Vinju
 * + Tero Hasu
 * 
 *************************************************************************/
package org.nuthatchery.pica.terms;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.rascalmpl.value.IAnnotatable;
import org.rascalmpl.value.IBool;
import org.rascalmpl.value.IConstructor;
import org.rascalmpl.value.IDateTime;
import org.rascalmpl.value.IExternalValue;
import org.rascalmpl.value.IInteger;
import org.rascalmpl.value.IList;
import org.rascalmpl.value.IMap;
import org.rascalmpl.value.INode;
import org.rascalmpl.value.IRational;
import org.rascalmpl.value.IReal;
import org.rascalmpl.value.ISet;
import org.rascalmpl.value.ISourceLocation;
import org.rascalmpl.value.IString;
import org.rascalmpl.value.ITuple;
import org.rascalmpl.value.IValue;
import org.rascalmpl.value.io.StandardTextReader;
import org.rascalmpl.value.io.StandardTextWriter;
import org.rascalmpl.value.type.Type;
import org.rascalmpl.value.visitors.IValueVisitor;

/**
 * This class implements the standard readable syntax for {@link IValue}'s. See
 * also {@link StandardTextReader}. This is a public copy of the exact same
 * visitor as is found inside {@link StandardTextWriter}, to allow for
 * invocation and customization.
 */
public class ImpTermTextWriterVisitor implements IValueVisitor<IValue, IOException> {
	private final OutputStream stream;
	private final int tabSize;
	private final boolean indented;
	private int tabLevel = 0;


	public ImpTermTextWriterVisitor(OutputStream stream, boolean indented, int tabSize) {
		this.stream = stream;
		this.indented = indented;
		this.tabSize = tabSize;
	}


	@Override
	public IValue visitBoolean(IBool boolValue) throws IOException {
		append(boolValue.getValue() ? "true" : "false");
		return boolValue;
	}


	@Override
	public IValue visitConstructor(IConstructor o) throws IOException {
		return visitNode(o);
	}


	@Override
	public IValue visitDateTime(IDateTime o) throws IOException {
		append("$");
		if(o.isDate()) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
			append(df.format(new Date(o.getInstant())));
		}
		else if(o.isTime()) {
			SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSSZZZ", Locale.ROOT);
			append("T");
			append(df.format(new Date(o.getInstant())));
		}
		else {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ", Locale.ROOT);
			append(df.format(new Date(o.getInstant())));
		}
		return o;
	}


	@Override
	public IValue visitExternal(IExternalValue externalValue) throws IOException {
		append(externalValue.toString());
		return externalValue;
	}


	@Override
	public IValue visitInteger(IInteger o) throws IOException {
		append(o.getStringRepresentation());
		return o;
	}


	@Override
	public IValue visitList(IList o) throws IOException {
		append('[');

		boolean indent = checkIndent(o);
		Iterator<IValue> listIterator = o.iterator();
		tab();
		indent(indent);
		if(listIterator.hasNext()) {
			listIterator.next().accept(this);

			while(listIterator.hasNext()) {
				append(',');
				if(indent) {
					indent();
				}
				listIterator.next().accept(this);
			}
		}
		untab();
		indent(indent);
		append(']');

		return o;
	}


	@Override
	public IValue visitListRelation(IList o) throws IOException {
		return visitList(o);
	}


	@Override
	public IValue visitMap(IMap o) throws IOException {
		append('(');
		tab();
		boolean indent = checkIndent(o);
		indent(indent);
		Iterator<IValue> mapIterator = o.iterator();
		if(mapIterator.hasNext()) {
			IValue key = mapIterator.next();
			key.accept(this);
			append(':');
			o.get(key).accept(this);

			while(mapIterator.hasNext()) {
				append(',');
				indent(indent);
				key = mapIterator.next();
				key.accept(this);
				append(':');
				o.get(key).accept(this);
			}
		}
		untab();
		indent(indent);
		append(')');

		return o;
	}


	@Override
	public IValue visitNode(INode o) throws IOException {
		String name = o.getName();

		if(name.indexOf('-') != -1) {
			append('\\');
		}
		append(name);

		boolean indent = checkIndent(o);

		append('(');
		tab();
		indent(indent);
		Iterator<IValue> it = o.iterator();
		while(it.hasNext()) {
			it.next().accept(this);
			if(it.hasNext()) {
				append(',');
				indent(indent);
			}
		}
		append(')');
		untab();
		if(o.isAnnotatable()) {
			IAnnotatable<? extends INode> annotatable = o.asAnnotatable();

			if(annotatable.hasAnnotations()) {
				append('[');
				tab();
				indent();
				int i = 0;
				Map<String, IValue> annotations = annotatable.getAnnotations();
				for(Entry<String, IValue> entry : annotations.entrySet()) {
					append("@" + entry.getKey() + "=");
					entry.getValue().accept(this);

					if(++i < annotations.size()) {
						append(",");
						indent();
					}
				}
				untab();
				indent();
				append(']');
			}
		}
		return o;
	}


	@Override
	public IValue visitRational(IRational o) throws IOException {
		append(o.getStringRepresentation());
		return o;
	}


	@Override
	public IValue visitReal(IReal o) throws IOException {
		append(o.getStringRepresentation());
		return o;
	}


	@Override
	public IValue visitRelation(ISet o) throws IOException {
		return visitSet(o);
	}


	@Override
	public IValue visitSet(ISet o) throws IOException {
		append('{');

		boolean indent = checkIndent(o);
		tab();
		indent(indent);
		Iterator<IValue> setIterator = o.iterator();
		if(setIterator.hasNext()) {
			setIterator.next().accept(this);

			while(setIterator.hasNext()) {
				append(",");
				indent(indent);
				setIterator.next().accept(this);
			}
		}
		untab();
		indent(indent);
		append('}');
		return o;
	}


	@Override
	public IValue visitSourceLocation(ISourceLocation o) throws IOException {
		append('|');
		append(o.getURI().toString());
		append('|');

		if(o.hasOffsetLength()) {
			append('(');
			append(Integer.toString(o.getOffset()));
			append(',');
			append(Integer.toString(o.getLength()));
			append(',');
			append('<');
			append(Integer.toString(o.getBeginLine()));
			append(',');
			append(Integer.toString(o.getBeginColumn()));
			append('>');
			append(',');
			append('<');
			append(Integer.toString(o.getEndLine()));
			append(',');
			append(Integer.toString(o.getEndColumn()));
			append('>');
			append(')');
		}
		return o;
	}


	@Override
	public IValue visitString(IString o) throws IOException {
		append('\"');
		for(byte ch : o.getValue().getBytes()) {
			switch(ch) {
			case '\"':
				append('\\');
				append('\"');
				break;
			case '>':
				append('\\');
				append('>');
				break;
			case '<':
				append('\\');
				append('<');
				break;
			case '\'':
				append('\\');
				append('\'');
				break;
			case '\\':
				append('\\');
				append('\\');
				break;
			case '\n':
				append('\\');
				append('n');
				break;
			case '\r':
				append('\\');
				append('r');
				break;
			case '\t':
				append('\\');
				append('t');
				break;
			default:
				append((char) ch);
			}
		}
		append('\"');
		return o;
	}


	@Override
	public IValue visitTuple(ITuple o) throws IOException {
		append('<');

		Iterator<IValue> it = o.iterator();

		if(it.hasNext()) {
			it.next().accept(this);
		}

		while(it.hasNext()) {
			append(',');
			it.next().accept(this);
		}
		append('>');

		return o;
	}


	private void append(char c) throws IOException {
		try {
			stream.write(c);
		}
		catch(IOException e) {
			throw new IOException(e);
		}
	}


	private boolean checkIndent(IList o) {
		if(indented && o.length() > 1) {
			for(IValue x : o) {
				Type type = x.getType();
				if(type.isNode() || type.isTuple() || type.isList() || type.isSet() || type.isMap() || type.isRelation()) {
					return true;
				}
			}
		}
		return false;
	}


	private boolean checkIndent(IMap o) {
		if(indented && o.size() > 1) {
			for(IValue x : o) {
				Type type = x.getType();
				Type vType = o.get(x).getType();
				if(type.isNode() || type.isTuple() || type.isList() || type.isSet() || type.isMap() || type.isRelation()) {
					return true;
				}
				if(vType.isNode() || vType.isTuple() || vType.isList() || vType.isSet() || vType.isMap() || vType.isRelation()) {
					return true;
				}
			}
		}
		return false;
	}


	private boolean checkIndent(INode o) {
		if(indented && o.arity() > 1) {
			for(IValue x : o) {
				Type type = x.getType();
				if(type.isNode() || type.isTuple() || type.isList() || type.isSet() || type.isMap() || type.isRelation()) {
					return true;
				}
			}
		}
		return false;
	}


	private boolean checkIndent(ISet o) {
		if(indented && o.size() > 1) {
			for(IValue x : o) {
				Type type = x.getType();
				if(type.isNode() || type.isTuple() || type.isList() || type.isSet() || type.isMap() || type.isRelation()) {
					return true;
				}
			}
		}
		return false;
	}


	private void indent() throws IOException {
		indent(indented);
	}


	private void indent(boolean indent) throws IOException {
		if(indent) {
			append('\n');
			for(int i = 0; i < tabSize * tabLevel; i++) {
				append(' ');
			}
		}
	}


	private void tab() {
		this.tabLevel++;
	}


	private void untab() {
		this.tabLevel--;
	}


	protected void append(String string) throws IOException {
		stream.write(string.getBytes());
	}
}
