package org.magnolialang.pica.eclipse;

import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.magnolialang.errors.Severity;
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


	public void logMsg(String msg, Severity sev, Throwable t) {
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
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
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
