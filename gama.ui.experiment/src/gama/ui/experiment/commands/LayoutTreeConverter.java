/*******************************************************************************************************
 *
 * LayoutTreeConverter.java, in gama.ui.shared.experiment, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.commands;

import static gama.annotations.constants.IKeyword.LAYOUT;
import static gama.api.gaml.constants.GamlCoreConstants.horizontal;
import static gama.api.gaml.constants.GamlCoreConstants.none;
import static gama.api.gaml.constants.GamlCoreConstants.split;
import static gama.api.gaml.constants.GamlCoreConstants.stack;
import static gama.api.gaml.constants.GamlCoreConstants.vertical;
import static gama.api.utils.collections.GamaTree.withRoot;
import static gama.gaml.operators.Displays.HORIZONTAL;
import static gama.gaml.operators.Displays.STACK;
import static gama.gaml.operators.Displays.VERTICAL;
import static gama.ui.experiment.commands.ArrangeDisplayViews.DISPLAY_INDEX_KEY;
import static gama.ui.experiment.commands.ArrangeDisplayViews.collectAndPrepareDisplayViews;
import static gama.ui.experiment.commands.ArrangeDisplayViews.getDisplaysPlaceholder;
import static gama.ui.shared.utils.ViewsHelper.getDisplayViews;
import static java.lang.String.valueOf;
import static one.util.streamex.StreamEx.of;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;

import gama.api.ui.IGamaView.Display;
import gama.api.utils.collections.GamaNode;
import gama.api.utils.collections.GamaTree;
import gama.api.utils.prefs.GamaPreferences;
import one.util.streamex.IntStreamEx;

/**
 * The Class LayoutTreeConverter.
 */
public class LayoutTreeConverter {

	/**
	 * Convert.
	 *
	 * <p>
	 * Converts a built-in layout index into the corresponding {@link GamaTree}. This variant reuses an already-built
	 * list of {@link MPlaceholder} holders (obtained by a prior call to
	 * {@link ArrangeDisplayViews#collectAndPrepareDisplayViews()}) so that the expensive E4 model traversal and index
	 * assignment are not repeated.
	 * </p>
	 *
	 * @param layout
	 *            the integer index into {@link GamaPreferences.Displays#LAYOUTS}
	 * @param holders
	 *            the pre-collected and index-annotated list of display placeholders; if {@code null} the method falls
	 *            back to calling {@link ArrangeDisplayViews#collectAndPrepareDisplayViews()} itself
	 * @return the resulting layout tree, or {@code null} if the layout index is out of range
	 */
	public static GamaTree<String> convert(final int layout, final List<MPlaceholder> holders) {
		if (layout < 0 || layout >= GamaPreferences.Displays.LAYOUTS.size()) return null;
		if (holders == null) { collectAndPrepareDisplayViews(); }
		final int[] indices = of(getDisplayViews(null)).mapToInt(Display::getIndex).toArray();
		// Issue #2740 -- proceed anyway with only 1 display
		// if (indices.length <= 1) { return null; }
		Arrays.sort(indices);
		final GamaTree<String> result = newLayoutTree();
		switch (layout) {
			case none:
			case stack:
				return buildStackTree(result, indices);
			case split:
				return buildGridTree(result, indices);
			case horizontal:
			case vertical:
				return buildHorizontalOrVerticalTree(result, indices, layout == horizontal);
			// Issue #3313. Forcing a layout seems to be the solution to the sizing problem of Java2D displays
			// case none:
			// return null;
		}
		return null;
	}

	/**
	 * Convert.
	 *
	 * <p>
	 * Convenience overload that collects the display placeholders itself before converting the layout. Equivalent to
	 * {@code convert(layout, null)}.
	 * </p>
	 *
	 * @param layout
	 *            the integer index into {@link GamaPreferences.Displays#LAYOUTS}
	 * @return the resulting layout tree, or {@code null} if the layout index is out of range
	 */
	public static GamaTree<String> convert(final int layout) {
		return convert(layout, null);
	}

	/**
	 * New layout tree.
	 *
	 * @return the gama tree
	 */
	static GamaTree<String> newLayoutTree() {
		return withRoot(LAYOUT);
	}

	/**
	 * Builds the stack tree.
	 *
	 * @param result
	 *            the result
	 * @param indices
	 *            the indices
	 * @return the gama tree
	 */
	static GamaTree<String> buildStackTree(final GamaTree<String> result, final int[] indices) {
		if (indices.length == 0) return result;
		final GamaNode<String> root = result.getRoot().addChild(STACK);
		IntStreamEx.of(indices).forEach(i -> root.addChild(valueOf(i), 5000));
		return result;
	}

	/**
	 * Builds the grid tree.
	 *
	 * @param result
	 *            the result
	 * @param indices
	 *            the indices
	 * @return the gama tree
	 */
	static GamaTree<String> buildGridTree(final GamaTree<String> result, final int[] indices) {
		if (indices.length == 0) return result;
		final GamaNode<String> initialSash = result.getRoot().addChild(HORIZONTAL);
		final List<GamaNode<String>> placeholders = new ArrayList<>();
		buildPlaceholders(initialSash, placeholders, indices.length);
		int i = 0;
		for (final GamaNode<String> node : placeholders) { node.setData(valueOf(indices[i++])); }
		return result;
	}

