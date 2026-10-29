import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.imageio.ImageIO;

/** Converts PNGs decoded from the existing ICNS artwork into cross-platform document icons. */
public final class GenerateDocumentIcons {

	public static void main(final String[] args) throws Exception {
		final Path resources = Path.of(args[0]);
		final String name = args[1];
		final var source = ImageIO.read(Path.of(args[2]).toFile());
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
	}
}
