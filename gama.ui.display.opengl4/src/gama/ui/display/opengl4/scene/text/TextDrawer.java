/*******************************************************************************************************
 *
 * TextDrawer.java, in gama.ui.display.opengl4, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
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
import static com.jogamp.opengl.GL.GL_TRIANGLE_STRIP;
import static java.awt.geom.PathIterator.SEG_CLOSE;
import static java.awt.geom.PathIterator.SEG_LINETO;
import static java.awt.geom.PathIterator.SEG_MOVETO;
import static com.jogamp.opengl.glu.GLU.*;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.DoubleBuffer;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jogamp.graph.curve.Region;
import com.jogamp.graph.curve.opengl.RegionRenderer;
import com.jogamp.graph.curve.opengl.TextRegionUtil;
import com.jogamp.graph.font.FontFactory;
import com.jogamp.math.Recti;
import com.jogamp.math.Vec3f;
import com.jogamp.math.Vec4f;
import com.jogamp.math.util.PMVMatrix4f;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUtessellator;

import gama.api.types.color.IColor;
import gama.api.types.font.IFont;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.ui.layers.IDrawingAttributes;
import gama.api.utils.geometry.AxisAngle;
import gama.api.utils.geometry.GamaCoordinateSequenceFactory;
import gama.api.utils.geometry.ICoordinates;
import gama.dev.DEBUG;
import gama.ui.display.opengl4.OpenGL;
import gama.ui.display.opengl.ITesselator;
import gama.ui.display.opengl.scene.ObjectDrawer;
import gama.ui.display.opengl.scene.text.StringObject;

/**
 * The Class TextDrawer.
 *
 * <p>
 * Renders text strings in the OpenGL 4.1 core-profile renderer using the <b>JOGL graph/curve library</b>
 * ({@code com.jogamp.graph.curve.opengl}). This replaces the old GLUT bitmap-string path (removed in GL4 core) and
 * eliminates per-frame AWT tessellation by caching glyph outlines on the GPU.
 * </p>
 *
 * <h3>Architecture</h3>
 * <ul>
 * <li>A single {@link RegionRenderer} + {@link RenderState} is kept per {@code TextDrawer} instance (i.e. per OpenGL
 * context). The {@code RegionRenderer} manages a lazily created shader program that renders GPU-tessellated cubic
 * Bézier curves with sub-pixel accuracy.</li>
 * <li>A {@link GLRegion} per unique (font, text) pair is <em>not</em> cached at this level; instead
 * {@link TextRegionUtil#drawString3D} is used in pass-through mode which manages its own internal glyph cache.</li>
 * <li>For <b>non-perspective</b> (overlay / screen-space) text the renderer switches the projection to an orthographic
 * screen-space matrix before issuing the draw call, then restores it.</li>
 * <li>Extruded, bordered, textured and wireframe perspective text use one AWT outline for both caps and sides,
 * tessellated by GLU on the CPU and rendered through the core-profile VBO path.</li>
 * </ul>
 *
 * <h3>Thread safety</h3> All methods must be called from the GL thread.
 */
public class TextDrawer extends ObjectDrawer<StringObject, OpenGL> implements ITesselator {

	// -------------------------------------------------------------------------
	// JOGL graph / curve API state
	// -------------------------------------------------------------------------

	/**
	 * Draw curves directly into the scene's multisampled framebuffer. JOGL's two-pass mode composites
	 * into the context's default framebuffer, not necessarily the display's active render target.
	 */
	private static final int RENDER_HINTS = Region.NORM_RENDERING_BIT;

	/**
	 * The JOGL graph region renderer. Created lazily on first use so that it is initialised on the correct GL thread.
	 * One instance is shared for all text draw calls from this {@code TextDrawer}.
	 */
	private RegionRenderer regionRenderer;

	private int diagnosticDraws;

	public static void traceState(final GL4 gl4, final String stage) {
		final int[] state = new int[10];
		gl4.glGetIntegerv(GL.GL_VIEWPORT, state, 0);
		gl4.glGetIntegerv(GL.GL_DRAW_FRAMEBUFFER_BINDING, state, 4);
		gl4.glGetIntegerv(GL.GL_READ_FRAMEBUFFER_BINDING, state, 5);
		gl4.glGetIntegerv(GL4.GL_VERTEX_ARRAY_BINDING, state, 6);
		gl4.glGetIntegerv(GL2ES2.GL_CURRENT_PROGRAM, state, 7);
		gl4.glGetIntegerv(GL.GL_SAMPLES, state, 8);
		gl4.glGetIntegerv(GL.GL_DEPTH_FUNC, state, 9);
		DEBUG.ERR("[GL4-TEXT single-pass-20261104] context=" + gl4.getContext().hashCode() + " " + stage
				+ " viewport/fboDraw/fboRead/vao/program/samples/depthFunc=" + Arrays.toString(state)
				+ " framebufferStatus=0x" + Integer.toHexString(gl4.glCheckFramebufferStatus(GL.GL_DRAW_FRAMEBUFFER))
				+ " depth=" + gl4.glIsEnabled(GL.GL_DEPTH_TEST) + " blend=" + gl4.glIsEnabled(GL.GL_BLEND)
				+ " cull=" + gl4.glIsEnabled(GL.GL_CULL_FACE) + " scissor=" + gl4.glIsEnabled(GL.GL_SCISSOR_TEST));
		// Reading errors consumes them; keep this temporary diagnostic bounded.
		for (int i = 0; i < 8; i++) {
			final int error = gl4.glGetError();
			if (error == GL.GL_NO_ERROR) break;
			DEBUG.ERR("[GL4-TEXT] " + stage + " GL error=0x" + Integer.toHexString(error));
		}
	}

	/**
	 * High-level utility wrapper around the region renderer that provides the {@link TextRegionUtil#drawString3D}
	 * convenience method.
	 */
	private TextRegionUtil textUtil;

	private int textVertexArray;

	/**
	 * Cache of JOGL fonts, including the screen/world fallback family in the key.
	 */
	private record FontKey(Font font, boolean overlay) {}

	private final Map<FontKey, com.jogamp.graph.font.Font> fontCache = new HashMap<>();

