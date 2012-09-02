package org.magnolialang.resources;

import java.net.URI;

import org.eclipse.imp.pdb.facts.IExternalValue;
import org.eclipse.imp.pdb.facts.type.ExternalType;
import org.eclipse.imp.pdb.facts.type.Type;

public interface IManagedResource extends IExternalValue {
	public static final Type	ResourceType	= new ExternalType() {
												};


	/**
	 * @return The parent, or null if getKind() == PROJECT
	 * @axiom getParent().isContainer() is always true
	 */
	IManagedResource getParent();


	URI getURI();


	/**
	 * A file or a package.
	 * 
	 * @return true if the resource is a file or a package
	 */
	boolean isFile();


	/**
	 * A file system folder or a package.
	 * 
	 * @return true if the resource can have children
	 */
	boolean isContainer();


	/**
	 * A code unit may also be a file and/or a container.
	 * 
	 * @return true if the resource is a package
	 */
	boolean isCodeUnit();


	/**
	 * isProject() implies isContainer()
	 * 
	 * @return true if the resource is a project
	 */
	boolean isProject();


	long getModificationStamp();


	void onResourceChanged();
}
