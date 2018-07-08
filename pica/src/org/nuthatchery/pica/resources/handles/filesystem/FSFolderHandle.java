package org.nuthatchery.pica.resources.handles.filesystem;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import org.nuthatchery.pica.resources.handles.IFolderHandle;
import org.nuthatchery.pica.resources.handles.IResourceHandle;
import org.nuthatchery.pica.resources.handles.PicaOpenOption;
import org.nuthatchery.pica.tasks.ITaskMonitor;
import org.nuthatchery.pica.tasks.TaskId;

public class FSFolderHandle extends AbstractFSResourceHandle implements IFolderHandle {

	protected FSFolderHandle(Path path) {
		super(path);
	}


	@Override
	public void create(ITaskMonitor tm, OpenOption... flags) throws IOException {
		tm.begin(new TaskId("FSFolderHandle.create", "Create " + resource, resource));

		boolean makeParents = false;
		for(OpenOption o : flags) {
			if(o == PicaOpenOption.CREATE_PARENTS) {
				makeParents = true;
			}
		}

		if(makeParents) {
			Files.createDirectories(resource);
		}
		else {
			Files.createDirectory(resource);
		}
	}


	@Override
	public Collection<IResourceHandle> getChildren(ITaskMonitor tm) throws IOException {
		Collection<IResourceHandle> children = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(resource)) {
			for(Path p : stream) {
				children.add(AbstractFSResourceHandle.makeHandle(p));
			}
		}

		return children;
	}


	@Override
	public Collection<IResourceHandle> getChildren(String glob, ITaskMonitor tm) throws IOException {
		Collection<IResourceHandle> children = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(resource, glob)) {
			for(Path p : stream) {
				children.add(AbstractFSResourceHandle.makeHandle(p));
			}
		}

		return children;
	}


	@Override
	public boolean isFile() {
		return true;
	}


	@Override
	public boolean isFolder() {
		return true;
	}
}
