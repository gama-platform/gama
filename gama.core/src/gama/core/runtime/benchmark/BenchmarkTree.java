/*******************************************************************************************************
 *
 * BenchmarkTree.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.runtime.benchmark;

import gama.core.common.interfaces.IBenchmarkable;
import gama.core.util.tree.GamaNode;
import gama.core.util.tree.GamaTree;
import gama.gaml.descriptions.ExperimentDescription;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.expressions.operators.IOperator;

/**
 * The Class BenchmarkTree.
 */
public class BenchmarkTree extends GamaTree<IBenchmarkable> {

	/**
	 * Instantiates a new benchmark tree.
	 *
	 * @param model the model
	 * @param focusedExperiment the focused experiment
	 */
	public BenchmarkTree(final IDescription model, final ExperimentDescription focusedExperiment) {
		setRoot(new GamaNode<>(model, 0));
		build(model, focusedExperiment, getRoot(), 1);
	}

	/**
	 * Builds the.
	 *
	 * @param desc the desc
	 * @param focusedExperiment the focused experiment
	 * @param node the node
	 * @param level the level
	 */
	private void build(final IDescription desc, final ExperimentDescription focusedExperiment,
			final GamaNode<IBenchmarkable> node, final int level) {
		desc.visitFacets((name, exp) -> {
			final IExpression expr = exp.getExpression();
			if (expr instanceof IOperator) {
				final IOperator op = (IOperator) expr;
				final GamaNode<IBenchmarkable> newNode = node.addChild(new GamaNode<>(op, level));
				build(op, newNode, level + 1);
			}
			return true;
		});
		desc.visitOwnChildren((d) -> {
			if (d instanceof ExperimentDescription && !d.equals(focusedExperiment)) { return true; }
			final GamaNode<IBenchmarkable> newNode = node.addChild(new GamaNode<>(d, level));
			build(d, focusedExperiment, newNode, level + 1);
			return true;
		});
	}

	/**
	 * Builds the.
	 *
	 * @param op the op
	 * @param currentNode the current node
	 * @param level the level
	 */
	private void build(final IOperator op, final GamaNode<IBenchmarkable> currentNode, final int level) {
		op.visitSuboperators((o) -> {
			final GamaNode<IBenchmarkable> node = currentNode.addChild(new GamaNode<>(o, level));
			build(o, node, level + 1);
		});

	}

}
