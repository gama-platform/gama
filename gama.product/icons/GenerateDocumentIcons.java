import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.imageio.ImageIO;

/**
 * Converts PNG artwork into document icons, or draws the flat model-project package.
 * Run from the repository root:
 * {@code java -Djava.awt.headless=true gama.product/icons/GenerateDocumentIcons.java gama.product/extraresources ModelProject --model-project}
 */
public final class GenerateDocumentIcons {

	public static void main(final String[] args) throws Exception {
		final Path resources = Path.of(args[0]);
		final String name = args[1];
		final var source = "--model-project".equals(args[2]) ? modelProject() : ImageIO.read(Path.of(args[2]).toFile());
		if (source == null) throw new IllegalStateException("Unable to read document artwork");
		final int[] sizes = { 16, 24, 32, 48, 64, 128, 256, 512, 1024 };
		final var pngs = new ArrayList<byte[]>();
		for (final int size : sizes) {
			final var image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
			final var graphics = image.createGraphics();
			try {
				graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				graphics.drawImage(source, 0, 0, size, size, null);
			} finally { graphics.dispose(); }
			final var png = new ByteArrayOutputStream();
			if (!ImageIO.write(image, "png", png)) throw new IllegalStateException("PNG encoder unavailable");
			pngs.add(png.toByteArray());
		}
		final int count = 7; // Windows uses sizes through 256 pixels.
		final var ico = ByteBuffer.allocate(6 + count * 16).order(ByteOrder.LITTLE_ENDIAN);
		ico.putShort((short) 0).putShort((short) 1).putShort((short) count);
		int offset = ico.capacity();
		for (int i = 0; i < count; i++) {
			ico.put((byte) sizes[i]).put((byte) sizes[i]).putShort((short) 0);
			ico.putShort((short) 1).putShort((short) 32).putInt(pngs.get(i).length).putInt(offset);
			offset += pngs.get(i).length;
		}
		try (var output = Files.newOutputStream(resources.resolve(name + ".ico"))) {
			output.write(ico.array());
			for (int i = 0; i < count; i++) output.write(pngs.get(i));
		}
		Files.write(resources.resolve(name + ".png"), pngs.get(7));
		final int[] macSizes = { 0, 2, 4, 5, 6, 7, 8 };
		final String[] macTypes = { "icp4", "icp5", "icp6", "ic07", "ic08", "ic09", "ic10" };
		int length = 8;
		for (final int index : macSizes) length += 8 + pngs.get(index).length;
		final var icns = ByteBuffer.allocate(length).order(ByteOrder.BIG_ENDIAN);
		icns.putInt(0x69636e73).putInt(length);
		for (int i = 0; i < macSizes.length; i++) {
			for (int j = 0; j < 4; j++) icns.put((byte) macTypes[i].charAt(j));
			final byte[] png = pngs.get(macSizes[i]);
			icns.putInt(8 + png.length).put(png);
		}
		Files.write(resources.resolve(name + ".icns"), icns.array());
	}

	private static BufferedImage modelProject() {
		final var image = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_ARGB);
		final var graphics = image.createGraphics();
		try {
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			final var box = new RoundRectangle2D.Double(96, 128, 832, 800, 40, 40);
			graphics.setColor(new Color(0xfff9ef));
			graphics.fill(box);
			graphics.setClip(box);
			graphics.setColor(new Color(0xf3d9ac));
			graphics.fillRect(96, 128, 832, 208);
			graphics.setStroke(new BasicStroke(12));
			graphics.setColor(new Color(0xb48b50));
			graphics.drawLine(96, 336, 928, 336);
			graphics.drawLine(448, 128, 448, 336);
			graphics.drawLine(576, 128, 576, 336);
			graphics.setClip(null);
			graphics.draw(box);

			// Retain the GAMA document artwork's orange/red rings and blue G, without a page fold or play symbol.
			final int[] radii = { 230, 180 };
			final int[] pale = { 0xf9ead5, 0xf4e0d9 };
			final int[] colors = { 0xefaa39, 0xc65329 };
			graphics.setStroke(new BasicStroke(32));
			for (int i = 0; i < radii.length; i++) {
				final int r = radii[i];
				graphics.setColor(new Color(pale[i]));
				graphics.draw(new Ellipse2D.Double(512 - r, 635 - r, r * 2, r * 2));
				graphics.setColor(new Color(colors[i]));
				graphics.draw(new Arc2D.Double(512 - r, 635 - r, r * 2, r * 2, 65, 180, Arc2D.OPEN));
			}
			graphics.setStroke(new BasicStroke(40));
			graphics.setColor(new Color(0x285e90));
			graphics.draw(new Arc2D.Double(382, 505, 260, 260, 45, 270, Arc2D.OPEN));
			graphics.fillRect(512, 625, 150, 40);
			graphics.fillRect(622, 625, 40, 140);
		} finally { graphics.dispose(); }
		return image;
	}
}
