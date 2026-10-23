/*******************************************************************************************************
 *
 * UsingStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.usage;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.compilation.descriptions.IDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.topology.GamaTopologyFactory;
import gama.api.types.topology.ITopology;

/**
 * "using" is a statement that allows to set the topology to use by its sub-statements. They can gather it by asking the
 * scope to provide it.
 *
 * @author drogoul 19 janv. 13
 */
@symbol (
		name = IKeyword.USING,
		kind = ISymbolKind.SEQUENCE_STATEMENT,
		with_sequence = true,
		concept = { IConcept.TOPOLOGY })
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER },
		symbols = IKeyword.CHART)
@facets (
		value = { @facet (
				name = IKeyword.TOPOLOGY,
				type = IType.TOPOLOGY,
				optional = false,
				doc = @doc ("the topology")) },
		omissible = IKeyword.TOPOLOGY)
@doc (
		value = "`" + IKeyword.USING
				+ "` is a statement that allows to set the topology to use by its sub-statements. They can gather it by asking the scope to provide it.",
		usages = { @usage (
				value = "All the spatial operations are topology-dependent (e.g. neighbors are not the same in a continuous and in a grid topology). So `"
						+ IKeyword.USING
						+ "` statement allows modelers to specify the topology in which the spatial operation will be computed.",
				examples = { @example (
						value = "float dist <- 0.0;",
						isExecutable = false),
						@example (
								value = "using topology(grid_ant) {",
								isExecutable = false),
						@example (
								value = "	d (self.location distance_to target.location);",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false) }) })
public class UsingStatement extends AbstractStatementSequence {

	/** The topology. */
	final IExpression topology;

	/**
	 * Constructor.
	 *
	 * @param desc,
	 *            the description of the statement.
	 */
	public UsingStatement(final IDescription desc) {
		super(desc);
		topology = getFacet(IKeyword.TOPOLOGY);
		setName("using " + topology.serializeToGaml(false));
	}

	/**
	 * Executes the sub-statements with the topology (if not null) and restores the previous one afterwards. The previous
	 * topology is kept by the call itself, as the statement is shared by all the simulations and threads executing it
	 * (and can be executed recursively).
	 */
	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		final ITopology topo = GamaTopologyFactory.castToTopology(scope, topology.value(scope));
		if (topo == null) return super.executeOn(scope);
		final ITopology previous = scope.setTopology(topo);
		try {
			return super.executeOn(scope);
		} finally {
			scope.setTopology(previous);
		}
	}

}