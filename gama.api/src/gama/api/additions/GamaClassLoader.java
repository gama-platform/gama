/*******************************************************************************************************
 *
 * GamaClassLoader.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.additions;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;

/**
 * The class GamaClassLoader. A custom class loader that can build class loaders for the bundles containing additions to
 * GAML, and keeps a history of them in order to resolve the classes they refer.
 *
 * @author drogoul
 * @since 23 janv. 2012
 *
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaClassLoader extends ClassLoader {

	/** The loader. */
	private volatile static GamaClassLoader instance;

	/** The loaders. */
	private final Map<String, ClassLoader> bundleLoaders = new HashMap<>();

	/**
	 * Gets the single instance of GamaClassLoader.
	 *
	 * @return single instance of GamaClassLoader
	 */
	public static GamaClassLoader getInstance() {
		if (instance == null) { instance = new GamaClassLoader(); }
		return instance;
	}

	/**
	 * Instantiates a new gama class loader.
	 */
	private GamaClassLoader() {}

	/**
	 * Adds a bundle to the class loader registry. Creates a custom ClassLoader for the given OSGi bundle that
	 * delegates class loading, resource loading, and resource enumeration to the bundle. The loader is stored
	 * using the bundle's symbolic name as the key.
	 *
	 * @param bundle
	 *            the OSGi bundle to register with this class loader
	 */
	public void addBundle(final Bundle bundle) {
		bundleLoaders.put(bundle.getSymbolicName(), new ClassLoader(null) {
			@Override
			protected Class findClass(final String name) throws ClassNotFoundException {
				try {
					return bundle.loadClass(name);
				} catch (final Exception cnfe) {
					throw new ClassNotFoundException(name + " not found in [" + bundle.getSymbolicName() + "]", cnfe);
				}
			}

			@Override
			protected URL findResource(final String name) {
				return bundle.getResource(name);
			}

			@Override
			protected Enumeration findResources(final String name) throws IOException {
				return bundle.getResources(name);
			}

			@Override
			public URL getResource(final String name) {
				return findResource(name);
			}

			@Override
			protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
				return findClass(name);
			}

		});
	}

	/**
	 * Finds and loads the class with the specified binary name by iterating through all registered bundle loaders.
	 * This method is called by the ClassLoader when a class needs to be loaded. It attempts to load the class
	 * from each registered bundle loader in sequence until one succeeds.
	 *
	 * @param name
	 *            the binary name of the class to find
	 * @return the resulting Class object
	 * @throws ClassNotFoundException
	 *             if the class could not be found in any registered loader
	 */
	@Override
	protected Class findClass(final String name) throws ClassNotFoundException {
		for (ClassLoader loader2 : bundleLoaders.values()) {
			try {
				return loader2.loadClass(name);
			} catch (final ClassNotFoundException cnfe) {}
		}
		throw new ClassNotFoundException(name + " not found in any registered loader");
	}

	/**
	 * Finds the resource with the specified name by searching through all registered bundle loaders.
	 * This method iterates over each bundle loader and attempts to retrieve the resource.
	 * The first successfully found resource is returned.
	 *
	 * @param name
	 *            the name of the resource to find
	 * @return a URL for reading the resource, or null if the resource could not be found
	 */
	@Override
	protected URL findResource(final String name) {
		for (ClassLoader loader2 : bundleLoaders.values()) {
			final URL url = loader2.getResource(name);
			if (url != null) return url;
		}
		return null;
	}

	/**
	 * Gets the resource with the specified name by delegating to the findResource method.
	 * This is an override of the ClassLoader's getResource method to ensure proper resource lookup
	 * through all registered bundle loaders.
	 *
	 * @param name
	 *            the name of the resource
	 * @return a URL for reading the resource, or null if the resource could not be found
	 */
	@Override
	public URL getResource(final String name) {
		return findResource(name);
	}

	/**
	 * Loads the class with the specified binary name. This method delegates to findClass to load
	 * the class from the registered bundle loaders.
	 *
	 * @param name
	 *            the binary name of the class
	 * @param resolve
	 *            if true, then resolve the class (currently not used in this implementation)
	 * @return the resulting Class object
	 * @throws ClassNotFoundException
	 *             if the class was not found
	 */
	@Override
	protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
		return findClass(name);
	}

}
