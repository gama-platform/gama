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
import com.jogamp.opengl.GL2ES2;

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
import gama.ui.display.opengl4.scene.ObjectDrawer;
import gama.ui.shared.utils.DPIHelper;

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
 * <li>For <b>3-D extruded</b> text ({@code depth > 0}) the flat face is rendered by the graph library while the lateral
 * (side) geometry is still produced by the AWT outline → GLU-tessellator path, because the graph library only provides
 * 2-D outline rendering.</li>
 * </ul>
 *
 * <h3>Thread safety</h3> All methods must be called from the GL thread.
 */
public class TextDrawer extends ObjectDrawer<StringObject> {

	// -------------------------------------------------------------------------
	// JOGL graph / curve API state
	// -------------------------------------------------------------------------

	/**
	 * Hint flags for the JOGL graph region renderer. {@code VBAA_RENDERING_BIT} enables 2-pass vertex-buffer
	 * anti-aliasing for smooth text at any size.
	 */
	private static final int RENDER_HINTS = Region.VBAA_RENDERING_BIT;

	/**
	 * The JOGL graph region renderer. Created lazily on first use so that it is initialised on the correct GL thread.
	 * One instance is shared for all text draw calls from this {@code TextDrawer}.
	 */
	private RegionRenderer regionRenderer;

	/**
	 * High-level utility wrapper around the region renderer that provides the {@link TextRegionUtil#drawString3D}
	 * convenience method.
	 */
	private TextRegionUtil textUtil;

	/**
	 * Cache of wrapped JOGL {@link com.jogamp.graph.font.Font} objects, keyed by the AWT {@link Font}. Wrapping an AWT
	 * font is inexpensive but not free; caching avoids repeated reflection overhead.
	 */
	private final Map<Font, com.jogamp.graph.font.Font> fontCache = new HashMap<>();

	// -------------------------------------------------------------------------
	// Contour geometry state (used for 3-D extrusion side geometry and FastTriangulation face tessellation)
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
	 * Initial capacity (in {@code double} values) for the per-call geometry buffers. 1 M doubles = 8 MB per buffer; the
	 * buffer is cleared (not reallocated) on every draw call.
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
			DEBUG.ERR("TextDrawer: failed to initialise RegionRenderer — " + e.getMessage());
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
		// Note: the cache key is the AWT Font; overlay fonts (defaulting to JetBrainsMonoNL) and world-space
		// fonts (defaulting to Poppins) with the same AWT Font object would collide — but in practice the
		// AWT Font object already encodes the family name, so "default" overlay vs. "default" world-space
		// would differ only if both requested the exact same AWT Font, which is unlikely.
		return fontCache.computeIfAbsent(awtFont, f -> loadJoglFont(f, overlay));
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
		drawWithGraphLibrary(text, attributes, overlay);
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
		final com.jogamp.graph.font.Font joglFont = getJoglFont(awtFont, overlay);
		if (joglFont == null) {
			if (!overlay) { drawLegacyPerspective(text, attributes); }
			return;
		}

