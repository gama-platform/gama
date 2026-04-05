/*******************************************************************************************************
 *
 * CompressionUtils.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.files;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * The Class SerialisedSimulationManipulator.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 8 août 2023
 */
public abstract class CompressionUtils {

	/** The Constant CS. */
	final static Charset CS = StandardCharsets.ISO_8859_1;

	/** The null. */
	static byte[] NULL = {};

	/**
	 * Zip bytes.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param filename
	 *            the filename
	 * @param input
	 *            the input
	 * @return the byte[]
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @date 7 août 2023
	 */
	public static byte[] zip(final byte[] input) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				GZIPOutputStream zos = new GZIPOutputStream(baos);) {
			zos.write(input);
			zos.finish();
			return baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return NULL;
		}
	}

	/**
	 * Zip as string.
	 *
	 * @param input
	 *            the input
	 * @return the string
	 */
	public static String zip(final String input) {
		return new String(zip(input.getBytes(CS)), CS);
	}

	/**
	 * Unzip.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param input
	 *            the input
	 * @return the byte[]
	 * @date 7 août 2023
	 */
	public static byte[] unzip(final byte[] input) {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(input);
				GZIPInputStream zos = new GZIPInputStream(bais);) {
			return zos.readAllBytes();
		} catch (IOException e) {
			e.printStackTrace();
			return NULL;
		}
	}

	/**
	 * Unzip.
	 *
	 * @param input
	 *            the input
	 * @return the string
	 */
	public static String unzip(final String input) {
		return new String(unzip(input.getBytes(CS)), CS);
	}

}
