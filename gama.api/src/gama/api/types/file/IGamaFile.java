/*******************************************************************************************************
 *
 * IGamaFile.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.file;

import org.eclipse.emf.common.util.URI;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.getter;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.annotations.support.ITypeProvider;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IShape;
import gama.api.types.list.IList;
import gama.api.types.misc.IContainer;
import gama.api.types.misc.IRuntimeContainer;
import gama.api.ui.displays.IAsset;
import gama.api.utils.geometry.IEnvelopeProvider;

/**
 * The root interface for all file types in the GAMA modeling platform.
 * 
 * <p>
 * {@code IGamaFile} provides a unified abstraction for accessing and manipulating files within GAMA models.
 * It extends both {@link gama.api.types.misc.IRuntimeContainer.Addressable} and
 * {@link gama.api.types.misc.IRuntimeContainer.Modifiable} to enable file contents to be treated as addressable
 * and modifiable containers, allowing natural integration with GAML's container operations.
 * </p>
 * 
 * <h2>Type Parameters</h2>
 * <ul>
 * <li>{@code C} - The container type used to store file contents (e.g., IList, IMatrix, IMap)</li>
 * <li>{@code Contents} - The type of individual elements within the container</li>
 * </ul>
 * 
 * <h2>Key Responsibilities</h2>
 * <ul>
 * <li><b>File Metadata:</b> Provides access to file properties (name, path, extension, existence)</li>
 * <li><b>Content Access:</b> Manages lazy loading and buffering of file contents</li>
 * <li><b>Container Semantics:</b> Exposes file data as GAML containers for iteration and manipulation</li>
 * <li><b>I/O Operations:</b> Supports both reading and writing file data</li>
 * <li><b>Attributes:</b> Provides access to file-specific metadata (e.g., CSV headers, shapefile fields)</li>
 * </ul>
 * 
 * <h2>File Attributes</h2>
 * <p>
 * All files expose the following attributes accessible in GAML via dot notation:
 * </p>
 * <ul>
 * <li>{@code name} - The file name without path</li>
 * <li>{@code path} - The absolute file path</li>
 * <li>{@code extension} - The file extension (without dot)</li>
 * <li>{@code exists} - Whether the file exists in the file system</li>
 * <li>{@code readable} - Whether the file can be read</li>
 * <li>{@code writable} - Whether the file can be written</li>
 * <li>{@code isfolder} - Whether this represents a folder/directory</li>
 * <li>{@code attributes} - File-specific attributes (varies by file type)</li>
 * <li>{@code contents} - The file contents as a container</li>
 * </ul>
 * 
 * <h2>Lifecycle</h2>
 * <ol>
 * <li><b>Creation:</b> File object is created with a path (local or URL)</li>
 * <li><b>Validation:</b> Path is resolved and validated</li>
 * <li><b>Lazy Loading:</b> Contents are not loaded until first access</li>
 * <li><b>Buffering:</b> Once loaded, contents remain in memory (buffer)</li>
 * <li><b>Writing:</b> Modified contents can be flushed back to disk</li>
 * </ol>
 * 
 * <h2>Specialized File Types</h2>
 * <p>
 * GAMA provides specialized interfaces and implementations for specific file types:
 * </p>
 * <ul>
 * <li>{@link Drawable} - Marker interface for files that can be displayed visually</li>
 * <li>{@link WithGeometry} - Files containing geospatial data (e.g., shapefiles, GeoJSON)</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // In Java - working with a file
 * IGamaFile<IList<String>, String> file = new GenericFile(scope, "data.txt");
 * 
 * // Check if file exists
 * if (file.exists(scope)) {
 *     // Get contents as a list
 *     IList<String> lines = file.getContents(scope);
 *     
 *     // Iterate over lines
 *     for (String line : lines) {
 *         System.out.println(line);
 *     }
 *     
 *     // Modify and save
 *     lines.add("New line");
 *     file.setContents(lines);
 *     // Flush to disk via save operation
 * }
 * 
 * // In GAML - natural container syntax
 * file my_file <- file("data.txt");
 * loop line over: my_file {
 *     write line;
 * }
 * }</pre>
 * 
 * <h2>Implementation Notes</h2>
 * <ul>
 * <li>Implementations should handle both local file paths and URLs</li>
 * <li>Path resolution must account for relative paths and model locations</li>
 * <li>Buffering should be lazy to avoid unnecessary I/O</li>
 * <li>Error handling should provide clear feedback about file issues</li>
 * </ul>
 * 
 * @param <C>
 *            the runtime container type for storing file contents (must be both Addressable and Modifiable)
 * @param <Contents>
 *            the type of individual elements within the container
 * 
 * @see gama.api.types.file.GamaFile
 * @see gama.api.types.file.GenericFile
 * @see gama.api.types.misc.IContainer
 * @see gama.api.gaml.types.GamaFileType
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 */
@vars ({ @variable (
		name = IKeyword.NAME,
		type = IType.STRING,
		doc = { @doc ("Returns the name of the receiver file") }),
		@variable (
				name = IKeyword.EXTENSION,
				type = IType.STRING,
				doc = { @doc ("Returns the extension of the receiver file") }),
		@variable (
				name = IKeyword.PATH,
				type = IType.STRING,
				doc = { @doc ("Returns the absolute path of the receiver file") }),
		@variable (
				name = IKeyword.EXISTS,
				type = IType.BOOL,
				doc = { @doc ("Returns whether the receiver file exists or not in the filesystem") }),
		@variable (
				name = IKeyword.ISFOLDER,
				type = IType.BOOL,
				doc = { @doc ("Returns whether the receiver file is a folder or not") }),
		@variable (
				name = IKeyword.READABLE,
				type = IType.BOOL,
				doc = { @doc ("Returns true if the contents of the receiver file can be read") }),
		@variable (
				name = IKeyword.WRITABLE,
				type = IType.BOOL,
				doc = { @doc ("Returns true if the contents of the receiver file can be written") }),
		@variable (
				name = IKeyword.ATTRIBUTES,
				type = IType.LIST,
				of = IType.STRING,
				doc = { @doc ("Retrieves the list of 'attributes' present in the receiver files that support this concept (and an empty list for the others). For instance, in a CSV file, the attributes represent the headers of the columns (if any); in a shape file, the attributes provided to the objects, etc.") }),
		@variable (
				name = IKeyword.CONTENTS,
				type = ITypeProvider.WRAPPED,
				of = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
				index = ITypeProvider.KEY_TYPE_AT_INDEX + 1,
				doc = { @doc ("Returns the contents of the receiver file in the form of a container") }),
		@variable (
				name = IKeyword.TEXT,
				type = IType.STRING,
				doc = { @doc ("Returns the whole content of the receiver file as a single string") }) })
