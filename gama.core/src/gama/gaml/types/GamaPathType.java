/*******************************************************************************************************
 *
 * GamaPathType.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.types;

import java.util.List;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.core.util.path.GamaPath;
import gama.core.util.path.IPath;
import gama.core.util.path.PathFactory;
import gama.gaml.operators.Cast;

/**
 * The Class GamaPathType.
 */
@type (
		name = IKeyword.PATH,
		id = IType.PATH,
		wraps = { IPath.class, GamaPath.class },
		kind = ISymbolKind.Variable.REGULAR,
		concept = { IConcept.TYPE },
		doc = @doc ("Ordered lists of objects that represent a path in a graph"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaPathType extends GamaType<IPath> {

	@doc (
			value = "Cast any object as a path",
			usages = { @usage (
					value = "if the operand is a path, returns this path"),
					@usage (
							value = "if the operand is a geometry of an agent, returns a path from the list of points of the geometry"),
					@usage (
							value = "if the operand is a list, cast each element of the list as a point and create a path from these points",
							examples = { @example ("path p <- path([{12,12},{30,30},{50,50}]);") }) })
	@Override
	public IPath cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return staticCast(scope, obj, param, copy);
	}

	@Override
	public IPath getDefault() {
		return null;
	}

	/**
	 * Static cast.
	 *
	 * @param scope the scope
	 * @param obj the obj
	 * @param param the param
	 * @param copy the copy
	 * @return the i path
	 */
	public static IPath staticCast(final IScope scope, final Object obj, final Object param, final boolean copy) {
		if (obj instanceof IPath) return (IPath) obj;
		if (obj instanceof IShape) {
			IShape shape = (IShape) obj;
			return PathFactory.newInstance(scope, shape.getPoints(), false);
		}

		if (obj instanceof List) {
			// List<GamaPoint> list = new GamaList();
			final List<IShape> list = GamaListFactory.create(Types.GEOMETRY);
			boolean isEdges = true;

			for (final Object p : (List) obj) {
				list.add(Cast.asPoint(scope, p));
				if (isEdges && (!(p instanceof IShape) || !((IShape) p).isLine())) { isEdges = false; }
			}
			// return new GamaPath(scope.getTopology(), list);
			return PathFactory.newInstance(scope, isEdges ? (IList<IShape>) obj : (IList<IShape>) list, isEdges);
		}
		return null;
	}

	@Override
	public boolean isDrawable() {
		return true;
	}

	@Override
	public boolean canCastToConst() {
		return false;
	}

}
