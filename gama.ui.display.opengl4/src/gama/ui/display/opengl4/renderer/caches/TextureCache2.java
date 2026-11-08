/*******************************************************************************************************
 *
 * TextureCache2.java, in gama.ui.display.opengl4, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl4.renderer.caches;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.SinglePixelPackedSampleModel;
import java.nio.IntBuffer;
import java.util.IdentityHashMap;
import java.util.HashMap;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;

import gama.api.utils.interfaces.IImageProvider;
import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;
import gama.ui.display.opengl4.OpenGL;
import gama.ui.display.opengl.renderer.caches.ITextureCache;

/**
 * The Class TextureCache2.
 */
public class TextureCache2 implements ITextureCache {

	static {
		DEBUG.ON();
	}

	/** The volatile textures. */
	private final Map<String, Texture> volatileTextures = new ConcurrentHashMap<>();
	private final Map<BufferedImage, Map<Boolean, Texture>> imageTextures = new IdentityHashMap<>();
	private final Set<Integer> mipmappedTextures = new HashSet<>();

	/** Generate missing levels for the texture currently bound to GL_TEXTURE_2D. */
	public void ensureMipmaps(final int texture) {
		if (mipmappedTextures.contains(texture)) return;
		gl.getGL().glGenerateMipmap(GL.GL_TEXTURE_2D);
		mipmappedTextures.add(texture);
	}

	private IntBuffer pixelBuffer;
	private int[] pixelArray;
	private final Deque<Texture> reusableTextures = new ArrayDeque<>();
	private long reusableTextureBytes;
	private static final long MAX_REUSABLE_TEXTURE_BYTES = 64L * 1024 * 1024;

	/** Scene objects retain bare texture IDs, so static textures live until this context is disposed. */
	private final Cache<String, Texture> staticTextures =
			CacheBuilder.newBuilder().build();

	/** The textures to process. */
	final Map<String, IImageProvider> texturesToProcess = new ConcurrentHashMap<>();

	/** The gl. */
	final OpenGL gl;

	/** The is non power of 2 textures available. */
	// Boolean isNonPowerOf2TexturesAvailable;

