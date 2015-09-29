package org.nuthatchery.pica.eclipse;

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.annotation.Nullable;

public class EclipseResourceMapper {

	/**
	 * @param uri
	 *            The URI of the desired file
	 * @return An IFile representing the URI
	 */
	@Nullable
	public IFile getFileHandle(URI uri) {
		IPath path = null;
		throw new UnsupportedOperationException();

//		try {
//			path = new Path(new File(Pica.getResolverRegistry().getResourceURI(uri)).getAbsolutePath());
//		}
//		catch(UnsupportedSchemeException e) {
//			Pica.get().logException(e.getMessage(), e);
//			e.printStackTrace();
//			return null;
//		}
//		catch(IOException e) {
//			Pica.get().logException(e.getMessage(), e);
//			e.printStackTrace();
//			return null;
//		}
//		return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
	}

}
