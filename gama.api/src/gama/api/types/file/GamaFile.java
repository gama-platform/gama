/*******************************************************************************************************
 *
 * GamaFile.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.file;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.emf.common.util.URI;

import gama.annotations.file;
import gama.annotations.constants.IKeyword;
import gama.api.GAMA;
import gama.api.exceptions.FlushBufferException;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IContainerType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.IMap;
import gama.api.types.matrix.IMatrix;
import gama.api.types.misc.IContainer;
import gama.api.ui.IStatusMessage;
import gama.api.utils.files.FileUtils;
import gama.api.utils.geometry.IEnvelope;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;
import gama.dependencies.webb.Webb;
import gama.dependencies.webb.WebbException;
import one.util.streamex.StreamEx;

/**
 * Abstract base class for all file types in the GAMA modeling platform.
 * 
 * <p>
 * {@code GamaFile} provides the foundational implementation for file handling in GAMA, including:
 * </p>
 * <ul>
 * <li>Path resolution (relative, absolute, and URL-based)</li>
 * <li>Lazy loading and buffering of file contents</li>
 * <li>Read/write operations with proper error handling</li>
 * <li>Integration with GAML's type system and container operations</li>
 * </ul>
 * 
 * <h2>Type Parameters</h2>
 * <ul>
 * <li>{@code Container} - The container type used to hold file contents. Must be both
 * {@link gama.api.types.misc.IContainer.Addressable} (support indexed/keyed access) and
 * {@link gama.api.types.misc.IContainer.Modifiable} (support modifications)</li>
 * <li>{@code Contents} - The type of individual elements stored in the container</li>
 * </ul>
 * 
 * <h2>Path Resolution</h2>
 * <p>
 * GamaFile handles various path formats:
 * </p>
 * <ul>
 * <li><b>Relative paths:</b> Resolved relative to the model file's location</li>
 * <li><b>Absolute paths:</b> Used directly from the file system</li>
 * <li><b>HTTP/HTTPS URLs:</b> Downloaded to temporary local storage</li>
 * <li><b>Platform-independent:</b> Path separators are normalized across operating systems</li>
 * </ul>
 * 
 * <h2>Buffering Strategy</h2>
 * <p>
 * File contents are loaded lazily using a buffering mechanism:
 * </p>
 * <ol>
 * <li>When a file is created, only the path is stored</li>
 * <li>On first access to contents, {@link #fillBuffer(IScope)} is called</li>
 * <li>The loaded contents are cached in the buffer</li>
 * <li>Subsequent accesses return the cached buffer</li>
 * <li>Modified contents can be flushed back using {@link #flushBuffer(IScope, Facets)}</li>
 * </ol>
 * 
 * <h2>Implementing a New File Type</h2>
 * <p>
 * To create a new file type, subclass {@code GamaFile} and:
 * </p>
 * <ol>
 * <li>Specify appropriate type parameters for {@code Container} and {@code Contents}</li>
 * <li>Implement {@link #fillBuffer(IScope)} to load file contents into the buffer</li>
 * <li>Optionally implement {@link #flushBuffer(IScope, Facets)} to support file writing</li>
 * <li>Implement {@link #getAttributes(IScope)} to return file-specific metadata</li>
 * <li>Annotate the class with {@link gama.annotations.file} for GAML registration</li>
 * </ol>
 * 
 * <h2>Example Implementation</h2>
 * <pre>{@code
 * {@literal @}file(
 *     name = "my_format",
 *     extensions = {"myf", "myformat"},
 *     buffer_content = IType.LIST,
 *     buffer_index = IType.INT,
 *     buffer_type = IType.STRING
 * )
 * public class MyFormatFile extends GamaFile<IList<String>, String> {
 *     
 *     public MyFormatFile(IScope scope, String path) {
 *         super(scope, path);
 *     }
 *     
 *     {@literal @}Override
 *     protected void fillBuffer(IScope scope) throws GamaRuntimeException {
 *         IList<String> data = GamaListFactory.create(Types.STRING);
 *         // Read file and populate data
 *         setBuffer(data);
 *     }
 *     
 *     {@literal @}Override
 *     protected void flushBuffer(IScope scope, Facets facets) 
 *             throws GamaRuntimeException {
 *         // Write buffer contents to file
 *     }
 *     
 *     {@literal @}Override
 *     public IList<String> getAttributes(IScope scope) {
 *         // Return file-specific attributes
 *         return GamaListFactory.create(Types.STRING);
 *     }
 * }
 * }</pre>
 * 
 * <h2>URL Support</h2>
 * <p>
 * Files can be loaded from HTTP/HTTPS URLs. When a URL is detected:
 * </p>
 * <ol>
 * <li>The file is downloaded to a temporary location</li>
 * <li>The local path is updated to point to the temporary file</li>
 * <li>Subsequent operations work on the local copy</li>
 * <li>For output operations, the local file is retained for upload</li>
 * </ol>
 * 
 * <h2>Error Handling</h2>
 * <p>
 * File operations may throw {@link GamaRuntimeException} for:
 * </p>
 * <ul>
 * <li>File not found or inaccessible</li>
 * <li>Invalid file format or corrupted data</li>
 * <li>I/O errors during read/write operations</li>
 * <li>Network errors when fetching from URLs</li>
 * <li>Invalid path specifications</li>
 * </ul>
 * 
 * @param <Container>
 *            the container type for buffering file contents
 * @param <Contents>
 *            the element type contained within the container
 * 
 * @see IGamaFile
 * @see gama.api.types.file.GenericFile
 * @see gama.api.types.misc.IContainer
 * @see gama.annotations.file
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 */

