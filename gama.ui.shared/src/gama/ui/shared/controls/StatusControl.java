/*******************************************************************************************************
 *
 * StatusControl.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
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
 * The Class ExperimentControl.
 */
public class StatusControl implements IStatusControl {

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
	private final static int WIDTH = 400;

	/** The instance. */
	static StatusControl INSTANCE = new StatusControl();

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
			if (label == null || label.isDisposed()) return Status.CANCEL_STATUS;
			label.setImageWithoutRecomputingSize(GamaIcon.named(IStatusMessage.IDLE_ICON).image());
			label.setTextWithoutRecomputingSize("Idle");
			return Status.OK_STATUS;
		}
	};

	/**
	 * Gets the single instance of ExperimentControl.
	 *
	 * @return single instance of ExperimentControl
	 */
	public static StatusControl getInstance() { return INSTANCE; }

	/**
	 * Instantiates a new status control contribution.
	 */
	public StatusControl() {
		WorkbenchHelper.getService(IStatusDisplayer.class).setStatusTarget(this);
	}

	/**
	 * Install on.
	 *
	 * @param parent
	 *            the parent
	 * @return the tool item
	 */
	public static Control installOn(final Composite parent) {
		return INSTANCE.createControl(parent);
	}

	/**
	 * Creates the control.
	 *
	 * @param parent
	 *            the parent
	 * @return the control
	 */
	protected Control createControl(final Composite parent) {
		// final Composite compo = new Composite(parent, SWT.DOUBLE_BUFFERED);
		// GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(compo);
		inactiveColor = ThemeHelper.isDark() ? GamaColors.get(GamaColors.get(parent.getBackground()).lighter())
				: GamaColors.get(GamaColors.get(parent.getBackground()).darker());
		label = FlatButton.label(parent, inactiveColor, "", WIDTH).addMenuSign();
		label.setEnabled(false);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(label);
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
				if (WorkbenchHelper.getWorkbench().isClosing()) return;
				String name = job.getName() == null ? "" : job.getName().strip();
				if (uselessJobs.contains(name)) return;
				Object jobProperty = job.getProperty(IStatusMessage.JOB_KEY);
				if (IStatusMessage.INTERNAL_STATUS_REFRESH_JOB.equals(jobProperty)) return;
				boolean isView = IStatusMessage.VIEW_JOB.equals(jobProperty);
				WorkbenchHelper.asyncRun(() -> updateWith(StatusMessageFactory.CUSTOM(name, StatusType.REGULAR,
						isView ? IStatusMessage.VIEW_ICON : IStatusMessage.SYSTEM_ICON, null)));
			}

			@Override
			public void done(final IJobChangeEvent event) {}

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
			public void scheduled(final IJobChangeEvent event) {}

			@Override
			public void awake(final IJobChangeEvent event) {}

			@Override
			public void sleeping(final IJobChangeEvent event) {}
		});

		return label;
	}

	@Override
	public boolean isDisposed() { return label.isDisposed(); }

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
	 * @see gama.ui.shared.factories.IStatusControl.swt.controls.ThreadedUpdater.IUpdaterTarget#updateWith(java.lang.Object)
	 */
	@Override
	public void updateWith(final IStatusMessage m) {
		if (isUpdating) return;
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
					if (historyPopup.isVisible()) { historyPopup.display(); }
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
		if (taskName == null) { taskName = ""; }
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

	@Override
	public void setWidth(final int i) {
		label.withWidth(i);
		label.computePreferredSize();
	}

}
