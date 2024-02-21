/*******************************************************************************************************
 *
 * GamaTopologyType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.types;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.ITopology;
import gama.core.metamodel.topology.continuous.ContinuousTopology;
import gama.core.metamodel.topology.continuous.MultipleTopology;
import gama.core.metamodel.topology.graph.GamaSpatialGraph;
import gama.core.metamodel.topology.graph.ISpatialGraph;
import gama.core.metamodel.topology.grid.GridTopology;
import gama.core.metamodel.topology.grid.IGrid;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IContainer;
import gama.core.util.IMap;
import gama.gaml.operators.Cast;
import gama.gaml.species.ISpecies;

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
	 * Static cast.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @param copy
	 *            the copy
	 * @return the i topology
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@SuppressWarnings ("rawtypes")
	public static ITopology staticCast(final IScope scope, final Object obj, final boolean copy)
			throws GamaRuntimeException {
		// Many cases.
		if (obj == null) return null;
		if (obj instanceof ISpatialGraph) return ((ISpatialGraph) obj).getTopology(scope);
		if (obj instanceof ITopology) return (ITopology) obj;
		if (obj instanceof IAgent) return ((IAgent) obj).getTopology();
		if (obj instanceof IPopulation) return ((IPopulation) obj).getTopology();
		if (obj instanceof ISpecies) return staticCast(scope, scope.getAgent().getPopulationFor((ISpecies) obj), copy);
		if (obj instanceof IShape) return from(scope, (IShape) obj);
		if (obj instanceof IContainer) return from(scope, (IContainer) obj);
		return staticCast(scope, Cast.asGeometry(scope, obj, copy), copy);
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
		return staticCast(scope, obj, copy);
	}

	/**
	 * From.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @return the i topology
	 */
	public static ITopology from(final IScope scope, final IShape obj) {
		return new ContinuousTopology(scope, obj);
	}

	/**
	 * @throws GamaRuntimeException
	 * @param scope
	 * @param obj
	 * @return
	 */
	private static ITopology from(final IScope scope, final IContainer<?, IShape> obj) throws GamaRuntimeException {
		if (obj instanceof GamaSpatialGraph) return ((GamaSpatialGraph) obj).getTopology(scope);
		if (obj instanceof IGrid) return new GridTopology(scope, (IGrid) obj);
		return new MultipleTopology(scope, obj);
	}

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
		return from(scope, Cast.asGeometry(scope, map2.get("environment")));
	}

}
