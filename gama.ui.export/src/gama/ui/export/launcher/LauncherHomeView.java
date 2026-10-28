package gama.ui.export.launcher;

import java.nio.file.Path;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import gama.api.GAMA;
import gama.dev.DEBUG;
import gama.ui.export.commands.SaveSimulationsArtifacts;
import gama.workspace.manager.WorkspacePaths;

public final class LauncherHomeView extends ViewPart {

	public static final String ID = "gama.ui.export.views.Home";
	private Composite content;

	@Override
	public void createPartControl(final Composite parent) {
		final var helper = StartupModelHelper.getInstance();
		final var configuration = helper.getConfiguration();
		final var scroll = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		content = new Composite(scroll, SWT.NONE);
		final GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 40;
		layout.marginHeight = 32;
		layout.verticalSpacing = 18;
		content.setLayout(layout);
		scroll.setContent(content);
		final Label title = new Label(content, SWT.WRAP);
		title.setText(configuration.title().isBlank() ? "GAMA simulation launcher" : configuration.title());
		title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		if (!configuration.description().isBlank()) {
			final Label description = new Label(content, SWT.WRAP);
			description.setText(configuration.description());
			description.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}
		if (!configuration.image().isBlank()) {
			final Path imagePath = WorkspacePaths.installationDirectory().resolve(configuration.image());
			try {
				final Image image = new Image(parent.getDisplay(), imagePath.toString());
				final Label illustration = new Label(content, SWT.NONE);
				illustration.setImage(image);
				illustration.addDisposeListener(event -> image.dispose());
			} catch (final RuntimeException e) {
				DEBUG.ERR("Unable to load launcher home image " + imagePath, e);
				final Label error = new Label(content, SWT.WRAP);
				error.setText("Unable to load the home page image: " + imagePath);
			}
		}
		final Label prompt = new Label(content, SWT.NONE);
		prompt.setText("Choose an experiment");
		for (final String selection : helper.getSelections()) {
			final Button launch = new Button(content, SWT.PUSH);
			launch.setText(helper.label(selection));
			launch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			launch.addListener(SWT.Selection, event -> {
				launch.setEnabled(false);
				try {
					helper.launch(selection);
				} catch (final RuntimeException e) {
					DEBUG.ERR("Unable to launch the selected experiment", e);
					GAMA.getGui().getDialogFactory().error("Unable to launch the experiment: " + e.getMessage());
				} finally {
					if (!launch.isDisposed()) launch.setEnabled(true);
				}
			});
		}
		final Button save = new Button(content, SWT.PUSH);
		save.setText("Save Simulation Data...");
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
	}

	@Override
	public void setFocus() { content.setFocus(); }
}
