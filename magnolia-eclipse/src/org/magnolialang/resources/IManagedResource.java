package org.magnolialang.resources;

import java.net.URI;

import org.eclipse.imp.pdb.facts.IExternalValue;
import org.eclipse.imp.pdb.facts.type.ExternalType;
import org.eclipse.imp.pdb.facts.type.Type;

public interface IManagedResource extends IExternalValue {
	public static final Type	ResourceType	= new ExternalType() {
	};


	long getModificationStamp();


	/**
	 * @return The parent, or null if getKind() == PROJECT
	 * @axiom getParent().isContainer() is always true
	 */
	IManagedResource getParent();


	URI getURI();


	/**
	 * A code unit may also be a file and/or a container.
	 * 
	 * @return true if the pkg is a package
	 */
	boolean isCodeUnit();


	/**
	 * A file system folder or a package.
	 * 
	 * @return true if the pkg can have children
	 */
	boolean isContainer();


	/**
	 * A file or a package.
	 * 
	 * @return true if the pkg is a file or a package
	 */
	boolean isFile();


	/**
	 * isProject() implies isContainer()
	 * 
	 * @return true if the pkg is a project
	 */
	boolean isProject();


	void onResourceChanged();
}
