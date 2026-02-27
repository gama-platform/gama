/*******************************************************************************************************
 *
 * GamaFileType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.additions.IGamaGetter;
import gama.api.constants.IKeyword;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.file.GamaFolderFile;
import gama.api.types.file.IGamaFile;
import gama.api.types.misc.IContainer;

/**
 * Generic supertype representing files in GAML - the foundation for all file I/O operations.
 * <p>
 * The file type system in GAMA is extensible and supports numerous file formats through specialized subtypes. Each
 * file type is associated with specific file extensions and provides type-safe operations for reading and writing
 * data. Files act as containers, with their contents accessible through GAML operations.
 * </p>
 * 
 * <h2>Key Features:</h2>
 * <ul>
 * <li>Extensible type system for multiple file formats</li>
 * <li>Extension-based file type detection</li>
 * <li>Container interface for file contents</li>
 * <li>Read and write operations</li>
 * <li>Type-specific parsing and serialization</li>
 * <li>Support for both files and directories</li>
 * </ul>
 * 
 * <h2>Built-in File Types:</h2>
 * <ul>
 * <li>Text files (.txt, .text, .data)</li>
 * <li>CSV files (.csv)</li>
 * <li>Shapefiles (.shp) - GIS vector data</li>
 * <li>Image files (.jpg, .png, .gif, .bmp, etc.)</li>
 * <li>Grid files (.asc, .tif) - raster data</li>
 * <li>Graph files (.dot, .graphml, etc.)</li>
 * <li>JSON files (.json)</li>
 * <li>XML files (.xml)</li>
 * <li>Property files (.properties)</li>
 * <li>GAML model files (.gaml)</li>
 * <li>And many more...</li>
 * </ul>
 * 
 * <h2>Usage Examples:</h2>
 * 
 * <pre>
 * {@code
 * // Load a shapefile
 * file shape_file <- file("../includes/roads.shp");
 * geometry roads <- shape_file;
 * 
 * // Read CSV data
 * file data_file <- csv_file("data.csv", true);  // true = has header
 * matrix data <- matrix(data_file);
 * 
 * // Save to file
 * save agent_list to: "output.csv" type: csv;
 * 
 * // Image file
 * file img <- image_file("background.png");
 * 
 * // Generic file (auto-detects type)
 * file my_file <- file("data.json");
 * 
 * // Check file properties
 * bool exists <- my_file.exists;
 * string path <- my_file.path;
 * 
 * // Folder/directory
 * file folder <- folder("../includes");
 * list<file> contents <- folder.contents;
 * }
 * </pre>
 * 
 * <h2>Extension System:</h2>
 * <p>
 * New file types can be registered through the plugin system using the {@link #addFileTypeDefinition} method. Each
 * file type is associated with file extensions, a content type, and a builder for creating file instances.
 * </p>
 * 
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @see GamaContainerType
 * @see IGamaFile
 * @see ParametricFileType
 * @since GAMA 1.0
 */
