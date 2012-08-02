package org.magnolialang.resources.internal;

import static org.magnolialang.terms.TermFactory.vf;

import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.magnolialang.compiler.ICompiler;
import org.magnolialang.errors.ErrorMarkers;
import org.magnolialang.errors.ImplementationError;
import org.magnolialang.magnolia.MagnoliaFacts;
import org.magnolialang.magnolia.MarkerListener;
import org.magnolialang.nullness.Nullable;
import org.magnolialang.resources.ILanguage;
import org.magnolialang.resources.IManagedFile;
import org.magnolialang.resources.IManagedResource;
import org.magnolialang.resources.IManagedResourceListener;
import org.magnolialang.resources.IModuleManager;
import org.magnolialang.resources.IResourceManager;
import org.magnolialang.resources.LanguageRegistry;
import org.magnolialang.tasks.Transaction;

public final class ProjectManager implements IModuleManager, IManagedResourceListener {
	ReadWriteLock									lock				= new ReentrantReadWriteLock();
	private final IResourceManager					manager;
	private Transaction								tr;
	private final Map<IPath, FileLinkFact>			resources			= new HashMap<IPath, FileLinkFact>();
	private final Map<String, FileLinkFact>			modulesByName		= new HashMap<String, FileLinkFact>();
	private final Map<IPath, String>				moduleNamesByPath	= new HashMap<IPath, String>();
	private final Map<ILanguage, ICompiler>			compilers			= new HashMap<ILanguage, ICompiler>();
	private final IProject							project;
	private final IPath								basePath;
	private final MarkerListener					markerListener;
	private final List<IManagedResourceListener>	listeners			= new ArrayList<IManagedResourceListener>();
	private final static String						MODULE_LANG_SEP		= "%";
	private static final String						OUT_FOLDER			= "cxx";
	private static final boolean					debug				= false;
	private final IPath								srcPath;
	private final IPath								outPath;


	public ProjectManager(IResourceManager manager, IProject project, Set<IPath> contents) {

		this.manager = manager;
		this.project = project;
		this.basePath = project.getFullPath();
		srcPath = null;
		outPath = project.getFolder(OUT_FOLDER).getFullPath();
		this.markerListener = new MarkerListener();
		manager.addListener(this);
		tr = initializeTransaction();
		System.err.println("New projectmanager: basepath=" + basePath);
		for(IPath p : contents) {
			System.err.println("New projectmanager: adding " + p);
			resourceAdded(p);
		}
		dataInvariant();
	}


	private IPath makeProjectRelativePath(IPath path) {
		return path.makeRelativeTo(basePath);
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
		tr.registerListener(markerListener, MagnoliaFacts.Type_ErrorMark);
		return tr;
	}


