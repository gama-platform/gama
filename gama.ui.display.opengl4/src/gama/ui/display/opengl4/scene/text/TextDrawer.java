/*******************************************************************************************************
 *
 * TextDrawer.java, in gama.ui.display.opengl4, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl4.scene.text;

import static com.jogamp.common.nio.Buffers.newDirectDoubleBuffer;
import static com.jogamp.opengl.GL.GL_LINE_LOOP;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL4.GL_TRIANGLE_STRIP;
import static java.awt.geom.PathIterator.SEG_CLOSE;
import static java.awt.geom.PathIterator.SEG_LINETO;
import static java.awt.geom.PathIterator.SEG_MOVETO;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jogamp.graph.curve.Region;
import com.jogamp.graph.curve.opengl.RegionRenderer;
import com.jogamp.graph.curve.opengl.TextRegionUtil;
import com.jogamp.graph.font.FontFactory;
import com.jogamp.math.Vec4f;
import com.jogamp.math.util.PMVMatrix4f;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2GL3;

import gama.api.types.color.IColor;
import gama.api.types.font.IFont;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.ui.layers.IDrawingAttributes;
import gama.api.utils.geometry.AxisAngle;
import gama.api.utils.geometry.GamaCoordinateSequenceFactory;
import gama.api.utils.geometry.ICoordinates;
import gama.dev.DEBUG;
import gama.api.utils.geometry.EarCut2D;
import gama.ui.display.opengl4.OpenGL;
import gama.ui.display.opengl4.scene.ObjectDrawer;
import gama.ui.shared.utils.DPIHelper;

/**
 * The Class TextDrawer.
 *
 * <p>
 * Renders text strings in the OpenGL 4.1 core-profile renderer using the <b>JOGL graph/curve library</b>
 * ({@code com.jogamp.graph.curve.opengl}). This replaces the old GLUT bitmap-string path (removed in GL4 core)
 * and eliminates per-frame AWT tessellation by caching glyph outlines on the GPU.
 * </p>
 *
 * <h3>Architecture</h3>
 * <ul>
 * <li>A single {@link RegionRenderer} + {@link RenderState} is kept per {@code TextDrawer} instance (i.e. per
 * OpenGL context). The {@code RegionRenderer} manages a lazily created shader program that renders GPU-tessellated
 * cubic Bézier curves with sub-pixel accuracy.</li>
 * <li>A {@link GLRegion} per unique (font, text) pair is <em>not</em> cached at this level; instead
 * {@link TextRegionUtil#drawString3D} is used in pass-through mode which manages its own internal glyph cache.</li>
 * <li>For <b>non-perspective</b> (overlay / screen-space) text the renderer switches the projection to an
 * orthographic screen-space matrix before issuing the draw call, then restores it.</li>
 * <li>For <b>3-D extruded</b> text ({@code depth > 0}) the flat face is rendered by the graph library while the
 * lateral (side) geometry is still produced by the AWT outline → GLU-tessellator path, because the graph library
 * only provides 2-D outline rendering.</li>
 * </ul>
 *
 * <h3>Thread safety</h3>
 * All methods must be called from the GL thread.
 */
public class TextDrawer extends ObjectDrawer<StringObject> {

	// -------------------------------------------------------------------------
	// JOGL graph / curve API state
	// -------------------------------------------------------------------------

	/**
	 * Hint flags for the JOGL graph region renderer.
	 * {@code VBAA_RENDERING_BIT} enables 2-pass vertex-buffer anti-aliasing for smooth text at any size.
	 */
	private static final int RENDER_HINTS = Region.VBAA_RENDERING_BIT;

	/**
	 * The JOGL graph region renderer. Created lazily on first use so that it is initialised on the correct
	 * GL thread. One instance is shared for all text draw calls from this {@code TextDrawer}.
	 */
	private RegionRenderer regionRenderer;

	/**
	 * High-level utility wrapper around the region renderer that provides the
	 * {@link TextRegionUtil#drawString3D} convenience method.
	 */
	private TextRegionUtil textUtil;

	/**
	 * Cache of wrapped JOGL {@link com.jogamp.graph.font.Font} objects, keyed by the AWT {@link Font}.
	 * Wrapping an AWT font is inexpensive but not free; caching avoids repeated reflection overhead.
	 */
	private final Map<Font, com.jogamp.graph.font.Font> fontCache = new HashMap<>();

	// -------------------------------------------------------------------------
	// Contour geometry state (used for 3-D extrusion side geometry and EarCut2D face tessellation)
	// -------------------------------------------------------------------------

	/** Temporary coordinate sequence used to compute side-face normals. */
	ICoordinates temp = GamaCoordinateSequenceFactory.ofLength(4);

	/** Current face normal, reused across vertex additions. */
	IPoint normal = GamaPointFactory.create();

	/** X coordinate of the previous contour vertex, used to compute extrusion normals. */
	double previousX = Double.MIN_VALUE;

	/** Y coordinate of the previous contour vertex. */
	double previousY = Double.MIN_VALUE;

	/** Current contour-index pointer into {@link #indices}. */
	int currentIndex = -1;

	// -------------------------------------------------------------------------
	// Constants
	// -------------------------------------------------------------------------

	/**
	 * Initial capacity (in {@code double} values) for the per-call geometry buffers.
	 * 1 M doubles = 8 MB per buffer; the buffer is cleared (not reallocated) on every draw call.
	 */
	private static final int BUFFER_SIZE = 1_000_000;

