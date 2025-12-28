/*******************************************************************************************************
 *
 * Activator.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies;

import java.lang.reflect.Field;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;

import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.util.factory.GeoTools;
import org.geotools.util.factory.Hints;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import gama.dependencies.logging.GamaLoggingServiceProvider;
import gama.dev.BANNER_CATEGORY;
import gama.dev.DEBUG;
import it.geosolutions.jaiext.ConcurrentOperationRegistry;
import one.util.streamex.StreamEx;

/**
 * The Class Activator.
 */
public class Activator implements BundleActivator {

	/*
	 * (non-Javadoc)
	 *
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework. BundleContext)
	 */
	@Override
	public void start(final BundleContext bundleContext) throws Exception {

		hackLoggingSubSystem();

		// Forces early initialisation of operation registry of JAI. It fixes initialisation problems in some third
		// party equinox x@applications such as OpenMOLE.
		final JAI jaiDef = JAI.getDefaultInstance();
		if (!(jaiDef.getOperationRegistry() instanceof ConcurrentOperationRegistry)) {
			jaiDef.setOperationRegistry(ConcurrentOperationRegistry.initializeRegistry());
		}
		ImageIO.scanForPlugins();
		Hints.putSystemDefault(Hints.FILTER_FACTORY, CommonFactoryFinder.getFilterFactory(null));
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
			DEBUG.BANNER(BANNER_CATEGORY.JAI, "ImageIO extensions", "loaded for",
					StreamEx.of(ImageIO.getReaderFileSuffixes()).remove(String::isBlank).sorted().joining("|"));
		}
		// Installs the new RelateNG JTS library (supposedly more efficient that RelateOp).
		System.setProperty("jts.relate", "ng");
	}

	/**
	 * Hacks the SFL4J logging sub system. Directly sets the static fields of LoggerFactory to use Gama's logging
	 * service provider.
	 *
	 * @throws NoSuchFieldException
	 *             the no such field exception
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 */
	private void hackLoggingSubSystem() throws NoSuchFieldException, IllegalAccessException {
		Field is = LoggerFactory.class.getDeclaredField("INITIALIZATION_STATE");
		is.setAccessible(true);
		is.set(null, 3); // Mark as INITIALIZED to avoid further attempts to initialize SLF4J
		Field pr = LoggerFactory.class.getDeclaredField("PROVIDER");
		pr.setAccessible(true);
		pr.set(null, new GamaLoggingServiceProvider());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(final BundleContext bundleContext) throws Exception {
		// Activator.context = null;
	}

}