	// -------------------------------------------------------------------------
	// Contour geometry state
	// -------------------------------------------------------------------------

	/** Temporary coordinate sequence used to compute side-face normals. */
	ICoordinates temp = GamaCoordinateSequenceFactory.ofLength(4);

	/** Current face normal, reused across vertex additions. */
	IPoint normal = GamaPointFactory.create();

	/** X coordinate of the previous contour vertex, used to compute extrusion normals. */
	double previousX = Double.NaN;

	/** Y coordinate of the previous contour vertex. */
	double previousY = Double.NaN;

	private int contourStartNormalPos = -1;

	private final GLUtessellator tessellator = gluNewTess();

	/** Current contour-index pointer into {@link #indices}. */
	int currentIndex = -1;

	// -------------------------------------------------------------------------
	// Constants
	// -------------------------------------------------------------------------

	/**
	 * Initial capacity (in {@code double} values) for the per-call geometry buffers. 1 M doubles = 8 MB per buffer; the
	 * buffer is cleared (not reallocated) on every draw call.
	 */
	private static final int BUFFER_SIZE = 1_000_000;

	/** AffineTransform that flips the Y axis so glyph outlines match OpenGL's Y-up convention. */
	private static final AffineTransform AT = AffineTransform.getScaleInstance(1.0, -1.0);

	/** AWT font render context for outline text. */
	private static final FontRenderContext FONT_CTX = new FontRenderContext(new AffineTransform(), true, true);

	// -------------------------------------------------------------------------
	// Per-call geometry buffers (3-D extrusion only)
	// -------------------------------------------------------------------------

	/**
	 * Vertex buffer for the tessellated front/back face triangles of extruded text. Layout:
	 * {@code [x, y, z, x, y, z, …]}.
	 */
	DoubleBuffer faceVertexBuffer = newDirectDoubleBuffer(BUFFER_SIZE);

	/**
	 * Texture-coordinate buffer parallel to {@link #faceVertexBuffer}. Layout: {@code [u, v, u, v, …]}.
	 */
	DoubleBuffer faceTextureBuffer = newDirectDoubleBuffer(BUFFER_SIZE * 2 / 3);

	/**
	 * Index array that records the start position in {@link #sideQuadsBuffer} of each contour's first vertex, and the
	 * end position after its last vertex. Entries are in pairs: {@code [begin0, end0, begin1, end1, …]}.
	 */
	int[] indices = new int[1000];

	/**
	 * Normal buffer for the lateral (side) quad-strip faces of extruded text. Layout: {@code [nx,ny,nz, …]}. Two normal
	 * values are written per quad-strip vertex pair (top and bottom share the same normal).
	 */
	private DoubleBuffer sideNormalBuffer = newDirectDoubleBuffer(BUFFER_SIZE);

	/**
	 * Vertex buffer for the lateral (side) quad-strip faces of extruded text. If {@code depth == 0}: layout
	 * {@code [x, y, 0, …]} (one z-level). If {@code depth > 0}: layout {@code [x, y, 0, x, y, depth, …]} (alternating
	 * z=0 and z=depth rows).
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
	 * @param gl
	 *            the {@link OpenGL} helper that owns this drawer
	 */
	public TextDrawer(final OpenGL gl) {
		super(gl);
		gluTessCallback(tessellator, GLU_TESS_VERTEX, this);
		gluTessCallback(tessellator, GLU_TESS_COMBINE, this);
		gluTessCallback(tessellator, GLU_TESS_ERROR, this);
		// An edge-flag callback requests independent triangles, not strips or fans.
		gluTessCallback(tessellator, GLU_TESS_EDGE_FLAG, this);
	}

	// -------------------------------------------------------------------------
	// Graph / curve renderer initialisation
	// -------------------------------------------------------------------------

	/**
	 * Lazily initialises the JOGL graph region renderer the first time text needs to be drawn. Must be called from the
	 * GL thread.
	 *
	 * @param gl4
	 *            the current GL4 context
	 * @return the initialised {@link RegionRenderer}, or {@code null} if initialisation failed
	 */
	private RegionRenderer getRegionRenderer(final com.jogamp.opengl.GL4 gl4) {
		if (regionRenderer != null) return regionRenderer;
		try {
			final GL2ES2 gl2es2 = gl4.getGL2ES2();
			// JOGL 2.6 API: RegionRenderer.create(PMVMatrix4f, GLCallback, GLCallback)
			regionRenderer = RegionRenderer.create(new PMVMatrix4f(), RegionRenderer.defaultBlendEnable,
					RegionRenderer.defaultBlendDisable);
			regionRenderer.init(gl2es2);
			textUtil = new TextRegionUtil(RENDER_HINTS);
		} catch (final Exception e) {
			DEBUG.ERR("TextDrawer: failed to initialise RegionRenderer", e);
			regionRenderer = null;
		}
		return regionRenderer;
	}

	/**
	 * Utility class for locating system font files without relying on {@code sun.font} internal APIs.
	 *
	 * <p>
	 * {@code FontLoader} walks the platform-specific system font directories to find TTF/OTF files that match a
	 * requested font family and style. The search is purely file-system based: candidate files are matched by
	 * comparing the lower-cased file name against the family name (spaces stripped) and the expected bold/italic
	 * style suffix.
	 * </p>
	 *
	 * <h3>Style suffix conventions</h3>
	 * <p>
	 * Font files commonly embed their style in the file name using one of several conventions, e.g.:
	 * {@code Arial-Bold.ttf}, {@code ArialBold.ttf}, {@code Arial_Bold.ttf}. The search tries the most common
	 * suffixes in order and falls back to the plain family name file when no styled variant is found.
	 * </p>
	 *
	 * <h3>Platform font directories</h3>
	 * <ul>
	 * <li><b>Windows:</b> {@code %WINDIR%\Fonts}</li>
	 * <li><b>macOS:</b> {@code /Library/Fonts}, {@code /System/Library/Fonts}, {@code ~/Library/Fonts}</li>
	 * <li><b>Linux:</b> {@code /usr/share/fonts}, {@code /usr/local/share/fonts}, {@code ~/.local/share/fonts},
	 * {@code ~/.fonts}</li>
	 * </ul>
	 */
	public static class FontLoader {

