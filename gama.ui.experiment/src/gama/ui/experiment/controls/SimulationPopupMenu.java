/*******************************************************************************************************
 *
 * SimulationPopupMenu.java, in gama.ui.experiment, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.controls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import gama.api.GAMA;
import gama.api.compilation.documentation.GamlIdiomsProvider;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.types.color.IColor;
import gama.dev.DEBUG;
import gama.ui.experiment.menus.SimulationsMenu;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class CustomMenu. An alternative to Popup & Regular menus
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 22 août 2023
 */
public class SimulationPopupMenu extends PopupDialog {

	static {
		DEBUG.OFF();
	}

	/** The contents. */
	Composite parent, contents, toolbarComposite;

	/** The toolbar. */
	ToolBar toolbar;

	/** The labels. */
	List<Composite> labels = new CopyOnWriteArrayList<>();

	/** The save. */
	ToolItem add, kill, loadNew, loadAndReplace, duplicate, save, saveHistory;

	/** The provider. */
	private final ExperimentControlContribution status;

	/** The hide. */
	final Listener hide = event -> hide();

	/** Hides the popup when a click occurs outside it. */
	private final Listener dismissOnOutsideClick;

	private Shell observedPopupShell;

	/** Delay (in ms) during which a dismissed popup cannot be reopened by the same click. */
	private static final long REOPEN_DELAY = 300;

	/** The last time the popup was hidden. */
	private volatile long lastHideTime;

	/** Whether rows were added or removed, or a text no longer fits, since the last sizing. */
	private boolean needsResize;

	private static final int SIMULATION_INDENT = 24;

	private record ContextEntry(ITopLevelAgent agent, int depth, int parentIndex) {}

	/**
	 * Sets the text of a label and records whether the popup needs to be enlarged to show it.
	 */
	private void setText(final Label label, final String text) {
		if (text.equals(label.getText())) return;
		label.setText(text);
		if (!needsResize && label.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x > label.getSize().x) {
			needsResize = true;
		}
	}

	/**
	 * Instantiates a new popup 2.
	 *
	 * @param status
	 *            the provider
	 * @param controls
	 *            the controls
	 */
	/*
	 *
	 */
	public SimulationPopupMenu(final ExperimentControlContribution status) {
		super(WorkbenchHelper.getShell(), SWT.NO_TRIM | SWT.NO_FOCUS, false, false, false, false, false, null, null);
		this.status = status;
		dismissOnOutsideClick = event -> {
			if (!(event.widget instanceof Control control)) return;
			final Shell popupShell = getShell();
			if (popupShell != null && !popupShell.isDisposed() && control.getShell() != popupShell
					&& !this.status.isStatusControl(control)) { hide(); }
		};
		final Shell shell = status.getControllingShell();
		shell.addListener(SWT.Move, hide);
		shell.addListener(SWT.Resize, hide);
		shell.addListener(SWT.Close, hide);
		shell.addListener(SWT.Hide, hide);
		final Display display = shell.getDisplay();
		display.addFilter(SWT.Deactivate, hide);
		display.addFilter(SWT.MouseDown, dismissOnOutsideClick);
		shell.addListener(SWT.Dispose, event -> {
			display.removeFilter(SWT.Deactivate, hide);
			display.removeFilter(SWT.MouseDown, dismissOnOutsideClick);
			close();
		});
	}

	/**
	 * Gets the agents to display.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the agents to display
	 * @date 25 août 2023
	 */
	private List<ContextEntry> getAgentsToDisplay() {
		IExperimentSpecies plan = GAMA.getExperiment();
		if (plan == null) return List.of();
		List<ContextEntry> agents = new ArrayList<>();
		addExperiment(plan.getAgent(), 0, -1, agents, Collections.newSetFromMap(new IdentityHashMap<>()));
		return agents;
	}

	private void addExperiment(final IExperimentAgent experiment, final int depth, final int parentIndex,
			final List<ContextEntry> agents, final Set<IExperimentAgent> visited) {
		if (experiment == null || experiment.dead() || experiment.getScope().isClosed() || !visited.add(experiment))
			return;
		final int experimentIndex = agents.size();
		agents.add(new ContextEntry(experiment, depth, parentIndex));
		final var simulations = experiment.getSimulationPopulation();
		if (simulations != null) {
			for (final ISimulationAgent simulation : simulations.toArray(new ISimulationAgent[0])) {
				if (!simulation.dead() && !simulation.getScope().isClosed()) {
					agents.add(new ContextEntry(simulation, depth + 1, experimentIndex));
				}
			}
		}
		for (final var population : experiment.getExternMicroPopulations()) {
			for (final IAgent agent : population.toArray(new IAgent[0])) {
				if (agent instanceof IExperimentAgent controlled) {
					addExperiment(controlled, depth + 1, experimentIndex, agents, visited);
				}
			}
		}
	}

