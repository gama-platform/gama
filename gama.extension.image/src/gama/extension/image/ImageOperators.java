/*******************************************************************************************************
 *
 * ImageOperators.java, in gama.extension.image, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.image;

import static gama.extension.image.ImageHelper.apply;
import static gama.extension.image.ImageHelper.resize;
import static gama.extension.image.ImageHelper.rotate;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.RescaleOp;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.no_test;
import gama.annotations.no_fuzz_test;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.tests;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.api.GAMA;
import gama.api.gaml.types.IType;
import gama.api.kernel.PlatformAgent;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.types.geometry.IPoint;
import gama.api.types.matrix.GamaMatrixFactory;
import gama.api.types.matrix.IMatrix;
import gama.api.ui.IOutput;
import gama.api.ui.displays.IDisplaySurface;
import gama.api.utils.server.MessageType;
import gama.core.outputs.LayeredDisplayOutput;
import gama.core.util.matrix.GamaIntMatrix;
import gama.extension.image.ImageHelper.Mode;
import gama.extension.image.ImageHelper.TransferableImage;

/**
 * The Class ImageOperators. largely inspired from imgscalr library
 * (https://github.com/rkalla/imgscalr/blob/master/src/main/java/org/imgscalr/Scalr.java)
 *
 * @author Riyad Kalla (software@thebuzzmedia.com)
 */
public class ImageOperators implements ImageConstants {

	/**
	 * Snapshot.
	 *
	 * @param scope
	 *            the scope
	 * @param displayName
	 *            the display name
	 * @return the gama image
	 */
	@no_fuzz_test ("acts on the outside world (files, network, clipboard, user interface, shell...)")
	@operator (
			value = "snapshot",
			can_be_const = false)
	@doc ("""
			Takes a snapshot of the display whose name is passed in parameter and returns the image. \
			The search for the display begins in the current agent's simulation and, if not found, its experiment. \
			Returns nil if no display can be found or the snapshot cannot be taken.""")
	@no_test
	public static GamaImage snapshot(final IScope scope, final String displayName) {
		return snapshot(scope, scope.getAgent(), displayName);
	}

	/**
	 * Snapshot.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @param displayName
	 *            the display name
	 * @return the gama image
	 */

	/**
	 * Snapshot.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @param displayName
	 *            the display name
	 * @return the gama image
	 */
	@no_fuzz_test ("acts on the outside world (files, network, clipboard, user interface, shell...)")
	@operator (
			value = "snapshot",
			can_be_const = false)
	@doc ("""
			Takes a snapshot of the display whose name is passed in parameter and returns the image. \
			The search for the display begins in the agent passed in parameter and, if not found, its experiment. The size of the snapshot will be that of the view\
			Returns nil if no display can be found or the snapshot cannot be taken.""")
	@no_test
	public static GamaImage snapshot(final IScope scope, final IAgent exp, final String displayName) {
		if (exp == null) return null;
		ITopLevelAgent agentWithOutputs;
		if (exp instanceof ITopLevelAgent top) {
			agentWithOutputs = top;
		} else {
			agentWithOutputs = exp.getTopLevelHost();
		}
		IOutput output = null;
		while (agentWithOutputs != null && output == null) {
			output = agentWithOutputs.getOutputManager().getOutputWithOriginalName(displayName);
			agentWithOutputs = agentWithOutputs.getTopLevelHost();
		}
		if (!(output instanceof LayeredDisplayOutput ldo)) return null;
		IDisplaySurface surface = ldo.getSurface();
		return SnapshotMaker.getInstance().captureImage(surface, null);
	}

	/**
	 * Snapshot.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @param displayName
	 *            the display name
	 * @return the gama image
	 */
	@no_fuzz_test ("acts on the outside world (files, network, clipboard, user interface, shell...)")
	@operator (
			value = "snapshot",
			can_be_const = false)
	@doc ("""
			Takes a snapshot of the display whose name is passed in parameter and returns the image. \
			The search for the display begins in the agent passed in parameter and, if not found, its experiment. A custom size (a point representing width x height) can be given \
			Returns nil if no display can be found or the snapshot cannot be taken.""")
	@no_test
	public static GamaImage snapshot(final IScope scope, final IAgent exp, final String displayName,
			final IPoint customDimensions) {
		if (exp == null) return null;
		ITopLevelAgent agentWithOutputs;
		if (exp instanceof ITopLevelAgent top) {
			agentWithOutputs = top;
		} else {
			agentWithOutputs = exp.getTopLevelHost();
		}
		IOutput output = null;
		while (agentWithOutputs != null && output == null) {
			output = agentWithOutputs.getOutputManager().getOutputWithOriginalName(displayName);
			agentWithOutputs = agentWithOutputs.getTopLevelHost();
		}
		if (!(output instanceof LayeredDisplayOutput ldo)) return null;
		IDisplaySurface surface = ldo.getSurface();
		return SnapshotMaker.getInstance().captureImage(surface, customDimensions);
	}

