package gama.ui.export.launcher;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;

import gama.api.GAMA;
import gama.dev.DEBUG;
import gama.ui.export.commands.SaveSimulationsArtifacts;
import gama.ui.shared.utils.LaunchPresentation;

public final class LauncherHomeView extends ViewPart {

	public static final String ID = "gama.ui.export.views.Home";
	private Composite content;
	private Composite parent;
	private LaunchPresentation presentation;
	private String selectedExperiment;
	private IPartListener2 visibilityListener;

	@Override
	public void createPartControl(final Composite parent) {
		this.parent = parent;
		if (StartupModelHelper.getInstance().isHomeVisibleAtStartup()) {
			createHome();
		} else {
			// Direct startup uses an empty landing page until the experiment perspective is ready.
			visibilityListener = new IPartListener2() {
				@Override
				public void partVisible(final IWorkbenchPartReference part) {
					if (ID.equals(part.getId()) && StartupModelHelper.getInstance().isHomeVisibleAtStartup()
							&& content == null) createHome();
				}
			};
			getSite().getPage().addPartListener(visibilityListener);
		}
	}

	private void createHome() {
		final var helper = StartupModelHelper.getInstance();
		final var configuration = helper.getConfiguration();
		selectedExperiment = configuration.startupExperiment().isBlank() ? helper.getSelections().getFirst()
				: configuration.startupExperiment();
		final var scroll = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		scroll.setBackground(parent.getShell().getBackground());
		scroll.setForeground(parent.getShell().getForeground());
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		scroll.setBackgroundMode(SWT.INHERIT_FORCE);
		scroll.setLayout(new GridLayout());
		content = new Composite(scroll, SWT.NONE);
		final GridLayout layout = new GridLayout();
		layout.marginWidth = 40;
		layout.marginHeight = 32;
		content.setLayout(layout);
		scroll.setContent(content);
		final Composite center = new Composite(content, SWT.NONE);
		center.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		final GridLayout centerLayout = new GridLayout();
		centerLayout.verticalSpacing = 18;
		center.setLayout(centerLayout);
		presentation = new LaunchPresentation(center, helper.getPresentation(), selectedExperiment, false,
				selection -> selectedExperiment = selection, () -> {
			final var shell = parent.getShell();
			final var origin = shell.toControl(presentation.toDisplay(0, 0));
			final var size = presentation.getSize();
			shell.setData(LaunchPresentation.SHELL_DATA_KEY, new LaunchPresentation.State(helper.getPresentation(),
					selectedExperiment, new org.eclipse.swt.graphics.Rectangle(origin.x, origin.y, size.x, size.y)));
			try {
				helper.launch(selectedExperiment);
			} catch (final RuntimeException e) {
				DEBUG.ERR("Unable to launch the selected experiment", e);
				GAMA.getGui().getDialogFactory().error("Unable to launch the experiment: " + e.getMessage());
			}
		});
		presentation.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		final Button save = new Button(center, SWT.PUSH);
		save.setText("Save Simulation Data...");
		save.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		save.addListener(SWT.Selection, event -> {
			try {
				new SaveSimulationsArtifacts().execute(null);
			} catch (final org.eclipse.core.commands.ExecutionException e) {
				DEBUG.ERR("Unable to save simulation data", e);
				GAMA.getGui().getDialogFactory().error(e.getMessage());
			}
		});
		scroll.addListener(SWT.Resize, event -> {
			final int width = Math.max(300, scroll.getClientArea().width);
			scroll.setMinSize(content.computeSize(width, SWT.DEFAULT));
		});
		scroll.setMinSize(content.computeSize(600, SWT.DEFAULT));
		parent.layout(true, true);
	}

	@Override
	public void setFocus() {
		if (content == null && StartupModelHelper.getInstance().isHomeVisibleAtStartup()) createHome();
		if (presentation != null) presentation.setFocus();
	}

	@Override
	public void dispose() {
		if (visibilityListener != null) getSite().getPage().removePartListener(visibilityListener);
		super.dispose();
	}
}
