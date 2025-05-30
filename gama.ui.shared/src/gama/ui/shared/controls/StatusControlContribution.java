/*******************************************************************************************************
 *
 * StatusControlContribution.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.controls;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
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
import org.eclipse.ui.progress.UIJob;

import gama.core.common.IStatusMessage;
import gama.core.common.IStatusMessage.StatusType;
import gama.core.common.StatusMessage;
import gama.core.common.StatusMessageFactory;
import gama.core.common.interfaces.IStatusControl;
import gama.core.common.interfaces.IStatusDisplayer;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.dev.DEBUG;
import gama.ui.application.workbench.ThemeHelper;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaColors.GamaUIColor;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class ExperimentControlContribution.
 */
public class StatusControlContribution extends WorkbenchWindowControlContribution implements IStatusControl {

    static {
	DEBUG.OFF();
    }

    /** The current status. */
    StatusType currentStatus;

    /** The current exception. */
    GamaRuntimeException currentException;

    /** The is updating. */
    private volatile boolean isUpdating;

    /** The label. */
    private FlatButton label;

    /** The history popup. */
    private StatusHistoryPopUpMenu historyPopup;

    /** The sub task completion. */
    private volatile Double taskCompletion;

    /** The Constant WIDTH. */
    private final static int WIDTH = 300;

    /** The instance. */
    static StatusControlContribution INSTANCE;

    /** The inactive color. */
    private GamaUIColor inactiveColor;

    /** The show system events. */
    boolean showSystemEvents = true;

    /** The show view events. */
    boolean showViewEvents = true;

    /** The icon provider. */
    StatusIconProvider iconProvider = new StatusIconProvider();

    /** The idle job. */
    UIJob idleJob = new UIJob("Idle") {

	@Override
	public IStatus runInUIThread(final IProgressMonitor monitor) {
	    if (label == null || label.isDisposed()) {
		return Status.CANCEL_STATUS;
	    }
	    label.setImageWithoutRecomputingSize(GamaIcon.named(IStatusMessage.IDLE_ICON).image());
	    label.setTextWithoutRecomputingSize("Idle");
	    return Status.OK_STATUS;
	}
    };

    /**
     * Gets the single instance of ExperimentControlContribution.
     *
     * @return single instance of ExperimentControlContribution
     */
    public static StatusControlContribution getInstance() {
	return INSTANCE;
    }

    /**
     * Instantiates a new status control contribution.
     */
    public StatusControlContribution() {
	INSTANCE = this;
	WorkbenchHelper.getService(IStatusDisplayer.class).setStatusTarget(this);
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
    protected Control createControl(final Composite parent) {
	final Composite compo = new Composite(parent, SWT.DOUBLE_BUFFERED);
	inactiveColor = ThemeHelper.isDark() ? GamaColors.get(GamaColors.get(parent.getBackground()).lighter())
		: GamaColors.get(GamaColors.get(parent.getBackground()).darker());
	GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false).applyTo(compo);
	label = FlatButton.label(compo, inactiveColor, "", WIDTH).addMenuSign();
	label.setEnabled(false);
	GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(WIDTH, 25).applyTo(label);
	historyPopup = new StatusHistoryPopUpMenu(this);

	label.addMouseListener(new MouseAdapter() {

	    @Override
	    public void mouseDown(final MouseEvent e) {

		if (historyPopup.isVisible()) {
		    historyPopup.hide();
		} else {
		    WorkbenchHelper.asyncRun(historyPopup::display);
		}
	    }

	});
	Job.getJobManager().addJobChangeListener(new JobChangeAdapter() {

	    final Set<String> uselessJobs = Set.of("Win32 refresh daemon", "Animation start", "Decoration Calculation",
		    "Update Capability Enablement for Natures", "Status refresh", "Update for Decoration Completion",
		    "Change cursor", "Searching for local changes", "Hooking to commands", "Update Job",
		    "Check for workspace changes", "Refreshing view", "Mark Occurrences", "XtextReconcilerJob",
		    "Xtext validation", "Searching for markers", "Idle");

	    @Override
	    public void aboutToRun(final IJobChangeEvent event) {
		Job job = event.getJob();
		if (WorkbenchHelper.getWorkbench().isClosing()) {
		    return;
		}
		String name = job.getName() == null ? "" : job.getName().strip();
		if (uselessJobs.contains(name)) {
		    return;
		}
		// DEBUG.OUT("Name " + job.getName() + " - Group " +
		// job.getJobGroup() + " - Rule " + job.getRule()
		// + " - Priority " + jobPriority(job.getPriority()));
		Object jobProperty = job.getProperty(IStatusMessage.JOB_KEY);
		if (IStatusMessage.INTERNAL_STATUS_REFRESH_JOB.equals(jobProperty)) {
		    return;
		}
		boolean isView = IStatusMessage.VIEW_JOB.equals(jobProperty);
		// if (isView ? !showViewEvents : !showSystemEvents) {}
		WorkbenchHelper.asyncRun(() -> updateWith(StatusMessageFactory.CUSTOM(name, StatusType.REGULAR,
			isView ? IStatusMessage.VIEW_ICON : IStatusMessage.SYSTEM_ICON, null)));
	    }

	    // private boolean intersect(final String s1, final String s2) {
	    // if (s1 == null) return s2 == null;
	    // if (s2 == null) return false;
	    // return s1.contains(s2) || s2.contains(s1);
	    // }

	    @Override
	    public void done(final IJobChangeEvent event) {
		// if (WorkbenchHelper.getWorkbench().isClosing() ||
		// event.getJob() instanceof StatusRefresher) return;
		// String message = event.getJob().getName();
		// if (intersect(label.getText(), message)) {
		// WorkbenchHelper.asyncRun(() ->
		// updateWith(StatusMessage.IDLE()));
		// }
		// else {
		// WorkbenchHelper
		// .asyncRun(() ->
		// updateWith(StatusMessage.END(event.getJob().getName() + "
		// (ended)")));
		// }
		// DEBUG.OUT("Job finished : " + event.getJob().toString() + "
		// with priority "
		// + jobPriority(event.getJob().getPriority()));
	    }

	    private String jobPriority(final int p) {
		return switch (p) {
		case Job.INTERACTIVE -> "INTERACTIVE";
		case Job.BUILD -> "BUILD";
		case Job.DECORATE -> "DECORATE";
		case Job.LONG -> "LONG";
		case Job.SHORT -> "SHORT";
		default -> "NONE";
		};
	    }

	    @Override
	    public void scheduled(final IJobChangeEvent event) {
	    }

	    @Override
	    public void awake(final IJobChangeEvent event) {
	    }

	    @Override
	    public void sleeping(final IJobChangeEvent event) {
	    }
	});

	// label.addMouseListener(new MouseAdapter() {
	//
	// @Override
	// public void mouseDown(final MouseEvent e) {
	//
	// if (currentStatus == StatusType.ERROR) {
	// if (historyPopup.isVisible()) {
	// historyPopup.hide();
	// } else {
	// WorkbenchHelper.asyncRun(() ->
	// historyPopup.display(currentException));
	// }
	// }
	//
	// }
	//
	// });
	return compo;
    }