	/** AffineTransform that flips the Y axis so glyph outlines match OpenGL's Y-up convention. */
	private static final AffineTransform AT = AffineTransform.getScaleInstance(1.0, -1.0);

	/** AWT font render context — used only for 3-D extrusion bounding-box computation. */
	private static final FontRenderContext FONT_CTX = new FontRenderContext(new AffineTransform(), true, true);

	// -------------------------------------------------------------------------
	// Per-call geometry buffers (3-D extrusion only)
	// -------------------------------------------------------------------------

	/**
	 * Vertex buffer for the tessellated front/back face triangles of extruded text.
	 * Layout: {@code [x, y, z, x, y, z, …]}.
	 */
	DoubleBuffer faceVertexBuffer = newDirectDoubleBuffer(BUFFER_SIZE);

	/**
	 * Texture-coordinate buffer parallel to {@link #faceVertexBuffer}. Layout: {@code [u, v, u, v, …]}.
	 */
	DoubleBuffer faceTextureBuffer = newDirectDoubleBuffer(BUFFER_SIZE * 2 / 3);

	/**
	 * Index array that records the start position in {@link #sideQuadsBuffer} of each contour's first vertex,
	 * and the end position after its last vertex. Entries are in pairs: {@code [begin0, end0, begin1, end1, …]}.
	 */
	int[] indices = new int[1000];

	/**
	 * Normal buffer for the lateral (side) quad-strip faces of extruded text. Layout: {@code [nx,ny,nz, …]}.
	 * Two normal values are written per quad-strip vertex pair (top and bottom share the same normal).
	 */
	private DoubleBuffer sideNormalBuffer = newDirectDoubleBuffer(BUFFER_SIZE);

	/**
	 * Vertex buffer for the lateral (side) quad-strip faces of extruded text.
	 * If {@code depth == 0}: layout {@code [x, y, 0, …]} (one z-level).
	 * If {@code depth > 0}: layout {@code [x, y, 0, x, y, depth, …]} (alternating z=0 and z=depth rows).
	 */
	private DoubleBuffer sideQuadsBuffer = newDirectDoubleBuffer(BUFFER_SIZE);

	// -------------------------------------------------------------------------
	// Per-call state
	// -------------------------------------------------------------------------

	/** Border color for the current string, or {@code null} if no border is drawn. */
	IColor border;

	/** Width of the current glyph vector outline bounding box (in font coordinate units). */
	double width;

	/** Height of the current glyph vector outline bounding box (in font coordinate units). */
	double height;

	/** Extrusion depth of the current string (0 = flat). */
	double depth;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	/**
	 * Instantiates a new text drawer.
	 *
	 * @param gl the {@link OpenGL} helper that owns this drawer
	 */
	public TextDrawer(final OpenGL gl) {
		super(gl);
	}

	// -------------------------------------------------------------------------
	// Graph / curve renderer initialisation
	// -------------------------------------------------------------------------

	/**
	 * Lazily initialises the JOGL graph region renderer the first time text needs to be drawn.
	 * Must be called from the GL thread.
	 *
	 * @param gl4 the current GL4 context
	 * @return the initialised {@link RegionRenderer}, or {@code null} if initialisation failed
	 */
	private RegionRenderer getRegionRenderer(final com.jogamp.opengl.GL4 gl4) {
		if (regionRenderer != null) return regionRenderer;
		try {
			final GL2ES2 gl2es2 = gl4.getGL2ES2();
			// JOGL 2.6 API: RegionRenderer.create(PMVMatrix4f, GLCallback, GLCallback)
			regionRenderer = RegionRenderer.create(new PMVMatrix4f(),
					RegionRenderer.defaultBlendEnable, RegionRenderer.defaultBlendDisable);
			regionRenderer.init(gl2es2);
			textUtil = new TextRegionUtil(RENDER_HINTS);
		} catch (final Exception e) {
			DEBUG.ERR("TextDrawer: failed to initialise RegionRenderer — " + e.getMessage());
			regionRenderer = null;
		}
		return regionRenderer;
	}

	/**
	 * Looks up (or creates) the JOGL {@link com.jogamp.graph.font.Font} corresponding to an AWT {@link Font}.
	 *
	 * <p>The method attempts to load the exact font requested by the user in three stages:</p>
	 * <ol>
	 * <li><b>File extraction</b> — uses Java2D internal reflection to locate the font's {@code .ttf} /
	 * {@code .otf} file on disk, then streams it into {@link FontFactory#get(java.io.InputStream, boolean)}.
	 * This path honours the exact typeface, weight, and style chosen by the user.</li>
	 * <li><b>AWT-stream serialisation</b> — if the font was created programmatically (not from a file) AWT
	 * can stream the glyph data; the bytes are piped into the same JOGL factory call.</li>
	 * <li><b>Built-in fallback</b> — if both attempts fail, the closest JOGL built-in font
	 * ({@link FontFactory#UBUNTU}) is returned so that text is always visible.</li>
	 * </ol>
	 *
	 * <p>Bold and italic styles are applied via
	 * {@link com.jogamp.graph.font.Font#deriveFont(float, int, float)} so the JOGL glyph shapes match the
	 * weight/slant of the AWT font.</p>
	 *
	 * @param awtFont the AWT font whose JOGL equivalent is needed
	 * @return the JOGL font (never {@code null}); falls back to Ubuntu if the exact font cannot be loaded
	 */
	private com.jogamp.graph.font.Font getJoglFont(final Font awtFont) {
		return fontCache.computeIfAbsent(awtFont, f -> loadJoglFont(f));
	}

