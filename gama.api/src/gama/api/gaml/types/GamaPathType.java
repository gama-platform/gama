/*******************************************************************************************************
 *
 * GamaPathType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.type;
import gama.annotations.usage;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.constants.IKeyword;
import gama.api.data.factories.GamaPathFactory;
import gama.api.data.objects.IPath;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;

/**
 * The Class GamaPathType.
 */
@type (
		name = IKeyword.PATH,
		id = IType.PATH,
		wraps = { IPath.class },
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
		return GamaPathFactory.createFrom(scope, obj, param, copy);
	}

	@Override
	public IPath getDefault() { return null; }

	@Override
	public boolean isDrawable() { return true; }

	@Override
	public boolean canCastToConst() {
		return false;
	}

}
