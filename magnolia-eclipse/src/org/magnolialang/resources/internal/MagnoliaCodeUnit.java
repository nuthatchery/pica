package org.magnolialang.resources.internal;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;
import org.magnolialang.resources.ILanguage;
import org.magnolialang.resources.IManagedCodeUnit;
import org.magnolialang.resources.IManagedPackage;
import org.magnolialang.resources.IManagedResource;
import org.rascalmpl.interpreter.IRascalMonitor;

public class MagnoliaCodeUnit implements IManagedCodeUnit {

	private final IConstructor		defInfo;
	private final IManagedPackage	parent;
	private final URI				uri;
	private final IConstructor		id;


	public MagnoliaCodeUnit(IConstructor defInfo, IManagedPackage parent) {
		this.defInfo = defInfo;
		this.parent = parent;
		this.id = (IConstructor) defInfo.get("qName");
		this.uri = parent.getURI().resolve("#" + parent.getLanguage().getNameString(id));
	}


	@Override
	public IManagedResource getParent() {
		return parent;
	}


	@Override
	public boolean isFile() {
		return false;
	}


	@Override
	public boolean isContainer() {
		return false;
	}


	@Override
	public boolean isCodeUnit() {
		return true;
	}


	@Override
	public boolean isProject() {
		return false;
	}


	@Override
	public void onResourceChanged() {
		throw new UnsupportedOperationException();
	}


	@Override
	public ILanguage getLanguage() {
		return parent.getLanguage();
	}


	@Override
	public Collection<IManagedPackage> getDepends(IRascalMonitor rm) {
		return Collections.EMPTY_SET;
	}


	@Override
	public Collection<IManagedPackage> getTransitiveDepends(IRascalMonitor rm) {
		return Collections.EMPTY_SET;
	}


	@Override
	public IConstructor getId() {
		return id;
	}


	@Override
	public String getName() {
		return parent.getLanguage().getNameString(id);
	}


	@Override
	public IConstructor getAST(IRascalMonitor rm) {
		return (IConstructor) defInfo.get("body");
	}


	@Override
	public IConstructor getDefInfo(IRascalMonitor rm) {
		return defInfo;
	}


	@Override
	public URI getURI() {
		return uri;
	}


	@Override
	public long getModificationStamp() {
		return parent.getModificationStamp();
	}


	@Override
	public Type getType() {
		return IManagedResource.ResourceType;
	}


	@Override
	public <T> T accept(IValueVisitor<T> v) throws VisitorException {
		return v.visitConstructor(defInfo);
	}


	@Override
	public boolean isEqual(IValue other) {
		if(other instanceof IManagedCodeUnit) {
			return defInfo.isEqual(((IManagedCodeUnit) other).getDefInfo(null));
		}
		else
			return false;
	}

}
