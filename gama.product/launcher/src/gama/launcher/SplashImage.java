package gama.launcher;

import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

/** Java2D is used only on Windows/Linux; macOS draws through Cocoa without starting AWT. */
final class SplashImage {

	private SplashImage() {}

	static void write(final String image, final String title, final String description, final Path output) throws IOException {
		final var canvas = new BufferedImage(680, 520, BufferedImage.TYPE_INT_RGB);
		final var graphics = canvas.createGraphics();
		try {
			graphics.setColor(new Color(250, 250, 250));
			graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			graphics.setColor(Color.DARK_GRAY);
			graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
			final var metrics = graphics.getFontMetrics();
			String label = title.isBlank() ? "Simulation" : title.replace('\n', ' ');
			while (metrics.stringWidth(label) > 620 && label.length() > 1) label = label.substring(0, label.length() - 1);
			graphics.drawString(label, (680 - metrics.stringWidth(label)) / 2, 60);
			graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
			final var text = new java.text.AttributedString(description.isBlank() ? " " : description);
			text.addAttribute(java.awt.font.TextAttribute.FONT, graphics.getFont());
			final var paragraph = new java.awt.font.LineBreakMeasurer(text.getIterator(),
					graphics.getFontRenderContext());
			for (float y = 100; paragraph.getPosition() < description.length() && y < 150; y += 20) {
				final var line = paragraph.nextLayout(560);
				line.draw(graphics, (680 - line.getAdvance()) / 2, y);
			}
			if (!image.isBlank()) {
				final var illustration = ImageIO.read(Path.of(image).toFile());
				if (illustration == null) throw new IOException("Unable to read the package presentation image");
				final double scale = Math.min(1d, Math.min(560d / illustration.getWidth(), 240d / illustration.getHeight()));
				final int width = Math.max(1, (int) (illustration.getWidth() * scale));
				final int height = Math.max(1, (int) (illustration.getHeight() * scale));
				graphics.drawImage(illustration, (680 - width) / 2, 170 + (240 - height) / 2, width, height, null);
			}
			graphics.setColor(Color.GRAY);
			graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
			for (final var line : java.util.Map.of(465, "Opening simulation...", 495, "Powered by GAMA").entrySet())
				graphics.drawString(line.getValue(), (680 - graphics.getFontMetrics().stringWidth(line.getValue())) / 2, line.getKey());
		} finally {
			graphics.dispose();
		}
		if (!ImageIO.write(canvas, "png", output.toFile())) throw new IOException("PNG image writer unavailable");
	}
}