	/**
	 * Img to base 64 string.
	 *
	 * @param img
	 *            the img
	 * @param formatName
	 *            the format name
	 * @return the string
	 */
	public static String imgToBase64String(final GamaImage img, final String formatName) {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			ImageIO.write(img, formatName, os);
			return Base64.getEncoder().encodeToString(os.toByteArray());
		} catch (final IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}

	/**
	 * Send image websocket.
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @param format
	 *            the format
	 * @return the gama image
	 */
	@no_fuzz_test ("acts on the outside world (files, network, clipboard, user interface, shell...)")
	@operator (
			value = "send_image_to_websocket",
			can_be_const = false)
	@doc ("Send the given image to the websocket in Base64 using the given format.")
	@no_test
	public static GamaImage sendImageWebsocket(final IScope scope, final GamaImage image, final String format) {

		PlatformAgent pa = (PlatformAgent) GAMA.getPlatformAgent();

		pa.sendMessage(scope, imgToBase64String(image, format), MessageType.SimulationImage);
		return image;
	}

	/**
	 * Send image websocket.
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @return the gama image
	 */
	@no_fuzz_test ("acts on the outside world (files, network, clipboard, user interface, shell...)")
	@operator (
			value = "send_image_to_websocket",
			can_be_const = false)
	@doc ("Send the given image to the websocket using Base64 assuming the format is png.")
	@no_test
	public static GamaImage sendImageWebsocket(final IScope scope, final GamaImage image) {
		return sendImageWebsocket(scope, image, "png");
	}

	/**
	 * Grayscale.
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @return the gama image
	 */
	@operator ("grayscale")
	@doc ("Used to convert any image to a grayscale color palette and return it. The original image is left untouched")
	@no_test
	@tests ({
			@test ("image img <- image(4, 4, #red); image gray <- grayscale(img); gray != nil"),
			@test ("image img2 <- image(4, 4, #red); image gray2 <- grayscale(img2); gray2.width = 4"),
			@test ("image img3 <- image(4, 4, #red); image gray3 <- grayscale(img3); gray3.height = 4"),
			@test ("image red_image <- image(4, 2, #red); rgb grey <- rgb(matrix(grayscale(red_image))[0, 0]); grey.red = grey.green and grey.green = grey.blue"),
			@test ("image red_image2 <- image(4, 2, #red); rgb grey2 <- rgb(matrix(grayscale(red_image2))[0, 0]); grey2.red > 0 and grey2.red < 255")
	})
	public static GamaImage grayscale(final IScope scope, final GamaImage image) {
		try {
			return apply(image, OP_GRAYSCALE);
		} catch (Exception e) {
			return image;
		}
	}

	/**
	 * Darker.
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @return the gama image
	 */
	@operator (IKeyword.DARKER)
	@doc ("Used to return an image 10% darker. This operation can be applied multiple times in a row if greater than 10% changes in brightness are desired.")
	@no_test
	@tests ({
			@test ("image red_image <- image(4, 2, #red); rgb darkened <- rgb(matrix(darker(red_image, 0.5))[0, 0]); rgb slightly_darkened <- rgb(matrix(darker(red_image))[0, 0]); slightly_darkened.red < 255 and slightly_darkened.red > darkened.red")
	})
	public static GamaImage darker(final IScope scope, final GamaImage image) {
		try {
			return apply(image, OP_DARKER);
		} catch (Exception e) {
			return image;
		}
	}

