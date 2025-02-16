/*******************************************************************************************************
 *
 * GamaSVGFile.java, in gama.extension.image, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.image;

import static gama.core.common.geometry.GeometryUtils.GEOMETRY_FACTORY;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;

import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.geometry.size.FloatSize;
import com.github.weisj.jsvg.parser.LoaderContext;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.renderer.awt.NullPlatformSupport;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.file;
import gama.annotations.precompiler.GamlAnnotations.no_test;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.IConcept;
import gama.core.common.geometry.Envelope3D;
import gama.core.metamodel.shape.GamaShapeFactory;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.core.util.file.GamaGeometryFile;
import gama.core.util.file.IGamaFile;
import gama.dev.DEBUG;
import gama.gaml.types.GamaGeometryType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * Class GamaSVGFile. Only loads vector shapes right now (and none of the associated elements: textures, colors, fonts,
 * etc., unless the 'image' operator is invoked on the file)
 *
 * @author drogoul
 * @since 30 déc. 2013
 *
 */
@file (
		name = "svg",
		extensions = "svg",
		buffer_type = IType.LIST,
		buffer_content = IType.GEOMETRY,
		buffer_index = IType.INT,
		concept = { IConcept.SVG },
		doc = @doc ("Represents 2D geometries described in a SVG file. The internal representation is a list of geometries. Using the 'image' operator on the file allows to retrieve the full image"))
public class GamaSVGFile extends GamaGeometryFile {
	static RenderingHints RENDER_HINTS = new RenderingHints(null);
	static {
		DEBUG.OFF();
		RENDER_HINTS.putAll(Map.of(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
				RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY, RenderingHints.KEY_DITHERING,
				RenderingHints.VALUE_DITHER_DISABLE, RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC, RenderingHints.KEY_ALPHA_INTERPOLATION,
				RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY, RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON, RenderingHints.KEY_COLOR_RENDERING,
				RenderingHints.VALUE_COLOR_RENDER_QUALITY, RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_PURE));
		RENDER_HINTS.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}

	/**
	 * Instantiates a new gama SVG file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@doc (
			value = "This file constructor allows to read a svg file",
			examples = { @example (
					value = "file f <-svg_file(\"file.svg\");",
					isExecutable = false) })
	public GamaSVGFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName);
	}

	@Override
	protected IShape buildGeometry(final IScope scope) {
		return GamaGeometryType.geometriesToGeometry(scope, getBuffer());
	}

	@Override
	public IList<String> getAttributes(final IScope scope) {
		// TODO are there attributes ?
		return GamaListFactory.create(Types.STRING);
	}

	/** The document. */
	private SVGDocument document;
	static LoaderContext lc = LoaderContext.builder().build();

	/**
	 * Gets the root.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return the root
	 * @date 21 juil. 2023
	 */
	private SVGDocument getDocument(final IScope scope) {
		if (document == null) {
			SVGLoader loader = new SVGLoader();
			File f = getFile(scope);
			try (InputStream is = Files.newInputStream(f.toPath())) {
				document = loader.load(is, f.toURI(), lc);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return document;
	}

	private void addShape(final Geometry g) {
		int n = g.getNumGeometries();
		if (n == 1) {
			if (g instanceof GeometryCollection gc) {
				addShape(gc.getGeometryN(0));
			} else {
				getBuffer().add(GamaShapeFactory.createFrom(g));
			}
		} else {
			for (int i = 0; i < n; i++) { addShape(g.getGeometryN(i)); }
		}
	}

	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		if (getBuffer() != null) return;
		setBuffer(GamaListFactory.create());
		try {
			addShape(ShapeReader.read(getDocument(scope).computeShape().getPathIterator(null, 1.0), GEOMETRY_FACTORY));
			Envelope3D env = Envelope3D.of(getBuffer());
			for (IShape s : getBuffer()) { s.setLocation(s.getLocation().minus(env.getMinX(), env.getMinY(), 0)); }
		} catch (final Exception e) {
			throw GamaRuntimeException.create(e, scope);
		}
	}

	/**
	 * Gets the image.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param b
	 *            the b
	 * @return the image
	 * @date 16 juil. 2023
	 */
	public BufferedImage getImage(final IScope scope, final boolean useCache) {
		FloatSize size = getDocument(scope).size();
		return getImage(scope, useCache, Math.round(size.width), Math.round(size.height));
	}

	/**
	 * Gets the image.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param useCache
	 *            the use cache
	 * @param width
	 *            the x
	 * @param height
	 *            the y
	 * @return the image
	 * @date 21 juil. 2023
	 */

	public BufferedImage getImage(final IScope scope, final boolean useCache, final int width, final int height) {
		if (useCache) {
			String key = getPath(scope) + width + "x" + height;
			BufferedImage im = ImageCache.getInstance().getImage(key);
			if (im == null) {
				im = getImage(scope, false, width, height);
				ImageCache.getInstance().forceCacheImage(im, key);
			}
			return im;
		}
		SVGDocument svg = getDocument(scope);
		GamaImage gi = ImageHelper.createPremultipliedBlankImage(width, height);
		Graphics2D g2 = gi.createGraphics();
		try {
			g2.addRenderingHints(RENDER_HINTS);
			svg.renderWithPlatform(NullPlatformSupport.INSTANCE, g2, new ViewBox(width, height));
		} catch (RuntimeException e) {}

		finally {
			g2.dispose();
		}
		return gi;
	}

	/**
	 * Image.
	 *
	 * @param w
	 *            the w
	 * @param h
	 *            the h
	 * @param type
	 *            the type
	 * @return the gama image
	 */
	@operator (
			can_be_const = true,
			value = "image")
	@doc ("Builds a new image from the specified file, passing the width and height in parameter ")
	@no_test
	public static GamaImage image(final IScope scope, final IGamaFile file, final int w, final int h) {
		if (file instanceof GamaSVGFile svg)
			return GamaImage.from(svg.getImage(scope, true, w, h), true, file.getPath(scope) + w + "x" + h);
		if (file instanceof GamaImageFile f) return ImageOperators.with_size(scope,
				GamaImage.from(f.getImage(scope, true), true, f.getOriginalPath()), w, h);
		return null;
	}

}
