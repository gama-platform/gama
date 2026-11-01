package gama.export;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import gama.api.additions.GamaBundleLoader.LoadingStatus;

/** Installs startup text before activating the modeling application's bundles. */
public final class StartupSplash {

	private static final String INSTALLED = StartupSplash.class.getName();

	private StartupSplash() {}

	public static void install(final Shell splash) {
		if (splash == null || splash.isDisposed() || Boolean.getBoolean("exported_runner")
				|| Boolean.TRUE.equals(splash.getData(INSTALLED))) return;
		splash.setData(INSTALLED, Boolean.TRUE);
		final Display display = splash.getDisplay();
		final Color background = new Color(display, 24, 60, 85);
		final Color foreground = new Color(display, 204, 219, 214);
		final String family = display.getFontList("Helvetica Neue", true).length == 0
				? splash.getFont().getFontData()[0].getName() : "Helvetica Neue";
		final Font font = new Font(display, family, 14, SWT.NORMAL);
		// A foreign Cocoa shell has bottom-up coordinates; a full-size SWT container normalizes its children.
		final Composite content = new Composite(splash, SWT.NO_BACKGROUND);
		final var area = splash.getClientArea();
		content.setBounds(0, 0, area.width, area.height);
		final double scaleX = area.width / 900d;
		final double scaleY = area.height / 560d;
		final int width = Math.max(1, (int) Math.round(274 * scaleX));
		final Label text = new Label(content, SWT.WRAP);
		text.setBackground(background);
		text.setForeground(foreground);
		text.setFont(font);
		content.moveAbove(null);
		final Runnable paint = new Runnable() {
			private boolean dispatching;

			@Override
			public void run() {
				if (dispatching || text.isDisposed()) return;
				dispatching = true;
				try {
					// Cocoa presents drawing through its event loop, not Control.update().
					final long deadline = System.nanoTime() + 20_000_000L;
					for (int events = 0; events < 100 && !text.isDisposed() && !display.isDisposed()
							&& System.nanoTime() < deadline && display.readAndDispatch(); events++) {}
				} finally {
					dispatching = false;
				}
			}
		};
		final Consumer<String> listener = message -> {
			final Runnable update = () -> {
				if (text.isDisposed()) return;
				text.setText(message);
				final int height = text.computeSize(width, SWT.DEFAULT).y;
				text.setBounds((int) Math.round(60 * scaleX),
						(int) Math.round(280 * scaleY) - height / 2, width, height);
				text.redraw();
				text.update();
				paint.run();
			};
			if (Display.getCurrent() == display) {
				update.run();
			} else if (!display.isDisposed()) {
				display.asyncExec(update);
			}
		};
		text.addDisposeListener(event -> {
			LoadingStatus.removeListener(listener);
			font.dispose();
			foreground.dispose();
			background.dispose();
		});
		LoadingStatus.addListener(listener);
	}

}
