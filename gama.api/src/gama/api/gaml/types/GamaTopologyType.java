/*******************************************************************************************************
 *
 * GamaTopologyType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
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
import gama.api.data.factories.GamaShapeFactory;
import gama.api.data.factories.GamaTopologyFactory;
import gama.api.data.objects.IMap;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.topology.ITopology;
import gama.api.runtime.scope.IScope;

/**
 * The type topology.
 *
 * @author Alexis Drogoul
 * @since 26 nov. 2011
 *
 */
@SuppressWarnings ("unchecked")
@type (
		name = IKeyword.TOPOLOGY,
		id = IType.TOPOLOGY,
		wraps = { ITopology.class },
		kind = ISymbolKind.Variable.REGULAR,
		concept = { IConcept.TYPE, IConcept.TOPOLOGY },
		doc = @doc ("Represents a topology, obtained from agents or geometries, that can be used to compute distances, neighbours, etc."))
public class GamaTopologyType extends GamaType<ITopology> {

	/**
	 * @param typesManager
	 * @param varKind
	 * @param id
	 * @param name
	 * @param support
	 */
	public GamaTopologyType(final ITypesManager typesManager) {
		super(typesManager);
	}

	@Override
	@doc (
			value = "casting of the operand to a topology.",
			usages = { @usage ("if the operand is a topology, returns the topology itself;"),
					@usage ("if the operand is a spatial graph, returns the graph topology associated;"),
					@usage ("if the operand is a population, returns the topology of the population;"),
					@usage ("if the operand is a shape or a geometry, returns the continuous topology bounded by the geometry;"),
					@usage ("if the operand is a matrix, returns the grid topology associated"),
					@usage ("if the operand is another kind of container, returns the multiple topology associated to the container"),
					@usage ("otherwise, casts the operand to a geometry and build a topology from it.") },
			examples = { @example (
					value = "topology(0)",
					equals = "nil",
					isExecutable = true),
					@example (
							value = "topology(a_graph)	--: Multiple topology in POLYGON ((24.712119771887785 7.867357373616512, 24.712119771887785 61.283226839310565, 82.4013676510046  7.867357373616512)) "
									+ "at location[53.556743711446195;34.57529210646354]",
							isExecutable = false) },
			see = { "geometry" })
	public ITopology cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return GamaTopologyFactory.createFrom(scope, obj, copy);
	}

	/**
	 * @throws GamaRuntimeException
	 * @param scope
	 * @param obj
	 * @return
	 */

	/**
	 * @see gama.internal.types.GamaType#getDefault()
	 */
	@Override
	public ITopology getDefault() { return null; }

	@Override
	public IType<?> getContentType() { return Types.GEOMETRY; }

	@Override
	public boolean canCastToConst() {
		return false;
	}

	@Override
	public ITopology deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		return GamaTopologyFactory.createFrom(scope,
				GamaShapeFactory.createFrom(scope, map2.get("environment"), false));
	}

}
