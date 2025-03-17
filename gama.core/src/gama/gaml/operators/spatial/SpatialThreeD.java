/*******************************************************************************************************
 *
 * SpatialThreeD.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.operators.spatial;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Geometry;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.GamlAnnotations.test;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IOperatorCategory;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IContainer;
import gama.gaml.types.Types;

/**
 * The Class ThreeD.
 */
public class SpatialThreeD {

	/**
	 * Sets the z.
	 *
	 * @param scope
	 *            the scope
	 * @param geom
	 *            the geom
	 * @param index
	 *            the index
	 * @param z
	 *            the z
	 * @return the i shape
	 */
	@operator (
			value = { "set_z" },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.THREED },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.POINT, IConcept.THREED })
	@doc (
			value = "Sets the z ordinate of the n-th point of a geometry to the value provided by the third argument",
			masterDoc = true,
			examples = { @example (
					value = "set_z (triangle(3), 1, 3.0)",
					test = false) },
			see = {})
	@test ("set_z (triangle(3), 1, 3.0).points[1].z = 3.0")
	public static IShape set_z(final IScope scope, final IShape geom, final Integer index, final Double z) {
		if (geom == null) return null;
		final Geometry g = geom.getInnerGeometry();
		if (g == null) return geom;
		if (index < 0 || index > g.getNumPoints() - 1)
			throw GamaRuntimeException.warning("Trying to modify a point outside the bounds of the geometry", scope);
		g.apply(new CoordinateSequenceFilter() {

			boolean done = false;

			@Override
			public void filter(final CoordinateSequence seq, final int i) {
				if (i == index) {
					seq.getCoordinate(i).z = z;
					done = true;
				}
			}

			@Override
			public boolean isDone() { return done; }

			@Override
			public boolean isGeometryChanged() { return done; }
		});

		return geom;
	}

	/**
	 * Sets the z.
	 *
	 * @param scope
	 *            the scope
	 * @param geom
	 *            the geom
	 * @param coords
	 *            the coords
	 * @return the i shape
	 */
	@operator (
			value = { "set_z" },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.THREED },
			concept = {})
	@doc (
			value = "Sets the z ordinate of each point of a geometry to the value provided, in order, by the right argument",
			examples = { @example (
					value = "triangle(3) set_z [5,10,14]",
					test = false) },
			see = {})
	@test ("list<int> zzz <- (triangle(3) set_z [5,10,14]).points collect each.z; zzz[1] = 10")
	public static IShape set_z(final IScope scope, final IShape geom, final IContainer coords) {
		if (geom == null) return null;
		final Geometry g = geom.getInnerGeometry();
		if (g == null) return geom;
		if (coords == null || coords.isEmpty(scope)) return null;
		if (coords.length(scope) > g.getNumPoints())
			throw GamaRuntimeException.warning("Trying to modify a point outside the bounds of the geometry", scope);
		final double[] zs = new double[coords.length(scope)];
		int i = 0;
		for (final Object o : coords.iterable(scope)) { zs[i++] = Types.FLOAT.cast(scope, o, null, false); }
		g.apply(new CoordinateSequenceFilter() {

			@Override
			public void filter(final CoordinateSequence seq, final int i) {
				if (i <= zs.length - 1) { seq.getCoordinate(i).z = zs[i]; }
			}

			@Override
			public boolean isDone() { return false; }

			@Override
			public boolean isGeometryChanged() { return true; }
		});

		return geom;
	}

}
