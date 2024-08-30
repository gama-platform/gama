/*******************************************************************************************************
 *
 * StatusControlContribution.java, in gama.ui.shared.experiment, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.controls;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

import gama.core.common.ErrorStatusMessage;
import gama.core.common.StatusMessage;
import gama.core.common.SubTaskMessage;
import gama.core.common.UserStatusMessage;
import gama.core.common.interfaces.IGui;
import gama.core.common.interfaces.IStatusMessage;
import gama.core.common.interfaces.IUpdaterTarget;
import gama.core.kernel.experiment.IExperimentAgent;
import gama.core.kernel.experiment.IExperimentPlan;
import gama.core.kernel.experiment.ITopLevelAgent;
import gama.core.kernel.root.PlatformAgent;
import gama.core.kernel.simulation.SimulationAgent;
import gama.core.kernel.simulation.SimulationClock;
import gama.core.kernel.simulation.SimulationPopulation;
import gama.core.runtime.GAMA;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.dev.DEBUG;
import gama.gaml.operators.Strings;
import gama.ui.shared.controls.FlatButton;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaColors.GamaUIColor;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaColors;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class StatusControlContribution.
 */
public class StatusControlContribution extends WorkbenchWindowControlContribution
		implements IUpdaterTarget<IStatusMessage> {

	static {
		DEBUG.ON();
	}

	GamaRuntimeException currentException;

	/** The is updating. */
	private volatile boolean isUpdating;

	/** The label. */
	private FlatButton label;

	/** The popup. */
	private SimulationPopupMenu popup;

	private ErrorPopUpMenu errorPopup;

	/** The state. */
	private int state;

	/** The main task name. */
	private volatile String mainTaskName;

	/** The sub task name. */
	private volatile String subTaskName;

	/** The in sub task. */
	private volatile boolean inSubTask = false;

	/** The in user status. */
	private volatile boolean inUserStatus = false;

	private volatile boolean inErrorStatus = false;

	/** The sub task completion. */
	private volatile Double subTaskCompletion;

	/** The Constant WIDTH. */
	private final static int WIDTH = 400;

	/** The color. */
	private GamaUIColor color;

	/** The text. */
	private final StringBuilder text = new StringBuilder(2000);

	/** The instance. */
	static StatusControlContribution INSTANCE;

	/**
	 * Gets the single instance of StatusControlContribution.
	 *
	 * @return single instance of StatusControlContribution
	 */
	public static StatusControlContribution getInstance() { return INSTANCE; }

	/**
	 * Instantiates a new status control contribution.
	 */
	public StatusControlContribution() {
		INSTANCE = this;
	}

	/**
	 * Instantiates a new status control contribution.
	 *
	 * @param id
	 *            the id
	 */
	public StatusControlContribution(final String id) { // NO_UCD (unused code)
		super(id);
		INSTANCE = this;
	}

	@Override
	protected int computeWidth(final Control control) {
		return WIDTH;
	}

	@Override
	public boolean isBusy() { return isUpdating; }

	@Override
	protected Control createControl(final Composite parent) {
		final Composite compo = new Composite(parent, SWT.DOUBLE_BUFFERED);
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false).applyTo(compo);
		label = FlatButton.label(compo, IGamaColors.NEUTRAL, "No experiment running", WIDTH)
				.setImage(GamaIcon.named(IGamaIcons.STATUS_CLOCK).image());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).hint(WIDTH, 24).applyTo(label);
		popup = new SimulationPopupMenu(this);
		errorPopup = new ErrorPopUpMenu(this);
		label.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(final MouseEvent e) {
				if (inErrorStatus) {
					if (errorPopup.isVisible()) {
						errorPopup.hide();
					} else {
						WorkbenchHelper.asyncRun(() -> errorPopup.display(currentException));
					}
				} else if (popup.isVisible()) {
					popup.hide();
				} else {
					final ITopLevelAgent agent = GAMA.getCurrentTopLevelAgent();
					if (!inErrorStatus && state != IGui.ERROR && state != IGui.WAIT && agent != null && !agent.dead()
							&& !agent.getScope().isClosed() && agent.getExperiment() != null
							&& !(agent instanceof PlatformAgent)) {
						WorkbenchHelper.asyncRun(popup::display);
					}
				}
			}

		});
		return compo;
	}

	@Override
	public boolean isDisposed() { return label.isDisposed(); }

	/**
	 * Sets the selection.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param agent
	 *            the new selection
	 * @date 26 août 2023
	 */

	public void setSelection(final ITopLevelAgent agent) {
		if (agent instanceof IExperimentAgent exp) {
			GAMA.changeCurrentTopLevelAgent(exp, false);
		} else if (agent instanceof SimulationAgent sim) { GAMA.getExperiment().getAgent().setCurrentSimulation(sim); }
	}

	/**
	 * Popup text for.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param exp
	 *            the exp
	 * @return the string
	 * @date 26 août 2023
	 */
	String popupTextFor(final ITopLevelAgent exp) {
		if (exp == null) return "";
		text.setLength(0);
		// text.append(Strings.LN);
		final SimulationClock clock = exp.getClock();
		clock.getInfo(text).append(Strings.LN);
		text.append("Durations: cycle ").append(clock.getDuration()).append("ms; average ")
				.append((int) clock.getAverageDuration()).append("ms; total ").append(clock.getTotalDuration())
				.append("ms");
		// text.append(Strings.LN);
		return text.toString();
	}

	/**
	 * Gets the popup background.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the popup background
	 * @date 26 août 2023
	 */
	// @Override
	public GamaUIColor getPopupBackground() {
		if (inUserStatus && color != null) return color;
		return state == IGui.ERROR ? IGamaColors.ERROR : state == IGui.WAIT ? IGamaColors.WARNING
				: state == IGui.NEUTRAL ? IGamaColors.NEUTRAL : IGamaColors.OK;
	}

	/**
	 * Gets the controlling shell.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the controlling shell
	 * @date 26 août 2023
	 */
	// @Override
	public Shell getControllingShell() { return label.getShell(); }

	/**
	 * Gets the absolute origin.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the absolute origin
	 * @date 26 août 2023
	 */
	public Point getLocation() { return label.toDisplay(label.getLocation()); }

	/**
	 * Gets the popup width.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the popup width
	 * @date 26 août 2023
	 */
	public int getWidth() { return label.getSize().x; }

	/**
	 * Gets the height.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the height
	 * @date 26 août 2023
	 */
	public int getHeight() { return label.getSize().y; }

	/**
	 * Method updateWith()
	 *
	 * @see gama.gui.swt.controls.ThreadedUpdater.IUpdaterTarget#updateWith(java.lang.Object)
	 */
	@Override
	public void updateWith(final IStatusMessage m) {
		if (isUpdating) return;
		prepareForUpdate();
		if (m instanceof SubTaskMessage m2) {
			if (inUserStatus) return;
			processSubTaskMessage(m2);
		} else if (m instanceof UserStatusMessage usm) {
			processUserStatusMessage(usm);
		} else if (m instanceof StatusMessage sm) {
			if (inUserStatus) return;
			processStatusMessage(sm);
		} else if (m instanceof ErrorStatusMessage esm) { processErrorStatusMessage(esm); }

		label.setImageWithoutRecomputingSize(m.getIcon() == null ? null : GamaIcon.named(m.getIcon()).image());
		label.setColor(getPopupBackground());
		if (!inUserStatus && !inSubTask && mainTaskName == null) {
			label.setColor(GamaColors.get(GAMA.getCurrentTopLevelAgent().getColor()));
		}

		if (inSubTask) {
			label.setTextWithoutRecomputingSize(
					subTaskName + (subTaskCompletion != null ? " [" + (int) (subTaskCompletion * 100) + "%]" : ""));
		} else if (mainTaskName == null) {
			label.setTextWithoutRecomputingSize(getClockMessage());
		} else {
			label.setTextWithoutRecomputingSize(mainTaskName);
		}
		if (popup.isVisible()) { popup.display(); }
		isUpdating = false;
		inUserStatus = false;
	}

	private void processErrorStatusMessage(final ErrorStatusMessage esm) {
		inErrorStatus = true;
		inSubTask = false; // in case
		state = esm.getCode();
		mainTaskName = "Error in previous experiment";
		currentException = esm.getException();
	}

	private void processStatusMessage(final StatusMessage sm) {
		inSubTask = false; // in case
		mainTaskName = sm.getText();
		state = sm.getCode();
	}

	private void processUserStatusMessage(final UserStatusMessage usm) {
		final String s = usm.getText();
		if (s == null) {
			resume();
		} else {
			inSubTask = false; // in case
			inUserStatus = true;
			final java.awt.Color c = usm.getColor();
			if (c == null) {
				color = null;
				state = IGui.NEUTRAL;
			} else {
				color = GamaColors.get(c);
			}
			mainTaskName = usm.getText();
		}
	}

	private void processSubTaskMessage(final SubTaskMessage m2) {
		final Boolean beginOrEnd = m2.getBeginOrEnd();
		if (beginOrEnd == null) {
			// completion
			subTaskCompletion = m2.getCompletion();
		} else {
			if (beginOrEnd) {
				// begin task
				subTaskName = m2.getText();
				inSubTask = true;
			} else {
				// end task
				inSubTask = false;
			}
			subTaskCompletion = null;
		}
	}

	private void prepareForUpdate() {
		currentException = null;
		inErrorStatus = false;
		if (GAMA.getExperiment() == null) {
			label.removeMenuSign();
			popup.wipe();
			if (popup.isVisible()) { popup.hide(); }
		} else {
			label.addMenuSign();
		}
		isUpdating = true;
	}

	/**
	 * Gets the clock message.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param agent
	 *            the agent
	 * @return the clock message
	 * @date 26 août 2023
	 */
	private String getClockMessage() {
		ITopLevelAgent agent = GAMA.getCurrentTopLevelAgent();
		if (agent == null) return "";
		if (agent instanceof PlatformAgent) {
			WorkbenchHelper.run(() -> {
				popup.wipe();
				if (popup.isVisible()) { popup.hide(); }
				label.removeMenuSign();
			});
			return "No experiment running";
		}
		text.setLength(0);
		agent.getClock().getInfo(text);
		final IExperimentAgent exp = agent.getExperiment();
		if (exp == null) return "";
		final SimulationPopulation pop = exp.getSimulationPopulation();
		final int nbThreads = pop == null ? 1 : pop.getNumberOfActiveThreads();
		if (agent.getScope().isOnUserHold()) {
			text.append(" (waiting)");
		} else if (nbThreads > 1) { text.append(" (" + nbThreads + " threads)"); }
		final IExperimentPlan plan = exp.getSpecies();
		if (plan.shouldBeBenchmarked()) { text.append(" [benchmarking]"); }
		return text.toString();
	}

	@Override
	public int getCurrentState() { return state; }

	/**
	 * Method resume()
	 *
	 * @see gama.core.common.interfaces.IUpdaterTarget#resume()
	 */
	@Override
	public void resume() {
		inUserStatus = false;
		color = null;
		mainTaskName = null;
	}

}
