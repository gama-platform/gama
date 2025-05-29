/*******************************************************************************************************
 *
 * GlobalToolbar.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.toolbar;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;

import gama.core.kernel.experiment.IExperimentPlan;
import gama.core.runtime.GAMA;
import gama.core.runtime.IExperimentStateListener;
import gama.dev.DEBUG;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;

/**
 * The Class GlobalToolbar.
 */
public class GlobalToolbar extends PerspectiveAdapter implements IExperimentStateListener {

	/** The Constant INSTANCE. */
	static private final GlobalToolbar INSTANCE = new GlobalToolbar();

	/**
	 * Gets the single instance of GlobalToolbar.
	 *
	 * @return single instance of GlobalToolbar
	 */
	public static GlobalToolbar getInstance() { return INSTANCE; }

	static {
		DEBUG.ON();
	}

	/** The toolbar. */
	GamaToolbarSimple toolbar;

	/** The experiment item. */
	ExperimentItem experimentItem;

	/** The speed item. */
	SpeedItem speedItem;

	/** The item. */
	MemoryItem memoryItem;

	/** The search item. */
	SearchItem searchItem;

	/** The status item. */
	StatusItem statusItem;

	/** The space item. */
	ToolItem spaceItem;

	/** The stop item. */
	StopItem stopItem;

	/** The reload item. */
	ReloadItem reloadItem;

	/** The sync item. */
	SyncItem syncItem;

	/** The step item. */
	StepItem stepItem;

	/** The run item. */
	RunItem runItem;

	/** The back item. */
	BackItem backItem;

	/** The items. */
	final List<GlobalToolbarItem> items = new ArrayList<>();

	/** The perspective item. */
	PerspectiveItem perspectiveItem;

	/** The previous state. */
	State previousState;

	/**
	 * Display on.
	 *
	 * @param parent
	 *            the parent
	 * @return the control
	 */
	private void createOn(final Composite parent) {
		enlarge(parent);
		toolbar = new GamaToolbarSimple(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(toolbar);
		// GamaColors.setBackground(IGamaColors.DARK_ORANGE.color(), toolbar);
		// From left to right
		items.add(perspectiveItem = new PerspectiveItem(toolbar));
		toolbar.space(24);
		items.add(experimentItem = new ExperimentItem(toolbar));
		toolbar.space(24);
		items.add(backItem = new BackItem(toolbar));
		items.add(runItem = new RunItem(toolbar));
		items.add(stepItem = new StepItem(toolbar));
		items.add(speedItem = new SpeedItem(toolbar));
		items.add(syncItem = new SyncItem(toolbar));
		items.add(reloadItem = new ReloadItem(toolbar));
		items.add(stopItem = new StopItem(toolbar));
		spaceItem = toolbar.space(1);
		items.add(statusItem = new StatusItem(toolbar));
		toolbar.space(24);
		items.add(searchItem = new SearchItem(toolbar));
		items.add(memoryItem = new MemoryItem(toolbar));

		parent.requestLayout();
		parent.addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(final ControlEvent e) {
				if (toolbar.isDisposed()) return;
				toolbar.setLayoutDeferred(true);
				try {
					int availableWidth = toolbar.getParent().getSize().x;
					DEBUG.OUT("Available width is " + availableWidth);
					for (GlobalToolbarItem item : items) { availableWidth -= item.getDefaultWidth(); }
					// Remove the spacers
					availableWidth -= 72;
					// Remove the spacing between items + spacers
					availableWidth -= (items.size() + 3) * 5;
					if (availableWidth < 0) {
						int widthToRemove = -availableWidth / 2;
						if (widthToRemove >= 300) { widthToRemove = 300; }
						statusItem.setWidth(statusItem.getDefaultWidth() - widthToRemove);
						experimentItem.setWidth(experimentItem.getDefaultWidth() - widthToRemove);
					} else {
						statusItem.setWidth(statusItem.getDefaultWidth());
						experimentItem.setWidth(experimentItem.getDefaultWidth());
					}
					// for (ToolItem item : toolbar.getItems()) {
					// DEBUG.OUT(item.getToolTipText() + " -- Item width is " + item.getWidth());
					// availableWidth -= item.getWidth() + 20;
					// }
					spaceItem.setWidth(availableWidth);
				} finally {
					toolbar.setLayoutDeferred(false);
					parent.requestLayout();
				}

			}

		});
	}

	/**
	 * Enlarge.
	 *
	 * @param parent
	 *            the parent
	 */
	private void enlarge(final Composite parent) {
		for (Control c : parent.getChildren()) { c.dispose(); }
		// GamaColors.setBackground(IGamaColors.BLUE.color(), parent);
		GridLayoutFactory.fillDefaults().extendedMargins(5, 5, 5, 5).applyTo(parent);
	}

	/**
	 * Install.
	 */
	public static void install() {
		WorkbenchHelper.runInUI("Install GAMA Status and Heap Controls", 0, m -> {
			final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window instanceof WorkbenchWindow ww) {
				final MTrimBar topTrim = ww.getTopTrim();
				for (final MTrimElement element : topTrim.getChildren()) {
					if ("SearchField".equals(element.getElementId())) {
						final Composite parent = ((Control) element.getWidget()).getParent();
						final Control old = (Control) element.getWidget();
						WorkbenchHelper.asyncRun(() -> old.dispose(), 500, () -> true);
						INSTANCE.createOn(parent);
						element.setWidget(INSTANCE.toolbar);
						parent.requestLayout();
						break;
					}
				}
			}
			WorkbenchHelper.getWindow().addPerspectiveListener(INSTANCE);
			GAMA.addExperimentStateListener(INSTANCE);
			INSTANCE.updateStates();
		});
	}

	/**
	 * Update states.
	 */
	void updateStates() {
		WorkbenchHelper.runInUI("Update toolbar", 0, e -> {
			items.forEach(GlobalToolbarItem::update);
			if (!toolbar.isDisposed()) { toolbar.update(); }
		});

	}

	/**
	 * Reninit states.
	 */
	void reinitStates() {
		WorkbenchHelper.runInUI("Update toolbar", 0, e -> {
			items.forEach(GlobalToolbarItem::reinit);
			if (!toolbar.isDisposed()) { toolbar.update(); }
		});

	}

	/**
	 * Perspective activated.
	 *
	 * @param page
	 *            the page
	 * @param perspective
	 *            the perspective
	 */
	@Override
	public void perspectiveActivated(final IWorkbenchPage page, final IPerspectiveDescriptor perspective) {
		DEBUG.OUT("Perspective activated " + perspective.getId());
		updateStates();
	}

	/**
	 * Update state to.
	 *
	 * @param experiment
	 *            the experiment
	 * @param state
	 *            the state
	 */
	@Override
	public void updateStateTo(final IExperimentPlan experiment, final State state) {
		if (state == previousState) return;
		if (previousState == State.NOTREADY) { reinitStates(); }
		previousState = state;
		DEBUG.OUT("Updating the state of the Experiment to " + state);
		updateStates();
	}

}
