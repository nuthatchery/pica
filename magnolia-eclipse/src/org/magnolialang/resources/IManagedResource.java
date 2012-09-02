package org.magnolialang.resources;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.pdb.facts.IExternalValue;
import org.eclipse.imp.pdb.facts.type.ExternalType;
import org.eclipse.imp.pdb.facts.type.Type;

public interface IManagedResource extends IExternalValue {
	public static final Type	ResourceType	= new ExternalType() {
												};


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
