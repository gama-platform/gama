/*******************************************************************************************************
 *
 * GamaIcon.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.resources;

import static gama.dev.DEBUG.TIMER_WITH_EXCEPTIONS;
import static org.eclipse.core.runtime.FileLocator.toFileURL;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.internal.DPIUtil.ElementAtZoom;
import org.eclipse.swt.internal.NativeImageLoader;
import org.osgi.framework.Bundle;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import gama.core.util.GamaColor;
import gama.dev.DEBUG;
import gama.ui.application.workbench.ThemeHelper;

/**
 * The Class GamaSVGIcon.
 */
public class GamaIcon implements IGamaIcons {

	static {
		DEBUG.OFF();
	}

	/** The icon cache. */
	private static final Cache<String, GamaIcon> ICON_CACHE = CacheBuilder.newBuilder().build();

	/** The Constant PATH_TO_ICONS. */
	private static final Path PATH_TO_ICONS;

	static {
		// we need to use a tmp variable because PATH_TO_ICONS is final
		Path tmp = null;
		try {
			URL folderURL = toFileURL(Platform.getBundle(PLUGIN_ID).getEntry(ICONS_PATH));
			tmp = Path.of(new URI(folderURL.getProtocol(), folderURL.getPath(), null).normalize());
		} catch (Exception e) {}
		PATH_TO_ICONS = tmp;
	}

	/**
	 * Preload all icons.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void preloadAllIcons() throws IOException {
		TIMER_WITH_EXCEPTIONS("GAMA", "Preloading icons", "done in", () -> {
			if (PATH_TO_ICONS == null) return;
			Files.walkFileTree(PATH_TO_ICONS, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
					if (attrs.isRegularFile()) {
						String s = FilenameUtils.separatorsToUnix(PATH_TO_ICONS.relativize(file).toString());
						if (FilenameUtils.isExtension(s, "svg")) { named(FilenameUtils.removeExtension(s)); }
					}
					return FileVisitResult.CONTINUE;
				}
			});
		});
	}

	/**
	 * Named.
	 *
	 * @param s
	 *            the s
	 * @return the gama SVG icon
	 */
	public static GamaIcon named(final String s) {
		try {
			if (s != null) return ICON_CACHE.get(s, () -> new GamaIcon(s));
		} catch (ExecutionException e) {}
		if (MISSING.equals(s)) return null;
		return named(MISSING);
	}

