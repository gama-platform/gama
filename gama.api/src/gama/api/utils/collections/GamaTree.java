/*******************************************************************************************************
 *
 * GamaTree.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.collections;

/*
 * Copyright 2010 Vivin Suresh Paliath Distributed under the BSD License
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IContainerType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.types.matrix.GamaMatrixFactory;
import gama.api.types.matrix.IMatrix;
import gama.api.types.misc.IContainer;
import gama.api.types.tree.ITree;
import gama.api.utils.RANDOM;

/**
 * The Class GamaTree.
 *
 * @param <T>
 *            the generic type
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaTree<T> implements ITree<T> {

	/**
	 * With root.
	 *
	 * @param <T>
	 *            the generic type
	 * @param root
	 *            the root
	 * @return the gama tree
	 */
	public static <T> GamaTree<T> withRoot(final GamaNode<T> root) {
		final GamaTree<T> tree = new GamaTree<>();
		tree.setRoot(root);
		return tree;
	}

	/**
	 * With root.
	 *
	 * @param <T>
	 *            the generic type
	 * @param root
	 *            the root
	 * @return the gama tree
	 */
	public static <T> GamaTree<T> withRoot(final T root) {
		final GamaTree<T> tree = new GamaTree<>();
		tree.setRoot(root);
		return tree;
	}

	/**
	 * The Enum Order.
	 */
	public enum Order {

		/** The pre order. */
		PRE_ORDER,
		/** The post order. */
		POST_ORDER
	}

	/** The root. */
	private GamaNode<T> root;

	/**
	 * Gets the root.
	 *
	 * @return the root
	 */
	@Override
	public GamaNode<T> getRoot() { return this.root; }

	/**
	 * Sets the root.
	 *
	 * @param root
	 *            the new root
	 */
	@Override
	public void setRoot(final GamaNode<T> root) { this.root = root; }

	/**
	 * Sets the root.
	 *
	 * @param data
	 *            the new root
	 */
	@Override
	public GamaNode<T> setRoot(final T data) {
		return setRoot(data, GamaNode.DEFAULT_WEIGHT);
	}

	/**
	 * Sets the root.
	 *
	 * @param root
	 *            the root
	 * @param weight
	 *            the weight
	 * @return the gama node
	 */
	public GamaNode<T> setRoot(final T root, final Integer weight) {
		final GamaNode<T> result = new GamaNode(root, weight);
		setRoot(result);
		return result;
	}

	/**
	 * Visits the tree in the order defined. No pruning is done.
	 *
	 * @param traversalOrder
	 * @param visitor
	 */
	public void visit(final Order traversalOrder, final Consumer<GamaNode<T>> visitor) {
		if (root == null) return;
		if (traversalOrder == Order.PRE_ORDER) {
			visitPreOrder(root, visitor);
		} else if (traversalOrder == Order.POST_ORDER) { visitPostOrder(root, visitor); }
	}

	/**
	 * Visit pre order.
	 *
	 * @param node
	 *            the node
	 * @param visitor
	 *            the visitor
	 */
	public void visitPreOrder(final GamaNode<T> node, final Consumer<GamaNode<T>> visitor) {
		visitor.accept(node);
		for (final GamaNode<T> child : node.getChildren()) { visitPreOrder(child, visitor); }
	}

	/**
	 * Visit post order.
	 *
	 * @param node
	 *            the node
	 * @param visitor
	 *            the visitor
	 */
	public void visitPostOrder(final GamaNode<T> node, final Consumer<GamaNode<T>> visitor) {
		for (final GamaNode<T> child : node.getChildren()) { visitPostOrder(child, visitor); }
		visitor.accept(node);
	}

	/**
	 * List.
	 *
	 * @param traversalOrder
	 *            the traversal order
	 * @return the list
	 */
	public List<GamaNode<T>> list(final Order traversalOrder) {
		if (root == null) return Collections.EMPTY_LIST;
		final List<GamaNode<T>> returnList = new ArrayList<>();
		visit(traversalOrder, returnList::add);
		return returnList;
	}

	@Override
	public IList<GamaNode<T>> getNodes(final IScope scope) {
		final IList<GamaNode<T>> nodeList = GamaListFactory.create();
		if (root != null) {
			visit(Order.PRE_ORDER, nodeList::add);
		}
		return nodeList;
	}

	@Override
	public IList<GamaNode<T>> getLeaves(final IScope scope) {
		final IList<GamaNode<T>> leaves = GamaListFactory.create();
		if (root != null) {
			visit(Order.PRE_ORDER, n -> {
				if (!n.hasChildren()) { leaves.add(n); }
			});
		}
		return leaves;
	}

	@Override
	public int getDepth() {
		if (root == null) return 0;
		return getDepth(root);
	}

	private int getDepth(final GamaNode<T> node) {
		if (!node.hasChildren()) return 1;
		int maxChildDepth = 0;
		for (final GamaNode<T> child : node.getChildren()) {
			maxChildDepth = Math.max(maxChildDepth, getDepth(child));
		}
		return 1 + maxChildDepth;
	}

	@Override
	public IList<GamaNode<T>> getChildrenOf(final IScope scope, final GamaNode<T> node) {
		if (node == null) return GamaListFactory.create();
		return GamaListFactory.wrap(Types.NO_TYPE, node.getChildren());
	}

	@Override
	public GamaNode<T> getParentOf(final GamaNode<T> node) {
		if (node == null) return null;
		return node.getParent();
	}

	@Override
	public GamaNode<T> getNodeWithData(final Object value) {
		if (root == null) return null;
		final GamaNode<T>[] result = new GamaNode[1];
		visit(Order.PRE_ORDER, n -> {
			if (result[0] == null && Objects.equals(n.getData(), value)) {
				result[0] = n;
			}
		});
		return result[0];
	}

	/**
	 * Map by depth.
	 *
	 * @param traversalOrder
	 *            the traversal order
	 * @return the map
	 */
	public Map<GamaNode<T>, Integer> mapByDepth(final Order traversalOrder) {
		if (root == null) return Collections.EMPTY_MAP;
		final Map<GamaNode<T>, Integer> returnMap = GamaMapFactory.create();
		if (traversalOrder == Order.PRE_ORDER) {
			mapPreOrderWithDepth(root, returnMap, 0);
		} else if (traversalOrder == Order.POST_ORDER) { mapPostOrderWithDepth(root, returnMap, 0); }
		return returnMap;
	}

	/**
	 * Map pre order with depth.
	 *
	 * @param node
	 *            the node
	 * @param traversalResult
	 *            the traversal result
	 * @param depth
	 *            the depth
	 */
	private void mapPreOrderWithDepth(final GamaNode<T> node, final Map<GamaNode<T>, Integer> traversalResult,
			final int depth) {
		traversalResult.put(node, depth);
		for (final GamaNode<T> child : node.getChildren()) { mapPreOrderWithDepth(child, traversalResult, depth + 1); }
	}

	/**
	 * Map post order with depth.
	 *
	 * @param node
	 *            the node
	 * @param traversalResult
	 *            the traversal result
	 * @param depth
	 *            the depth
	 */
	private void mapPostOrderWithDepth(final GamaNode<T> node, final Map<GamaNode<T>, Integer> traversalResult,
			final int depth) {
		for (final GamaNode<T> child : node.getChildren()) { mapPostOrderWithDepth(child, traversalResult, depth + 1); }
		traversalResult.put(node, depth);
	}

	// IContainer methods

	@Override
	public IContainer<GamaNode<T>, T> copy(final IScope scope) throws GamaRuntimeException {
		final GamaTree<T> newTree = new GamaTree<>();
		if (root != null) {
			newTree.setRoot(copyNode(root));
		}
		return newTree;
	}

	private GamaNode<T> copyNode(final GamaNode<T> orig) {
		final GamaNode<T> copy = new GamaNode<>(orig.getData(), orig.getWeight());
		for (final GamaNode<T> child : orig.getChildren()) {
			copy.addChild(copyNode(child));
		}
		return copy;
	}

	@Override
	public IContainerType<?> getGamlType() {
		return Types.TREE;
	}

	@Override
	public IList<T> listValue(final IScope scope, final IType<?> contentType, final boolean copy) {
		final IList list = GamaListFactory.create(contentType);
		if (root != null) {
			visit(Order.PRE_ORDER, n -> list.add(n.getData()));
		}
		return list;
	}

	@Override
	public IMatrix<?> matrixValue(final IScope scope, final IType<?> contentType, final boolean copy) {
		final IList<T> values = listValue(scope, contentType, false);
		return GamaMatrixFactory.create(scope, values);
	}

	@Override
	public IMatrix<?> matrixValue(final IScope scope, final IType<?> contentType, final IPoint size, final boolean copy) {
		final IList<T> values = listValue(scope, contentType, false);
		return GamaMatrixFactory.create(scope, values, size);
	}

	@Override
	public <D, C> IMap<C, D> mapValue(final IScope scope, final IType<C> keyType, final IType<D> contentType,
			final boolean copy) {
		final IMap map = GamaMapFactory.create(keyType, contentType);
		if (root != null) {
			visit(Order.PRE_ORDER, n -> {
				final IList childrenData = GamaListFactory.create();
				for (final GamaNode<T> child : n.getChildren()) {
					childrenData.add(child.getData());
				}
				map.put(n.getData(), childrenData);
			});
		}
		return map;
	}

	@Override
	public java.lang.Iterable<? extends T> iterable(final IScope scope) {
		return listValue(scope, Types.NO_TYPE, false);
	}

	@Override
	public boolean contains(final IScope scope, final Object o) throws GamaRuntimeException {
		if (root == null || o == null) return false;
		if (o instanceof GamaNode node) {
			return containsKey(scope, node);
		}
		return getNodeWithData(o) != null;
	}

	@Override
	public boolean containsKey(final IScope scope, final Object o) throws GamaRuntimeException {
		if (root == null || !(o instanceof GamaNode node)) return false;
		final boolean[] found = new boolean[1];
		visit(Order.PRE_ORDER, n -> {
			if (n.equals(node)) found[0] = true;
		});
		return found[0];
	}

	@Override
	public T firstValue(final IScope scope) throws GamaRuntimeException {
		return root != null ? root.getData() : null;
	}

	@Override
	public T lastValue(final IScope scope) throws GamaRuntimeException {
		final IList<GamaNode<T>> leaves = getLeaves(scope);
		return leaves.isEmpty() ? null : leaves.get(leaves.size() - 1).getData();
	}

	@Override
	public int length(final IScope scope) {
		if (root == null) return 0;
		final int[] count = new int[1];
		visit(Order.PRE_ORDER, n -> count[0]++);
		return count[0];
	}

	@Override
	public boolean isEmpty(final IScope scope) {
		return root == null;
	}

	@Override
	public IContainer<?, ?> reverse(final IScope scope) throws GamaRuntimeException {
		final GamaTree<T> reversed = (GamaTree<T>) copy(scope);
		if (reversed.root != null) {
			reversed.visit(Order.PRE_ORDER, n -> {
				if (n.hasChildren()) {
					Collections.reverse(n.getChildren());
				}
			});
		}
		return reversed;
	}

	@Override
	public T anyValue(final IScope scope) {
		final IList<T> all = listValue(scope, Types.NO_TYPE, false);
		if (all.isEmpty()) return null;
		return RANDOM.opOneOf(scope, all);
	}

	// ToGet & ToSet

	@Override
	public T get(final IScope scope, final GamaNode<T> index) throws GamaRuntimeException {
		return index != null ? index.getData() : null;
	}

	@Override
	public T getFromIndicesList(final IScope scope, final IList<GamaNode<T>> indices) throws GamaRuntimeException {
		if (indices == null || indices.isEmpty()) return null;
		return get(scope, indices.get(0));
	}

	@Override
	public void addValue(final IScope scope, final T value) {
		if (root == null) {
			setRoot(value);
		} else {
			root.addChild(value);
		}
	}

	@Override
	public void addValueAtIndex(final IScope scope, final Object index, final T value) {
		if (index instanceof GamaNode parentNode) {
			parentNode.addChild(value);
		} else if (index != null) {
			final GamaNode target = getNodeWithData(index);
			if (target != null) {
				target.addChild(value);
			} else {
				addValue(scope, value);
			}
		} else {
			addValue(scope, value);
		}
	}

	@Override
	public void setValueAtIndex(final IScope scope, final Object index, final T value) {
		if (index instanceof GamaNode node) {
			node.setData(value);
		} else if (index != null) {
			final GamaNode target = getNodeWithData(index);
			if (target != null) {
				target.setData(value);
			}
		}
	}

	@Override
	public void addValues(final IScope scope, final Object index, final IContainer<?, ?> values) {
		if (values == null) return;
		for (final Object v : values.iterable(scope)) {
			addValueAtIndex(scope, index, (T) v);
		}
	}

	@Override
	public void setAllValues(final IScope scope, final T value) {
		if (root != null) {
			visit(Order.PRE_ORDER, n -> n.setData(value));
		}
	}

	@Override
	public void removeValue(final IScope scope, final Object value) {
		if (value instanceof GamaNode node) {
			removeIndex(scope, node);
		} else if (value != null) {
			final GamaNode target = getNodeWithData(value);
			if (target != null) {
				removeIndex(scope, target);
			}
		}
	}

	@Override
	public void removeIndex(final IScope scope, final Object index) {
		if (index instanceof GamaNode node) {
			if (node.equals(root)) {
				dispose();
			} else if (node.getParent() != null) {
				node.getParent().getChildren().remove(node);
			}
		}
	}

	@Override
	public void removeIndexes(final IScope scope, final IContainer<?, ?> index) {
		if (index == null) return;
		for (final Object idx : index.iterable(scope)) {
			removeIndex(scope, idx);
		}
	}

	@Override
	public void removeValues(final IScope scope, final IContainer<?, ?> values) {
		if (values == null) return;
		for (final Object v : values.iterable(scope)) {
			removeValue(scope, v);
		}
	}

	@Override
	public void removeAllOccurrencesOfValue(final IScope scope, final Object value) {
		removeValue(scope, value);
	}

	@Override
	public String toString() {
		return root != null ? root.toString() : "nil";
	}

	/**
	 * Dispose.
	 */
	public void dispose() {
		if (root != null) {
			root.dispose();
			root = null;
		}
	}

}
