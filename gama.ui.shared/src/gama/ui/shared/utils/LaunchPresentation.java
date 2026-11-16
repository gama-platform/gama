package gama.ui.shared.utils;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import gama.dev.DEBUG;
import gama.ui.application.workbench.ThemeHelper;
import gama.ui.shared.controls.FlatButton;
import gama.ui.shared.menus.ExperimentMenu;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.IGamaIcons;

/** Shared Home/launch card; choosing an experiment starts it directly when several are available. */
public final class LaunchPresentation extends Composite {

	public static final String SHELL_DATA_KEY = LaunchPresentation.class.getName();
	private static final String BACKGROUND_SURFACE_KEY = SHELL_DATA_KEY + ".background";

	public record Content(String title, String description, Path image, List<ExperimentMenu.Entry> experiments) {
		public Content {
			experiments = List.copyOf(experiments);
		}
	}

	/** Bounds are relative to the workbench client area, so launching does not move the Home card. */
	public record State(Content content, String selection, Rectangle bounds) {}

	private final Control actionControl;

	public static Color backgroundColor() {
		return ThemeHelper.backgroundColor(SWT.COLOR_WIDGET_BACKGROUND);
	}

	public static Color foregroundColor(final Color background) {
		return (GamaColors.isDark(background) ? GamaColors.get(235, 231, 219) : GamaColors.get(36, 40, 35))
				.color();
	}

	/** Installs a centered watermark; descendants can inherit it without obscuring it with solid backgrounds. */
	public static void installBackground(final Composite surface) {
		surface.setData(BACKGROUND_SURFACE_KEY, true);
		final Color surfaceBackground = backgroundColor();
		GamaColors.setBackground(surfaceBackground, surface);
		surface.setForeground(foregroundColor(surfaceBackground));
		final ImageDescriptor descriptor =
				AbstractUIPlugin.imageDescriptorFromPlugin("gama.ui.shared", "welcome/welcome.png");
		if (descriptor == null) throw new IllegalStateException("The GAMA welcome image is missing");
		final Image watermark = descriptor.createImage(false, surface.getDisplay());
		if (watermark == null) throw new IllegalStateException("Unable to load the GAMA welcome image");
		class Background implements Runnable {
			private Image image;

			@Override
			public void run() {
				final Rectangle area = surface.getClientArea();
				if (area.width <= 0 || area.height <= 0) return;
				final Image next = new Image(surface.getDisplay(), area.width, area.height);
				final GC gc = new GC(next);
				try {
					gc.setBackground(surface.getBackground());
					gc.fillRectangle(next.getBounds());
					gc.setInterpolation(SWT.HIGH);
					gc.setAlpha(28);
					final Rectangle source = watermark.getBounds();
					final double scale = 0.85 * Math.min((double) area.width / source.width,
							(double) area.height / source.height);
					final int width = Math.max(1, (int) (source.width * scale));
					final int height = Math.max(1, (int) (source.height * scale));
					gc.drawImage(watermark, 0, 0, source.width, source.height, (area.width - width) / 2,
							(area.height - height) / 2, width, height);
				} finally {
					gc.dispose();
				}
				surface.setBackgroundImage(next);
				if (image != null) image.dispose();
				image = next;
			}
		}
		final Background background = new Background();
		surface.addListener(SWT.Resize, event -> background.run());
		surface.addDisposeListener(event -> {
			if (background.image != null) background.image.dispose();
			watermark.dispose();
		});
		background.run();
	}

