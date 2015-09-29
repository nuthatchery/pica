package org.nuthatchery.pica.resources;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtil {
	public static String getFileExtension(Path path) {
		Path fileName = path.getFileName();
		if(fileName != null) {
			String name = fileName.toString();
			if(name.lastIndexOf('.') >= 0) {
				return name.substring(name.lastIndexOf('.') + 1, name.length());
			}
		}

		return null;
	}


	public static String getFileExtension(URI uri) {
		return getFileExtension(Paths.get(uri.getPath()));
	}
}
