/*******************************************************************************************************
 *
 * GamaPointType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.validation.IOperatorValidator;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.map.IMap;

/**
 * Type representing spatial points (locations) in 2D or 3D space. Points are fundamental geometric primitives in GAMA
 * used for agent locations, coordinates, and geometric operations.
 * 
 * <p>
 * Points in GAML:
 * </p>
 * <ul>
 * <li>Represent locations with x, y, and optionally z coordinates</li>
 * <li>Use floating-point precision (double) for coordinates</li>
 * <li>Support 2D (x, y) and 3D (x, y, z) representations</li>
 * <li>Are immutable value objects (operations return new points)</li>
 * <li>Can be used in arithmetic operations (addition, subtraction, scaling)</li>
 * <li>Serve as a special case of geometry (zero-dimensional)</li>
 * </ul>
 * 
 * <p>
 * Point literals and construction in GAML:
 * </p>
 * 
 * <pre>
 * // 2D point literal
 * point loc2d <- {10.5, 20.3};
 * 
 * // 3D point literal
 * point loc3d <- {10.5, 20.3, 5.0};
 * 
 * // Point from coordinates
 * point p <- point(5, 10);
 * 
 * // Point operations
 * point sum <- {1, 2} + {3, 4};     // {4, 6, 0}
 * point scaled <- {2, 3} * 2.0;      // {4, 6, 0}
 * float dist <- {0, 0} distance_to {3, 4};  // 5.0
 * </pre>
 * 
 * <p>
 * Type conversion to point:
 * </p>
 * <ul>
 * <li><b>Geometry → Point:</b> Returns the geometry's centroid/location</li>
 * <li><b>List → Point:</b> Uses first 3 elements as [x, y, z] coordinates</li>
 * <li><b>Map → Point:</b> Looks for 'x', 'y', 'z' keys to build coordinates</li>
 * <li><b>Number → Point:</b> Creates point {n, n, n} with all coordinates equal</li>
 * <li><b>Agent → Point:</b> Returns the agent's location</li>
 * </ul>
 * 
 * <p>
 * Points are compound types, meaning they can be decomposed into their components (x, y, z) when cast to containers.
 * The content type is float, and the key type is int (for indexing: 0=x, 1=y, 2=z).
 * </p>
 * 
 * @author drogoul
 * @since GAMA 1.0
 * 
 * @see IPoint
 * @see GamaPointFactory
 * @see GamaGeometryType
 */
