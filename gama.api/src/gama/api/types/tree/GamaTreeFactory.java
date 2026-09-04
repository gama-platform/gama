/*******************************************************************************************************
 *
 * GamaTreeFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.tree;

import java.util.Map;

import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;
import gama.api.types.map.IMap;
import gama.api.utils.collections.GamaNode;
import gama.api.utils.collections.GamaTree;

/**
 * Factory for creating and casting ITree instances.
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaTreeFactory {

	/**
	 * Creates an empty tree.
	 *
	 * @param <T>
	 *            the node data type
	 * @return new empty GamaTree
	 */
	public static <T> GamaTree<T> create() {
		return new GamaTree<>();
	}

	/**
	 * Creates a tree with root data.
	 *
	 * @param <T>
	 *            the node data type
	 * @param rootData
	 *            the root data
	 * @return new GamaTree with root
	 */
	public static <T> GamaTree<T> create(final T rootData) {
		return GamaTree.withRoot(rootData);
	}

	/**
	 * Creates a tree with root node.
	 *
	 * @param <T>
	 *            the node data type
	 * @param rootNode
	 *            the root node
	 * @return new GamaTree with root node
	 */
	public static <T> GamaTree<T> create(final GamaNode<T> rootNode) {
		return GamaTree.withRoot(rootNode);
	}

	/**
	 * Casts an object to ITree.
	 *
	 * @param scope
	 *            the execution scope
	 * @param obj
	 *            the object to cast
	 * @param param
	 *            optional parameter
	 * @param copy
	 *            whether to copy
	 * @return ITree representation
	 */
	public static ITree castToTree(final IScope scope, final Object obj, final Object param, final boolean copy) {
		if (obj == null) return create();
		if (obj instanceof ITree tree) return copy ? (ITree) tree.copy(scope) : tree;
		if (obj instanceof GamaNode node) return create(node);
		if (obj instanceof IMap map) return createFromMap(map);
		if (obj instanceof IList list) return createFromList(list);
		return create(obj);
	}

	/**
	 * Creates a tree from a map (parent -> child/children).
	 */
	private static ITree createFromMap(final IMap map) {
		final GamaTree tree = create();
		if (map.isEmpty()) return tree;

		for (final Object entryObj : map.entrySet()) {
			final Map.Entry entry = (Map.Entry) entryObj;
			addMapEntryToTree(tree, entry.getKey(), entry.getValue());
		}
		return tree;
	}

	/**
	 * Adds a parent-child map entry to the tree.
	 */
	private static void addMapEntryToTree(final GamaTree tree, final Object parentObj, final Object childObj) {
		final GamaNode parentNode = findOrCreateParentNode(tree, parentObj);
		addChildrenToParent(parentNode, childObj);
	}

	/**
	 * Finds an existing node with parentObj or creates one.
	 */
	private static GamaNode findOrCreateParentNode(final GamaTree tree, final Object parentObj) {
		final GamaNode existingNode = tree.getNodeWithData(parentObj);
		if (existingNode != null) return existingNode;

		if (tree.getRoot() == null) {
			return tree.setRoot(parentObj);
		}
		return tree.getRoot().addChild(parentObj);
	}

	/**
	 * Adds child objects to a parent node.
	 */
	private static void addChildrenToParent(final GamaNode parentNode, final Object childObj) {
		if (childObj == null) return;
		if (childObj instanceof Iterable children) {
			for (final Object child : children) {
				parentNode.addChild(child);
			}
		} else {
			parentNode.addChild(childObj);
		}
	}

	/**
	 * Creates a tree from a list as a chain of nodes.
	 */
	private static ITree createFromList(final IList list) {
		final GamaTree tree = create();
		if (list.isEmpty()) return tree;

		GamaNode current = tree.setRoot(list.get(0));
		for (int i = 1; i < list.size(); i++) {
			current = current.addChild(list.get(i));
		}
		return tree;
	}

	/**
	 * Casts an object to ITree with explicit content type.
	 *
	 * @param scope
	 *            execution scope
	 * @param obj
	 *            object to cast
	 * @param param
	 *            optional param
	 * @param contentType
	 *            type of tree content
	 * @param copy
	 *            whether to copy
	 * @return ITree representation
	 */
	public static ITree castToTree(final IScope scope, final Object obj, final Object param, final IType contentType,
			final boolean copy) {
		return castToTree(scope, obj, param, copy);
	}
}
