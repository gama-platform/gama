/*******************************************************************************************************
 *
 * GamaFolderFile.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.file;

import static gama.api.types.list.GamaListFactory.createWithoutCasting;

import java.io.File;

import gama.annotations.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.types.GamaFileType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.misc.IContainer;
import gama.api.utils.geometry.IEnvelope;

/**
 * Represents a folder (directory) in the file system.
 * 
 * <p>
 * {@code GamaFolderFile} extends {@link GamaFile} to provide access to directory contents
 * as a list of file/folder names. It allows GAML models to explore directory structures,
 * list files, and perform operations on folders.
 * </p>
 * 
 * <h2>Key Features</h2>
 * <ul>
 * <li><b>Directory listing:</b> Returns names of all files and subdirectories</li>
 * <li><b>Container semantics:</b> Contents accessible as {@code IList<String>}</li>
 * <li><b>Validation:</b> Ensures the path points to an existing directory</li>
 * <li><b>Cross-platform:</b> Works across different operating systems</li>
 * </ul>
 * 
 * <h2>Content Structure</h2>
 * <p>
 * The folder contents are represented as:
 * </p>
 * <ul>
 * <li><b>Container type:</b> {@code IList<String>} - list of file/folder names</li>
 * <li><b>Element type:</b> {@code String} - each file or folder name (not full path)</li>
 * <li><b>Order:</b> Names are returned in the order provided by the file system</li>
 * <li><b>Hidden files:</b> Platform behavior varies (may or may not include hidden files)</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>In GAML</h3>
 * <pre>{@code
 * // Access a folder
 * folder my_folder <- folder("data");
 * 
 * // List all files in the folder
 * list<string> file_names <- my_folder.contents;
 * loop file_name over: file_names {
 *     write file_name;
 * }
 * 
 * // Check folder properties
 * write "Folder exists: " + my_folder.exists;
 * write "Number of files: " + length(my_folder);
 * 
 * // Iterate over files
 * loop fn over: my_folder {
 *     file f <- file("data/" + fn);
 *     // Process each file
 * }
 * }</pre>
 * 
 * <h3>In Java</h3>
 * <pre>{@code
 * // Create a folder file
 * GamaFolderFile folder = new GamaFolderFile(scope, "data");
 * 
 * // Get list of files
 * IList<String> fileNames = folder.getContents(scope);
 * 
 * // Process each file
 * for (String name : fileNames) {
 *     System.out.println("Found: " + name);
 * }
 * }</pre>
 * 
 * <h2>Creation and Validation</h2>
 * <p>
 * GamaFolderFile requires the folder to exist at creation time:
 * </p>
 * <ul>
 * <li>Throws {@link GamaRuntimeException} if the path doesn't exist</li>
 * <li>Throws {@link GamaRuntimeException} if the path points to a file (not a folder)</li>
 * <li>Use {@code new_folder} operator in GAML to create new folders</li>
 * </ul>
 * 
 * <h2>Differences from Regular Files</h2>
 * <ul>
 * <li>No {@code attributes} - folders don't have format-specific metadata</li>
 * <li>Contents are names only, not file objects</li>
 * <li>No writing support - use file system operations to modify folder contents</li>
 * <li>The {@code isfolder} attribute returns true</li>
 * </ul>
 * 
 * <h2>Common Use Cases</h2>
 * <ul>
 * <li>Batch processing of files in a directory</li>
 * <li>Discovering data files at runtime</li>
 * <li>Listing simulation outputs</li>
 * <li>Building file paths dynamically</li>
 * <li>Checking for file existence before loading</li>
 * </ul>
 * 
 * <h2>Limitations</h2>
 * <ul>
 * <li>Only returns immediate children (not recursive)</li>
 * <li>Returns names only, not full paths</li>
 * <li>No filtering or sorting of results</li>
 * <li>Cannot distinguish files from subdirectories in the list</li>
 * </ul>
 * 
 * <h2>See Also</h2>
 * <p>
 * For creating new folders, use GAML's {@code new_folder} operator:
 * </p>
 * <pre>{@code
 * folder new_dir <- new_folder("output");
 * }</pre>
 * 
 * @see GamaFile
 * @see IGamaFile
 * @see gama.api.types.list.IList
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 */
public class GamaFolderFile extends GamaFile<IList<String>, String> {

	/**
	 * Instantiates a new gama folder file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public GamaFolderFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName);
		// AD 27/04/13 Let the flags of the file remain the same. Can be turned
		// off and on using the "read" and
		// "write" operators, so no need to decide for a default here
		// setWritable(true);
	}

	/**
	 * Instantiates a new gama folder file.
	 *
	 * @param scope
	 *            the scope
	 * @param pn
	 *            the pn
	 * @param forReading
	 *            the for reading
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public GamaFolderFile(final IScope scope, final String pn, final boolean forReading) throws GamaRuntimeException {
		super(scope, pn, forReading);
	}

	@Override
	protected void checkValidity(final IScope scope) throws GamaRuntimeException {
		final File file = getFile(scope);
		if (file == null || !file.exists()) throw GamaRuntimeException.error(
				"The folder " + getFile(scope).getAbsolutePath() + " does not exist. Please use 'new_folder' instead",
				scope);
		if (!getFile(scope).isDirectory())
			throw GamaRuntimeException.error(getFile(scope).getAbsolutePath() + "is not a folder", scope);
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return IKeyword.FOLDER + "('" + /* StringUtils.toGamlString(getPath()) */getPath(null) + "')";
	}

	@Override
	public IList<String> getAttributes(final IScope scope) {
		// No attributes to speak of
		return GamaListFactory.create(Types.STRING);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.core.util.GamaFile#fillBuffer()
	 */
	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		if (getBuffer() != null) return;
		final String[] list = getFile(scope).list();
		final IList<String> result =
				list == null ? GamaListFactory.getEmptyList() : createWithoutCasting(Types.STRING, list);
		setBuffer(result);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.core.util.GamaFile#flushBuffer()
	 */
	@Override
	protected void flushBuffer(final IScope scope, final Facets facets) throws GamaRuntimeException {
		// Nothing to do
	}

	@SuppressWarnings ("rawtypes")
	@Override
	public IEnvelope computeEnvelope(final IScope scope) {
		final IContainer<Integer, String> files = getContents(scope);
		IEnvelope globalEnv = null;
		for (final String s : files.iterable(scope)) {
			final IGamaFile f = GamaFileType.createFile(scope, s, true, null);
			if (f != null) {
				final IEnvelope env = f.computeEnvelope(scope);
				if (globalEnv == null) {
					globalEnv = env;
				} else {
					globalEnv.expandToInclude(env);
				}
			}
		}
		return globalEnv;
	}

}
