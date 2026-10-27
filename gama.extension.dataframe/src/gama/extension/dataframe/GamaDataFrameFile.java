/*******************************************************************************************************
 *
 * GamaDataFrameFile.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.dataframe;

import java.io.File;

import org.apache.commons.csv.CSVFormat;
import org.dflib.avro.Avro;
import org.dflib.csv.Csv;
import org.dflib.excel.Excel;
import org.dflib.json.Json;
import org.dflib.parquet.Parquet;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.file;
import gama.annotations.support.IConcept;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.file.GamaFile;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.utils.geometry.IEnvelope;
import gama.api.utils.prefs.GamaPreferences;

/**
 * A file type that reads tabular data into a {@link IDataFrame}, exposing dataframe loading through GAMA's standard
 * file interface (like {@code csv_file}, {@code shape_file}, …) instead of ad-hoc operators.
 *
 * <p>
 * The actual format is chosen from the file extension: {@code csv}/{@code tsv} (with optional separator, header and
 * encoding), {@code xlsx}, {@code json}, {@code parquet} and {@code avro}. Only
 * {@code xlsx}/{@code parquet}/{@code avro} are registered for the generic {@code file(...)} resolver (the
 * {@code csv}/{@code tsv}/{@code json} extensions keep their existing {@code csv_file}/{@code json_file} meaning); the
 * CSV/JSON formats remain available here through the explicit {@code dataframe_file(...)} constructor.
 * </p>
 *
 * <p>
 * The buffer is a {@link GamaMutableDataFrame} so the file contents can be modified in place (e.g.
 * {@code add [..] to: my_file;}). It casts directly to a {@code dataframe}.
 * </p>
 */
