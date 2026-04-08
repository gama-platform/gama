/*******************************************************************************************************
 *
 * ArrangeDisplayViews.java, in gama.ui.experiment, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.commands;

import static gama.gaml.operators.Displays.HORIZONTAL;
import static gama.gaml.operators.Displays.VERTICAL;
import static org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory.INSTANCE;
import static org.eclipse.e4.ui.workbench.modeling.EModelService.IN_ACTIVE_PERSPECTIVE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

import gama.api.runtime.SystemInfo;
import gama.api.ui.IGamaView;
import gama.api.utils.collections.GamaNode;
import gama.api.utils.collections.GamaTree;
import gama.api.utils.prefs.GamaPreferences;
import gama.core.outputs.LayeredDisplayOutput;
import gama.dev.DEBUG;
import gama.ui.application.workbench.PerspectiveHelper;
import gama.ui.application.workbench.SimulationPerspectiveDescriptor;
import gama.ui.application.workbench.ThemeHelper;
import gama.ui.shared.utils.ViewsHelper;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class ArrangeDisplayViews.
 */
@SuppressWarnings ({ "rawtypes" })
public class ArrangeDisplayViews extends AbstractHandler {

	/**
	 * Gets the part service.
	 *
	 * @return the part service
	 */
	private static EPartService getPartService() { return WorkbenchHelper.getService(EPartService.class); }

	/**
	 * Gets the application.
	 *
	 * @return the application
	 */
	private static MApplication getApplication() { return WorkbenchHelper.getService(MApplication.class); }

	/**
	 * Gets the model service.
	 *
	 * @return the model service
	 */
	private static EModelService getModelService() { return WorkbenchHelper.getService(EModelService.class); }

	static {
		DEBUG.ON();
	}

	/** The Constant LAYOUT_KEY. */
	public static final String LAYOUT_KEY = "gama.displays.layout";

	/** The Constant DISPLAY_INDEX_KEY. */
	static final String DISPLAY_INDEX_KEY = "GamaIndex";

	@Override
	public Object execute(final ExecutionEvent e) {
		execute(GamaPreferences.Displays.LAYOUTS.indexOf(e.getParameter(LAYOUT_KEY)));
		return true;
	}

	/**
	 * Execute.
	 *
	 * <p>
	 * Dispatches to the appropriate overload depending on the runtime type of {@code layout}. When the layout is given
	 * as an {@link Integer} index, the display-placeholder collection is performed <em>once</em> and the resulting list
	 * is passed to {@link LayoutTreeConverter#convert(int, List)} so that the E4 model traversal is not duplicated.
	 * </p>
	 *
	 * @param layout
	 *            an {@link Integer} layout index, a {@link GamaTree} or a {@link GamaNode}; {@code null} is silently
	 *            ignored
	 */
	@SuppressWarnings ("unchecked")
	public static void execute(final Object layout) {
		switch (layout) {
			case Integer i -> {
				// Collect once; pass the list to convert() so it doesn't collect again.
				final List<MPlaceholder> holders = collectAndPrepareDisplayViews();
				execute(LayoutTreeConverter.convert(i, holders), holders);
			}
			case GamaTree t -> execute(t, null);
			case GamaNode n -> {
				final GamaTree<String> tree = LayoutTreeConverter.newLayoutTree();
				n.attachTo(tree.getRoot());
				execute(tree, null);
			}
			case null, default -> {
			}
		}
	}

	/**
	 * Execute.
	 *
	 * <p>
	 * Public entry point that collects the display placeholders before applying the layout described by {@code tree}.
	 * Delegates to {@link #execute(GamaTree, List)}.
	 * </p>
	 *
	 * @param tree
	 *            the layout tree; if {@code null} or root has no children only {@link #decorateDisplays()} is called
	 */
	public static void execute(final GamaTree<String> tree) {
		execute(tree, null);
	}

