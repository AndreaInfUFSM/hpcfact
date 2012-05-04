package br.ufsm.inf.viewCriticalSection;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import br.ufsm.inf.viewCriticalSection.views.CriticalSectionModel;

/**
 * The main plugin class to be used in the desktop.
 */
public class ViewCriticalSectionPlugin extends AbstractUIPlugin { 

	//The shared instance.
	private static ViewCriticalSectionPlugin plugin;
	private static CriticalSectionModel model;
	
	/**
	 * The constructor.
	 */
	public ViewCriticalSectionPlugin() {
		plugin = this;
		model = new CriticalSectionModel();
		
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static ViewCriticalSectionPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 */
	public static ImageDescriptor getImageDescriptor(String path) { 
		return AbstractUIPlugin.imageDescriptorFromPlugin("br.ufsm.inf.viewCriticalSection", path);
	}

	public CriticalSectionModel getModel() {
		return model;
	}
}
