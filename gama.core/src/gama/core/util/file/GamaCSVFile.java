/*******************************************************************************************************
 *
 * GamaCSVFile.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.file;
import gama.annotations.support.IConcept;
import gama.api.GAMA;
import gama.api.data.csv.CsvReader;
import gama.api.data.factories.GamaListFactory;
import gama.api.data.factories.GamaMatrixFactory;
import gama.api.data.factories.GamaPointFactory;
import gama.api.data.objects.IEnvelope;
import gama.api.data.objects.IList;
import gama.api.data.objects.IMatrix;
import gama.api.data.objects.IPoint;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IStatusMessage;
import gama.api.utils.IFieldMatrixProvider;
import gama.api.utils.files.GamaFile;
import gama.api.utils.files.IFileMetadataProvider;
import gama.api.utils.files.IGamaFileMetaData;
import gama.core.util.matrix.GamaFloatMatrix;
import gama.core.util.matrix.GamaIntMatrix;
import gama.core.util.matrix.GamaObjectMatrix;

/**
 * Class GamaCSVFile.
 *
 * @author drogoul
 * @since 9 janv. 2014
 *
 */
@file (
		name = "csv",
		extensions = { "csv", "tsv" },
		buffer_type = IType.MATRIX,
		buffer_index = IType.POINT,
		concept = { IConcept.CSV, IConcept.FILE },
		doc = @doc ("A type of text file that contains comma-separated values"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaCSVFile extends GamaFile<IMatrix<Object>, Object> implements IFieldMatrixProvider {

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

	/**
	 * The Class CSVInfo.
	 */
	public static class CSVInfo {

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
		 * @param fileName
		 *            the file name
		 * @param modificationStamp
		 *            the modification stamp
		 * @param CSVsep
		 *            the CS vsep
		 */
		public CSVInfo(final String fileName, final long modificationStamp, final String CSVsep) {
			try (CsvReader reader = new CsvReader(fileName)) {
				process(reader, CSVsep);
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
			final String[] segments =
					StringUtils.splitByWholeSeparatorPreserveAllTokens(propertyString, IGamaFileMetaData.DELIMITER);
			cols = Integer.parseInt(segments[1]);
			rows = Integer.parseInt(segments[2]);
			header = Boolean.parseBoolean(segments[3]);
			delimiter = segments[4].charAt(0);
			type = Types.get(segments[5]);
			if (header) {
				headers = StringUtils.splitByWholeSeparatorPreserveAllTokens(segments[6],
						IGamaFileMetaData.SUB_DELIMITER);
			} else {

				headers = new String[cols];
				Arrays.fill(headers, "");
			}
		}

	}

	/** The csv separator. */
	String csvSeparator = null;

	/** The text qualifier. */
	Character textQualifier = '"';

	/** The contents type. */
	IType contentsType;

	/** The user size. */
	IPoint userSize;

	/** The has header. */
	Boolean hasHeader;

	/** The headers. */
	IList<String> headers;

	/** The info. */
	CSVInfo info;

	/**
	 * @param scope
	 * @param pathName
	 * @throws GamaRuntimeException
	 */
	@doc (
			value = "This file constructor allows to read a CSV file with the default separator (coma), no header, and no assumption on the type of data. No text qualifier will be used",
			examples = { @example (
					value = "csv_file f <- csv_file(\"file.csv\");",
					isExecutable = false) })
	public GamaCSVFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		this(scope, pathName, (String) null);
	}

	/**
	 * Instantiates a new gama CSV file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param withHeader
	 *            the with header
	 */
	@doc (
			value = "This file constructor allows to read a CSV file with the default separator (coma), with specifying if the model has a header or not (boolean), and no assumption on the type of data. No text qualifier will be used",
			examples = { @example (
					value = "csv_file f <- csv_file(\"file.csv\",true);",
					isExecutable = false) })
	public GamaCSVFile(final IScope scope, final String pathName, final Boolean withHeader) {
		this(scope, pathName);
		hasHeader = withHeader;
	}

	/**
	 * Instantiates a new gama CSV file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param separator
	 *            the separator
	 */
	@doc (
			value = "This file constructor allows to read a CSV file and specify the separator used, without making any assumption on the type of data. Headers should be detected automatically if they exist. No text qualifier will be used",
			examples = { @example (
					value = "csv_file f <- csv_file(\"file.csv\", \";\");",
					isExecutable = false) })
	public GamaCSVFile(final IScope scope, final String pathName, final String separator) {
		this(scope, pathName, separator, (IType) null);
	}

	/**
	 * Instantiates a new gama CSV file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param separator
	 *            the separator
	 * @param withHeader
	 *            the with header
	 */
	@doc (
			value = "This file constructor allows to read a CSV file and specify (1) the separator used; (2) if the model has a header or not, without making any assumption on the type of data. No text qualifier will be used",
			examples = { @example (
					value = "csv_file f <- csv_file(\"file.csv\", \";\",true);",
					isExecutable = false) })
	public GamaCSVFile(final IScope scope, final String pathName, final String separator, final Boolean withHeader) {
		this(scope, pathName, separator, (IType) null);
		hasHeader = withHeader;
	}

	/**
	 * Instantiates a new gama CSV file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param separator
	 *            the separator
	 * @param qualifier
	 *            the qualifier
	 * @param withHeader
	 *            the with header
	 */
	@doc (
			value = "This file constructor allows to read a CSV file and specify (1) the separator used; (2) the text qualifier used; (3) if the model has a header or not, without making any assumption on the type of data",
			examples = { @example (
					value = "csv_file f <- csv_file(\"file.csv\", ';', '\"', true);",
					isExecutable = false) })
	public GamaCSVFile(final IScope scope, final String pathName, final String separator, final String qualifier,
			final Boolean withHeader) {
		this(scope, pathName, separator, (IType) null);
		textQualifier = qualifier == null || qualifier.isEmpty() ? null : qualifier.charAt(0);
		hasHeader = withHeader;
	}

	/**
	 * Instantiates a new gama CSV file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param separator
	 *            the separator
	 * @param type
	 *            the type
	 */
	@doc (
			value = "This file constructor allows to read a CSV file with a given separator, no header, and the type of data. No text qualifier will be used",
			examples = { @example (
					value = "csv_file f <- csv_file(\"file.csv\", \";\",int);",
					isExecutable = false) })
	public GamaCSVFile(final IScope scope, final String pathName, final String separator, final IType type) {
		this(scope, pathName, separator, type, (Boolean) null);
	}

	/**
	 * Instantiates a new gama CSV file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param separator
	 *            the separator
	 * @param qualifier
	 *            the qualifier
	 * @param type
	 *            the type
	 */
	@doc (
			value = "This file constructor allows to read a CSV file and specify the separator, text qualifier to use, and the type of data to read.  Headers should be detected automatically if they exist.  ",
			examples = { @example (
					value = "csv_file f <- csv_file(\"file.csv\", ';', '\"', int);",
					isExecutable = false) })
	public GamaCSVFile(final IScope scope, final String pathName, final String separator, final String qualifier,
			final IType type) {
		this(scope, pathName, separator, type, (Boolean) null);
		textQualifier = qualifier == null || qualifier.isEmpty() ? null : qualifier.charAt(0);
	}

	/**
	 * Instantiates a new gama CSV file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param separator
	 *            the separator
	 * @param type
	 *            the type
	 * @param withHeader
	 *            the with header
	 */
	@doc (
			value = "This file constructor allows to read a CSV file with a given separator, the type of data, with specifying if the model has a header or not (boolean). No text qualifier will be used",
			examples = { @example (
					value = "csv_file f <- csv_file(\"file.csv\", \";\",int,true);",
					isExecutable = false) })
	public GamaCSVFile(final IScope scope, final String pathName, final String separator, final IType type,
			final Boolean withHeader) {
		this(scope, pathName, separator, type, (IPoint) null);
		hasHeader = withHeader;
	}

	/**
	 * Instantiates a new gama CSV file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param separator
	 *            the separator
	 * @param type
	 *            the type
	 * @param size
	 *            the size
	 */
	@doc (
			value = "This file constructor allows to read a CSV file with a given separator, the type of data, with specifying the number of cols and rows taken into account. No text qualifier will be used",
			examples = { @example (
					value = "csv_file f <- csv_file(\"file.csv\", \";\",int,true, {5, 100});",
					isExecutable = false) })
	public GamaCSVFile(final IScope scope, final String pathName, final String separator, final IType type,
			final IPoint size) {
		super(scope, pathName);
		setCsvSeparators(separator);
		contentsType = type;
		userSize = size;
	}

	/**
	 * Instantiates a new gama CSV file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param matrix
	 *            the matrix
	 */
	@doc (
			value = "This file constructor allows to store a matrix in a CSV file (it does not save it - just store it in memory),",
			examples = { @example (
					value = "csv_file f <- csv_file(\"file.csv\", matrix([10,10],[10,10]));",
					isExecutable = false) })

	public GamaCSVFile(final IScope scope, final String pathName, final IMatrix<Object> matrix) {
		super(scope, pathName, matrix);
		if (matrix != null) {
			userSize = matrix.getDimensions();
			contentsType = matrix.getGamlType().getContentType();
		}
	}

	/**
	 * Sets the csv separators.
	 *
	 * @param string
	 *            the new csv separators
	 */
	public void setCsvSeparators(final String string) {
		if (string == null) return;
		if (string.length() >= 1) { csvSeparator = string; }
	}

	@Override
	public IList<String> getAttributes(final IScope scope) {
		if (getBuffer() == null) {
			final CSVInfo cvsInfo = getInfo(scope, null);
			if (cvsInfo != null) return cvsInfo.header ? GamaListFactory.wrap(Types.STRING, cvsInfo.headers)
					: GamaListFactory.getEmptyList();
		}
		fillBuffer(scope);
		return headers == null ? GamaListFactory.getEmptyList() : headers;
	}

	/**
	 * Gets the info.
	 *
	 * @param scope
	 *            the scope
	 * @param CSVSep
	 *            the CSV sep
	 * @return the info
	 */
	public CSVInfo getInfo(final IScope scope, final String CSVSep) {
		if (info != null) return info;
		final IFileMetadataProvider p = GAMA.getMetadataProvider();
		if (p != null) {
			final IGamaFileMetaData metaData = p.getMetaData(getFile(scope), false, true);
			if (metaData != null) {
				try {
					info = new CSVInfo(metaData.toPropertyString());
					if (CSVSep != null && info != null && !info.delimiter.equals(CSVSep.charAt(0))) { info = null; }
				} catch (Exception e) {
					info = null;
				}
			}
		}
		if (info == null) {
			info = new CSVInfo(getFile(scope).getAbsolutePath(), 0, CSVSep);
			// if (p != null) {
			// p.storeMetadata(getFile(), info);
			// }

		}
		if (hasHeader != null && hasHeader) {
			if (!info.header) {
				try (final CsvReader reader = new CsvReader(getPath(scope), info.delimiter)) {
					if (reader.readHeaders()) { info.headers = reader.getHeaders(); }
				} catch (final IOException e) {}
			}
			info.header = hasHeader;
		}
		return info;
	}

	@Override
	public void fillBuffer(final IScope scope) {
		if (getBuffer() != null) return;
		if (csvSeparator == null || contentsType == null || userSize == null) {
			scope.getGui().getStatus().beginTask("Opening file " + getName(scope), IStatusMessage.DOWNLOAD_ICON);
			final CSVInfo stats = getInfo(scope, csvSeparator);
			csvSeparator = csvSeparator == null ? "" + stats.delimiter : csvSeparator;
			contentsType = contentsType == null ? stats.type : contentsType;
			if (userSize == null) { userSize = GamaPointFactory.create(stats.cols, stats.rows); }

			// AD We take the decision for the modeler is he/she hasn't
			// specified if the header must be read or not.
			hasHeader = hasHeader == null ? stats.header : hasHeader;
			scope.getGui().getStatus().endTask("", IStatusMessage.DOWNLOAD_ICON);
		}
		try (CsvReader reader = new CsvReader(getPath(scope), csvSeparator.charAt(0))) {
			reader.setTextQualifier(textQualifier);
			if (hasHeader) {
				reader.readHeaders();
				headers = GamaListFactory.createWithoutCasting(Types.STRING, reader.getHeaders());
				// we remove one row so as to not read the headers as well
				// Cause for issue #3036
				userSize.add(0, -1, 0);
				// Make sure that we do not read more columns than the number of headers
				userSize.setX(headers.size());
			}
			// long t = System.currentTimeMillis();
			setBuffer(createMatrixFrom(scope, reader));
			// DEBUG.LOG("CSV stats: " + userSize.x * userSize.y + "
			// cells read in " +
			// (System.currentTimeMillis() - t) + " ms");
		} catch (final IOException e) {
			throw GamaRuntimeException.create(e, scope);
		} finally {
			// See Issue #3036 -- value must be modified when the file is reloaded
			if (hasHeader != null && hasHeader) { userSize.add(0, 1, 0); }
		}

	}

	/**
	 * Creates the matrix from.
	 *
	 * @param scope
	 *            the scope
	 * @param reader
	 *            the reader
	 * @return the i matrix
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private IMatrix createMatrixFrom(final IScope scope, final CsvReader reader) throws IOException {
		final int t = contentsType.id();
		double percentage = 0;
		IMatrix matrix;
		try {
			String task = "Reading file " + getName(scope);
			scope.getGui().getStatus().beginTask(task, IStatusMessage.DOWNLOAD_ICON);
			if (t == IType.INT) {
				matrix = GamaMatrixFactory.createIntMatrix((int) userSize.getX(), (int) userSize.getY());
				final int[] m = ((GamaIntMatrix) matrix).getMatrix();
				int i = 0;
				while (reader.readRecord()) {
					percentage = reader.getCurrentRecord() / userSize.getY();
					scope.getGui().getStatus().setTaskCompletion(task, percentage);
					int nbC = 0;
					for (final String s : reader.getValues()) {
						m[i++] = Cast.asInt(scope, s);
						nbC++;
					}
					while (nbC < matrix.getCols(null)) {
						m[i++] = 0;
						nbC++;
					}
				}
			} else if (t == IType.FLOAT) {
				matrix = GamaMatrixFactory.createFloatMatrix((int) userSize.getX(), (int) userSize.getY());
				final double[] m = ((GamaFloatMatrix) matrix).getMatrix();
				int i = 0;
				while (reader.readRecord()) {
					percentage = reader.getCurrentRecord() / userSize.getY();
					scope.getGui().getStatus().setTaskCompletion(task, percentage);
					int nbC = 0;
					for (final String s : reader.getValues()) {
						m[i++] = Cast.asFloat(scope, s);
						nbC++;
					}
					while (nbC < matrix.getCols(null)) {
						m[i++] = 0.0;
						nbC++;
					}
				}
			} else {
				matrix = GamaMatrixFactory.create((int) userSize.getX(), (int) userSize.getY(), Types.STRING);
				final Object[] m = ((GamaObjectMatrix) matrix).getMatrix();
				int i = 0;
				while (reader.readRecord()) {
					percentage = reader.getCurrentRecord() / userSize.getY();
					scope.getGui().getStatus().setTaskCompletion(task, percentage);
					int nbC = 0;

					for (final String s : reader.getValues()) {
						if (i == m.length) {
							GAMA.reportError(scope, GamaRuntimeException.warning("The file " + getFile(scope).getName()
									+ " seems to contain data that have not been processed", scope), false);
							break;
						}
						nbC++;
						m[i++] = s;
					}
					while (nbC < matrix.getCols(null)) {
						m[i++] = null;
						nbC++;
					}
				}
			}

			return matrix;
		} finally {
			scope.getGui().getStatus().endTask("Reading CSV File", IStatusMessage.DOWNLOAD_ICON);
		}
	}

	/**
	 * Method computeEnvelope()
	 *
	 * @see gama.api.utils.files.IGamaFile#computeEnvelope(gama.api.runtime.scope.IScope)
	 */
	@Override
	public IEnvelope computeEnvelope(final IScope scope) {
		// See how to read information from there
		return null;
	}

	/**
	 * @param asBool
	 */
	public void forceHeader(final Boolean asBool) {
		hasHeader = asBool;
	}

	/**
	 * Checks for header.
	 *
	 * @return the boolean
	 */
	public Boolean hasHeader(final IScope scope) {
		fillBuffer(scope);
		return hasHeader == null ? false : hasHeader;
	}

	@Override
	public int getRows(final IScope scope) {
		return getInfo(scope, null).rows;
	}

	@Override
	public int getCols(final IScope scope) {
		return getInfo(scope, null).cols;
	}

	@Override
	public double[] getBand(final IScope scope, final int index) {
		if (index > 0) return null;
		GamaFloatMatrix m = (GamaFloatMatrix) GamaMatrixFactory.createFromMatrix(scope, getContents(scope), Types.FLOAT,
				null, false);
		return m.getMatrix();

	}

}
