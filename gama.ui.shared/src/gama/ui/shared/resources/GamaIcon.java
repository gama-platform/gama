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
import static java.nio.file.Files.walk;
import static org.eclipse.core.runtime.FileLocator.toFileURL;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.awt.image.RescaleOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import gama.dev.DEBUG;
import gama.extension.image.GamaImage;
import gama.extension.image.ImageOperators;
import gama.ui.shared.resources.GamaColors.GamaUIColor;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class GamaIcon.
 */
public class GamaIcon {

	static {
		DEBUG.OFF();
	}

	/** The icon cache. */
	public static final Cache<String, GamaIcon> ICON_CACHE = CacheBuilder.newBuilder().build();

	/** The Constant MISSING. */
	static final String MISSING = "gaml" + File.separator + "_unknown";

	/** The Constant DISABLED_SUFFIX. */
	public static final String DISABLED_SUFFIX = "_disabled";

	/** The Constant TEMPLATES. */
	public static final String TEMPLATES = "templates" + File.separator;

	/** The Constant COLORS. */
	public static final String COLORS = "colors" + File.separator;

	/** The Constant PATH_TO_ICONS. */
	public static final Path PATH_TO_ICONS;

	static {
		// we need to use a tmp variable because PATH_TO_ICONS is final
		Path tmp = null;
		try {
			URL pngFolderURL = toFileURL(Platform.getBundle(IGamaIcons.PLUGIN_ID).getEntry(IGamaIcons.ICONS_PATH));
			tmp = Path.of(new URI(pngFolderURL.getProtocol(), pngFolderURL.getPath(), null).normalize());
		} catch (Exception e) {}
		PATH_TO_ICONS = tmp;
	}

	/**
	 * Preload icons.
	 *
	 * @param bundle
	 *            the bundle
	 * @throws IOException
	 */
	public static void preloadAllIcons() throws IOException {
		TIMER_WITH_EXCEPTIONS("GAMA", "Preloading icons", "done in",
				() -> walk(PATH_TO_ICONS).map(f -> PATH_TO_ICONS.relativize(f).toString())
						.filter(n -> n.endsWith(".png") && !n.contains("@") && !n.contains(DISABLED_SUFFIX))
						.forEach(f -> GamaIcon.named(f.replace(".png", ""))));
	}

	/**
	 * Returns the icon named after the path (eg "templates/square.template")
	 *
	 * @param path
	 *            the path
	 * @return the gama icon
	 */
	public static GamaIcon named(final String s) {

		try {
			// DEBUG.OUT("Looking for icon " + s);
			if (s != null) return ICON_CACHE.get(s, () -> new GamaIcon(s));
		} catch (ExecutionException e) {}
		return named(MISSING);
	}

	/**
	 * Creates a transparent icon of the desired and width (and 1px wide)
	 *
	 */

