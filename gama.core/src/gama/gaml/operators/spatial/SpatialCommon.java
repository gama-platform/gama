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
 * The class SpatialCommon.
 *
 * @author Alexis Drogoul, Patrick Taillandier, Arnaud Grignard
 * @since 29 nov. 2011
 *
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
			usages = { @usage (
					value = "has no effect if the topology passed as a parameter is nil") },
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
