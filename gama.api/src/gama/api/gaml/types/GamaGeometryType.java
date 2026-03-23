/*******************************************************************************************************
 *
 * GamaGeometryType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IShape;

/**
 * Type representing geometries in GAML - the foundation for all spatial operations and agent shapes.
 * <p>
 * Geometries are the primary spatial data type in GAMA, representing shapes, locations, and spatial extents. They
 * provide the support for agent shapes, spatial analysis, GIS operations, and all geometric computations. GAMA's
 * geometry type wraps JTS (Java Topology Suite) geometries with additional GAMA-specific functionality.
 * </p>
 * 
 * <h2>Key Features:</h2>
 * <ul>
 * <li>Support for all standard geometric types (point, line, polygon, multi-geometries)</li>
 * <li>Spatial operations (intersection, union, buffer, etc.)</li>
 * <li>GIS integration (shapefile import/export, coordinate systems)</li>
 * <li>Agent shape representation</li>
 * <li>Attributes and metadata storage</li>
 * <li>3D geometry support</li>
 * <li>Drawable for visualization</li>
 * </ul>
 * 
 * <h2>Geometric Types Supported:</h2>
 * <ul>
 * <li>Point - 0-dimensional location</li>
 * <li>LineString - 1-dimensional path</li>
 * <li>Polygon - 2-dimensional area with optional holes</li>
 * <li>MultiPoint, MultiLineString, MultiPolygon - collections</li>
 * <li>GeometryCollection - heterogeneous collections</li>
 * </ul>
 * 
 * <h2>Usage Examples:</h2>
 * 
 * <pre>
 * {@code
 * // Create from coordinates
 * geometry point_geom <- {10, 20};  // Point at (10, 20)
 * 
 * // Create polygon
 * geometry square <- square(50);
 * geometry circle <- circle(30);
 * 
 * // From WKT (Well-Known Text)
 * geometry poly <- geometry("POLYGON((0 0, 10 0, 10 10, 0 10, 0 0))");
 * 
 * // Spatial operations
 * geometry intersection <- geom1 inter geom2;
 * geometry buffer_zone <- my_geom buffer 10.0;
 * float area <- my_polygon.area;
 * float perimeter <- my_polygon.perimeter;
 * 
 * // Agent shape
 * create my_species {
 *     shape <- circle(5);
 *     location <- {50, 50};
 * }
 * 
 * // GIS operations
 * geometry transformed <- my_geom transformed_by("EPSG:4326", "EPSG:32632");
 * }
 * </pre>
 * 
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @see GamaType
 * @see IShape
 * @see gama.api.types.geometry.GamaShapeFactory
 * @since GAMA 1.0
 */
@type (
		name = IKeyword.GEOMETRY,
		id = IType.GEOMETRY,
		wraps = { IShape.class },
		kind = ISymbolKind.REGULAR,
		concept = { IConcept.TYPE, IConcept.GEOMETRY },
		doc = @doc ("Represents geometries, i.e. the support for the shapes of agents and all the spatial operations in GAMA."))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaGeometryType extends GamaType<IShape> {

	/**
	 * Constructs a new geometry type.
	 * 
	 * @param typesManager
	 *            the types manager responsible for type resolution and management
	 */
	public GamaGeometryType(final ITypesManager typesManager) {
		super(typesManager);
	}

	/**
	 * Casts an object to a geometry.
	 * <p>
	 * This method provides flexible conversion to geometries from various source types:
	 * <ul>
	 * <li>Geometry or Agent - returns the geometry directly or the agent's shape</li>
	 * <li>Species - returns the union of all its agents' geometries</li>
	 * <li>Pair - attempts to build a line segment from two points</li>
	 * <li>File (shapefile, etc.) - returns the union of geometries contained in the file</li>
	 * <li>Container of points - builds a polygon or linestring from the points</li>
	 * <li>Container of geometries - returns their spatial union</li>
	 * <li>String - interprets as WKT (Well-Known Text) specification</li>
	 * <li>Point - creates a point geometry</li>
	 * </ul>
	 * Returns null if conversion is not possible.
	 * </p>
	 * 
	 * @param scope
	 *            the current execution scope
	 * @param obj
	 *            the object to cast to a geometry
	 * @param param
	 *            optional parameter (not used for geometry casting)
	 * @param copy
	 *            whether to create a copy of the geometry
	 * @return the geometry representation of the object, or null if casting fails
	 * @throws GamaRuntimeException
	 *             if the casting operation encounters an error
	 */
	@Override
	@doc ("""
			Cast the argument into a geometry. If the argument is already a geometry or an agent, returns it; \
			if it is a species, returns the union of all its agents' geometries; if it is a pair, tries to build a segment from it; \
			if it is a file containing geometries, returns the union of these geometries; \
			if it is a container and its contents are points, builds the resulting geometry, \
			otherwise cast the objects present in the container as geometries and returns their union; \
			if it is a string, interprets it as a wkt specification; otherwise, returns nil.\s""")
	public IShape cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return GamaShapeFactory.castToShape(scope, obj, copy);
	}

	/**
	 * Returns the default value for geometry type.
	 * <p>
	 * The default geometry is null, as there is no meaningful default shape.
	 * </p>
	 * 
	 * @return null
	 */
	@Override
	public IShape getDefault() { return null; }

	/**
	 * Indicates whether geometries can be drawn/visualized.
	 * <p>
	 * Geometries are drawable and form the basis of GAMA's graphical displays.
	 * </p>
	 * 
	 * @return true, geometries can be visualized
	 */
	@Override
	public boolean isDrawable() { return true; }

	/**
	 * Returns the key type for accessing geometry attributes.
	 * <p>
	 * Geometry attributes are accessed using string keys (attribute names).
	 * </p>
	 * 
	 * @return the string type
	 */
	@Override
	public IType getKeyType() { return Types.STRING; }

	/**
	 * Indicates whether geometries have a fixed length.
	 * <p>
	 * Geometries are not fixed-length as their attribute collections can vary.
	 * </p>
	 * 
	 * @return false, geometry attribute count is not fixed
	 */
	@Override
	public boolean isFixedLength() { return false; }

	/**
	 * Indicates whether geometries can be cast to constant values.
	 * <p>
	 * Geometries cannot be constant as they are mutable and can change during simulation.
	 * </p>
	 * 
	 * @return false, geometries are not constant
	 */
	@Override
	public boolean canCastToConst() {
		return false;
	}

}