	public static GamaIcon ofSize(final int width, final int height) {
		final String name = "size" + width + "x" + height;
		try {
			return ICON_CACHE.get(name, () -> {
				DEBUG.OUT(name + " not found. Building it");
				GamaImage bi = GamaImage.ofDimensions(width, height, true);
				return new GamaIcon(name, bi);
			});
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Creates a color icon, either a round square or a circle
	 *
	 * @param gcolor
	 *            the gcolor
	 * @param square
	 *            the square
	 * @return the image
	 */

	public static GamaIcon ofColorWithAWTOld(final GamaUIColor gcolor, final boolean square) {
		String shape = square ? "square" : "circle";
		final String name = COLORS + shape + ".color." + String.format("%X", gcolor.gamaColor().getRGB());
		// DEBUG.OUT("Looking for " + name + ".png");
		try {
			return ICON_CACHE.get(name, () -> {
				// DEBUG.OUT(name + " not found. Building it");
				GamaImage bi = GamaImage.from(ImageIO.read(computeURL(TEMPLATES + shape + "_template")), true);
				Graphics2D gc = bi.createGraphics();
				gc.setColor(gcolor.gamaColor());
				gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				gc.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
						RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
				int size = bi.getWidth();
				if (square) {
					gc.fillRoundRect(size / 4, size / 4, size / 2 + 1, size / 2 + 1, 4, 4);
				} else {
					gc.fillOval(size / 4, size / 4, size / 2 + 1, size / 2 + 1);
				}
				gc.dispose();
				return new GamaIcon(name, bi);
			});
		} catch (Exception e) {
			return null;
		}

	}

	/**
	 * Of color with AWT.
	 *
	 * @param gcolor
	 *            the gcolor
	 * @param square
	 *            the square
	 * @return the gama icon
	 */
	public static GamaIcon ofColorWithAWT(final GamaUIColor gcolor, final boolean square) {
		String shape = square ? "square" : "circle";
		final String name = COLORS + shape + ".color." + String.format("%X", gcolor.gamaColor().getRGB());
		// DEBUG.OUT("Looking for " + name + ".png");
		try {
			return ICON_CACHE.get(name, () -> {
				// DEBUG.OUT(name + " not found. Building it");
				GamaImage bi = GamaImage.from(ImageIO.read(computeURL("spacer16")), true);
				Graphics2D gc = bi.createGraphics();
				gc.setColor(gcolor.gamaColor());
				gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				gc.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
						RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
				int size = bi.getWidth();
				if (square) {
					gc.fillRoundRect(4, 4, size - 8, size - 8, 4, 4);
				} else {
					gc.fillOval(4, 4, size - 8, size - 8);
				}
				gc.dispose();
				return new GamaIcon(name, bi);
			});
		} catch (Exception e) {
			return null;
		}

	}

	/**
	 * Of color with AWT.
	 *
	 * @param gcolor
	 *            the gcolor
	 * @param square
	 *            the square
	 * @return the gama icon
	 */
	public static GamaIcon Old2(final GamaUIColor gcolor, final boolean square) {
		String shape = square ? "square" : "circle";
		final String name = COLORS + shape + ".color." + String.format("%X", gcolor.gamaColor().getRGB());
		// DEBUG.OUT("Looking for " + name + ".png");
		try {
			return ICON_CACHE.get(name, () -> {
				// DEBUG.OUT(name + " not found. Building it");
				GamaImage bi = GamaImage.from(ImageIO.read(computeURL("spacer16")), true);
				Graphics2D gc = bi.createGraphics();
				gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				gc.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
						RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
				int size = bi.getWidth();
				if (square) {
					gc.setColor(java.awt.Color.white);
					gc.fillRoundRect(0, 0, size, size, 4, 4);
					gc.setColor(gcolor.gamaColor());
					gc.fillRoundRect(4, 4, size - 8, size - 8, 4, 4);
				} else {
					gc.setColor(java.awt.Color.white);
					gc.fillOval(0, 0, size, size);
					gc.setColor(gcolor.gamaColor());
					gc.fillOval(4, 4, size - 8, size - 8);
				}
				gc.dispose();
				return new GamaIcon(name, bi);
			});
		} catch (Exception e) {
			return null;
		}

	}

	/**
	 * Of color with SWT.
	 *
	 * @param gcolor
	 *            the gcolor
	 * @param square
	 *            the square
	 * @return the gama icon
	 */
	public static GamaIcon ofColorWithSWT(final GamaUIColor gcolor, final boolean square) {
		Image im = square ? createSquare(16, 4, gcolor) : createCircle(16, gcolor);
		String shape = square ? "square" : "circle";
		final String name = COLORS + shape + ".color." + String.format("%X", gcolor.gamaColor().getRGB());
		return new GamaIcon(name, im, new Image(WorkbenchHelper.getDisplay(), im, SWT.IMAGE_DISABLE));
	}

	/**
	 * Creates the rounded rectangle image.
	 *
	 * @param display
	 *            the display
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param cornerRadius
	 *            the corner radius
	 * @param red
	 *            the red
	 * @param green
	 *            the green
	 * @param blue
	 *            the blue
	 * @return the image
	 */
	private static Image createSquare(final int width, final int cornerRadius, final GamaUIColor gcolor) {
		Image src = named("spacer16").image();
		ImageData imageData = src.getImageData();
		// imageData.transparentPixel = imageData.getPixel(0, 0);
		// src.dispose();
		Image image = new Image(null, imageData);
		GC gc = new GC(image);
		try {
			Color fillColor = gcolor.active;
			gc.setBackground(GamaColors.system(SWT.COLOR_WHITE));
			gc.fillRoundRectangle(0, 0, width, width, cornerRadius, cornerRadius);
			gc.setBackground(fillColor);
			gc.fillRoundRectangle(4, 4, width - 8, width - 8, cornerRadius, cornerRadius);
		} finally {
			gc.dispose();
		}
		return image;
	}

	/**
	 * Creates the circle.
	 *
	 * @param width
	 *            the width
	 * @param gcolor
	 *            the gcolor
	 * @return the image
	 */
	private static Image createCircle(final int width, final GamaUIColor gcolor) {
		Image src = named("spacer16").image();
		ImageData imageData = src.getImageData();
		// imageData.transparentPixel = imageData.getPixel(0, 0);
		// src.dispose();
		Image image = new Image(null, imageData);
		GC gc = new GC(image);
		gc.setAdvanced(true);
		try {
			Color fillColor = gcolor.active;
			gc.setBackground(GamaColors.system(SWT.COLOR_WHITE));
			gc.fillOval(0, 0, width, width);
			gc.setBackground(fillColor);
			gc.fillOval(4, 4, width - 8, width - 8);
		} finally {
			gc.dispose();
		}
		return image;
	}

	/** The code. */
	final String code;

	/** The url. */
	final URL url, disabledUrl;

	/** The descriptor. */
	final ImageDescriptor descriptor, disabledDescriptor;

	/**
	 * Constructor for images loaded from a plugin
	 *
	 * @param c
	 *            the code
	 * @param p
	 *            the path (in the 'icons' folder)
	 * @param plugin
	 *            the id of the plugin in which the 'icons' folder resides
	 */
	private GamaIcon(final String c) {
		DEBUG.OUT("Creation of icon " + c, false);
		code = c;
		url = computeURL(code);
		DEBUG.OUT(" with URL " + url);
		disabledUrl = computeURL(code + DISABLED_SUFFIX);
		descriptor = ImageDescriptor.createFromURL(url);
		disabledDescriptor = ImageDescriptor.createFromURL(disabledUrl);
	}

	/**
	 * Instantiates a new gama icon directly from an image. We do not produce disabled versions
	 *
	 * @param name
	 *            the name
	 * @param im
	 *            the im
	 */
	private GamaIcon(final String path, final Image im, final Image disabled) {
		code = path;
		url = computeURL(code);
		disabledUrl = url;
		descriptor = ImageDescriptor.createFromImage(im);
		disabledDescriptor = ImageDescriptor.createFromImage(disabled);
	}

	/**
	 * Instantiates a new gama icon.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param path
	 *            the path
	 * @param im
	 *            the im
	 * @date 13 sept. 2023
	 */
	private GamaIcon(final String path, final GamaImage im) {
		this(path, toSWTImage(im), toDisabledSWTImage(im));
	}

	/**
	 * Creates a SWT image from a Java BufferedImage.
	 *
	 * @param bufferedImage
	 *            the image.
	 * @return returns a SWT image.
	 */
	public static Image toSWTImage(final GamaImage im) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ImageIO.write(im, "png", out);
		} catch (IOException e) {}
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		return new Image(Display.getCurrent(), new ImageData(in));
	}