	/**
	 * Darker.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @param percentage
	 *            the percentage
	 * @return the gama image
	 * @date 15 sept. 2023
	 */
	@operator (IKeyword.DARKER)
	@doc ("Used to return an image darker by a percentage (between 0 - no change - and 1 - 100% darker). If the percentage is below zero or above 1, returns the image untouched")
	@no_test
	@tests ({
			@test ("image img <- image(4, 4, #red); image drk <- darker(img, 0.5); drk != nil"),
			@test ("image img2 <- image(4, 4, #red); image drk2 <- darker(img2, 0.5); drk2.width = 4"),
			@test ("image red_image <- image(4, 2, #red); rgb darkened <- rgb(matrix(darker(red_image, 0.5))[0, 0]); darkened = rgb(127, 0, 0)")
	})
	public static GamaImage darker(final IScope scope, final GamaImage image, final double percentage) {
		try {
			if (percentage < 0 || percentage > 1) return image;
			float scale = (float) (1f - percentage);
			return apply(image, new RescaleOp(scale, 0, HINTS));
		} catch (Exception e) {
			return image;
		}
	}

	/**
	 * Brigther.
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @return the gama image
	 */
	@operator (IKeyword.BRIGHTER)
	@doc ("Used to return an image 10% brigther. This operation can be applied multiple times in a row if greater than 10% changes in brightness are desired.")
	@no_test
	@tests ({
			@test ("image img <- image(4, 4, #red); image brt <- brighter(img); brt != nil"),
			@test ("image img2 <- image(4, 4, #red); image brt2 <- brighter(img2); brt2.width = 4"),
			@test ("rgb brightened <- rgb(matrix(brighter(image(4, 2, rgb(100, 100, 100))))[0, 0]); brightened.red > 100"),
			@test ("rgb brightened2 <- rgb(matrix(brighter(image(4, 2, rgb(100, 100, 100))))[0, 0]); brightened2.red = brightened2.green and brightened2.green = brightened2.blue")
	})
	public static GamaImage brigther(final IScope scope, final GamaImage image) {
		try {
			return apply(image, OP_BRIGHTER);
		} catch (Exception e) {
			return image;
		}
	}

	/**
	 * Brigther.
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @param percentage
	 *            the percentage
	 * @return the gama image
	 */
	@operator (IKeyword.BRIGHTER)
	@doc ("Used to return an image brighter by a percentage (between 0 - no change - and 1 - 100% brighter). If the percentage is below zero or above 1, returns the image untouched")
	@no_test
	public static GamaImage brigther(final IScope scope, final GamaImage image, final double percentage) {
		try {
			if (percentage < 0 || percentage > 1) return image;
			float scale = (float) percentage;
			return apply(image, new RescaleOp(1f + scale, 0, HINTS));
		} catch (Exception e) {
			return image;
		}
	}

	/**
	 * Antialiased.
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @return the gama image
	 */
	@operator ("antialiased")
	@doc ("Application of a very light blur kernel that acts like an anti-aliasing filter when applied to an image. This operation can be applied multiple times in a row if greater.")
	@no_test
	public static GamaImage antialiased(final IScope scope, final GamaImage image) {
		try {
			return apply(image, OP_ANTIALIAS);
		} catch (Exception e) {
			return image;
		}
	}

	/**
	 * Strong antialiased.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @return the gama image
	 * @date 6 oct. 2023
	 */
	@operator ("antialiased")
	@doc ("Application of a very light blur kernel that acts like an anti-aliasing filter when applied to an image. If the last argument is > 0,  applies the filter the equivalent number of times. If it is equal or smaller than zero, the image is returned untouched")
	@no_test
	public static GamaImage antialiased(final IScope scope, final GamaImage image, final int count) {
		try {
			return apply(image, OP_ANTIALIAS, count);
		} catch (Exception e) {
			return image;
		}
	}

	/**
	 * Scaled by.
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @param scale
	 *            the scale
	 * @return the gama image
	 */
	@operator ("*")
	@doc ("Applies a proportional scaling ratio to the image passed in parameter and returns a new scaled image. "
			+ "A ratio of 0 will return nil, a ratio of 1 will return the original image. Automatic scaling and resizing methods are used. The original image is left untouched")
	@no_test
	public static GamaImage scaled_by(final IScope scope, final GamaImage image, final Double scale) {
		if (scale == 0d) return null;
		if (scale == 1d) return image;
		int newWidth = (int) Math.round(image.getWidth() * scale);
		int newHeight = (int) Math.round(image.getHeight() * scale);
		return resize(image, Mode.FIT_TO_WIDTH, newWidth, newHeight);
	}