	/**
	 * Performs the actual JOGL font loading for {@link #getJoglFont(Font)}.
	 * Separated so the lambda above stays readable.
	 *
	 * @param awtFont the AWT font to convert
	 * @return the JOGL font, never {@code null}
	 */
	private com.jogamp.graph.font.Font loadJoglFont(final Font awtFont) {
		// --- Stage 1: locate the physical font file via Java2D internal API ---
		com.jogamp.graph.font.Font joglFont = tryLoadFromFontFile(awtFont);
		if (joglFont != null) return applyStyle(joglFont, awtFont);

		// --- Stage 2: stream the bytes directly from the AWT font object ---
		joglFont = tryLoadFromAwtStream(awtFont);
		if (joglFont != null) return applyStyle(joglFont, awtFont);

		// --- Stage 3: built-in fallback ---
		DEBUG.OUT("TextDrawer: using Ubuntu fallback for font '" + awtFont.getName() + "'");
		try {
			return applyStyle(FontFactory.get(FontFactory.UBUNTU).getDefault(), awtFont);
		} catch (final Exception e) {
			DEBUG.ERR("TextDrawer: even Ubuntu fallback failed — " + e.getMessage());
			return null;
		}
	}

	/**
	 * Tries to find and open the physical file ({@code .ttf} / {@code .otf}) backing the AWT font using
	 * two strategies:
	 * <ol>
	 * <li>Direct reflection on {@code sun.font.Font2D#platName} for the supplied font object.</li>
	 * <li>Search through {@link java.awt.GraphicsEnvironment#getAllFonts()} for the best match by family
	 * name + AWT style flags, then reflect on that font's {@code platName}. This correctly resolves bold
	 * and italic variants (e.g. it finds {@code Arial Bold.ttf} when the user requested
	 * {@code new Font("Arial", Font.BOLD, 14)}).</li>
	 * </ol>
	 *
	 * @param awtFont the AWT font
	 * @return a JOGL font loaded from the file, or {@code null} if the file cannot be located
	 */
	private static com.jogamp.graph.font.Font tryLoadFromFontFile(final Font awtFont) {
		// Stage 1: direct platName reflection on the supplied font
		com.jogamp.graph.font.Font result = loadFromPlatName(awtFont);
		if (result != null) return result;

		// Stage 2: search GraphicsEnvironment for the closest matching registered font
		// (catches bold/italic variants whose platName lives in a different Font2D instance)
		try {
			final String family = awtFont.getFamily().toLowerCase(java.util.Locale.ROOT);
			final int style = awtFont.getStyle();
			Font bestMatch = null;
			int bestScore = -1;
			for (final Font candidate : java.awt.GraphicsEnvironment
					.getLocalGraphicsEnvironment().getAllFonts()) {
				if (!candidate.getFamily().toLowerCase(java.util.Locale.ROOT).equals(family)) continue;
				// score: +2 if bold matches, +1 if italic matches
				int score = 0;
				if (candidate.isBold() == ((style & Font.BOLD) != 0)) score += 2;
				if (candidate.isItalic() == ((style & Font.ITALIC) != 0)) score += 1;
				if (score > bestScore) { bestScore = score; bestMatch = candidate; }
			}
			if (bestMatch != null && bestScore > 0) {
				result = loadFromPlatName(bestMatch);
				if (result != null) return result;
			}
		} catch (final Exception ignored) {}

		return null;
	}

