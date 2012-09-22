package org.magnolialang.resources;

import java.net.URI;

public interface IManagedResourceListener {
	void resourceAdded(URI uri);


	void resourceChanged(URI uri);


	void resourceRemoved(URI uri);
}
