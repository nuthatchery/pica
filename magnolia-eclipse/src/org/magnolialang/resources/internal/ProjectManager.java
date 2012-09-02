package org.magnolialang.resources.internal;

import java.io.PrintWriter;
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
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
import org.magnolialang.tasks.Transaction;

public final class ProjectManager implements IResourceManager {
	ReadWriteLock									lock				= new ReentrantReadWriteLock();
	private final IWorkspaceManager					manager;
	private Transaction								tr;
	private final Map<IPath, IManagedResource>		resources			= new HashMap<IPath, IManagedResource>();
	private final Map<String, IManagedPackage>		packagesByName		= new HashMap<String, IManagedPackage>();
	private final Map<IPath, String>				packageNamesByPath	= new HashMap<IPath, String>();
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
		tr = initializeTransaction();
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
	public Transaction getTransaction() {
		Lock l = lock.readLock();
		l.lock();
		try {
			return tr;
		}
		finally {
			l.unlock();
		}
	}


	private Transaction initializeTransaction() {
		PrintWriter stderr = new PrintWriter(System.err); // new
// PrintWriter(RuntimePlugin.getInstance().getConsoleStream());
		Transaction tr = new Transaction(manager.getTransaction(), stderr, false);
		return tr;
	}


	@Override
	public IManagedResource find(IPath path) {
		Lock l = lock.readLock();
		l.lock();

		try {
			if(!path.isAbsolute())
				path = basePath.append(path);
			System.err.println("find: absolute path: " + path);
			IManagedResource res = resources.get(path);

			System.err.println("find: resource: " + res);
			return res;
		}
		finally {
			l.unlock();
		}
	}


	@Override
	public IManagedResource find(IProject project, IPath path) {
		Lock l = lock.readLock();
		l.lock();

		try {
			if(project.equals(this.project))
				return find(path);
			else
				return null;
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
			IPath path = resource.getFullPath();
			if(resources.get(path) != null)
				removeResource(path);

			ILanguage language = LanguageRegistry.getLanguageForFile(path);
			if(language != null)
				addPackageResource(path, resource, language);
			else
				addFileResource(path, resource);

			for(IManagedResourceListener l : listeners)
				l.resourceAdded(path);
		}
	}


	private void addFileResource(IPath path, IResource resource) {
		if(debug)
			System.err.println("PROJECT NEW FILE: " + path);
		ManagedFile file = new ManagedFile(this, resource);
		resources.put(resource.getFullPath(), file);
	}


	private void addPackageResource(IPath path, IResource resource, ILanguage lang) {
		if(debug)
			System.err.println("PROJECT NEW MODULE: " + path);

		if(lang != null) {
			IPath srcRelativePath = resource.getFullPath();
			srcRelativePath = srcRelativePath.makeRelativeTo(getSrcFolder());
			String modName = lang.getModuleName(srcRelativePath);
			IConstructor modId = lang.getNameAST(modName);
			MagnoliaPackage pkg = new MagnoliaPackage(this, resource, modId, lang);
			resources.put(resource.getFullPath(), pkg);
			packagesByName.put(lang.getId() + MODULE_LANG_SEP + modName, pkg);
			packageNamesByPath.put(path, lang.getId() + MODULE_LANG_SEP + modName);
		}
	}


	/**
	 * Called by the WorkspaceManager whenever a resource is removed from the
	 * workspace
	 * 
	 * @param path
	 *            A full, workspace-relative path
	 */
	public void onResourceRemoved(IPath path) {
		Lock l = lock.writeLock();
		l.lock();

		try {
			removeResource(path);
			dataInvariant();
		}
		finally {
			l.unlock();
		}
	}


	private void removeResource(IPath path) {
		if(debug)
			System.err.println("PROJECT REMOVED: " + path);
		IManagedResource removed = resources.remove(path);
		if(removed != null) {
			for(IManagedResourceListener l : listeners)
				l.resourceRemoved(path);
		}

		String modName = packageNamesByPath.remove(path);
		if(modName != null) {
			removed = packagesByName.remove(modName);
		}
	}