	/**
	 * Of size.
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @return the gama SVG icon
	 */
	public static GamaIcon ofSize(final int width, final int height) {
		final String name = "size" + width + "x" + height;
		try {
			return ICON_CACHE.get(name, () -> {
				String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 " + width + " " + height
						+ "\" width=\"" + width + "\" height=\"" + height + "\"></svg>";
				return new GamaIcon(name, svg);
			});
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Of color.
	 *
	 * @param gcolor
	 *            the gcolor
	 * @return the gama SVG icon
	 */
	public static GamaIcon ofColor(final GamaColor gcolor) {
		String name = COLOR_PATH + "circle.color." + String.format("%X", gcolor.getRGB()) + ".24";
		try {
			return ICON_CACHE.get(name, () -> {
				String hex = String.format("#%02x%02x%02x", gcolor.getRed(), gcolor.getGreen(), gcolor.getBlue());
				String stroke = ThemeHelper.isDark() ? "#E3E6E1" : "gray";
				String svg =
						"<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 24 24\" width=\"24\" height=\"24\">"
								+ "<circle cx=\"12.5\" cy=\"12.5\" r=\"7.5\" fill=\"" + hex + "\" stroke=\"" + stroke
								+ "\" stroke-width=\"0.3\"/>" + "</svg>";
				return new GamaIcon(name, svg);
			});
		} catch (Exception e) {
			return null;
		}
	}

	/** The code. */
	final String code;

	/** The descriptor. */
	final SVGImageDescriptor descriptor, disabledDescriptor;

	/**
	 * Instantiates a new gama SVG icon.
	 *
	 * @param code
	 *            the code
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private GamaIcon(final String code) throws IOException {
		this.code = code;
		URL url = computeURL(code);
		if (url == null) throw new IOException("Icon not found: " + code);
		String svgContent = "";
		try (InputStream in = url.openStream()) {
			svgContent = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
		this.descriptor = new SVGImageDescriptor(svgContent);
		this.disabledDescriptor = new SVGImageDescriptor(createDisabledSVG(svgContent));
	}

	/**
	 * Instantiates a new gama SVG icon.
	 *
	 * @param code
	 *            the code
	 * @param content
	 *            the content
	 */
	private GamaIcon(final String code, final String content) {
		this.code = code;
		String svgContent = content;
		this.descriptor = new SVGImageDescriptor(svgContent);
		this.disabledDescriptor = new SVGImageDescriptor(createDisabledSVG(svgContent));
	}

	/**
	 * Creates the disabled SVG.
	 *
	 * @param original
	 *            the original
	 * @return the string
	 */
	private String createDisabledSVG(final String original) {
		// We inject opacity and a grayscale filter (if supported by the renderer)
		// into the root svg tag
		return original.replace("<svg ", "<svg opacity=\"0.5\" style=\"filter:grayscale(100%);\" ");
	}

	/**
	 * Creates the darker SVG.
	 *
	 * @param original
	 *            the original
	 * @return the string
	 */
	private String createDarkerSVG(final String original) {
		int endTag = original.lastIndexOf("</svg>");
		if (endTag != -1) return original.substring(0, endTag)
				+ "<rect width=\"100%\" height=\"100%\" fill=\"black\" fill-opacity=\"0.5\"/></svg>";
		return original;
	}

	/**
	 * Image.
	 *
	 * @param key
	 *            the key
	 * @param imageCreator
	 *            the image creator
	 * @return the image
	 */
	private Image image(final String key, final Callable<Image> imageCreator) {
		Image image = JFaceResources.getImage(key);
		if (image == null) {
			try {
				image = imageCreator.call();
			} catch (Exception e) {}
			if (image == null && !MISSING.equals(key)) { image = named(MISSING).image(); }
			if (JFaceResources.getImageRegistry().get(key) == null) {
				JFaceResources.getImageRegistry().put(key, image);
			}
		}
		return image;
	}

	/**
	 * Image.
	 *
	 * @return the image
	 */
	public Image image() {
		return image(code, () -> descriptor.createImage(false));
	}

	/**
	 * Disabled.
	 *
	 * @return the image
	 */
	public Image disabled() {
		return image(code + "_disabled", () -> disabledDescriptor.createImage(false));
	}

	/**
	 * Checked.
	 *
	 * @return the image
	 */
	public Image checked() {
		return image(code + "_checked", () -> {
			String darker = createDarkerSVG(descriptor.data);
			return new SVGImageDescriptor(darker).createImage();
		});
	}

	/**
	 * Descriptor.
	 *
	 * @return the image descriptor
	 */
	public ImageDescriptor descriptor() {
		return descriptor;
	}

	/**
	 * Disabled descriptor.
	 *
	 * @return the image descriptor
	 */
	public ImageDescriptor disabledDescriptor() {
		return disabledDescriptor;
	}

	/**
	 * Gets the code.
	 *
	 * @return the code
	 */
	public String getCode() { return code; }

	/**
	 * Compute URL.
	 *
	 * @param code
	 *            the code
	 * @return the url
	 */
	public static URL computeURL(final String code) {
		Bundle bundle = Platform.getBundle(PLUGIN_ID);
		URL url = bundle.getEntry(ICONS_PATH + code + ".svg");
		if (url == null && !MISSING.equals(code)) return computeURL(MISSING);
		return url;
	}

	/**
	 * Exist.
	 *
	 * @param code
	 *            the code
	 * @return true, if successful
	 */
	public static boolean exist(final String code) {
		if (ICON_CACHE.getIfPresent(code) != null) return true;
		return computeURL(code) != null;
	}

	/**
	 * The Class SVGImageDescriptor.
	 */
	private static class SVGImageDescriptor extends ImageDescriptor {

		/** The data. */
		private final String data;

		/**
		 * Instantiates a new SVG image descriptor.
		 *
		 * @param data
		 *            the data
		 */
		SVGImageDescriptor(final String data) {
			this.data = data;
		}

		@Override
		public ImageData getImageData(final int zoom) {
			return NativeImageLoader
					.load(new ElementAtZoom<>(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)), 100),
							new ImageLoader(), zoom)
					.get(0).element();
		}

	}

}
