package gama.export;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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
		final Color background = new Color(display, 248, 246, 240);
		final Color foreground = new Color(display, 57, 66, 61);
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
		final Canvas text = new Canvas(content, SWT.DOUBLE_BUFFERED);
		text.setBackground(background);
		text.setForeground(foreground);
		text.setBounds((int) Math.round(60 * scaleX), (int) Math.round(200 * scaleY),
				width, (int) Math.round(160 * scaleY));
		final TextLayout messageLayout = new TextLayout(display);
		messageLayout.setFont(font);
		messageLayout.setWidth(width);
		// Measure and draw with the same layout instead of relying on native Label sizing at high DPI.
		text.addPaintListener(event -> {
			final var bounds = text.getClientArea();
			event.gc.setBackground(background);
			event.gc.fillRectangle(bounds);
			event.gc.setForeground(foreground);
			messageLayout.draw(event.gc, 0, Math.max(0, (bounds.height - messageLayout.getBounds().height) / 2));
		});
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
				messageLayout.setText(message);
				final int height = Math.max(text.getSize().y, messageLayout.getBounds().height);
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
			messageLayout.dispose();
			font.dispose();
			foreground.dispose();
			background.dispose();
		});
		LoadingStatus.addListener(listener);
	}

}