	/**
	 * Execute.
	 *
	 * <p>
	 * Applies the layout described by {@code tree} using the given pre-collected {@code holders}. If {@code holders} is
	 * {@code null} the method calls {@link #collectAndPrepareDisplayViews()} itself, avoiding a redundant second call
	 * when the caller has already collected them (e.g. the {@link Integer}-layout path in
	 * {@link #execute(Object)}).
	 * </p>
	 *
	 * @param tree
	 *            the layout tree; if {@code null} or the root has no children only {@link #decorateDisplays()} is
	 *            called
	 * @param preCollectedHolders
	 *            the already-collected-and-indexed list of display placeholders, or {@code null} to trigger collection
	 */
	public static void execute(final GamaTree<String> tree, final List<MPlaceholder> preCollectedHolders) {
		final long t0 = System.currentTimeMillis();
		final var window = WorkbenchHelper.getWindow();
		final var shell = window != null ? window.getShell() : null;
		if (shell != null) { shell.setRedraw(false); }
		try {
			final List<MPlaceholder> holders =
					preCollectedHolders != null ? preCollectedHolders : collectAndPrepareDisplayViews();
			DEBUG.OUT("[ArrangeDisplayViews] " + holders.size() + " display(s) to arrange");
			if (tree != null && tree.getRoot().hasChildren()) {
				layoutDisplays(tree, holders);
			} else {
				final MPartStack displayStack = getDisplaysPlaceholder();
				if (displayStack != null) { showDisplays(displayStack.getParent(), holders); }
			}
			DEBUG.OUT("[ArrangeDisplayViews] after show/layout in " + (System.currentTimeMillis() - t0) + "ms");
			// Drain the asyncExec callbacks posted by the E4 renderer during showPart() so that
			// widgets are in their final sash positions before setRedraw(true) fires the repaint.
			// We post a sentinel asyncExec first, then readAndDispatch() until it fires — this
			// bounds the drain to only the callbacks already queued by the renderer, avoiding
			// processing syncExec state-change notifications (fireSourceChanged etc.) that could
			// arrive while looping and attempt to update toolbar items that do not exist yet.
			final var display = WorkbenchHelper.getDisplay();
			if (display != null) {
				final boolean[] sentinel = { false };
				display.asyncExec(() -> sentinel[0] = true);
				int iterations = 0;
				while (!sentinel[0]) { display.readAndDispatch(); iterations++; }
				DEBUG.OUT("[ArrangeDisplayViews] readAndDispatch loop: " + iterations
						+ " iteration(s) in " + (System.currentTimeMillis() - t0) + "ms total");
			}
			// Force an immediate layout pass so that SWT computes all sash weights and sizes
			// the display canvases to their final positions BEFORE setRedraw(true) triggers the
			// repaint. Without this, JOGL canvases (which bypass SWT's paint suppression and
			// render directly to the GL context) may display at the wrong size until the next
			// layout event, producing the "fullscreen → resize" flash the user perceives.
			if (shell != null) { shell.layout(true, true); }
			DEBUG.OUT("[ArrangeDisplayViews] after shell.layout in " + (System.currentTimeMillis() - t0) + "ms");
			decorateDisplays();
			DEBUG.OUT("[ArrangeDisplayViews] after decorateDisplays in " + (System.currentTimeMillis() - t0) + "ms");
		} catch (Exception e) {
			DEBUG.ERR(e);
		} finally {
			if (shell != null) { shell.setRedraw(true); }
		}
		DEBUG.OUT("[ArrangeDisplayViews] TOTAL execute() time=" + (System.currentTimeMillis() - t0) + "ms");
	}

