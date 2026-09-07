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
 * @param <V>
 *            the element payload type stored in the tree
 */
public interface ITree<V> extends IContainer.Modifiable<V, V, V, V>, IContainer.Addressable<V, V, V, V> {

	/**
	 * Gets the root payload element.
	 *
	 * @return the root element
	 */
	V getRoot();

	/**
	 * Gets the internal root node.
	 *
	 * @return the root node
	 */
	GamaNode<V> getRootNode();

	/**
	 * Sets the root element payload.
	 *
	 * @param data
	 *            the data for the root
	 * @return the created root node
	 */
	GamaNode<V> setRoot(V data);

	/**
	 * Sets the internal root node.
	 *
	 * @param root
	 *            the root node
	 */
	void setRoot(GamaNode<V> root);

	/**
	 * Gets all payload elements in pre-order traversal.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return list of elements
	 */
	IList<V> getNodes(IScope scope);

	/**
	 * Gets all leaf elements.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return list of leaf elements
	 */
	IList<V> getLeaves(IScope scope);

	/**
	 * Gets the height (maximum depth) of the tree.
	 *
	 * @return tree depth
	 */
	int getDepth();

	/**
	 * Gets the depth of a specific element (distance from root).
	 *
	 * @param element
	 *            the element
	 * @return depth (0 for root)
	 */
	int getDepthOf(V element);

	/**
	 * Gets children elements of a given element.
	 *
	 * @param scope
	 *            execution scope
	 * @param element
	 *            parent element
	 * @return list of children elements
	 */
	IList<V> getChildrenOf(IScope scope, V element);

	/**
	 * Gets the parent element of a given element.
	 *
	 * @param element
	 *            child element
	 * @return parent element or null
	 */
	V getParentOf(V element);

	/**
	 * Gets ancestor elements of a given element up to the root.
	 *
	 * @param scope
	 *            execution scope
	 * @param element
	 *            element
	 * @return list of ancestors
	 */
	IList<V> getAncestorsOf(IScope scope, V element);

	/**
	 * Gets descendant elements of a given element.
	 *
	 * @param scope
	 *            execution scope
	 * @param element
	 *            element
	 * @return list of descendants
	 */
	IList<V> getDescendantsOf(IScope scope, V element);

	/**
	 * Adds a child element to a parent element in the tree.
	 *
	 * @param parent
	 *            parent element
	 * @param child
	 *            child element
	 * @return true if added
	 */
	boolean addChild(V parent, V child);

	/**
	 * Removes an element and its subtree from the tree.
	 *
	 * @param element
	 *            element to remove
	 * @return true if removed
	 */
	boolean removeNode(V element);

	/**
	 * Checks if an element is a leaf.
	 *
	 * @param element
	 *            element
	 * @return true if leaf
	 */
	boolean isLeaf(V element);

	/**
	 * Checks if an element is the root.
	 *
	 * @param element
	 *            element
	 * @return true if root
	 */
	boolean isRoot(V element);

	/**
	 * Finds the internal node containing the given payload value.
	 *
	 * @param value
	 *            the payload value
	 * @return internal node or null
	 */
	GamaNode<V> getNodeWithData(Object value);
}
