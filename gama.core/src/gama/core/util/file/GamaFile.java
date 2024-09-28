/*******************************************************************************************************
 *
 * GamaFile.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.emf.common.util.URI;

import gama.core.common.interfaces.IKeyword;
import gama.core.common.util.FileUtils;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IAddressableContainer;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.core.util.IModifiableContainer;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;
import gama.core.util.matrix.IMatrix;
import gama.dependencies.webb.Webb;
import gama.dependencies.webb.WebbException;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.statements.Facets;
import gama.gaml.types.IType;
import one.util.streamex.StreamEx;

/**
 * Written by drogoul Modified on 7 août 2010
 *
 * @todo Description
 *
 */
@SuppressWarnings ({ "rawtypes", "unchecked" })
public abstract class GamaFile<Container extends IAddressableContainer & IModifiableContainer, Contents>
		implements IGamaFile<Container, Contents> {

	/** The file. */
	private File file;

	/** The local path. */
	protected final String localPath;

	/** The original path. */
	protected final String originalPath;

	/** The url. */
	protected final URL url;

	/** The writable. */
	protected boolean writable = false;

	/** The buffer. */
	private Container buffer;

	/**
	 * Instantiates a new gama file.
	 *
	 * @param scope
	 *            the scope
	 * @param pn
	 *            the pn
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public GamaFile(final IScope scope, final String pn) throws GamaRuntimeException {
		this(scope, pn, true);
	}

	/**
	 * Instantiates a new gama file.
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
				tempPath = FileUtils.constructAbsoluteFilePath(scope, fetchFromURL(scope), forReading);
				if (tempPath == null) {
					// We do not attempt to create the file. It will probably be taken in charge later directly from the
					// URL or there has been an error trying to download it.
					tempPath = "";
				}
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
		scope.getGui().getStatus().beginTask(scope, status);
		final Webb web = Webb.create();
		try {
			web.post(urlPath).ensureSuccess().connectTimeout(20000).retry(1, false)
					.header(Webb.HDR_CONTENT_TYPE, getHttpContentType()).body(getFile(scope)).asVoid();
		} catch (final WebbException e) {
			throw GamaRuntimeException.create(e, scope);
		} finally {
			scope.getGui().getStatus().endTask(scope, status);
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
	 * Fill buffer.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		throw GamaRuntimeException.error("Loading is not yet impletemented for files of type "
				+ this.getExtension(scope) + ". Please post a request for enhancement to implement "
				+ getClass().getSimpleName() + ".fillBuffer(IScope, Facets)", scope);
	}

	/**
	 * The Class FlushBufferException.
	 */
	public static class FlushBufferException extends GamaRuntimeException {

		/**
		 * Instantiates a new flush buffer exception.
		 *
		 * @param scope
		 *            the scope
		 * @param s
		 *            the s
		 * @param warning
		 *            the warning
		 */
		protected FlushBufferException(final IScope scope, final String s, final boolean warning) {
			super(scope, s, warning);
		}

	}

	/**
	 * Flush buffer.
	 *
	 * @param scope
	 *            the scope
	 * @param facets
	 *            the facets
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
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
	public IMatrix<?> matrixValue(final IScope scope, final IType contentsType, final GamaPoint preferredSize,
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
	protected IMatrix _matrixValue(final IScope scope, final IType contentsType, final GamaPoint preferredSize,
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
	public void invalidateContents() {
		buffer = null;
	}

	@Override
	public IList<String> getAttributes(final IScope scope) {
		// TODO what to return ?
		return GamaListFactory.EMPTY_LIST;
	}

	/**
	 * This method is being called from the save statement (see SaveStatement.java). The scope and all the facets
	 * declared in the save statement are passed as parameters, which allows the programmer to retrieve them (for
	 * instance, to get the crs for shape files, or the attributes to save from a list of agents, etc.). This method
	 * cannot be redefined. Instead, programmers should redefine flushBuffer(), which takes the same arguments
	 */

	@Override
	public final void save(final IScope scope, final Facets saveFacets) {

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
	public JsonValue serializeToJson(final Json json) {
		return json.typedObject(getGamlType(), "path", originalPath);
	}

}
