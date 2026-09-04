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
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;
import gama.api.types.map.IMap;
import gama.api.utils.collections.GamaNode;
import gama.api.utils.collections.GamaTree;

/**
 * Factory for creating and casting ITree instances.
 */
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
	@SuppressWarnings ({ "unchecked", "rawtypes" })
	public static ITree castToTree(final IScope scope, final Object obj, final Object param, final boolean copy) {
		if (obj == null) return create();
		if (obj instanceof ITree tree) {
			return copy ? (ITree) tree.copy(scope) : tree;
		}
		if (obj instanceof GamaNode node) {
			return create(node);
		}
		if (obj instanceof IMap map) {
			final GamaTree tree = create();
			if (!map.isEmpty()) {
				// build tree from map: key = parent, value = children list or child
				for (final Object entryObj : map.entrySet()) {
					final Map.Entry entry = (Map.Entry) entryObj;
					final Object parentObj = entry.getKey();
					final Object childObj = entry.getValue();

					GamaNode parentNode = tree.getNodeWithData(parentObj);
					if (parentNode == null) {
						if (tree.getRoot() == null) {
							parentNode = tree.setRoot(parentObj);
						} else {
							parentNode = tree.getRoot().addChild(parentObj);
						}
					}

					if (childObj instanceof java.lang.Iterable children) {
						for (final Object child : children) {
							parentNode.addChild(child);
						}
					} else if (childObj != null) {
						parentNode.addChild(childObj);
					}
				}
			}
			return tree;
		}
		if (obj instanceof IList list) {
			final GamaTree tree = create();
			if (!list.isEmpty()) {
				GamaNode current = tree.setRoot(list.get(0));
				for (int i = 1; i < list.size(); i++) {
					current = current.addChild(list.get(i));
				}
			}
			return tree;
		}
		// Fallback: single root object
		return create(obj);
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