@SuppressWarnings ({ "rawtypes", "unchecked" })
public abstract class GamaFile<Container extends IContainer.Addressable & IContainer.Modifiable, Contents>
		implements IGamaFile<Container, Contents> {

	/** The file. */
	private File file;

	/** The buffer type. */
	private IType<?> bufferType;

	/** The local path. */
	protected final String localPath;

	/** The original path. */
	protected final String originalPath;

	@Override
	public IEnvelope computeEnvelope(final IScope scope) {
		return null;
	}

	@Override
	public final IContainerType getGamlType() {
		return GamaType.from(Types.FILE, getBufferType().getKeyType(), getBufferType().getContentType());
	}

	/** The url. */
	protected final URL url;

	/** The writable. */
	protected boolean writable = false;

	/** The buffer. */
	private Container buffer;

	/**
	 * Constructs a new GamaFile for reading.
	 * 
	 * <p>
	 * Creates a file instance that will be used primarily for reading operations.
	 * The path is resolved relative to the model location if not absolute.
	 * If the path starts with "http", the file will be downloaded from the URL.
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope (used for path resolution and error reporting)
	 * @param pn
	 *            the path or URL to the file (must not be null)
	 * @throws GamaRuntimeException
	 *             if the path is null or if path resolution fails
	 */
	public GamaFile(final IScope scope, final String pn) throws GamaRuntimeException {
		this(scope, pn, true);
	}

	/**
	 * Constructs a new GamaFile with explicit read/write mode.
	 * 
	 * <p>
	 * This constructor allows specifying whether the file is intended for reading or writing.
	 * For output files (forReading = false), additional temporary paths may be created.
	 * URL-based files are handled differently depending on the mode:
	 * </p>
	 * <ul>
	 * <li><b>Reading mode:</b> File is downloaded to a local temporary location</li>
	 * <li><b>Writing mode:</b> A temporary local file is created for later upload</li>
	 * </ul>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param pn
	 *            the path or URL to the file
	 * @param forReading
	 *            true if the file is for reading, false if for writing
	 * @throws GamaRuntimeException
	 *             if the path is null, invalid, or cannot be resolved
	 */
	protected GamaFile(final IScope scope, final String pn, boolean forReading) throws GamaRuntimeException {
		// See #3684 -- we temporarily consider files as output files if we are invoked by 'save'
		if (forReading) { forReading = scope != null && scope.getData(IGamaFile.KEY_TEMPORARY_OUTPUT) == null; }
		originalPath = pn;
		String tempPath = originalPath;
		if (originalPath == null)
			throw GamaRuntimeException.error("Attempt to " + (forReading ? "read" : "write") + " a null file", scope);
		if (originalPath.startsWith("http")) {
			url = buildURL(scope, originalPath);
		} else {
			url = null;
		}
		if (url != null) {
			if (forReading) {
				// fetchFromURL already returns an absolute filesystem path to the cache file.
				// Do NOT pass it through constructAbsoluteFilePath: that method tries to create
				// Eclipse workspace linked resources (folder.create / file.createLink) for any
				// absolute path that exists on disk. Those workspace writes happen from the
				// experiment thread, corrupt the workspace .metadata on crash, and cause
				// "Workspace is closed" on the next GAMA launch.
				final String fetched = fetchFromURL(scope);
				tempPath = (fetched != null) ? fetched : "";
			} else {
				tempPath = FileUtils.constructAbsoluteTempFilePath(scope, url);
			}
		} else {
			tempPath = FileUtils.constructAbsoluteFilePath(scope, originalPath, forReading);
		}

		localPath = tempPath;
		checkValidity(scope);
	}

	/**
	 * Gets the buffer type.
	 *
	 * @return the buffer type
	 */
	public IType<?> getBufferType() {
		if (bufferType == null) {
			file annot = getClass().getAnnotation(file.class);
			if (annot == null) {
				bufferType = Types.NO_TYPE;
			} else {
				bufferType = GamaType.from(Types.get(annot.buffer_type()), Types.get(annot.buffer_index()),
						Types.get(annot.buffer_content()));
			}
		}
		return bufferType;
	}

	/**
	 * Checks if is remote.
	 *
	 * @return true, if is remote
	 */
	public boolean isRemote() { return url != null; }

	/**
	 * Instantiates a new gama file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param container
	 *            the container
	 */
	public GamaFile(final IScope scope, final String pathName, final Container container) {
		this(scope, pathName, false);
		setWritable(scope, true);
		setContents(container);
	}

	@Override
	public String getOriginalPath() { return originalPath; }

	/**
	 * Whether or not passing an URL will automatically make GAMA cache its contents in a temp file. Should be redefined
	 * by GamaFiles that can retrieve from URL directly (like, e.g., GeoTools'datastore-backed files). If false, the url
	 * will be initialized, but the path will be set to the empty string and no attempt will be made to download data
	 * later. In that case, it is the responsibility of subclasses to use the url -- and NOT the path -- to download the
	 * contents of the file later (for example in fillBuffer()). The default is true.
	 *
	 * @return true or false depending on whether the contents should be cached in a temp file
	 */
	protected boolean automaticallyFetchFromURL() {
		return true;
	}

	/**
	 * Fetch from URL.
	 *
	 * @param scope
	 *            the scope
	 * @return the string
	 */
	protected String fetchFromURL(final IScope scope) {
		if (!automaticallyFetchFromURL()) return null;
		return FileUtils.fetchToTempFile(scope, url);
	}

	/**
	 * Send to URL.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected void sendToURL(final IScope scope) throws GamaRuntimeException {
		final String urlPath = url.toExternalForm();
		final String status = "Uploading file to " + urlPath;
		scope.getGui().getStatus().beginTask(status, IStatusMessage.DOWNLOAD_ICON);
		final Webb web = Webb.create();
		try {
			web.post(urlPath).ensureSuccess().connectTimeout(20000).retry(1, false)
					.header(Webb.HDR_CONTENT_TYPE, getHttpContentType()).body(getFile(scope)).asVoid();
		} catch (final WebbException e) {
			throw GamaRuntimeException.create(e, scope);
		} finally {
			scope.getGui().getStatus().endTask(status, IStatusMessage.DOWNLOAD_ICON);
		}
	}

	/**
	 * The content type to use for uploading the contents of the file. see
	 * http://www.iana.org/assignments/media-types/media-types.xhtml
	 *
	 * @return
	 */
	protected String getHttpContentType() { return "text/plain"; }

	/**
	 * Builds the URL.
	 *
	 * @param scope
	 *            the scope
	 * @param urlPath
	 *            the url path
	 * @return the url
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected URL buildURL(final IScope scope, final String urlPath) throws GamaRuntimeException {
		try {
			return new URL(urlPath);
		} catch (final MalformedURLException e1) {
			throw GamaRuntimeException.error("Malformed URL " + urlPath, scope);
		}
	}

	/**
	 * Check validity.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected void checkValidity(final IScope scope) throws GamaRuntimeException {
		if (getFile(scope).exists() && getFile(scope).isDirectory()) throw GamaRuntimeException
				.error(getFile(scope).getAbsolutePath() + " is a folder. Files can not overwrite folders", scope);
	}

	@Override
	public void setWritable(final IScope scope, final boolean w) {
		writable = w;
	}

	/**
	 * Loads the file contents into the internal buffer.
	 * 
	 * <p>
	 * This method is called lazily when file contents are first accessed. Subclasses must
	 * implement this method to read the file from disk (or other source) and populate the
	 * buffer with appropriate data. The loaded contents should be set using {@link #setBuffer(IContainer)}.
	 * </p>
	 * 
	 * <p>
	 * Implementation guidelines:
	 * </p>
	 * <ul>
	 * <li>Check if buffer is already filled before loading to avoid redundant reads</li>
	 * <li>Use the File object from {@link #getFile(IScope)} for reading</li>
	 * <li>Handle file format parsing and validation</li>
	 * <li>Set the buffer using {@link #setBuffer(IContainer)} before returning</li>
	 * <li>Throw {@link GamaRuntimeException} for any read or parse errors</li>
	 * </ul>
	 * 
	 * <p>
	 * Example implementation:
	 * </p>
	 * <pre>{@code
	 * {@literal @}Override
	 * protected void fillBuffer(IScope scope) throws GamaRuntimeException {
	 *     if (getBuffer() != null) return; // Already loaded
	 *     
	 *     IList<String> lines = GamaListFactory.create(Types.STRING);
	 *     try (BufferedReader reader = new BufferedReader(new FileReader(getFile(scope)))) {
	 *         String line;
	 *         while ((line = reader.readLine()) != null) {
	 *             lines.add(line);
	 *         }
	 *     } catch (IOException e) {
	 *         throw GamaRuntimeException.create(e, scope);
	 *     }
	 *     setBuffer(lines);
	 * }
	 * }</pre>
	 *
	 * @param scope
	 *            the current execution scope (for error reporting and context)
	 * @throws GamaRuntimeException
	 *             if the file cannot be read, parsed, or if any I/O error occurs
	 */
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		throw GamaRuntimeException.error("Loading is not yet impletemented for files of type "
				+ this.getExtension(scope) + ". Please post a request for enhancement to implement "
				+ getClass().getSimpleName() + ".fillBuffer(IScope, Facets)", scope);
	}

	/**
	 * Writes the buffered contents back to the file.
	 * 
	 * <p>
	 * This method is called when a file needs to be saved (e.g., via GAML's {@code save} statement).
	 * Subclasses should implement this method to write the buffer contents to disk in the appropriate
	 * format. The buffer contents can be accessed via {@link #getBuffer()}.
	 * </p>
	 * 
	 * <p>
	 * Implementation guidelines:
	 * </p>
	 * <ul>
	 * <li>Get the buffer contents using {@link #getBuffer()}</li>
	 * <li>Use the File object from {@link #getFile(IScope)} for writing</li>
	 * <li>Format the data according to the file type specifications</li>
	 * <li>Use facets to customize the save operation (e.g., header, delimiter for CSV)</li>
	 * <li>Ensure proper resource cleanup (close streams in finally blocks or use try-with-resources)</li>
	 * <li>Throw {@link FlushBufferException} or {@link GamaRuntimeException} for write errors</li>
	 * </ul>
	 * 
	 * <p>
	 * Example implementation:
	 * </p>
	 * <pre>{@code
	 * {@literal @}Override
	 * protected void flushBuffer(IScope scope, Facets facets) throws GamaRuntimeException {
	 *     try (BufferedWriter writer = new BufferedWriter(new FileWriter(getFile(scope)))) {
	 *         for (String line : (IList<String>) getBuffer()) {
	 *             writer.write(line);
	 *             writer.newLine();
	 *         }
	 *     } catch (IOException e) {
	 *         throw new FlushBufferException(scope, e, false);
	 *     }
	 * }
	 * }</pre>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param facets
	 *            additional parameters for customizing the save operation (can be null)
	 * @throws GamaRuntimeException
	 *             if the file cannot be written or if any I/O error occurs
	 */
	protected void flushBuffer(final IScope scope, final Facets facets) throws GamaRuntimeException {
		throw new FlushBufferException(scope,
				"Saving is not yet impletemented for files of type " + this.getExtension(scope)
						+ ". Please post a request for enhancement to implement " + getClass().getSimpleName()
						+ ".flushBuffer(IScope, Facets)",
				false);
	}

	@Override
	public final void setContents(final Container cont) throws GamaRuntimeException {
		if (writable) { setBuffer(cont); }
	}

	/**
	 * String value.
	 *
	 * @param scope
	 *            the scope
	 * @return the string
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected String _stringValue(final IScope scope) throws GamaRuntimeException {
		return getPath(scope);
	}

	// 09/01/14:Trying to keep the interface simple.
	// Three methods for add and put operations:
	// The simple method, that simply contains the object to add
	@Override
	public void addValue(final IScope scope, final Object value) {
		fillBuffer(scope);
		getBuffer().addValue(scope, value);
	}

	// The same but with an index (this index represents the old notion of
	// parameter where it is needed.
	@Override
	public void addValueAtIndex(final IScope scope, final Object index, final Object value) {
		fillBuffer(scope);
		getBuffer().addValueAtIndex(scope, index, value);
	}

	// Put, that takes a mandatory index (also replaces the parameter)
	@Override
	public void setValueAtIndex(final IScope scope, final Object index, final Object value) {
		fillBuffer(scope);
		getBuffer().setValueAtIndex(scope, index, value);
	}

	// Then, methods for "all" operations
	// Adds the values if possible, without replacing existing ones
	@Override
	public void addValues(final IScope scope, final Object index, final IContainer values) {
		// Addition of the index (see #2985)
		fillBuffer(scope);
		getBuffer().addValues(scope, index, values);
	}

	// Adds this value to all slots (if this operation is available), otherwise
	// replaces the values with this one
	@Override
	public void setAllValues(final IScope scope, final Object value) {
		fillBuffer(scope);
		getBuffer().setAllValues(scope, value);
	}

	@Override
	public void removeValue(final IScope scope, final Object value) {
		fillBuffer(scope);
		getBuffer().removeValue(scope, value);
	}

	@Override
	public void removeIndex(final IScope scope, final Object index) {
		fillBuffer(scope);
		getBuffer().removeIndex(scope, index);
	}

	@Override
	public void removeValues(final IScope scope, final IContainer values) {
		fillBuffer(scope);
		getBuffer().removeValues(scope, values);
	}

	@Override
	public void removeAllOccurrencesOfValue(final IScope scope, final Object value) {
		fillBuffer(scope);
		getBuffer().removeAllOccurrencesOfValue(scope, value);
	}

	@Override
	public void removeIndexes(final IScope scope, final IContainer indexes) {
		fillBuffer(scope);
		getBuffer().removeIndexes(scope, indexes);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.interfaces.IGamaContainer#checkBounds(java.lang.Object, boolean)
	 */
	// @Override
	// public boolean checkBounds(final IScope scope, final Object index, final boolean forAdding) {
	// getContents(scope);
	// return getBuffer().checkBounds(scope, index, forAdding);
	//
	// }

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.interfaces.IGamaContainer#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(final IScope scope, final Object o) throws GamaRuntimeException {
		getContents(scope);
		return getBuffer().contains(scope, o);

	}

	@Override
	public IGamaFile copy(final IScope scope) {
		// files are supposed to be immutable
		return this;
	}

	@Override
	public Boolean exists(final IScope scope) {
		return getFile(scope).exists();
	}

	/*
	 * @see gama.interfaces.IGamaContainer#first()
	 */
	@Override
	public Contents firstValue(final IScope scope) throws GamaRuntimeException {
		getContents(scope);
		return (Contents) getBuffer().firstValue(scope);
	}

	/*
	 * @see gama.interfaces.IGamaContainer#get(java.lang.Object)
	 */
	@Override
	public Contents get(final IScope scope, final Object index) throws GamaRuntimeException {
		getContents(scope);
		return (Contents) getBuffer().get(scope, index);
	}

	@Override
	public Contents getFromIndicesList(final IScope scope, final IList indices) throws GamaRuntimeException {
		getContents(scope);
		return (Contents) getBuffer().getFromIndicesList(scope, indices);
	}

	@Override
	// @getter( IKeyword.EXTENSION)
	public String getExtension(final IScope scope) {
		// In order to avoid too many calls to the file system, we can safely consider that the extension of files
		// remain the same between the urls, local paths and links to external paths
		final String path = getOriginalPath().toLowerCase();
		// final String path = getPath(scope).toLowerCase();
		final int mid = path.lastIndexOf('.');
		if (mid == -1) return "";
		return path.substring(mid + 1);
	}

	@Override
	public String getName(final IScope scope) {
		return getFile(scope).getName();
	}

	@Override
	public String getPath(final IScope scope) {
		return localPath;
	}

	@Override
	public String getText(final IScope scope) throws GamaRuntimeException {
		try {
			return java.nio.file.Files.readString(java.nio.file.Paths.get(getPath(scope)));
		} catch (java.io.IOException e) {
			throw GamaRuntimeException.create(e, scope);
		}
	}

	@Override
	public Container getContents(final IScope scope) throws GamaRuntimeException {
		if (buffer == null && !exists(scope))
			throw GamaRuntimeException.error("File " + getFile(scope).getAbsolutePath() + " does not exist", scope);
		fillBuffer(scope);
		return getBuffer();
	}

	@Override
	public boolean isEmpty(final IScope scope) {
		getContents(scope);
		return getBuffer().isEmpty(scope);
	}

	@Override
	public Boolean isFolder(final IScope scope) {
		return getFile(scope).isDirectory();
	}

	@Override
	public Boolean isReadable(final IScope scope) {
		return getFile(scope).canRead();
	}

	@Override
	public Boolean isWritable(final IScope scope) {
		return getFile(scope).canWrite();
	}

	@Override
	public java.lang.Iterable<? extends Contents> iterable(final IScope scope) {
		return getContents(scope).iterable(scope);
	}

	@Override
	public Contents lastValue(final IScope scope) throws GamaRuntimeException {
		getContents(scope);
		return (Contents) getBuffer().lastValue(scope);
	}

	@Override
	public int length(final IScope scope) {
		getContents(scope);
		return getBuffer().length(scope);
	}

	@Override
	public IList<Contents> listValue(final IScope scope, final IType contentsType, final boolean copy)
			throws GamaRuntimeException {
		getContents(scope);
		return getBuffer().listValue(scope, contentsType, copy);
	}

	@Override
	public StreamEx<Contents> stream(final IScope scope) {
		getContents(scope);
		return getBuffer().stream(scope);
	}

	@Override
	public IMap<?, ?> mapValue(final IScope scope, final IType keyType, final IType contentsType, final boolean copy)
			throws GamaRuntimeException {
		getContents(scope);
		return getBuffer().mapValue(scope, keyType, contentsType, copy);
	}

	@Override
	public IMatrix<?> matrixValue(final IScope scope, final IType contentsType, final boolean copy)
			throws GamaRuntimeException {
		return matrixValue(scope, contentsType, null, copy);
	}

	@Override
	public IMatrix<?> matrixValue(final IScope scope, final IType contentsType, final IPoint preferredSize,
			final boolean copy) throws GamaRuntimeException {
		return _matrixValue(scope, contentsType, preferredSize, copy);
	}

	/**
	 * Matrix value.
	 *
	 * @param scope
	 *            the scope
	 * @param contentsType
	 *            the contents type
	 * @param preferredSize
	 *            the preferred size
	 * @param copy
	 *            the copy
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected IMatrix _matrixValue(final IScope scope, final IType contentsType, final IPoint preferredSize,
			final boolean copy) throws GamaRuntimeException {
		getContents(scope);
		return getBuffer().matrixValue(scope, contentsType, preferredSize, copy);
	}

	@Override
	public IContainer<?, ?> reverse(final IScope scope) throws GamaRuntimeException {
		getContents(scope);
		return getBuffer().reverse(scope);
		// No side effect
	}

	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return _stringValue(scope);
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return "file('" + getPath(GAMA.getRuntimeScope()) + "')";
	}

	@Override
	public Contents anyValue(final IScope scope) {
		getContents(scope);
		return (Contents) getBuffer().anyValue(scope);
	}

	/**
	 * Gets the file.
	 *
	 * @param scope
	 *            the scope
	 * @return the file
	 */
	public File getFile(final IScope scope) {
		if (file == null) { file = new File(getPath(scope)); }
		return file;
	}

	@Override
	public Container getBuffer() { return buffer; }

	/**
	 * Sets the buffer.
	 *
	 * @param buffer
	 *            the new buffer
	 */
	protected void setBuffer(final Container buffer) { this.buffer = buffer; }

	/**
	 * Invalidate contents.
	 */
	@Override
	public void invalidateContents() {
		buffer = null;
	}

	@Override
	public IList<String> getAttributes(final IScope scope) {
		// TODO what to return ?
		return GamaListFactory.getEmptyList();
	}

	/**
	 * This method is being called from the save statement (see SaveStatement.java). The scope and all the facets
	 * declared in the save statement are passed as parameters, which allows the programmer to retrieve them (for
	 * instance, to get the crs for shape files, or the attributes to save from a list of agents, etc.). This method
	 * cannot be redefined. Instead, programmers should redefine flushBuffer(), which takes the same arguments
	 */

	@Override
	public void save(final IScope scope, final Facets saveFacets) {

		// TODO AD
		// Keep in mind that facets might contain a method for uploading (like method: #post) ?
		// Keep in mind that facets might contain a content-type
		// Keep in mind possible additional resources (shp additions)

		final IExpression exp = saveFacets.getExpr(IKeyword.REWRITE);
		final boolean overwrite = exp == null || Cast.asBool(scope, exp.value(scope));
		if (overwrite && getFile(scope).exists()) { getFile(scope).delete(); }
		if (!writable) throw GamaRuntimeException.error("File " + getName(scope) + " is not writable", scope);
		// This will save to the local file
		flushBuffer(scope, saveFacets);
		if (isRemote()) { sendToURL(scope); }

	}

	/**
	 * Gets the URI relative to workspace.
	 *
	 * @return the URI relative to workspace
	 */
	@Override
	public URI getURIRelativeToWorkspace() { return FileUtils.getURI(localPath, null); }

	/**
	 * A basic implementation that shoud be reused to enrich the necessary attributes along the hierarchy using the
	 * pattern: return super.serializeToJson().add(attr1, value1).add...
	 */
	@Override
	public IJsonValue serializeToJson(final IJson json) {
		return json.typedObject(getGamlType(), "path", originalPath);
	}

	@Override
	public IType<?> computeRuntimeType(final IScope scope) {
		Container contents = getContents(scope);
		IType<?> type = GamaType.actualTypeOf(scope, contents);
		return Types.FILE.of(type.getKeyType(), type.getContentType());
	}
}