@file (
		name = "dataframe",
		extensions = { "xlsx", "parquet", "avro" },
		buffer_type = IDataframeConstants.ID,
		buffer_index = IType.STRING,
		concept = { IConcept.FILE, IDataframeConstants.CONCEPT },
		doc = @doc ("A file that reads tabular data (csv, tsv, xlsx, json, parquet, avro) into a dataframe"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaDataFrameFile extends GamaFile<GamaMutableDataFrame, Object> {

	/** The CSV/TSV column separator (null → CSV separator preference). */
	private String separator;

	/** Whether the first CSV/TSV row is a header (null → true). */
	private Boolean header;

	/** The CSV/TSV character encoding (null → UTF-8). */
	private String encoding;

	/**
	 * Reads a dataframe from a file, the format being deduced from the extension.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path to the file
	 */
	@doc (
			value = "Reads a dataframe from a file. The format is deduced from the extension (xlsx, parquet, avro, csv, tsv, json).",
			examples = { @example (
					value = "dataframe d <- dataframe_file(\"../includes/data.xlsx\");",
					isExecutable = false) })
	public GamaDataFrameFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName);
	}

	/**
	 * Reads a dataframe from a CSV/TSV file using the given column separator.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path to the file
	 * @param separator
	 *            the column separator (single character)
	 */
	@doc (
			value = "Reads a dataframe from a CSV/TSV file with the given column separator.",
			examples = { @example (
					value = "dataframe d <- dataframe_file(\"../includes/data.csv\", \";\");",
					isExecutable = false) })
	public GamaDataFrameFile(final IScope scope, final String pathName, final String separator) {
		super(scope, pathName);
		this.separator = separator;
	}

	/**
	 * Reads a dataframe from a CSV/TSV file with a separator and header flag.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path to the file
	 * @param separator
	 *            the column separator (single character)
	 * @param header
	 *            whether the first row is a header
	 */
	@doc (
			value = "Reads a dataframe from a CSV/TSV file with a separator and a header flag.",
			examples = { @example (
					value = "dataframe d <- dataframe_file(\"../includes/data.csv\", \";\", true);",
					isExecutable = false) })
	public GamaDataFrameFile(final IScope scope, final String pathName, final String separator, final Boolean header) {
		super(scope, pathName);
		this.separator = separator;
		this.header = header;
	}

	/**
	 * Reads a dataframe from a CSV/TSV file with a separator, header flag and character encoding.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path to the file
	 * @param separator
	 *            the column separator (single character)
	 * @param header
	 *            whether the first row is a header
	 * @param encoding
	 *            the character encoding (e.g. "UTF-8", "ISO-8859-1")
	 */
	@doc (
			value = "Reads a dataframe from a CSV/TSV file with a separator, header flag and character encoding.",
			examples = { @example (
					value = "dataframe d <- dataframe_file(\"../includes/data.csv\", \";\", false, \"ISO-8859-1\");",
					isExecutable = false) })
	public GamaDataFrameFile(final IScope scope, final String pathName, final String separator, final Boolean header,
			final String encoding) {
		super(scope, pathName);
		this.separator = separator;
		this.header = header;
		this.encoding = encoding;
	}

	/**
	 * Stores a dataframe in memory (does not save it). The file can be written later with the {@code save} statement.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path to the file
	 * @param df
	 *            the dataframe to store
	 */
	@doc (
			value = "Stores a dataframe in a file object in memory (it does not save it). It can then be saved with the 'save' statement.",
			examples = { @example (
					value = "dataframe_file f <- dataframe_file(\"out.parquet\", my_dataframe);",
					isExecutable = false) })
	public GamaDataFrameFile(final IScope scope, final String pathName, final IDataFrame df) {
		super(scope, pathName, GamaDataFrameFactory.mutable(df));
	}

	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		if (getBuffer() != null) return;
		final String path = getPath(scope);
		final String ext = getExtension(scope);
		final IDataFrame df = switch (ext) {
			case "csv", "tsv" -> {
				final char sep = separator != null && !separator.isEmpty() ? separator.charAt(0)
						: GamaPreferences.External.CSV_SEPARATOR.value(scope).toString().charAt(0);
				yield GamaDataFrameFactory.fromCSV(scope, path, sep, header == null || header, encoding);
			}
			case "xlsx" -> GamaDataFrameFactory.fromExcel(scope, path);
			case "json" -> GamaDataFrameFactory.fromJson(scope, path);
			case "parquet" -> GamaDataFrameFactory.fromParquet(scope, path);
			case "avro" -> GamaDataFrameFactory.fromAvro(scope, path);
			default -> throw GamaRuntimeException.error("dataframe_file cannot read files with extension '." + ext
					+ "'. Supported formats: csv, tsv, xlsx, json, parquet, avro.", scope);
		};
		setBuffer(GamaDataFrameFactory.mutable(df));
	}

	@Override
	protected void flushBuffer(final IScope scope, final Facets facets) throws GamaRuntimeException {
		final GamaMutableDataFrame buffer = getBuffer();
		if (buffer == null) return;
		final org.dflib.DataFrame inner = buffer.getInner();
		final File file = getFile(scope);
		final String ext = getExtension(scope);
		switch (ext) {
			case "csv", "tsv" -> {
				final char sep = separator != null && !separator.isEmpty() ? separator.charAt(0)
						: GamaPreferences.External.CSV_SEPARATOR.value(scope).toString().charAt(0);
				Csv.saver().format(CSVFormat.DEFAULT.withDelimiter(sep)).save(inner, file);
			}
			case "xlsx" -> Excel.saveSheet(inner, file, "Sheet1");
			case "json" -> Json.save(inner, file);
			case "parquet" -> Parquet.save(inner, file);
			case "avro" -> Avro.save(inner, file);
			default -> throw GamaRuntimeException.error("dataframe_file cannot write files with extension '." + ext
					+ "'. Supported formats: csv, tsv, xlsx, json, parquet, avro.", scope);
		}
	}

	@Override
	public IList<String> getAttributes(final IScope scope) {
		getContents(scope);
		final GamaMutableDataFrame buffer = getBuffer();
		return buffer == null ? GamaListFactory.getEmptyList() : buffer.getColumns();
	}

	@Override
	public IEnvelope computeEnvelope(final IScope scope) {
		return null;
	}
}
