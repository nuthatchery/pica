package org.nuthatchery.pica.resources.marks;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.errors.Severity;
import org.nuthatchery.pica.util.NullnessHelper;

public class MarkBuilder {
	private final Mark mark;


	/**
	 * Create a mark builder.
	 * 
	 * <p>
	 * Use the methods of the builder to add information to the mark, then call
	 * {@link #done()} in order to obtain a usable mark.
	 * 
	 * <p>
	 * The following information should be provided, at a minimum:
	 * 
	 * <li>{@link #uri(URI)} the resource on which the mark should appear</li>
	 * <li>{@link #source(String)} id of the code that generated the mark</li>
	 * <li>{@link #message(String)} the message itself</li>
	 * 
	 * <p>
	 * In addition, the following information should be provided if available:
	 * 
	 * <li>{@link #at(int)} start position of the marked region</li>
	 * <li>{@link #length(int)} length of the marked region</li>
	 * <li>{@link #severity(Severity)} severity of the message</li>
	 */
	public MarkBuilder() {
		this.mark = new Mark();
	}


	public MarkBuilder(IMark mark) {
		this.mark = new Mark(mark);
	}


	private MarkBuilder(MarkBuilder src) {
		this.mark = new Mark(src.mark);
	}


	/**
	 * Set the start position of the mark.
	 * 
	 * @param offset
	 *            The start offset, with 0 being the first character
	 * @return The current mark builder, modified
	 */
	public MarkBuilder at(int offset) {
		if(offset < 0) {
			throw new IllegalArgumentException();
		}
		mark.offset = offset;
		return this;
	}


	/**
	 * Refers to another mark as the cause of this one.
	 * 
	 * @param mark
	 *            The other mark
	 * @return The current mark builder, modified
	 */
	public MarkBuilder causedBy(IMark mark) {
		return xref("Caused by", mark);
	}


	public MarkBuilder context(@Nullable String context) {
		mark.context = context;
		return this;

	}


	/**
	 * Copy this builder.
	 * 
	 * Allows a builder to be easily used as a template for constructing marks.
	 * 
	 * @return An exact copy of this builder.
	 */
	public MarkBuilder copy() {
		return new MarkBuilder(this);
	}


	/**
	 * Finalize construction of the mark.
	 * 
	 * @return A mark
	 * @throws IllegalArgumentException
	 *             if sufficient information is not provided to build a mark
	 */
	public IMark done() throws IllegalArgumentException {
		Mark m = new Mark(mark);

		if(m.uri == null) {
			throw new IllegalArgumentException("No URI given");
		}
		if(m.source == null) {
			throw new IllegalArgumentException("No source given");
		}
		if(m.message == null) {
			throw new IllegalArgumentException("No message given");
		}
		if(m.offset >= 0 && m.length < 0) {
			m.length = 1;
		}
		if(m.length >= 0 && m.offset < 0) {
			throw new IllegalArgumentException("Length without offset");
		}
		return m;
	}


	/**
	 * Set the length of the code area of the mark.
	 * 
	 * @param length
	 *            The length
	 * @return The current mark builder, modified
	 */
	public MarkBuilder length(int length) {
		if(length < 0) {
			throw new IllegalArgumentException();
		}
		mark.length = length;
		return this;
	}


	public MarkBuilder loc(ISourceLocation loc) {
		uri(NullnessHelper.assertNonNull(loc.getURI()));
		if(loc.hasOffsetLength()) {
			at(loc.getOffset());
			length(loc.getLength());
		}
		return this;
	}


	public MarkBuilder message(String message) {
		mark.message = message;
		return this;
	}


	/**
	 * Indicate that the mark is transient, and should not be stored across
	 * editing sessions.
	 * 
	 * @return The current mark builder, modified.
	 */
	public MarkBuilder nonPersistent() {
		mark.isTransient = true;
		return this;
	}


	public MarkBuilder severity(Severity severity) {
		mark.severity = severity;
		return this;
	}


	/**
	 * Indicte the part of the system that generated the mark.
	 * 
	 * @param source
	 *            A unique identifier of a part of the system (e.g., qualified
	 *            class / method name)
	 * @return The current mark builder, modified
	 */
	public MarkBuilder source(String source) {
		mark.source = source;
		return this;
	}


	/**
	 * Set the URI of the mark.
	 * 
	 * @param uri
	 *            The URI
	 * @return The mark builder, modified
	 */
	public MarkBuilder uri(URI uri) {
		mark.uri = uri;
		return this;
	}


	public MarkBuilder xref(String relation, IMark mark) {
		this.mark.xrefs.put(relation, mark);
		return this;

	}


	static class Mark implements IMark {
		@Nullable
		protected String source = null;
		@Nullable
		protected String context = null;
		@Nullable
		protected URI uri = null;
		protected int offset = -1;
		protected int length = -1;
		@Nullable
		protected String message = null;
		protected Severity severity = Severity.DEFAULT;
		protected Map<String, IMark> xrefs = new HashMap<String, IMark>();
		protected boolean isTransient;


		protected Mark() {
		}


		protected Mark(IMark src) {
			this.xrefs = new HashMap<String, IMark>(xrefs);
			this.length = src.hasOffsetAndLength() ? src.getLength() : -1;
			this.offset = src.hasOffsetAndLength() ? src.getOffset() : -1;
			this.source = src.getSource();
			this.uri = src.getURI();
			this.message = src.getMessage();
			this.severity = src.getSeverity();
			this.isTransient = src.isTransient();
			this.context = src.getContext();
		}


		@Override
		@Nullable
		public String getContext() {
			return context;
		}


		@Override
		public int getLength() {
			if(!hasOffsetAndLength()) {
				throw new UnsupportedOperationException();
			}
			return length;
		}


		@Override
		public String getMessage() {
			assert message != null;
			return message;
		}


		@Override
		public int getOffset() {
			if(!hasOffsetAndLength()) {
				throw new UnsupportedOperationException();
			}
			return offset;
		}


		@Override
		@Nullable
		public IMark getRelation(String relationName) {
			return xrefs.get(relationName);
		}


		@Override
		public Iterable<String> getRelations() {
			return NullnessHelper.assertNonNull(xrefs.keySet());
		}


		@Override
		public Severity getSeverity() {
			return severity;
		}


		@Override
		public String getSource() {
			assert source != null;
			return source;
		}


		@Override
		public URI getURI() {
			assert uri != null;
			return uri;
		}


		@Override
		public boolean hasOffsetAndLength() {
			return offset >= 0;
		}


		@Override
		public boolean isTransient() {
			return isTransient;
		}


		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			b.append("Mark(");
			b.append("uri=");
			b.append(getURI());
			b.append(", at=");
			b.append(getOffset());
			b.append(", length=");
			b.append(getLength());
			b.append(", source=\"");
			b.append(getSource());
			b.append("\"");
			if(getContext() != null) {
				b.append(", context=\"");
				b.append(getContext());
				b.append("\"");
			}
			b.append(", severity=");
			b.append(getSeverity().name());
			b.append(", transient=");
			b.append(isTransient());
			b.append(")");
			return b.toString();
		}
	}
}
