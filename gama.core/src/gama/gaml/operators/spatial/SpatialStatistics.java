/*******************************************************************************************************
 *
 * SpatialStatistics.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.operators.spatial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.usage;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.annotations.support.ITypeProvider;
import gama.annotations.support.Reason;
import gama.api.data.factories.GamaMapFactory;
import gama.api.data.objects.IContainer;
import gama.api.data.objects.IList;
import gama.api.data.objects.IMap;
import gama.api.data.objects.IMatrix;
import gama.api.data.objects.IPoint;
import gama.api.data.objects.IShape;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.utils.IAgentFilter;
import gama.api.utils.collections.Collector;
import gama.api.utils.geometry.GamaPointFactory;
import gama.api.utils.list.GamaListFactory;
import gama.core.topology.filter.In;
import gama.gaml.operators.Containers;

/**
 * The Class SpatialStatistics.
 */
public class SpatialStatistics {

	/**
	 * KNN from Nguyen Dich Nhat Minh
	 *
	 */
	@operator (
			value = { "k_nearest_neighbors" },
			content_type = ITypeProvider.TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_STATISTICAL, IOperatorCategory.STATISTICAL },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.AGENT_LOCATION, IConcept.STATISTIC })
	@doc (
			value = "This operator allows user to find the attribute of an agent basing on its k-nearest agents",
			comment = """
					In order to use this operator, users have to create a map which map the agents with\
					 one of their attributes (for example color or size,..). In the example below, \
					'map' is the map that I mention above, 'k' is the number of the nearest agents that we are\
					considering""",
			examples = { @example (
					value = "self k_nearest_neighbors (map,k)",
					equals = "this will return the attribute which has highest frequency in the "
							+ "k-nearest neighbors of our agent ",
					isExecutable = false) })
	@no_test
	public static Object KNN(final IScope scope, final IAgent agent, final IMap<IAgent, Object> agents, final int k) {
		/**
		 * Create an inner class named "DistanceCalc" with Comparable interface, we will use this later to compare the
		 * distance between our agent and other agents
		 */
		class DistanceCalc implements Comparable<DistanceCalc> {
			public Object label;
			public Double dist;

			public DistanceCalc(final IAgent a, final IAgent b, final Object label) {
				this.label = label;
				this.dist = scope.getTopology().distanceBetween(scope, a, b);
			}

			@Override
			public int compareTo(final DistanceCalc other) {
				if (this.dist.equals(other.dist)) return 0;
				if (this.dist > other.dist) return 1;
				return -1;
			}
		}

		ArrayList<DistanceCalc> result = new ArrayList<>();
		for (var key : agents.getKeys()) { result.add(new DistanceCalc(agent, key, agents.get(key))); }
		Collections.sort(result);
		// store k nearest neighbors
		ArrayList<Object> K_neighbors = new ArrayList<>();
		for (int i = 0; i < Math.min(k, result.size()); i++) { K_neighbors.add(result.get(i).label); }
		// find most frequent element (majority voting)
		int mostFrequent = 0;
		Object predictedLabel = null;
		for (int i = 0; i < k; i++) {
			int temp = Collections.frequency(K_neighbors, K_neighbors.get(i));
			if (temp > mostFrequent) {
				mostFrequent = temp;
				predictedLabel = K_neighbors.get(i);
			}
		}
		return predictedLabel;
	}

	/**
	 * Simple clustering by distance.
	 *
	 * @param scope
	 *            the scope
	 * @param agents
	 *            the agents
	 * @param distance
	 *            the distance
	 * @return the i list
	 */
	@operator (
			value = { "simple_clustering_by_distance", "simple_clustering_by_envelope_distance" },
			content_type = ITypeProvider.TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_STATISTICAL, IOperatorCategory.STATISTICAL },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.AGENT_LOCATION, IConcept.STATISTIC })
	@doc (
			value = "A list of agent groups clustered by distance considering a distance min between two groups.",
			examples = { @example (
					value = "[ag1, ag2, ag3, ag4, ag5] simpleClusteringByDistance 20.0",
					equals = "for example, can return [[ag1, ag3], [ag2], [ag4, ag5]]",
					isExecutable = false) },
			see = { "hierarchical_clustering" })
	@no_test (Reason.IMPOSSIBLE_TO_TEST)
	public static IList<IList<IAgent>> simpleClusteringByDistance(final IScope scope,
			final IContainer<?, IAgent> agents, final Double distance) {
		final IList<IList<IAgent>> groups =
				GamaListFactory.create(Types.LIST.of(agents.getGamlType().getContentType()));
		final IAgentFilter filter = In.list(scope, agents);
		if (filter == null) return groups;
		try (Collector.AsOrderedSet<IAgent> clusteredCells = Collector.getOrderedSet()) {
			for (final IAgent ag : agents.iterable(scope)) {
				if (!clusteredCells.contains(ag)) {
					groups.add(simpleClusteringByDistanceRec(scope, filter, distance, clusteredCells, ag));
				}
			}
			return groups;
		}
	}

	/**
	 * Simple clustering by distance rec.
	 *
	 * @param scope
	 *            the scope
	 * @param filter
	 *            the filter
	 * @param distance
	 *            the distance
	 * @param clusteredAgs
	 *            the clustered ags
	 * @param currentAg
	 *            the current ag
	 * @return the i list
	 */
	public static IList<IAgent> simpleClusteringByDistanceRec(final IScope scope, final IAgentFilter filter,
			final Double distance, final Collection<IAgent> clusteredAgs, final IAgent currentAg) {
		final IList<IAgent> group = GamaListFactory.create(Types.AGENT);
		final List<IAgent> ags =
				new ArrayList<>(scope.getTopology().getNeighborsOf(scope, currentAg, distance, filter));
		clusteredAgs.add(currentAg);
		group.add(currentAg);
		for (final IAgent ag : ags) {
			if (!clusteredAgs.contains(ag)) {
				group.addAll(simpleClusteringByDistanceRec(scope, filter, distance, clusteredAgs, ag));
			}
		}
		return group;
	}

	/**
	 * Hierarchical clusteringe.
	 *
	 * @param scope
	 *            the scope
	 * @param agents
	 *            the agents
	 * @param distance
	 *            the distance
	 * @return the i list
	 */
	@operator (
			value = { "hierarchical_clustering" },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_STATISTICAL, IOperatorCategory.STATISTICAL },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.AGENT_LOCATION, IConcept.STATISTIC })
	@doc (
			value = "A tree (list of list) contained groups of agents clustered by distance considering a distance min between two groups.",
			comment = "use of hierarchical clustering with Minimum for linkage criterion between two groups of agents.",
			examples = { @example (
					value = "[ag1, ag2, ag3, ag4, ag5] hierarchical_clustering 20.0",
					equals = "for example, can return [[[ag1],[ag3]], [ag2], [[[ag4],[ag5]],[ag6]]",
					isExecutable = false) },
			see = { "simple_clustering_by_distance" })
	@no_test (Reason.IMPOSSIBLE_TO_TEST)
	public static IList hierarchicalClusteringe(final IScope scope, final IContainer<?, IAgent> agents,
			final Double distance) {
		final int nb = agents.length(scope);
		final IList<IList<IAgent>> groups = GamaListFactory.create();

		if (nb == 0) // scope.setStatus(ExecutionStatus.failure);
			return groups;
		double distMin = Double.MAX_VALUE;

		IList<IAgent>[] minFusion = null;

		final Map<IList[], Double> distances = new HashMap<>();
		for (final IAgent ag : agents.iterable(scope)) {
			final IList<IAgent> group = GamaListFactory.create(Types.AGENT);
			group.add(ag);
			groups.add(group);
		}

		if (nb == 1) return groups;
		// BY GEOMETRIES
		for (int i = 0; i < nb - 1; i++) {
			final IList<IAgent> g1 = groups.get(i);
			for (int j = i + 1; j < nb; j++) {
				final IList<IAgent> g2 = groups.get(j);
				final IList<IAgent>[] distGp = new IList[2];
				distGp[0] = g1;
				distGp[1] = g2;
				final IAgent a = g1.get(0);
				final IAgent b = g2.get(0);
				final Double dist = scope.getTopology().distanceBetween(scope, a, b);
				if (dist < distance) {
					distances.put(distGp, dist);
					if (dist < distMin) {
						distMin = dist;
						minFusion = distGp;
					}
				}
			}
		}
		if (minFusion == null) return groups;
		while (distMin <= distance) {

			IList<IList<IAgent>> fusionL = GamaListFactory.create();
			fusionL.add(minFusion[0]);
			fusionL.add(minFusion[1]);
			final IList<IAgent> g1 = fusionL.get(0);
			final IList<IAgent> g2 = fusionL.get(1);
			distances.remove(minFusion);
			fusionL = null;
			groups.remove(g2);
			groups.remove(g1);
			final IList<IList<IAgent>> groupeF = GamaListFactory.create(Types.LIST.of(Types.AGENT));
			groupeF.add(g2);
			groupeF.add(g1);

			for (final IList<IAgent> groupe : groups) {
				final IList[] newDistGp = new IList[2];
				newDistGp[0] = groupe;
				newDistGp[1] = g1;
				double dist1 = Double.MAX_VALUE;
				if (distances.containsKey(newDistGp)) { dist1 = distances.remove(newDistGp); }
				newDistGp[1] = g2;
				double dist2 = Double.MAX_VALUE;
				if (distances.containsKey(newDistGp)) { dist2 = distances.remove(newDistGp); }
				final double dist = Math.min(dist1, dist2);
				if (dist <= distance) {
					newDistGp[1] = groupeF;
					distances.put(newDistGp, dist);
				}

			}
			groups.addAll(groupeF);

			distMin = Double.MAX_VALUE;
			for (final var es : distances.entrySet()) {
				final double dist = es.getValue();
				if (dist < distMin) {
					minFusion = es.getKey();
					distMin = dist;
				}
			}
		}
		return groups;
	}

	/**
	 * Prim IDW.
	 *
	 * @param scope
	 *            the scope
	 * @param geometries
	 *            the geometries
	 * @param points
	 *            the points
	 * @param power
	 *            the power
	 * @return the i map
	 */
	@operator (
			value = { "IDW", "inverse_distance_weighting" },
			category = { IOperatorCategory.SPATIAL },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.STATISTIC })
	@doc (
			value = """
					Inverse Distance Weighting (IDW) is a type of deterministic method for multivariate \
					interpolation with a known scattered set of points. The assigned values to each geometry are calculated with a weighted average of the values available at the known points. See: http://en.wikipedia.org/wiki/Inverse_distance_weighting \
					Usage: IDW (list of geometries, map of points (key: point, value: value), power parameter)""",
			examples = { @example (
					value = "IDW([ag1, ag2, ag3, ag4, ag5],[{10,10}::25.0, {10,80}::10.0, {100,10}::15.0], 2)",
					equals = "for example, can return [ag1::12.0, ag2::23.0,ag3::12.0,ag4::14.0,ag5::17.0]",
					isExecutable = false) })
	@test ("""
			map<point, float> mapLocationPoints <- [{0,0}::10.0,{0,10}::-3.0];\r
					list<point> queryPoint <- [{0,5}];\r
					float((IDW(list(geometry(queryPoint)),mapLocationPoints,1)).pairs[0].value) with_precision 1 = 3.5""")
	public static IMap<IShape, Double> primIDW(final IScope scope, final IContainer<?, ? extends IShape> geometries,
			final IMap points, final int power) {
		final IMap<IShape, Double> results = GamaMapFactory.create(Types.GEOMETRY, Types.FLOAT);
		if (points == null || points.isEmpty()) return null;
		if (geometries == null || geometries.isEmpty(scope)) return results;
		for (final IShape geom : geometries.iterable(scope)) {
			double sum = 0;
			double weight = 0;
			double sumNull = 0;
			int nbNull = 0;
			for (final Object obj : points.keySet()) {
				final IPoint pt = GamaPointFactory.toPoint(scope, obj);
				final double dist = scope.getTopology().distanceBetween(scope, geom, pt);
				if (dist == 0) {
					nbNull++;
					sumNull += Cast.asFloat(scope, points.get(pt));
				}
				if (nbNull == 0) {
					final double w = 1 / Math.pow(dist, power);
					weight += w;
					sum += w * Cast.asFloat(scope, points.get(pt));
				}
			}
			if (nbNull > 0) {
				results.put(geom, sumNull / nbNull);
			} else {
				results.put(geom, sum / weight);
			}

		}
		return results;
	}

	/**
	 * Moran index.
	 *
	 * @param scope
	 *            the scope
	 * @param vals
	 *            the vals
	 * @param mat
	 *            the mat
	 * @return the double
	 */
	@operator (
			value = "moran",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.STATISTICAL },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION })
	@doc (
			usages = { @usage (
					value = "return the Moran Index of the given list of interest points (list of floats) and the weight matrix (matrix of float)",
					examples = { @example (
							value = "moran([1.0, 0.5, 2.0], weight_matrix)",
							equals = "the Moran index is computed",
							test = false,
							isExecutable = false) }) })
	@no_test (Reason.IMPOSSIBLE_TO_TEST)
	public static double moranIndex(final IScope scope, final IList<Double> vals, final IMatrix<Double> weightMatrix) {
		if (weightMatrix == null || weightMatrix.getCols(scope) != weightMatrix.getRows(scope))
			throw GamaRuntimeException.error("A squared weight matrix should be given for the moran index computation",
					scope);
		final int N = vals.size();
		if (N != weightMatrix.getRows(scope)) throw GamaRuntimeException
				.error("The lengths of the value list and of the weight matrix do not match", scope);
		double I = 0.0;
		double sumWeights = 0.0;
		double sumXi = 0;
		final Double mean = (Double) Containers.opMean(scope, vals);
		for (int i = 0; i < N; i++) {
			final double xi = vals.get(i);
			final double xiDev = xi - mean;
			sumXi += Math.pow(xiDev, 2);
			for (int j = 0; j < N; j++) {
				final Double weight = weightMatrix.get(scope, i, j);
				sumWeights += weight;
				I += weight * xiDev * (vals.get(j) - mean);
			}
		}
		I /= sumXi;
		I *= N / sumWeights;
		return I;
	}
}
