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

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.annotations.support.ITypeProvider;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.graph.NodeToAdd;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.tree.GamaTreeFactory;
import gama.api.types.tree.ITree;
import gama.api.utils.collections.GamaNode;

/**
 * GAML operators for tree containers.
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class Trees {

	/**
	 * Tree from root payload.
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
	@test ("tree('root').root = 'root'")
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
	 * @return the root element
	 */
	@operator (
			value = "root_of",
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns the root payload element of the tree.",
			examples = @example (
					value = "root_of(tree(\"a\"))",
					equals = "'a'",
					isExecutable = false))
	@test ("root_of(tree('a')) = 'a'")
	public static Object rootOf(final IScope scope, final ITree tree) {
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
	 * @return the leaf elements
	 */
	@operator (
			value = "leaves_of",
			type = IType.LIST,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns the list of leaf elements of the tree.",
			examples = @example (
					value = "leaves_of(as_tree(['root'::['c1', 'c2']]))",
					equals = "['c1', 'c2']",
					isExecutable = false))
	@test ("length(leaves_of(as_tree(['root'::['c1', 'c2']]))) = 2")
	public static IList leavesOf(final IScope scope, final ITree tree) {
		if (tree == null) return GamaListFactory.create();
		return tree.getLeaves(scope);
	}

	/**
	 * Children of parent element in tree.
	 *
	 * @param scope
	 *            the scope
	 * @param tree
	 *            the tree
	 * @param parent
	 *            parent element
	 * @return list of children
	 */
	@operator (
			value = "children_of",
			type = IType.LIST,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns the list of children elements of the given parent in the tree.",
			examples = @example (
					value = "children_of(as_tree(['p'::['c1', 'c2']]), 'p')",
					equals = "['c1', 'c2']",
					isExecutable = false))
	@test ("length(children_of(as_tree(['p'::['c1', 'c2']]), 'p')) = 2")
	public static IList childrenOf(final IScope scope, final ITree tree, final Object parent) {
		if (tree == null) return GamaListFactory.create();
		return tree.getChildrenOf(scope, parent);
	}

	/**
	 * Parent of child element in tree.
	 *
	 * @param scope
	 *            the scope
	 * @param tree
	 *            the tree
	 * @param child
	 *            child element
	 * @return parent element
	 */
	@operator (
			value = "parent_of",
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns the parent element of the given child element in the tree.",
			examples = @example (
					value = "parent_of(as_tree(['p'::['c1', 'c2']]), 'c1')",
					equals = "'p'",
					isExecutable = false))
	@test ("parent_of(as_tree(['p'::['c1', 'c2']]), 'c1') = 'p'")
	public static Object parentOf(final IScope scope, final ITree tree, final Object child) {
		if (tree == null) return null;
		return tree.getParentOf(child);
	}

	/**
	 * Ancestors of element in tree.
	 *
	 * @param scope
	 *            the scope
	 * @param tree
	 *            the tree
	 * @param element
	 *            the element
	 * @return list of ancestors
	 */
	@operator (
			value = "ancestors_of",
			type = IType.LIST,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns the list of ancestor elements of the given element up to the root.",
			examples = @example (
					value = "ancestors_of(my_tree, leaf_elem)",
					equals = "[parent, root]",
					isExecutable = false))
	@no_test
	public static IList ancestorsOf(final IScope scope, final ITree tree, final Object element) {
		if (tree == null) return GamaListFactory.create();
		return tree.getAncestorsOf(scope, element);
	}

	/**
	 * Descendants of element in tree.
	 *
	 * @param scope
	 *            the scope
	 * @param tree
	 *            the tree
	 * @param element
	 *            the element
	 * @return list of descendants
	 */
	@operator (
			value = "descendants_of",
			type = IType.LIST,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns the list of all descendant elements of the given element in the tree.",
			examples = @example (
					value = "descendants_of(my_tree, root_elem)",
					equals = "all descendant elements",
					isExecutable = false))
	@no_test
	public static IList descendantsOf(final IScope scope, final ITree tree, final Object element) {
		if (tree == null) return GamaListFactory.create();
		return tree.getDescendantsOf(scope, element);
	}

	/**
	 * Add child to parent in tree.
	 *
	 * @param scope
	 *            the scope
	 * @param tree
	 *            the tree
	 * @param parent
	 *            parent element
	 * @param child
	 *            child element
	 * @return the updated tree
	 */
	@operator (
			value = "add_child",
			type = IType.TREE,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "adds a child element to the parent element in the tree.",
			examples = @example (
					value = "add_child(tree('r'), 'r', 'c')",
					equals = "tree with root 'r' and child 'c'",
					isExecutable = false))
	@test ("children_of(add_child(tree('r'), 'r', 'c'), 'r')[0] = 'c'")
	public static ITree addChild(final IScope scope, final ITree tree, final Object parent, final Object child) {
		if (tree != null) {
			final Object parentObj = parent instanceof NodeToAdd nta ? nta.object() : parent;
			final Object childObj = child instanceof NodeToAdd nta ? nta.object() : child;
			tree.addChild(parentObj, childObj);
		}
		return tree;
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
			value = "returns the height (maximum depth) of the tree.",
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
	 * Depth of element in tree.
	 *
	 * @param scope
	 *            the scope
	 * @param tree
	 *            the tree
	 * @param element
	 *            the element
	 * @return int depth
	 */
	@operator (
			value = "depth_of",
			type = IType.INT,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns the depth of the element (distance from the root, root = 0) in the tree.",
			examples = @example (
					value = "depth_of(tree('r'), 'r')",
					equals = "0",
					isExecutable = false))
	@test ("depth_of(tree('r'), 'r') = 0")
	public static int depthOf(final IScope scope, final ITree tree, final Object element) {
		if (tree == null) return 0;
		return tree.getDepthOf(element);
	}

	/**
	 * Is leaf.
	 *
	 * @param scope
	 *            the scope
	 * @param tree
	 *            the tree
	 * @param element
	 *            the element
	 * @return boolean
	 */
	@operator (
			value = "is_leaf",
			type = IType.BOOL,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns true if the element in the tree is a leaf (has no children).",
			examples = @example (
					value = "is_leaf(tree('r'), 'r')",
					equals = "true",
					isExecutable = false))
	@test ("is_leaf(tree('r'), 'r') = true")
	public static boolean isLeaf(final IScope scope, final ITree tree, final Object element) {
		if (tree == null) return false;
		return tree.isLeaf(element);
	}

	/**
	 * Is root.
	 *
	 * @param scope
	 *            the scope
	 * @param tree
	 *            the tree
	 * @param element
	 *            the element
	 * @return boolean
	 */
	@operator (
			value = "is_root",
			type = IType.BOOL,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns true if the element in the tree is the root.",
			examples = @example (
					value = "is_root(tree('r'), 'r')",
					equals = "true",
					isExecutable = false))
	@test ("is_root(tree('r'), 'r') = true")
	public static boolean isRoot(final IScope scope, final ITree tree, final Object element) {
		if (tree == null) return false;
		return tree.isRoot(element);
	}

	/**
	 * Removes node element from tree.
	 *
	 * @param scope
	 *            the scope
	 * @param element
	 *            element to remove
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
			value = "removes the given element and its subtree from the tree.",
			examples = @example (
					value = "remove_node_from('child', my_tree)",
					equals = "the updated tree",
					isExecutable = false))
	@no_test
	public static ITree removeNodeFrom(final IScope scope, final Object element, final ITree tree) {
		if (tree != null && element != null) {
			final Object elemObj = element instanceof NodeToAdd nta ? nta.object() : element;
			tree.removeNode(elemObj);
		}
		return tree;
	}

}
