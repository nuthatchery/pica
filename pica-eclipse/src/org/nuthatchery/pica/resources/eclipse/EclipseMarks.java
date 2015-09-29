package org.nuthatchery.pica.resources.eclipse;

import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.errors.ErrorMarkers;
import org.nuthatchery.pica.errors.Severity;
import org.nuthatchery.pica.resources.managed.IManagedResource;
import org.nuthatchery.pica.resources.marks.IMark;
import org.nuthatchery.pica.resources.marks.MarkBuilder;
import org.nuthatchery.pica.util.NullnessHelper;

public class EclipseMarks {
	private static final String CONTEXT_ID = "picaContextId";
	private static final String XREF_PREFIX = "picaXref_";


	private EclipseMarks() {

	}


	/**
	 * Link a Pica mark with an Eclipse marker, so that the offset and length is
	 * automatically updated.
	 * 
	 * @param mark
	 *            The Pica mark
	 * @param marker
	 *            The Eclipse marker
	 * @return A linked marker
	 */
	public static IMark linkWithMarker(IMark mark, IMarker marker) {
		if(mark instanceof LinkedMark) {
			return new LinkedMark(((LinkedMark) mark).mark, marker);
		}
		else {
			return new LinkedMark(mark, marker);
		}
	}


	/**
	 * Build a Pica mark from an Eclipse marker.
	 * 
	 * To be used when restoring the workbench state by finding all persistent
	 * Eclipse markers in a project.
	 * 
	 * @param resource
	 *            The resource the marker is set on
	 * @param marker
	 *            The marker
	 * @return A corresponding mark, linked to the Eclipse marker
	 * @throws CoreException
	 * @see @{@link #linkWithMarker(IMark, IMarker)}
	 */
	public static IMark markerToMark(IManagedResource resource, IMarker marker) throws CoreException {
		MarkBuilder builder = new MarkBuilder();
		int start = marker.getAttribute(IMarker.CHAR_START, -1);
		int end = marker.getAttribute(IMarker.CHAR_END, -1);
		if(start != -1) {
			builder.at(start);
			if(end != -1)
				builder.length(end - start);
		}

		builder.message(NullnessHelper.assertNonNull(marker.getAttribute(IMarker.MESSAGE, "")));

		int severity = marker.getAttribute(IMarker.SEVERITY, Severity.DEFAULT.getValue());
		if(severity == IMarker.SEVERITY_ERROR)
			builder.severity(Severity.ERROR);
		else if(severity == IMarker.SEVERITY_WARNING)
			builder.severity(Severity.WARNING);
		else if(severity == IMarker.SEVERITY_INFO)
			builder.severity(Severity.INFO);
		else
			builder.severity(Severity.DEFAULT);

		if(marker.getAttribute(IMarker.TRANSIENT, true))
			builder.nonPersistent();

		String context = marker.getAttribute(CONTEXT_ID, null);
		if(context != null)
			builder.context(context);

		Map<String, Object> attributes = marker.getAttributes();
		for(Entry<String, Object> e : attributes.entrySet()) {
			if(e.getKey().startsWith(XREF_PREFIX)) {
				String xrefName = e.getKey().substring(0, XREF_PREFIX.length());
				System.err.println("EclipseMarks: Xrefname: " + xrefName + " xref to: " + e.getValue());
			}
		}

		builder.uri(resource.getURI());

		return linkWithMarker(builder.done(), marker);
	}


	/**
	 * Make an Eclipse marker for a Pica mark.
	 * 
	 * The mark will be attached to the resource immediately.
	 * 
	 * Further processing of the Pica mark should be done on a linked mark.
	 * 
	 * @param resource
	 *            The Eclipse resource the mark should be associated with
	 * @param mark
	 *            The mark
	 * @throws CoreException
	 * @see @{@link #linkWithMarker(IMark, IMarker)}
	 */
	public static IMarker markToMarker(IResource resource, IMark mark) throws CoreException {
		IMarker marker = resource.createMarker(ErrorMarkers.TYPE_DEFAULT);
		if(mark.hasOffsetAndLength()) {
			marker.setAttribute(IMarker.CHAR_START, mark.getOffset());
			marker.setAttribute(IMarker.CHAR_END, mark.getOffset() + mark.getLength());
		}
		marker.setAttribute(IMarker.MESSAGE, mark.getMessage());
		marker.setAttribute(IMarker.SEVERITY, mark.getSeverity().getValue());
		marker.setAttribute(IMarker.TRANSIENT, true);
		marker.setAttribute(IMarker.SOURCE_ID, mark.getSource());
		String context = mark.getContext();
		if(context != null)
			marker.setAttribute(CONTEXT_ID, context);
		return marker;
	}


	static class LinkedMark implements IMark {
		IMark mark;
		IMarker marker;


		public LinkedMark(IMark mark, IMarker marker) {
			this.mark = mark;
			this.marker = marker;
		}


		@Override
		public @Nullable
		String getContext() {
			return mark.getContext();
		}


		@Override
		public int getLength() {
			int start = marker.getAttribute(IMarker.CHAR_START, -1);
			int end = marker.getAttribute(IMarker.CHAR_END, -1);
			if(start == -1 || end == -1) {
				throw new UnsupportedOperationException();
			}

			return end - start;
		}


		@Override
		public String getMessage() {
			return mark.getMessage();
		}


		@Override
		public int getOffset() {
			int start = marker.getAttribute(IMarker.CHAR_START, -1);
			if(start == -1) {
				throw new UnsupportedOperationException();
			}

			return start;
		}


		@Override
		public @Nullable
		IMark getRelation(String relationName) {
			return mark.getRelation(relationName);
		}


		@Override
		public Iterable<String> getRelations() {
			return mark.getRelations();
		}


		@Override
		public Severity getSeverity() {
			return mark.getSeverity();
		}


		@Override
		public String getSource() {
			return mark.getSource();
		}


		@Override
		public URI getURI() {
			return mark.getURI();
		}


		@Override
		public boolean hasOffsetAndLength() {
			int start = marker.getAttribute(IMarker.CHAR_START, -1);
			int end = marker.getAttribute(IMarker.CHAR_END, -1);
			return !(start == -1 || end == -1);
		}


		@Override
		public boolean isTransient() {
			return mark.isTransient();
		}


		@Override
		public String toString() {
			return mark.toString();
		}
	}
}