	/**
	 * With width
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @param scale
	 *            the scale
	 * @return the gama image
	 */
	@operator ("with_width")
	@doc ("Applies a proportional scaling to the image passed in parameter to  return a new scaled image with the corresponding width. "
			+ "A width of 0 will return nil, a width equal to the width of the image will return the original image. Automatic scaling and resizing methods are used. The original image is left untouched")
	@no_test
	@tests ({
			// giving one dimension keeps the proportions
			@test ("image red_image <- image(4, 2, #red); with_width(red_image, 8).height = 4")
	})
	public static GamaImage with_width(final IScope scope, final GamaImage image, final Integer width) {
		return image == null || width <= 0d ? null : width == image.getWidth() ? image
				: resize(image, Mode.FIT_TO_WIDTH, width, width);
	}

	/**
	 * With width.
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @param height
	 *            the width
	 * @return the gama image
	 */
	@operator ("with_height")
	@doc ("Applies a proportional scaling to the image passed in parameter to return a new scaled image with the corresponding height. "
			+ "A height of 0 will return nil, a height equal to the height of the image will return the original image. Automatic scaling and resizing methods are used. The original image is left untouched")
	@no_test
	@tests ({
			@test ("image red_image <- image(4, 2, #red); with_height(red_image, 4).width = 8")
	})
	public static GamaImage with_height(final IScope scope, final GamaImage image, final Integer height) {
		return image == null || height <= 0d ? null : height == image.getHeight() ? image
				: resize(image, Mode.FIT_TO_HEIGHT, height, height);
	}

	/**
	 * With size.
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @return the gama image
	 */
	@operator ("with_size")
	@doc ("Applies a non-proportional scaling to the image passed in parameter to return a new scaled image with the corresponding width and height. "
			+ "A height of 0 or a width of 0 will return nil. If the width and height parameters are repectively equal to the width and height of the original image, it is returned. Automatic scaling and resizing methods are used. The original image is left untouched")
	@no_test
	@tests ({
			@test ("image red_image <- image(4, 2, #red); image resized <- with_size(red_image, 8, 4); resized.width = 8"),
			@test ("image red_image2 <- image(4, 2, #red); image resized2 <- with_size(red_image2, 8, 4); resized2.height = 4")
	})
	public static GamaImage with_size(final IScope scope, final GamaImage image, final Integer width,
			final Integer height) {
		return image == null || height <= 0d || width <= 0d ? null
				: height == image.getHeight() && width == image.getWidth() ? image
				: resize(image, Mode.FIT_EXACT, width, height);
	}

	/**
	 * Horizontal flip.
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @return the gama image
	 */
	@operator ("horizontal_flip")
	@doc ("Returns an image flipped horizontally by reflecting the original image around the y axis. The original image is left untouched")
	@no_test
	@tests ({
			@test ("image img <- image(8, 2, #green); image flipped <- horizontal_flip(img); flipped != nil"),
			@test ("image img2 <- image(8, 2, #green); image flipped2 <- horizontal_flip(img2); flipped2.width = 8"),
			@test ("image img3 <- image(8, 2, #green); image flipped3 <- horizontal_flip(img3); flipped3.height = 2"),
			@test ("image red_image <- image(4, 2, #red); rgb(matrix(horizontal_flip(red_image))[0, 0]) = #red")
	})
	public static GamaImage horizontalFlip(final IScope scope, final GamaImage image) {
		return rotate(image, ImageConstants.FLIP_HORZ);
	}

	/**
	 * Vertical flip.
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @return the gama image
	 */
	@operator ("vertical_flip")
	@doc ("Returns an image flipped vertically by reflecting the original image around the x axis. The original image is left untouched")
	@no_test
	@tests ({
			@test ("image red_image <- image(4, 2, #red); rgb(matrix(vertical_flip(red_image))[3, 1]) = #red")
	})
	public static GamaImage verticalFlip(final IScope scope, final GamaImage image) {
		return rotate(image, ImageConstants.FLIP_VERT);
	}

