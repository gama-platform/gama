/*******************************************************************************************************
 *
 * PixelDrawing.java, in gama.api, is part of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui.displays;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import gama.api.runtime.scope.IScope;
import gama.api.utils.interfaces.IImageProvider;

/** A packed-pixel snapshot. Image allocation/conversion is deferred until a renderer consumes it. */
public final class PixelDrawing implements IImageProvider {

	private static final AtomicLong IDS = new AtomicLong();
	private final String id = "display-pixels-" + IDS.incrementAndGet();
	private final int cols, rows;
	private final int[] pixels;
	private final double[] numericPixels;
	private BufferedImage image;

	public PixelDrawing(final int cols, final int rows, final int[] pixels) {
		this(cols, rows, pixels, true);
	}

	private PixelDrawing(final int cols, final int rows, final int[] pixels, final boolean copy) {
		if (cols <= 0 || rows <= 0 || pixels.length != Math.multiplyExact(cols, rows)) {
			throw new IllegalArgumentException("Invalid display pixel dimensions");
		}
		this.cols = cols;
		this.rows = rows;
		this.pixels = copy ? Arrays.copyOf(pixels, pixels.length) : pixels;
		numericPixels = null;
	}

	public PixelDrawing(final int cols, final int rows, final double[] pixels) {
		if (cols <= 0 || rows <= 0 || pixels.length != Math.multiplyExact(cols, rows)) {
			throw new IllegalArgumentException("Invalid display pixel dimensions");
		}
		this.cols = cols;
		this.rows = rows;
		this.pixels = null;
		numericPixels = Arrays.copyOf(pixels, pixels.length);
	}

	public static PixelDrawing fromImage(final BufferedImage image) {
		return new PixelDrawing(image.getWidth(), image.getHeight(),
				image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth()), false);
	}

	@Override
	public String getId() { return id; }

	@Override
	public int getCols(final IScope scope) { return cols; }

	@Override
	public int getRows(final IScope scope) { return rows; }

	@Override
	public synchronized BufferedImage getImage(final IScope scope, final boolean useCache) {
		if (image == null) {
			final int[] data;
			if (pixels != null) {
				data = pixels;
			} else {
				data = new int[numericPixels.length];
				for (int i = 0; i < data.length; i++) { data[i] = (int) numericPixels[i]; }
			}
			final ColorModel colors = ColorModel.getRGBdefault();
			final var raster = Raster.createWritableRaster(colors.createCompatibleSampleModel(cols, rows),
					new DataBufferInt(data, data.length), null);
			image = new BufferedImage(colors, raster, false, null);
		}
		return image;
	}

	@Override
	public boolean isAnimated() { return true; } // Scene-local GPU texture IDs must be rebound after retirement.
}
