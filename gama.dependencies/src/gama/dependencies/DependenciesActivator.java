/*******************************************************************************************************
 *
 * DependenciesActivator.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies;

import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;

import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.referencing.factory.PropertyAuthorityFactory;
import org.geotools.util.factory.GeoTools;
import org.geotools.util.factory.Hints;
import org.osgi.framework.BundleContext;

import gama.dev.BANNER_CATEGORY;
import gama.dev.DEBUG;
import it.geosolutions.jaiext.ConcurrentOperationRegistry;
import one.util.streamex.StreamEx;

/**
 * The Class DependenciesActivator. This activator initializes the JAI operation registry, scans for ImageIO plugins in
 * parallel with GeoTools hints setup, and configures various GeoTools factories and system properties.
 */
public class DependenciesActivator extends GamaBundleActivator {

	/**
	 * Logger used by GeoTools coverage readers (GeoTools names loggers after the package, not the class). Kept as a
	 * strong reference because java.util.logging only weakly references loggers, which would otherwise lose the filter.
	 */
	private static final Logger COVERAGE_IO_LOGGER = Logger.getLogger("org.geotools.coverage.grid.io");

	/**
	 * GeoTools tries to parse GDAL ".aux.xml" sidecars (PAM) with JAXB, which has no implementation available in GAMA.
	 * The failure is harmless (the sidecar is ignored) but GeoTools logs it with a full stack trace: drop these records
	 * without hiding the other messages of the package.
	 */
	private static void silenceGdalSidecarTraces() {
		COVERAGE_IO_LOGGER.setFilter(r -> {
			final String msg = r.getMessage();
			return msg == null || !msg.startsWith("GDAL") || !msg.contains("could not be parsed");
		});
	}

	@Override
	public void initialize(final BundleContext bundleContext) {

		// Installs the new RelateNG JTS library (supposedly more efficient that RelateOp).
		System.setProperty("jts.relate", "ng");

		silenceGdalSidecarTraces();

		// Forces early initialisation of operation registry of JAI. It fixes initialisation
		// problems in some third party equinox x@applications such as OpenMOLE.
		// Launch ImageIO plugin scan on a virtual thread in parallel with GeoTools hints setup.
		runAsync(() -> {
			final JAI jaiDef = JAI.getDefaultInstance();
			if (!(jaiDef.getOperationRegistry() instanceof ConcurrentOperationRegistry)) {
				jaiDef.setOperationRegistry(ConcurrentOperationRegistry.initializeRegistry());
			}
			ImageIO.scanForPlugins();
			DEBUG.BANNER(BANNER_CATEGORY.JAI, "ImageIO extensions", "loaded for",
					StreamEx.of(ImageIO.getReaderFileSuffixes()).remove(String::isBlank).map(String::toUpperCase)
							.distinct().sorted().joining("|"));
		});

		// Set some GeoTools default hints to speed up operations
		runAsync(() -> {
			DEBUG.TIMER(BANNER_CATEGORY.GEOTLS, "GeoTools hints setup", "completed in", () -> {
				Hints.putSystemDefault(Hints.CRS_AUTHORITY_FACTORY, PropertyAuthorityFactory.class);
				Hints.putSystemDefault(Hints.FILTER_FACTORY, CommonFactoryFinder.getFilterFactory(null));
				Hints.putSystemDefault(Hints.STYLE_FACTORY, CommonFactoryFinder.getStyleFactory(null));
				Hints.putSystemDefault(Hints.FEATURE_FACTORY, CommonFactoryFinder.getFeatureFactory(null));
				Hints.putSystemDefault(Hints.USE_JAI_IMAGEREAD, true);
				final Hints defHints = GeoTools.getDefaultHints();
				// Initialize GridCoverageFactory so that we don't make a lookup every time a factory is needed
				Hints.putSystemDefault(Hints.GRID_COVERAGE_FACTORY,
						CoverageFactoryFinder.getGridCoverageFactory(defHints));
			});
		});

	}

}