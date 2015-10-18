package org.nuthatchery.pica.resources.handles;

import java.io.IOException;
import java.util.Collection;

import org.nuthatchery.pica.tasks.ITaskMonitor;

public interface IFolderHandle extends IResourceHandle {
	Collection<IResourceHandle> getChildren(ITaskMonitor tm) throws IOException;


	Collection<IResourceHandle> getChildren(String glob, ITaskMonitor tm) throws IOException;

}
