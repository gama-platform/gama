/**
 *
 */
package gaml.compiler.gaml.resource;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.emf.common.util.URI;

import gaml.compiler.gaml.preprocessor.GamlResourceOffsetMap;

/**
 *
 */
public class GamlResourceReader extends Reader {

	/**
	 * Offset map.
	 *
	 * @return the offset map
	 */
	final GamlResourceOffsetMap offsetMap;

	/** The delegate. */
	final Reader delegate;

	/** The uri. */
	final URI uri;

	/**
	 * Instantiates a new gaml resource reader.
	 *
	 * @param delegate
	 *            the delegate
	 * @param offsetMap
	 *            offset map.
	 */
	GamlResourceReader(final URI uri, final Reader delegate, final GamlResourceOffsetMap offsetMap) {
		this.uri = uri;
		this.delegate = delegate;
		this.offsetMap = offsetMap;
	}

	@Override
	public int read(final char[] cbuf, final int off, final int len) throws IOException {
		return delegate.read(cbuf, off, len);
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}

	/**
	 * Gets the offset map.
	 *
	 * @return the offset map
	 */
	public GamlResourceOffsetMap getOffsetMap() { return offsetMap; }

	/**
	 * Gets the uri.
	 *
	 * @return the uri
	 */
	public URI getURI() { return uri; }

}