		/**
		 * Locates a TrueType font file by exact file name and returns it as an {@link InputStream}.
		 *
		 * <p>
		 * The method walks all platform font directories recursively until a file whose name matches
		 * {@code fontFileName} (case-insensitively) is found.
		 * </p>
		 *
		 * @param fontFileName
		 *            the exact file name to locate, e.g. {@code "Arial.ttf"} or {@code "Ubuntu-R.ttf"}
		 * @return an open {@link InputStream} for the located file
		 * @throws FileNotFoundException
		 *             if no file with the given name can be found in any system font directory
		 */
		public static InputStream loadSystemFont(final String fontFileName) throws FileNotFoundException {
			for (final String path : getSystemFontPaths()) {
				final File fontFile = findFileRecursive(new File(path), fontFileName);
				if (fontFile != null && fontFile.exists()) return new FileInputStream(fontFile);
			}
			throw new FileNotFoundException("Could not find font: " + fontFileName);
		}

		/**
		 * Searches the system font directories for a font file that matches the given family name and bold/italic style
		 * flags, then loads and returns the corresponding JOGL {@link com.jogamp.graph.font.Font}.
		 *
		 * <p>
		 * The search strategy is:
		 * </p>
		 * <ol>
		 * <li>Build a list of candidate file-name stems that encode the requested style, e.g. for a bold-italic Arial
		 * request the list would include {@code "arial-bolditalic"}, {@code "arial-bold-italic"},
		 * {@code "arialbolditalic"}, etc. If no styled variant is found the plain family name is tried as a last
		 * resort.</li>
		 * <li>Walk the system font directories recursively. For every {@code .ttf} / {@code .otf} file encountered,
		 * compare its lower-cased base name (without extension) against the candidate list. The first match wins.</li>
		 * <li>Stream the winning file into {@link FontFactory#get(InputStream, boolean)} and return the result.</li>
		 * </ol>
		 *
		 * @param family
		 *            the font family name as declared in GAMA, e.g. {@code "Arial"} or {@code "Times New Roman"}
		 * @param bold
		 *            {@code true} if the bold variant is required
		 * @param italic
		 *            {@code true} if the italic variant is required
		 * @return the loaded JOGL font, or {@code null} if no matching file can be found or loaded
		 */
		public static com.jogamp.graph.font.Font loadSystemFontByFamily(final String family, final boolean bold,
				final boolean italic) {
			// Build candidate stems ordered by preference (most-specific first)
			final List<String> candidates = buildCandidateStems(family, bold, italic);
			for (final String path : getSystemFontPaths()) {
				final File fontFile = findFontFileByCandidates(new File(path), candidates);
				if (fontFile != null) {
					try (final InputStream is = new FileInputStream(fontFile)) {
						return FontFactory.get(is, false);
					} catch (final Exception e) {
						DEBUG.ERR("FontLoader: failed to load '" + fontFile.getPath() + "': " + e.getMessage());
					}
				}
			}
			return null;
		}

		/**
		 * Builds an ordered list of lower-cased file-name stems (without extension) that represent common naming
		 * conventions for the given family + style combination.
		 *
		 * <p>
		 * For example, a bold-italic "Arial" request produces stems such as {@code "arial-bolditalic"},
		 * {@code "arial-bold-italic"}, {@code "arialbolditalic"}, {@code "arial_bolditalic"} and also the plain
		 * {@code "arial"} as a last-resort fallback.
		 * </p>
		 *
		 * @param family
		 *            the font family name (may contain spaces)
		 * @param bold
		 *            whether to include bold markers
		 * @param italic
		 *            whether to include italic markers
		 * @return ordered list of candidate stems, never {@code null}
		 */
		private static List<String> buildCandidateStems(final String family, final boolean bold,
				final boolean italic) {
			final String base = family.toLowerCase(java.util.Locale.ROOT);
			final String baseNoSpace = base.replace(" ", "");
			final List<String> stems = new ArrayList<>();

			if (bold || italic) {
				final String suffix;
				if (bold && italic) {
					suffix = "bolditalic";
				} else if (bold) {
					suffix = "bold";
				} else {
					suffix = "italic";
				}
				// Common separator conventions: hyphen, none, underscore, space
				for (final String sep : new String[] { "-", "", "_", " " }) {
					stems.add(baseNoSpace + sep + suffix);
					if (!base.equals(baseNoSpace)) { stems.add(base.replace(" ", sep) + sep + suffix); }
				}
				// bold-italic split variant: Family-Bold-Italic
				if (bold && italic) {
					for (final String sep : new String[] { "-", "_" }) {
						stems.add(baseNoSpace + sep + "bold" + sep + "italic");
					}
				}
			}
			// Plain family as fallback
			stems.add(baseNoSpace);
			if (!base.equals(baseNoSpace)) { stems.add(base); }
			return stems;
		}

		/**
		 * Walks {@code directory} recursively looking for a {@code .ttf} or {@code .otf} file whose lower-cased base
		 * name (without extension) appears in {@code candidates}.
		 *
		 * @param directory
		 *            the root directory to search
		 * @param candidates
		 *            ordered list of lower-cased stem names to match against
		 * @return the first matching {@link File}, or {@code null} if none found
		 */
		private static File findFontFileByCandidates(final File directory, final List<String> candidates) {
			if (!directory.isDirectory() || !directory.canRead()) return null;
			final File[] files = directory.listFiles();
			if (files == null) return null;
			// Check files first, recurse into sub-directories after (breadth-first-like per directory)
			File subResult = null;
			for (final File file : files) {
				if (file.isDirectory()) {
					if (subResult == null) { subResult = findFontFileByCandidates(file, candidates); }
				} else {
					final String name = file.getName().toLowerCase(java.util.Locale.ROOT);
					if (!name.endsWith(".ttf") && !name.endsWith(".otf")) { continue; }
					final String stem = name.substring(0, name.lastIndexOf('.'));
					if (candidates.contains(stem)) return file;
				}
			}
			return subResult;
		}

