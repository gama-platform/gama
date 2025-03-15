/*******************************************************************************************************
 *
 * Activator.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;

import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.util.factory.GeoTools;
import org.geotools.util.factory.Hints;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import gama.dev.STRINGS;
import it.geosolutions.jaiext.ConcurrentOperationRegistry;
import one.util.streamex.StreamEx;

/**
 * The Class Activator.
 */
public class Activator implements BundleActivator {

	/** The context. */
	private static BundleContext context;

	/**
	 * Gets the context.
	 *
	 * @return the context
	 */
	static BundleContext getContext() { return context; }

	/*
	 * (non-Javadoc)
	 *
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework. BundleContext)
	 */
	@Override
	public void start(final BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		// Forces early initialisation of operation registry of JAI. It fixes initialisation problems in some third
		// party equinox x@applications such as OpenMOLE.
		final JAI jaiDef = JAI.getDefaultInstance();
		if (!(jaiDef.getOperationRegistry() instanceof ConcurrentOperationRegistry)) {
			jaiDef.setOperationRegistry(ConcurrentOperationRegistry.initializeRegistry());
		}
		ImageIO.scanForPlugins();
		Hints.putSystemDefault(Hints.FILTER_FACTORY, CommonFactoryFinder.getFilterFactory2(null));
		Hints.putSystemDefault(Hints.STYLE_FACTORY, CommonFactoryFinder.getStyleFactory(null));
		Hints.putSystemDefault(Hints.FEATURE_FACTORY, CommonFactoryFinder.getFeatureFactory(null));
		Hints.putSystemDefault(Hints.USE_JAI_IMAGEREAD, true);
		final Hints defHints = GeoTools.getDefaultHints();
		// Initialize GridCoverageFactory so that we don't make a lookup every time a factory is needed
		Hints.putSystemDefault(Hints.GRID_COVERAGE_FACTORY, CoverageFactoryFinder.getGridCoverageFactory(defHints));

		//
		// See FLAGS.java
		String log = System.getProperty("enable_logging");
		if (log == null || "true".equals(log)) {
			BANNER("JAI", "ImageIO extensions", "loaded for",
					StreamEx.of(ImageIO.getReaderFileSuffixes()).remove(String::isBlank).sorted().joining("|"));
		}
		// Installs the new RelateNG JTS library (supposedly more efficient that RelateOp).
		System.setProperty("jts.relate", "ng");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(final BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

	/**
	 * Pad.
	 *
	 * @param string
	 *            the string
	 * @param minLength
	 *            the min length
	 * @param pad
	 *            the pad
	 * @return the string
	 */
	// See DEBUG.java
	public static String PAD(final String string, final int minLength, final char pad) {
		if (string.length() >= minLength) return string;
		final StringBuilder sb = new StringBuilder(minLength);
		sb.append(string);
		for (int i = string.length(); i < minLength; i++) { sb.append(pad); }
		return sb.toString();
	}

	/**
	 * Banner.
	 *
	 * @param title
	 *            the title
	 * @param state
	 *            the state
	 * @param result
	 *            the result
	 */
	public static void BANNER(final String category, final String title, final String state, final String result) {
		String cat = STRINGS.PAD("> " + category, 8, ' ') + ": ";
		System.out.println(STRINGS.PAD(cat + title + " ", 55, ' ') + STRINGS.PAD(" " + state, 15, '_') + " " + result);
	}

}
