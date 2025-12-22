/*******************************************************************************************************
 *
 * IFeatureCollectionHandler.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies.geojson.feature;

import org.geotools.api.feature.simple.SimpleFeature;

import gama.dependencies.geojson.IContentHandler;

/**
 * The Interface IFeatureCollectionHandler.
 */
public interface IFeatureCollectionHandler extends IContentHandler<SimpleFeature> {

	// boolean hasMoreFeatures();
}