@type (
		name = IKeyword.FILE,
		id = IType.FILE,
		wraps = { IGamaFile.class },
		kind = ISymbolKind.CONTAINER,
		concept = { IConcept.TYPE, IConcept.FILE },
		doc = @doc ("Generic super-type of all file types"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaFileType extends GamaContainerType<IGamaFile> {

	/**
	 * Constructs a new file type.
	 * 
	 * @param typesManager
	 *            the types manager responsible for type resolution and management
	 */
	public GamaFileType(final ITypesManager typesManager) {
		super(typesManager);
	}

	/** Maps file extensions to their corresponding ParametricFileType. */
	public static final Map<String, ParametricFileType> extensionsToFullType = new HashMap<>();

	/** Maps file type aliases to their corresponding ParametricFileType. */
	static final Map<String, ParametricFileType> aliasesToFullType = new HashMap<>();

	/** Maps file type aliases to their supported file extensions. */
	static final Multimap<String, String> aliasesToExtensions = HashMultimap.<String, String> create();

	/** Counter for assigning unique type IDs to file types. */
	private static int currentFileTypeIndex = 0;

	/**
	 * Registers a new file type definition in the type system.
	 * <p>
	 * This method is used by plugins to register support for additional file formats. Each file type is associated
	 * with a type alias, supported extensions, and a builder for creating file instances.
	 * </p>
	 * 
	 * @param alias
	 *            the type alias used in GAML (e.g., "csv", "shapefile", "image")
	 * @param bufferType
	 *            the container type used to store file contents internally
	 * @param keyType
	 *            the type of keys used to index file contents (e.g., int for lists)
	 * @param contentType
	 *            the type of elements stored in the file
	 * @param clazz
	 *            the Java class implementing this file type
	 * @param builder
	 *            a getter/factory for creating instances of this file type
	 * @param extensions
	 *            array of file extensions associated with this type (without leading dots)
	 * @param plugin
	 *            the plugin ID that defines this file type
	 */
	public static void addFileTypeDefinition(final String alias, final IType<?> bufferType, final IType<?> keyType,
			final IType<?> contentType, final Class clazz, final IGamaGetter<IGamaFile<?, ?>> builder,
			final String[] extensions, final String plugin) {
		// Added to ensure that extensions do not begin with a "." or contain
		// blank characters
		for (final String ext : extensions) {
			String clean = ext.toLowerCase();
			if (clean.startsWith(".")) { clean = clean.substring(1); }
			aliasesToExtensions.put(alias, clean);
		}

		// classToExtensions.put(clazz, exts);
		final ParametricFileType t = new ParametricFileType(alias + "_file", clazz, builder, bufferType, keyType,
				contentType, provideNewIndex());
		t.setDefiningPlugin(plugin);
		aliasesToFullType.put(alias, t);
		for (final String s : aliasesToExtensions.get(alias)) { extensionsToFullType.put(s, t); }
		t.setParent(Types.FILE);
		Types.getBuiltInTypeManager().addRegularType(t.getName(), t, plugin);
	}

	/**
	 * Retrieves the file type associated with a type alias.
	 * 
	 * @param alias
	 *            the file type alias (e.g., "csv", "shapefile")
	 * @return the ParametricFileType for the alias, or the generic file type if not found
	 */
	public static ParametricFileType getTypeFromAlias(final String alias) {
		final ParametricFileType ft = aliasesToFullType.get(alias);
		if (ft == null) return ParametricFileType.getGenericFileType();
		return ft;
	}

	/**
	 * Determines the file type from a filename based on its extension.
	 * 
	 * @param fileName
	 *            the file name or path
	 * @return the ParametricFileType matching the file extension, or the generic file type if not recognized
	 */
	public static ParametricFileType getTypeFromFileName(final String fileName) {
		final IPath p = new Path(fileName);
		final String ext = p.getFileExtension();
		ParametricFileType ft = extensionsToFullType.get(ext);
		if (ft == null) { ft = ParametricFileType.getGenericFileType(); }
		return ft;
	}

	/**
	 * Verifies if a file path has the correct extension for a given file type.
	 * 
	 * @param alias
	 *            the file type alias
	 * @param path
	 *            the file path to check
	 * @return true if the path's extension matches one of the type's registered extensions, false otherwise
	 */
	public static boolean verifyExtension(final String alias, final String path) {
		final ParametricFileType ft = getTypeFromAlias(alias);
		final ParametricFileType ft2 = getTypeFromFileName(path);
		return ft.equals(ft2);
	}

	/**
	 * Checks if a given file extension is managed by a registered file type.
	 * 
	 * @param ext
	 *            the file extension (without leading dot)
	 * @return true if the extension is registered, false otherwise
	 */
	public static boolean managesExtension(final String ext) {
		return extensionsToFullType.containsKey(ext);
	}

	/**
	 * Creates a file instance from a path, optionally including folders.
	 * <p>
	 * If the path points to a directory and includingFolders is true, returns a GamaFolderFile. Otherwise, determines
	 * the file type from the extension and creates an appropriate file instance.
	 * </p>
	 * 
	 * @param scope
	 *            the current execution scope
	 * @param path
	 *            the file path
	 * @param includingFolders
	 *            whether to create folder files for directories
	 * @param contents
	 *            optional initial contents for the file
	 * @return the created file instance, or null if path is a directory and includingFolders is false
	 */
	public static IGamaFile createFile(final IScope scope, final String path, final boolean includingFolders,
			final IContainer.Modifiable contents) {
		if (new File(path).isDirectory()) {
			if (includingFolders) return new GamaFolderFile(scope, path);
			return null;
		}
		final ParametricFileType ft = getTypeFromFileName(path);
		return ft.createFile(scope, path, contents);
	}

	/**
	 * Casts an object to a file.
	 * <p>
	 * Supports casting from:
	 * <ul>
	 * <li>IGamaFile - returns the file itself</li>
	 * <li>String - creates a file from the path (auto-detecting type from extension)</li>
	 * <li>String with container parameter - creates a file and initializes with the container contents</li>
	 * </ul>
	 * </p>
	 * 
	 * @param scope
	 *            the current execution scope
	 * @param obj
	 *            the object to cast to a file
	 * @param param
	 *            optional parameter (can be initial contents as a container)
	 * @param keyType
	 *            the key type (not used for file casting)
	 * @param contentType
	 *            the content type (not used for file casting)
	 * @param copy
	 *            whether to create a copy (not used for file casting)
	 * @return the file representation of the object, or the default (null) if casting fails
	 */
	@Override
	public IGamaFile cast(final IScope scope, final Object obj, final Object param, final IType keyType,
			final IType contentType, final boolean copy) {
		if (obj == null) return getDefault();
		// 04/03/14 Problem of initialization of files. See if it works or not.
		// No copy of the file is done.
		if (obj instanceof IGamaFile) return (IGamaFile) obj;
		if (obj instanceof String) {
			if (param == null) return createFile(scope, (String) obj, true, null);
			if (param instanceof IContainer.Modifiable)
				return createFile(scope, (String) obj, true, (IContainer.Modifiable) param);
		}
		return getDefault();
	}

	/**
	 * Indicates whether files can be cast to constant values.
	 * <p>
	 * Files cannot be constant as they represent external resources that may change.
	 * </p>
	 * 
	 * @return false, files are not constant
	 */
	@Override
	public boolean canCastToConst() {
		return false;
	}

	/**
	 * Determines the specific file type when casting an expression.
	 * <p>
	 * If the expression is constant and context-independent, analyzes its value to determine the specific file type
	 * based on the file extension.
	 * </p>
	 * 
	 * @param exp
	 *            the expression being cast to a file type
	 * @return the specific ParametricFileType if determinable, otherwise the generic file type
	 */
	@Override
	public IContainerType typeIfCasting(final IExpression exp) {
		if (exp.isConst() && exp.isContextIndependant()) {
			final String s = Cast.asString(null, exp.getConstValue());
			return getTypeFromFileName(s);
		}
		return super.typeIfCasting(exp);
	}

	/**
	 * Provides a new unique type ID for file type registration.
	 * 
	 * @return a unique type ID for a new file type
	 */
	public static int provideNewIndex() {
		return IType.BEGINNING_OF_FILE_TYPES + ++currentFileTypeIndex;
	}

}