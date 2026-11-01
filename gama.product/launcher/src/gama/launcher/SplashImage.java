package gama.launcher;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.text.AttributedString;

import javax.imageio.ImageIO;

/** Split-panel player splash. macOS uses the matching Cocoa layout without starting AWT. */
final class SplashImage {

	private static final int WIDTH = 900, HEIGHT = 560, PANEL = 330, MARGIN = 28;
	private static final int TEXT_WIDTH = PANEL - 2 * MARGIN;
	private static final Color INK = new Color(14, 43, 53), MUTED = new Color(81, 105, 112);

	private SplashImage() {}

	static void write(final String image, final String title, final String description, final Path output) throws IOException {
		final var logo = ImageIO.read(new ByteArrayInputStream(PackageLauncher.logo()));
		if (logo == null) throw new IOException("Unable to read the GAMA splash logo");
		final var canvas = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		final var graphics = canvas.createGraphics();
		try {
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			graphics.setColor(new Color(247, 246, 242));
			graphics.fillRect(0, 0, WIDTH, HEIGHT);
			graphics.setColor(new Color(227, 237, 235));
			graphics.fillRect(PANEL, 0, WIDTH - PANEL, HEIGHT);
			if (image.isBlank()) {
				graphics.drawImage(logo, PANEL + (WIDTH - PANEL - 240) / 2, (HEIGHT - 240) / 2, 240, 240, null);
			} else {
				final var illustration = ImageIO.read(Path.of(image).toFile());
				if (illustration == null) throw new IOException("Unable to read the package presentation image");
				final double scale = Math.max((double) (WIDTH - PANEL) / illustration.getWidth(),
						(double) HEIGHT / illustration.getHeight());
				final int width = (int) Math.ceil(illustration.getWidth() * scale);
				final int height = (int) Math.ceil(illustration.getHeight() * scale);
				graphics.setClip(PANEL, 0, WIDTH - PANEL, HEIGHT);
				graphics.drawImage(illustration, PANEL + (WIDTH - PANEL - width) / 2, (HEIGHT - height) / 2,
						width, height, null);
				graphics.setClip(null);
			}
			final String label = normalized(title.isBlank() ? "Simulation" : title);
			Font font = new Font(Font.SANS_SERIF, Font.BOLD, 32);
			while (font.getSize() > 18 && textHeight(graphics, label, font) > 200)
				font = font.deriveFont((float) font.getSize() - 1);
			graphics.setColor(INK);
			final float titleBottom = drawText(graphics, label, font, 42, 200);
			graphics.setColor(MUTED);
			drawText(graphics, normalized(description), new Font(Font.SANS_SERIF, Font.PLAIN, 14),
					titleBottom + 20, Math.min(120, 364 - titleBottom - 20));
			graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
			graphics.drawString("Opening simulation...", MARGIN, 404);
			graphics.drawImage(logo, MARGIN, 430, 96, 96, null);
			graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
			graphics.drawString("powered by", 136, 466);
			graphics.setColor(INK);
			graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
			graphics.drawString("GAMA", 136, 496);
		} finally {
			graphics.dispose();
		}
		if (!ImageIO.write(canvas, "png", output.toFile())) throw new IOException("PNG image writer unavailable");
	}

	private static String normalized(final String text) {
		return text.replaceAll("\\s+", " ").strip();
	}

	private static LineBreakMeasurer measurer(final Graphics2D graphics, final String text, final Font font) {
		final var attributed = new AttributedString(text);
		attributed.addAttribute(TextAttribute.FONT, font);
		return new LineBreakMeasurer(attributed.getIterator(), graphics.getFontRenderContext());
	}

	private static float textHeight(final Graphics2D graphics, final String text, final Font font) {
		final var paragraph = measurer(graphics, text, font);
		float height = 0;
		while (paragraph.getPosition() < text.length()) {
			final var line = paragraph.nextLayout(TEXT_WIDTH);
			height += line.getAscent() + line.getDescent() + line.getLeading();
		}
		return height;
	}

	private static float drawText(final Graphics2D graphics, final String text, final Font font,
			final float top, final float height) {
		if (text.isEmpty() || height <= 0) return top;
		graphics.setFont(font);
		final var paragraph = measurer(graphics, text, font);
		float y = top;
		while (paragraph.getPosition() < text.length()) {
			final int start = paragraph.getPosition();
			final var line = paragraph.nextLayout(TEXT_WIDTH);
			final float lineHeight = line.getAscent() + line.getDescent() + line.getLeading();
			if (y + lineHeight > top + height) break;
			y += line.getAscent();
			if (y + line.getDescent() + line.getLeading() + lineHeight > top + height
					&& paragraph.getPosition() < text.length()) {
				String tail = text.substring(start, paragraph.getPosition()).stripTrailing();
				final var metrics = graphics.getFontMetrics();
				while (!tail.isEmpty() && metrics.stringWidth(tail + "...") > TEXT_WIDTH)
					tail = tail.substring(0, tail.offsetByCodePoints(tail.length(), -1));
				graphics.drawString(tail + "...", MARGIN, y);
				return y + line.getDescent() + line.getLeading();
			}
			line.draw(graphics, MARGIN, y);
			y += line.getDescent() + line.getLeading();
		}
		return y;
	}
}