	/**
	 * Layout displays.
	 *
	 * @param tree
	 *            the tree
	 * @param holders
	 *            the holders
	 */
	private static void layoutDisplays(final GamaTree<String> tree, final List<MPlaceholder> holders) {
		GamaNode<String> child = tree.getRoot().getChildren().get(0);
		if (child.getWeight() == null) { child.setWeight(5000); }
		final MPartStack displayStack = getDisplaysPlaceholder();
		if (displayStack == null) return;
		final MElementContainer<?> root = displayStack.getParent();

		// Build the sash subtree WITHOUT attaching it to the live model yet.
		// process() with a null initial root creates containers in memory only;
		// create() returns the new top-level container without calling root.getChildren().add().
		// We capture that top-level container by temporarily wrapping the build.
		final MElementContainer<?> subtree = buildDetachedSubtree(child, holders);
		if (subtree != null) {
			// Single add() — the E4 renderer materialises the complete sash structure
			// in one synchronous call instead of N incremental ones, eliminating the
			// "sash containers appearing one by one" effect that caused the split-screen flash.
			@SuppressWarnings ("unchecked") final MElementContainer raw = root;
			raw.getChildren().add(subtree);
		}
		showDisplays(root, holders);
	}

	/**
	 * Builds the complete E4 sash-container subtree for {@code treeRoot} entirely in memory, without attaching any
	 * node to the live E4 model. Returns the top-level {@link MElementContainer} that should be attached to the
	 * perspective's display area as a single {@code root.getChildren().add()} call.
	 *
	 * <p>
	 * The previous {@link #process(MElementContainer, GamaNode, Map)} approach called
	 * {@code root.getChildren().add(container)} at every recursion level, which fired an {@code EContentAdapter}
	 * notification and caused the E4 renderer to immediately create and show an SWT widget for each intermediate sash
	 * container. The user perceived this as the sash grid being built incrementally before the display views appeared.
	 * By building the full tree off-screen and attaching it in one step, the renderer materialises the complete
	 * structure in a single synchronous pass.
	 * </p>
	 *
	 * @param treeRoot
	 *            the root node of the layout tree
	 * @param holders
	 *            the display placeholders, already indexed
	 * @return the top-level container of the detached subtree, or {@code null} if nothing was built
	 */
	@SuppressWarnings ("unchecked")
	private static MElementContainer<?> buildDetachedSubtree(final GamaNode<String> treeRoot,
			final List<MPlaceholder> holders) {
		final Map<String, MPlaceholder> holdersByIndex = new HashMap<>(holders.size() * 2);
		for (final MPlaceholder h : holders) {
			final Object key = h.getTransientData().get(DISPLAY_INDEX_KEY);
			if (key != null) { holdersByIndex.put(key.toString(), h); }
		}
		// Use a single-element array to capture the top-level container created by the first
		// create() call inside buildNode(). Subsequent create() calls attach to already-detached
		// parents, so they don't touch the live model either.
		final MElementContainer<?>[] top = new MElementContainer<?>[1];
		buildNode(null, treeRoot, holdersByIndex, top);
		return top[0];
	}

	/**
	 * Recursive helper for {@link #buildDetachedSubtree}. Mirrors the old {@link #process} logic but passes
	 * {@code parent} as an already-detached container (or {@code null} for the root call) so that
	 * {@link #create(MElementContainer, String, Boolean)} never touches the live E4 model.
	 */
	@SuppressWarnings ("unchecked")
	private static void buildNode(final MElementContainer<?> parent, final GamaNode<String> treeRoot,
			final Map<String, MPlaceholder> holdersByIndex, final MElementContainer<?>[] topCapture) {
		final String data = treeRoot.getData();
		final String weight = String.valueOf(treeRoot.getWeight());
		final Boolean dir = !HORIZONTAL.equals(data) && !VERTICAL.equals(data) ? null : HORIZONTAL.equals(data);

		final MPlaceholder holder = holdersByIndex.get(data);
		// create() with a null parent builds the container in memory without adding to any live list.
		final MElementContainer container = create(parent, weight, dir);
		// Capture the very first (top-level) container so the caller can attach it in one add().
		if (topCapture[0] == null && container != parent) { topCapture[0] = container; }
		if (holder != null) {
			if (container.equals(parent)) { holder.setContainerData(weight); }
			container.getChildren().add(holder);
		} else {
			for (final GamaNode<String> node : treeRoot.getChildren()) {
				buildNode(container, node, holdersByIndex, topCapture);
			}
		}
	}

