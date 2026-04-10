package gama.gaml.operators.spatial;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.usage;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.annotations.support.ITypeProvider;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.topology.ITopology;

/**
 * Provides common GAML spatial operators shared across geometry types (union, buffer, convex hull,
 * etc.) and the topology-switching utility operator {@code using}.
 * <p>
 * This class currently hosts:
 * <ul>
 *   <li>{@code using} — evaluates an expression in the context of a given topology, overriding
 *       the default topology of the calling agent for the duration of that evaluation.</li>
 * </ul>
 * <p>
 * Operators that involve agent populations or topology-aware distance computations are annotated
 * with {@code @no_test} because they require an active GAMA simulation. Pure geometric operations
 * (e.g. union, buffer, convex_hull — defined elsewhere) delegate to JTS (Java Topology Suite)
 * and can often be tested with literal geometry values.
 *
 * @author Alexis Drogoul, Patrick Taillandier, Arnaud Grignard
 * @since 29 nov. 2011
 * @see gama.api.types.geometry.IShape
 * @see gama.api.types.topology.ITopology
 */
public class SpatialCommon {

	/**
	 * Using.
	 *
	 * @param scope
	 *            the scope
	 * @param expression
	 *            the expression
	 * @param topology
	 *            the topology
	 * @return the object
	 */
	@operator (
			value = "using",
			category = { IOperatorCategory.SPATIAL },
			concept = { IConcept.TOPOLOGY, IConcept.SPATIAL_COMPUTATION },
			type = ITypeProvider.TYPE_AT_INDEX + 1,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			index_type = ITypeProvider.KEY_TYPE_AT_INDEX + 1)
	@doc (
			value = "Allows to specify in which topology a spatial computation should take place.",
			usages = { @usage ("Has no effect if the topology passed as a parameter is nil; the current topology of the calling agent is used instead."),
					@usage ("The topology change is scoped to the expression evaluation only; the calling agent's own topology is restored immediately after."),
					@usage ("Can be used to force distance computations, neighbor queries, and path computations to use a specific topology (e.g. the world's continuous topology instead of a graph topology).") },
			examples = { @example (
					value = "(agents closest_to self) using topology(world)",
					equals = "the closest agent to self (the caller) in the continuous topology of the world",
					test = false) })
	@no_test // comment="See Topology.experiment in test models"
	public static Object using(final IScope scope, final IExpression expression, final ITopology topology) {
		final ITopology oldTopo = scope.getTopology();
		try {
			if (topology != null) { scope.setTopology(topology); }
			return expression.value(scope);
		} finally {
			scope.setTopology(oldTopo);
		}
	}

}
