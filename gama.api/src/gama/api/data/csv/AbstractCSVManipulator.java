/*******************************************************************************************************
 *
 * AbstractCSVManipulator.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.data.csv;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

import gama.api.utils.StringUtils;
import gama.api.utils.prefs.GamaPreferences;

/**
 * The Class AbstractCSVManipulator.
 */
public abstract class AbstractCSVManipulator implements Closeable {

	/** The replacements. */
	public static final Map<Character, Character> REPLACEMENTS =
			Map.of(';', ',', ',', ';', ' ', ';', '|', ';', ':', ';', '\t', ';');

	/** The Constant MAX_BUFFER_SIZE. */
	public static final int MAX_BUFFER_SIZE = 1024;

	/** The Constant MAX_FILE_BUFFER_SIZE. */
	public static final int MAX_FILE_BUFFER_SIZE = 4 * 1024;

	/** The Constant INITIAL_COLUMN_COUNT. */
	public static final int INITIAL_COLUMN_COUNT = 10;

	/** The Constant INITIAL_COLUMN_BUFFER_SIZE. */
	public static final int INITIAL_COLUMN_BUFFER_SIZE = 50;

	/** The first column. */
	protected boolean firstColumn = true;

	/** The file name. */
	protected String fileName = null;

	/** The Text qualifier. */
	public Character textQualifier = getDefaultQualifier();

	/** The Delimiter. */
	public char delimiter = getDefaultDelimiter();

	/**
	 * Gets the default delimiter.
	 *
	 * @return the default delimiter
	 */
	public static char getDefaultDelimiter() {
		String del = GamaPreferences.External.CSV_SEPARATOR.getValue();
		if (del == null || del.isEmpty()) return StringUtils.Letters.COMMA;
		return del.charAt(0);
	}

	/**
	 * Gets the default qualifier.
	 *
	 * @return the default qualifier
	 */
	public static char getDefaultQualifier() {
		String del = GamaPreferences.External.CSV_STRING_QUALIFIER.getValue();
		if (del == null || del.isEmpty()) return StringUtils.Letters.QUOTE;
		return del.charAt(0);
	}

	@Override
	public abstract void close() throws IOException;

	/**
	 * Sets the character to use as the column delimiter. Default is comma, ','.
	 *
	 * @param delimiter
	 *            The character to use as the column delimiter.
	 */
	public void setDelimiter(final char delimiter) { this.delimiter = delimiter; }

	/**
	 * Sets the character to use as a text qualifier in the data.
	 *
	 * @param textQualifier
	 *            The character to use as a text qualifier in the data.
	 */
	public void setTextQualifier(final Character textQualifier) { this.textQualifier = textQualifier; }

	/**
	 * End record.
	 */
	public abstract void endRecord() throws IOException;

}
