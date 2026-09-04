/*******************************************************************************************************
 *
 * ITree.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.tree;

import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;
import gama.api.types.misc.IContainer;
import gama.api.utils.collections.GamaNode;

/**
 * Main interface for tree structures in GAMA.
 *
 * @param <T>
 *            the element type stored in the tree nodes
 */
public interface ITree<T> extends IContainer.Modifiable<GamaNode<T>, T, GamaNode<T>, T>,
		IContainer.Addressable<GamaNode<T>, T, GamaNode<T>, GamaNode<T>> {

	/**
	 * Gets the root node.
	 *
	 * @return the root node
	 */
	GamaNode<T> getRoot();

	/**
	 * Sets the root node.
	 *
	 * @param root
	 *            the new root node
	 */
	void setRoot(GamaNode<T> root);

	/**
	 * Sets the root data.
	 *
	 * @param data
	 *            the data for the root
	 * @return the created root node
	 */
	GamaNode<T> setRoot(T data);

	/**
	 * Gets all nodes in traversal order.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return list of nodes
	 */
	IList<GamaNode<T>> getNodes(IScope scope);

	/**
	 * Gets all leaf nodes.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return list of leaf nodes
	 */
	IList<GamaNode<T>> getLeaves(IScope scope);

	/**
	 * Gets the depth/height of the tree.
	 *
	 * @return the tree depth
	 */
	int getDepth();

	/**
	 * Gets the children of a given node.
	 *
	 * @param scope
	 *            the current execution scope
	 * @param node
	 *            the node
	 * @return list of children nodes
	 */
	IList<GamaNode<T>> getChildrenOf(IScope scope, GamaNode<T> node);

	/**
	 * Gets the parent of a given node.
	 *
	 * @param node
	 *            the node
	 * @return parent node or null if root
	 */
	GamaNode<T> getParentOf(GamaNode<T> node);

	/**
	 * Finds the first node containing the given payload value.
	 *
	 * @param value
	 *            the value
	 * @return the node or null if not found
	 */
	GamaNode<T> getNodeWithData(Object value);
}
