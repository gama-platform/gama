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
import gama.api.types.matrix.IMatrix;
import gama.api.types.misc.IContainer;
import gama.api.types.tree.ITree;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;

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

	/** The root node. */
	private GamaNode<T> root;

	/**
	 * Gets the root element payload.
	 *
	 * @return the root element payload
	 */
	@Override
	public T getRoot() { return this.root != null ? this.root.getData() : null; }

	/**
	 * Gets the internal root node.
	 *
	 * @return root node
	 */
	@Override
	public GamaNode<T> getRootNode() { return this.root; }

	/**
	 * Sets the root node.
	 *
	 * @param root
	 *            the new root node
	 */
	@Override
	public void setRoot(final GamaNode<T> root) { this.root = root; }

	/**
	 * Sets the root payload.
	 *
	 * @param data
	 *            the data for the root
	 */
	@Override
	public GamaNode<T> setRoot(final T data) {
		return setRoot(data, GamaNode.DEFAULT_WEIGHT);
	}

	/**
	 * Sets the root payload with weight.
	 *
	 * @param root
	 *            root payload
	 * @param weight
	 *            weight
	 * @return the gama node
	 */
	public GamaNode<T> setRoot(final T root, final Integer weight) {
		final GamaNode<T> result = new GamaNode(root, weight);
		setRoot(result);
		return result;
	}

	/**
	 * Visits the tree in the order defined.
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
	 */
	public void visitPreOrder(final GamaNode<T> node, final Consumer<GamaNode<T>> visitor) {
		visitor.accept(node);
		for (final GamaNode<T> child : node.getChildren()) { visitPreOrder(child, visitor); }
	}

	/**
	 * Visit post order.
	 */
	public void visitPostOrder(final GamaNode<T> node, final Consumer<GamaNode<T>> visitor) {
		for (final GamaNode<T> child : node.getChildren()) { visitPostOrder(child, visitor); }
		visitor.accept(node);
	}

	/**
	 * List of nodes.
	 */
	public List<GamaNode<T>> list(final Order traversalOrder) {
		if (root == null) return Collections.EMPTY_LIST;
		final List<GamaNode<T>> returnList = new ArrayList<>();
		visit(traversalOrder, returnList::add);
		return returnList;
	}

	@Override
	public IList<T> getNodes(final IScope scope) {
		final IList<T> nodeList = GamaListFactory.create();
		if (root != null) {
			visit(Order.PRE_ORDER, n -> nodeList.add(n.getData()));
		}
		return nodeList;
	}

	@Override
	public IList<T> getLeaves(final IScope scope) {
		final IList<T> leaves = GamaListFactory.create();
		if (root != null) {
			visit(Order.PRE_ORDER, n -> {
				if (!n.hasChildren()) { leaves.add(n.getData()); }
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
	public int getDepthOf(final T element) {
		final GamaNode<T> node = getNodeWithData(element);
		return node != null ? node.getDepth() : 0;
	}

	@Override
	public IList<T> getChildrenOf(final IScope scope, final T element) {
		final GamaNode<T> node = element != null ? getNodeWithData(element) : root;
		if (node == null) return GamaListFactory.create();
		final IList<T> children = GamaListFactory.create();
		for (final GamaNode<T> child : node.getChildren()) {
			children.add(child.getData());
		}
		return children;
	}

	@Override
	public T getParentOf(final T element) {
		final GamaNode<T> node = getNodeWithData(element);
		if (node == null || node.getParent() == null) return null;
		return node.getParent().getData();
	}

	@Override
	public IList<T> getAncestorsOf(final IScope scope, final T element) {
		final IList<T> ancestors = GamaListFactory.create();
		GamaNode<T> node = getNodeWithData(element);
		if (node == null) return ancestors;
		GamaNode<T> curr = node.getParent();
		while (curr != null) {
			ancestors.add(curr.getData());
			curr = curr.getParent();
		}
		return ancestors;
	}

	@Override
	public IList<T> getDescendantsOf(final IScope scope, final T element) {
		final IList<T> descendants = GamaListFactory.create();
		final GamaNode<T> node = element != null ? getNodeWithData(element) : root;
		if (node == null) return descendants;
		for (final GamaNode<T> child : node.getChildren()) {
			collectDescendantPayloads(child, descendants);
		}
		return descendants;
	}

	private void collectDescendantPayloads(final GamaNode<T> node, final IList<T> list) {
		list.add(node.getData());
		for (final GamaNode<T> child : node.getChildren()) {
			collectDescendantPayloads(child, list);
		}
	}

	@Override
	public boolean addChild(final T parent, final T child) {
		if (root == null) {
			setRoot(parent != null ? parent : child);
			if (parent != null && !Objects.equals(parent, child)) {
				root.addChild(child);
			}
			return true;
		}
		final GamaNode<T> parentNode = parent != null ? getNodeWithData(parent) : root;
		if (parentNode != null) {
			parentNode.addChild(child);
			return true;
		}
		return false;
	}

	@Override
	public boolean removeNode(final T element) {
		final GamaNode<T> node = getNodeWithData(element);
		if (node == null) return false;
		if (node.equals(root)) {
			dispose();
		} else {
			node.detach();
		}
		return true;
	}

	@Override
	public boolean isLeaf(final T element) {
		final GamaNode<T> node = getNodeWithData(element);
		return node != null && node.isLeaf();
	}

	@Override
	public boolean isRoot(final T element) {
		final GamaNode<T> node = getNodeWithData(element);
		return node != null && node.isRoot();
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
	 */
	public Map<GamaNode<T>, Integer> mapByDepth(final Order traversalOrder) {
		if (root == null) return Collections.EMPTY_MAP;
		final Map<GamaNode<T>, Integer> returnMap = GamaMapFactory.create();
		if (traversalOrder == Order.PRE_ORDER) {
			mapPreOrderWithDepth(root, returnMap, 0);
		} else if (traversalOrder == Order.POST_ORDER) { mapPostOrderWithDepth(root, returnMap, 0); }
		return returnMap;
	}

	private void mapPreOrderWithDepth(final GamaNode<T> node, final Map<GamaNode<T>, Integer> traversalResult,
			final int depth) {
		traversalResult.put(node, depth);
		for (final GamaNode<T> child : node.getChildren()) { mapPreOrderWithDepth(child, traversalResult, depth + 1); }
	}

	private void mapPostOrderWithDepth(final GamaNode<T> node, final Map<GamaNode<T>, Integer> traversalResult,
			final int depth) {
		for (final GamaNode<T> child : node.getChildren()) { mapPostOrderWithDepth(child, traversalResult, depth + 1); }
		traversalResult.put(node, depth);
	}

	// IValue & IJsonable methods

	@Override
	public String stringValue(final IScope scope) {
		return toString();
	}

	@Override
	public IJsonValue serializeToJson(final IJson json) {
		return json.typedObject(Types.TREE, "root", root != null ? root.getData() : null);
	}

	// IContainer methods

	@Override
	public IContainer<T, T> copy(final IScope scope) throws GamaRuntimeException {
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
		return listValue(scope, contentType, false).matrixValue(scope, contentType, copy);
	}

	@Override
	public IMatrix<?> matrixValue(final IScope scope, final IType<?> contentType, final IPoint size, final boolean copy) {
		return listValue(scope, contentType, false).matrixValue(scope, contentType, size, copy);
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
		return getNodeWithData(o) != null;
	}

	@Override
	public boolean containsKey(final IScope scope, final Object o) throws GamaRuntimeException {
		if (root == null || o == null) return false;
		return getNodeWithData(o) != null;
	}

	@Override
	public T firstValue(final IScope scope) throws GamaRuntimeException {
		return getRoot();
	}

	@Override
	public T lastValue(final IScope scope) throws GamaRuntimeException {
		final IList<T> leaves = getLeaves(scope);
		return leaves.isEmpty() ? null : leaves.get(leaves.size() - 1);
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
		return all.anyValue(scope);
	}

	// ToGet & ToSet

	@Override
	public T get(final IScope scope, final T index) throws GamaRuntimeException {
		final GamaNode<T> node = getNodeWithData(index);
		return node != null ? node.getData() : null;
	}

	@Override
	public T getFromIndicesList(final IScope scope, final IList<T> indices) throws GamaRuntimeException {
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
		if (index != null) {
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
		if (index != null) {
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
		removeNode((T) value);
	}

	@Override
	public void removeIndex(final IScope scope, final Object index) {
		removeNode((T) index);
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
