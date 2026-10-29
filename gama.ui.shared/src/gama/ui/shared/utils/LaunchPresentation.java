package gama.ui.shared.utils;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import gama.dev.DEBUG;
import gama.ui.shared.controls.FlatButton;
import gama.ui.shared.menus.ExperimentMenu;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaColors;
import gama.ui.shared.resources.IGamaIcons;

/** The same presentation card is used before and during a simulation-player launch. */
public final class LaunchPresentation extends Composite {

	public static final String SHELL_DATA_KEY = LaunchPresentation.class.getName();

	public record Content(String title, String description, Path image, List<ExperimentMenu.Entry> experiments) {
		public Content {
			experiments = List.copyOf(experiments);
		}
	}

	/** Bounds are relative to the workbench client area, so launching does not move the Home card. */
	public record State(Content content, String selection, Rectangle bounds) {}

	private final ToolBar actionBar;

	public LaunchPresentation(final Composite parent, final Content content, final String selection,
			final boolean launching, final Consumer<String> choose, final Runnable action) {
		super(parent, SWT.NONE);
		setBackground(parent.getShell().getBackground());
		setForeground(parent.getShell().getForeground());
		setFont(parent.getShell().getFont());
		setBackgroundMode(SWT.INHERIT_FORCE);
		final GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.verticalSpacing = 4;
		setLayout(layout);
		final Label title = new Label(this, SWT.CENTER | SWT.WRAP);
		title.setText(content.title());
		final Font font = new Font(getDisplay(), getFont().getFontData()[0].getName(), 20, SWT.BOLD);
		title.setFont(font);
		title.addDisposeListener(event -> font.dispose());
		final GridData titleData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		titleData.widthHint = 560;
		title.setLayoutData(titleData);
		final String experiment = content.experiments().stream().filter(entry -> entry.name().equals(selection))
				.map(ExperimentMenu.Entry::label).findFirst().orElse(selection);
		if (content.experiments().size() > 1) {
			final FlatButton selector = FlatButton.menu(this, IGamaColors.OK, experiment).withHeight(24);
			selector.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			selector.setToolTipText(launching ? "Launching " + experiment : "Choose an experiment");
			selector.setEnabled(!launching);
			if (!launching) {
				final var menu = ExperimentMenu.create(selector, content.experiments(), next -> {
					selector.setText(content.experiments().stream().filter(entry -> entry.name().equals(next))
							.map(ExperimentMenu.Entry::label).findFirst().orElse(next));
					choose.accept(next);
					layout(true, true);
				});
				selector.setSelectionListener(event -> ExperimentMenu.open(menu, selector));
			}
		} else {
			final Label subtitle = new Label(this, SWT.CENTER | SWT.WRAP);
			subtitle.setText(experiment);
			subtitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}
		actionBar = new ToolBar(this, SWT.FLAT);
		final GridData actionData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		actionData.verticalIndent = 8;
		actionBar.setLayoutData(actionData);
		final ToolItem button = new ToolItem(actionBar, SWT.PUSH);
		final GamaIcon icon = GamaIcon.named(launching ? IGamaIcons.EXPERIMENT_STOP : IGamaIcons.EXPERIMENT_RUN);
		button.setImage(icon.image());
		button.setDisabledImage(icon.disabled());
		button.setToolTipText(launching ? "Cancel launch and return to Home" : "Run experiment");
		button.addListener(SWT.Selection, event -> {
			button.setEnabled(false);
			try {
				action.run();
			} finally {
				if (!button.isDisposed() && !launching) button.setEnabled(true);
			}
		});
		if (!content.description().isBlank()) {
			final Label description = new Label(this, SWT.CENTER | SWT.WRAP);
			description.setText(content.description());
			final GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
			data.widthHint = 560;
			data.verticalIndent = 14;
			description.setLayoutData(data);
		}
		if (content.image() != null) {
			try {
				final Image image = illustration(content.image());
				final Label illustration = new Label(this, SWT.NONE);
				illustration.setImage(image);
				final GridData data = new GridData(SWT.CENTER, SWT.CENTER, false, false);
				data.verticalIndent = 14;
				illustration.setLayoutData(data);
				illustration.addDisposeListener(event -> image.dispose());
			} catch (final RuntimeException e) {
				DEBUG.ERR("Unable to load launcher image " + content.image(), e);
				new Label(this, SWT.WRAP).setText("Unable to load the home page image: " + content.image());
			}
		}
	}

	private Image illustration(final Path path) {
		final Image original = new Image(getDisplay(), path.toString());
		final Rectangle size = original.getBounds();
		final double scale = Math.min(1d, Math.min(560d / size.width, 240d / size.height));
		if (scale == 1d) return original;
		try {
			return new Image(getDisplay(), original.getImageData().scaledTo(Math.max(1, (int) (size.width * scale)),
					Math.max(1, (int) (size.height * scale))));
		} finally {
			original.dispose();
		}
	}

	@Override
	public boolean setFocus() { return actionBar.setFocus(); }
}
