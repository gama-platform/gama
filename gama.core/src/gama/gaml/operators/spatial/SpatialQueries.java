/*******************************************************************************************************
 *
 * SpatialQueries.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.operators.spatial;

import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;

import com.google.common.collect.Ordering;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.no_test;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IOperatorCategory;
import gama.annotations.precompiler.ITypeProvider;
import gama.annotations.precompiler.Reason;
import gama.core.common.preferences.GamaPreferences;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.AbstractTopology;
import gama.core.metamodel.topology.ITopology;
import gama.core.metamodel.topology.filter.Different;
import gama.core.metamodel.topology.filter.IAgentFilter;
import gama.core.metamodel.topology.filter.In;
import gama.core.runtime.IScope;
import gama.core.util.Collector;
import gama.core.util.GamaListFactory;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.gaml.operators.Cast;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class Queries.
 */
public class SpatialQueries {

	/**
	 * Neighbors of.
	 *
	 * @param scope
	 *            the scope
	 * @param t
	 *            the t
	 * @param agent
	 *            the agent
	 * @return the i list
	 */
	@operator (
			value = "neighbors_of",
			content_type = IType.AGENT,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_QUERIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.AGENT_LOCATION, IConcept.NEIGHBORS })
	@doc (
			value = "a list, containing all the agents of the same species than the argument (if it is an agent) located at a distance inferior or equal to 1 to the right-hand operand agent considering the left-hand operand topology.",
			masterDoc = true,
			examples = { @example (
					value = "topology(self) neighbors_of self",
					equals = "returns all the agents located at a distance lower or equal to 1 to the agent applying the operator considering its topology.",
					test = false) },
			see = { "neighbors_at", "closest_to", "overlapping", "agents_overlapping", "agents_inside",
					"agent_closest_to" })
	@no_test // already done in Spatial tests Models
	public static IList neighbors_of(final IScope scope, final ITopology t, final IAgent agent) {
		return _neighbors(scope, In.list(scope, agent.getPopulation()), agent, 1.0, t);
		// TODO We could compute a filter based on the population if it is
		// an agent
	}

	/**
	 * Neighbors of.
	 *
	 * @param scope
	 *            the scope
	 * @param t
	 *            the t
	 * @param agent
	 *            the agent
	 * @param distance
	 *            the distance
	 * @return the i list
	 */
	@operator (
			value = "neighbors_of",
			content_type = IType.AGENT,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_QUERIES },
			concept = {})
	/* TODO, expected_content_type = { IType.FLOAT, IType.INT } */
	@doc (
			usages = @usage (
					value = "a list, containing all the agents of the same species than the left argument (if it is an agent) located at a distance inferior or equal to the third argument to the second argument (agent, geometry or point) considering the first operand topology.",
					examples = { @example (
							value = "neighbors_of (topology(self), self,10)",
							equals = "all the agents located at a distance lower or equal to 10 to the agent applying the operator considering its topology.",
							test = false) }))
	@no_test // already done in Spatial tests Models
	public static IList neighbors_of(final IScope scope, final ITopology t, final IShape agent, final Double distance) {
		return _neighbors(scope,
				agent instanceof IAgent ? In.list(scope, ((IAgent) agent).getPopulation()) : Different.with(), agent,
				distance, t);
		// TODO We could compute a filter based on the population if it is
		// an agent
	}

	/**
	 * Neighbors at.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the agent
	 * @param distance
	 *            the distance
	 * @return the i list
	 */
	@operator (
			value = "neighbors_at",
			content_type = ITypeProvider.TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_QUERIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.AGENT_LOCATION, IConcept.NEIGHBORS })
	@doc (
			value = "a list, containing all the agents of the same species than the left argument (if it is an agent) located at a distance inferior or equal to the right-hand operand to the left-hand operand (geometry, agent, point).",
			comment = "The topology used to compute the neighborhood  is the one of the left-operand if this one is an agent; otherwise the one of the agent applying the operator.",
			examples = { @example (
					value = "(self neighbors_at (10))",
					equals = "all the agents located at a distance lower or equal to 10 to the agent applying the operator.",
					test = false) },
			see = { "neighbors_of", "closest_to", "overlapping", "agents_overlapping", "agents_inside",
					"agent_closest_to", "at_distance" })
	@no_test // already done in Spatial tests Models
	public static IList neighbors_at(final IScope scope, final IShape source, final Double distance) {
		ITopology topology;
		IAgentFilter filter;
		// See issue #3926 : distinguish when the source is an agent or a simple geometry
		if (source instanceof IAgent agent) {
			topology = agent.getTopology();
			filter = In.list(scope, agent.getPopulation());
		} else {
			topology = scope.getTopology();
			filter = Different.with();
		}
		return _neighbors(scope, filter, source, distance, topology);
	}

	/**
	 * At distance.
	 *
	 * @param scope
	 *            the scope
	 * @param list
	 *            the list
	 * @param distance
	 *            the distance
	 * @return the i list<? extends I shape>
	 */
	@operator (
			value = "at_distance",
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_QUERIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.AGENT_LOCATION })
	@doc (
			value = "A list of agents or geometries among the left-operand list that are located at a distance <= the right operand from the caller agent (in its topology)",
			examples = { @example (
					value = "[ag1, ag2, ag3] at_distance 20",
					equals = "the agents of the list located at a distance <= 20 from the caller agent (in the same order).",
					isExecutable = false) },
			see = { "neighbors_at", "neighbors_of", "agent_closest_to", "agents_inside", "closest_to", "inside",
					"overlapping" })
	@no_test // already done in Spatial tests Models
	public static IList<? extends IShape> at_distance(final IScope scope, final IContainer<?, ? extends IShape> list,
			final Double distance) {
		final IType contentType = list.getGamlType().getContentType();
		if (contentType.isAgentType()) return _neighbors(scope, In.list(scope, list), scope.getAgent(), distance);
		if (contentType == Types.GEOMETRY) return geomAtDistance(scope, list, distance);
		return GamaListFactory.create();
	}

	/**
	 * Geom at distance.
	 *
	 * @param scope
	 *            the scope
	 * @param list
	 *            the list
	 * @param distance
	 *            the distance
	 * @return the i list<? extends I shape>
	 */
	public static IList<? extends IShape> geomAtDistance(final IScope scope, final IContainer<?, ? extends IShape> list,
			final Double distance) {
		final IShape ag = scope.getAgent();
		final IList<IShape> geoms = GamaListFactory.create(Types.GEOMETRY);
		for (final Object shape : list.listValue(scope, Types.GEOMETRY, false)) {
			if (!(shape instanceof IShape)) { continue; }
			if (scope.getTopology().distanceBetween(scope, ag, (IShape) shape) <= distance) {
				geoms.add((IShape) shape);
			}
		}
		return geoms;
	}

	/**
	 * Related entities.
	 *
	 * @param scope
	 *            the scope
	 * @param list
	 *            the list
	 * @param source
	 *            the source
	 * @param relation
	 *            the relation
	 * @return the i list<? extends I shape>
	 */
	static IList<? extends IShape> relatedEntities(final IScope scope, final IContainer<?, ? extends IShape> list,
			final IShape source, final ITopology.SpatialRelation relation) {
		final IType contentType = list.getGamlType().getContentType();
		if (contentType.isAgentType()) return _gather(scope, In.list(scope, list), source, relation);
		if (Types.GEOMETRY.isAssignableFrom(contentType)) return geomsRelated(scope, list, source, relation);
		return GamaListFactory.EMPTY_LIST;
	}

	/**
	 * Inside.
	 *
	 * @param scope
	 *            the scope
	 * @param list
	 *            the list
	 * @param source
	 *            the source
	 * @return the i list<? extends I shape>
	 */
	@operator (
			value = { "inside" },
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_QUERIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.AGENT_LOCATION })
	@doc (
			value = "A list of agents or geometries among the left-operand list, species or meta-population (addition of species), covered by the operand (casted as a geometry).",
			examples = { @example (
					value = "[ag1, ag2, ag3] inside(self)",
					equals = "the agents among ag1, ag2 and ag3 that are covered by the shape of the right-hand argument.",
					isExecutable = false),
					@example (
							value = "(species1 + species2) inside (self)",
							equals = "the agents among species species1 and species2 that are covered by the shape of the right-hand argument.",
							isExecutable = false) },
			see = { "neighbors_at", "neighbors_of", "closest_to", "overlapping", "agents_overlapping", "agents_inside",
					"agent_closest_to" })
	@no_test // already done in Spatial tests Models
	public static IList<? extends IShape> inside(final IScope scope, final IContainer<?, ? extends IShape> list,
			final IShape source) {
		return relatedEntities(scope, list, source, ITopology.SpatialRelation.INSIDE);
	}

	/**
	 * Covering.
	 *
	 * @param scope
	 *            the scope
	 * @param list
	 *            the list
	 * @param source
	 *            the source
	 * @return the i list<? extends I shape>
	 */
	@operator (
			value = { "covering" },
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_QUERIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.AGENT_LOCATION })
	@doc (
			value = "A list of agents or geometries among the left-operand list, species or meta-population (addition of species), covering the operand (casted as a geometry).",
			examples = { @example (
					value = "[ag1, ag2, ag3] covering(self)",
					equals = "the agents among ag1, ag2 and ag3 that cover the shape of the right-hand argument.",
					isExecutable = false),
					@example (
							value = "(species1 + species2) covering (self)",
							equals = "the agents among species species1 and species2 that covers the shape of the right-hand argument.",
							isExecutable = false) },
			see = { "neighbors_at", "neighbors_of", "closest_to", "overlapping", "agents_overlapping", "inside",
					"agents_inside", "agent_closest_to" })
	@no_test // already done in Spatial tests Models
	public static IList<? extends IShape> covering(final IScope scope, final IContainer<?, ? extends IShape> list,
			final IShape source) {
		return relatedEntities(scope, list, source, ITopology.SpatialRelation.COVER);
	}

	/**
	 * Crossing.
	 *
	 * @param scope
	 *            the scope
	 * @param list
	 *            the list
	 * @param source
	 *            the source
	 * @return the i list<? extends I shape>
	 */
	@operator (
			value = { "crossing" },
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_QUERIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.AGENT_LOCATION })
	@doc (
			value = "A list of agents or geometries among the left-operand list, species or meta-population (addition of species), crossing the operand (casted as a geometry).",
			examples = { @example (
					value = "[ag1, ag2, ag3] crossing(self)",
					equals = "the agents among ag1, ag2 and ag3 that cross the shape of the right-hand argument.",
					isExecutable = false),
					@example (
							value = "(species1 + species2) crossing (self)",
							equals = "the agents among species species1 and species2 that cross the shape of the right-hand argument.",
							isExecutable = false) },
			see = { "neighbors_at", "neighbors_of", "closest_to", "overlapping", "agents_overlapping", "inside",
					"agents_inside", "agent_closest_to" })
	@no_test // already done in Spatial tests Models
	public static IList<? extends IShape> crossing(final IScope scope, final IContainer<?, ? extends IShape> list,
			final IShape source) {
		return relatedEntities(scope, list, source, ITopology.SpatialRelation.CROSS);
	}

	/**
	 * Touching.
	 *
	 * @param scope
	 *            the scope
	 * @param list
	 *            the list
	 * @param source
	 *            the source
	 * @return the i list<? extends I shape>
	 */
	@operator (
			value = { "touching" },
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_QUERIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.AGENT_LOCATION })
	@doc (
			value = "A list of agents or geometries among the left-operand list, species or meta-population (addition of species), touching the operand (casted as a geometry).",
			examples = { @example (
					value = "[ag1, ag2, ag3] toucing(self)",
					equals = "the agents among ag1, ag2 and ag3 that touch the shape of the right-hand argument.",
					isExecutable = false),
					@example (
							value = "(species1 + species2) touching (self)",
							equals = "the agents among species species1 and species2 that touch the shape of the right-hand argument.",
							isExecutable = false) },
			see = { "neighbors_at", "neighbors_of", "closest_to", "overlapping", "agents_overlapping", "inside",
					"agents_inside", "agent_closest_to" })
	@no_test // already done in Spatial tests Models
	public static IList<? extends IShape> touching(final IScope scope, final IContainer<?, ? extends IShape> list,
			final IShape source) {
		return relatedEntities(scope, list, source, ITopology.SpatialRelation.TOUCH);
	}

	/**
	 * Partially overlapping.
	 *
	 * @param scope
	 *            the scope
	 * @param list
	 *            the list
	 * @param source
	 *            the source
	 * @return the i list<? extends I shape>
	 */
	@operator (
			value = { "partially_overlapping" },
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_QUERIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.AGENT_LOCATION })
	@doc (
			value = "A list of agents or geometries among the left-operand list, species or meta-population (addition of species), partially_overlapping the operand (casted as a geometry).",
			examples = { @example (
					value = "[ag1, ag2, ag3] partially_overlapping(self)",
					equals = "the agents among ag1, ag2 and ag3 that partially_overlap the shape of the right-hand argument.",
					isExecutable = false),
					@example (
							value = "(species1 + species2) partially_overlapping (self)",
							equals = "the agents among species species1 and species2 that partially_overlap the shape of the right-hand argument.",
							isExecutable = false) },
			see = { "neighbors_at", "neighbors_of", "closest_to", "overlapping", "agents_overlapping", "inside",
					"agents_inside", "agent_closest_to" })
	@no_test // already done in Spatial tests Models
	public static IList<? extends IShape> partiallyOverlapping(final IScope scope,
			final IContainer<?, ? extends IShape> list, final IShape source) {
		return relatedEntities(scope, list, source, ITopology.SpatialRelation.PARTIALLY_OVERLAP);
	}

	/**
	 * Overlapping.
	 *
	 * @param scope
	 *            the scope
	 * @param list
	 *            the list
	 * @param source
	 *            the source
	 * @return the i list<? extends I shape>
	 */
	@operator (
			value = { "overlapping", "intersecting" },
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_QUERIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.AGENT_LOCATION })
	@doc (
			value = "A list of agents or geometries among the left-operand list, species or meta-population (addition of species), overlapping the operand (casted as a geometry).",
			examples = { @example (
					value = "[ag1, ag2, ag3] overlapping(self)",
					equals = "return the agents among ag1, ag2 and ag3 that overlap the shape of the agent applying the operator.",
					isExecutable = false),
					@example (
							value = "(species1 + species2) overlapping self",
							isExecutable = false) },
			see = { "neighbors_at", "neighbors_of", "agent_closest_to", "agents_inside", "closest_to", "inside",
					"agents_overlapping" })
	@no_test // test already done in Spatial tests models
	public static IList<? extends IShape> overlapping(final IScope scope, final IContainer<?, ? extends IShape> list,
			final IShape source) {
		return relatedEntities(scope, list, source, ITopology.SpatialRelation.OVERLAP);
	}

	/**
	 * Geoms related.
	 *
	 * @param scope
	 *            the scope
	 * @param list
	 *            the list
	 * @param source
	 *            the source
	 * @param relation
	 *            the relation
	 * @return the i list<? extends I shape>
	 */
	public static IList<? extends IShape> geomsRelated(final IScope scope, final IContainer<?, ? extends IShape> list,
			final IShape source, final ITopology.SpatialRelation relation) {
		final IList<IShape> geoms = GamaListFactory.create(Types.GEOMETRY);
		PreparedGeometryFactory pgFact = new PreparedGeometryFactory();
		PreparedGeometry pg = pgFact.create(source.getInnerGeometry());
		for (final Object shape : list.listValue(scope, Types.GEOMETRY, false)) {
			if (!(shape instanceof IShape)) { continue; }
			if (AbstractTopology.accept(pg, ((IShape) shape).getInnerGeometry(), relation)) {
				geoms.add((IShape) shape);
			}
		}
		return geoms;
	}

	/**
	 * Closest to.
	 *
	 * @param scope
	 *            the scope
	 * @param list
	 *            the list
	 * @param source
	 *            the source
	 * @return the i shape
	 */
	@operator (
			value = { "closest_to" },
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_QUERIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.AGENT_LOCATION })
	@doc (
			value = "An agent or a geometry among the left-operand list of agents, species or meta-population (addition of species), the closest to the operand (casted as a geometry).",
			comment = "the distance is computed in the topology of the calling agent (the agent in which this operator is used), with the distance algorithm specific to the topology.",
			examples = { @example (
					value = "[ag1, ag2, ag3] closest_to(self)",
					equals = "return the closest agent among ag1, ag2 and ag3 to the agent applying the operator.",
					isExecutable = false),
					@example (
							value = "(species1 + species2) closest_to self",
							isExecutable = false) },
			see = { "neighbors_at", "neighbors_of", "inside", "overlapping", "agents_overlapping", "agents_inside",
					"agent_closest_to" })
	@no_test // already done in Spatial tests Models
	public static IShape closest_to(final IScope scope, final IContainer<?, ? extends IShape> list,
			final IShape source) {
		if (list == null) return null;
		final IType contentType = list.getGamlType().getContentType();
		if (contentType.isAgentType()) return _closest(scope, In.list(scope, list), source);
		if (list.getGamlType().getContentType().isTranslatableInto(Types.GEOMETRY))
			return geomClostestTo(scope, list, source);
		return null;
	}

	/**
	 * Closest to.
	 *
	 * @param scope
	 *            the scope
	 * @param list
	 *            the list
	 * @param source
	 *            the source
	 * @param number
	 *            the number
	 * @return the i list
	 */
	@operator (
			value = { "closest_to" },
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_QUERIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.AGENT_LOCATION })
	@doc (
			value = "The N agents or geometries among the left-operand list of agents, species or meta-population (addition of species), that are the closest to the operand (casted as a geometry).",
			comment = "the distance is computed in the topology of the calling agent (the agent in which this operator is used), with the distance algorithm specific to the topology.",
			examples = { @example (
					value = "[ag1, ag2, ag3] closest_to(self, 2)",
					equals = "return the 2 closest agents among ag1, ag2 and ag3 to the agent applying the operator.",
					isExecutable = false),
					@example (
							value = "(species1 + species2) closest_to (self, 5)",
							isExecutable = false) },
			see = { "neighbors_at", "neighbors_of", "inside", "overlapping", "agents_overlapping", "agents_inside",
					"agent_closest_to" })
	@no_test // already done in Spatial tests Models
	public static IList<IShape> closest_to(final IScope scope, final IContainer<?, ? extends IShape> list,
			final IShape source, final int number) {
		if (list == null || list.isEmpty(scope)) return GamaListFactory.EMPTY_LIST;
		final IType contentType = list.getGamlType().getContentType();
		if (contentType.isAgentType()) return (IList) _closest(scope, In.list(scope, list), source, number);
		if (list.getGamlType().getContentType().isTranslatableInto(Types.GEOMETRY))
			return geomClostestTo(scope, list, source, number);
		return GamaListFactory.create(contentType);
	}

	/**
	 * Farthest to.
	 *
	 * @param scope
	 *            the scope
	 * @param list
	 *            the list
	 * @param source
	 *            the source
	 * @return the i shape
	 */
	@operator (
			value = { "farthest_to" },
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_QUERIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.AGENT_LOCATION })
	@doc (
			value = "An agent or a geometry among the left-operand list of agents, species or meta-population (addition of species), the farthest to the operand (casted as a geometry).",
			comment = "the distance is computed in the topology of the calling agent (the agent in which this operator is used), with the distance algorithm specific to the topology.",
			examples = { @example (
					value = "[ag1, ag2, ag3] closest_to(self)",
					equals = "return the farthest agent among ag1, ag2 and ag3 to the agent applying the operator.",
					isExecutable = false),
					@example (
							value = "(species1 + species2) closest_to self",
							isExecutable = false) },
			see = { "neighbors_at", "neighbors_of", "neighbors_at", "inside", "overlapping", "agents_overlapping",
					"agents_inside", "agent_closest_to", "closest_to", "agent_farthest_to" })
	@no_test // already done in Spacial tests Models
	public static IShape farthest_to(final IScope scope, final IContainer<?, ? extends IShape> list,
			final IShape source) {
		final IType contentType = list.getGamlType().getContentType();
		if (contentType.isAgentType()) return _farthest(scope, In.list(scope, list), source);
		if (list.getGamlType().getContentType().isTranslatableInto(Types.GEOMETRY))
			return geomFarthestTo(scope, list, source);
		return null;
	}

	/**
	 * Geom clostest to.
	 *
	 * @param scope
	 *            the scope
	 * @param list
	 *            the list
	 * @param source
	 *            the source
	 * @return the i shape
	 */
	public static IShape geomClostestTo(final IScope scope, final IContainer<?, ? extends IShape> list,
			final IShape source) {
		IShape shp = null;
		double distMin = Double.MAX_VALUE;
		for (final Object shape : list.listValue(scope, Types.GEOMETRY, false)) {
			if (!(shape instanceof IShape)) { continue; }
			final double dist = scope.getTopology().distanceBetween(scope, source, (IShape) shape);
			if (dist < distMin) {
				shp = (IShape) shape;
				distMin = dist;
			}
		}
		return shp;
	}

	/**
	 * Geom clostest to.
	 *
	 * @param scope
	 *            the scope
	 * @param list
	 *            the list
	 * @param source
	 *            the source
	 * @param number
	 *            the number
	 * @return the collection
	 */
	public static IList<IShape> geomClostestTo(final IScope scope, final IContainer<?, ? extends IShape> list,
			final IShape source, final int number) {
		final IList<?> objects = list.listValue(scope, Types.GEOMETRY, true);
		objects.removeIf(a -> !(a instanceof IShape));
		final IList<IShape> shapes = (IList<IShape>) objects;
		if (shapes.size() <= number) return shapes;
		scope.getRandom().shuffleInPlace(shapes);
		final Ordering<IShape> ordering = Ordering.natural().onResultOf(input -> source.euclidianDistanceTo(input));
		return GamaListFactory.wrap(Types.GEOMETRY, ordering.leastOf(shapes, number));
	}

	/**
	 * Geom farthest to.
	 *
	 * @param scope
	 *            the scope
	 * @param list
	 *            the list
	 * @param source
	 *            the source
	 * @return the i shape
	 */
	public static IShape geomFarthestTo(final IScope scope, final IContainer<?, ? extends IShape> list,
			final IShape source) {
		IShape shp = null;
		double distMax = Double.MIN_VALUE;
		for (final Object shape : list.listValue(scope, Types.GEOMETRY, false)) {
			if (!(shape instanceof IShape)) { continue; }
			final double dist = scope.getTopology().distanceBetween(scope, source, (IShape) shape);
			if (dist > distMax) {
				shp = (IShape) shape;
				distMax = dist;
			}
		}
		return shp;
	}

	/**
	 * Agent closest to.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @return the i agent
	 */
	@operator (
			value = "agent_closest_to",
			type = IType.AGENT,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_QUERIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.AGENT_LOCATION })
	@doc (
			value = "An agent, the closest to the operand (casted as a geometry).",
			comment = "the distance is computed in the topology of the calling agent (the agent in which this operator is used), with the distance algorithm specific to the topology.",
			examples = { @example (
					value = "agent_closest_to(self)",
					equals = "the closest agent to the agent applying the operator.",
					test = false) },
			see = { "neighbors_at", "neighbors_of", "agents_inside", "agents_overlapping", "closest_to", "inside",
					"overlapping" })
	@no_test // already done in Spatial tests Models
	public static IAgent agent_closest_to(final IScope scope, final Object source) {
		return _closest(scope, Different.with(), source);
	}

	/**
	 * Agent farthest to.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @return the i agent
	 */
	@operator (
			value = "agent_farthest_to",
			type = IType.AGENT,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_QUERIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.AGENT_LOCATION })
	@doc (
			value = "An agent, the farthest to the operand (casted as a geometry).",
			comment = "the distance is computed in the topology of the calling agent (the agent in which this operator is used), with the distance algorithm specific to the topology.",
			examples = { @example (
					value = "agent_farthest_to(self)",
					equals = "the farthest agent to the agent applying the operator.",
					test = false) },
			see = { "neighbors_at", "neighbors_of", "agents_inside", "agents_overlapping", "closest_to", "inside",
					"overlapping", "agent_closest_to", "farthest_to" })
	@no_test (Reason.IMPOSSIBLE_TO_TEST)
	public static IAgent agent_farthest_to(final IScope scope, final Object source) {
		return _farthest(scope, Different.with(), source);
	}

	/**
	 * Agents touching.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @return the i list
	 */
	@operator (
			value = "agents_touching",
			content_type = IType.AGENT,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_QUERIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.AGENT_LOCATION })
	@doc (
			value = "A list of agents touching the operand (casted as a geometry).",
			examples = { @example (
					value = "agents_touching(self)",
					equals = "the agents that touch the shape of the agent applying the operator.",
					test = false) },
			see = { "agent_closest_to", "agents_overlapping", "closest_to", "inside", "overlapping" })
	@no_test // already done in Spacial tests Models
	public static IList<IAgent> agents_touching(final IScope scope, final Object source) {
		return _gather(scope, Different.with(), source, ITopology.SpatialRelation.TOUCH);
	}

	/**
	 * Agents crossing.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @return the i list
	 */
	@operator (
			value = "agents_crossing",
			content_type = IType.AGENT,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_QUERIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.AGENT_LOCATION })
	@doc (
			value = "A list of agents cross the operand (casted as a geometry).",
			examples = { @example (
					value = "agents_crossing(self)",
					equals = "the agents that crossing the shape of the agent applying the operator.",
					test = false) },
			see = { "agent_closest_to", "agents_overlapping", "closest_to", "inside", "overlapping" })
	@no_test // already done in Spacial tests Models
	public static IList<IAgent> agents_crossing(final IScope scope, final Object source) {
		return _gather(scope, Different.with(), source, ITopology.SpatialRelation.CROSS);
	}

	/**
	 * Agents partially overlapping.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @return the i list
	 */
	@operator (
			value = "agents_partially_overlapping",
			content_type = IType.AGENT,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_QUERIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.AGENT_LOCATION })
	@doc (
			value = "A list of agents that partially overlap the operand (casted as a geometry).",
			examples = { @example (
					value = "agents_partially_overlapping(self)",
					equals = "the agents that partially overlap the shape of the agent applying the operator.",
					test = false) },
			see = { "agent_closest_to", "agents_overlapping", "closest_to", "inside", "overlapping" })
	@no_test // already done in Spacial tests Models
	public static IList<IAgent> agents_partially_overlapping(final IScope scope, final Object source) {
		return _gather(scope, Different.with(), source, ITopology.SpatialRelation.PARTIALLY_OVERLAP);
	}

	/**
	 * Agents cover.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @return the i list
	 */
	@operator (
			value = "agents_covering",
			content_type = IType.AGENT,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_QUERIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.AGENT_LOCATION })
	@doc (
			value = "A list of agents covered by the operand (casted as a geometry).",
			examples = { @example (
					value = "agents_covering(self)",
					equals = "the agents that cover the shape of the agent applying the operator.",
					test = false) },
			see = { "agent_closest_to", "agents_overlapping", "closest_to", "inside", "overlapping" })
	@no_test // already done in Spacial tests Models
	public static IList<IAgent> agents_cover(final IScope scope, final Object source) {
		return _gather(scope, Different.with(), source, ITopology.SpatialRelation.COVER);
	}

	/**
	 * Agents inside.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @return the i list
	 */
	@operator (
			value = "agents_inside",
			content_type = IType.AGENT,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_QUERIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.AGENT_LOCATION })
	@doc (
			value = "A list of agents covered by the operand (casted as a geometry).",
			examples = { @example (
					value = "agents_inside(self)",
					equals = "the agents that are covered by the shape of the agent applying the operator.",
					test = false) },
			see = { "agent_closest_to", "agents_overlapping", "closest_to", "inside", "overlapping" })
	@no_test // already done in Spacial tests Models
	public static IList<IAgent> agents_inside(final IScope scope, final Object source) {
		return _gather(scope, Different.with(), source, ITopology.SpatialRelation.INSIDE);
	}

	/**
	 * Agents overlapping.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @return the i list
	 */
	@operator (
			value = { "agents_overlapping", "agent_intersecting" },
			content_type = IType.AGENT,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_QUERIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.AGENT_LOCATION })
	@doc (
			value = "A list of agents overlapping the operand (casted as a geometry).",
			examples = { @example (
					value = "agents_overlapping(self)",
					equals = "the agents that overlap the shape of the agent applying the operator.",
					test = false) },
			see = { "neighbors_at", "neighbors_of", "agent_closest_to", "agents_inside", "closest_to", "inside",
					"overlapping", "at_distance" })
	@no_test // already done in Spatial tests Models
	public static IList<IAgent> agents_overlapping(final IScope scope, final Object source) {
		return _gather(scope, Different.with(), source, ITopology.SpatialRelation.OVERLAP);
	}

	/**
	 * Agents at distance.
	 *
	 * @param scope
	 *            the scope
	 * @param distance
	 *            the distance
	 * @return the i list
	 */
	@operator (
			value = "agents_at_distance",
			content_type = IType.AGENT,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_QUERIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.AGENT_LOCATION })
	@doc (
			value = "A list of agents situated at a distance lower than the right argument.",
			examples = { @example (
					value = "agents_at_distance(20)",
					equals = "all the agents (excluding the caller) which distance to the caller is lower than 20",
					test = false) },
			see = { "neighbors_at", "neighbors_of", "agent_closest_to", "agents_inside", "closest_to", "inside",
					"overlapping", "at_distance" })
	@no_test (Reason.IMPOSSIBLE_TO_TEST)
	public static IList agents_at_distance(final IScope scope, final Double distance) {
		return _neighbors(scope, Different.with(), scope.getAgent(), distance);
	}

	// Support methods used by the different queries

	/**
	 * Gather.
	 *
	 * @param scope
	 *            the scope
	 * @param filter
	 *            the filter
	 * @param source
	 *            the source
	 * @param relation
	 *            the relation
	 * @return the i list
	 */
	private static IList<IAgent> _gather(final IScope scope, final IAgentFilter filter, final Object source,
			final ITopology.SpatialRelation relation) {
		if (filter == null || source == null) return GamaListFactory.EMPTY_LIST;
		final IType type = filter.getSpecies() == null ? Types.AGENT : scope.getType(filter.getSpecies().getName());
		return GamaListFactory.wrap(type,
				scope.getTopology().getAgentsIn(scope, Cast.asGeometry(scope, source, false), filter, relation));
	}

	/**
	 * Closest.
	 *
	 * @param scope
	 *            the scope
	 * @param filter
	 *            the filter
	 * @param source
	 *            the source
	 * @return the i agent
	 */
	private static IAgent _closest(final IScope scope, final IAgentFilter filter, final Object source) {
		if (filter == null || source == null) return null;
		ITopology topology = scope.getTopology();
		if (topology == null) return null;
		return topology.getAgentClosestTo(scope, Cast.asGeometry(scope, source, false), filter);
	}

	/**
	 * Closest.
	 *
	 * @param scope
	 *            the scope
	 * @param filter
	 *            the filter
	 * @param source
	 *            the source
	 * @param number
	 *            the number
	 * @return the collection
	 */
	private static IList<IAgent> _closest(final IScope scope, final IAgentFilter filter, final Object source,
			final int number) {
		if (filter == null || source == null) return null;
		final IType type = filter.getSpecies() == null ? Types.AGENT : scope.getType(filter.getSpecies().getName());
		return GamaListFactory.wrap(type,
				scope.getTopology().getAgentClosestTo(scope, Cast.asGeometry(scope, source, false), filter, number));
	}

	/**
	 * Farthest.
	 *
	 * @param scope
	 *            the scope
	 * @param filter
	 *            the filter
	 * @param source
	 *            the source
	 * @return the i agent
	 */
	private static IAgent _farthest(final IScope scope, final IAgentFilter filter, final Object source) {
		if (filter == null || source == null) return null;
		return scope.getTopology().getAgentFarthestTo(scope, Cast.asGeometry(scope, source, false), filter);
	}

	/**
	 * Neighbors.
	 *
	 * @param scope
	 *            the scope
	 * @param filter
	 *            the filter
	 * @param source
	 *            the source
	 * @param distance
	 *            the distance
	 * @return the i list
	 */
	private static IList<IAgent> _neighbors(final IScope scope, final IAgentFilter filter, final Object source,
			final Object distance) {
		return _neighbors(scope, filter, source, distance, scope.getTopology());
	}

	/**
	 * Neighbors.
	 *
	 * @param scope
	 *            the scope
	 * @param filter
	 *            the filter
	 * @param source
	 *            the source
	 * @param distance
	 *            the distance
	 * @param t
	 *            the t
	 * @return the i list
	 */
	static IList<IAgent> _neighbors(final IScope scope, final IAgentFilter filter, final Object source,
			final Object distance, final ITopology t) {
		if (filter == null || source == null) return GamaListFactory.EMPTY_LIST;
		final IType type = filter.getSpecies() == null ? Types.AGENT : scope.getType(filter.getSpecies().getName());
		return GamaListFactory.wrap(type,
				t.getNeighborsOf(scope, Cast.asGeometry(scope, source, false), Cast.asFloat(scope, distance), filter));
	}

}
