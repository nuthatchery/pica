package org.nuthatchery.pica.resources.internal;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.resources.ILanguage;
import org.nuthatchery.pica.resources.IXRefInfo;
import org.nuthatchery.pica.resources.managed.IManagedCodeUnit;
import org.nuthatchery.pica.resources.managed.IManagedContainer;
import org.nuthatchery.pica.resources.regions.IOffsetLength;
import org.nuthatchery.pica.resources.storage.IStorage;
import org.nuthatchery.pica.util.ISignature;
import org.nuthatchery.pica.util.Pair;
import org.nuthatchery.pica.tasks.ITaskMonitor;

public class SpecialCodeUnit extends AbstractManagedResource implements IManagedCodeUnit {
	public static final IManagedCodeUnit INCOMPLETE_DEPENDS = new SpecialCodeUnit("__INCOMPLETE_DEPENDS__");
	private final String name;


	private SpecialCodeUnit(String name) {
		super(URI.create("special://" + name), null);
		this.name = name;
	}


	@Override
	public Collection<? extends IManagedCodeUnit> getDepends(ITaskMonitor rm) {
		return Collections.emptySet();
	}


	@Override
	public ISignature getFullSignature(ITaskMonitor tm) {
		return null;
	}


	@Override
	public Object getId() {
		throw new UnsupportedOperationException();
	}


	@Override
	public String getKind(ITaskMonitor rm) {
		return "__SPECIAL__";
	}


	@Override
	public ILanguage getLanguage() {
		return SpecialLanguage.INSTANCE;
	}


	@Override
	public long getLength() throws UnsupportedOperationException, IOException {
		return 0;
	}


	@Override
	public String getName() {
		return name;
	}


	@Override
	public long getOffset() throws UnsupportedOperationException {
		return 0;
	}


	@Override
	@Nullable
	public IManagedContainer getParent() {
		return null;
	}


	@Override
	public ISignature getSourceSignature(ITaskMonitor rm) {
		throw new UnsupportedOperationException();
	}


	@Override
	@Nullable
	public IStorage getStorage() {
		return null;
	}


	@Override
	public Collection<? extends IManagedCodeUnit> getTransitiveDepends(ITaskMonitor rm) {
		return Collections.emptySet();
	}


	@Override
	@Nullable
	public IXRefInfo getXRefs(IOffsetLength loc, ITaskMonitor rm) {
		return null;
	}


	@Override
	public Collection<Pair<IOffsetLength, Object>> getXRefs(ITaskMonitor rm) {
		return Collections.emptySet();
	}


	@Override
	public boolean hasIncompleteDepends(ITaskMonitor rm) {
		return false;
	}


	@Override
	public boolean isCodeUnit() {
		return true;
	}


	@Override
	public boolean isFragment() {
		return false;
	}


	@Override
	public boolean isRoot() {
		return false;
	}


	@Override
	public void onDependencyChanged() {
	}


	@Override
	public void onResourceChanged() {
	}

}