		final IColor c = gl.getCurrentColor();
		final Vec4f color = new Vec4f((float) (c.red() / 255.0), (float) (c.green() / 255.0),
				(float) (c.blue() / 255.0), (float) (c.alpha() / 255.0 * gl.getCurrentObjectAlpha()));

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
	 * Draws extruded 3-D text. The front and back faces are rendered by the graph library (flat, at z=0 and z=depth).
	 * The lateral side geometry is built from the AWT glyph outline and rendered via the standard
	 * {@link OpenGL#beginDrawing}/{@link OpenGL#endDrawing} VBO path.
	 *
	 * @param text
	 *            the string to render
	 * @param attributes
	 *            the drawing attributes
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
			gl.translateBy(p.getX() - width * scale * anchor.getX(), p.getY() + bounds.getY() * scale * anchor.getY(),
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
	 * Legacy perspective-text drawing path using AWT outline tessellation. Used as a fallback when the JOGL graph
	 * library is unavailable, and as the source of side-geometry data for extruded text.
	 *
	 * @param text
	 *            the string to draw
	 * @param attributes
	 *            the drawing attributes
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
	 * @param attributes
	 *            the drawing attributes
	 * @param y
	 *            the baseline Y offset from {@link Rectangle2D#getY()}
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
	 * <p>
	 * Contour vertices are collected into per-contour rings. In solid mode the rings are passed to
	 * {@link FastTriangulation#triangulate} to fill {@link #faceVertexBuffer} with triangle data (replaces the former
	 * GLU tessellator path). In wireframe mode only the side contour vertices are collected. Side geometry (for depth >
	 * 0) is always collected into {@link #sideQuadsBuffer}.
	 * </p>
	 *
	 * @param pi
	 *            the glyph outline path iterator (pre-flattened: only SEG_MOVETO/SEG_LINETO/SEG_CLOSE)
	 */
	void process(final PathIterator pi) {
		final boolean wireframe = gl.isWireframe();

		// Collect all contours: each is a flat double[x0,y0,x1,y1,...] open ring.
		final List<double[]> contours = new ArrayList<>();
		final List<double[]> contourZ = new ArrayList<>(); // parallel z values (always 0 for text faces)
		double[] curX = null, curY = null;
		int curLen = 0;
		double x0 = 0, y0 = 0;

		while (!pi.isDone()) {
			final double[] coords = new double[6];
			switch (pi.currentSegment(coords)) {
				case SEG_MOVETO:
					// Start a new contour ring
					curX = new double[256];
					curY = new double[256];
					curLen = 0;
					x0 = coords[0];
					y0 = coords[1];
					curX[curLen] = x0;
					curY[curLen] = y0;
					curLen++;
					beginNewContour();
					addContourVertex0(x0, y0);
					break;
				case SEG_LINETO:
					if (curX != null) {
						if (curLen >= curX.length) {
							curX = java.util.Arrays.copyOf(curX, curLen * 2);
							curY = java.util.Arrays.copyOf(curY, curLen * 2);
						}
						curX[curLen] = coords[0];
						curY[curLen] = coords[1];
						curLen++;
					}
					addContourVertex0(coords[0], coords[1]);
					break;
				case SEG_CLOSE:
					if (curX != null && curLen >= 3) {
						final double[] flatXY = new double[curLen * 2];
						for (int i = 0; i < curLen; i++) {
							flatXY[i * 2] = curX[i];
							flatXY[i * 2 + 1] = curY[i];
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

		// ---- face tessellation via FastTriangulation (solid mode only) ----
		if (!wireframe && !contours.isEmpty()) {
			// Determine outer ring: the one with the largest absolute signed area (CW in screen space)
			// AWT uses a Y-down coordinate system after the AT (y-flip), so the outer ring has the largest area.
			int outerIdx = 0;
			double maxArea = 0;
			for (int i = 0; i < contours.size(); i++) {
				final double area = Math.abs(signedArea(contours.get(i)));
				if (area > maxArea) {
					maxArea = area;
					outerIdx = i;
				}
			}
			final double[] outerRing = contours.get(outerIdx);
			final int outerN = outerRing.length / 2;

			// Holes: all other contours
			final List<double[]> holes = new ArrayList<>(contours.size() - 1);
			for (int i = 0; i < contours.size(); i++) { if (i != outerIdx) { holes.add(contours.get(i)); } }
			final double[][] holesArr = holes.isEmpty() ? null : holes.toArray(new double[0][]);

			final int[] triIndices = FastTriangulation.triangulate(outerRing, holesArr, outerN);

			// Build combined flat vertex arrays (outer first, then holes)
			int totalV = outerN;
			if (holesArr != null) { for (final double[] h : holesArr) { totalV += h.length / 2; } }
			final double[] allX = new double[totalV], allY = new double[totalV];
			for (int i = 0; i < outerN; i++) {
				allX[i] = outerRing[i * 2];
				allY[i] = outerRing[i * 2 + 1];
			}
			int off = outerN;
			if (holesArr != null) {
				for (final double[] h : holesArr) {
					for (int i = 0; i < h.length / 2; i++) {
						allX[off + i] = h[i * 2];
						allY[off + i] = h[i * 2 + 1];
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
	 * Computes the signed area of a flat {@code double[x0,y0,x1,y1,…]} open ring using the shoelace formula. Positive =
	 * CCW, negative = CW (in Y-up space).
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
	 * Adds a vertex at z=0 to {@link #sideQuadsBuffer} and, if {@link #depth} > 0, also adds the same vertex at z=depth
	 * and computes the side-face normal for the segment from the previous vertex to this one.
	 *
	 * @param x
	 *            the X coordinate in font units
	 * @param y
	 *            the Y coordinate in font units
	 */
	public void addContourVertex0(final double x, final double y) {
		sideQuadsBuffer.put(x).put(y).put(0);
		if (depth > 0) {
			if (previousX > Double.MIN_VALUE) {
				temp.setTo(previousX, previousY, 0, previousX, previousY, depth, x, y, 0, previousX, previousY, 0);
				temp.getNormal(true, 1, normal);
				sideNormalBuffer.put(new double[] { normal.getX(), normal.getY(), normal.getZ(), normal.getX(),
						normal.getY(), normal.getZ() });
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
		drawBorderFallback();
	}

	/**
	 * Draws the front or back face of extruded text using the triangle data produced by {@link FastTriangulation}.
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
	 *            the location point (mutated to zero if a rotation is applied, to avoid double-translation)
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
	 * @param up
	 *            {@code true} for upward-facing (front) normal, {@code false} for downward (back)
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
	 * Draws the lateral quad-strip side faces from {@link #sideQuadsBuffer} as {@code GL_TRIANGLE_STRIP} bands, one per
	 * outline contour.
	 *
	 * @param openGL
	 *            the {@link OpenGL} helper to draw into
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
	 * Draws the outline of each glyph contour from {@link #sideQuadsBuffer} as {@code GL_LINE_LOOP} primitives, one per
	 * contour.
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