	/**
	 * The Class DisabledFilter.
	 */
	private static class DisabledFilter extends RGBImageFilter {

		/** The min. */
		private final float min;

		/** The factor. */
		private final float factor;

		/**
		 * Instantiates a new disabled filter.
		 *
		 * @param min
		 *            the min
		 * @param max
		 *            the max
		 */
		DisabledFilter() {
			canFilterIndexColorModel = true;
			this.min = 160;
			this.factor = (255 - min) / 255f;
		}

		@Override
		public int filterRGB(final int x, final int y, final int rgb) {
			// Coefficients are from the sRGB color space:
			int gray = Math.min(255,
					(int) ((0.2125f * (rgb >> 16 & 0xFF) + 0.7154f * (rgb >> 8 & 0xFF) + 0.0721f * (rgb & 0xFF) + .5f)
							* factor + min));
			return rgb & 0xff000000 | gray << 16 | gray << 8 | gray << 0;
		}
	}

	/** The Constant filter. */
	static final DisabledFilter FILTER = new DisabledFilter();

	/**
	 * To buffered image.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param img
	 *            the img
	 * @return the buffered image
	 * @date 15 sept. 2023
	 */
	public static BufferedImage toBufferedImage(final java.awt.Image img) {
		if (img instanceof BufferedImage) return (BufferedImage) img;
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();
		return bimage;
	}

