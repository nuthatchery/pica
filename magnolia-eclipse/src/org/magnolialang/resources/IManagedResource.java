package org.magnolialang.resources;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public interface IManagedResource {
	enum Kind {
		FOLDER, FILE, CODE, PROJECT
	};


	/**
	 * @return The parent, or null if getKind() == PROJECT
	 */
	IManagedResource getParent();


	Kind getResourceKind();


	URI getURI();


	boolean isFile();


	boolean isFolder();


	IPath getPath();


	IPath getFullPath();


	IProject getProject();


	long getModificationStamp();


	void onResourceChanged();
}
