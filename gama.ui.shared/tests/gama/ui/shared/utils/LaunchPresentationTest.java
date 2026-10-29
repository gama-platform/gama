package gama.ui.shared.utils;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;

import gama.ui.shared.controls.FlatButton;
import gama.ui.shared.menus.ExperimentMenu;

/** Runs in an initialized workbench so icons, themes and overlay display suppression are available. */
public final class LaunchPresentationTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static Object field(final LaunchingOverlay overlay, final String name) throws Exception {
		final Field field = LaunchingOverlay.class.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(overlay);
	}

	public static void runInWorkbench() throws Exception {
		final Display display = Display.getCurrent();
		final var path = Files.createTempFile("gama-launch-presentation-", ".png");
		final Image original = new Image(display, 1200, 600);
		try {
			final ImageLoader writer = new ImageLoader();
			final var pixels = original.getImageData();
			pixels.alphaData = new byte[pixels.width * pixels.height];
			java.util.Arrays.fill(pixels.alphaData, (byte) 128);
			writer.data = new org.eclipse.swt.graphics.ImageData[] { pixels };
			writer.save(path.toString(), SWT.IMAGE_PNG);
		} finally {
			original.dispose();
		}
		try {
			for (final boolean multiple : List.of(false, true)) {
				final Shell shell = new Shell(display);
				LaunchingOverlay overlay = null;
				try {
					shell.setSize(1000, 850);
					final var entries = multiple ? List.of(new ExperimentMenu.Entry("First", "First experiment", "gui"),
							new ExperimentMenu.Entry("Second", "Second experiment", "gui"))
							: List.of(new ExperimentMenu.Entry("First", "First experiment", "gui"));
					final var content = new LaunchPresentation.Content("My simulation", "Description stays in place",
							path, entries);
					final var home = new LaunchPresentation(shell, content, "First", false, selection -> {}, () -> {});
					final var size = home.computeSize(560, SWT.DEFAULT);
					home.setBounds(220, 100, size.x, size.y);
					shell.open();
					shell.layout(true, true);
					shell.setData(LaunchPresentation.SHELL_DATA_KEY, new LaunchPresentation.State(content, "First", home.getBounds()));
					final CountDownLatch canceled = new CountDownLatch(1);
					overlay = new LaunchingOverlay(shell, "model", "First", null, null, canceled::countDown);
					overlay.show();
					final var launching = (LaunchPresentation) field(overlay, "presentation");
					final var overlayShell = (Shell) field(overlay, "overlayShell");
					final var position = overlayShell.toControl(launching.toDisplay(0, 0));
					check(launching != null && new Rectangle(position.x, position.y, launching.getSize().x,
							launching.getSize().y).equals(home.getBounds()),
							"Launching must keep the Home presentation at exactly the same bounds");
					compare(home, launching);
					final var toolbar = java.util.Arrays.stream(launching.getChildren()).filter(ToolBar.class::isInstance)
							.map(ToolBar.class::cast).findFirst().orElseThrow();
					check(toolbar.getItem(0).getToolTipText().equals("Cancel launch and return to Home"),
							"Only the play action changes to cancel during launch");
					final var console = (Control) field(overlay, "consoleText");
					check(console.getBounds().y >= position.y + launching.getSize().y,
							"Launch output must not cover the presentation");
					shell.setSize(820, 400);
					shell.notifyListeners(SWT.Resize, null);
					check(launching.getBounds().width <= shell.getClientArea().width,
							"Keep the presentation inside the resized window");
					final var viewport = (org.eclipse.swt.custom.ScrolledComposite) launching.getParent();
					check(viewport.getVerticalBar().getVisible()
							&& viewport.getBounds().y + viewport.getSize().y <= console.getBounds().y
							&& console.getSize().y > 0, "Scroll long presentations instead of covering or hiding launch output");
					toolbar.getItem(0).notifyListeners(SWT.Selection, null);
					check(canceled.await(5, TimeUnit.SECONDS), "Cancel must still execute the supplied action");
				} finally {
					if (overlay != null) overlay.hide();
					shell.dispose();
				}
			}
			final Shell normal = new Shell(display);
			final var overlay = new LaunchingOverlay(normal, "model", "experiment", null, null, () -> {});
			try {
				normal.setSize(800, 600);
				normal.open();
				overlay.show();
				check(field(overlay, "presentation") == null, "Normal modeling launches must retain their existing overlay");
			} finally {
				overlay.hide();
				normal.dispose();
			}
			System.out.println("Shared Home/launch presentation, image scaling, geometry and cancel checks passed");
		} finally {
			Files.delete(path);
		}
	}

	private static void compare(final Composite home, final Composite launching) {
		final Control[] before = home.getChildren();
		final Control[] after = launching.getChildren();
		check(before.length == after.length, "Home and launching must use the same presentation structure");
		for (int i = 0; i < before.length; i++) {
			check(before[i].getClass() == after[i].getClass() && before[i].getBounds().equals(after[i].getBounds()),
					"Title, experiment, controls, description and illustration must not move");
			check(before[i].getBackground().equals(after[i].getBackground())
					&& java.util.Arrays.equals(before[i].getFont().getFontData(), after[i].getFont().getFontData()),
					"Share theme colors and typography");
			if (before[i] instanceof Label label) {
				final Label next = (Label) after[i];
				check(label.getText().equals(next.getText()), "Keep the title, experiment and description unchanged");
				if (label.getImage() != null) {
					final Rectangle bounds = label.getImage().getBounds();
					check(bounds.equals(next.getImage().getBounds()) && bounds.width <= 560 && bounds.height <= 240,
							"Use the same bounded illustration on both screens");
					check(label.getImage().getImageData().getAlpha(0, 0) == 128,
							"Scaling must preserve illustration transparency in both themes");
				}
			} else if (before[i] instanceof FlatButton selector) {
				check(selector.getText().equals(((FlatButton) after[i]).getText()), "Keep the experiment selector label");
			}
		}
	}
}