@SuppressWarnings ({ "rawtypes" })
public interface IGamaFile<C extends IRuntimeContainer.Modifiable, Contents>
		extends IRuntimeContainer.Addressable, IRuntimeContainer.Modifiable, IEnvelopeProvider, IAsset {

	/**
	 * Marker interface for file types that can be rendered visually in displays.
	 * 
	 * <p>
	 * Files implementing this interface can be directly used in GAML display statements
	 * (e.g., image files, 3D mesh files). This allows them to be drawn or rendered in
	 * graphical displays without additional conversion.
	 * </p>
	 * 
	 * @see gama.api.ui.displays.IAsset
	 */
	interface Drawable {

	}

	/**
	 * Specialized interface for files containing geospatial/geometric data.
	 * 
	 * <p>
	 * Files implementing this interface contain shape/geometry information and can provide
	 * their contents as a list of {@link IShape} objects. This includes file types like:
	 * </p>
	 * <ul>
	 * <li>Shapefiles (.shp)</li>
	 * <li>GeoJSON files (.geojson, .json)</li>
	 * <li>KML/KMZ files</li>
	 * <li>OSM (OpenStreetMap) files</li>
	 * <li>GeoTIFF files (with vector data)</li>
	 * </ul>
	 * 
	 * <p>
	 * These files are both {@link Drawable} (can be displayed) and provide geometric
	 * operations through the {@link IShape} interface.
	 * </p>
	 * 
	 * @see gama.api.types.geometry.IShape
	 * @see gama.api.utils.geometry.IEnvelopeProvider
	 */
	interface WithGeometry extends IGamaFile<IList<IShape>, IShape>, Drawable {

		/**
		 * Returns the combined geometry of all shapes in this file.
		 * 
		 * <p>
		 * This method computes and returns a single {@link IShape} that represents
		 * the union or collection of all geometric features contained in the file.
		 * For files with multiple features, this is typically a multi-geometry or
		 * geometry collection.
		 * </p>
		 *
		 * @param scope
		 *            the current execution scope
		 * @return a shape representing all geometries in the file, or null if the file contains no geometries
		 */
		IShape getGeometry(IScope scope);

	}

	/**
	 * The "temporary output" key. Used to indicate in the scope (see {@link IScope#setData(String, Object)} that the
	 * current file is created for serving as an output file, for saving data
	 */
	String KEY_TEMPORARY_OUTPUT = "key_temporary_output";

	/**
	 * Sets whether this file should be treated as writable.
	 * 
	 * <p>
	 * This method controls whether the file can accept modifications and be saved back to disk.
	 * Setting a file as writable enables operations like {@link #setContents(IRuntimeContainer.Modifiable)} and
	 * allows the file to be used as an output destination in save operations.
	 * </p>
	 * 
	 * <p>
	 * Note: This affects the file object's behavior, not the actual file system permissions.
	 * A file that is writable in the object sense may still fail to write if the underlying
	 * file system permissions don't allow it.
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param w
	 *            true to mark this file as writable, false otherwise
	 */
	void setWritable(IScope scope, final boolean w);

	/**
	 * Returns whether the file contents contain the provided value.
	 *
	 * @param scope
	 *            the current execution scope
	 * @param o
	 *            the searched value
	 * @return {@code true} if the buffered contents contain the value
	 */
	@operator (
			value = { "contains", "contains_value" },
			can_be_const = true,
			category = { IOperatorCategory.CONTAINER, IOperatorCategory.FILE },
			concept = { IConcept.CONTAINER, IConcept.FILE })
	@doc (value = "true if the buffered contents of the file contain the right operand, false otherwise")
	@Override
	boolean contains(IScope scope, Object o) throws GamaRuntimeException;

	/**
	 * Sets the contents of this file.
	 * 
	 * <p>
	 * Replaces the current buffered contents with the provided container. This operation
	 * updates the in-memory representation but does not immediately write to disk. To persist
	 * changes, the file must be flushed using appropriate save operations.
	 * </p>
	 * 
	 * <p>
	 * The file must be writable for this operation to succeed. The provided container should
	 * match the expected type for this file (e.g., IList for text files, IMatrix for grid files).
	 * </p>
	 *
	 * @param cont
	 *            the new contents to set
	 * @throws GamaRuntimeException
	 *             if the file is not writable or the contents are invalid
	 */
	void setContents(final C cont) throws GamaRuntimeException;

	/**
	 * Creates a copy of this file.
	 * 
	 * <p>
	 * Returns a new file instance with the same path and properties as this file.
	 * The buffer contents are shared or copied depending on the implementation.
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope
	 * @return a new file instance that is a copy of this one
	 */
	@Override
	IGamaFile copy(IScope scope);

	/**
	 * Returns the first value of the buffered contents.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return the first buffered value, or {@code nil} when appropriate
	 * @throws GamaRuntimeException
	 *             if the buffered contents cannot be accessed
	 */
	@operator (
			value = "first",
			can_be_const = true,
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER, IOperatorCategory.FILE },
			concept = { IConcept.CONTAINER, IConcept.FILE })
	@doc (value = "the first value of the buffered file contents")
	@Override
	Contents firstValue(IScope scope) throws GamaRuntimeException;

	/**
	 * Returns the last value of the buffered contents.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return the last buffered value, or {@code nil} when appropriate
	 * @throws GamaRuntimeException
	 *             if the buffered contents cannot be accessed
	 */
	@operator (
			value = "last",
			can_be_const = true,
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER, IOperatorCategory.FILE },
			concept = { IConcept.CONTAINER, IConcept.FILE })
	@doc (value = "the last value of the buffered file contents")
	@Override
	Contents lastValue(IScope scope) throws GamaRuntimeException;

	/**
	 * Returns the number of buffered elements in this file.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return the buffered length of the file contents
	 */
	@operator (
			value = "length",
			can_be_const = true,
			category = { IOperatorCategory.CONTAINER, IOperatorCategory.FILE },
			concept = { IConcept.CONTAINER, IConcept.FILE })
	@doc (value = "the number of elements in the buffered file contents")
	@Override
	int length(IScope scope);

	/**
	 * Returns whether the buffered contents are empty.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return {@code true} if the buffered contents are empty
	 */
	@operator (
			value = "empty",
			can_be_const = true,
			category = { IOperatorCategory.CONTAINER, IOperatorCategory.FILE },
			concept = { IConcept.CONTAINER, IConcept.FILE })
	@doc (value = "true if the buffered file contents are empty, false otherwise")
	@Override
	boolean isEmpty(IScope scope);

	/**
	 * Returns a reversed view or copy of the buffered contents.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return the reversed buffered contents
	 * @throws GamaRuntimeException
	 *             if the buffered contents cannot be reversed
	 */
	@operator (
			value = "reverse",
			can_be_const = true,
			type = ITypeProvider.TYPE_AT_INDEX + 1,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER, IOperatorCategory.FILE },
			concept = { IConcept.CONTAINER, IConcept.FILE })
	@doc (value = "a reversed copy of the buffered contents of the file")
	@Override
	IRuntimeContainer<?, ?> reverse(IScope scope) throws GamaRuntimeException;

	/**
	 * Returns one buffered value, typically chosen at random.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return one buffered value, or {@code nil} if the contents are empty
	 */
	@operator (
			value = { "one_of", "any" },
			can_be_const = false,
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER, IOperatorCategory.FILE },
			concept = { IConcept.CONTAINER, IConcept.FILE })
	@doc (value = "one of the elements of the buffered file contents")
	@Override
	Contents anyValue(IScope scope);

	/**
	 * Gets the internal buffer containing the file's loaded contents.
	 * 
	 * <p>
	 * Returns the cached container holding the file contents. If the file hasn't been
	 * loaded yet, this may return null. Use {@link #getContents(IScope)} to ensure
	 * the file is loaded before accessing contents.
	 * </p>
	 * 
	 * <p>
	 * Direct buffer access should be used cautiously as it bypasses lazy loading mechanisms.
	 * </p>
	 *
	 * @return the buffer container, or null if not yet loaded
	 */
	C getBuffer();

	/**
	 * Exists.
	 *
	 * @param scope
	 *            the scope
	 * @return the boolean
	 */
	@getter (
			value = IKeyword.EXISTS,
			initializer = true)
	Boolean exists(IScope scope);

	/**
	 * Gets the extension.
	 *
	 * @param scope
	 *            the scope
	 * @return the extension
	 */
	@getter (
			value = IKeyword.EXTENSION,
			initializer = true)
	String getExtension(IScope scope);

	/**
	 * Gets the name.
	 *
	 * @param scope
	 *            the scope
	 * @return the name
	 */
	@getter (
			value = IKeyword.NAME,
			initializer = true)
	String getName(IScope scope);

	/**
	 * Gets the path.
	 *
	 * @param scope
	 *            the scope
	 * @return the path
	 */
	@getter (
			value = IKeyword.PATH,
			initializer = true)
	String getPath(IScope scope);

	/**
	 * Gets the contents.
	 *
	 * @param scope
	 *            the scope
	 * @return the contents
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@getter (IKeyword.CONTENTS)
	C getContents(IScope scope) throws GamaRuntimeException;

	/**
	 * Gets the text.
	 *
	 * @param scope
	 *            the scope
	 * @return the text
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@getter (IKeyword.TEXT)
	String getText(IScope scope) throws GamaRuntimeException;

	/**
	 * Gets the attributes.
	 *
	 * @param scope
	 *            the scope
	 * @return the attributes
	 */
	@getter (IKeyword.ATTRIBUTES)
	/**
	 * Retrieves the list of "attributes" present in files that support this concept (and an empty list for the others).
	 * For instance, in a CSV file, attributes represent the headers of the columns (if any); in a shape file, the
	 * attributes provided to the objects, etc.
	 *
	 * @param scope
	 * @return a list of string or an empty list (never null)
	 */
	IList<String> getAttributes(IScope scope);

	/**
	 * Checks if is folder.
	 *
	 * @param scope
	 *            the scope
	 * @return the boolean
	 */
	@getter (
			value = IKeyword.ISFOLDER,
			initializer = true)
	Boolean isFolder(IScope scope);

	/**
	 * Checks if is readable.
	 *
	 * @param scope
	 *            the scope
	 * @return the boolean
	 */
	@getter (
			value = IKeyword.READABLE,
			initializer = true)
	Boolean isReadable(IScope scope);

	/**
	 * Checks if is writable.
	 *
	 * @param scope
	 *            the scope
	 * @return the boolean
	 */
	@getter (
			value = IKeyword.WRITABLE,
			initializer = true)
	Boolean isWritable(IScope scope);

	/**
	 * Save.
	 *
	 * @param scope
	 *            the scope
	 * @param parameters
	 *            the parameters
	 */
	void save(IScope scope, Facets parameters);

	/**
	 * Gets the original path.
	 *
	 * @return the original path
	 */
	String getOriginalPath();

	/**
	 * Contains key.
	 *
	 * @param scope
	 *            the scope
	 * @param o
	 *            the o
	 * @return true, if successful
	 */
	@operator (
			value = { "contains_key" },
			can_be_const = true,
			category = { IOperatorCategory.CONTAINER, IOperatorCategory.FILE },
			concept = { IConcept.CONTAINER, IConcept.FILE })
	@doc (value = "true if the buffered contents of the file contain the right operand as a key or index")
	@Override
	default boolean containsKey(final IScope scope, final Object o) {
		final C contents = getContents(scope);
		return contents != null && contents.containsKey(scope, o);
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	@Override
	default String getId() { return this.getOriginalPath(); }

	/**
	 * Gets the URI relative to workspace.
	 *
	 * @return the URI relative to workspace
	 */
	URI getURIRelativeToWorkspace();

	/**
	 * Checks for geo data available.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if successful
	 * @date 15 juil. 2023
	 */
	default boolean hasGeoDataAvailable(final IScope scope) {
		return false;
	}

	/**
	 * Ensure contents is compatible.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param contents
	 *            the contents
	 * @return the i modifiable container
	 * @date 4 nov. 2023
	 */
	default IRuntimeContainer.Modifiable ensureContentsIsCompatible(final IRuntimeContainer.Modifiable contents) {
		return contents;
	}

	/**
	 * Invalidate contents.
	 */
	void invalidateContents();

}