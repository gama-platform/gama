/*******************************************************************************************************
 *
 * IJsonConstants.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file.json;

import gama.core.common.interfaces.IKeyword;

/**
 * The Interface IJsonConstants.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 nov. 2023
 */
public interface IJsonConstants {

	/** The Constant NAME_GEOMETRY. */
	String NAME_GEOMETRY = "geometry";

	/** The Constant NAME_FEATURES. */
	String NAME_FEATURES = "features";

	/** The Constant NAME_GEOMETRIES. */
	String NAME_GEOMETRIES = "geometries";

	/** The Constant NAME_CRS. */
	String NAME_CRS = "crs";

	/** The Constant NAME_PROPERTIES. */
	String NAME_PROPERTIES = "properties";

	/** The Constant NAME_NAME. */
	String NAME_NAME = IKeyword.NAME;

	/** The Constant NAME_TYPE. */
	String NAME_TYPE = IKeyword.TYPE;

	/** The Constant NAME_POINT. */
	String NAME_POINT = "Point";

	/** The Constant NAME_LINESTRING. */
	String NAME_LINESTRING = "LineString";

	/** The Constant NAME_POLYGON. */
	String NAME_POLYGON = "Polygon";

	/** The Constant NAME_COORDINATES. */
	String NAME_COORDINATES = "coordinates";

	/** The Constant NAME_GEOMETRYCOLLECTION. */
	String NAME_GEOMETRYCOLLECTION = "GeometryCollection";

	/** The Constant NAME_MULTIPOLYGON. */
	String NAME_MULTIPOLYGON = "MultiPolygon";

	/** The Constant NAME_MULTILINESTRING. */
	String NAME_MULTILINESTRING = "MultiLineString";

	/** The Constant NAME_MULTIPOINT. */
	String NAME_MULTIPOINT = "MultiPoint";

	/** The Constant NAME_FEATURE. */
	String NAME_FEATURE = "Feature";

	/** The Constant NAME_FEATURECOLLECTION. */
	String NAME_FEATURECOLLECTION = "FeatureCollection";

	/** The Constant GAML_TYPE_LABEL. */
	String GAML_TYPE_LABEL = "gaml_type";

	/** The Constant GAML_SPECIES_LABEL. */
	String GAML_SPECIES_LABEL = "gaml_species";

	/** The Constant GAMA_OBJECT_LABEL. */
	String CONTENTS_WITH_REFERENCES_LABEL = "gama_contents";

	/** The Constant AGENT_REFERENCE_LABEL. */
	String AGENT_REFERENCE_LABEL = "agent_reference";

	/** The Constant REFERENCE_TABLE_LABEL. */
	String REFERENCE_TABLE_LABEL = "gama_references";

	/**
	 * Represents the JSON literal <code>null</code>.
	 */
	JsonValue NULL = new JsonNull();

	/**
	 * Represents the JSON literal <code>true</code>.
	 */
	JsonValue TRUE = new JsonTrue();

	/**
	 * Represents the JSON literal <code>false</code>.
	 */
	JsonValue FALSE = new JsonFalse();

	/** The nan. */
	JsonValue NAN = new JsonFloat(Double.NaN);

	/** The positive infinity. */
	JsonValue POSITIVE_INFINITY = new JsonFloat(Double.POSITIVE_INFINITY);

	/** The negative infinity. */
	JsonValue NEGATIVE_INFINITY = new JsonFloat(Double.NEGATIVE_INFINITY);

}