@type (
		name = IKeyword.POINT,
		id = IType.POINT,
		wraps = { IPoint.class },
		kind = ISymbolKind.Variable.NUMBER,
		concept = { IConcept.TYPE, IConcept.POINT },
		doc = @doc ("Represent locations in either 2 or 3 dimensions"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaPointType extends GamaType<IPoint> {

	/**
	 * Constructs a new point type with the specified types manager.
	 *
	 * @param typesManager
	 *            the types manager that owns this type
	 */
	public GamaPointType(final ITypesManager typesManager) {
		super(typesManager);
	}

	/**
	 * Casts an object to a GAMA point.
	 * 
	 * <p>
	 * Conversion strategies:
	 * </p>
	 * <ul>
	 * <li><b>IPoint → Point:</b> Returns the point itself (or a copy if requested)</li>
	 * <li><b>Geometry → Point:</b> Returns the geometry's location/centroid</li>
	 * <li><b>Agent → Point:</b> Returns the agent's location attribute</li>
	 * <li><b>List → Point:</b> Uses first 3 numeric elements as x, y, z coordinates</li>
	 * <li><b>Map → Point:</b> Extracts "x", "y", "z" keys from map (defaults to 0 if missing)</li>
	 * <li><b>Number → Point:</b> Creates point where x = y = z = number</li>
	 * <li><b>String → Point:</b> Parses string representation like "{x, y, z}"</li>
	 * </ul>
	 * 
	 * <p>
	 * Examples:
	 * </p>
	 * 
	 * <pre>
	 * {@code
	 * // Cast list to point
	 * IList coords = GamaListFactory.create(scope, Types.FLOAT, 10.0, 20.0, 5.0);
	 * IPoint p1 = pointType.cast(scope, coords, null, false);
	 * // Returns point(10.0, 20.0, 5.0)
	 * 
	 * // Cast number to point
	 * IPoint p2 = pointType.cast(scope, 5.0, null, false);
	 * // Returns point(5.0, 5.0, 5.0)
	 * 
	 * // Cast map to point
	 * IMap map = GamaMapFactory.create();
	 * map.put("x", 10.0);
	 * map.put("y", 20.0);
	 * IPoint p3 = pointType.cast(scope, map, null, false);
	 * // Returns point(10.0, 20.0, 0.0)
	 * }
	 * </pre>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param obj
	 *            the object to cast to a point
	 * @param param
	 *            optional parameter (not used for points)
	 * @param copy
	 *            if true, creates a copy of the point (for mutable operations)
	 * @return the object cast to an IPoint
	 * @throws GamaRuntimeException
	 *             if the cast fails
	 * 
	 * @see GamaPointFactory#castToPoint(IScope, Object, boolean)
	 */
	@Override
	@doc ("""
			Transforms the parameter into a point. If it is already a point, returns it. \
			If it is a geometry, returns its location. If it is a list, interprets its elements as float values and use up to the first 3 ones to return a point. \
			If it is a map, tries to find 'x', 'y' and 'z' keys in it. If it is a number, returns a point with the x, y and equal to this value""")
	public IPoint cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return GamaPointFactory.castToPoint(scope, obj, copy);
	}

	/**
	 * Returns the default value for point type, which is null.
	 * 
	 * <p>
	 * Unlike primitive types (int defaults to 0, bool to false), points default to null to avoid creating unnecessary
	 * point objects. Code should check for null before using point values.
	 * </p>
	 *
	 * @return null
	 */
	@Override
	public IPoint getDefault() { return null; }

	/**
	 * Returns the content type for points when decomposed into components.
	 * 
	 * <p>
	 * Points are compound types that decompose into their x, y, z coordinates, which are floating-point values.
	 * </p>
	 *
	 * @return Types.FLOAT, the type of point coordinates
	 */
	@Override
	public IType getContentType() { return Types.get(FLOAT); }

	/**
	 * Returns the key type for indexing into point components.
	 * 
	 * <p>
	 * Points can be indexed like containers: p[0] = x, p[1] = y, p[2] = z. The index type is integer.
	 * </p>
	 *
	 * @return Types.INT, the type used for indexing point components
	 */
	@Override
	public IType getKeyType() { return Types.get(INT); }

	/**
	 * Indicates that points can be cast to constant expressions.
	 * 
	 * <p>
	 * Point literals like {10, 20} can be evaluated at compile time and used in constant contexts.
	 * </p>
	 *
	 * @return true, points support constant casting
	 */
	@Override
	public boolean canCastToConst() {
		return true;
	}

	/**
	 * Indicates that points are drawable in displays.
	 * 
	 * <p>
	 * Points can be rendered in graphical displays as markers, dots, or geometric primitives.
	 * </p>
	 *
	 * @return true, points can be drawn
	 */
	@Override
	public boolean isDrawable() { return true; }

	/**
	 * Indicates that points are compound types.
	 * 
	 * <p>
	 * Points can be decomposed into their x, y, z components when cast to containers like lists.
	 * </p>
	 *
	 * @return true, points are compound types
	 */
	@Override
	public boolean isCompoundType() { return true; }

	/**
	 * Deserializes a point from a JSON map representation.
	 * 
	 * <p>
	 * Expects a map with "x", "y", and optionally "z" keys. Missing coordinates default to 0.0.
	 * </p>
	 * 
	 * <p>
	 * Example JSON:
	 * </p>
	 * 
	 * <pre>
	 * {"x": 10.5, "y": 20.3, "z": 5.0}
	 * </pre>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param map2
	 *            the JSON map containing point coordinates
	 * @return the deserialized point
	 * 
	 * @see GamaPointFactory#createFromXYZMap(IScope, IMap)
	 */
	@Override
	public IPoint deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		return GamaPointFactory.createFromXYZMap(scope, map2);
	}

	/**
	 * Validator for point constructor expressions. Ensures that all coordinates are numeric types (int or float).
	 * 
	 * <p>
	 * Used during compilation to validate expressions like {@code point(x, y)} or {@code point(x, y, z)}, ensuring
	 * that all arguments are numbers.
	 * </p>
	 * 
	 * @author drogoul
	 */
	public static class PointValidator implements IOperatorValidator {

		/**
		 * Validates that all arguments to a point constructor are numeric.
		 *
		 * @param context
		 *            the compilation context for error reporting
		 * @param emfContext
		 *            the EMF model element being validated
		 * @param arguments
		 *            the expressions passed to the point constructor
		 * @return true if all arguments are numeric, false otherwise
		 */
		@Override
		public boolean validate(final IDescription context, final EObject emfContext, final IExpression... arguments) {
			for (final IExpression expr : arguments) {
				if (!expr.getGamlType().isNumber()) {
					context.error("Points can only be built with int or float coordinates", WRONG_TYPE, emfContext);
					return false;
				}
			}
			return true;
		}
	}

}
