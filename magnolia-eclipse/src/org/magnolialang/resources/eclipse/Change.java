package org.magnolialang.resources.eclipse;

import java.net.URI;

import org.eclipse.core.resources.IResource;

class Change {
	final Change.Kind	kind;
	final URI			uri;
	final IResource		resource;


	Change(URI uri, IResource resource, Change.Kind kind) {
		this.uri = uri;
		this.kind = kind;
		this.resource = resource;
	}


	enum Kind {
		ADDED, REMOVED, CHANGED
	}
}
