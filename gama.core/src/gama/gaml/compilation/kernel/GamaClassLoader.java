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
package gama.gaml.compilation.kernel;

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
	 * Adds the bundle.
	 *
	 * @param bundle
	 *            the bundle
	 * @return the class loader
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
			protected synchronized Class loadClass(final String name, final boolean resolve)
					throws ClassNotFoundException {
				return findClass(name);
			}

		});
	}

	@Override
	protected Class findClass(final String name) throws ClassNotFoundException {
		for (ClassLoader loader2 : bundleLoaders.values()) {
			try {
				return loader2.loadClass(name);
			} catch (final ClassNotFoundException cnfe) {}
		}
		throw new ClassNotFoundException(name + " not found in any registered loader");
	}

	@Override
	protected URL findResource(final String name) {
		for (ClassLoader loader2 : bundleLoaders.values()) {
			final URL url = loader2.getResource(name);
			if (url != null) return url;
		}
		return null;
	}

	@Override
	public URL getResource(final String name) {
		return findResource(name);
	}

	@Override
	protected synchronized Class loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
		return findClass(name);
	}

}
