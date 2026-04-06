/*******************************************************************************************************
 *
 * CSVInfo.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;

import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.utils.csv.CsvReader;
import gama.api.utils.files.AbstractFileMetaData;

/**
 * The Class CSVInfo.
 */
public class CSVInfo extends AbstractFileMetaData {

	/**
	 * The Class StringAnalysis.
	 */
	private static class StringAnalysis {

		/** The is float. */
		boolean isFloat = true;

		/** The is int. */
		boolean isInt = true;

		/** The is number sequence. */
		boolean isNumberSequence = true;

		/**
		 * Instantiates a new string analysis.
		 *
		 * @param s
		 *            the s
		 */
		StringAnalysis(final String s) {

			for (final char c : s.toCharArray()) {
				final boolean isDigit = Character.isDigit(c);
				if (!isDigit) {
					if (c == '.') {
						isInt = false;
					} else if (Character.isLetter(c)) {
						isInt = false;
						isFloat = false;
						isNumberSequence = false;
						break;
					} else if (c == gama.api.utils.StringUtils.Letters.COMMA
							|| c == gama.api.utils.StringUtils.Letters.SEMICOLUMN
							|| c == gama.api.utils.StringUtils.Letters.PIPE
							|| c == gama.api.utils.StringUtils.Letters.COLUMN
							|| c == gama.api.utils.StringUtils.Letters.SLASH || Character.isWhitespace(c)
							|| c == gama.api.utils.StringUtils.Letters.QUOTE) {
						isInt = false;
						isFloat = false;
					}
				}
			}
			if (isInt && isFloat) { isFloat = false; }
		}

	}

	/** The cols. */
	public int cols;

	/** The rows. */
	public int rows;

	/** The header. */
	public boolean header, atLeastOneNumber;

	/** The delimiter. */
	public Character delimiter;

	/** The type. */
	public IType type, firstLineType;

	/** The headers. */
	public String[] headers;

	/**
	 * Instantiates a new CSV info.
	 *
	 * @param file
	 *            the file
	 * @throws MalformedURLException
	 *             the malformed URL exception
	 */
	public CSVInfo(final IFile file) {
		super(file);
		createFrom(file.getLocation().toOSString());
	}

	/**
	 * Instantiates a new CSV info.
	 *
	 * @param fileName
	 *            the file name
	 * @param modificationStamp
	 *            the modification stamp
	 * @param CSVsep
	 *            the CS vsep
	 */
	private void createFrom(final String fileName) {
		try (CsvReader reader = new CsvReader(fileName)) {
			process(reader, null);
		} catch (FileNotFoundException e) {}
	}

	/**
	 * Process.
	 *
	 * @param reader
	 *            the reader
	 * @param CSVsep
	 *            the CS vsep
	 */
	public void process(final CsvReader reader, final String CSVsep) {
		// By default now (see #3786)
		// reader.setTextQualifier(AbstractCSVManipulator.getDefaultQualifier());
		boolean firstLineHasNumber = false;
		try {
			// firstLine
			final String s = reader.skipLine();
			headers = processFirstLine(s, CSVsep);
			firstLineHasNumber = atLeastOneNumber;
			atLeastOneNumber = false;
			reader.setDelimiter(delimiter);
			// secondLine

			if (!reader.readRecord()) {
				// We only have one line
				type = firstLineType;
				rows = 1;
			} else {
				// We process the second line
				type = processRecord(reader.getValues());
			}
			while (reader.readRecord()) { if (reader.columnsCount > cols) { cols = reader.columnsCount; } }
		} catch (final IOException e) {}
		if (!type.equals(firstLineType) || !firstLineHasNumber && atLeastOneNumber) {
			header = true;
			cols = headers.length;
		}
		rows = (int) reader.currentRecord + 1;
		reader.close();
	}