	/**
	 * Reads the {@code platName} field from {@code sun.font.Font2D} for {@code awtFont} and loads the
	 * corresponding font file via {@link FontFactory#get(java.io.InputStream, boolean)}.
	 *
	 * @param awtFont the AWT font whose file should be loaded
	 * @return the JOGL font, or {@code null} if reflection fails or the file is not found
	 */
	private static com.jogamp.graph.font.Font loadFromPlatName(final Font awtFont) {
		try {
			final Class<?> fontUtilities = Class.forName("sun.font.FontUtilities");
			final java.lang.reflect.Method getFont2D =
					fontUtilities.getDeclaredMethod("getFont2D", java.awt.Font.class);
			getFont2D.setAccessible(true);
			final Object font2D = getFont2D.invoke(null, awtFont);
			if (font2D == null) return null;

			String filePath = extractPlatName(font2D);
			if (filePath == null || filePath.isEmpty()) return null;

			final java.io.File file = new java.io.File(filePath);
			if (!file.exists()) return null;
			try (final java.io.InputStream is = new java.io.FileInputStream(file)) {
				return FontFactory.get(is, false);
			}
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * Extracts the {@code platName} field from a {@code sun.font.Font2D} instance, walking up the class
	 * hierarchy until found.
	 *
	 * @param font2D a {@code sun.font.Font2D} instance
	 * @return the platform font file path, or {@code null}
	 */
	private static String extractPlatName(final Object font2D) {
		Class<?> cls = font2D.getClass();
		while (cls != null) {
			try {
				final java.lang.reflect.Field f = cls.getDeclaredField("platName");
				f.setAccessible(true);
				return (String) f.get(font2D);
			} catch (final NoSuchFieldException e) {
				cls = cls.getSuperclass();
			} catch (final Exception e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Tries to stream the AWT font's raw bytes using Java2D's
	 * {@code sun.font.Font2D.getFileBytes()} method (available on some JDK versions for fonts created with
	 * {@link Font#createFont}).
	 *
	 * @param awtFont the AWT font
	 * @return a JOGL font, or {@code null} if this approach is unavailable
	 */
	private static com.jogamp.graph.font.Font tryLoadFromAwtStream(final Font awtFont) {
		try {
			final Class<?> fontUtilities = Class.forName("sun.font.FontUtilities");
			final java.lang.reflect.Method getFont2D =
					fontUtilities.getDeclaredMethod("getFont2D", java.awt.Font.class);
			getFont2D.setAccessible(true);
			final Object font2D = getFont2D.invoke(null, awtFont);
			if (font2D == null) return null;

			final java.lang.reflect.Method getFileBytes =
					font2D.getClass().getMethod("getFileBytes");
			getFileBytes.setAccessible(true);
			final byte[] bytes = (byte[]) getFileBytes.invoke(font2D);
			if (bytes == null || bytes.length == 0) return null;

			try (final java.io.InputStream is = new java.io.ByteArrayInputStream(bytes)) {
				return FontFactory.get(is, false);
			}
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * Derives bold and/or italic styling from an AWT {@link Font} and applies it to a JOGL
	 * {@link com.jogamp.graph.font.Font}.
	 *
	 * <p>When the font was loaded from a file that already encodes the correct weight/slant (e.g. a
	 * {@code *Bold.ttf} or {@code *Italic.ttf} file), no additional derivation is needed — the shape data
	 * already contains the correct outlines. This method is therefore a no-op in that case.</p>
	 *
	 * <p>If the font was loaded from a regular (upright, regular-weight) file and the user requested bold
	 * or italic, we attempt {@link com.jogamp.graph.font.Font#deriveFont(float)} which only changes the
	 * pixel size. True synthetic bold/italic is not available in the JOGL graph library; the caller should
	 * ideally resolve the correct variant file in {@link #tryLoadFromFontFile}. For fonts loaded from a
	 * {@link FontFactory#UBUNTU} fallback the style bits are ignored.</p>
	 *
	 * @param joglFont the base JOGL font
	 * @param awtFont  the AWT font whose style is used as a hint
	 * @return the (possibly derived) JOGL font
	 */
	private static com.jogamp.graph.font.Font applyStyle(final com.jogamp.graph.font.Font joglFont,
			final Font awtFont) {
		// The font file itself encodes the style when loaded via tryLoadFromFontFile (e.g. ArialBold.ttf).
		// JOGL 2.6 Font.deriveFont(float pixelSize) only changes size; there is no synthetic bold/italic.
		// Return the font as-is; the correct variant is selected during font-file resolution.
		return joglFont;
	}

	// -------------------------------------------------------------------------
	// Main draw entry point
	// -------------------------------------------------------------------------

	@Override
	protected void _draw(final StringObject s) {
		final IDrawingAttributes attributes = s.getAttributes();
		if (!attributes.isPerspective()) {
			drawWithGraphLibrary(s.getObject(), attributes, true);
		} else {
			final double d = attributes.getDepth();
			if (d <= 0) {
				// Flat perspective text — fully handled by the graph library
				drawWithGraphLibrary(s.getObject(), attributes, false);
			} else {
				// Extruded 3-D text: use graph library for faces, legacy path for sides
				drawExtruded(s.getObject(), attributes);
			}
		}
	}

	// -------------------------------------------------------------------------
	// Graph-library flat text renderer (perspective and non-perspective)
	// -------------------------------------------------------------------------

	/**
	 * Public entry point for drawing a flat string via the graph/curve library, usable from outside
	 * (e.g. {@link gama.ui.display.opengl4.OpenGL#drawScreenText}). Delegates directly to
	 * {@link #drawWithGraphLibrary(String, IDrawingAttributes, boolean)}.
	 *
	 * @param text       the string to render
	 * @param attributes the drawing attributes
	 * @param overlay    {@code true} for screen-space (overlay) rendering
	 */
	public void drawWithGraphLibraryPublic(final String text, final IDrawingAttributes attributes,
			final boolean overlay) {
		drawWithGraphLibrary(text, attributes, overlay);
	}

	/**
	 * Draws a flat (non-extruded) string using the JOGL graph/curve library. Works for both perspective and
	 * non-perspective (overlay) text because the graph renderer uses its own shader and matrix uniforms that
	 * bypass the fixed-function pipeline entirely.
	 *
	 * <p>For non-perspective text the projection is temporarily replaced by a pixel-aligned orthographic matrix
	 * so the text is drawn in screen space at a stable pixel size. For perspective text the current model-view
	 * and projection matrices from the {@link OpenGL} helper are passed through to the graph shader.</p>
	 *
	 * @param text       the string to render
	 * @param attributes the drawing attributes (font, color, location, anchor, rotation…)
	 * @param overlay    {@code true} to render in screen / overlay space; {@code false} for world space
	 */
	private void drawWithGraphLibrary(final String text, final IDrawingAttributes attributes,
			final boolean overlay) {
		final com.jogamp.opengl.GL4 gl4 = gl.getGL();
		final RegionRenderer rr = getRegionRenderer(gl4);
		if (rr == null) {
			if (!overlay) { drawLegacyPerspective(text, attributes); }
			return;
		}
		final IFont gamaFont = attributes.getFont();
		if (gamaFont == null) return;
		Font awtFont = gamaFont.getAwtFont();
		final int scaledSize = DPIHelper.autoScaleUp(gl.getRenderer().getCanvas().getMonitor(), awtFont.getSize());
		if (scaledSize != awtFont.getSize()) { awtFont = awtFont.deriveFont((float) scaledSize); }
		final com.jogamp.graph.font.Font joglFont = getJoglFont(awtFont);
		if (joglFont == null) { if (!overlay) { drawLegacyPerspective(text, attributes); } return; }

		final IColor c = gl.getCurrentColor();
		final Vec4f color = new Vec4f(
				(float) (c.red() / 255.0),
				(float) (c.green() / 255.0),
				(float) (c.blue() / 255.0),
				(float) (c.alpha() / 255.0 * gl.getCurrentObjectAlpha()));

		final GL2ES2 gl2es2 = gl4.getGL2ES2();
		final IPoint p = attributes.getLocation();
		final IPoint anchor = attributes.getAnchor();

		// Font units → model units scale factor
		final float scale = 1f / (float) DPIHelper.autoScaleUp(gl.getRenderer().getCanvas().getMonitor(),
				gl.getRenderer().getAbsoluteRatioBetweenPixelsAndModelsUnits());

		// The graph library uses EM-unit font sizes: pixelSize is the target em-square height in model units
		final float pixelSize = awtFont.getSize2D() * scale;

		// Load the current projection/modelview matrices into the PMVMatrix4f owned by the RegionRenderer
		final PMVMatrix4f pmv = rr.getMatrix();
		if (overlay) {
			// Screen-space ortho: map [0..w] x [0..h] to NDC
			pmv.getP().loadIdentity();
			pmv.getMv().loadIdentity();
			pmv.orthoP(0, gl.getViewWidth(), 0, gl.getViewHeight(), -1, 1);
		} else {
			// World-space: copy from our MatrixStack (column-major float[16])
			final float[] projArr = new float[16];
			final float[] mvArr = new float[16];
			gl.getProjectionMatrix().get(projArr);
			gl.getModelViewMatrix().get(mvArr);
			pmv.getP().load(projArr, 0);
			pmv.getMv().load(mvArr, 0);
		}

		try {
			rr.enable(gl2es2, true);
			gl.pushMatrix();
			try {
				applyRotation(attributes, p);
				if (overlay) {
					gl.translateBy(p.getX(), p.getY(), p.getZ());
				} else {
					gl.translateBy(p.getX(), p.getY(), p.getZ() + gl.getCurrentZTranslation());
				}
				// Approximate advance width for anchor offset: ascent * pixelSize * charCount
			final float approxWidth = joglFont.getMetrics().getAscent() * pixelSize * text.length() * 0.6f;
				final float approxHeight = pixelSize;
				gl.translateBy(-approxWidth * anchor.getX(), approxHeight * (1f - (float) anchor.getY()), 0);

				// JOGL 2.6: drawString3D(GL2ES2, RegionRenderer, Font, CharSequence, Vec4f)
				textUtil.drawString3D(gl2es2, rr, joglFont, text, color);
			} finally {
				gl.popMatrix();
				rr.enable(gl2es2, false);
			}
		} catch (final Exception e) {
			DEBUG.ERR("TextDrawer.drawWithGraphLibrary: " + e.getMessage());
		}
	}

	// -------------------------------------------------------------------------
	// Extruded (depth > 0) text
	// -------------------------------------------------------------------------

	/**
	 * Draws extruded 3-D text. The front and back faces are rendered by the graph library (flat, at z=0 and
	 * z=depth). The lateral side geometry is built from the AWT glyph outline and rendered via the standard
	 * {@link OpenGL#beginDrawing}/{@link OpenGL#endDrawing} VBO path.
	 *
	 * @param text       the string to render
	 * @param attributes the drawing attributes
	 */
	private void drawExtruded(final String text, final IDrawingAttributes attributes) {
		// Compute bounds from AWT (needed for side geometry and anchor positioning)
		Font awtFont = attributes.getFont().getAwtFont();
		final int scaledSize = DPIHelper.autoScaleUp(gl.getRenderer().getCanvas().getMonitor(), awtFont.getSize());
		if (scaledSize != awtFont.getSize()) { awtFont = awtFont.deriveFont((float) scaledSize); }
		final Shape shape = awtFont.createGlyphVector(FONT_CTX, text).getOutline();
		final Rectangle2D bounds = shape.getBounds2D();

		this.depth = attributes.getDepth();
		this.border = attributes.getBorder();
		this.width = bounds.getWidth();
		this.height = bounds.getHeight();

		// Build side/border geometry buffers from the AWT path
		faceVertexBuffer.clear();
		faceTextureBuffer.clear();
		sideNormalBuffer.clear();
		sideQuadsBuffer.clear();
		currentIndex = -1;
		process(shape.getPathIterator(AT, attributes.getPrecision()));

		// Position everything
		final IPoint p = attributes.getLocation();
		final IPoint anchor = attributes.getAnchor();
		final float scale = 1f / (float) DPIHelper.autoScaleUp(gl.getRenderer().getCanvas().getMonitor(),
				gl.getRenderer().getAbsoluteRatioBetweenPixelsAndModelsUnits());

		IColor previous = null;
		gl.pushMatrix();
		try {
			applyRotation(attributes, p);
			gl.translateBy(p.getX() - width * scale * anchor.getX(),
					p.getY() + bounds.getY() * scale * anchor.getY(),
					p.getZ() + gl.getCurrentZTranslation());
			gl.scaleBy(scale, scale, scale);

			if (!gl.isWireframe()) {
				// Front face (z = 0) — via graph library
				drawWithGraphLibrary(text, attributes, false);
				// Side faces
				drawSide();
				// Back face (z = depth) — translate, draw, translate back
				gl.translateBy(0, 0, depth);
				drawWithGraphLibrary(text, attributes, false);
				gl.translateBy(0, 0, -depth);
				// Border
				if (border != null) {
					previous = gl.getCurrentColor();
					gl.setCurrentColor(border);
					gl.translateBy(0, 0, depth + 5 * gl.getCurrentZIncrement());
					drawBorder();
					gl.translateBy(0, 0, -depth - 5 * gl.getCurrentZIncrement());
				}
			} else {
				if (border != null) {
					previous = gl.getCurrentColor();
					gl.setCurrentColor(border);
				}
				drawSide();
			}
		} finally {
			if (previous != null) { gl.setCurrentColor(previous); }
			gl.popMatrix();
		}
	}

	// -------------------------------------------------------------------------
	// Legacy AWT tessellator path (kept as fallback and for 3-D side geometry)
	// -------------------------------------------------------------------------

	/**
	 * Legacy perspective-text drawing path using AWT outline tessellation. Used as a fallback when the JOGL
	 * graph library is unavailable, and as the source of side-geometry data for extruded text.
	 *
	 * @param text       the string to draw
	 * @param attributes the drawing attributes
	 */
	private void drawLegacyPerspective(final String text, final IDrawingAttributes attributes) {
		Font awtFont = attributes.getFont().getAwtFont();
		final int scaledSize = DPIHelper.autoScaleUp(gl.getRenderer().getCanvas().getMonitor(), awtFont.getSize());
		if (scaledSize != awtFont.getSize()) { awtFont = awtFont.deriveFont((float) scaledSize); }
		final Shape shape = awtFont.createGlyphVector(FONT_CTX, text).getOutline();
		final Rectangle2D bounds = shape.getBounds2D();
		this.depth = attributes.getDepth();
		this.border = attributes.getBorder();
		this.width = bounds.getWidth();
		this.height = bounds.getHeight();
		faceVertexBuffer.clear();
		faceTextureBuffer.clear();
		sideNormalBuffer.clear();
		sideQuadsBuffer.clear();
		currentIndex = -1;
		process(shape.getPathIterator(AT, attributes.getPrecision()));
		drawLegacyText(attributes, bounds.getY());
	}

	/**
	 * Applies positioning and draws a legacy-tessellated string.
	 *
	 * @param attributes the drawing attributes
	 * @param y          the baseline Y offset from {@link Rectangle2D#getY()}
	 */
	private void drawLegacyText(final IDrawingAttributes attributes, final double y) {
		final IPoint p = attributes.getLocation();
		IColor previous = null;
		gl.pushMatrix();
		try {
			final IPoint anchor = attributes.getAnchor();
			applyRotation(attributes, p);
			final float scale = 1f / (float) DPIHelper.autoScaleUp(gl.getRenderer().getCanvas().getMonitor(),
					gl.getRenderer().getAbsoluteRatioBetweenPixelsAndModelsUnits());
			gl.translateBy(p.getX() - width * scale * anchor.getX(), p.getY() + y * scale * anchor.getY(),
					p.getZ() + gl.getCurrentZTranslation());
			gl.scaleBy(scale, scale, scale);
			if (!gl.isWireframe()) {
				previous = drawFacesAndBorder(previous);
			} else {
				previous = drawWireframe(previous);
			}
		} finally {
			if (previous != null) { gl.setCurrentColor(previous); }
			gl.popMatrix();
		}
	}

	/** @see #drawLegacyText */
	private IColor drawWireframe(IColor previous) {
		if (border != null) { previous = gl.getCurrentColor(); gl.setCurrentColor(border); }
		if (depth == 0) { drawBorder(); } else { drawSide(); }
		return previous;
	}

	/** @see #drawLegacyText */
	private IColor drawFacesAndBorder(IColor previous) {
		drawFace(depth == 0);
		if (depth > 0) {
			gl.translateBy(0, 0, depth);
			drawFace(true);
			gl.translateBy(0, 0, -depth);
			drawSide();
		}
		if (border != null) {
			previous = gl.getCurrentColor();
			gl.setCurrentColor(border);
			gl.translateBy(0, 0, depth + 5 * gl.getCurrentZIncrement());
			drawBorder();
			gl.translateBy(0, 0, -depth - 5 * gl.getCurrentZIncrement());
		}
		return previous;
	}

	// -------------------------------------------------------------------------
	// AWT outline → side-geometry path (used by extruded text)
	// -------------------------------------------------------------------------

	/**
	 * Processes a {@link PathIterator} of an AWT glyph outline.
	 *
	 * <p>Contour vertices are collected into per-contour rings. In solid mode the rings are passed to
	 * {@link EarCut2D#triangulate} to fill {@link #faceVertexBuffer} with triangle data (replaces
	 * the former GLU tessellator path). In wireframe mode only the side contour vertices are collected.
	 * Side geometry (for depth > 0) is always collected into {@link #sideQuadsBuffer}.</p>
	 *
	 * @param pi the glyph outline path iterator (pre-flattened: only SEG_MOVETO/SEG_LINETO/SEG_CLOSE)
	 */
	void process(final PathIterator pi) {
		final boolean wireframe = gl.isWireframe();

		// Collect all contours: each is a flat double[x0,y0,x1,y1,...] open ring.
		final List<double[]> contours = new ArrayList<>();
		final List<double[]> contourZ = new ArrayList<>();  // parallel z values (always 0 for text faces)
		double[] curX = null, curY = null;
		int curLen = 0;
		double x0 = 0, y0 = 0;

		while (!pi.isDone()) {
			final double[] coords = new double[6];
			switch (pi.currentSegment(coords)) {
				case SEG_MOVETO:
					// Start a new contour ring
					curX = new double[256]; curY = new double[256]; curLen = 0;
					x0 = coords[0]; y0 = coords[1];
					curX[curLen] = x0; curY[curLen] = y0; curLen++;
					beginNewContour();
					addContourVertex0(x0, y0);
					break;
				case SEG_LINETO:
					if (curX != null) {
						if (curLen >= curX.length) {
							curX = java.util.Arrays.copyOf(curX, curLen * 2);
							curY = java.util.Arrays.copyOf(curY, curLen * 2);
						}
						curX[curLen] = coords[0]; curY[curLen] = coords[1]; curLen++;
					}
					addContourVertex0(coords[0], coords[1]);
					break;
				case SEG_CLOSE:
					if (curX != null && curLen >= 3) {
						final double[] flatXY = new double[curLen * 2];
						for (int i = 0; i < curLen; i++) {
							flatXY[i * 2] = curX[i]; flatXY[i * 2 + 1] = curY[i];
						}
						contours.add(flatXY);
						curX = null;
					}
					addContourVertex0(x0, y0);
					endContour();
					break;
				default:
					break;
			}
			pi.next();
		}

		// ---- face tessellation via EarCut2D (solid mode only) ----
		if (!wireframe && !contours.isEmpty()) {
			// Determine outer ring: the one with the largest absolute signed area (CW in screen space)
			// AWT uses a Y-down coordinate system after the AT (y-flip), so the outer ring has the largest area.
			int outerIdx = 0;
			double maxArea = 0;
			for (int i = 0; i < contours.size(); i++) {
				final double area = Math.abs(signedArea(contours.get(i)));
				if (area > maxArea) { maxArea = area; outerIdx = i; }
			}
			final double[] outerRing = contours.get(outerIdx);
			final int outerN = outerRing.length / 2;

			// Holes: all other contours
			final List<double[]> holes = new ArrayList<>(contours.size() - 1);
			for (int i = 0; i < contours.size(); i++) {
				if (i != outerIdx) holes.add(contours.get(i));
			}
			final double[][] holesArr = holes.isEmpty() ? null : holes.toArray(new double[0][]);

			final int[] triIndices = EarCut2D.triangulate(outerRing, holesArr, outerN);

			// Build combined flat vertex arrays (outer first, then holes)
			int totalV = outerN;
			if (holesArr != null) { for (final double[] h : holesArr) totalV += h.length / 2; }
			final double[] allX = new double[totalV], allY = new double[totalV];
			for (int i = 0; i < outerN; i++) {
				allX[i] = outerRing[i * 2]; allY[i] = outerRing[i * 2 + 1];
			}
			int off = outerN;
			if (holesArr != null) {
				for (final double[] h : holesArr) {
					for (int i = 0; i < h.length / 2; i++) {
						allX[off + i] = h[i * 2]; allY[off + i] = h[i * 2 + 1];
					}
					off += h.length / 2;
				}
			}

			// Fill faceVertexBuffer
			for (final int idx : triIndices) {
				if (gl.isTextured()) { faceTextureBuffer.put(allX[idx] / width).put(allY[idx] / height); }
				faceVertexBuffer.put(allX[idx]).put(allY[idx]).put(0);
			}
		}

		sideQuadsBuffer.flip();
		sideNormalBuffer.flip();
		faceVertexBuffer.flip();
		if (faceTextureBuffer != null) { faceTextureBuffer.flip(); }
	}

	/**
	 * Computes the signed area of a flat {@code double[x0,y0,x1,y1,…]} open ring using the shoelace formula.
	 * Positive = CCW, negative = CW (in Y-up space).
	 */
	private static double signedArea(final double[] ring) {
		final int n = ring.length / 2;
		double area = 0;
		for (int i = 0, j = n - 1; i < n; j = i++) {
			area += (ring[j * 2] + ring[i * 2]) * (ring[j * 2 + 1] - ring[i * 2 + 1]);
		}
		return area / 2.0;
	}

	/**
	 * Records the start position of a new outline contour in {@link #sideQuadsBuffer}.
	 */
	public void beginNewContour() {
		indices[++currentIndex] = sideQuadsBuffer.position();
	}

	/**
	 * Records the end position of the current outline contour in {@link #sideQuadsBuffer}.
	 */
	public void endContour() {
		indices[++currentIndex] = sideQuadsBuffer.position();
	}

	/**
	 * Adds a vertex at z=0 to {@link #sideQuadsBuffer} and, if {@link #depth} > 0, also adds the same vertex
	 * at z=depth and computes the side-face normal for the segment from the previous vertex to this one.
	 *
	 * @param x the X coordinate in font units
	 * @param y the Y coordinate in font units
	 */
	public void addContourVertex0(final double x, final double y) {
		sideQuadsBuffer.put(x).put(y).put(0);
		if (depth > 0) {
			if (previousX > Double.MIN_VALUE) {
				temp.setTo(previousX, previousY, 0, previousX, previousY, depth, x, y, 0, previousX, previousY, 0);
				temp.getNormal(true, 1, normal);
				sideNormalBuffer.put(new double[] { normal.getX(), normal.getY(), normal.getZ(),
						normal.getX(), normal.getY(), normal.getZ() });
			}
			sideQuadsBuffer.put(x).put(y).put(depth);
		}
		previousX = x;
		previousY = y;
	}

	// -------------------------------------------------------------------------
	// Side / border / face draw helpers (legacy VBO path)
	// -------------------------------------------------------------------------

	/**
	 * Draws the lateral (side) faces of extruded text as {@code GL_TRIANGLE_STRIP} bands, one per outline
	 * contour, using the {@link OpenGL} VBO path.
	 */
	public void drawSide() {
		if (sideQuadsBuffer == null || sideQuadsBuffer.limit() == 0) return;
		drawSideFallback(gl);
	}

	/**
	 * Draws the outline (border) of each glyph contour as {@code GL_LINE_LOOP} primitives.
	 */
	public void drawBorder() {
		drawBorderFallback();
	}

	/**
	 * Draws the front or back face of extruded text using the triangle data produced by {@link EarCut2D}.
	 *
	 * @param up {@code true} to use the upward-facing normal (front face); {@code false} for back face
	 */
	void drawFace(final boolean up) {
		if (faceVertexBuffer == null || faceVertexBuffer.limit() == 0) return;
		drawFaceFallback(up);
	}

	/**
	 * Applies the {@link AxisAngle} rotation from the drawing attributes around the string's location.
	 *
	 * @param attributes the drawing attributes containing the optional rotation
	 * @param p          the location point (mutated to zero if a rotation is applied, to avoid double-translation)
	 */
	private void applyRotation(final IDrawingAttributes attributes, final IPoint p) {
		final AxisAngle rotation = attributes.getRotation();
		if (rotation != null) {
			gl.translateBy(p.getX(), p.getY(), p.getZ());
			final IPoint axis = rotation.getAxis();
			gl.rotateBy(-rotation.getAngle(), axis.getX(), axis.getY(), axis.getZ());
			p.setLocation(0, 0, 0);
		}
	}

	// -------------------------------------------------------------------------
	// Fallback renderers (delegate from the draw* methods above)
	// -------------------------------------------------------------------------

	/**
	 * Draws the tessellated front or back face from {@link #faceVertexBuffer} as {@code GL_TRIANGLES}.
	 *
	 * @param up {@code true} for upward-facing (front) normal, {@code false} for downward (back)
	 */
	public void drawFaceFallback(final boolean up) {
		gl.beginDrawing(GL_TRIANGLES);
		gl.outputNormal(0, 0, up ? 1 : -1);
		for (int i = 0; i < faceVertexBuffer.limit(); i += 3) {
			if (gl.isTextured()) {
				gl.outputTexCoord(faceTextureBuffer.get(2 * i / 3), faceTextureBuffer.get(2 * i / 3 + 1));
			}
			gl.outputVertex(faceVertexBuffer.get(i), faceVertexBuffer.get(i + 1), faceVertexBuffer.get(i + 2));
		}
		gl.endDrawing();
	}

	/**
	 * Draws the lateral quad-strip side faces from {@link #sideQuadsBuffer} as {@code GL_TRIANGLE_STRIP}
	 * bands, one per outline contour.
	 *
	 * @param openGL the {@link OpenGL} helper to draw into
	 */
	public void drawSideFallback(final OpenGL openGL) {
		int i = -1;
		while (i < currentIndex) {
			final int begin = indices[++i];
			final int end = indices[++i];
			openGL.beginDrawing(GL_TRIANGLE_STRIP);
			for (int index = begin; index < end; index += 3) {
				openGL.outputNormal(sideNormalBuffer.get(index), sideNormalBuffer.get(index + 1),
						sideNormalBuffer.get(index + 2));
				openGL.outputVertex(sideQuadsBuffer.get(index), sideQuadsBuffer.get(index + 1),
						sideQuadsBuffer.get(index + 2));
			}
			openGL.endDrawing();
		}
	}

	/**
	 * Draws the outline of each glyph contour from {@link #sideQuadsBuffer} as {@code GL_LINE_LOOP}
	 * primitives, one per contour.
	 */
	public void drawBorderFallback() {
		final int stride = depth == 0 ? 3 : 6;
		int i = -1;
		while (i < currentIndex) {
			final int begin = indices[++i];
			final int end = indices[++i];
			gl.beginDrawing(GL_LINE_LOOP);
			for (int index = begin; index < end; index += stride) {
				gl.outputVertex(sideQuadsBuffer.get(index), sideQuadsBuffer.get(index + 1),
						sideQuadsBuffer.get(index + 2));
			}
			gl.endDrawing();
		}
	}

	// -------------------------------------------------------------------------
	// Lifecycle
	// -------------------------------------------------------------------------

	@Override
	public void dispose() {
		if (regionRenderer != null) {
			try {
				regionRenderer.destroy(gl.getGL().getGL2ES2());
			} catch (final Exception e) {
				DEBUG.ERR("TextDrawer.dispose: " + e.getMessage());
			}
			regionRenderer = null;
			textUtil = null;
		}
		fontCache.clear();
		faceVertexBuffer = faceTextureBuffer = sideNormalBuffer = sideQuadsBuffer = null;
	}
}
