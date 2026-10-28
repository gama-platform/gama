package gama.ui.shared.resources;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.internal.DPIUtil.ElementAtZoom;
import org.eclipse.swt.internal.NativeImageLoader;
import org.eclipse.swt.widgets.Display;

/** Standalone SWT check; on macOS, run with -XstartOnFirstThread from the repository root. */
public final class FindOverlayIconsTest {

	public static void main(final String[] args) throws Exception {
		Display display = new Display();
		ImageRegistry registry = new ImageRegistry(display);
		try {
			Class<?> images = Class.forName(
					"org.eclipse.ui.internal.findandreplace.overlay.FindReplaceOverlayImages");
			var getRegistry = images.getDeclaredMethod("getImageRegistry");
			getRegistry.setAccessible(true);
			var registryField = images.getDeclaredField("fgImageRegistry");
			registryField.setAccessible(true);
			registryField.set(null, registry);
			if (getRegistry.invoke(null) != registry)
				throw new AssertionError("Eclipse Find registry accessor changed");
			for (String[] mapping : new String[][] {
					{ "KEY_FIND_PREV", "select_prev", "search.previous" },
					{ "KEY_FIND_NEXT", "select_next", "search.next" },
					{ "KEY_SEARCH_ALL", "search_all", "search.all" } }) {
				var key = images.getDeclaredField(mapping[0]);
				key.setAccessible(true);
				if (!key.get(null).equals("org.eclipse.ui.workbench.texteditor.elcl." + mapping[1]))
					throw new AssertionError("Eclipse Find icon key changed: " + mapping[0]);
			}
			for (String icon : new String[] { "search.previous", "search.next", "search.all", "search" }) {
				for (String theme : new String[] { "light", "dark" }) {
					Path path = Path.of("gama.ui.shared/icons_svg", theme, "editor", icon + ".svg");
					for (int zoom : new int[] { 100, 200, 300 }) {
						try (var input = Files.newInputStream(path)) {
							var image = NativeImageLoader.load(new ElementAtZoom<>(input, 100),
									new ImageLoader(), zoom).getFirst().element();
							if (image.width != 25 * zoom / 100 || image.height != image.width)
								throw new AssertionError("Incorrect Find icon dimensions: " + path + " at " + zoom);
							if (image.getAlpha(image.width / 2, image.height / 2) == 0)
								throw new AssertionError("Empty Find icon: " + path);
							if (image.getAlpha(0, 0) != 0)
								throw new AssertionError("Find icon background must be transparent: " + path);
						}
					}
				}
			}
			System.out.println("Find overlay: Eclipse icon keys and light/dark SVGs at 100%, 200%, 300% passed");
		} finally {
			registry.dispose();
			display.dispose();
		}
	}
}
