/*******************************************************************************************************
 *
 * AbstractDisplayGraphics.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.display;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import gama.api.GAMA;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.ui.displays.IDisplayData;
import gama.api.ui.displays.IDisplaySurface;
import gama.api.ui.displays.IGraphics;
import gama.api.ui.layers.ILayer;
import gama.api.utils.geometry.IEnvelope;
import gama.api.utils.random.IRandom;
import gama.api.utils.random.RandomUtils;
import gama.core.outputs.layers.OverlayLayer;
import gama.dev.DEBUG;

/**
 * The Class AbstractDisplayGraphics.
 */
public abstract class AbstractDisplayGraphics implements IGraphics {

	/** The cached GC. */
	private static GraphicsConfiguration cachedGC;

	/**
	 * Gets the cached GC.
	 *
	 * @return the cached GC
	 */
	public static GraphicsConfiguration getCachedGC() {
		if (cachedGC == null) {
			DEBUG.OUT("Creating cached Graphics ConfigurationPreferenceStore");
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			DEBUG.OUT("Local Graphics Environment selected");
			GraphicsDevice gd = ge.getDefaultScreenDevice();
			DEBUG.OUT("Default Graphics Device selected");
			cachedGC = gd.getDefaultConfiguration();
			DEBUG.OUT("Default Graphics ConfigurationPreferenceStore selected");
		}
		return cachedGC;
	}

	/**
	 * Creates the compatible image.
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param forOpenGL
	 *            the for open GL
	 * @return the buffered image
	 */
	public static BufferedImage createCompatibleImage(final int width, final int height) {
		BufferedImage newImage = null;
		if (GAMA.isInHeadLessMode() || GraphicsEnvironment.isHeadless()) {
			newImage = new BufferedImage(width > 0 ? width : 1024, height > 0 ? height : 1024,
					BufferedImage.TYPE_INT_ARGB);
		} else {
			newImage = getCachedGC().createCompatibleImage(width, height);
		}
		return newImage;
	}

	/**
	 * To compatible image.
	 *
	 * @param image
	 *            the image
	 * @return the buffered image
	 */
	public static BufferedImage toCompatibleImage(final BufferedImage image) {
		// if image is already compatible and optimized for current system settings, simply return it
		if (GAMA.isInHeadLessMode() || GraphicsEnvironment.isHeadless()
				|| image.getColorModel().equals(getCachedGC().getColorModel()))
			return image;
		// image is not optimized, so create a new image that is
		final BufferedImage newImage =
				getCachedGC().createCompatibleImage(image.getWidth(), image.getHeight(), image.getTransparency());
		final Graphics2D g2d = (Graphics2D) newImage.getGraphics();
		// actually draw the image and dispose of context no longer needed
		g2d.drawImage(image, 0, 0, null);
		g2d.dispose();
		// return the new optimized image
		return newImage;
	}

	/** The rect. */
	protected final Rectangle2D rect = new Rectangle2D.Double(0, 0, 1, 1);

	/** The Constant origin. */
	protected static final IPoint origin = GamaPointFactory.create();

	/** The current layer alpha. */
	protected double currentLayerAlpha = 1;

	/** The data. */
	public IDisplayData data;

	/** The surface. */
	protected IDisplaySurface surface;

	/** The highlight. */
	public boolean highlight = false;

	/** The random number generator specific to this graphics. See Issue #3250. */
	private final IRandom random = new RandomUtils();

	/** The current layer. */
	protected ILayer currentLayer;

	@Override
	public void setDisplaySurface(final IDisplaySurface surface) {
		this.surface = surface;
		data = surface.getData();
	}

	@Override
	public boolean isNotReadyToUpdate() { return surface.isDisposed(); }

	@Override
	public void dispose() {
		currentLayer = null;
	}

	@Override
	public void beginHighlight() {
		highlight = true;
	}

	@Override
	public void endHighlight() {
		highlight = false;
	}

	@Override
	public void setAlpha(final double alpha) {
		// 1 means opaque ; 0 means transparent
		currentLayerAlpha = alpha;
	}

