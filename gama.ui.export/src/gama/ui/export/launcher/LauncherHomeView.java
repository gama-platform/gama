package gama.ui.export.launcher;

import java.nio.file.Path;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;

import gama.api.GAMA;
import gama.dev.DEBUG;
import gama.ui.export.commands.SaveSimulationsArtifacts;
import gama.ui.shared.controls.FlatButton;
import gama.ui.shared.menus.ExperimentMenu;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaColors;
import gama.ui.shared.resources.IGamaIcons;
import gama.workspace.manager.WorkspacePaths;

public final class LauncherHomeView extends ViewPart {

	public static final String ID = "gama.ui.export.views.Home";
	private Composite content;
	private Composite parent;
	private ToolBar playBar;
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
		final Label title = new Label(center, SWT.CENTER | SWT.WRAP);
		title.setText(configuration.title().isBlank() ? "GAMA simulation launcher" : configuration.title());
		final FontData base = parent.getFont().getFontData()[0];
		final Font titleFont = new Font(parent.getDisplay(), base.getName(), 20, SWT.BOLD);
		title.setFont(titleFont);
		title.addDisposeListener(event -> titleFont.dispose());
		title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		if (helper.getSelections().size() > 1) {
			final FlatButton selector = FlatButton.menu(center, IGamaColors.OK, helper.label(selectedExperiment))
					.withHeight(24);
			selector.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			selector.setToolTipText("Choose an experiment");
			final var menu = ExperimentMenu.create(selector, helper.getSelections().stream()
					.map(selection -> new ExperimentMenu.Entry(selection, helper.label(selection), "gui")).toList(),
					selection -> {
						selectedExperiment = selection;
						selector.setText(helper.label(selection));
						center.layout(true, true);
					});
			selector.setSelectionListener(event -> ExperimentMenu.open(menu, selector));
		}
		playBar = new ToolBar(center, SWT.FLAT);
		playBar.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		final ToolItem play = new ToolItem(playBar, SWT.PUSH);
		final GamaIcon icon = GamaIcon.named(IGamaIcons.EXPERIMENT_RUN);
		play.setImage(icon.image());
		play.setDisabledImage(icon.disabled());
		play.setToolTipText("Run experiment");
		play.addListener(SWT.Selection, event -> {
			play.setEnabled(false);
			try {
				helper.launch(selectedExperiment);
			} catch (final RuntimeException e) {
				DEBUG.ERR("Unable to launch the selected experiment", e);
				GAMA.getGui().getDialogFactory().error("Unable to launch the experiment: " + e.getMessage());
			} finally {
				if (!play.isDisposed()) play.setEnabled(true);
			}
		});
		if (!configuration.description().isBlank()) {
			final Label description = new Label(center, SWT.CENTER | SWT.WRAP);
			description.setText(configuration.description());
			final GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
			data.widthHint = 560;
			description.setLayoutData(data);
		}
		if (!configuration.image().isBlank()) {
			final Path imagePath = WorkspacePaths.installationDirectory().resolve(configuration.image());
			try {
				final Image image = new Image(parent.getDisplay(), imagePath.toString());
				final Label illustration = new Label(center, SWT.NONE);
				illustration.setImage(image);
				illustration.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
				illustration.addDisposeListener(event -> image.dispose());
			} catch (final RuntimeException e) {
				DEBUG.ERR("Unable to load launcher home image " + imagePath, e);
				new Label(center, SWT.WRAP).setText("Unable to load the home page image: " + imagePath);
			}
		}
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
		if (playBar != null) playBar.setFocus();
	}

	@Override
	public void dispose() {
		if (visibilityListener != null) getSite().getPage().removePartListener(visibilityListener);
		super.dispose();
	}
}
