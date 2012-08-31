package org.magnolialang.resources.internal;


public class OverlayModuleManager { // NOPMD by anya on 1/5/12 5:06 AM
}
/*
 * implements IResourceManager {
 * ReadWriteLock lock = new ReentrantReadWriteLock();
 * private Transaction tr;
 * private final Map<IPath, IManagedResource> resources = new HashMap<IPath,
 * IManagedResource>();
 * private final Map<String, IManagedResource> modulesByName = new
 * HashMap<String, IManagedResource>();
 * private final Map<IPath, String> moduleNamesByPath = new HashMap<IPath,
 * String>();
 * private final MarkerListener markerListener;
 * private final List<IManagedResourceListener> listeners = new
 * ArrayList<IManagedResourceListener>();
 * private final static String MODULE_LANG_SEP = "%";
 * private final boolean debug = false;
 * private final IResourceManager manager;
 * 
 * 
 * public OverlayModuleManager(IResourceManager manager,
 * Collection<IManagedResource> overlaidResources) {
 * 
 * this.manager = manager;
 * this.markerListener = new MarkerListener();
 * initializeTransaction();
 * dataInvariant();
 * }
 * 
 * 
 * @Override
 * public Transaction getTransaction() {
 * Lock l = lock.readLock();
 * l.lock();
 * try {
 * return tr;
 * }
 * finally {
 * l.unlock();
 * }
 * }
 * 
 * 
 * private void initializeTransaction() {
 * PrintWriter stderr = new
 * PrintWriter(RuntimePlugin.getInstance().getConsoleStream());
 * if(tr != null)
 * tr.abandon();
 * tr = new Transaction(getResourceManager().getTransaction(), stderr, false);
 * tr.registerListener(markerListener, MagnoliaFacts.Type_ErrorMark);
 * 
 * }
 * 
 * 
 * @Override
 * public IManagedResource find(IPath path) {
 * Lock l = lock.readLock();
 * l.lock();
 * 
 * try {
 * IManagedResource res = resources.get(path);
 * if(res != null)
 * return res;
 * else
 * return manager.find(path);
 * }
 * finally {
 * l.unlock();
 * }
 * }
 * 
 * 
 * @Override
 * public IManagedResource find(IProject project, IPath path) {
 * return manager.find(project, path);
 * }
 * 
 * 
 * private void addResource(IManagedResource resource) {
 * if(resource instanceof IManagedFile) {
 * IPath path = resource.getFullPath();
 * if(resources.get(path) != null)
 * removeResource(path);
 * 
 * addFileResource(path, (IManagedFile) resource);
 * addModuleResource(path, (IManagedFile) resource);
 * 
 * for(IManagedResourceListener l : listeners)
 * l.resourceAdded(path);
 * }
 * }
 * 
 * 
 * private void addFileResource(IPath path, IManagedFile resource) {
 * if(debug)
 * System.err.println("PROJECT NEW FILE: " + path);
 * FileLinkFact fact = new FileLinkFact(resource, Type_FileResource,
 * vf.string(makeProjectRelativePath(path).toString()));
 * resources.put(resource.getFullPath(), fact);
 * try {
 * tr.setFact(Type_FileResource, vf.string(path.toString()), fact);
 * }
 * catch(NullPointerException e) {
 * e.printStackTrace();
 * }
 * }
 * 
 * 
 * private void addModuleResource(IPath path, IManagedFile resource) {
 * if(debug)
 * System.err.println("PROJECT NEW MODULE: " + path);
 * ILanguage lang = resource.getLanguage();
 * if(lang != null) {
 * String modName = lang.getModuleName(resource.getPath());
 * IConstructor modNameAST = lang.getNameAST(modName);
 * FileLinkFact fact = new FileLinkFact(resource, Type_ModuleResource,
 * modNameAST);
 * resources.put(resource.getFullPath(), fact);
 * modulesByName.put(lang.getId() + MODULE_LANG_SEP + modName, fact);
 * moduleNamesByPath.put(path, lang.getId() + MODULE_LANG_SEP + modName);
 * tr.setFact(Type_ModuleResource, modNameAST, fact);
 * }
 * }
 * 
 * 
 * private void removeResource(IPath path) {
 * if(debug)
 * System.err.println("PROJECT REMOVED: " + path);
 * FileLinkFact removed = resources.remove(path);
 * if(removed != null) {
 * tr.removeFact(removed);
 * 
 * for(IManagedResourceListener l : listeners)
 * l.resourceRemoved(path);
 * }
 * 
 * String modName = moduleNamesByPath.remove(path);
 * if(modName != null) {
 * removed = modulesByName.remove(modName);
 * if(removed != null)
 * tr.removeFact(removed);
 * }
 * }
 * 
 * 
 * @Override
 * public void addListener(IManagedResourceListener listener) {
 * Lock l = lock.writeLock();
 * l.lock();
 * 
 * try {
 * listeners.add(listener);
 * manager.addListener(listener);
 * }
 * finally {
 * l.unlock();
 * }
 * }
 * 
 * 
 * @Override
 * public void removeListener(IManagedResourceListener listener) {
 * Lock l = lock.writeLock();
 * l.lock();
 * 
 * try {
 * listeners.remove(listener);
 * manager.removeListener(listener);
 * }
 * finally {
 * l.unlock();
 * }
 * }
 * 
 * 
 * @Override
 * public void dispose() {
 * Lock l = lock.writeLock();
 * l.lock();
 * 
 * try {
 * // for(IPath path : resources.keySet()) {
 * // resourceRemoved(path);
 * // }
 * try {
 * if(!resources.isEmpty())
 * throw new ImplementationError("Leftover files in project on shutdown: " +
 * project);
 * if(!moduleNamesByPath.isEmpty())
 * throw new
 * ImplementationError("Leftover module-name mappings in project on shutdown: "
 * + project);
 * if(!modulesByName.isEmpty())
 * throw new ImplementationError("Leftover modules in project on shutdown: " +
 * project);
 * }
 * finally {
 * resources.clear();
 * moduleNamesByPath.clear();
 * modulesByName.clear();
 * }
 * tr.abandon();
 * dataInvariant();
 * }
 * finally {
 * l.unlock();
 * }
 * }
 * 
 * 
 * private void dataInvariant() {
 * for(IPath p : resources.keySet()) {
 * if(resources.get(p) == null || !resources.get(p).getFullPath().equals(p))
 * throw new ImplementationError("Data invariant check failed: " +
 * " inconsistent resource map entry for  " + p);
 * }
 * for(IPath p : moduleNamesByPath.keySet()) {
 * if(!resources.containsKey(p) ||
 * !modulesByName.containsKey(moduleNamesByPath.get(p)) ||
 * !modulesByName.get(moduleNamesByPath.get(p)).getFullPath().equals(p))
 * throw new ImplementationError("Data invariant check failed: " +
 * " inconsistent module resource maps entry for  " + p);
 * 
 * }
 * if(moduleNamesByPath.size() != modulesByName.size())
 * throw new ImplementationError("Data invariant check failed: " +
 * " modulesNamesByPath should be same size as modulesByName");
 * if(moduleNamesByPath.size() > resources.size())
 * throw new ImplementationError("Data invariant check failed: " +
 * " more modules than resources");
 * 
 * }
 * 
 * 
 * @Override
 * public ICompiler getCompiler(ILanguage language) {
 * return manager.getCompiler(language);
 * }
 * 
 * 
 * @Override
 * public ICompiler getCompiler(IPath sourceFile) {
 * return manager.getCompiler(sourceFile);
 * }
 * 
 * 
 * @Override
 * public IManagedResource findModule(ILanguage language, String moduleName) {
 * Lock l = lock.readLock();
 * l.lock();
 * 
 * try {
 * IManagedResource resource = modulesByName.get(language.getId() +
 * MODULE_LANG_SEP + moduleName);
 * if(resource == null)
 * return manager.findModule(language, moduleName);
 * else
 * return resource;
 * }
 * finally {
 * l.unlock();
 * }
 * }
 * 
 * 
 * @Override
 * public IManagedResource findModule(IValue moduleName) {
 * Lock l = lock.readLock();
 * l.lock();
 * Transaction localTr = tr;
 * l.unlock(); // avoid holding the lock through getFact()
 * 
 * IConstructor res = (IConstructor) localTr.getFact(new NullRascalMonitor(),
 * Type_ModuleResource, moduleName);
 * l.lock();
 * try {
 * if(res != null) {
 * IManagedResource resource = resources.get(new Path(((IString)
 * res.get("val")).getValue()));
 * if(resource == null)
 * return manager.findModule(moduleName);
 * else
 * return resource;
 * }
 * else
 * return manager.findModule(moduleName);
 * }
 * finally {
 * l.unlock();
 * }
 * }
 * 
 * 
 * @Override
 * public IConstructor getModuleId(IPath path) {
 * // no lock needed
 * IManagedResource resource = find(path);
 * if(resource != null) {
 * String modName = resource.getLanguage().getModuleName(resource.getPath());
 * return resource.getLanguage().getNameAST(modName);
 * }
 * else
 * return null;
 * }
 * 
 * 
 * @Override
 * public String getModuleName(IPath path) {
 * // no lock needed
 * IManagedResource resource = find(path);
 * if(resource != null)
 * return resource.getLanguage().getModuleName(resource.getPath());
 * else
 * return null;
 * }
 * 
 * 
 * @Override
 * public IPath getPath(URI uri) {
 * Lock l = lock.readLock();
 * l.lock();
 * 
 * try {
 * 
 * if(uri.getScheme().equals("project")) {
 * String projName = uri.getHost();
 * if(projName.equals(project.getName())) {
 * IPath p = project.getFullPath().append(uri.getPath());
 * return p;
 * }
 * }
 * return getResourceManager().getPath(uri);
 * }
 * finally {
 * l.unlock();
 * }
 * }
 * 
 * 
 * @Override
 * public IPath getPath(String path) {
 * Lock l = lock.readLock();
 * l.lock();
 * 
 * try {
 * IPath p = new Path(path);
 * if(p.isAbsolute())
 * return p;
 * else
 * return project.getFullPath().append(p);
 * }
 * finally {
 * l.unlock();
 * }
 * }
 * 
 * 
 * @Override
 * public void refresh() {
 * Lock l = lock.writeLock();
 * l.lock();
 * 
 * try {
 * List<IPath> paths = new ArrayList<IPath>(resources.keySet());
 * for(IPath p : paths)
 * removeResource(p);
 * dispose();
 * initializeTransaction();
 * for(IPath p : paths) {
 * addResource(p);
 * }
 * for(ICompiler c : compilers.values()) {
 * c.refresh();
 * }
 * markerListener.refresh();
 * dataInvariant();
 * }
 * finally {
 * l.unlock();
 * }
 * }
 * 
 * 
 * @Override
 * public Collection<IPath> allModules(final ILanguage language) {
 * Lock l = lock.readLock();
 * l.lock();
 * 
 * try {
 * List<IPath> list = new ArrayList<IPath>();
 * for(Entry<IPath, FileLinkFact> entry : resources.entrySet()) {
 * if(entry.getValue().getLanguage().equals(language))
 * list.add(entry.getKey());
 * }
 * return list;
 * }
 * finally {
 * l.unlock();
 * }
 * }
 * 
 * 
 * @Override
 * public Collection<IPath> allFiles() {
 * Lock l = lock.readLock();
 * l.lock();
 * 
 * try {
 * return Collections.unmodifiableSet(resources.keySet());
 * }
 * finally {
 * l.unlock();
 * }
 * }
 * 
 * 
 * @Override
 * public void addMarker(String message, ISourceLocation loc) {
 * addMarker(message, loc, ErrorMarkers.COMPILATION_ERROR,
 * ErrorMarkers.SEVERITY_ERROR_NUMBER);
 * }
 * 
 * 
 * @Override
 * public void addMarker(String message, ISourceLocation loc, int severity) {
 * addMarker(message, loc, ErrorMarkers.COMPILATION_ERROR, severity);
 * }
 * 
 * 
 * @Override
 * public void addMarker(String message, ISourceLocation loc, String markerType)
 * {
 * addMarker(message, loc, markerType, ErrorMarkers.SEVERITY_ERROR_NUMBER);
 * }
 * 
 * 
 * @Override
 * public void addMarker(String message, ISourceLocation loc, String markerType,
 * int severity) {
 * Lock l = lock.readLock(); // maybe a write lock? or add lock to
 * // markerListener
 * l.lock();
 * 
 * try {
 * if(loc != null) {
 * URI uri = loc.getURI();
 * IPath path = getPath(uri);
 * FileLinkFact fact = resources.get(path);
 * markerListener.addMarker(message, loc, markerType, severity, fact);
 * }
 * else
 * throw new ImplementationError("Missing location on marker add: " + message);
 * }
 * finally {
 * l.unlock();
 * }
 * }
 * 
 * 
 * @Override
 * public IWorkspaceManager getResourceManager() {
 * return manager.getResourceManager();
 * }
 * 
 * }
 */