	/**
	 * Instantiates a new texture cache 2.
	 *
	 * @param gl
	 *            the gl
	 */
	public TextureCache2(final OpenGL gl) {
		this.gl = gl;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.ui.display.opengl4.renderer.caches.ITextureCache#deleteVolatileTextures()
	 */
	@Override
	public void deleteVolatileTextures() {
		volatileTextures.values().forEach(this::recycleTexture);
		volatileTextures.clear();
		imageTextures.values().forEach(variants -> variants.forEach((mipmap, texture) -> {
			if (mipmap) {
				mipmappedTextures.remove(texture.getTextureObject());
				texture.destroy(gl.getGL());
			} else {
				recycleTexture(texture);
			}
		}));
		imageTextures.clear();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.ui.display.opengl4.renderer.caches.ITextureCache#dispose()
	 */
	@Override
	public void dispose() {
		DEBUG.OUT("TextureCache disposed");
		deleteVolatileTextures();
		reusableTextures.forEach(texture -> texture.destroy(gl.getGL()));
		reusableTextures.clear();
		reusableTextureBytes = 0;
		mipmappedTextures.clear();
		staticTextures.asMap().forEach((s, t) -> { t.destroy(gl.getGL()); });
		staticTextures.invalidateAll();
		staticTextures.cleanUp();
		texturesToProcess.clear();
		pixelBuffer = null;
		pixelArray = null;
	}

	/**
	 * Processs.
	 *
	 * @param file
	 *            the file
	 */
	/*
	 * (non-Javadoc)
	 *
	 * @see gama.ui.display.opengl4.renderer.caches.ITextureCache#processs(java.io.File)
	 */
	@Override
	public void processs(final IImageProvider file) {

		if (!texturesToProcess.containsKey(file.getId())) {
			DEBUG.OUT("Adding image to process " + file.getId());
			texturesToProcess.put(file.getId(), file);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.ui.display.opengl4.renderer.caches.ITextureCache#processUnloaded()
	 */
	@Override
	public void processUnloaded() {
		texturesToProcess.forEach((n, i) -> {
			if (getTexture(i, false, true) != null) { texturesToProcess.remove(n, i); }
		});
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.ui.display.opengl4.renderer.caches.ITextureCache#getTexture(java.awt.image.BufferedImage)
	 */
	@Override
	public Texture getTexture(final BufferedImage img) {
		return getTexture(img, false);
	}

	public Texture getTexture(final BufferedImage img, final boolean mipmap) {
		if (img == null) return null;
		// ModelScene.wipe invalidates these textures each simulation update, including in-place pixel changes.
		final Map<Boolean, Texture> variants = imageTextures.computeIfAbsent(img, image -> new HashMap<>());
		Texture texture = variants.get(mipmap);
		if (texture == null) {
			texture = buildTexture(gl.getGL(), img, mipmap);
			if (texture != null) { variants.put(mipmap, texture); }
			gl.invalidateTextureCache();
		}
		return texture;
	}

	/**
	 * Gets the texture.
	 *
	 * @param file
	 *            the file
	 * @param isAnimated
	 *            the is animated
	 * @param useCache
	 *            the use cache
	 * @return the texture
	 */
	@Override
	public Texture getTexture(final IImageProvider file, final boolean isAnimated, final boolean useCache) {
		if (file == null) return null;
		Texture texture = null;
		if (isAnimated || !useCache) {
			String path = file.getId();
			texture = volatileTextures.get(path);
			if (texture == null) {
				final BufferedImage image = file.getImage(null, useCache);
				DEBUG.LOG("Building a new volatile texture... " + file.getId());
				texture = this.buildTexture(gl.getGL(), image);
				volatileTextures.put(path, texture);
			}
		} else {
			try {

				texture = staticTextures.get(file.getId(), () -> buildTexture(gl.getGL(), file));
			} catch (final ExecutionException e) {
				e.printStackTrace();
			}
		}
		return texture;
	}

	/**
	 * Builds the texture.
	 *
	 * @param gl
	 *            the gl
	 * @param file
	 *            the file
	 * @return the texture
	 */
	private Texture buildTexture(final GL gl, final IImageProvider file) {
		return buildTexture(gl, file.getImage(null, GamaPreferences.Displays.OPENGL_USE_IMAGE_CACHE.getValue()));
	}

	/**
	 * Builds the texture.
	 *
	 * @param gl
	 *            the gl
	 * @param im
	 *            the im
	 * @return the texture
	 */
	Texture buildTexture(final GL gl, final BufferedImage im) {
		return buildTexture(gl, im, false);
	}

	private Texture buildTexture(final GL gl, final BufferedImage im, final boolean mipmap) {
		if (im == null) return null;
		Texture texture = null;
		try {
			// JOGL's automatic path only generates mipmaps in compatibility profiles, not GL4 core.
			final TextureData data = createTextureData(gl.getGLProfile(), im, false);
			try {
				if (!mipmap) { texture = takeReusableTexture(im.getWidth(), im.getHeight()); }
				if (texture == null) {
					texture = new Texture(gl, data);
				} else {
					texture.updateSubImage(gl, data, 0, 0, 0);
					// Recycled texture objects may have been used with different filtering.
					this.gl.invalidateTextureCache();
				}
				if (mipmap) { ensureMipmaps(texture.getTextureObject()); }
			} finally {
				data.flush();
			}
			return texture;
		} catch (final RuntimeException e) {
			if (texture != null) {
				mipmappedTextures.remove(texture.getTextureObject());
				texture.destroy(gl);
			}
			DEBUG.ERR("TextureCache2.buildTexture failed", e);
			return null;
		}
	}

	private static long textureBytes(final Texture texture) {
		return (long) texture.getWidth() * texture.getHeight() * Integer.BYTES;
	}

	private void recycleTexture(final Texture texture) {
		mipmappedTextures.remove(texture.getTextureObject());
		final long bytes = textureBytes(texture);
		if (bytes > MAX_REUSABLE_TEXTURE_BYTES || texture.getTarget() != GL.GL_TEXTURE_2D) {
			texture.destroy(gl.getGL());
			return;
		}
		reusableTextures.addLast(texture);
		reusableTextureBytes += bytes;
		while (reusableTextures.size() > 16 || reusableTextureBytes > MAX_REUSABLE_TEXTURE_BYTES) {
			final Texture evicted = reusableTextures.removeFirst();
			reusableTextureBytes -= textureBytes(evicted);
			evicted.destroy(gl.getGL());
		}
	}

	private Texture takeReusableTexture(final int width, final int height) {
		final var iterator = reusableTextures.iterator();
		while (iterator.hasNext()) {
			final Texture texture = iterator.next();
			if (texture.getWidth() == width && texture.getHeight() == height
					&& texture.getImageWidth() == width && texture.getImageHeight() == height) {
				iterator.remove();
				reusableTextureBytes -= textureBytes(texture);
				return texture;
			}
		}
		return null;
	}

	private TextureData createTextureData(final GLProfile profile, final BufferedImage image, final boolean mipmap) {
		final int w = image.getWidth(), h = image.getHeight(), size = Math.multiplyExact(w, h);
		if (pixelBuffer == null || pixelBuffer.capacity() < size) {
			pixelBuffer = Buffers.newDirectIntBuffer(size);
		}
		pixelBuffer.clear();
		final var raster = image.getRaster();
		if (image.getType() == BufferedImage.TYPE_INT_ARGB && !image.isAlphaPremultiplied()
				&& raster.getDataBuffer() instanceof DataBufferInt data && data.getNumBanks() == 1
				&& raster.getSampleModel() instanceof SinglePixelPackedSampleModel sample
				&& sample.getScanlineStride() == w && raster.getSampleModelTranslateX() == 0
				&& raster.getSampleModelTranslateY() == 0) {
			pixelBuffer.put(data.getData(), data.getOffset(), size);
		} else {
			if (pixelArray == null || pixelArray.length < size) { pixelArray = new int[size]; }
			// Normalize premultiplied, indexed and other image layouts without an intermediate image.
			image.getRGB(0, 0, w, h, pixelArray, 0, w);
			pixelBuffer.put(pixelArray, 0, size);
		}
		pixelBuffer.flip();
		return new TextureData(profile, GL.GL_RGBA, w, h, 0, GL.GL_BGRA, GL2GL3.GL_UNSIGNED_INT_8_8_8_8_REV,
				mipmap, false, true, pixelBuffer, null);
	}

}
