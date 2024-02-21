/*******************************************************************************************************
 *
 * UsingStatement.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.statements;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.topology.ITopology;
import gama.core.runtime.IScope;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.types.IType;

/**
 * "using" is a statement that allows to set the topology to use by its
 * sub-statements. They can gather it by asking the scope to provide it.
 * 
 * @author drogoul 19 janv. 13
 */
@symbol(name = IKeyword.USING, kind = ISymbolKind.SEQUENCE_STATEMENT, with_sequence = true, concept = {
		IConcept.TOPOLOGY })
@inside(kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER }, symbols = IKeyword.CHART)
@facets(value = {
		@facet(name = IKeyword.TOPOLOGY, type = IType.TOPOLOGY, optional = false, doc = @doc("the topology")) }, omissible = IKeyword.TOPOLOGY)
@doc(value = "`" + IKeyword.USING
		+ "` is a statement that allows to set the topology to use by its sub-statements. They can gather it by asking the scope to provide it.", usages = {
				@usage(value = "All the spatial operations are topology-dependent (e.g. neighbors are not the same in a continuous and in a grid topology). So `"
						+ IKeyword.USING
						+ "` statement allows modelers to specify the topology in which the spatial operation will be computed.", examples = {
								@example(value = "float dist <- 0.0;", isExecutable = false),
								@example(value = "using topology(grid_ant) {", isExecutable = false),
								@example(value = "	d (self.location distance_to target.location);", isExecutable = false),
								@example(value = "}", isExecutable = false) }) })
public class UsingStatement extends AbstractStatementSequence {

	/** The topology. */
	final IExpression topology;
	
	/** The previous. */
	final ThreadLocal<ITopology> previous = new ThreadLocal<>();

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
	 * When entering the scope, the statement pushes the topology (if not null)
	 * to it and remembers the one that was previously pushed.
	 * 
	 * @see gama.gaml.statements.AbstractStatementSequence#enterScope(gama.core.runtime.IScope)
	 */
	@Override
	public void enterScope(final IScope scope) {
		super.enterScope(scope);
		final ITopology topo = Cast.asTopology(scope, topology.value(scope));
		if (topo != null) {
			previous.set(scope.setTopology(topo));
		}
	}

	/**
	 * When leaving the scope, the statement replaces its topology by the
	 * previous one.
	 * 
	 * @see gama.gaml.statements.AbstractStatementSequence#leaveScope(gama.core.runtime.IScope)
	 */

	@Override
	public void leaveScope(final IScope scope) {
		scope.setTopology(previous.get());
		previous.set(null);
		super.leaveScope(scope);
	}

}