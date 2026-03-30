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
		DEBUG.OFF();
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
		final var shell = WorkbenchHelper.getShell();
		if (shell != null) { shell.setRedraw(false); }
		try {
			final List<MPlaceholder> holders =
					preCollectedHolders != null ? preCollectedHolders : collectAndPrepareDisplayViews();
			if (tree != null && tree.getRoot().hasChildren()) {
				layoutDisplays(tree, holders);
			} else {
				final MPartStack displayStack = getDisplaysPlaceholder();
				if (displayStack != null) { showDisplays(displayStack.getParent(), holders); }
			}
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
				while (!sentinel[0]) { display.readAndDispatch(); }
			}
			decorateDisplays();
		} catch (Exception e) {
			DEBUG.ERR(e);
		} finally {
			if (shell != null) { shell.setRedraw(true); }
		}
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
		// DEBUG.LOG("Tree root = " + child.getData() + " weight " +
		// child.getWeight());
		if (child.getWeight() == null) { child.setWeight(5000); }
		final MPartStack displayStack = getDisplaysPlaceholder();
		if (displayStack == null) return;
		final MElementContainer<?> root = displayStack.getParent();
		// displayStack.getChildren().addAll(holders);
		process(root, child, holders);
		showDisplays(root, holders);
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