	/**
	 * Process first line.
	 *
	 * @param line
	 *            the line
	 * @param CSVsep
	 *            the CS vsep
	 * @return the string[]
	 */
	private String[] processFirstLine(final String line, final String CSVsep) {
		if (CSVsep != null && !CSVsep.isEmpty()) {
			delimiter = CSVsep.charAt(0);
		} else {
			String[] s = StringUtils.splitByWholeSeparatorPreserveAllTokens(line, ",");
			if (s.length != 1 || s[0].indexOf(' ') == -1 && s[0].indexOf(';') == -1
					&& s[0].indexOf(gama.api.utils.StringUtils.Letters.TAB) == -1) {
				// We are likely dealing with a unicolum file
				delimiter = gama.api.utils.StringUtils.Letters.COMMA;
			} else {
				// there should be another delimiter
				s = StringUtils.splitByWholeSeparatorPreserveAllTokens(line, ";");
				if (s.length == 1) {
					// Try with tab
					s = StringUtils.splitByWholeSeparatorPreserveAllTokens(line,
							"" + gama.api.utils.StringUtils.Letters.TAB);
					if (s.length == 1) {
						s = StringUtils.splitByWholeSeparatorPreserveAllTokens(line,
								"" + gama.api.utils.StringUtils.Letters.SPACE);
						if (s.length == 1) {
							delimiter = gama.api.utils.StringUtils.Letters.PIPE;
						} else {
							delimiter = gama.api.utils.StringUtils.Letters.SPACE;
						}
					} else {
						delimiter = gama.api.utils.StringUtils.Letters.TAB;
					}
				} else {
					delimiter = ';';
				}
			}
		}
		final String[] s2 = StringUtils.splitByWholeSeparatorPreserveAllTokens(line, delimiter.toString());
		firstLineType = processRecord(s2);
		return s2;
	}

	/**
	 * Process record.
	 *
	 * @param values
	 *            the values
	 * @return the i type
	 */
	private IType processRecord(final String[] values) {
		// Fix for #3294
		if (values.length > cols) { cols = values.length; }
		IType temp = null;
		for (final String s : values) {
			final StringAnalysis sa = new StringAnalysis(s);
			atLeastOneNumber = sa.isFloat || sa.isInt || sa.isNumberSequence;
			if (sa.isInt) {
				if (temp == null) { temp = Types.INT; }
			} else if (sa.isFloat) {
				if (temp == null || temp == Types.INT) { temp = Types.FLOAT; }
			} else {
				temp = Types.NO_TYPE;
			}
		}
		// in case nothing has been read (i.e. empty file)
		if (temp == null) { temp = Types.NO_TYPE; }
		return temp;
	}

	/**
	 * Instantiates a new CSV info.
	 *
	 * @param propertyString
	 *            the property string
	 */
	public CSVInfo(final String propertyString) {
		super(propertyString);
		final String[] segments = split(propertyString);
		cols = Integer.parseInt(segments[1]);
		rows = Integer.parseInt(segments[2]);
		header = Boolean.parseBoolean(segments[3]);
		delimiter = segments[4].charAt(0);
		type = Types.get(segments[5]);
		if (header) {
			headers = StringUtils.splitByWholeSeparatorPreserveAllTokens(segments[6], SUB_DELIMITER);
		} else {

			headers = new String[cols];
			Arrays.fill(headers, "");
		}
	}

	/**
	 * Gets the documentation.
	 *
	 * @return the documentation
	 */
	@Override
	public IGamlDocumentation getDocumentation() {
		GamlRegularDocumentation sb = new GamlRegularDocumentation();
		sb.append("CSV File ").append(header ? "with header" : "no header").append(StringUtils.LF);
		sb.append("Dimensions: ").append(cols + " columns x " + (header ? rows - 1 : rows) + " rows")
				.append(StringUtils.LF);
		sb.append("Delimiter: ").append(delimiter).append(StringUtils.LF).append("Contents type: ")
				.append(type.toString()).append(StringUtils.LF);
		if (header && headers != null) {
			sb.append("Headers: ");
			for (final String header2 : headers) { sb.append(header2).append(" | "); }
		}
		return sb;
	}

	/**
	 * Append suffix.
	 *
	 * @param sb
	 *            the sb
	 */
	@Override
	public void appendSuffix(final StringBuilder sb) {
		sb.append(cols).append("x").append(rows).append(SUFFIX_DEL);
		sb.append(header ? "with header" : "no header").append(SUFFIX_DEL);
		sb.append("delimiter: '").append(delimiter).append("'").append(SUFFIX_DEL).append(type);
	}

	/**
	 * @return
	 */
	@Override
	public String toPropertyString() {
		return super.toPropertyString() + DELIMITER + cols + DELIMITER + rows + DELIMITER + header + DELIMITER
				+ delimiter + DELIMITER + type + (header ? DELIMITER + String.join(SUB_DELIMITER, headers) : "");
	}

}
