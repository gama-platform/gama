/*******************************************************************************************************
 *
 * SimulationPopupMenu.java, in gama.ui.shared.experiment, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.controls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import gama.core.kernel.experiment.IExperimentAgent;
import gama.core.kernel.experiment.IExperimentPlan;
import gama.core.kernel.experiment.ITopLevelAgent;
import gama.core.kernel.simulation.SimulationAgent;
import gama.core.kernel.simulation.SimulationPopulation;
import gama.core.runtime.GAMA;
import gama.core.util.GamaColor;
import gama.dev.DEBUG;
import gama.gaml.compilation.GamlIdiomsProvider;
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
		DEBUG.ON();
	}

	/** The contents. */
	Composite parent, contents, toolbarComposite;

	/** The toolbar. */
	ToolBar toolbar;

	/** The labels. */
	List<Composite> labels = new CopyOnWriteArrayList<>();

	/** The save. */
	ToolItem add, kill, loadNew, loadAndReplace, duplicate, save, saveHistory;

	/** The hide. */
	final Listener hide = event -> hide();

	/** The provider. */
	private final ExperimentControlContribution status;

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
		super(WorkbenchHelper.getShell(), PopupDialog.HOVER_SHELLSTYLE, false, false, false, false, false, null, null);
		this.status = status;
		final Shell shell = status.getControllingShell();
		shell.addListener(SWT.Move, hide);
		shell.addListener(SWT.Resize, hide);
		shell.addListener(SWT.Close, hide);
		shell.addListener(SWT.Deactivate, hide);
		shell.addListener(SWT.Hide, hide);
		shell.addListener(SWT.Dispose, event -> close());
	}

	/**
	 * Gets the agents to display.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the agents to display
	 * @date 25 août 2023
	 */
	List<ITopLevelAgent> getAgentsToDisplay() {
		IExperimentPlan plan = GAMA.getExperiment();
		if (plan == null) return Collections.EMPTY_LIST;
		IExperimentAgent exp = GAMA.getExperiment().getAgent();
		if (exp == null) return Collections.EMPTY_LIST;
		List<ITopLevelAgent> agents = new ArrayList<>();
		agents.add(exp);
		SimulationPopulation simPop = exp.getSimulationPopulation();
		if (simPop == null) return agents;
		agents.addAll(GAMA.getExperiment().getAgent().getSimulationPopulation());
		return agents;
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
		}
		fillLabels(getAgentsToDisplay());
		return contents;
	}

	/**
	 * Fill labels.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param agents
	 *            the agents
	 * @date 26 août 2023
	 */
	private void fillLabels(final List<ITopLevelAgent> agents) {
		int size = agents.size();
		//createLabels(size);
		for (int i = 0; i < size; i++) {
			try {
				ITopLevelAgent agent = agents.get(i);
				Composite labelComposite = getOrCreateLabel(i);
				final Label b = (Label) labelComposite.getChildren()[0];
				final Label label = (Label) labelComposite.getChildren()[1];
				b.setImage(GamaIcon.ofColor(GamaColors.get(agent.getColor()), false).image());
				labelComposite.setData(agent);
				label.setText(GamlIdiomsProvider.toText(status.popupTextFor(agent)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (size < labels.size()) {
			for (int i = size; i < labels.size(); i++) {
				labels.get(i).dispose();
				labels.remove(i);
			}
		}

	}
	
	Composite getOrCreateLabel(int i) {
		if (i > labels.size() - 1) {
			labels.add(createLabel());
		}
		Composite c = labels.get(i);
		if (c == null || c.isDisposed()) {
			labels.remove(i);
			c = createLabel();
			labels.add(i,c);
		}
		return c;
	}


	/**
	 * Creates the label.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param labels
	 *            the labels
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
					GamaColor c = ((ITopLevelAgent) labelComposite.getData()).getColor();
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
					status.setSelection((ITopLevelAgent) labelComposite.getData());
				}

			});
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
		boolean isExperiment = GAMA.getCurrentTopLevelAgent() instanceof IExperimentAgent;
		boolean isSimulation = GAMA.getCurrentTopLevelAgent() instanceof SimulationAgent;
		boolean isBackward = isSimulation && GAMA.getExperiment() != null && GAMA.getExperiment().isMemorize();
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
		Point p = status.getLocation();
		return new Point(p.x, p.y + status.getHeight() - 4);
	}

	@Override
	protected Point getDefaultSize() {
		int width = status.getWidth();
		if (width <= 0) { width = SWT.DEFAULT; }
		return getShell().computeSize(width, SWT.DEFAULT, true);
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
			shell.setLocation(getDefaultLocation(null));
			shell.setSize(getDefaultSize());
			shell.setVisible(true);
		} else {
			open();
		}
	}

	/**
	 * Hide.
	 */
	public void hide() {
		final Shell shell = getShell();
		if (shell != null && !shell.isDisposed()) { shell.setVisible(false); }
	}

	/**
	 * Wipe.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 31 août 2023
	 */
	public void wipe() {
		for (Control c : labels) {
			if (!c.isDisposed()) c.setData(null); }
	}
}