	/**
	 * Creates a SWT image from a Java BufferedImage.
	 *
	 * @param bufferedImage
	 *            the image.
	 * @return returns a SWT image.
	 */
	public static Image toDisabledSWTImage(final BufferedImage im) {
		ImageProducer prod = new FilteredImageSource(im.getSource(), FILTER);
		java.awt.Image gray = Toolkit.getDefaultToolkit().createImage(prod);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ImageIO.write(toBufferedImage(gray), "png", out);
		} catch (IOException e) {}
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		return new Image(Display.getCurrent(), new ImageData(in));
	}

	/** The hints. */
	RenderingHints HINTS = new RenderingHints(Map.of(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON,
			RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC, RenderingHints.KEY_RENDERING,
			RenderingHints.VALUE_RENDER_QUALITY));

	/**
	 * A {@link RescaleOp} used to make any input image 10% darker.
	 */
	RescaleOp OP_DARKER = new RescaleOp(0.9f, 0, HINTS);

	/**
	 * To checked SWT image.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param im
	 *            the im
	 * @return the image
	 * @date 15 sept. 2023
	 */
	public static Image toCheckedSWTImage(final GamaImage im) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ImageIO.write(ImageOperators.darker(null, im, 0.5), "png", out);
		} catch (IOException e) {}
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		return new Image(Display.getCurrent(), new ImageData(in));

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
	 * Image.
	 *
	 * @return the image
	 */
	private Image image(final String key, final Callable<Image> imageCreator) {
		Image image = JFaceResources.getImage(key);
		if (image == null) {
			try {
				image = imageCreator.call();
			} catch (Exception e) {}
			if (image == null) { image = named(MISSING).image(); }
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
		return image(url.toString(), () -> descriptor.createImage(false));
	}

	/**
	 * Disabled.
	 *
	 * @return the image
	 */
	public Image disabled() {
		return image(disabledUrl.toString(), () -> disabledDescriptor.createImage(false));
	}

	/**
	 * Checked.
	 *
	 * @return the image
	 */
	public Image checked() {
		return image(code + "_checked", () -> {
			GamaImage bi = GamaImage.from(ImageIO.read(url), true);
			return toCheckedSWTImage(bi);
		});
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
	 * @return the url
	 */
	public static URL computeURL(final String code) {
		IPath uriPath = new org.eclipse.core.runtime.Path("/plugin").append(IGamaIcons.PLUGIN_ID)
				.append(IGamaIcons.ICONS_PATH + code + ".png");
		try {
			URI uri = new URI("platform", null, uriPath.toString(), null);
			return uri.toURL();
		} catch (MalformedURLException | URISyntaxException e) {
			return computeURL(GamaIcon.MISSING);
		}
	}

}