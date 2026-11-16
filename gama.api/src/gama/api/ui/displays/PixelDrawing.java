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
		final int w = image.getWidth(), h = image.getHeight(), size = Math.multiplyExact(w, h);
		final var raster = image.getRaster();
		if (image.getType() == BufferedImage.TYPE_INT_ARGB && !image.isAlphaPremultiplied()
				&& raster.getDataBuffer() instanceof DataBufferInt data && data.getNumBanks() == 1
				&& raster.getSampleModel() instanceof java.awt.image.SinglePixelPackedSampleModel sample
				&& sample.getScanlineStride() == w && raster.getSampleModelTranslateX() == 0
				&& raster.getSampleModelTranslateY() == 0) {
			final int offset = data.getOffset();
			return new PixelDrawing(w, h, Arrays.copyOfRange(data.getData(), offset, offset + size), false);
		}
		return new PixelDrawing(w, h, image.getRGB(0, 0, w, h, null, 0, w), false);
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
			image = new BufferedImage(cols, rows, BufferedImage.TYPE_INT_ARGB);
			final int[] dst = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
			if (pixels != null) {
				System.arraycopy(pixels, 0, dst, 0, dst.length);
			} else {
				for (int i = 0; i < dst.length; i++) { dst[i] = (int) numericPixels[i]; }
			}
		}
		return image;
	}

	@Override
	public boolean isAnimated() { return true; } // Scene-local GPU texture IDs must be rebound after retirement.
}
