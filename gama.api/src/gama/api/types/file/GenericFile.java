/*******************************************************************************************************
 *
 * GenericFile.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.utils.geometry.GamaEnvelopeFactory;
import gama.api.utils.geometry.IEnvelope;

/**
 * Represents a generic text file with line-based content access.
 * 
 * <p>
 * {@code GenericFile} is the default file type used when no specific file type can be determined
 * from the file extension. It treats files as collections of text lines, providing access to
 * file contents as a list of strings. Each element in the list represents one line from the file.
 * </p>
 * 
 * <h2>Key Features</h2>
 * <ul>
 * <li><b>Line-based reading:</b> File contents are split into lines automatically</li>
 * <li><b>Binary detection:</b> Attempts to detect binary files and warns users</li>
 * <li><b>Simple access:</b> Contents exposed as {@code IList<String>} for easy iteration</li>
 * <li><b>Generic fallback:</b> Used when no specialized file type matches the extension</li>
 * </ul>
 * 
 * <h2>Content Structure</h2>
 * <p>
 * The file contents are represented as:
 * </p>
 * <ul>
 * <li><b>Container type:</b> {@code IList<String>} - an ordered list of lines</li>
 * <li><b>Element type:</b> {@code String} - each line as a text string</li>
 * <li><b>Line breaks:</b> Platform-specific line endings are handled automatically</li>
 * <li><b>Empty lines:</b> Preserved in the list (represented as empty strings)</li>
 * </ul>
 * 
 * <h2>Binary File Detection</h2>
 * <p>
 * GenericFile attempts to detect binary files using a heuristic approach:
 * </p>
 * <ul>
 * <li>Reads the first 1024 bytes of the file</li>
 * <li>Checks for control characters (bytes < 0x09)</li>
 * <li>Calculates the ratio of non-ASCII to ASCII characters</li>
 * <li>Issues a warning if the file appears to be binary</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>In GAML</h3>
 * <pre>{@code
 * // Read a generic text file
 * file my_file <- file("data.txt");
 * 
 * // Iterate over lines
 * loop line over: my_file {
 *     write line;
 * }
 * 
 * // Access specific lines
 * string first_line <- my_file[0];
 * int line_count <- length(my_file);
 * }</pre>
 * 
 * <h3>In Java</h3>
 * <pre>{@code
 * // Create and read a generic file
 * GenericFile file = new GenericFile(scope, "data.txt");
 * IList<String> lines = file.getContents(scope);
 * 
 * // Process lines
 * for (String line : lines) {
 *     System.out.println(line);
 * }
 * 
 * // Add new lines
 * lines.add("New line");
 * file.setContents(lines);
 * }</pre>
 * 
 * <h2>When to Use</h2>
 * <p>
 * Use GenericFile for:
 * </p>
 * <ul>
 * <li>Plain text files without specific structure</li>
 * <li>Log files</li>
 * <li>Configuration files</li>
 * <li>Any text file when structure-specific parsing isn't needed</li>
 * <li>Files with unknown or unsupported extensions</li>
 * </ul>
 * 
 * <h2>Limitations</h2>
 * <ul>
 * <li>No parsing of structured formats (use CSV, JSON, XML file types for those)</li>
 * <li>No column/field separation (entire line is one string)</li>
 * <li>Binary files generate warnings but no structured data</li>
 * <li>Very large files are loaded entirely into memory</li>
 * </ul>
 * 
 * <h2>Writing Support</h2>
 * <p>
 * GenericFile supports basic writing through the standard {@code save} operation,
 * though the {@link #flushBuffer(IScope, Facets)} method is currently a no-op.
 * For writing text files, consider using GAML's save statement with explicit format.
 * </p>
 * 
 * @see GamaFile
 * @see IGamaFile
 * @see gama.api.types.list.IList
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 */
public class GenericFile extends GamaFile<IList<String>, String> {

