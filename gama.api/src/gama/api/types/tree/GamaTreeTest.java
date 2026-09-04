/*******************************************************************************************************
 *
 * GamaTreeTest.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import gama.api.gaml.types.Types;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.utils.collections.GamaNode;
import gama.api.utils.collections.GamaTree;

/**
 * Unit tests for GamaTree, ITree, and GamaTreeFactory.
 */
public class GamaTreeTest {

	@Test
	public void testTreeCreationAndContainerMethods() {
		final GamaTree<String> tree = GamaTreeFactory.create("root");
		assertNotNull(tree.getRoot());
		assertEquals("root", tree.getRoot().getData());
		assertEquals(1, tree.length(null));
		assertFalse(tree.isEmpty(null));

		final GamaNode<String> child1 = tree.getRoot().addChild("child1");
		final GamaNode<String> child2 = tree.getRoot().addChild("child2");

		assertEquals(3, tree.length(null));
		assertEquals(2, tree.getLeaves(null).size());

		assertTrue(tree.contains(null, "child1"));
		assertTrue(tree.containsKey(null, child2));

		assertEquals("root", tree.firstValue(null));

		final IList<String> listValues = tree.listValue(null, Types.STRING, false);
		assertEquals(3, listValues.size());
		assertEquals("root", listValues.get(0));
		assertEquals("child1", listValues.get(1));
		assertEquals("child2", listValues.get(2));
	}

	@Test
	public void testTreeFactoryFromMap() {
		final IMap<String, String> map = GamaMapFactory.create();
		map.put("parent", "child1");

		final ITree tree = GamaTreeFactory.castToTree(null, map, null, false);
		assertNotNull(tree.getRoot());
		assertEquals("parent", tree.getRoot().getData());
		assertEquals(2, tree.length(null));
	}

	@Test
	public void testNodeMethods() {
		final GamaNode<String> root = new GamaNode<>("r");
		assertTrue(root.isRoot());
		assertTrue(root.isLeaf());
		assertEquals(0, root.getDepth());

		final GamaNode<String> child = root.addChild("c");
		assertFalse(root.isLeaf());
		assertFalse(child.isRoot());
		assertTrue(child.isLeaf());
		assertEquals(1, child.getDepth());
		assertEquals(root, child.getParent());
	}
}
