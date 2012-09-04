package org.magnolialang.resources;

import java.util.Collection;

import org.rascalmpl.interpreter.IRascalMonitor;

public interface IManagedContainer extends IManagedResource {
	Collection<? extends IManagedResource> getChildren(IRascalMonitor rm);
}
