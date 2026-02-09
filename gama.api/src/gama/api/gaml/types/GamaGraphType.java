/*******************************************************************************************************
 *
 * GamaGraphType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.constants.IKeyword;
import gama.api.data.factories.GamaGraphFactory;
import gama.api.data.objects.IGraph;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;

/**
 * The Class GamaGraphType.
 */
@type (
		name = IKeyword.GRAPH,
		id = IType.GRAPH,
		wraps = { IGraph.class },
		kind = ISymbolKind.Variable.CONTAINER,
		concept = { IConcept.TYPE, IConcept.GRAPH },
		doc = @doc ("Special type of container composed of edges and vertices"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaGraphType extends GamaContainerType<IGraph> {

	@Override
	public IGraph cast(final IScope scope, final Object obj, final Object param, final IType keyType,
			final IType contentsType, final boolean copy) throws GamaRuntimeException {
		return GamaGraphFactory.createFrom(scope, obj, param, copy);
	}

	@Override
	public int getNumberOfParameters() { return 2; }

	@Override
	public boolean canCastToConst() {
		return false;
	}

	@Override
	public boolean isDrawable() { return true; }

}