	public LaunchPresentation(final Composite parent, final Content content, final String selection,
			final boolean launching, final Consumer<String> choose, final Runnable action) {
		super(parent, SWT.NONE);
		final Color foreground = foregroundColor(parent.getBackground());
		setForeground(foreground);
		setFont(parent.getShell().getFont());
		setBackgroundMode(SWT.INHERIT_FORCE);
		final GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		// Balance the extra title spacing below the card to keep the launch action in place when centered.
		layout.marginBottom = 24;
		layout.verticalSpacing = 4;
		setLayout(layout);
		final Label title = new Label(this, SWT.CENTER | SWT.WRAP);
		title.setForeground(foreground);
		title.setText(content.title());
		final Font font = new Font(getDisplay(), getFont().getFontData()[0].getName(), 20, SWT.BOLD);
		title.setFont(font);
		title.addDisposeListener(event -> font.dispose());
		final GridData titleData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		titleData.widthHint = 560;
		title.setLayoutData(titleData);
		final String experiment = content.experiments().stream().filter(entry -> entry.name().equals(selection))
				.map(ExperimentMenu.Entry::label).findFirst().orElse(selection);
		final boolean multiple = content.experiments().size() > 1;
		final Label subtitle = new Label(this, SWT.CENTER | SWT.WRAP);
		subtitle.setForeground(foreground);
		subtitle.setText(multiple && !launching ? "" : experiment);
		final GridData subtitleData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		subtitleData.verticalIndent = 24;
		if (multiple) subtitleData.heightHint = 24;
		subtitle.setLayoutData(subtitleData);
		final Composite actions = new Composite(this, SWT.NONE);
		actions.setBackgroundMode(SWT.INHERIT_FORCE);
		final GridLayout actionsLayout = new GridLayout();
		actionsLayout.marginWidth = actionsLayout.marginHeight = 0;
		actionsLayout.verticalSpacing = 0;
		actions.setLayout(actionsLayout);
		final GridData actionData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		actionData.verticalIndent = 8;
		actions.setLayoutData(actionData);
		final ToolBar actionBar = new ToolBar(actions, SWT.FLAT);
		final GridData toolbarData = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		toolbarData.exclude = multiple && !launching;
		actionBar.setLayoutData(toolbarData);
		actionBar.setVisible(!toolbarData.exclude);
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
		if (multiple) {
			final var accent = ThemeHelper.isDark() ? GamaColors.get(139, 177, 195) : GamaColors.get(40, 81, 106);
			final FlatButton selector = FlatButton.menu(actions, accent, "Choose Experiment...")
					.withHeight(24).withHorizontalPadding(12);
			selector.setToolTipText("Choose an experiment to launch");
			final GridData selectorData = new GridData(SWT.CENTER, SWT.CENTER, true, true);
			selectorData.exclude = launching;
			selector.setLayoutData(selectorData);
			selector.setVisible(!launching);
			selector.setEnabled(!launching);
			// Reserve the same action row in Home and the overlay, even though their controls differ.
			final var selectorSize = selector.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			final var toolbarSize = actionBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			actionData.widthHint = Math.max(selectorSize.x, toolbarSize.x);
			actionData.heightHint = Math.max(selectorSize.y, toolbarSize.y);
			if (!launching) {
				final var menu = ExperimentMenu.create(selector, content.experiments(), next -> {
					selector.setEnabled(false);
					try {
						choose.accept(next);
						action.run();
					} finally {
						if (!selector.isDisposed()) selector.setEnabled(true);
					}
				});
				selector.setSelectionListener(event -> ExperimentMenu.open(menu, selector));
			}
			actionControl = launching ? actionBar : selector;
		} else {
			actionControl = actionBar;
		}
		if (!content.description().isBlank()) {
			final Label description = new Label(this, SWT.CENTER | SWT.WRAP);
			description.setForeground(foreground);
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
		Composite surface = parent;
		while (surface != null && surface.getData(BACKGROUND_SURFACE_KEY) == null) {
			surface = surface.getParent();
		}
		if (surface != null) installControlBackgrounds(this, surface);
	}

	/** Native labels on Cocoa paint solid backgrounds even when their composites inherit the watermark. */
	private static void installControlBackgrounds(final Control control, final Composite surface) {
		final Image[] background = new Image[1];
		final Listener update = event -> {
			if (control.isDisposed() || surface.isDisposed()) return;
			final Image source = surface.getBackgroundImage();
			final var size = control.getSize();
			if (source == null || size.x <= 0 || size.y <= 0) return;
			final var origin = surface.toControl(control.toDisplay(0, 0));
			final Image next = new Image(control.getDisplay(), size.x, size.y);
			final GC gc = new GC(next);
			try {
				gc.setBackground(surface.getBackground());
				gc.fillRectangle(next.getBounds());
				gc.drawImage(source, -origin.x, -origin.y);
			} finally {
				gc.dispose();
			}
			control.setBackgroundImage(next);
			if (background[0] != null) background[0].dispose();
			background[0] = next;
			control.redraw();
		};
		control.addListener(SWT.Move, update);
		control.addListener(SWT.Resize, update);
		for (Composite ancestor = control.getParent(); ancestor != surface; ancestor = ancestor.getParent()) {
			ancestor.addListener(SWT.Move, update);
		}
		surface.addListener(SWT.Resize, update);
		control.addDisposeListener(event -> {
			if (!surface.isDisposed()) surface.removeListener(SWT.Resize, update);
			for (Composite ancestor = control.getParent(); ancestor != surface; ancestor = ancestor.getParent()) {
				if (!ancestor.isDisposed()) ancestor.removeListener(SWT.Move, update);
			}
			if (background[0] != null) background[0].dispose();
		});
		update.handleEvent(null);
		if (control instanceof Composite composite) {
			for (final Control child : composite.getChildren()) {
				installControlBackgrounds(child, surface);
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
	public boolean setFocus() { return actionControl.setFocus(); }
}
