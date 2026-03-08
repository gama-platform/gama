/*******************************************************************************************************
 *
 * BenchmarkTree.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.benchmark;

import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IExperimentDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IOperator;
import gama.api.utils.collections.GamaNode;
import gama.api.utils.collections.GamaTree;

/**
 * The Class BenchmarkTree.
 */
public class BenchmarkTree extends GamaTree<IBenchmarkable> {

	/**
	 * Instantiates a new benchmark tree.
	 *
	 * @param model
	 *            the model
	 * @param focusedExperiment
	 *            the focused experiment
	 */
	public BenchmarkTree(final IModelDescription model, final IExperimentDescription focusedExperiment) {
		setRoot(new GamaNode<>(model, 0));
		build(model, focusedExperiment, getRoot(), 1);
	}

	/**
	 * Builds the.
	 *
	 * @param desc
	 *            the desc
	 * @param focusedExperiment
	 *            the focused experiment
	 * @param node
	 *            the node
	 * @param level
	 *            the level
	 */
	private void build(final IDescription desc, final IExperimentDescription focusedExperiment,
			final GamaNode<IBenchmarkable> node, final int level) {
		desc.visitFacets((name, exp) -> {
			final IExpression expr = exp.getExpression();
			if (expr instanceof final IOperator op) {
				final GamaNode<IBenchmarkable> newNode = node.addChild(new GamaNode<>(op, level));
				build(op, newNode, level + 1);
			}
			return true;
		});
		desc.visitOwnChildren(d -> {
			if (d instanceof IExperimentDescription && !d.equals(focusedExperiment)) return true;
			final GamaNode<IBenchmarkable> newNode = node.addChild(new GamaNode<>(d, level));
			build(d, focusedExperiment, newNode, level + 1);
			return true;
		});
	}

	/**
	 * Builds the.
	 *
	 * @param op
	 *            the op
	 * @param currentNode
	 *            the current node
	 * @param level
	 *            the level
	 */
	private void build(final IOperator op, final GamaNode<IBenchmarkable> currentNode, final int level) {
		op.visitSuboperators(o -> {
			final GamaNode<IBenchmarkable> node = currentNode.addChild(new GamaNode<>(o, level));
			build(o, node, level + 1);
		});

	}

}