	/**
	 * Rotated.
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @param angleInDegrees
	 *            the angle in degrees
	 * @return the gama image
	 */
	@operator ("rotated_by")
	@doc ("Returns the image rotated using the angle in degrees passed in parameter. A positive angle means a clockwise rotation, and a negative one a counter-clockwise. The original image is left untouched")
	@no_test
	@tests ({
			@test ("image img <- image(8, 2, #green); image rotated <- img rotated_by 90.0; rotated != nil"),
			@test ("image img2 <- image(8, 2, #green); image rotated2 <- img2 rotated_by 90.0; rotated2.width = 2"),
			@test ("image img3 <- image(8, 2, #green); image rotated3 <- img3 rotated_by 90.0; rotated3.height = 8"),
			@test ("image red_image <- image(4, 2, #red); rgb(matrix(red_image rotated_by 90)[0, 0]) = #red")
	})
	public static GamaImage rotated(final IScope scope, final GamaImage image, final double angleInDegrees) {
		double angle = Math.abs(angleInDegrees) % 360 * Math.signum(angleInDegrees);
		if (angle == Math.floor(angle)) {
			switch ((int) angle) {
				case 0:
					return image;
				case 90, -270:
					return rotate(image, 90);
				case 180, -180:
					return rotate(image, 180);
				case 270, -90:
					return rotate(image, 270);
			}
		}
		double rads = Math.toRadians(angle);
		double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
		int w = image.getWidth();
		int h = image.getHeight();
		int newWidth = (int) Math.floor(w * cos + h * sin);
		int newHeight = (int) Math.floor(h * cos + w * sin);
		GamaImage rotated = GamaImage.ofDimensions(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = rotated.createGraphics();
		// Make sure the background is transparent
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0f));
		g2.setColor(new Color(0, 0, 0, 0));
		g2.fillRect(0, 0, newWidth, newHeight);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
		g2.setRenderingHints(HINTS);
		g2.translate((newWidth - w) / 2, (newHeight - h) / 2);
		g2.rotate(rads, w / 2d, h / 2d);
		g2.drawImage(image, 0, 0, null);
		g2.dispose();
		rotated.setId(image.getId() + "rotated" + angleInDegrees);
		return rotated;
	}

	/**
	 * Tint.
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @param color
	 *            the color
	 * @return the gama image
	 */
	@operator ({ "tinted_with", "*" })
	@doc ("Returns the image tinted using the color passed in parameter. This effectively multiplies the colors of the image by it. The original image is left untouched")
	@no_test
	@tests ({
			@test ("image img <- image(4, 4, #red); image tinted <- img tinted_with #blue; tinted != nil"),
			@test ("image img2 <- image(4, 4, #red); image tinted2 <- img2 tinted_with #blue; tinted2.width = 4")
	})
	public static GamaImage tint(final IScope scope, final GamaImage image, final IColor color) {
		GamaImage result = GamaImage.ofDimensions(image.getWidth(), image.getHeight(), Transparency.TRANSLUCENT);
		Graphics2D graphics = result.createGraphics();
		graphics.drawImage(image, 0, 0, null);
		graphics.dispose();
		float r = color.red() / 255f;
		float g = color.green() / 255f;
		float b = color.blue() / 255f;
		float a = color.alpha() / 255f;
		if (result.getRaster().getDataBuffer() instanceof DataBufferInt buffer) {
			final int[] pixels = buffer.getData();
			for (int i = 0; i < pixels.length; i++) {
				final int p = pixels[i];
				int ax = (int) (((p >>> 24) & 0xFF) * a);
				int rx = (int) (((p >>> 16) & 0xFF) * r);
				int gx = (int) (((p >>> 8) & 0xFF) * g);
				int bx = (int) ((p & 0xFF) * b);
				pixels[i] = ax << 24 | rx << 16 | gx << 8 | bx;
			}
		} else {
			for (int i = 0; i < result.getWidth(); i++) {
				for (int j = 0; j < result.getHeight(); j++) {
					final int p = result.getRGB(i, j);
					int ax = (int) (((p >>> 24) & 0xFF) * a);
					int rx = (int) (((p >>> 16) & 0xFF) * r);
					int gx = (int) (((p >>> 8) & 0xFF) * g);
					int bx = (int) ((p & 0xFF) * b);
					result.setRGB(i, j, ax << 24 | rx << 16 | gx << 8 | bx);
				}
			}
		}
		result.setId(image.getId() + "tinted" + color.getRGB());
		return result;
	}

	/**
	 * Tint.
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @param color
	 *            the color
	 * @return the gama image
	 */

	/**
	 * Tint.
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @param color
	 *            the color
	 * @param ratio
	 *            the ratio
	 * @return the gama image
	 */
	@operator ({ "tinted_with" })
	@doc ("Returns the image tinted using the color passed in parameter and a factor between 0 and 1, determining the transparency of the dyeing to apply. The original image is left untouched")
	@no_test
	@tests ({
			// the ratio is the strength of the tint
			@test ("image red_image <- image(4, 2, #red); rgb(matrix(tinted_with(red_image, #blue, 1.0))[0, 0]) = #blue"),
			@test ("image red_image2 <- image(4, 2, #red); rgb(matrix(tinted_with(red_image2, #blue, 0.0))[0, 0]) = #red")
	})
	public static GamaImage tint(final IScope scope, final GamaImage image, final IColor color, final double ratio) {
		int w = image.getWidth();
		int h = image.getHeight();
		GamaImage result = GamaImage.ofDimensions(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = result.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.setComposite(AlphaComposite.SrcAtop.derive(Math.min(1f, Math.max((float) ratio, 0f))));
		g.setColor(IColor.toAWTColor(color));
		g.fillRect(0, 0, w, h);
		g.dispose();
		result.setId(image.getId() + "tinted" + color + "|" + ratio);
		return result;
	}

	/**
	 * Blend.
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @param overlay
	 *            the overlay
	 * @param ratio
	 *            the ratio
	 * @return the gama image
	 */
	@operator (
			value = "blend",
			can_be_const = true)
	@doc (
			value = "Blend two images with an optional ratio between 0 and 1 (determines the transparency of the second image, applied as an overlay to the first). The size of the resulting image is that of the first parameter. The original image is left untouched",
			masterDoc = true,
			examples = { @example (
					value = "blend(img1, img2, 0.3)",
					equals = "to a composed image with the two",
					isExecutable = false) })
	@no_test
	@tests ({
			@test ("image red_image <- image(4, 2, #red); image blue_image <- image(4, 2, #blue); rgb mixed <- rgb(matrix(blend(red_image, blue_image, 0.5))[0, 0]); mixed.red > 120 and mixed.red < 135"),
			@test ("image red_image2 <- image(4, 2, #red); image blue_image2 <- image(4, 2, #blue); rgb mixed2 <- rgb(matrix(blend(red_image2, blue_image2, 0.5))[0, 0]); mixed2.blue > 120 and mixed2.blue < 135"),
			@test ("image red_image3 <- image(4, 2, #red); image blue_image3 <- image(4, 2, #blue); rgb mixed3 <- rgb(matrix(blend(red_image3, blue_image3, 0.5))[0, 0]); mixed3.green = 0"),
			// the ratio is the weight of the second image
			@test ("image red_image4 <- image(4, 2, #red); image blue_image4 <- image(4, 2, #blue); rgb(matrix(blend(red_image4, blue_image4, 1.0))[0, 0]) = #blue"),
			@test ("image red_image5 <- image(4, 2, #red); image blue_image5 <- image(4, 2, #blue); rgb(matrix(blend(red_image5, blue_image5, 0.0))[0, 0]) = #red")
	})
	public static GamaImage blend(final IScope scope, final GamaImage image, final GamaImage overlay,
			final double ratio) {
		GamaImage result = ImageHelper.copyToOptimalImage(image);
		Graphics2D g2d = result.createGraphics();
		g2d.setComposite(AlphaComposite.SrcOver.derive(Math.min(1f, Math.max((float) ratio, 0f))));
		int x = (result.getWidth() - overlay.getWidth()) / 2;
		int y = (result.getHeight() - overlay.getHeight()) / 2;
		g2d.drawImage(overlay, x, y, null);
		g2d.dispose();
		result.setId(image.getId() + "|" + overlay.getId());
		return result;
	}

	/**
	 * Blur.
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @return the gama image
	 */
	@operator ("blurred")
	@doc ("Application of a blurrying filter to the image passed in parameter. This operation can be applied multiple times. The original image is left untouched")
	@no_test
	@tests ({
			@test ("image img <- image(4, 4, #red); image blur <- blurred(img); blur != nil"),
			@test ("image img2 <- image(4, 4, #red); image blur2 <- blurred(img2); blur2.width = 4"),
			@test ("image red_image <- image(4, 2, #red); rgb(matrix(blurred(red_image))[1, 1]) = #red")
	})
	public static GamaImage blur(final IScope scope, final GamaImage image) {
		return apply(image, OP_BLUR);
	}

	/**
	 * Blur.
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @return the gama image
	 */
	@operator ("blurred")
	@doc ("Application of a blurrying filter to the image passed in parameter. This operation is applied multiple times if the last argument is > 0. The original image is left untouched")
	@no_test
	public static GamaImage blur(final IScope scope, final GamaImage image, final int count) {
		return apply(image, OP_BLUR, count);
	}

	/**
	 * Sharpen.
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @return the gama image
	 */
	@operator ("sharpened")
	@doc ("Application of a sharpening filter to the image passed in parameter. This operation can be applied multiple times. The original image is left untouched")
	@no_test
	@tests ({
			@test ("image img <- image(4, 4, #red); image sharp <- sharpened(img); sharp != nil"),
			@test ("image img2 <- image(4, 4, #red); image sharp2 <- sharpened(img2); sharp2.width = 4"),
			@test ("image red_image <- image(4, 2, #red); rgb(matrix(sharpened(red_image))[1, 1]) = #red")
	})
	public static GamaImage sharpen(final IScope scope, final GamaImage image) {
		return apply(image, OP_SHARPEN);
	}

	/**
	 * Sharpen.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @param count
	 *            the count
	 * @return the gama image
	 * @date 7 oct. 2023
	 */
	@operator ("sharpened")
	@doc ("Application of a sharpening filter to the image passed in parameter. This operation is applied multiple times if the last argument is > 0. The original image is left untouched")
	@no_test
	public static GamaImage sharpen(final IScope scope, final GamaImage image, final int count) {
		return apply(image, OP_SHARPEN, count);
	}

	/**
	 * Cropped.
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @param ox
	 *            the ox
	 * @param oy
	 *            the oy
	 * @param ow
	 *            the ow
	 * @param oh
	 *            the oh
	 * @return the gama image
	 */
	@operator ({ "clipped_with", "cropped_to" })
	@doc ("""
			Used to crop the given image using a rectangle starting at the top-left x, y coordinates and expanding using the width and height. \
			If one of the dimensions of the resulting image is 0, of if they are equal to that of the given image, returns it. \
			 The original image is left untouched""")
	@no_test

	@tests ({
			@test ("image red_image <- image(4, 2, #red); image cropped <- cropped_to(red_image, 0, 0, 2, 1); cropped.width = 2"),
			@test ("image red_image2 <- image(4, 2, #red); image cropped2 <- cropped_to(red_image2, 0, 0, 2, 1); cropped2.height = 1"),
			@test ("image red_image3 <- image(4, 2, #red); image cropped3 <- cropped_to(red_image3, 0, 0, 2, 1); rgb(matrix(cropped3)[1, 0]) = #red")
	})
	public static GamaImage cropped(final IScope scope, final GamaImage image, final int ox, final int oy, final int ow,
			final int oh) {
		int iw = image.getWidth();
		int ih = image.getHeight();
		int width = Math.min(iw, Math.max(0, ow));
		int height = Math.min(ih, Math.max(0, oh));
		int x = Math.min(iw, Math.max(0, ox));
		int y = Math.min(ih, Math.max(0, oy));
		if (x == width || width == 0 || height == 0 || y == height) return image;
		if (x == 0 && y == 0 && width == iw && height == ih) return image;
		GamaImage result = GamaImage.bestFor(image, width, height);
		Graphics g = result.getGraphics();
		g.drawImage(image, 0, 0, width, height, x, y, x + width, y + height, null);
		g.dispose();
		result.setId(image.getId() + "crop" + ox + "|" + oy + "|" + ow + "|" + oh);
		return result;
	}

	/**
	 * Copy to clipboard.
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @return the boolean
	 */
	@no_fuzz_test ("acts on the outside world (files, network, clipboard, user interface, shell...)")
	@operator (
			value = "copy_to_clipboard",
			can_be_const = false,
			category = { IOperatorCategory.SYSTEM },
			concept = { IConcept.SYSTEM })
	@doc (
			examples = @example ("bool copied  <- copy_to_clipboard(img);"),
			value = "Tries to copy the given image to the clipboard and returns whether it has been correctly copied or not (for instance it might be impossible in a headless environment)")
	@no_test ()
	public static Boolean copyToClipboard(final IScope scope, final GamaImage image) {
		if (image == null || ImageConstants.clipboard == null) return false;
		ImageConstants.clipboard.setContents(new TransferableImage(image), null);
		return true;
	}

	/**
	 * Image.
	 *
	 * @param w
	 *            the w
	 * @param h
	 *            the h
	 * @param type
	 *            the type
	 * @return the gama image
	 */
	@operator (
			can_be_const = true,
			value = "image")
	@doc ("Builds a new blank image of the specified dimensions, which does not accept transparency")
	@no_test
	@tests ({
			@test ("image blank <- image(4, 4); blank != nil"),
			@test ("image blank2 <- image(4, 4); blank2.width = 4"),
			@test ("image blank3 <- image(4, 4); blank3.height = 4")
	})
	public static GamaImage image(final int w, final int h) {
		return GamaImage.ofDimensions(w, h, BufferedImage.TYPE_INT_ARGB);
	}

	/**
	 * Image.
	 *
	 * @param w
	 *            the w
	 * @param h
	 *            the h
	 * @param color
	 *            the color
	 * @return the gama image
	 */
	@operator (
			can_be_const = true,
			value = "image")
	@doc ("Builds a new image with the specified dimensions and already filled with the given rgb color")
	@no_test
	@tests ({
			@test ("image colored <- image(8, 2, #red); colored.width = 8"),
			@test ("image colored2 <- image(8, 2, #red); colored2.height = 2"),
			@test ("image red_image <- image(4, 2, #red); image doubled <- red_image * 2; doubled.width = 8"),
			@test ("image red_image2 <- image(4, 2, #red); image doubled2 <- red_image2 * 2; doubled2.height = 4"),
			// the original is left untouched
			@test ("image red_image3 <- image(4, 2, #red); red_image3.width = 4"),
			@test ("image red_image4 <- image(4, 2, #red); red_image4.height = 2")
	})
	public static GamaImage image(final int w, final int h, final IColor color) {
		GamaImage gi = GamaImage.ofDimensions(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = gi.createGraphics();
		g.setColor(IColor.toAWTColor(color));
		g.fillRect(0, 0, w, h);
		g.dispose();
		return gi;
	}

	/**
	 * Image.
	 *
	 * @param w
	 *            the w
	 * @param h
	 *            the h
	 * @param type
	 *            the type
	 * @return the gama image
	 */
	@operator (
			can_be_const = true,
			value = "image")
	@doc ("Builds a new blank image with the specified dimensions and indicates if it will support transparency or not")
	@no_test
	public static GamaImage image(final int w, final int h, final boolean alpha) {
		return GamaImage.ofDimensions(w, h, alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
	}

	/**
	 * Matrix.
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @return the gama int matrix
	 */
	@operator (
			value = "matrix",
			content_type = IType.INT,
			can_be_const = true)
	@doc ("Returns the matrix<int> value of the image passed in parameter, where each pixel is represented by the RGB int value. The dimensions of the matrix are those of the image. ")
	@no_test
	@tests ({
			@test ("matrix<int> pixels <- matrix(image(4, 2, rgb(10, 20, 30))); pixels.columns = 4"),
			@test ("matrix<int> pixels2 <- matrix(image(4, 2, rgb(10, 20, 30))); pixels2.rows = 2"),
			@test ("matrix<int> pixels3 <- matrix(image(4, 2, rgb(10, 20, 30))); rgb(pixels3[0, 0]) = rgb(10, 20, 30)"),
			@test ("matrix<int> pixels4 <- matrix(image(4, 2, rgb(10, 20, 30))); rgb(pixels4[3, 1]) = rgb(10, 20, 30)"),
			@test ("matrix<int> pixels5 <- matrix(image(4, 2, rgb(10, 20, 30))); remove_duplicates(list(pixels5)) = [int(rgb(10, 20, 30))]"),
			@test ("matrix<int> blank_pixels <- matrix(image(2, 2)); rgb(blank_pixels[0, 0]).alpha = 0"),
			// two images with the same pixels have equal matrices
			@test ("matrix(image(4, 2, #red)) = matrix(image(4, 2, #red))"),
			@test ("matrix(image(4, 2, #red)) != matrix(image(4, 2, #blue))"),
			@test ("image red_image <- image(4, 2, #red); image doubled <- red_image * 2; rgb(matrix(doubled)[7, 3]) = #red")
	})
	public static IMatrix matrix(final IScope scope, final GamaImage image) {
		final int xSize = image.getWidth();
		final int ySize = image.getHeight();
		final IMatrix matrix = GamaMatrixFactory.createIntMatrix(xSize, ySize);
		if (matrix instanceof GamaIntMatrix gim) {
			final int[] target = gim.getMatrix();
			if (image.getRaster().getDataBuffer() instanceof DataBufferInt buffer) {
				System.arraycopy(buffer.getData(), 0, target, 0, Math.min(buffer.getData().length, target.length));
			} else {
				final int[] source = image.getRGB(0, 0, xSize, ySize, null, 0, xSize);
				System.arraycopy(source, 0, target, 0, Math.min(source.length, target.length));
			}
			return matrix;
		}
		for (int i = 0; i < xSize; i++) {
			for (int j = 0; j < ySize; j++) { matrix.set(scope, i, j, image.getRGB(i, j)); }
		}
		return matrix;
	}

}