	/**
	 * Builds the placeholders.
	 *
	 * @param root
	 *            the root
	 * @param list
	 *            the list
	 * @param size
	 *            the size
	 */
	static void buildPlaceholders(final GamaNode<String> root, final List<GamaNode<String>> list, final int size) {
		if (size == 0) return;
		if (size == 1) {
			list.add(root);
		} else {
			final int half = size / 2;
			final String orientation = HORIZONTAL.equals(root.getData()) ? VERTICAL : HORIZONTAL;
			buildPlaceholders(root.addChild(orientation, 5000), list, half);
			buildPlaceholders(root.addChild(orientation, 5000), list, size - half);
		}
	}

	/**
	 * Builds the horizontal or vertical tree.
	 *
	 * @param result
	 *            the result
	 * @param indices
	 *            the indices
	 * @param horizon
	 *            the horizon
	 * @return the gama tree
	 */
	static GamaTree<String> buildHorizontalOrVerticalTree(final GamaTree<String> result, final int[] indices,
			final boolean horizon) {
		final GamaNode<String> sashNode = result.getRoot().addChild(horizon ? HORIZONTAL : VERTICAL);
		IntStreamEx.of(indices).forEach(i -> sashNode.addChild(valueOf(i), 5000));
		return result;
	}

	/**
	 * Convert current layout.
	 *
	 * <p>
	 * Captures the current E4 part-sash arrangement as a {@link GamaTree} so it can later be restored. The
	 * {@code holders} list is converted to a {@link HashSet} once at entry so that the per-element membership tests in
	 * {@link #save} and {@link #isEmpty} are O(1) rather than O(n).
	 * </p>
	 *
	 * @param holders
	 *            the list of display placeholders whose current arrangement is to be saved
	 * @return the layout tree representing the current arrangement, or {@code null} if the displays placeholder cannot
	 *         be found
	 */
	public static GamaTree<String> convertCurrentLayout(final List<MPlaceholder> holders) {
		final MPartStack displayStack = getDisplaysPlaceholder();
		if (displayStack == null) return null;
		final GamaTree<String> tree = newLayoutTree();
		final Set<MPlaceholder> holderSet = new HashSet<>(holders);
		save(displayStack.getParent(), holderSet, tree.getRoot(), null);
		return tree;
	}

	/**
	 * Gets the weight.
	 *
	 * <p>
	 * Walks up the element's parent chain until a non-{@code null} {@link MUIElement#getContainerData() containerData}
	 * value is found or the root is reached. The previous implementation had a bug where the {@code parent} variable
	 * was never advanced inside the loop, which could result in an infinite loop when both the element and its
	 * immediate parent had {@code null} container data.
	 * </p>
	 *
	 * @param element
	 *            the element whose weight is looked up
	 * @return the first non-{@code null} container-data string found in the element's ancestry, or {@code null} if
	 *         none exists
	 */
	private static String getWeight(final MUIElement element) {
		MUIElement current = element;
		while (current != null) {
			String data = current.getContainerData();
			if (data != null) return data;
			current = current.getParent();
		}
		return null;
	}

	/**
	 * Save.
	 *
	 * @param element
	 *            the element
	 * @param holders
	 *            the set of display placeholders (O(1) membership test)
	 * @param parent
	 *            the parent tree node
	 * @param weight
	 *            the weight
	 */
	private static void save(final MUIElement element, final Set<MPlaceholder> holders, final GamaNode<String> parent,
			final String weight) {
		final String data = weight == null ? getWeight(element) : weight;
		if (element instanceof MPlaceholder && holders.contains(element)) {
			parent.addChild(valueOf(element.getTransientData().get(DISPLAY_INDEX_KEY)), parseInt(data));
		} else if (element instanceof MElementContainer) {
			final MElementContainer<?> container = (MElementContainer<?>) element;
			final List<? extends MUIElement> children = getNonEmptyChildren(container, holders);
			if (children.size() == 0) return;
			if (children.size() == 1) {
				save(children.get(0), holders, parent, data);
			} else {
				final GamaNode<String> node = parent.addChild(prefix(container), parseInt(data));
				children.forEach(e -> save(e, holders, node, null));
			}
		}
	}

	/**
	 * Parses the int.
	 *
	 * @param data
	 *            the data
	 * @return the int
	 */
	static int parseInt(final String data) {
		try {
			return data == null ? 0 : Integer.parseInt(data);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * Prefix.
	 *
	 * @param container
	 *            the container
	 * @return the string
	 */
	private static String prefix(final MElementContainer<?> container) {
		return container instanceof MPartStack ? STACK : container instanceof MPartSashContainer
				? ((MPartSashContainer) container).isHorizontal() ? HORIZONTAL : VERTICAL : "";
	}

	/**
	 * Checks if is empty.
	 *
	 * @param element
	 *            the element
	 * @param holders
	 *            the set of display placeholders (O(1) membership test)
	 * @return true, if is empty
	 */
	private static boolean isEmpty(final MUIElement element, final Set<MPlaceholder> holders) {
		if (element instanceof MElementContainer)
			return of(((MElementContainer<?>) element).getChildren()).allMatch(e -> isEmpty(e, holders));
		return !holders.contains(element);
	}

	/**
	 * Gets the non empty children.
	 *
	 * @param container
	 *            the container
	 * @param holders
	 *            the set of display placeholders (O(1) membership test)
	 * @return the non empty children
	 */
	static List<? extends MUIElement> getNonEmptyChildren(final MElementContainer<? extends MUIElement> container,
			final Set<MPlaceholder> holders) {
		return of(container.getChildren()).filter(e -> !isEmpty(e, holders)).toList();
	}

}