	/**
	 * Instantiates a new generic file.
	 *
	 * @param pathName
	 *            the path name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public GenericFile(final String pathName) throws GamaRuntimeException {
		super(GAMA.getRuntimeScope(), pathName);
	}

	/**
	 * Instantiates a new generic file.
	 *
	 * @param pathName
	 *            the path name
	 * @param shouldExist
	 *            the should exist
	 */
	public GenericFile(final String pathName, final boolean shouldExist) {
		super(GAMA.getRuntimeScope(), pathName, shouldExist);
	}

	/**
	 * Instantiates a new generic file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public GenericFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName, false);
	}

	@Override
	public IEnvelope computeEnvelope(final IScope scope) {
		return GamaEnvelopeFactory.EMPTY;
	}

	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		if (getBuffer() != null) return;
		try {
			if (isBinaryFile(scope)) {
				GAMA.reportAndThrowIfNeeded(scope,
						GamaRuntimeException.warning(
								"Problem identifying the contents of " + getFile(scope).getAbsolutePath(), scope),
						false);
				setBuffer(GamaListFactory.getEmptyList());
			} else {
				try (final BufferedReader in = new BufferedReader(new FileReader(getFile(scope)))) {
					final IList<String> allLines = GamaListFactory.create(Types.STRING);
					String str = in.readLine();
					while (str != null) {
						allLines.add(str);
						str = in.readLine();
					}
					setBuffer(allLines);
				}
			}
		} catch (IOException e) {
			throw GamaRuntimeException.create(e, scope);
		}

	}

	/**
	 * Detects whether the file contains binary data using heuristic analysis.
	 * 
	 * <p>
	 * This method attempts to determine if a file is binary by examining its first 1024 bytes
	 * (or fewer if the file is smaller). The detection uses two criteria:
	 * </p>
	 * <ol>
	 * <li><b>Control character check:</b> Any byte below 0x09 (except valid whitespace) indicates binary</li>
	 * <li><b>Non-ASCII ratio:</b> If more than 95% of characters are non-ASCII, the file is likely binary</li>
	 * </ol>
	 * 
	 * <p>
	 * Valid text characters include:
	 * </p>
	 * <ul>
	 * <li>Tab (0x09)</li>
	 * <li>Line feed (0x0A)</li>
	 * <li>Form feed (0x0C)</li>
	 * <li>Carriage return (0x0D)</li>
	 * <li>Printable ASCII (0x20-0x7E)</li>
	 * </ul>
	 * 
	 * <p>
	 * <b>Note:</b> This is a heuristic method and may produce false positives/negatives for:
	 * </p>
	 * <ul>
	 * <li>Files with extended character encodings (UTF-16, UTF-32)</li>
	 * <li>Very short files</li>
	 * <li>Text files with many special characters</li>
	 * </ul>
	 *
	 * @param scope
	 *            the current execution scope (for error reporting)
	 * @return true if the file appears to be binary, false if it appears to be text
	 * @throws FileNotFoundException
	 *             if the file does not exist
	 * @throws IOException
	 *             if an I/O error occurs while reading the file
	 */
	public boolean isBinaryFile(final IScope scope) throws FileNotFoundException, IOException {
		File f = getFile(scope);
		if (f == null || !f.exists()) return false;
		try (InputStream in = Files.newInputStream(f.toPath())) {
			int ascii = 0;
			int other = 0;
			for (final byte b : in.readNBytes(Math.min(1024, in.available()))) {
				if (b < 0x09) return true;
				if (b == 0x09 || b == 0x0A || b == 0x0C || b == 0x0D || b >= 0x20 && b <= 0x7E) {
					ascii++;
				} else {
					other++;
				}
			}
			return other == 0 ? false : 100 * other / (ascii + other) > 95;
		}
	}

	@Override
	protected void flushBuffer(final IScope scope, final Facets facets) throws GamaRuntimeException {}

}