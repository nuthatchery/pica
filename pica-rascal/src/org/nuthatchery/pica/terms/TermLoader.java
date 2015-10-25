package org.nuthatchery.pica.terms;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.rascalmpl.value.IConstructor;
import org.rascalmpl.value.IValue;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class TermLoader {
	final Pattern stringPattern = Pattern.compile("(([^\"\\\\]|\\\\\")*)\"");
	final Pattern namePattern = Pattern.compile("(([\\w\\d@:])*)(\\z|[^\\w\\d@:])");
	String input = "";
	int pos = 0;
	int length = 0;


	public TermLoader(String inputString) {
		input = inputString;
		pos = 0;
		length = inputString.length();
	}


	public IConstructor parseTerm() {
		if(reading("[")) {
			ArrayList<IConstructor> children = new ArrayList<>();
			if(!reading("]")) {
				do {
					children.add(parseTerm());
				}
				while(reading(","));
				expect("]");
			}
			return TermFactory.seq(children.toArray(new IValue[children.size()]));
		}
		else if(reading("\"")) {
			Matcher matcher = stringPattern.matcher(input).region(pos, length);
			if(matcher.lookingAt()) {
				String str = matcher.group(1);
				System.err.println("Match string: " + matcher.start() + " to " + matcher.end());
				pos = matcher.end();
				return TermFactory.leaf(str);
			}
			else
				throw new ParseError("\"");
		}
		else {
			skipSpaces();
			Matcher matcher = namePattern.matcher(input).region(pos, length);
			if(matcher.lookingAt()) {
				String name = matcher.group(1);
				pos = pos + name.length();
				if(name.equals(""))
					name = "_";
				ArrayList<IConstructor> children = new ArrayList<>();
				if(reading("(")) {
					if(!reading(")")) {
						do {
							children.add(parseTerm());
						}
						while(reading(","));
						expect(")");
					}
					return TermFactory.cons(name, children.toArray(new IValue[children.size()]));
				}
				else {
					return TermFactory.leaf(name);
				}
			}
			else
				throw new ParseError("<constructor name>");
		}
	}


	private void expect(String string) {
		if(!reading(string))
			throw new ParseError(string);
	}


	private boolean reading(String string) {
		skipSpaces();
		if(input.startsWith(string, pos)) {
			pos = pos + string.length();
			return true;
		}
		else
			return false;
	}


	private void skipSpaces() {
		while(pos < length && Character.isWhitespace(input.charAt(pos))) {
			pos++;
		}
	}


	public class ParseError extends RuntimeException {
		private static final long serialVersionUID = -2955892082668393274L;
		final String input;
		final int pos;
		@Nullable
		final String expected;


		ParseError() {
			super("Parse error at position " + TermLoader.this.pos);
			this.input = TermLoader.this.input;
			this.pos = TermLoader.this.pos;
			this.expected = null;
		}


		ParseError(String expected) {
			super("Parse error at position " + TermLoader.this.pos + ", expected '" + expected + "'");
			this.input = TermLoader.this.input;
			this.pos = TermLoader.this.pos;
			this.expected = expected;
		}


		public String getMessage() {
			String message = super.getMessage();
			int i = input.indexOf('\n', pos);
			String substring = input.substring(pos, i < 0 ? length : i);
			return message + "\n'" + substring + "'";
		}
	}
}
