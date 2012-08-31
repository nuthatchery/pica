package org.magnolialang.resources;

import org.eclipse.core.runtime.IPath;

public interface IManagedResourceListener {
	void resourceAdded(IPath path);


	void resourceRemoved(IPath path);


	void resourceChanged(IPath path);
}