	/**
	 * Gets the displays placeholder.
	 *
	 * @return the displays placeholder
	 */
	public static MPartStack getDisplaysPlaceholder() {
		final Object displayStack = getModelService().find("displays", getApplication());
		// DEBUG.OUT("Element displays found : " + displayStack);
		return displayStack instanceof MPartStack m ? m : null;
	}

	/**
	 * Show displays.
	 *
	 * @param root
	 *            the root
	 * @param holders
	 *            the holders
	 */
	private static void showDisplays(final MElementContainer<?> root, final List<MPlaceholder> holders) {
		holders.forEach(ph -> {
			if (ph.getRef() instanceof MPart part) { getPartService().showPart(part, PartState.VISIBLE); }
		});
	}

	/**
	 * Decorate displays.
	 *
	 * <p>
	 * Applies per-display decorations (toolbar visibility, overlay visibility, canvas focus) and sets the sash
	 * background colour for the active simulation perspective. The active perspective is resolved <em>once</em> at the
	 * start; previous code resolved it independently inside each of {@link PerspectiveHelper#keepToolbars()},
	 * {@link PerspectiveHelper#showOverlays()} and {@link PerspectiveHelper#getBackground()}, causing three separate
	 * round-trips through {@link org.eclipse.ui.PlatformUI#getWorkbench()}.
	 * </p>
	 */
	public static void decorateDisplays() {
		// Resolve the active perspective once to avoid repeated PlatformUI lookups.
		final SimulationPerspectiveDescriptor sd = PerspectiveHelper.getActiveSimulationPerspective();
		final Boolean tb = sd != null ? sd.keepToolbars() : null;
		final boolean showOverlays = PerspectiveHelper.showOverlays();

		List<IGamaView.Display> displays = ViewsHelper.getDisplayViews(null);
		displays.forEach(v -> {
			if (tb != null) { v.showToolbar(tb); }
			v.showOverlay(showOverlays);
		});
		if (sd != null && sd.getBackground() != null) {
			ThemeHelper.changeSashBackground(sd.getBackground());
			sd.setRestoreBackground(ThemeHelper::restoreSashBackground);
		}
		// Attempt to solve the problem expressed in #3587 and #667 by forcing the focus
		// on the canvases at least once. Modified to only target 2d displays as it was creating a problem on macOS
		// (perspective not able to go back to modeling and forth)

		if (SystemInfo.isWindows() || SystemInfo.isMac()) {
			displays.forEach(d -> { if (d.is2D()) { d.focusCanvas(); } });
		}

		// If a display is declared fullscreen, enter fullscreen NOW — while the launching overlay still
		// covers the workbench window. The fullscreen shell is built and made visible here, so when the
		// overlay is subsequently removed the user sees the final fullscreen state immediately rather
		// than seeing the normal-size view first and then watching it expand.
		displays.forEach(d -> {
			if (!d.isFullScreen() && d.getOutput() instanceof LayeredDisplayOutput ldo
					&& ldo.getData().fullScreen() > -1) {
				d.toggleFullScreen();
			}
		});

	}

