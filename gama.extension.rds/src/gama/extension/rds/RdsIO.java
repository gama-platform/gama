/*******************************************************************************************************
 *
 * RdsIO.java, in gama.extension.rds, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.rds;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.renjin.serialization.RDataReader;
import org.renjin.serialization.RDataWriter;
import org.renjin.sexp.SEXP;

/** Version-2 RDS streams, without the workspace header used by R's save()/load(). */
public final class RdsIO {

	public record Header(boolean gzip, char format, int version, int writerVersion, int minimumReaderVersion) {
		public String encoding() {
			return switch (format) {
				case 'X' -> "XDR";
				case 'A' -> "ASCII";
				default -> "native binary";
			};
		}

		public static String rVersion(final int encoded) {
			return (encoded >>> 16) + "." + (encoded >>> 8 & 255) + "." + (encoded & 255);
		}
	}

	private RdsIO() {}

	public static Header readHeader(final InputStream input) throws IOException {
		try (var raw = new BufferedInputStream(input)) {
			raw.mark(2);
			final boolean gzip = raw.read() == 0x1f && raw.read() == 0x8b;
			raw.reset();
			try (var data = new BufferedInputStream(gzip ? new GZIPInputStream(raw) : raw)) {
				return header(data, gzip);
			}
		}
	}

	public static SEXP read(final Path file) throws IOException {
		return read(Files.newInputStream(file));
	}

	/** Reads and closes the supplied stream. */
	public static SEXP read(final InputStream input) throws IOException {
		try (var raw = new BufferedInputStream(input)) {
			raw.mark(2);
			final int first = raw.read();
			final int second = raw.read();
			raw.reset();
			try (var data = new BufferedInputStream(first == 0x1f && second == 0x8b
					? new GZIPInputStream(raw) : raw)) {
				checkHeader(data);
				try (var reader = new RDataReader(data)) {
					return reader.readFile();
				}
			}
		}
	}

	private static void checkHeader(final BufferedInputStream data) throws IOException {
		data.mark(128);
		final Header header = header(data, false);
		if (header.version() != 2) throw new IOException("RDS serialization version " + header.version()
				+ " is not supported. Re-export in R with saveRDS(object, path, version = 2).");
		data.reset();
	}

	private static Header header(final InputStream data, final boolean gzip) throws IOException {
		final int format = data.read();
		if (data.read() != '\n' || format != 'X' && format != 'B' && format != 'A') {
			throw new IOException("Expected an RDS stream (gzip or uncompressed), not an R workspace or another "
					+ "compression format. Re-export with saveRDS(object, path, version = 2, compress = TRUE).");
		}
		return new Header(gzip, (char) format, readInteger(data, format), readInteger(data, format),
				readInteger(data, format));
	}

	private static int readInteger(final InputStream data, final int format) throws IOException {
		if (format == 'A') {
			final StringBuilder line = new StringBuilder();
			int c;
			while ((c = data.read()) != '\n' && c != -1 && line.length() < 16) { line.append((char) c); }
			try {
				return Integer.parseInt(line.toString().trim());
			} catch (final NumberFormatException e) {
				throw new IOException("Invalid RDS serialization version", e);
			}
		} else {
			final byte[] bytes = new byte[4];
			if (data.readNBytes(bytes, 0, 4) != 4) throw new EOFException("Truncated RDS header");
			return ByteBuffer.wrap(bytes).order(format == 'B' ? ByteOrder.nativeOrder() : ByteOrder.BIG_ENDIAN)
					.getInt();
		}
	}

	/** Writes and closes the supplied stream, using gzip compression. */
	public static void write(final SEXP value, final OutputStream output) throws IOException {
		try (var gzip = new GZIPOutputStream(output); var writer = new RDataWriter(gzip)) {
			writer.serialize(value);
		}
	}

	/** Converts before calling this method so invalid values cannot damage an existing destination. */
	public static void write(final SEXP value, final Path file) throws IOException {
		write(value, file, true);
	}

	public static void write(final SEXP value, final Path file, final boolean overwrite) throws IOException {
		final Path destination = file.toAbsolutePath();
		Files.createDirectories(destination.getParent());
		final Path temporary = Files.createTempFile(destination.getParent(), ".gama-rds-", ".tmp");
		try {
			write(value, Files.newOutputStream(temporary));
			if (overwrite) Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
			else Files.move(temporary, destination);
		} finally {
			Files.deleteIfExists(temporary);
		}
	}
}
