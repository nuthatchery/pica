package org.magnolialang.resources.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;
import org.magnolialang.compiler.ICompiler;
import org.magnolialang.eclipse.MagnoliaPlugin;
import org.magnolialang.errors.ErrorMarkers;
import org.magnolialang.errors.ImplementationError;
import org.magnolialang.nullness.Nullable;
import org.magnolialang.resources.ILanguage;
import org.magnolialang.resources.IManagedPackage;
import org.magnolialang.resources.IManagedResource;
import org.magnolialang.resources.IManagedResourceListener;
import org.magnolialang.resources.IResourceManager;
import org.magnolialang.resources.IWorkspaceManager;
import org.magnolialang.resources.LanguageRegistry;
import org.magnolialang.resources.WorkspaceManager;
import org.rascalmpl.interpreter.IRascalMonitor;

public final class ProjectManager implements IResourceManager {
	ReadWriteLock									lock				= new ReentrantReadWriteLock();
	private final IWorkspaceManager					manager;
	private final Map<URI, IManagedResource>		resources			= new HashMap<URI, IManagedResource>();
	private final Map<String, IManagedPackage>		packagesByName		= new HashMap<String, IManagedPackage>();
	private final Map<URI, String>					packageNamesByURI	= new HashMap<URI, String>();
	private final Map<ILanguage, ICompiler>			compilers			= new HashMap<ILanguage, ICompiler>();
	private final IProject							project;
	private final IPath								basePath;
	private final List<IManagedResourceListener>	listeners			= new ArrayList<IManagedResourceListener>();
	private final static String						MODULE_LANG_SEP		= "%";
	private static final String						OUT_FOLDER			= "cxx";
	private static final boolean					debug				= false;
	private final IPath								srcPath;
	private final IPath								outPath;


	public ProjectManager(IWorkspaceManager manager, IProject project) {

		this.manager = manager;
		this.project = project;
		this.basePath = project.getFullPath();
		srcPath = null;
		outPath = project.getFolder(OUT_FOLDER).getFullPath();
		System.err.println("New projectmanager: basepath=" + basePath);
		addAllResources();

		dataInvariant();
	}


