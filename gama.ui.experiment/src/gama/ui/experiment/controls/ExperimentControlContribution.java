/*******************************************************************************************************
 *
 * ExperimentControlContribution.java, in gama.ui.experiment, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.controls;

import static gama.ui.shared.resources.GamaColors.get;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

import gama.api.GAMA;
import gama.api.kernel.PlatformAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.simulation.IClock;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.ui.IStatusControl;
import gama.api.ui.IStatusDisplayer;
import gama.api.ui.IStatusMessage;
import gama.api.utils.StringUtils;
import gama.ui.shared.controls.FlatButton;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaColors;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class ExperimentControlContribution.
 */
public class ExperimentControlContribution extends WorkbenchWindowControlContribution implements IStatusControl {

	/** The is updating. */
	private volatile boolean isUpdating;

	/** The label. */
	private FlatButton label;

	/** The popup. */
	private SimulationPopupMenu popup;

	/** The Constant WIDTH. */
	private final static int WIDTH = 300;

	/** The text. */
	private final StringBuilder text = new StringBuilder(2000);

	/**
	 * Instantiates a new status control contribution.
	 */
	public ExperimentControlContribution() {
		WorkbenchHelper.getService(IStatusDisplayer.class).setExperimentTarget(this);
	}

	/**
	 * Instantiates a new status control contribution.
	 *
	 * @param id
	 *            the id
	 */
	public ExperimentControlContribution(final String id) { // NO_UCD (unused
		// code)
		super(id);
		WorkbenchHelper.getService(IStatusDisplayer.class).setExperimentTarget(this);
	}

	@Override
	protected int computeWidth(final Control control) {
		return WIDTH;
	}

	@Override
	protected Control createControl(final Composite parent) {
		final Composite compo = new Composite(parent, SWT.DOUBLE_BUFFERED);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(compo);
		Label l = new Label(compo, SWT.NONE);
		l.setText("");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(8, 25).applyTo(l);
		label = FlatButton.label(compo, IGamaColors.NEUTRAL, "No experiment running", WIDTH).withHeight(25)
				.setImage(GamaIcon.named(IGamaIcons.STATUS_CLOCK).image());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(WIDTH, 25).applyTo(label);
		l = new Label(compo, SWT.NONE);
		l.setText("");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(8, 25).applyTo(l);
		popup = new SimulationPopupMenu(this);
		label.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(final MouseEvent e) {

				if (popup.isVisible()) {
					popup.hide();
				} else {
					final ITopLevelAgent agent = GAMA.getCurrentTopLevelAgent();
					if (agent != null && !agent.dead() && !agent.getScope().isClosed() && agent.getExperiment() != null
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
		} else if (agent instanceof ISimulationAgent sim) { GAMA.getExperiment().getAgent().setCurrentSimulation(sim); }
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
		final IClock clock = exp.getClock();
		clock.getInfo(text).append(StringUtils.LN);
		text.append("Durations: cycle ").append(clock.getDuration()).append("ms; average ")
				.append((int) clock.getAverageDuration()).append("ms; total ").append(clock.getTotalDuration())
				.append("ms");
		return text.toString();
	}

	/**
	 * Gets the controlling shell.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the controlling shell
	 * @date 26 août 2023
	 */
	public Shell getControllingShell() { return label.getShell(); }

	/**
	 * Gets the absolute origin.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the absolute origin
	 * @date 26 août 2023
	 */
	public Point getLocation() {
		Point p = label.toDisplay(label.getLocation());
		p.x -= 14;
		p.y += getHeight() - 4;
		return p;
	}

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
	 * @see gama.api.interfaces.IStatusControl.swt.controls.ThreadedUpdater.IUpdaterTarget#updateWith(java.lang.Object)
	 */
	@Override
	public void updateWith(final IStatusMessage m) {
		if (isUpdating) return;
		try {
			isUpdating = true;
			// DEBUG.OUT("Updating with current experiment " +
			// GAMA.getExperiment());
			if (GAMA.getExperiment() == null) {
				label.removeMenuSign();
				popup.wipe();
				if (popup.isVisible()) { popup.hide(); }
			} else {
				label.addMenuSign();
			}
			ITopLevelAgent agent = GAMA.getCurrentTopLevelAgent();
			// label.setImageWithoutRecomputingSize(m.icon() == null ? null :
			// GamaIcon.named(m.icon()).image());
			label.setColor(get(agent.getColor()));
			label.setTextWithoutRecomputingSize(getClockMessage(agent));
			if (popup.isVisible()) { popup.display(); }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			isUpdating = false;
		}
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
	private String getClockMessage(final ITopLevelAgent agent) {
		if (agent == null) return "";
		if (agent instanceof PlatformAgent) return "No experiment running";
		final IExperimentAgent exp = agent.getExperiment();
		if (exp == null) return "";
		text.setLength(0);
		agent.getClock().getInfo(text);
		final IPopulation.Simulation pop = exp.getSimulationPopulation();
		final int nbThreads = pop == null ? 1 : pop.getNumberOfActiveThreads();
		if (agent.getScope().isOnUserHold()) {
			text.append(" (waiting)");
		} else if (nbThreads > 1) { text.append(" (" + nbThreads + " threads)"); }
		final IExperimentSpecies plan = exp.getSpecies();
		if (plan.shouldBeBenchmarked()) { text.append(" [benchmarking]"); }
		return text.toString();
	}

}
