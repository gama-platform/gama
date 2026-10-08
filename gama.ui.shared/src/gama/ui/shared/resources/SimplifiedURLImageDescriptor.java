/*******************************************************************************************************
 *
 * SimplifiedURLImageDescriptor.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.resources;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageDataProvider;
import org.eclipse.swt.graphics.ImageFileNameProvider;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.internal.DPIUtil.ElementAtZoom;
import org.eclipse.swt.internal.NativeImageLoader;
import org.eclipse.swt.internal.image.FileFormat;

import gama.dev.DEBUG;

/**
 * An ImageDescriptor that gets its information from a URL. This class is not public API. Use
 * ImageDescriptor#createFromURL to create a descriptor that uses a URL.
 */
public class SimplifiedURLImageDescriptor extends ImageDescriptor
		implements IAdaptable, ImageDataProvider, ImageFileNameProvider {

	/** The url. */
	private final URL url;

	/**
	 * Creates a new SimplifiedURLImageDescriptor.
	 *
	 * @param url
	 *            The URL to load the image from. Must be non-null.
	 */
	public SimplifiedURLImageDescriptor(final URL url) {
		forceCaching();
		this.url = url;
	}

	/**
	 * Force caching.
	 */
	private void forceCaching() {
		try {
			Field shouldBeCached = getClass().getSuperclass().getSuperclass().getDeclaredField("shouldBeCached");
			shouldBeCached.setAccessible(true);
			shouldBeCached.setBoolean(this, true);
		} catch (Exception e) {
			DEBUG.LOG("Error in finding field 'shouldBeCached': " + e.getMessage());
			return;
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof SimplifiedURLImageDescriptor uid)) return false;
		return uid.url.equals(this.url);
	}

	/**
	 * Gets the image data.
	 *
	 * @return the image data
	 * @deprecated
	 */
	@Deprecated
	@Override
	public ImageData getImageData() { return getImageData(url); }

	/**
	 * Gets the image data.
	 *
	 * @param zoom
	 *            the zoom
	 * @return the image data
	 */
	@Override
	public ImageData getImageData(final int zoom) {
		if (url != null) {
			if (zoom == 100 || canLoadAtZoom(url, zoom)) return getImageData(url, 100, zoom);
			URL xUrl = getxURL(url, zoom);
			if (xUrl != null) {
				ImageData xdata = getImageData(xUrl, zoom, zoom);
				if (xdata != null) return xdata;
			}
		}
		return null;
	}

	/**
	 * Gets the image data.
	 *
	 * @param url
	 *            the url
	 * @return the image data
	 */
	private static ImageData getImageData(final URL url) {
		if (url == null) return null;
		try (InputStream in = getStream(url)) {
			return new ImageData(in);
		} catch (Exception e) {}
		return null;
	}

	/**
	 * Can load at zoom.
	 *
	 * @param url
	 *            the url
	 * @param zoom
	 *            the zoom
	 * @return true, if successful
	 */
	@SuppressWarnings ("restriction")
	private static boolean canLoadAtZoom(final URL url, final int zoom) {
		try (InputStream in = getStream(url)) {
			if (in != null) return FileFormat.canLoadAtZoom(new ElementAtZoom<>(in, 100), zoom);
		} catch (IOException e) {
			Policy.logException(e);
		}
		return false;
	}

	/**
	 * @param url2
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings ("resource")
	private static InputStream getStream(final URL url) throws IOException {
		return new BufferedInputStream(FileLocator.find(url).openStream());
	}

	/**
	 * Gets the image data.
	 *
	 * @param url
	 *            the url
	 * @param fileZoom
	 *            the file zoom
	 * @param targetZoom
	 *            the target zoom
	 * @return the image data
	 */
	private static ImageData getImageData(final URL url, final int fileZoom, final int targetZoom) {
		try (InputStream in = getStream(url)) {
			if (in != null) return loadImageFromStream(new BufferedInputStream(in), fileZoom, targetZoom);
		} catch (SWTException e) {
			if (e.code != SWT.ERROR_INVALID_IMAGE) throw e;
			// fall through otherwise
		} catch (IOException e) {
			Policy.logException(e);
		}
		return null;
	}

	/**
	 * Load image from stream.
	 *
	 * @param stream
	 *            the stream
	 * @param fileZoom
	 *            the file zoom
	 * @param targetZoom
	 *            the target zoom
	 * @return the image data
	 */
	@SuppressWarnings ("restriction")
	private static ImageData loadImageFromStream(final InputStream stream, final int fileZoom, final int targetZoom) {
		return NativeImageLoader.load(new ElementAtZoom<>(stream, fileZoom), new ImageLoader(), targetZoom).get(0)
				.element();
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		return url.hashCode();
	}

	/**
	 * Gets the x URL.
	 *
	 * @param url
	 *            the url
	 * @param zoom
	 *            the zoom
	 * @return the x URL
	 */
	static URL getxURL(final URL url, final int zoom) {
		String path = url.getPath();
		if (path.endsWith(".svg")) return url;
		int dot = path.lastIndexOf('.');
		if (dot != -1 && zoom > 100) {
			String lead = path.substring(0, dot);
			String tail = path.substring(dot);
			String x = zoom == 150 ? "@1.5x" : zoom == 125 ? "@1.25x" : zoom == 175 ? "@1.75x" : "@2x";
			try {
				return new URL(url.getProtocol(), url.getHost(), url.getPort(), lead + x + tail);
			} catch (MalformedURLException e) {}
		}
		return null;

	}

	/**
	 * Creates the image.
	 *
	 * @param returnMissingImageOnError
	 *            the return missing image on error
	 * @param device
	 *            the device
	 * @return the image
	 */
	@Override
	public Image createImage(final boolean returnMissingImageOnError, final Device device) {
		return new Image(device, (ImageFileNameProvider) this);
	}

	@Override
	public <T> T getAdapter(final Class<T> adapter) {
		if (adapter == URL.class) return adapter.cast(url);
		if (adapter == ImageFileNameProvider.class || adapter == ImageDataProvider.class) return adapter.cast(this);
		return null;
	}

	@Override
	public String getImagePath(final int zoom) {
		URL platformURL = FileLocator.find(zoom > 100 ? getxURL(url, zoom) : url);
		URL locatedURL;
		try {
			locatedURL = FileLocator.toFileURL(platformURL);
			String filePath = IPath.fromOSString(locatedURL.getPath()).toOSString();
			if (Files.exists(Path.of(filePath))) return filePath;
		} catch (IOException e) {}
		return null;
	}

}