	private void addAllResources() {
		try {
			project.accept(new IResourceVisitor() {
				@Override
				public boolean visit(IResource resource) {
					if(resource.getType() == IResource.FILE) {
						addResource(resource);
					}
					return true;
				}

			});
		}
		catch(CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Override
	public IManagedResource findResource(URI uri) {
		Lock l = lock.readLock();
		l.lock();
		try {

			// see if we already track the URI
			IManagedResource res = resources.get(uri);
			if(res != null)
				return res;

			String scheme = uri.getScheme();

			// check if it is a project URI 
			if(scheme.equals("project")) {
				if(uri.getHost().equals(project.getName()))
					return null; // we should already have found it if we were tracking it
				else {
					IResourceManager mng = WorkspaceManager.getManager(uri.getHost());
					if(mng != null)
						return mng.findResource(uri);
					else
						return null;
				}
			}
			else if(scheme.equals("magnolia")) {
				return null; // not handled yet
			}
			// see if we can find it using Eclipse's resource system
			try {
				IFileStore store = EFS.getStore(uri);
				return findResource(uri, store);
			}
			catch(CoreException e) {

			}

			// give up
			return null;
		}
		finally {
			l.unlock();
		}
	}


	private IManagedResource findResource(URI uri, IFileStore store) {
		IResource[] rs;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IManagedResource res = null;
		if(store.fetchInfo().isDirectory())
			rs = root.findContainersForLocationURI(uri);
		else
			rs = root.findFilesForLocationURI(uri);
		// search in *this* project first
		for(IResource r : rs) {
			if(r.getProject().equals(project))
				res = findResource(MagnoliaPlugin.constructProjectURI(project, r.getProjectRelativePath()));
			if(res != null)
				return res;
		}
		for(IResource r : rs) {
			res = findResource(MagnoliaPlugin.constructProjectURI(r.getProject(), r.getProjectRelativePath()));
			if(res != null)
				return res;
		}
		return null;
	}


	@Override
	public IManagedResource findResource(String path) {
		Lock l = lock.readLock();
		l.lock();

		try {
			URI uri = MagnoliaPlugin.constructProjectURI(project, new Path(path));
			System.err.println("find: uri: " + uri);
			IManagedResource resource = findResource(uri);

			System.err.println("find: resource: " + resource);
			return resource;
		}
		finally {
			l.unlock();
		}
	}


	/**
	 * Called by the WorkspaceManager whenever a resource is added to the
	 * project.
	 * 
	 * @param resource
	 */
	public void onResourceAdded(IResource resource) {
		Lock l = lock.writeLock();
		l.lock();

		try {
			addResource(resource);
			dataInvariant();
		}
		finally {
			l.unlock();
		}
	}


	private void addResource(IResource resource) {
//		IResource resource = project.findMember(path);
		if(resource instanceof IFile) {
			URI uri = MagnoliaPlugin.constructProjectURI(project, resource.getProjectRelativePath());
			if(resources.get(uri) != null)
				removeResource(uri);

			ILanguage language = LanguageRegistry.getLanguageForFile(uri);
			if(language != null)
				addPackageResource(uri, resource, language);
			else
				addFileResource(uri, resource);

			for(IManagedResourceListener l : listeners)
				l.resourceAdded(uri);
		}
	}


	private void addFileResource(URI uri, IResource resource) {
		if(debug)
			System.err.println("PROJECT NEW FILE: " + uri);
		ManagedFile file = new ManagedFile(this, resource);
		resources.put(uri, file);
	}


	private void addPackageResource(URI uri, IResource resource, ILanguage lang) {
		if(debug)
			System.err.println("PROJECT NEW MODULE: " + uri);

		if(lang != null) {
			IPath srcRelativePath = resource.getFullPath();
			srcRelativePath = srcRelativePath.makeRelativeTo(getSrcFolder());
			String modName = lang.getModuleName(srcRelativePath.toString());
			IConstructor modId = lang.getNameAST(modName);
			MagnoliaPackage pkg = new MagnoliaPackage(this, resource, modId, lang);
			resources.put(uri, pkg);
			packagesByName.put(lang.getId() + MODULE_LANG_SEP + modName, pkg);
			packageNamesByURI.put(uri, lang.getId() + MODULE_LANG_SEP + modName);
		}
	}


	/**
	 * Called by the WorkspaceManager whenever a resource is removed from the
	 * workspace
	 * 
	 * @param uri
	 *            A full, workspace-relative path
	 */
	public void onResourceRemoved(URI uri) {
		Lock l = lock.writeLock();
		l.lock();

		try {
			removeResource(uri);
			dataInvariant();
		}
		finally {
			l.unlock();
		}
	}


	private void removeResource(URI uri) {
		if(debug)
			System.err.println("PROJECT REMOVED: " + uri);
		IManagedResource removed = resources.remove(uri);
		if(removed != null) {
			for(IManagedResourceListener l : listeners)
				l.resourceRemoved(uri);
		}

		String modName = packageNamesByURI.remove(uri);
		if(modName != null) {
			packagesByName.remove(modName);
		}
	}


	/**
	 * Called by the WorkspaceManager whenever a resource has been changed
	 * (i.e., the file contents have changed)
	 * 
	 * @param uri
	 *            A full, workspace-relative path
	 */
	public void onResourceChanged(URI uri) {
		if(debug)
			System.err.println("PROJECT CHANGED: " + uri);
		IManagedResource resource = resources.get(uri);
		if(resource != null) {
			resource.onResourceChanged();
			for(IManagedResourceListener l : listeners)
				l.resourceChanged(uri);
		}
	}


	@Override
	public void addListener(IManagedResourceListener listener) {
		Lock l = lock.writeLock();
		l.lock();

		try {
			listeners.add(listener);
		}
		finally {
			l.unlock();
		}
	}


	@Override
	public void removeListener(IManagedResourceListener listener) {
		Lock l = lock.writeLock();
		l.lock();

		try {
			listeners.remove(listener);
		}
		finally {
			l.unlock();
		}
	}


	@Override
	public void dispose() {
		Lock l = lock.writeLock();
		l.lock();

		try {
			// for(IPath path : resources.keySet()) {
			// resourceRemoved(path);
			// }
			try {
				if(!resources.isEmpty())
					throw new ImplementationError("Leftover files in project on shutdown: " + project);
				if(!packageNamesByURI.isEmpty())
					throw new ImplementationError("Leftover module-name mappings in project on shutdown: " + project);
				if(!packagesByName.isEmpty())
					throw new ImplementationError("Leftover modules in project on shutdown: " + project);
			}
			finally {
				resources.clear();
				packageNamesByURI.clear();
				packagesByName.clear();
			}
			dataInvariant();
		}
		finally {
			l.unlock();
		}
	}


	private void dataInvariant() {
	}


	@Override
	@Nullable
	public IManagedPackage findPackage(ILanguage language, String moduleName) {
		Lock l = lock.readLock();
		l.lock();

		try {
			return packagesByName.get(language.getId() + MODULE_LANG_SEP + moduleName);
		}
		finally {
			l.unlock();
		}
	}


	@Override
	@Nullable
	public IManagedPackage findPackage(ILanguage language, IConstructor moduleId) {
		Lock l = lock.readLock();
		l.lock();

		try {
			return packagesByName.get(language.getId() + MODULE_LANG_SEP + language.getNameString(moduleId));
		}
		finally {
			l.unlock();
		}
	}


	@Override
	public IManagedPackage findPackage(URI uri) {
		Lock l = lock.readLock();
		l.lock();

		try {
			IManagedResource resource = resources.get(uri);
			if(resource instanceof IManagedPackage)
				return (IManagedPackage) resource;
			else
				return null;
		}
		finally {
			l.unlock();
		}
	}


	@Override
	public boolean hasURI(URI uri) {
		return manager.hasURI(uri);
	}


	@Override
	public URI getURI(String path) throws URISyntaxException {
		Lock l = lock.readLock();
		l.lock();

		try {
			IPath p = new Path(path);
			p = p.makeAbsolute();
			return new URI("project", project.getName(), p.toString(), null);
		}
		finally {
			l.unlock();
		}
	}


	@Override
	public void refresh() {
		Lock l = lock.writeLock();
		l.lock();

		try {
			List<URI> uris = new ArrayList<URI>(resources.keySet());
			for(URI p : uris)
				removeResource(p);
			dispose();

			addAllResources();
			for(ICompiler c : compilers.values()) {
				c.refresh();
			}
			dataInvariant();
		}
		finally {
			l.unlock();
		}
	}


	@Override
	public Collection<IManagedPackage> allPackages(final ILanguage language) {
		Lock l = lock.readLock();
		l.lock();

		try {
			List<IManagedPackage> list = new ArrayList<IManagedPackage>();
			for(Entry<URI, IManagedResource> entry : resources.entrySet()) {
				if(entry instanceof IManagedPackage && ((IManagedPackage) entry.getValue()).getLanguage().equals(language))
					list.add((IManagedPackage) entry.getValue());
			}
			return list;
		}
		finally {
			l.unlock();
		}
	}


	@Override
	public Collection<IManagedResource> allFiles() {
		Lock l = lock.readLock();
		l.lock();

		try {
			return Collections.unmodifiableCollection(resources.values());
		}
		finally {
			l.unlock();
		}
	}


	@Override
	public void addMarker(String message, ISourceLocation loc) {
		addMarker(message, loc, ErrorMarkers.COMPILATION_ERROR, ErrorMarkers.SEVERITY_ERROR_NUMBER);
	}


	@Override
	public void addMarker(String message, ISourceLocation loc, int severity) {
		addMarker(message, loc, ErrorMarkers.COMPILATION_ERROR, severity);
	}


	@Override
	public void addMarker(String message, ISourceLocation loc, String markerType) {
		addMarker(message, loc, markerType, ErrorMarkers.SEVERITY_ERROR_NUMBER);
	}


	@Override
	public void addMarker(String message, ISourceLocation loc, String markerType, int severity) {
		Lock l = lock.readLock(); // maybe a write lock? or add lock to
									// markerListener
		l.lock();

		try {
			if(loc == null)
				throw new ImplementationError("Missing location on marker add: " + message);

			URI uri = loc.getURI();

			IManagedResource pkg = resources.get(uri);
			if(pkg instanceof IManagedPackage)
				((IManagedPackage) pkg).addMarker(message, loc, markerType, severity);
			else
				throw new ImplementationError(message + "\nat location " + loc + " (resource not found)");
		}
		finally {
			l.unlock();
		}
	}


	@Override
	public IWorkspaceManager getResourceManager() {
		return manager;
	}


	@Override
	public IPath getSrcFolder() {
		if(srcPath == null) {
			IResource src = project.findMember("src");
			if(src != null && src.getType() == IResource.FOLDER)
				return src.getFullPath();
			else
				return basePath;
		}
		else
			return srcPath;
	}


	@Override
	public IPath getOutFolder() {
		return outPath;
	}


	@Override
	public IManagedResource getParent() {
		return null;
	}


	@Override
	public URI getURI() {
		try {
			return new URI("project://" + project.getName());
		}
		catch(URISyntaxException e) {
			throw new ImplementationError("URI syntax", e);
		}
	}


	@Override
	public boolean isFile() {
		return false;
	}


	@Override
	public boolean isContainer() {
		return true;
	}


	@Override
	public long getModificationStamp() {
		return project.getModificationStamp();
	}


	@Override
	public void onResourceChanged() {
	}


	@Override
	public Type getType() {
		return IManagedResource.ResourceType;
	}


	@Override
	public <T> T accept(IValueVisitor<T> v) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean isEqual(IValue other) {
		return this == other;
	}


	@Override
	public Collection<IManagedResource> getChildren(IRascalMonitor rm) {
		return Collections.unmodifiableCollection(resources.values());
	}


	@Override
	public boolean isCodeUnit() {
		return false;
	}


	@Override
	public boolean isProject() {
		return true;
	}

}
