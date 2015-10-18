package org.nuthatchery.pica.resources.handles;

import java.nio.file.OpenOption;

public enum PicaOpenOption implements OpenOption {
	/**
	 * Indicate that a resource is derived from some other resource.
	 */
	DERIVED,
	/**
	 * Indicate that a file should be hidden, if the underlying filesystem
	 * supports this.
	 *
	 * Not all file systems support hiding, and not all kinds of resources may
	 * be hidden.
	 *
	 * Note that on Unix systems, hiding is based on the file name (any name
	 * starting with a dot is hidden).
	 */
	HIDDEN,
	/**
	 * When creating a resource, also create the parent and ancestors, if
	 * necessary.
	 */
	CREATE_PARENTS
}