	/**
	 * Called by the WorkspaceManager whenever a resource has been changed
	 * (i.e., the file contents have changed)
	 * 
	 * @param path
	 *            A full, workspace-relative path
	 */
	public void onResourceChanged(IPath path) {
		if(debug)
			System.err.println("PROJECT CHANGED: " + path);
		IManagedResource resource = resources.get(path);
		if(resource != null) {
			resource.onResourceChanged();
			for(IManagedResourceListener l : listeners)
				l.resourceChanged(path);
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
				if(!packageNamesByPath.isEmpty())
					throw new ImplementationError("Leftover module-name mappings in project on shutdown: " + project);
				if(!packagesByName.isEmpty())
					throw new ImplementationError("Leftover modules in project on shutdown: " + project);
			}
			finally {
				resources.clear();
				packageNamesByPath.clear();
				packagesByName.clear();
			}
			tr.abandon();
			dataInvariant();
		}
		finally {
			l.unlock();
		}
	}


	private void dataInvariant() {
		for(IPath p : resources.keySet()) {
			if(resources.get(p) == null || !resources.get(p).getFullPath().equals(p) || !resources.get(p).getProject().equals(project))
				throw new ImplementationError("Data invariant check failed: " + " inconsistent resource map entry for  " + p);
		}
		for(IPath p : packageNamesByPath.keySet()) {
			if(!resources.containsKey(p) || !packagesByName.containsKey(packageNamesByPath.get(p)) || !packagesByName.get(packageNamesByPath.get(p)).getFullPath().equals(p)
					|| !packagesByName.get(packageNamesByPath.get(p)).getProject().equals(project))
				throw new ImplementationError("Data invariant check failed: " + " inconsistent module resource maps entry for  " + p);

		}
		if(packageNamesByPath.size() != packagesByName.size())
			throw new ImplementationError("Data invariant check failed: " + " modulesNamesByPath should be same size as packagesByName");
		if(packageNamesByPath.size() > resources.size())
			throw new ImplementationError("Data invariant check failed: " + " more modules than resources");

	}


	@Override
	public ICompiler getCompiler(ILanguage language) {
		Lock l = lock.readLock(); // TODO: ?
		l.lock();

		try {
			ICompiler compiler = compilers.get(language);
			if(compiler == null)
				compiler = language.getCompiler(this);
			return compiler;
		}
		finally {
			l.unlock();
		}
	}


	@Override
	@Nullable
	public ICompiler getCompiler(IPath sourceFile) {
		Lock l = lock.readLock(); // TODO: ?
		l.lock();

		try {
			ILanguage language = LanguageRegistry.getLanguageForFile(sourceFile);
			ICompiler compiler = compilers.get(language);
			if(compiler == null)
				compiler = language.getCompiler(this);
			return compiler;
		}
		finally {
			l.unlock();
		}
	}


	@Override
	@Nullable
	public IManagedResource findPackage(ILanguage language, String moduleName) {
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
	public IManagedResource findPackage(ILanguage language, IConstructor moduleId) {
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
	public IManagedPackage findPackage(IPath path) {
		return null;
	}


	@Override
	@Nullable
	public IConstructor getPackageId(IPath path) {
		Lock l = lock.readLock();
		l.lock();
		try {
			IManagedResource resource = find(path);
			if(resource instanceof IManagedPackage)
				return ((IManagedPackage) resource).getId();
			else
				return null;
		}
		finally {
			l.unlock();
		}
	}


	@Override
	@Nullable
	public String getPackageName(IPath path) {
		Lock l = lock.readLock();
		l.lock();
		try {
			IManagedResource resource = find(path);
			if(resource instanceof IManagedPackage) {
				IConstructor id = ((IManagedPackage) resource).getId();
				return ((IManagedPackage) resource).getLanguage().getNameString(id);
			}
			else
				return null;
		}
		finally {
			l.unlock();
		}
	}


	@Override
	public IPath getPath(URI uri) {
		Lock l = lock.readLock();
		l.lock();

		try {

			if(uri.getScheme().equals("project")) {
				String projName = uri.getHost();
				if(projName.equals(project.getName())) {
					return project.getFullPath().append(uri.getPath());
				}
			}
			return manager.getPath(uri);
		}
		finally {
			l.unlock();
		}
	}


	@Override
	public boolean hasPath(URI uri) {
		return manager.hasPath(uri);
	}


	@Override
	public IPath getPath(String path) {
		Lock l = lock.readLock();
		l.lock();

		try {
			IPath p = new Path(path);
			if(p.isAbsolute())
				return p;
			else
				return project.getFullPath().append(p);
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
			List<IPath> paths = new ArrayList<IPath>(resources.keySet());
			for(IPath p : paths)
				removeResource(p);
			dispose();
			if(tr != null)
				tr.abandon();
			tr = initializeTransaction();
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
	public Collection<IPath> allPackages(final ILanguage language) {
		Lock l = lock.readLock();
		l.lock();

		try {
			List<IPath> list = new ArrayList<IPath>();
			for(Entry<IPath, IManagedResource> entry : resources.entrySet()) {
				if(entry instanceof IManagedPackage && ((IManagedPackage) entry.getValue()).getLanguage().equals(language))
					list.add(entry.getKey());
			}
			return list;
		}
		finally {
			l.unlock();
		}
	}


	@Override
	public Collection<IPath> allFiles() {
		Lock l = lock.readLock();
		l.lock();

		try {
			return Collections.unmodifiableSet(resources.keySet());
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
			IPath path = null;
			try {
				path = getPath(uri);
			}
			catch(IllegalArgumentException e) {
				throw new ImplementationError(message + "\nat location " + loc + " (outside workspace)", e);
			}
			IManagedResource pkg = resources.get(path);
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
	public Kind getResourceKind() {
		return IManagedResource.Kind.PROJECT;
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
	public boolean isFolder() {
		return false;
	}


	@Override
	public IPath getPath() {
		return new Path("");
	}


	@Override
	public IPath getFullPath() {
		return new Path("/");
	}


	@Override
	public IProject getProject() {
		return project;
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

}
