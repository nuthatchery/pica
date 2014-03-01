/**************************************************************************
 * Copyright (c) 2013 Anya Helene Bagge
 * Copyright (c) 2013 University of Bergen
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. See http://www.gnu.org/licenses/
 * 
 * 
 * See the file COPYRIGHT for more information.
 * 
 * Contributors:
 * * Anya Helene Bagge
 * 
 *************************************************************************/
package org.nuthatchery.pica.eclipse;

import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.nuthatchery.pica.errors.Severity;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class PicaActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "pica"; //$NON-NLS-1$

	// The shared instance
	private static PicaActivator plugin;


	/**
	 * The constructor
	 */
	public PicaActivator() {
	}


	public void logMsg(@Nullable String msg, @Nullable Severity sev, @Nullable Throwable t) {
		if(msg == null) {
			if(t == null || t.getMessage() == null) {
				msg = "Unknown message";
			}
			else {
				msg = t.getMessage();
			}
		}

		int status = Status.ERROR;
		switch(sev) {
		case DEFAULT:
			status = Status.ERROR;
			break;
		case ERROR:
			status = Status.ERROR;
			break;
		case INFO:
			status = Status.INFO;
			break;
		case NOTHING:
			status = Status.OK;
			break;
		case WARNING:
			status = Status.WARNING;
			break;
		}

		getLog().log(new Status(status, PLUGIN_ID, 0, msg, t));
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(@Nullable BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(@Nullable BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}


	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static PicaActivator getDefault() {
		return plugin;
	}

}
