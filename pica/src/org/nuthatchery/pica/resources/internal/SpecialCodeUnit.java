package org.nuthatchery.pica.resources.internal;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.resources.ILanguage;
import org.nuthatchery.pica.resources.IXRefInfo;
import org.nuthatchery.pica.resources.managed.IManagedCodeUnit;
import org.nuthatchery.pica.resources.managed.IManagedResource;
import org.nuthatchery.pica.resources.storage.IStorage;
import org.nuthatchery.pica.util.ISignature;
import org.nuthatchery.pica.util.Pair;
import org.rascalmpl.debug.IRascalMonitor;
import org.rascalmpl.uri.URIUtil;

public class SpecialCodeUnit extends AbstractManagedResource implements IManagedCodeUnit {
	public static final IManagedCodeUnit INCOMPLETE_DEPENDS = new SpecialCodeUnit("__INCOMPLETE_DEPENDS__");
	private final String name;


	private SpecialCodeUnit(String name) {
		super(URIUtil.assumeCorrect("special://" + name));
		this.name = name;
	}


	@Override
	public boolean exists() {
		return true;
	}


	@Override
	public IConstructor getAST(IRascalMonitor rm) {
		throw new UnsupportedOperationException();
	}


	@Override
	public IManagedResource getContainingFile() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}


	@Override
	public Collection<? extends IManagedCodeUnit> getDepends(IRascalMonitor rm) {
		return Collections.EMPTY_SET;
	}


	@Override
	public IConstructor getId() {
		throw new UnsupportedOperationException();
	}


	@Override
	public String getKind(IRascalMonitor rm) {
		return "__SPECIAL__";
	}


	@Override
	public ILanguage getLanguage() {
		return SpecialLanguage.INSTANCE;
	}


	@Override
	public int getLength() throws UnsupportedOperationException, IOException {
		return 0;
	}


	@Override
	public long getModificationStamp() {
		return 0;
	}


	@Override
	public String getName() {
		return name;
	}


	@Override
	public int getOffset() throws UnsupportedOperationException {
		return 0;
	}


	@Override
	@Nullable
	public IManagedResource getParent() {
		return null;
	}


	@Override
	public ISignature getSourceSignature(IRascalMonitor rm) {
		throw new UnsupportedOperationException();
	}


	@Override
	@Nullable
	public IStorage getStorage() {
		return null;
	}


	@Override
	public Collection<? extends IManagedCodeUnit> getTransitiveDepends(IRascalMonitor rm) {
		return Collections.EMPTY_SET;
	}


	@Override
	public Collection<Pair<ISourceLocation, IConstructor>> getXRefs(IRascalMonitor rm) {
		return Collections.EMPTY_SET;
	}


	@Override
	@Nullable
	public IXRefInfo getXRefs(ISourceLocation loc, IRascalMonitor rm) {
		return null;
	}


	@Override
	public boolean hasIncompleteDepends(IRascalMonitor rm) {
		return false;
	}


	@Override
	public boolean isCodeUnit() {
		return true;
	}


	@Override
	public boolean isContainer() {
		return true;
	}


	@Override
	public boolean isFile() {
		return true;
	}


	@Override
	public boolean isFragment() {
		return false;
	}


	@Override
	public boolean isProject() {
		return false;
	}


	@Override
	public void onDependencyChanged() {
	}


	@Override
	public void onResourceChanged() {
	}

}
