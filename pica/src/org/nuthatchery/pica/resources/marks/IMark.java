package org.nuthatchery.pica.resources.marks;

import java.net.URI;

import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.errors.Severity;

public interface IMark {
	@Nullable
	String getContext();


	long getLength();


	String getMessage();


	long getOffset();


	@Nullable
	IMark getRelation(String relationName);


	Iterable<String> getRelations();


	Severity getSeverity();


	String getSource();


	URI getURI();


	boolean hasOffsetAndLength();


	boolean isTransient();

}
