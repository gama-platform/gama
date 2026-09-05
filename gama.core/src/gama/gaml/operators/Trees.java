/*******************************************************************************************************
 *
 * Trees.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.operators;

import java.util.Collections;
import java.util.List;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.annotations.support.ITypeProvider;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.graph.NodeToAdd;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.tree.GamaTreeFactory;
import gama.api.types.tree.ITree;
import gama.api.utils.collections.GamaNode;

/**
 * GAML operators for tree containers and tree nodes.
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class Trees {

	/**
	 * Tree from root.
	 *
	 * @param scope
	 *            the scope
	 * @param rootData
	 *            the root data
	 * @return the i tree
	 */
	@operator (
			value = "tree",
			type = IType.TREE,
			content_type = ITypeProvider.TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "creates a tree with the given operand as the root payload.",
			examples = @example (
					value = "tree(\"root\")",
					equals = "a tree with root 'root'",
					isExecutable = false))
	@test ("tree('root').root.data = 'root'")
	public static ITree tree(final IScope scope, final Object rootData) {
		if (rootData instanceof GamaNode node) return GamaTreeFactory.create(node);
		if (rootData instanceof NodeToAdd nodeToAdd) return GamaTreeFactory.create(nodeToAdd.object());
		return GamaTreeFactory.create(rootData);
	}

	/**
	 * As tree.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @return the i tree
	 */
	@operator (
			value = "as_tree",
			type = IType.TREE,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER, IConcept.CAST })
	@doc (
			value = "casts a container or object into a tree.",
			examples = @example (
					value = "as_tree(['parent'::['child1', 'child2']])",
					equals = "a tree with root 'parent' and children 'child1', 'child2'",
					isExecutable = false))
	@test ("as_tree(['parent'::['child1', 'child2']]).length = 3")
	public static ITree asTree(final IScope scope, final Object obj) {
		return GamaTreeFactory.castToTree(scope, obj, null, false);
	}

	/**
	 * Root of tree.
	 *
	 * @param scope
	 *            the scope
	 * @param tree
	 *            the tree
	 * @return the root node
	 */
	@operator (
			value = "root_of",
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns the root node of the tree.",
			examples = @example (
					value = "root_of(tree(\"a\"))",
					equals = "node('a')",
					isExecutable = false))
	@test ("root_of(tree('a')).data = 'a'")
	public static GamaNode rootOf(final IScope scope, final ITree tree) {
		if (tree == null) return null;
		return tree.getRoot();
	}

	/**
	 * Leaves of tree.
	 *
	 * @param scope
	 *            the scope
	 * @param tree
	 *            the tree
	 * @return the leaf nodes
	 */
	@operator (
			value = "leaves_of",
			type = IType.LIST,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns the list of leaf nodes of the tree.",
			examples = @example (
					value = "leaves_of(as_tree(['root'::['c1', 'c2']]))",
					equals = "[node('c1'), node('c2')]",
					isExecutable = false))
	@test ("length(leaves_of(as_tree(['root'::['c1', 'c2']]))) = 2")
	public static IList leavesOf(final IScope scope, final ITree tree) {
		if (tree == null) return GamaListFactory.create();
		return tree.getLeaves(scope);
	}

	/**
	 * Children of node.
	 *
	 * @param scope
	 *            the scope
	 * @param target
	 *            the node or tree
	 * @return children list
	 */
	@operator (
			value = "children_of",
			type = IType.LIST,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns the list of children nodes of the given node or tree root.",
			examples = @example (
					value = "children_of(root_of(as_tree(['p'::['c1', 'c2']])))",
					equals = "[node('c1'), node('c2')]",
					isExecutable = false))
	@test ("length(children_of(root_of(as_tree(['p'::['c1', 'c2']])))) = 2")
	public static IList childrenOf(final IScope scope, final Object target) {
		final List<GamaNode> children = getInitialChildren(target);
		if (children.isEmpty()) return GamaListFactory.create();
		return GamaListFactory.wrap(Types.NO_TYPE, children);
	}

	/**
	 * Parent of node.
	 *
	 * @param scope
	 *            the scope
	 * @param node
	 *            the node
	 * @return parent node
	 */
	@operator (
			value = "parent_of",
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns the parent node of the given node.",
			examples = @example (
					value = "parent_of(children_of(tree('r'))[0])",
					equals = "node('r')",
					isExecutable = false))
	@test ("parent_of(root_of(tree('r'))) = nil")
	public static GamaNode parentOf(final IScope scope, final GamaNode node) {
		if (node == null) return null;
		return node.getParent();
	}

	/**
	 * Ancestors of node.
	 *
	 * @param scope
	 *            the scope
	 * @param node
	 *            the node
	 * @return list of ancestors
	 */
	@operator (
			value = "ancestors_of",
			type = IType.LIST,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns the list of ancestor nodes of the given node up to the root.",
			examples = @example (
					value = "ancestors_of(leaf_node)",
					equals = "[parent, root]",
					isExecutable = false))
	@no_test
	public static IList ancestorsOf(final IScope scope, final GamaNode node) {
		final IList ancestors = GamaListFactory.create();
		if (node == null) return ancestors;
		GamaNode curr = node.getParent();
		while (curr != null) {
			ancestors.add(curr);
			curr = curr.getParent();
		}
		return ancestors;
	}

	/**
	 * Descendants of node or tree.
	 *
	 * @param scope
	 *            the scope
	 * @param target
	 *            the node or tree
	 * @return list of descendants
	 */
	@operator (
			value = "descendants_of",
			type = IType.LIST,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns the list of all descendant nodes of the given node or tree.",
			examples = @example (
					value = "descendants_of(root_node)",
					equals = "all descendant nodes",
					isExecutable = false))
	@no_test
	public static IList descendantsOf(final IScope scope, final Object target) {
		final IList descendants = GamaListFactory.create();
		final List<GamaNode> startNodes = getInitialChildren(target);
		for (final GamaNode child : startNodes) {
			collectDescendants(child, descendants);
		}
		return descendants;
	}

	private static List<GamaNode> getInitialChildren(final Object target) {
		if (target instanceof ITree tree) {
			final GamaNode root = tree.getRoot();
			return root != null ? root.getChildren() : Collections.emptyList();
		}
		if (target instanceof GamaNode node) {
			return node.getChildren();
		}
		return Collections.emptyList();
	}

	private static void collectDescendants(final GamaNode node, final IList list) {
		list.add(node);
		for (final GamaNode child : (List<GamaNode>) node.getChildren()) {
			collectDescendants(child, list);
		}
	}

	/**
	 * Add child to parent node.
	 *
	 * @param scope
	 *            the scope
	 * @param parent
	 *            the parent node
	 * @param child
	 *            the child data or node
	 * @return the child node
	 */
	@operator (
			value = "add_child",
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "adds a child (data payload or node) to the parent node.",
			examples = @example (
					value = "add_child(root_node, 'child')",
					equals = "the created child node",
					isExecutable = false))
	@test ("add_child(root_of(tree('r')), 'c').data = 'c'")
	public static GamaNode addChild(final IScope scope, final GamaNode parent, final Object child) {
		if (parent == null) return null;
		if (child instanceof GamaNode childNode) return parent.addChild(childNode);
		if (child instanceof NodeToAdd nodeToAdd) return parent.addChild(nodeToAdd.object());
		return parent.addChild(child);
	}

	/**
	 * Height of tree.
	 *
	 * @param scope
	 *            the scope
	 * @param tree
	 *            the tree
	 * @return int height
	 */
	@operator (
			value = "height_of",
			type = IType.INT,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns the height (max depth) of the tree.",
			examples = @example (
					value = "height_of(tree('r'))",
					equals = "1",
					isExecutable = false))
	@test ("height_of(tree('r')) = 1")
	public static int heightOf(final IScope scope, final ITree tree) {
		if (tree == null) return 0;
		return tree.getDepth();
	}

	/**
	 * Depth of node.
	 *
	 * @param scope
	 *            the scope
	 * @param node
	 *            the node
	 * @return int depth
	 */
	@operator (
			value = "depth_of",
			type = IType.INT,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns the depth of the node (distance from the root, root = 0).",
			examples = @example (
					value = "depth_of(root_of(tree('r')))",
					equals = "0",
					isExecutable = false))
	@test ("depth_of(root_of(tree('r'))) = 0")
	public static int depthOf(final IScope scope, final GamaNode node) {
		if (node == null) return 0;
		return node.getDepth();
	}

	/**
	 * Is leaf.
	 *
	 * @param scope
	 *            the scope
	 * @param node
	 *            the node
	 * @return boolean
	 */
	@operator (
			value = "is_leaf",
			type = IType.BOOL,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns true if the node is a leaf (has no children).",
			examples = @example (
					value = "is_leaf(root_of(tree('r')))",
					equals = "true",
					isExecutable = false))
	@test ("is_leaf(root_of(tree('r'))) = true")
	public static boolean isLeaf(final IScope scope, final GamaNode node) {
		if (node == null) return false;
		return node.isLeaf();
	}

	/**
	 * Is root.
	 *
	 * @param scope
	 *            the scope
	 * @param node
	 *            the node
	 * @return boolean
	 */
	@operator (
			value = "is_root",
			type = IType.BOOL,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns true if the node is the root of a tree (has no parent).",
			examples = @example (
					value = "is_root(root_of(tree('r')))",
					equals = "true",
					isExecutable = false))
	@test ("is_root(root_of(tree('r'))) = true")
	public static boolean isRoot(final IScope scope, final GamaNode node) {
		if (node == null) return false;
		return node.isRoot();
	}

	/**
	 * Removes node from tree.
	 *
	 * @param scope
	 *            the scope
	 * @param node
	 *            the node to remove
	 * @param tree
	 *            the tree
	 * @return the tree
	 */
	@operator (
			value = "remove_node_from",
			type = IType.TREE,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "removes the given node from the tree.",
			examples = @example (
					value = "remove_node_from(child_node, my_tree)",
					equals = "the updated tree",
					isExecutable = false))
	@no_test
	public static ITree removeNodeFrom(final IScope scope, final GamaNode node, final ITree tree) {
		if (tree != null && node != null) {
			tree.removeIndex(scope, node);
		}
		return tree;
	}

}