	@Override
	public IManagedResource find(IPath path) {
		Lock l = lock.readLock();
		l.lock();

		try {
			if(path.isAbsolute())
				path = makeProjectRelativePath(path);
			System.err.println("find: relative path: " + path);
			IManagedResource res = resources.get(path);

			if(res == null) {
				IResource member = project.findMember(path);
				if(member == null)
					return null;
				else
					res = resources.get(member.getFullPath());
			}
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


	@Override
	public void resourceAdded(IPath path) {
		Lock l = lock.writeLock();
		l.lock();

		try {
			addResource(path);
			dataInvariant();
		}
		finally {
			l.unlock();
		}
	}


	private void addResource(IPath path) {
		IManagedResource resource = manager.find(path);
		if(resource instanceof IManagedFile && project.equals(resource.getProject())) {

			if(resources.get(path) != null)
				resourceRemoved(path);

			addFileResource(path, (IManagedFile) resource);
			addModuleResource(path, (IManagedFile) resource);

			for(IManagedResourceListener l : listeners)
				l.resourceAdded(path);
		}
	}


	private void addFileResource(IPath path, IManagedFile resource) {
		if(debug)
			System.err.println("PROJECT NEW FILE: " + path);
		FileLinkFact fact = new FileLinkFact(resource, Type_FileResource, vf.string(makeProjectRelativePath(path).toString()));
		resources.put(resource.getFullPath(), fact);
		tr.setFact(Type_FileResource, vf.string(makeProjectRelativePath(path).toString()), fact);
	}


	private void addModuleResource(IPath path, IManagedFile resource) {
		if(debug)
			System.err.println("PROJECT NEW MODULE: " + path);
		ILanguage lang = resource.getLanguage();
		if(lang != null) {
			IPath srcRelativePath = resource.getFullPath();
			srcRelativePath = srcRelativePath.makeRelativeTo(getSrcFolder());
			String modName = lang.getModuleName(srcRelativePath);
			IConstructor modNameAST = lang.getNameAST(modName);
			FileLinkFact fact = new FileLinkFact(resource, Type_ModuleResource, modNameAST);
			resources.put(resource.getFullPath(), fact);
			modulesByName.put(lang.getId() + MODULE_LANG_SEP + modName, fact);
			moduleNamesByPath.put(path, lang.getId() + MODULE_LANG_SEP + modName);
			tr.setFact(Type_ModuleResource, modNameAST, fact);
		}
	}


	@Override
	public void resourceRemoved(IPath path) {
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
		FileLinkFact removed = resources.remove(path);
		if(removed != null) {
			tr.removeFact(removed);

			for(IManagedResourceListener l : listeners)
				l.resourceRemoved(path);
		}

		String modName = moduleNamesByPath.remove(path);
		if(modName != null) {
			removed = modulesByName.remove(modName);
			if(removed != null)
				tr.removeFact(removed);
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
				if(!moduleNamesByPath.isEmpty())
					throw new ImplementationError("Leftover module-name mappings in project on shutdown: " + project);
				if(!modulesByName.isEmpty())
					throw new ImplementationError("Leftover modules in project on shutdown: " + project);
			}
			finally {
				resources.clear();
				moduleNamesByPath.clear();
				modulesByName.clear();
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
		for(IPath p : moduleNamesByPath.keySet()) {
			if(!resources.containsKey(p) || !modulesByName.containsKey(moduleNamesByPath.get(p)) || !modulesByName.get(moduleNamesByPath.get(p)).getFullPath().equals(p)
					|| !modulesByName.get(moduleNamesByPath.get(p)).getProject().equals(project))
				throw new ImplementationError("Data invariant check failed: " + " inconsistent module resource maps entry for  " + p);

		}
		if(moduleNamesByPath.size() != modulesByName.size())
			throw new ImplementationError("Data invariant check failed: " + " modulesNamesByPath should be same size as modulesByName");
		if(moduleNamesByPath.size() > resources.size())
			throw new ImplementationError("Data invariant check failed: " + " more modules than resources");

	}


	@Override
	public ICompiler getCompiler(ILanguage language) {
		Lock l = lock.readLock(); // TODO: ?
		l.lock();

		try {
			ICompiler compiler = compilers.get(language);
			if(compiler == null)
				compiler = language.makeCompiler(this);
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
				compiler = language.makeCompiler(this);
			return compiler;
		}
		finally {
			l.unlock();
		}
	}


	@Override
	@Nullable
	public IManagedResource findModule(ILanguage language, String moduleName) {
		Lock l = lock.readLock();
		l.lock();

		try {
			return modulesByName.get(language.getId() + MODULE_LANG_SEP + moduleName);
		}
		finally {
			l.unlock();
		}
	}


	@Override
	@Nullable
	public IManagedResource findModule(ILanguage language, IConstructor moduleId) {
		Lock l = lock.readLock();
		l.lock();

		try {
			return modulesByName.get(language.getId() + MODULE_LANG_SEP + language.getNameString(moduleId));
		}
		finally {
			l.unlock();
		}
	}


	/*
	@Override
	@Nullable
	public IManagedResource findModule(IValue moduleId) {
		Lock l = lock.readLock();
		l.lock();
		Transaction localTr = tr;
		l.unlock(); // avoid holding the lock through getFact()

		IValue fact = localTr.getFact(new NullRascalMonitor(), Type_ModuleResource, moduleId);
		System.err.println("findModule: " + fact);
		IConstructor res = (IConstructor) fact;
		l.lock();
		try {
			if(res == null)
				return null;
			else
				return resources.get(new Path(((IString) res.get("val")).getValue()));
		}
		finally {
			l.unlock();
		}
	}
	*/

	@Override
	@Nullable
	public IConstructor getModuleId(IPath path) {
		Lock l = lock.readLock();
		l.lock();
		try {
			IManagedResource resource = find(path);
			if(resource == null) {
				return null;
			}
			else {
				path = resource.getFullPath().makeRelativeTo(getSrcFolder());
				System.err.println("getModuleId: src relative path:" + path);
				String modName = resource.getLanguage().getModuleName(path);
				return resource.getLanguage().getNameAST(modName);
			}
		}
		finally {
			l.unlock();
		}
	}


	@Override
	@Nullable
	public String getModuleName(IPath path) {
		Lock l = lock.readLock();
		l.lock();
		try {
			IManagedResource resource = find(path);
			if(resource == null) {
				return null;
			}
			else {
				path = resource.getFullPath().makeRelativeTo(getSrcFolder());
				return resource.getLanguage().getModuleName(path);
			}
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
			for(IPath p : paths) {
				addResource(p);
			}
			for(ICompiler c : compilers.values()) {
				c.refresh();
			}
			markerListener.refresh();
			dataInvariant();
		}
		finally {
			l.unlock();
		}
	}


	@Override
	public Collection<IPath> allModules(final ILanguage language) {
		Lock l = lock.readLock();
		l.lock();

		try {
			List<IPath> list = new ArrayList<IPath>();
			for(Entry<IPath, FileLinkFact> entry : resources.entrySet()) {
				if(entry.getValue().getLanguage().equals(language))
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
			FileLinkFact fact = resources.get(path);
			if(fact == null)
				throw new ImplementationError(message + "\nat location " + loc + " (resource not found)");
			markerListener.addMarker(message, loc, markerType, severity, fact);
		}
		finally {
			l.unlock();
		}
	}


	@Override
	public IResourceManager getResourceManager() {
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

}
