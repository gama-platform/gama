/*******************************************************************************************************
 *
 * AdaptiveColor.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.color;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import gama.api.runtime.scope.IScope;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;

/** A contextual foreground colour, resolved from the scene and defined overlay/control backgrounds. */
public final class AdaptiveColor implements IColor {
	public static final AdaptiveColor INSTANCE = new AdaptiveColor();

	private AdaptiveColor() {}

	@Override
	public Color getAWTColor() {
		throw new UnsupportedOperationException("#adaptive is only supported as the color: of overlay controls");
	}

	@Override
	public Integer alpha() { return 255; }

	@Override
	public IColor copy(final IScope scope) { return this; }

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) { return "#adaptive"; }

	@Override
	public String stringValue(final IScope scope) { return "adaptive"; }

	@Override
	public String toString() { return "color[adaptive]"; }

	@Override
	public IJsonValue serializeToJson(final IJson json) {
		return json.typedObject(getGamlType(), "adaptive", true);
	}

	/** A background in overlay-local pixels, with the same opacity used for rendering. */
	public record Background(Shape bounds, IColor color, double opacity) {}

	/** Retains the contrast choice for one label, with hysteresis to avoid flickering on midtones. */
	public static final class Contrast {
		private static final double THRESHOLD = Math.sqrt(.05 * 1.05) - .05;
		private static final double[] LINEAR = new double[256];

		static {
			for (int i = 0; i < LINEAR.length; i++) {
				final double value = i / 255d;
				LINEAR[i] = value <= .04045 ? value / 12.92 : Math.pow((value + .055) / 1.055, 2.4);
			}
		}

		private Boolean darkText;

		public synchronized IColor resolve(final BufferedImage scene, final Rectangle2D sceneBounds,
				final Rectangle2D textBounds, final Background... backgrounds) {
			final Rectangle2D visible = sceneBounds.createIntersection(textBounds);
			if (scene == null || visible.isEmpty()) {
				throw new IllegalArgumentException("Adaptive text needs visible pixels from the pre-overlay scene");
			}
			final double scaleX = scene.getWidth() / sceneBounds.getWidth();
			final double scaleY = scene.getHeight() / sceneBounds.getHeight();
			final int columns = Math.min(24, Math.max(1, (int) Math.ceil(visible.getWidth() * scaleX)));
			final int rows = Math.min(24, Math.max(1, (int) Math.ceil(visible.getHeight() * scaleY)));
			double luminance = 0;
			for (int row = 0; row < rows; row++) {
				final double sampleY = visible.getY() + visible.getHeight() * (row + .5) / rows;
				final int y = Math.clamp((int) ((sampleY - sceneBounds.getY()) * scaleY), 0, scene.getHeight() - 1);
				for (int column = 0; column < columns; column++) {
					final double sampleX = visible.getX() + visible.getWidth() * (column + .5) / columns;
					final int x = Math.clamp((int) ((sampleX - sceneBounds.getX()) * scaleX), 0, scene.getWidth() - 1);
					final int rgb = scene.getRGB(x, y);
					double red = rgb >> 16 & 255, green = rgb >> 8 & 255, blue = rgb & 255;
					for (final Background background : backgrounds) {
						if (background == null || !background.bounds().contains(sampleX, sampleY)) continue;
						final IColor color = background.color();
						final double alpha = color.alpha() / 255d * background.opacity();
						red = red * (1 - alpha) + color.red() * alpha;
						green = green * (1 - alpha) + color.green() * alpha;
						blue = blue * (1 - alpha) + color.blue() * alpha;
					}
					luminance += .2126 * LINEAR[(int) Math.round(red)] + .7152 * LINEAR[(int) Math.round(green)]
							+ .0722 * LINEAR[(int) Math.round(blue)];
				}
			}
			final double hysteresis = darkText == null ? 0 : darkText ? -.02 : .02;
			darkText = luminance / (rows * columns) > THRESHOLD + hysteresis;
			return darkText ? GamaColorFactory.BLACK : GamaColorFactory.WHITE;
		}
	}
}