    @Override
    public boolean isDisposed() {
	return label.isDisposed();
    }

    /**
     * Gets the controlling shell.
     *
     * @author Alexis Drogoul (alexis.drogoul@ird.fr)
     * @return the controlling shell
     * @date 26 août 2023
     */
    public Shell getControllingShell() {
	return label.getShell();
    }

    /**
     * Gets the absolute origin.
     *
     * @author Alexis Drogoul (alexis.drogoul@ird.fr)
     * @return the absolute origin
     * @date 26 août 2023
     */
    public Point getLocation() {
	return label.toDisplay(label.getLocation());
    }

    /**
     * Gets the popup width.
     *
     * @author Alexis Drogoul (alexis.drogoul@ird.fr)
     * @return the popup width
     * @date 26 août 2023
     */
    public int getWidth() {
	return label.getSize().x;
    }

    /**
     * Gets the height.
     *
     * @author Alexis Drogoul (alexis.drogoul@ird.fr)
     * @return the height
     * @date 26 août 2023
     */
    public int getHeight() {
	return label.getSize().y;
    }

    /**
     * Method updateWith()
     *
     * @see gama.ui.shared.factories.IStatusControl.swt.controls.ThreadedUpdater.IUpdaterTarget#updateWith(java.lang.Object)
     */
    @Override
    public void updateWith(final IStatusMessage m) {
	if (isUpdating) {
	    return;
	}
	isUpdating = true;
	try {
	    if (m == null) {
		taskCompletion = null;
		currentException = null;
		currentStatus = StatusType.NONE;
		label.setColor(inactiveColor);
		label.setTextWithoutRecomputingSize("");
		label.setImageWithoutRecomputingSize(null);
	    } else {
		taskCompletion = m.completion();
		currentException = m.exception();
		currentStatus = m.type();
		String icon = iconProvider.getIcon();
		label.setImageWithoutRecomputingSize(icon == null ? null : GamaIcon.named(icon).image());
		// label.setColor(getLabelBackground(m));
		label.setTextWithoutRecomputingSize(getLabelText(m));
		if (currentStatus != StatusType.NONE) {
		    this.historyPopup.addStatus(m);
		    if (historyPopup.isVisible()) {
			historyPopup.display();
		    }
		}
	    }
	} finally {
	    isUpdating = false;
	    idleJob.schedule(2000);
	}

    }

    /**
     * Gets the label text.
     *
     * @param m
     *            the m
     * @return the label text
     */
    private String getLabelText(final IStatusMessage m) {
	String taskName = m.message();
	if (taskName == null) {
	    taskName = "";
	}
	return taskName + (taskCompletion != null ? " [" + (int) (taskCompletion * 100) + "%]" : "");
    }

    /**
     * Gets the label background.
     *
     * @param m
     *            the m
     * @return the label background
     */
    public GamaUIColor getLabelBackground(final StatusMessage m) {
	return GamaColors.get(m.color());
    }

    /**
     * Show system events.
     *
     * @param show
     *            the show
     */
    public void showSystemEvents(final boolean show) {
	this.showSystemEvents = show;
    }

    /**
     * Show view events.
     *
     * @param show
     *            the show
     */
    public void showViewEvents(final boolean show) {
	this.showViewEvents = show;
    }

    /**
     * Show system events.
     *
     * @return true, if successful
     */
    public boolean showSystemEvents() {
	return showSystemEvents;
    }

    /**
     * Show view events.
     *
     * @return true, if successful
     */
    public boolean showViewEvents() {
	return showSystemEvents;
    }

}