		/**
		 * Returns the platform-specific root font directories for the current OS.
		 *
		 * @return a mutable list of absolute directory paths; never {@code null}
		 */
		private static List<String> getSystemFontPaths() {
			final List<String> paths = new ArrayList<>();
			final String os = System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT);
			if (os.contains("win")) {
				paths.add(System.getenv("WINDIR") + "\\Fonts");
			} else if (os.contains("mac")) {
				paths.add("/Library/Fonts");
				paths.add("/System/Library/Fonts");
				paths.add(System.getProperty("user.home") + "/Library/Fonts");
			} else if (os.contains("nix") || os.contains("nux")) {
				paths.add("/usr/share/fonts");
				paths.add("/usr/local/share/fonts");
				paths.add(System.getProperty("user.home") + "/.local/share/fonts");
				paths.add(System.getProperty("user.home") + "/.fonts");
			}
			return paths;
		}

		/**
		 * Finds a file with the given exact name (case-insensitive) by recursively walking {@code directory}.
		 *
		 * @param directory
		 *            the directory to search
		 * @param fileName
		 *            the exact file name to locate (compared case-insensitively)
		 * @return the located {@link File}, or {@code null} if not found
		 */
		private static File findFileRecursive(final File directory, final String fileName) {
			if (!directory.isDirectory() || !directory.canRead()) return null;
			final File[] files = directory.listFiles();
			if (files == null) return null;
			for (final File file : files) {
				if (file.isDirectory()) {
					final File found = findFileRecursive(file, fileName);
					if (found != null) return found;
				} else if (file.getName().equalsIgnoreCase(fileName)) return file;
			}
			return null;
		}
	}

	// -------------------------------------------------------------------------
	// Font cache / loading
	// -------------------------------------------------------------------------

	/**
	 * Looks up (or creates) the JOGL {@link com.jogamp.graph.font.Font} corresponding to an AWT {@link Font}.
	 *
	 * <p>
	 * Font resolution is attempted in three stages:
	 * </p>
	 * <ol>
	 * <li><b>System font search</b> — searches OS font directories for a file whose name encodes the requested family
	 * and bold/italic style without using any {@code sun.font} internal APIs.</li>
	 * <li><b>Default font</b> — if the requested family name is {@code "default"}, {@code "jetbrainsmononl"},
	 * {@code "poppins"} (case-insensitive), or if the system search fails, loads a bundled TTF from the plugin's
	 * {@code fonts/} folder. For overlay (screen-space) text the default is <em>JetBrainsMonoNL</em>; for world-space
	 * text the default is <em>Poppins</em>.</li>
	 * <li><b>Bundled hard fallback</b> — if even the bundle load fails, returns {@code null} (caller will omit
	 * drawing).</li>
	 * </ol>
	 *
	 * <p>
	 * Bold and italic variants are resolved by choosing the matching {@code *-Bold.ttf}, {@code *-Italic.ttf} or
	 * {@code *-BoldItalic.ttf} file, so the JOGL glyph shapes accurately reflect the requested weight/slant.
	 * </p>
	 *
	 * @param awtFont
	 *            the AWT font whose JOGL equivalent is needed
	 * @param overlay
	 *            {@code true} when rendering in screen/overlay space (selects JetBrainsMonoNL as the default family);
	 *            {@code false} for world-space text (selects Poppins as the default family)
	 * @return the JOGL font, or {@code null} if no font could be loaded
	 */
	private com.jogamp.graph.font.Font getJoglFont(final Font awtFont, final boolean overlay) {
		return fontCache.computeIfAbsent(new FontKey(awtFont, overlay), key -> loadJoglFont(key.font(), key.overlay()));
	}

	/**
	 * Performs the actual JOGL font loading for {@link #getJoglFont(Font, boolean)}. The loading follows three stages:
	 *
	 * <ol>
	 * <li>Unless the family name signals a "default" font, attempt to locate a matching TTF/OTF file in the OS font
	 * directories via {@link FontLoader}, honouring the bold and italic style flags.</li>
	 * <li>Load the appropriate bundled default font ({@code JetBrainsMonoNL} for overlay, {@code Poppins} otherwise)
	 * from the plugin's {@code fonts/} resource folder, again picking the bold/italic variant file as needed.</li>
	 * <li>Return {@code null} if all attempts fail.</li>
	 * </ol>
	 *
	 * @param awtFont
	 *            the AWT font to convert
	 * @param overlay
	 *            {@code true} for screen-space (overlay) rendering; {@code false} for world-space
	 * @return the JOGL font, or {@code null} if loading failed
	 */
	private static com.jogamp.graph.font.Font loadJoglFont(final Font awtFont, final boolean overlay) {
		final String family = awtFont.getFamily().trim();
		final boolean bold = awtFont.isBold();
		final boolean italic = awtFont.isItalic();

		// --- Stage 1: search system fonts by family name (no sun.font internals) ---
		if (!isDefaultFamilyName(family)) {
			final com.jogamp.graph.font.Font joglFont = FontLoader.loadSystemFontByFamily(family, bold, italic);
			if (joglFont != null) return joglFont;
		}

		// --- Stage 2: bundled default fonts ---
		return loadBundledDefault(overlay, bold, italic);
	}

	/**
	 * Returns {@code true} when {@code family} indicates that the caller wants the built-in default font rather than a
	 * specific system typeface. Recognized values (case-insensitive): {@code "default"}, {@code "jetbrainsmononl"},
	 * {@code "poppins"}.
	 *
	 * @param family
	 *            the font family name to test
	 * @return {@code true} if the name maps to a bundled default
	 */
	private static boolean isDefaultFamilyName(final String family) {
		final String lower = family.toLowerCase(java.util.Locale.ROOT);
		return lower.equals("default") || lower.startsWith("jetbrainsmononl") || lower.startsWith("poppins");
	}

	/**
	 * Loads the appropriate bundled default font from the plugin's {@code fonts/} resource folder.
	 *
	 * <p>
	 * For overlay (screen-space) rendering the base family is <em>JetBrainsMonoNL</em>; for world-space rendering it
	 * is <em>Poppins</em>. The correct variant file ({@code Regular}, {@code Bold}, {@code Italic}, or
	 * {@code BoldItalic}) is selected from the bold/italic flags.
	 * </p>
	 *
	 * @param overlay
	 *            {@code true} selects JetBrainsMonoNL; {@code false} selects Poppins
	 * @param bold
	 *            whether the bold variant should be loaded
	 * @param italic
	 *            whether the italic variant should be loaded
	 * @return the loaded JOGL font, or {@code null} if the resource cannot be found
	 */
	private static com.jogamp.graph.font.Font loadBundledDefault(final boolean overlay, final boolean bold,
			final boolean italic) {
		final String base = overlay ? "JetBrainsMonoNL" : "Poppins";
		final String variant = bold && italic ? "BoldItalic" : bold ? "Bold" : italic ? "Italic" : "Regular";
		final String resourcePath = "/fonts/" + base + "-" + variant + ".ttf";
		try (final java.io.InputStream is = TextDrawer.class.getResourceAsStream(resourcePath)) {
			if (is == null) {
				DEBUG.ERR("TextDrawer: bundled font resource not found: " + resourcePath);
				return null;
			}
			return FontFactory.get(is, false);
		} catch (final Exception e) {
			DEBUG.ERR("TextDrawer: failed to load bundled font '" + resourcePath + "': " + e.getMessage());
			return null;
		}
	}

	// -------------------------------------------------------------------------
	// Main draw entry point
	// -------------------------------------------------------------------------

	@Override
	protected void _draw(final StringObject s) {
		final IDrawingAttributes attributes = s.getAttributes();
		if (diagnosticDraws < 12) {
			DEBUG.ERR("[GL4-TEXT] dispatch perspective=" + attributes.isPerspective() + " depth="
					+ attributes.getDepth() + " empty=" + attributes.isEmpty() + " border=" + attributes.getBorder()
					+ " textured=" + gl.isTextured());
		}
		if (!attributes.isPerspective()) {
			drawWithGraphLibrary(s.getObject(), attributes, true);
		} else if (attributes.getDepth() > 0 || attributes.isEmpty() || attributes.getBorder() != null
				|| gl.isTextured()) {
			final boolean diagnose = diagnosticDraws++ < 12;
			if (diagnose) { traceState(gl.getGL(), "before outline"); }
			try {
				drawOutline(s.getObject(), attributes);
			} finally {
				if (diagnose) { traceState(gl.getGL(), "after outline"); }
			}
		} else {
			drawWithGraphLibrary(s.getObject(), attributes, false);
		}
	}

	// -------------------------------------------------------------------------
	// Graph-library flat text renderer (perspective and non-perspective)
	// -------------------------------------------------------------------------

	/**
	 * Public entry point for drawing a flat string via the graph/curve library, usable from outside (e.g.
	 * {@link gama.ui.display.opengl4.OpenGL#drawScreenText}). Delegates directly to
	 * {@link #drawWithGraphLibrary(String, IDrawingAttributes, boolean)}.
	 *
	 * @param text
	 *            the string to render
	 * @param attributes
	 *            the drawing attributes
	 * @param overlay
	 *            {@code true} for screen-space (overlay) rendering
	 */
	public void drawWithGraphLibraryPublic(final String text, final IDrawingAttributes attributes,
			final boolean overlay) {
		drawWithGraphLibrary(text, attributes, overlay, overlay);
	}

	/**
	 * Draws a flat (non-extruded) string using the JOGL graph/curve library. Works for both perspective and
	 * non-perspective (overlay) text because the graph renderer uses its own shader and matrix uniforms that bypass the
	 * fixed-function pipeline entirely.
	 *
	 * <p>
	 * For non-perspective text the projection is temporarily replaced by a pixel-aligned orthographic matrix so the
	 * text is drawn in screen space at a stable pixel size. For perspective text the current model-view and projection
	 * matrices from the {@link OpenGL} helper are passed through to the graph shader.
	 * </p>
	 *
	 * @param text
	 *            the string to render
	 * @param attributes
	 *            the drawing attributes (font, color, location, anchor, rotation…)
	 * @param overlay
	 *            {@code true} to render in screen / overlay space; {@code false} for world space
	 */
	private void drawWithGraphLibrary(final String text, final IDrawingAttributes attributes, final boolean overlay) {
		drawWithGraphLibrary(text, attributes, overlay, false);
	}

	private void drawWithGraphLibrary(final String text, final IDrawingAttributes attributes, final boolean overlay,
			final boolean viewportPosition) {
		try {
			drawGraphText(text, attributes, overlay, viewportPosition);
		} catch (final RuntimeException | LinkageError e) {
			if (diagnosticDraws <= 12) { DEBUG.ERR("[GL4-TEXT] text preparation/draw failed", e); }
			throw e;
		}
	}

	private void drawGraphText(final String text, final IDrawingAttributes attributes, final boolean overlay,
			final boolean viewportPosition) {
		final com.jogamp.opengl.GL4 gl4 = gl.getGL();
		final boolean diagnose = diagnosticDraws++ < 12;
		if (diagnose) {
			traceState(gl4, "graph entry");
			DEBUG.ERR("[GL4-TEXT] textLength=" + text.length() + " billboard=" + overlay
					+ " viewportPosition=" + viewportPosition + " location=" + attributes.getLocation()
					+ " anchor=" + attributes.getAnchor() + " font=" + attributes.getFont()
					+ " view=" + gl.getViewWidth() + "x" + gl.getViewHeight()
					+ " pixel/model ratio=" + gl.getRenderer().getAbsoluteRatioBetweenPixelsAndModelsUnits());
		}
		if (diagnose) { DEBUG.ERR("[GL4-TEXT] preparing region renderer"); }
		final RegionRenderer rr = getRegionRenderer(gl4);
		if (diagnose) { DEBUG.ERR("[GL4-TEXT] region renderer ready=" + (rr != null)); }
		if (rr == null) {
			if (diagnose) { DEBUG.ERR("[GL4-TEXT] graph skipped: region renderer unavailable"); }
			if (!overlay) { drawOutline(text, attributes); }
			return;
		}
		final IFont gamaFont = attributes.getFont();
		if (gamaFont == null) {
			if (diagnose) { DEBUG.ERR("[GL4-TEXT] graph skipped: null GAMA font"); }
			return;
		}
		final Font awtFont = gamaFont.getAwtFont();
		if (diagnose) { DEBUG.ERR("[GL4-TEXT] resolving JOGL font for " + awtFont); }
		final com.jogamp.graph.font.Font joglFont = getJoglFont(awtFont, overlay);
		if (diagnose) { DEBUG.ERR("[GL4-TEXT] JOGL font resolved=" + (joglFont != null)); }
		if (joglFont == null) {
			if (diagnose) { DEBUG.ERR("[GL4-TEXT] graph skipped: JOGL font unavailable for " + awtFont); }
			if (!overlay) { drawOutline(text, attributes); }
			return;
		}

		final IColor c = gl.getCurrentColor();
		if (diagnose) { DEBUG.ERR("[GL4-TEXT] preparing color=" + c); }
		final Vec4f color = new Vec4f((float) (c.red() / 255.0), (float) (c.green() / 255.0),
				(float) (c.blue() / 255.0), (float) (c.alpha() / 255.0 * gl.getCurrentObjectAlpha()));

		final GL2ES2 gl2es2 = gl4.getGL2ES2();
		final IPoint p = attributes.getLocation();
		final IPoint anchor = attributes.getAnchor();

		final float scale = overlay ? 1f
				: 1f / (float) gl.getRenderer().getAbsoluteRatioBetweenPixelsAndModelsUnits();
		final float pixelSize = awtFont.getSize2D() * scale;

		final int[] viewport = gl.getViewport();
		if (diagnose) {
			DEBUG.ERR("[GL4-TEXT] preparing matrices: cached viewport=" + Arrays.toString(viewport));
		}
		rr.reshapeNotify(viewport[0], viewport[1], viewport[2], viewport[3]);
		final PMVMatrix4f pmv = rr.getMatrix();
		final float[] projection = new float[16];
		final float[] modelView = new float[16];
		gl.getProjectionMatrix().get(projection);
		gl.getModelViewMatrix().get(modelView);
		pmv.loadP(projection, 0);
		pmv.loadMv(modelView, 0);
		if (overlay) {
			final Vec3f position = new Vec3f((float) p.getX(), (float) p.getY(),
					(float) (p.getZ() + gl.getCurrentZTranslation()));
			if (!viewportPosition) {
				// Billboard text still needs the containing layer and camera to locate its anchor.
				if (!pmv.mapObjToWin(position, new Recti(0, 0, (int) gl.getViewWidth(),
						(int) gl.getViewHeight()), position)) {
					if (diagnose) { DEBUG.ERR("[GL4-TEXT] graph skipped: anchor projection failed"); }
					return;
				}
			} else {
				position.set((float) p.getX(), (float) p.getY(), 0);
			}
			pmv.loadPIdentity();
			pmv.loadMvIdentity();
			pmv.orthoP(0, gl.getViewWidth(), 0, gl.getViewHeight(), -1, 1);
			pmv.translateMv(position.x(), position.y(), 0);
		} else {
			pmv.translateMv((float) p.getX(), (float) p.getY(),
					(float) (p.getZ() + gl.getCurrentZTranslation()));
		}
		final AxisAngle rotation = attributes.getRotation();
		if (rotation != null) {
			final IPoint axis = rotation.getAxis();
			pmv.rotateMv((float) Math.toRadians(-rotation.getAngle()), (float) axis.getX(),
					(float) axis.getY(), (float) axis.getZ());
		}
		pmv.scaleMv(pixelSize, pixelSize, pixelSize);
		final var bounds = joglFont.getGlyphBounds(text);
		if (diagnose) { DEBUG.ERR("[GL4-TEXT] glyph bounds ready; applying anchor=" + anchor); }
		pmv.translateMv(-bounds.getMinX() - bounds.getWidth() * (float) anchor.getX(),
				-bounds.getMinY() - bounds.getHeight() * (float) anchor.getY(), 0);
		if (diagnose) {
			DEBUG.ERR("[GL4-TEXT] awtFont=" + awtFont + " joglFont=" + joglFont + " pixelSize=" + pixelSize
					+ " color=" + color + " bounds=" + bounds + " zTranslation=" + gl.getCurrentZTranslation()
					+ " projection=" + Arrays.toString(projection) + " modelView=" + Arrays.toString(modelView)
					+ " graphMatrices=" + pmv);
			traceState(gl4, "before graph enable");
		}
		final int[] state = new int[10];
		final int[] previousViewport = new int[4];
		gl4.glGetIntegerv(GL4.GL_VERTEX_ARRAY_BINDING, state, 0);
		gl4.glGetIntegerv(GL4.GL_DRAW_FRAMEBUFFER_BINDING, state, 1);
		gl4.glGetIntegerv(GL4.GL_READ_FRAMEBUFFER_BINDING, state, 2);
		gl4.glGetIntegerv(GL.GL_ACTIVE_TEXTURE, state, 3);
		gl4.glGetIntegerv(GL.GL_BLEND_SRC_RGB, state, 4);
		gl4.glGetIntegerv(GL.GL_BLEND_DST_RGB, state, 5);
		gl4.glGetIntegerv(GL.GL_BLEND_SRC_ALPHA, state, 6);
		gl4.glGetIntegerv(GL.GL_BLEND_DST_ALPHA, state, 7);
		gl4.glGetIntegerv(GL2ES2.GL_CURRENT_PROGRAM, state, 8);
		gl4.glGetIntegerv(GL.GL_VIEWPORT, previousViewport, 0);
		final boolean blendEnabled = gl4.glIsEnabled(GL.GL_BLEND);
		final byte[] depthWrite = new byte[1];
		gl4.glGetBooleanv(GL.GL_DEPTH_WRITEMASK, depthWrite, 0);
		gl4.glActiveTexture(GL.GL_TEXTURE0);
		gl4.glGetIntegerv(GL.GL_TEXTURE_BINDING_2D, state, 9);
		if (textVertexArray == 0) {
			final int[] vao = new int[1];
			gl4.glGenVertexArrays(1, vao, 0);
			textVertexArray = vao[0];
		}
		try {
			// JOGL's graph VBOs need a caller-owned VAO in a core-profile context.
			gl4.glBindVertexArray(textVertexArray);
			rr.enable(gl2es2, true);
			try {
				if (diagnose) { traceState(gl4, "before glyph draw"); }
				textUtil.drawString3D(gl2es2, rr, joglFont, text, color);
				if (diagnose) { traceState(gl4, "after glyph draw"); }
			} finally {
				rr.enable(gl2es2, false);
			}
		} catch (final Exception e) {
			DEBUG.ERR("TextDrawer.drawWithGraphLibrary", e);
		} finally {
			gl4.glBindFramebuffer(GL.GL_DRAW_FRAMEBUFFER, state[1]);
			gl4.glBindFramebuffer(GL.GL_READ_FRAMEBUFFER, state[2]);
			gl4.glViewport(previousViewport[0], previousViewport[1], previousViewport[2], previousViewport[3]);
			gl4.glBindVertexArray(state[0]);
			gl4.glUseProgram(state[8]);
			gl4.glActiveTexture(GL.GL_TEXTURE0);
			gl4.glBindTexture(GL.GL_TEXTURE_2D, state[9]);
			gl4.glActiveTexture(state[3]);
			gl4.glBlendFuncSeparate(state[4], state[5], state[6], state[7]);
			if (blendEnabled) {
				gl4.glEnable(GL.GL_BLEND);
			} else {
				gl4.glDisable(GL.GL_BLEND);
			}
			gl4.glDepthMask(depthWrite[0] != 0);
			if (diagnose) { traceState(gl4, "after graph restore"); }
		}
	}

	// -------------------------------------------------------------------------
	// AWT outline path
	// -------------------------------------------------------------------------

	/**
	 * One outline supplies the caps, borders and extrusion sides, including font fallback and glyph bearings.
	 * Also used for flat perspective text when the graph library is unavailable.
	 *
	 * @param text
	 *            the string to draw
	 * @param attributes
	 *            the drawing attributes
	 */
	private void drawOutline(final String text, final IDrawingAttributes attributes) {
		final Font awtFont = attributes.getFont().getAwtFont();
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
		process(shape.getPathIterator(AT, attributes.getPrecision()), gl.isWireframe());
		drawOutlineText(attributes, bounds);
	}

	/**
	 * Applies positioning and draws an outline-tessellated string.
	 *
	 * @param attributes
	 *            the drawing attributes
	 * @param bounds
	 *            the AWT outline bounds before the Y-axis flip
	 */
	private void drawOutlineText(final IDrawingAttributes attributes, final Rectangle2D bounds) {
		final IPoint p = attributes.getLocation();
		IColor previous = null;
		gl.pushMatrix();
		try {
			final IPoint anchor = attributes.getAnchor();
			final boolean rotated = applyRotation(attributes, p);
			final double locX = rotated ? 0 : p.getX();
			final double locY = rotated ? 0 : p.getY();
			final double locZ = rotated ? 0 : p.getZ();
			final float scale = 1f / (float) gl.getRenderer().getAbsoluteRatioBetweenPixelsAndModelsUnits();
			final double x = -bounds.getX() - width * anchor.getX();
			final double y = bounds.getMaxY() - height * anchor.getY();
			gl.translateBy(locX + x * scale, locY + y * scale,
					locZ + gl.getCurrentZTranslation());
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

	/** @see #drawOutlineText */
	private IColor drawWireframe(IColor previous) {
		if (border != null) {
			previous = gl.getCurrentColor();
			gl.setCurrentColor(border);
		}
		if (depth == 0) {
			drawBorder();
		} else {
			drawSide();
		}
		return previous;
	}

	/** @see #drawOutlineText */
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
	// AWT outline tessellation and side geometry
	// -------------------------------------------------------------------------

	/**
	 * Processes a {@link PathIterator} of an AWT glyph outline.
	 *
	 * <p>
	 * GLU resolves the path's winding rule across all contours, including disconnected letters and glyph holes.
	 * Side geometry is collected from that same path.
	 * </p>
	 *
	 * @param pi
	 *            the glyph outline path iterator (pre-flattened: only SEG_MOVETO/SEG_LINETO/SEG_CLOSE)
	 */
	void process(final PathIterator pi, final boolean wireframe) {
		if (!wireframe) {
			gluTessProperty(tessellator, GLU_TESS_WINDING_RULE,
					pi.getWindingRule() == PathIterator.WIND_EVEN_ODD ? GLU_TESS_WINDING_ODD
							: GLU_TESS_WINDING_NONZERO);
			gluTessNormal(tessellator, 0, 0, -1);
			gluTessBeginPolygon(tessellator, null);
		}
		double x0 = 0, y0 = 0;
		while (!pi.isDone()) {
			final double[] coords = new double[6];
			switch (pi.currentSegment(coords)) {
				case SEG_MOVETO:
					if (!wireframe) {
						gluTessBeginContour(tessellator);
						gluTessVertex(tessellator, coords, 0, coords);
					}
					x0 = coords[0];
					y0 = coords[1];
					beginNewContour();
					addContourVertex0(x0, y0);
					break;
				case SEG_LINETO:
					if (!wireframe) { gluTessVertex(tessellator, coords, 0, coords); }
					addContourVertex0(coords[0], coords[1]);
					break;
				case SEG_CLOSE:
					if (!wireframe) { gluTessEndContour(tessellator); }
					addContourVertex0(x0, y0);
					endContour();
					break;
				default:
					break;
			}
			pi.next();
		}
		if (!wireframe) { gluTessEndPolygon(tessellator); }

		sideQuadsBuffer.flip();
		sideNormalBuffer.flip();
		faceVertexBuffer.flip();
		if (faceTextureBuffer != null) { faceTextureBuffer.flip(); }
	}

	@Override
	public void drawVertex(final int i, final double x, final double y, final double z) {
		faceTextureBuffer.put(width == 0 ? 0 : x / width).put(height == 0 ? 0 : y / height);
		faceVertexBuffer.put(x).put(y).put(z);
	}

	@Override
	public void combine(final double[] coords, final Object[] data, final float[] weight, final Object[] outData) {
		outData[0] = coords.clone();
	}

	@Override
	public void error(final int code) {
		throw new IllegalStateException("Cannot tessellate text: " + new GLU().gluErrorString(code));
	}

	private void addContourIndex() {
		if (++currentIndex == indices.length) { indices = Arrays.copyOf(indices, indices.length * 2); }
		indices[currentIndex] = sideQuadsBuffer.position();
	}

	/**
	 * Records the start position of a new outline contour in {@link #sideQuadsBuffer}.
	 */
	public void beginNewContour() {
		addContourIndex();
		previousX = previousY = Double.NaN;
		contourStartNormalPos = -1;
	}

	/**
	 * Records the end position of the current outline contour in {@link #sideQuadsBuffer}.
	 */
	public void endContour() {
		addContourIndex();
		if (depth > 0 && contourStartNormalPos >= 0) {
			final int lastNormalPos = sideNormalBuffer.position() - 6;
			for (int i = 0; i < 6; i++) {
				sideNormalBuffer.put(contourStartNormalPos + i, sideNormalBuffer.get(lastNormalPos + i));
			}
		}
	}

	/**
	 * Adds a vertex at z=0 to {@link #sideQuadsBuffer} and, if {@link #depth} > 0, also adds the same vertex at z=depth
	 * and computes the side-face normal for the segment from the previous vertex to this one.
	 *
	 * @param x
	 *            the X coordinate in font units
	 * @param y
	 *            the Y coordinate in font units
	 */
	public void addContourVertex0(final double x, final double y) {
		if (x == previousX && y == previousY) return;
		sideQuadsBuffer.put(x).put(y).put(0);
		if (depth > 0) {
			if (!Double.isNaN(previousX)) {
				temp.setTo(previousX, previousY, 0, previousX, previousY, depth, x, y, 0, previousX, previousY, 0);
				temp.getNormal(true, 1, normal);
				sideNormalBuffer.put(new double[] { normal.getX(), normal.getY(), normal.getZ(), normal.getX(),
						normal.getY(), normal.getZ() });
			} else {
				contourStartNormalPos = sideNormalBuffer.position();
				sideNormalBuffer.put(new double[] { 0, 0, 1, 0, 0, 1 });
			}
			sideQuadsBuffer.put(x).put(y).put(depth);
		}
		previousX = x;
		previousY = y;
	}

	// -------------------------------------------------------------------------
	// Side / border / face VBO draw helpers
	// -------------------------------------------------------------------------

	/**
	 * Draws the lateral (side) faces of extruded text as {@code GL_TRIANGLE_STRIP} bands, one per outline contour,
	 * using the {@link OpenGL} VBO path.
	 */
	public void drawSide() {
		if (sideQuadsBuffer == null || sideQuadsBuffer.limit() == 0) return;
		drawSideFallback(gl);
	}

	/**
	 * Draws the outline (border) of each glyph contour as {@code GL_LINE_LOOP} primitives.
	 */
	public void drawBorder() {
		if (sideQuadsBuffer == null || sideQuadsBuffer.limit() == 0) return;
		drawBorderFallback();
	}

	/**
	 * Draws the front or back face of extruded text using the triangle data produced by GLU.
	 *
	 * @param up
	 *            {@code true} to use the upward-facing normal (front face); {@code false} for back face
	 */
	void drawFace(final boolean up) {
		if (faceVertexBuffer == null || faceVertexBuffer.limit() == 0) return;
		drawFaceFallback(up);
	}

	/**
	 * Applies the {@link AxisAngle} rotation from the drawing attributes around the string's location.
	 *
	 * @param attributes
	 *            the drawing attributes containing the optional rotation
	 * @param p
	 *            the location point (read-only — not mutated)
	 * @return true if rotation was applied (and the translation to p has already been performed), false otherwise
	 */
	private boolean applyRotation(final IDrawingAttributes attributes, final IPoint p) {
		final AxisAngle rotation = attributes.getRotation();
		if (rotation != null) {
			gl.translateBy(p.getX(), p.getY(), p.getZ());
			final IPoint axis = rotation.getAxis();
			gl.rotateBy(-rotation.getAngle(), axis.getX(), axis.getY(), axis.getZ());
			// Return true so callers use (0,0,0) as the effective position (translation already applied)
			return true;
		}
		return false;
	}

	// -------------------------------------------------------------------------
	// Fallback renderers (delegate from the draw* methods above)
	// -------------------------------------------------------------------------

	/**
	 * Draws the tessellated front or back face from {@link #faceVertexBuffer} as {@code GL_TRIANGLES}.
	 *
	 * @param up
	 *            {@code true} for upward-facing (front) normal, {@code false} for downward (back)
	 */
	public void drawFaceFallback(final boolean up) {
		if (faceVertexBuffer == null || faceVertexBuffer.limit() == 0) return;
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
	 * Draws the lateral quad-strip side faces from {@link #sideQuadsBuffer} as {@code GL_TRIANGLE_STRIP} bands, one per
	 * outline contour.
	 *
	 * @param openGL
	 *            the {@link OpenGL} helper to draw into
	 */
	public void drawSideFallback(final OpenGL openGL) {
		if (sideQuadsBuffer == null || sideQuadsBuffer.limit() == 0) return;
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
	 * Draws the outline of each glyph contour from {@link #sideQuadsBuffer} as {@code GL_LINE_LOOP} primitives, one per
	 * contour.
	 */
	public void drawBorderFallback() {
		if (sideQuadsBuffer == null || sideQuadsBuffer.limit() == 0) return;
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
		gluDeleteTess(tessellator);
		if (regionRenderer != null) {
			try {
				regionRenderer.destroy(gl.getGL().getGL2ES2());
			} catch (final Exception e) {
				DEBUG.ERR("TextDrawer.dispose: " + e.getMessage());
			}
			regionRenderer = null;
			textUtil = null;
		}
		if (textVertexArray != 0) {
			gl.getGL().glDeleteVertexArrays(1, new int[] { textVertexArray }, 0);
			textVertexArray = 0;
		}
		fontCache.clear();
		faceVertexBuffer = faceTextureBuffer = sideNormalBuffer = sideQuadsBuffer = null;
	}
}