	/**
	 * Process.
	 *
	 * <p>
	 * Recursively builds the E4 part-sash tree for the given layout {@code treeRoot}. The lookup of a placeholder by
	 * its display-index key used to use {@link Iterables#find} (an O(n) linear scan repeated at every level of
	 * recursion, giving O(n²) total cost). It now uses a {@code Map<String,MPlaceholder>} that is built once by the
	 * public entry point {@link #process(MElementContainer, GamaNode, List)} and threaded through the recursive calls,
	 * reducing lookup to O(1).
	 * </p>
	 *
	 * @param uiRoot
	 *            the E4 container that will receive the new child
	 * @param treeRoot
	 *            the current node of the layout tree being processed
	 * @param holdersByIndex
	 *            a map from display-index string to {@link MPlaceholder}, built once from the full holders list
	 */
	private static void process(final MElementContainer uiRoot, final GamaNode<String> treeRoot,
			final Map<String, MPlaceholder> holdersByIndex) {
		final String data = treeRoot.getData();
		final String weight = String.valueOf(treeRoot.getWeight());
		// DEBUG.OUT("Processing " + data + " with weight " + weight);
		final Boolean dir = !HORIZONTAL.equals(data) && !VERTICAL.equals(data) ? null : HORIZONTAL.equals(data);

		final MPlaceholder holder = holdersByIndex.get(data);
		final MElementContainer container = create(uiRoot, weight, dir);
		if (holder != null) {
			if (container.equals(uiRoot)) { holder.setContainerData(weight); }
			container.getChildren().add(holder);
		} else {
			for (final GamaNode<String> node : treeRoot.getChildren()) {
				process(container, node, holdersByIndex);
			}
		}
	}

	/**
	 * Process.
	 *
	 * <p>
	 * Public entry point for the recursive layout processing. Builds a {@code Map<String,MPlaceholder>} from the
	 * {@code holders} list (keyed on each holder's {@value #DISPLAY_INDEX_KEY} transient-data entry) before delegating
	 * to the private recursive overload, avoiding an O(n) list scan at every level.
	 * </p>
	 *
	 * @param uiRoot
	 *            the E4 container that will receive the new child
	 * @param treeRoot
	 *            the current node of the layout tree being processed
	 * @param holders
	 *            the full list of display placeholders with their indices already set
	 */
	public static void process(final MElementContainer uiRoot, final GamaNode<String> treeRoot,
			final List<MPlaceholder> holders) {
		// Build a lookup map once so recursive calls are O(1) instead of O(n) each.
		final Map<String, MPlaceholder> holdersByIndex = new HashMap<>(holders.size() * 2);
		for (final MPlaceholder h : holders) {
			final Object key = h.getTransientData().get(DISPLAY_INDEX_KEY);
			if (key != null) { holdersByIndex.put(key.toString(), h); }
		}
		process(uiRoot, treeRoot, holdersByIndex);
	}

	/**
	 * List display views.
	 *
	 * @return the list
	 */
	static final List<MPlaceholder> collectAndPrepareDisplayViews() {
		final List<MPlaceholder> holders = getModelService().findElements(getApplication(), MPlaceholder.class,
				IN_ACTIVE_PERSPECTIVE, e -> ViewsHelper.isDisplay(e.getElementId()));
		/// Issue #2680
		int currentIndex = 0;
		for (final MPlaceholder h : holders) {
			final IGamaView.Display display = ViewsHelper.findDisplay(h.getElementId());
			if (display != null) {
				display.setIndex(currentIndex);
				h.getTransientData().put(DISPLAY_INDEX_KEY, String.valueOf(currentIndex));
				currentIndex++;
			}
		}
		return holders;
	}

	/**
	 * Creates the.
	 *
	 * @param root
	 *            the root
	 * @param weight
	 *            the weight
	 * @param dir
	 *            the dir
	 * @return the m element container
	 */
	static MElementContainer create(final MElementContainer root, final String weight, final Boolean dir) {
		if (dir == null && root instanceof MPartStack) return root;
		final MElementContainer c;
		if (dir == null) {
			if (!PerspectiveHelper.keepTabs()) {
				c = INSTANCE.createPartSashContainer();
			} else {
				c = INSTANCE.createPartStack();
			}
		} else {
			c = INSTANCE.createPartSashContainer();
			((MPartSashContainer) c).setHorizontal(dir);
		}

		c.setContainerData(weight);
		if (root != null) { root.getChildren().add(c); }
		return c;
	}

}