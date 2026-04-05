/*******************************************************************************************************
 *
 * package-info.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

/**
 * Provides file handling and I/O capabilities for the GAMA modeling and simulation platform.
 * 
 * <p>
 * This package contains classes and interfaces for working with files and folders in GAMA models.
 * It provides a unified abstraction layer over file system operations, supporting various file types
 * including text files, CSV files, shape files, grid files, and more. The architecture enables both
 * local file system access and remote file retrieval via HTTP/HTTPS.
 * </p>
 * 
 * <h2>Core Architecture</h2>
 * 
 * <h3>File Abstraction</h3>
 * <ul>
 * <li>{@link gama.api.types.file.IGamaFile} - The root interface for all file types in GAMA,
 * extending {@link gama.api.types.misc.IContainer} to provide file contents as addressable,
 * modifiable containers</li>
 * <li>{@link gama.api.types.file.GamaFile} - Abstract base class implementing common file operations
 * including path resolution, URL handling, buffering, and persistence</li>
 * </ul>
 * 
 * <h3>Concrete Implementations</h3>
 * <ul>
 * <li>{@link gama.api.types.file.GenericFile} - Represents generic text files with line-based access</li>
 * <li>{@link gama.api.types.file.GamaFolderFile} - Represents directories/folders in the file system</li>
 * </ul>
 * 
 * <h2>Key Features</h2>
 * 
 * <h3>File Types</h3>
 * <p>
 * GAMA supports numerous specialized file types through extensions of {@link gama.api.types.file.GamaFile}.
 * Each file type provides tailored functionality for its specific format:
 * </p>
 * <ul>
 * <li><b>Text files:</b> Line-based reading and writing</li>
 * <li><b>CSV files:</b> Tabular data with header support</li>
 * <li><b>Shape files:</b> Geospatial vector data with geometry and attributes</li>
 * <li><b>Grid files:</b> Raster data for elevation models and land use</li>
 * <li><b>Image files:</b> Pixel-based image data</li>
 * <li><b>Graph files:</b> Network structures</li>
 * <li><b>3D files:</b> Three-dimensional mesh data</li>
 * </ul>
 * 
 * <h3>Path Resolution</h3>
 * <p>
 * File paths in GAMA are resolved intelligently:
 * </p>
 * <ul>
 * <li>Relative paths are resolved relative to the model file location</li>
 * <li>Absolute paths are used directly</li>
 * <li>URLs (http/https) trigger automatic download to temporary storage</li>
 * <li>Platform-specific path separators are handled automatically</li>
 * </ul>
 * 
 * <h3>Lazy Loading</h3>
 * <p>
 * File contents are loaded lazily through a buffering mechanism. The file is not read from disk
 * until its contents are first accessed, improving performance when files are created but not
 * immediately used.
 * </p>
 * 
 * <h3>Container Integration</h3>
 * <p>
 * All files implement {@link gama.api.types.misc.IContainer}, allowing their contents to be
 * treated as GAML containers (lists, matrices, maps) depending on the file type. This enables
 * natural iteration, filtering, and manipulation of file data within models.
 * </p>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Reading a Generic Text File</h3>
 * <pre>{@code
 * // In GAML
 * file my_file <- file("data/input.txt");
 * 
 * // Access contents as a list of strings
 * loop line over: my_file {
 *     write line;
 * }
 * 
 * // In Java
 * IGamaFile<IList<String>, String> file = 
 *     new GenericFile(scope, "data/input.txt");
 * IList<String> lines = file.getContents(scope);
 * }</pre>
 * 
 * <h3>Working with Folders</h3>
 * <pre>{@code
 * // In GAML
 * folder my_folder <- folder("data");
 * 
 * // List files in folder
 * list<string> file_names <- my_folder.contents;
 * 
 * // In Java
 * GamaFolderFile folder = new GamaFolderFile(scope, "data");
 * IList<String> fileNames = folder.getContents(scope);
 * }</pre>
 * 
 * <h3>Reading from URLs</h3>
 * <pre>{@code
 * // In GAML
 * file remote_file <- file("https://example.com/data.csv");
 * // File is automatically downloaded and cached
 * 
 * // In Java
 * IGamaFile file = new GenericFile(scope, "https://example.com/data.txt");
 * // Content fetched from URL and buffered locally
 * }</pre>
 * 
 * <h3>File Attributes</h3>
 * <pre>{@code
 * // In GAML - accessing file metadata
 * write "File name: " + my_file.name;
 * write "File path: " + my_file.path;
 * write "Extension: " + my_file.extension;
 * write "Exists: " + my_file.exists;
 * write "Readable: " + my_file.readable;
 * write "Writable: " + my_file.writable;
 * }</pre>
 * 
 * <h2>Writing Files</h2>
 * <p>
 * File writing is typically done using GAML's {@code save} statement, which delegates to the
 * file type's persistence mechanism:
 * </p>
 * <pre>{@code
 * // In GAML
 * save my_data to: "output/results.csv" type: csv;
 * save my_agents to: "output/locations.shp" type: shp;
 * }</pre>
 * 
 * <h2>Extension Points</h2>
 * 
 * <h3>Creating Custom File Types</h3>
 * <p>
 * New file types can be added by:
 * </p>
 * <ol>
 * <li>Extending {@link gama.api.types.file.GamaFile} with appropriate type parameters</li>
 * <li>Implementing {@code fillBuffer(IScope)} to load file contents</li>
 * <li>Optionally implementing {@code flushBuffer(IScope, Facets)} to support writing</li>
 * <li>Annotating the class with {@link gama.annotations.file} to register with GAML</li>
 * </ol>
 * 
 * <h2>Thread Safety</h2>
 * <p>
 * File operations in GAMA are generally not thread-safe. File reading and writing should be
 * performed within the context of a single simulation thread. The buffering mechanism maintains
 * one buffer per file instance.
 * </p>
 * 
 * <h2>Error Handling</h2>
 * <p>
 * File operations may throw {@link gama.api.exceptions.GamaRuntimeException} for various error
 * conditions:
 * </p>
 * <ul>
 * <li>File not found or inaccessible</li>
 * <li>Invalid file format or corrupted data</li>
 * <li>I/O errors during reading or writing</li>
 * <li>Network errors when fetching from URLs</li>
 * </ul>
 * 
 * <h2>Performance Considerations</h2>
 * <ul>
 * <li><b>Buffering:</b> Contents are cached after first read; multiple accesses don't re-read</li>
 * <li><b>Lazy loading:</b> Files aren't read until contents are accessed</li>
 * <li><b>Large files:</b> Some file types support streaming for memory efficiency</li>
 * <li><b>Remote files:</b> Downloaded once and cached locally for the session</li>
 * </ul>
 * 
 * <h2>Integration with GAML</h2>
 * <p>
 * File types are registered and accessible in GAML through:
 * </p>
 * <ul>
 * <li>The generic {@code file} type for automatic type detection</li>
 * <li>Specific file type keywords: {@code csv_file}, {@code shape_file}, {@code grid_file}, etc.</li>
 * <li>The {@code folder} type for directory operations</li>
 * <li>File attributes accessible via dot notation</li>
 * <li>Container operations (iteration, filtering, etc.) on file contents</li>
 * </ul>
 * 
 * @see gama.api.types.misc.IContainer
 * @see gama.api.gaml.types.GamaFileType
 * @see gama.annotations.file
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 */
package gama.api.types.file;