	/**
	 * X from model units to pixels.
	 *
	 * @param mu
	 *            the mu
	 * @return the double
	 */
	protected final double xFromModelUnitsToPixels(final double mu) {
		return getXOffsetInPixels() + getxRatioBetweenPixelsAndModelUnits() * mu /* + 0.5 */;
	}

	/**
	 * Y from model units to pixels.
	 *
	 * @param mu
	 *            the mu
	 * @return the double
	 */
	protected final double yFromModelUnitsToPixels(final double mu) {
		return getYOffsetInPixels() + getyRatioBetweenPixelsAndModelUnits() * mu;
	}

	@Override
	public double getAbsoluteRatioBetweenPixelsAndModelsUnits() {
		return Math.min(getyRatioBetweenPixelsAndModelUnits(), getxRatioBetweenPixelsAndModelUnits());

		// return Math.min(surface.getHeight() / data.getEnvHeight(), surface.getWidth() / data.getEnvWidth());
	}

	/**
	 * W from model units to pixels.
	 *
	 * @param mu
	 *            the mu
	 * @return the double
	 */
	protected final double wFromModelUnitsToPixels(final double mu) {
		return getxRatioBetweenPixelsAndModelUnits() * mu;
	}

	/**
	 * H from model units to pixels.
	 *
	 * @param mu
	 *            the mu
	 * @return the double
	 */
	protected final double hFromModelUnitsToPixels(final double mu) {
		return getyRatioBetweenPixelsAndModelUnits() * mu;
	}

	@Override
	public double getxRatioBetweenPixelsAndModelUnits() {
		if (currentLayer == null) return getDisplayWidth() / data.getEnvWidth();
		return currentLayer.getData().getSizeInPixels().x / data.getEnvWidth();
	}

	@Override
	public double getyRatioBetweenPixelsAndModelUnits() {
		if (currentLayer == null) return getDisplayHeight() / data.getEnvHeight();
		if (currentLayer instanceof OverlayLayer) return getxRatioBetweenPixelsAndModelUnits();
		return currentLayer.getData().getSizeInPixels().y / data.getEnvHeight();
	}

	@Override
	public double getXOffsetInPixels() {
		return currentLayer == null ? origin.getX() : currentLayer.getData().getPositionInPixels().getX();
	}

	@Override
	public double getYOffsetInPixels() {
		return currentLayer == null ? origin.getY() : currentLayer.getData().getPositionInPixels().getY();
	}

	@Override
	public boolean beginDrawingLayers() {
		return true;
	}

	@Override
	public void beginOverlay(final ILayer layer) {}

	@Override
	public void endOverlay() {}

	@Override
	public void beginDrawingLayer(final ILayer layer) {
		currentLayer = layer;

	}

	@Override
	public void endDrawingLayer(final ILayer layer) {
		currentLayer = null;
	}

	@Override
	public void endDrawingLayers() {}

	@Override
	public Double getZoomLevel() { return data.getZoomLevel(); }

	@Override
	public IDisplaySurface getSurface() { return surface; }

	@Override
	public int getViewWidth() { return surface.getWidth(); }

	@Override
	public int getViewHeight() { return surface.getHeight(); }

	@Override
	public int getDisplayWidth() { return (int) surface.getDisplayWidth(); }

	@Override
	public int getDisplayHeight() { return (int) surface.getDisplayHeight(); }

	/**
	 * Gets the layer width.
	 *
	 * @return the layer width
	 */
	@Override
	public int getLayerWidth() {
		return currentLayer == null ? getDisplayWidth() : currentLayer.getData().getSizeInPixels().x;
	}

	/**
	 * Gets the layer height.
	 *
	 * @return the layer height
	 */
	@Override
	public int getLayerHeight() {
		return currentLayer == null ? getDisplayHeight() : currentLayer.getData().getSizeInPixels().y;
	}

	@Override
	public IEnvelope getVisibleRegion() { return surface.getVisibleRegionForLayer(currentLayer); }

	@Override
	public IRandom getRandom() { return random; }

}