	@Override
	protected Control createContents(final Composite p) {
		if (parent != p || parent.isDisposed()) {
			this.parent = p;
			GridLayoutFactory.swtDefaults().numColumns(1).margins(0, 0).spacing(0, 0).applyTo(parent);
		}
		createToolbar();
		if (contents == null || contents.isDisposed()) {
			this.contents = (Composite) super.createDialogArea(parent);
			GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.CENTER).applyTo(contents);
			GridLayoutFactory.swtDefaults().numColumns(1).margins(5, 5).spacing(0, 5).applyTo(contents);
			contents.addPaintListener(this::paintHierarchy);
		}
		fillLabels(getAgentsToDisplay());
		return contents;
	}

	private void paintHierarchy(final PaintEvent event) {
		if (labels.size() < 2) return;
		event.gc.setForeground(contents.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
		for (int i = 1; i < labels.size(); i++) {
			final Composite row = labels.get(i);
			if (row.isDisposed() || !(row.getData() instanceof ContextEntry entry) || entry.parentIndex() < 0) continue;
			final Composite parentRow = labels.get(entry.parentIndex());
			if (parentRow.isDisposed()) continue;
			final Rectangle parentBounds = parentRow.getBounds();
			final Rectangle iconBounds = parentRow.getChildren()[0].getBounds();
			final int x = parentBounds.x + iconBounds.x + iconBounds.width / 2;
			final Rectangle bounds = row.getBounds();
			final int y = bounds.y + bounds.height / 2;
			event.gc.drawLine(x, parentBounds.y + parentBounds.height, x, y);
			event.gc.drawLine(x, y, bounds.x - 1, y);
		}
	}

	/**
	 * Fill labels.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param agents
	 *            the agents
	 * @date 26 août 2023
	 */
	private void fillLabels(final List<ContextEntry> agents) {
		int size = agents.size();
		// createLabels(size);
		for (int i = 0; i < size; i++) {
			try {
				final ContextEntry entry = agents.get(i);
				ITopLevelAgent agent = entry.agent();
				Composite labelComposite = getOrCreateLabel(i);
				final Label b = (Label) labelComposite.getChildren()[0];
				final Label label = (Label) labelComposite.getChildren()[1];
				b.setImage(GamaIcon.ofColor(agent.getColor()).image());
				labelComposite.setData(entry);
				final var layout = (GridData) labelComposite.getLayoutData();
				final int indent = entry.depth() * SIMULATION_INDENT;
				if (layout.horizontalIndent != indent) {
					layout.horizontalIndent = indent;
					needsResize = true;
				}
				setText(label, GamlIdiomsProvider.toText(status.popupTextFor(agent)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (size < labels.size()) {
			needsResize = true;
			while (labels.size() > size) { labels.remove(labels.size() - 1).dispose(); }
		}
		contents.redraw();
	}

	/**
	 * Gets the or create label.
	 *
	 * @param i
	 *            the i
	 * @return the or create label
	 */
	Composite getOrCreateLabel(final int i) {
		if (i > labels.size() - 1) { labels.add(createLabel()); }
		Composite c = labels.get(i);
		if (c == null || c.isDisposed()) {
			labels.remove(i);
			c = createLabel();
			labels.add(i, c);
		}
		return c;
	}

	/**
	 * Creates the label.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 26 août 2023
	 */
	private Composite createLabel() {
		try {
			final Composite labelComposite = new Composite(contents, SWT.NONE);
			GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).spacing(5, 0).applyTo(labelComposite);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(labelComposite);
			Label labelButton = new Label(labelComposite, SWT.NONE);
			final Label labelText = new Label(labelComposite, SWT.NONE);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, true).applyTo(labelButton);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(labelText);

			labelText.addMouseTrackListener(new MouseTrackListener() {

				Color background, foreground;

				@Override
				public void mouseHover(final MouseEvent e) {}

				@Override
				public void mouseEnter(final MouseEvent e) {
					background = labelText.getBackground();
					foreground = labelText.getForeground();
					IColor c = ((ContextEntry) labelComposite.getData()).agent().getColor();
					Color b = GamaColors.toSwtColor(c);
					GamaColors.setBackAndForeground(b, GamaColors.getTextColorForBackground(b).color(), labelText);
				}

				@Override
				public void mouseExit(final MouseEvent e) {
					GamaColors.setBackAndForeground(background, foreground, labelText);
				}
			});
			labelText.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseDown(final MouseEvent e) {
					hide();
					status.setSelection(((ContextEntry) labelComposite.getData()).agent());
				}

			});
			needsResize = true;
			return labelComposite;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creates the toolbar.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 25 août 2023
	 */
	@SuppressWarnings ("unused")
	private void createToolbar() {
		if (toolbarComposite == null || toolbarComposite.isDisposed()) {
			try {
				toolbarComposite = new Composite(parent, SWT.BORDER);
				RowLayoutFactory.swtDefaults().center(true).fill(true).applyTo(toolbarComposite);
				GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.CENTER).applyTo(toolbarComposite);
				toolbar = new ToolBar(toolbarComposite, SWT.FLAT | SWT.HORIZONTAL);
				RowDataFactory.swtDefaults().exclude(false).applyTo(toolbar);
				add = SimulationsMenu.addNewSimulation.toItem(toolbar);
				kill = SimulationsMenu.killCurrentSimulation.toItem(toolbar);
				duplicate = SimulationsMenu.duplicateCurrentSimulation.toItem(toolbar);
				new ToolItem(toolbar, SWT.SEPARATOR);
				save = SimulationsMenu.saveCurrentSimulation.toItem(toolbar);
				saveHistory = SimulationsMenu.saveCurrentSimulationAndHistory.toItem(toolbar);
				loadAndReplace = SimulationsMenu.replaceCurrentSimulation.toItem(toolbar);
				loadNew = SimulationsMenu.loadNewSimulation.toItem(toolbar);
			} catch (Exception e) {
				toolbarComposite = null;
				toolbar = null;
				return; // will be initialised next time
			}
		}
		// GamaColors.setBackground(provider.getColor(), toolbar, toolbarComposite);
		final ITopLevelAgent selected = GAMA.getCurrentTopLevelAgent();
		boolean isExperiment = selected instanceof IExperimentAgent;
		boolean isSimulation = selected instanceof ISimulationAgent;
		boolean isBackward = isSimulation && selected.getExperiment().getSpecies().isMemorize();
		add.setEnabled(isExperiment || isSimulation);
		kill.setEnabled(isSimulation);
		duplicate.setEnabled(isSimulation);
		save.setEnabled(isSimulation);
		saveHistory.setEnabled(isBackward);
		loadAndReplace.setEnabled(isSimulation);
		loadNew.setEnabled(isExperiment || isSimulation);
	}

	@Override
	protected boolean hasTitleArea() {
		return false;
	}

	@Override
	protected boolean hasInfoArea() {
		return false;
	}

	@Override
	protected void showDialogMenu() {}

	@Override
	protected void setInfoText(final String text) {}

	@Override
	protected void setTitleText(final String text) {}

	@Override
	protected void saveDialogBounds(final Shell shell) {}

	/**
	 * Gets the default location.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the default location
	 * @date 26 août 2023
	 */
	@Override
	protected Point getDefaultLocation(final Point initialSize) {
		final Point location = status.getLocation();
		final Shell shell = getShell();
		final Point size = initialSize == null && shell != null && !shell.isDisposed() ? shell.getSize() : initialSize;
		final Rectangle area = status.getControllingShell().getMonitor().getClientArea();
		if (size != null) {
			location.x = Math.max(area.x, Math.min(location.x, area.x + area.width - size.x));
			location.y = Math.max(area.y, Math.min(location.y, area.y + area.height - size.y));
		}
		return location;
	}

	@Override
	protected Point getDefaultSize() {
		final Point size = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		final Rectangle area = status.getControllingShell().getMonitor().getClientArea();
		size.x = Math.min(Math.max(size.x, status.getWidth()), area.width);
		return size;
	}

	/**
	 * Checks if is visible.
	 *
	 * @return true, if is visible
	 */
	public boolean isVisible() { return getShell() != null && getShell().isVisible(); }

	/**
	 * Display.
	 */
	public void display() {
		final Shell shell = getShell();
		if (shell != null && !shell.isDisposed()) {
			createContents(parent);
			observePopupShell(shell);
			needsResize = false;
			final Point size = getDefaultSize();
			shell.setSize(size);
			shell.setLocation(getDefaultLocation(size));
			shell.setVisible(true);
		} else {
			open();
			observePopupShell(getShell());
		}
	}

	private void observePopupShell(final Shell shell) {
		if (shell == null || shell.isDisposed() || shell == observedPopupShell) return;
		observedPopupShell = shell;
		shell.addListener(SWT.Deactivate, hide);
		shell.addListener(SWT.Dispose, event -> observedPopupShell = null);
	}

	/**
	 * Updates the contents without changing the popup's visibility.
	 */
	public void refresh() {
		final Shell shell = getShell();
		if (shell == null || shell.isDisposed() || !shell.isVisible()) return;
		createContents(parent);
		if (!needsResize) return;
		needsResize = false;
		final Point size = getDefaultSize();
		// Grows only: shorter texts do not shrink the popup while it is visible
		size.x = Math.max(size.x, shell.getSize().x);
		if (!size.equals(shell.getSize())) {
			shell.setSize(size);
		} else {
			shell.layout(true, true);
		}
		final Point location = getDefaultLocation(size);
		if (!location.equals(shell.getLocation())) { shell.setLocation(location); }
	}

	/**
	 * Hide.
	 */
	public void hide() {
		final Shell shell = getShell();
		if (shell != null && !shell.isDisposed() && shell.isVisible()) {
			shell.setVisible(false);
			lastHideTime = System.currentTimeMillis();
		}
	}

	/**
	 * Whether the popup has been hidden within the last few milliseconds. Used to prevent a click on the status control
	 * from reopening the popup it has just dismissed (through a deactivation event delivered before the click).
	 */
	public boolean wasJustHidden() {
		return System.currentTimeMillis() - lastHideTime < REOPEN_DELAY;
	}

	/**
	 * Wipe.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 31 août 2023
	 */
	public void wipe() {
		for (Control c : labels) { if (!c.isDisposed()) { c.setData(null); } }
	}
}
