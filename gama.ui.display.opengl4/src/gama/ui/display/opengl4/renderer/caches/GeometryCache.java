/*******************************************************************************************************
 *
 * GeometryCache.java, in gama.ui.display.opengl4, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl4.renderer.caches;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static gama.api.types.geometry.IShape.Type.CIRCLE;
import static gama.api.types.geometry.IShape.Type.CONE;
import static gama.api.types.geometry.IShape.Type.CUBE;
import static gama.api.types.geometry.IShape.Type.CYLINDER;
import static gama.api.types.geometry.IShape.Type.POINT;
import static gama.api.types.geometry.IShape.Type.PYRAMID;
import static gama.api.types.geometry.IShape.Type.SPHERE;
import static gama.api.types.geometry.IShape.Type.SQUARE;
import static gama.api.utils.geometry.GeometryUtils.getTypeOf;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.nio.DoubleBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFilter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GL4;

import gama.api.GAMA;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.geometry.IShape.Type;
import gama.api.utils.geometry.GamaCoordinateSequenceFactory;
import gama.api.utils.geometry.GamaEnvelopeFactory;
import gama.api.utils.geometry.ICoordinates;
import gama.api.utils.geometry.IEnvelope;
import gama.api.utils.prefs.GamaPreferences;
import gama.core.util.file.GamaGeometryFile;
import gama.core.util.file.GamaObjFile;
import gama.dev.DEBUG;
import gama.ui.display.opengl4.OpenGL;
import gama.ui.display.opengl4.files.ObjFileDrawer;
import gama.ui.display.opengl4.renderer.IOpenGLRenderer;

/**
 * The Class GeometryCache. Manages two kinds of geometry caches for the OpenGL 4 renderer:
 * <ul>
 * <li><b>Built-in geometry cache</b> ({@link #builtInCache}): stores pre-tessellated primitive shapes (sphere, cube,
 * cylinder, etc.) as {@link BuiltInGeometry} instances, each of which holds Runnable "list" IDs registered with
 * {@link OpenGL#compileAsList(Runnable)}.</li>
 * <li><b>File geometry cache</b> ({@link #fileCache}): caches 3-D geometry files (OBJ, etc.) by their path, also as
 * Runnable list IDs.</li>
 * </ul>
 *
 * <p>
 * Note: OpenGL display lists have been removed in the GL4 core profile. The Runnable-cache mechanism in
 * {@link OpenGL} replaces them: {@link OpenGL#compileAsList(Runnable)} stores a lambda, and
 * {@link OpenGL#drawList(int)} replays it. Each stored Runnable operates on its own private, immutable copy of any
 * geometry data it requires, so it is safe to replay from any frame.
 * </p>
 */
public class GeometryCache {

	static {
		DEBUG.OFF();
	}

	/** The Constant PI_2. */
	private static final double PI_2 = 2f * Math.PI;

	/**
	 * The pre-computed 2-D vertices for the rounded rectangle (40 vertices × 2 components).
	 * Note: this array is static/shared but the DoubleBuffer wrapping it is <em>instance</em>-local
	 * (see {@link #db}) to avoid cross-context / cross-thread races on position/limit.
	 */
	static final double[] roundRect = { .92, 0, .933892, .001215, .947362, .004825, .96, .010718, .971423, .018716,
			.981284, .028577, .989282, .04, .995175, .052638, .998785, .066108, 1, .08, 1, .92, .998785, .933892,
			.995175, .947362, .989282, .96, .981284, .971423, .971423, .981284, .96, .989282, .947362, .995175,
			.933892, .998785, .92, 1, .08, 1, .066108, .998785, .052638, .995175, .04, .989282, .028577, .981284,
			.018716, .971423, .010718, .96, .004825, .947362, .001215, .933892, 0, .92, 0, .08, .001215, .066108,
			.004825, .052638, .010718, .04, .018716, .028577, .028577, .018716, .04, .010718, .052638, .004825,
			.066108, .001215, .08, 0 };

	/**
	 * Instance-local (non-static) DoubleBuffer wrapping the shared {@link #roundRect} array.
	 * Each {@link GeometryCache} instance owns its own buffer so that {@code rewind()} calls are not shared
	 * across GL contexts or threads.
	 */
	private final DoubleBuffer db = Buffers.newDirectDoubleBuffer(roundRect.length).put(roundRect).rewind();

	/**
	 * The Class BuiltInGeometry. Holds the list IDs (Runnable-cache handles) for each face group of a
	 * built-in primitive shape.
	 *
	 * <ul>
	 * <li>{@link #bottom} – the bottom cap (disk, base polygon, …), or {@code null} if absent.</li>
	 * <li>{@link #top} – the top cap, or {@code null} if absent.</li>
	 * <li>{@link #faces} – the lateral faces (sphere, cylinder wall, …), or {@code null} if absent.</li>
	 * </ul>
	 */
	public static class BuiltInGeometry {

		/** The list ID of the bottom cap geometry, or {@code null}. */
		Integer bottom;

		/** The list ID of the top cap geometry, or {@code null}. */
		Integer top;

		/** The list ID of the lateral faces geometry, or {@code null}. */
		Integer faces;

		/**
		 * Assemble. Creates a new empty {@link BuiltInGeometry} whose parts can then be set via the fluent methods.
		 *
		 * @return a new {@link BuiltInGeometry}
		 */
		public static BuiltInGeometry assemble() {
			return new BuiltInGeometry(null, null, null);
		}

		/**
		 * Top. Sets the top-cap list ID.
		 *
		 * @param top the top-cap list ID
		 * @return {@code this} for fluent chaining
		 */
		public BuiltInGeometry top(final Integer top) {
			this.top = top;
			return this;
		}

		/**
		 * Faces. Sets the lateral-faces list ID.
		 *
		 * @param faces the lateral-faces list ID
		 * @return {@code this} for fluent chaining
		 */
		public BuiltInGeometry faces(final Integer faces) {
			this.faces = faces;
			return this;
		}

		/**
		 * Bottom. Sets the bottom-cap list ID.
		 *
		 * @param bottom the bottom-cap list ID
		 * @return {@code this} for fluent chaining
		 */
		public BuiltInGeometry bottom(final Integer bottom) {
			this.bottom = bottom;
			return this;
		}

		/**
		 * Instantiates a new built in geometry.
		 *
		 * @param bottom the bottom-cap list ID, or {@code null}
		 * @param top    the top-cap list ID, or {@code null}
		 * @param faces  the lateral-faces list ID, or {@code null}
		 */
		private BuiltInGeometry(final Integer bottom, final Integer top, final Integer faces) {
			this.bottom = bottom;
			this.top = top;
			this.faces = faces;
		}

		/**
		 * Draw. Replays each non-null face group via {@link OpenGL#drawList(int)}, enabling the alternate texture
		 * before drawing the lateral faces.
		 *
		 * @param gl the {@link OpenGL} helper
		 */
		public void draw(final OpenGL gl) {
			if (bottom != null) { gl.drawList(bottom); }
			if (top != null) { gl.drawList(top); }
			gl.enableAlternateTexture();
			if (faces != null) { gl.drawList(faces); }
		}

		/**
		 * Gets all list IDs held by this geometry as an array. Used to release them from the Runnable cache on
		 * disposal.
		 *
		 * @return array of non-null list IDs
		 */
		public Integer[] listIds() {
			return new Integer[] { bottom, top, faces };
		}
	}

	/** The built-in primitive geometry cache, keyed by {@link IShape.Type}. */
	private final Cache<IShape.Type, BuiltInGeometry> builtInCache;

	/**
	 * The file geometry cache, keyed by the absolute file path. Values are Runnable-cache list IDs registered
	 * with {@link OpenGL#compileAsList(Runnable)}.
	 */
	private final LoadingCache<String, Integer> fileCache;

	/**
	 * Maps file paths to the corresponding {@link GamaGeometryFile}, used when building a file list entry
	 * lazily.
	 */
	private final Map<String, GamaGeometryFile> fileMap = new ConcurrentHashMap<>();

	/**
	 * Files waiting to be loaded and uploaded to the GPU. Populated by {@link #process(GamaGeometryFile)} and
	 * consumed by {@link #processUnloaded()}.
	 */
	private final Map<String, GamaGeometryFile> geometriesToProcess = new ConcurrentHashMap<>();

	/** Cache of bounding envelopes for geometry files, keyed by path. Expires after 2 minutes of inactivity. */
	private final Cache<String, IEnvelope> envelopes;

	/** The GAMA scope used to load geometry files and compute envelopes. */
	private final IScope scope;

	/**
	 * Functional consumer that drives simple (non-OBJ) geometry drawing through the {@link
	 * gama.ui.display.opengl4.scene.geometry.GeometryDrawer}.
	 */
	private final Consumer<Geometry> drawer;

	/** Back-reference to the renderer, kept for list-ID cleanup on disposal. */
	private final IOpenGLRenderer renderer;

	/**
	 * Instantiates a new geometry cache.
	 *
	 * @param renderer the {@link IOpenGLRenderer} that owns this cache
	 */
	public GeometryCache(final IOpenGLRenderer renderer) {
		this.renderer = renderer;
		this.scope = renderer.getSurface().getScope().copy("in opengl geometry cache");
		this.drawer = g -> renderer.getOpenGLHelper().getGeometryDrawer().drawGeometry(g, null, 0, getTypeOf(g));
		envelopes = newBuilder().expireAfterAccess(2, MINUTES).build();
		builtInCache = newBuilder().concurrencyLevel(2).initialCapacity(10).build();
		fileCache = newBuilder().expireAfterAccess(2, MINUTES).initialCapacity(10).removalListener(notif -> {
			if (renderer.isDisposed()) return;
			// Release the Runnable stored in OpenGL's list cache when a file entry expires.
			renderer.getOpenGLHelper().deleteList((Integer) notif.getValue());
		}).build(new CacheLoader<String, Integer>() {
			@Override
			public Integer load(final String file) {
				return buildList(renderer.getOpenGLHelper(), file);
			}
		});
	}

	/**
	 * Gets the Runnable-cache list ID for a geometry file, loading it if not yet cached.
	 *
	 * @param file the geometry file
	 * @return the list ID
	 */
	public Integer get(final GamaGeometryFile file) {
		return fileCache.getUnchecked(file.getPath(scope));
	}

	/**
	 * Gets the built-in geometry for the given shape type, or {@code null} if not yet initialised.
	 *
	 * @param id the shape type
	 * @return the {@link BuiltInGeometry}, or {@code null}
	 */
	public BuiltInGeometry get(final IShape.Type id) {
		return builtInCache.getIfPresent(id);
	}

	/**
	 * Builds the Runnable-cache list entry for the given geometry file.
	 *
	 * @param gl   the {@link OpenGL} helper
	 * @param name the absolute path of the file
	 * @return the list ID
	 */
	Integer buildList(final OpenGL gl, final String name) {
		DEBUG.OUT("Building OpenGL list for " + name);
		final GamaGeometryFile file = fileMap.get(name);
		return gl.compileAsList(() -> {
			if (file instanceof GamaObjFile f) {
				f.loadObject(scope, true);
				ObjFileDrawer.drawToOpenGL(f, gl);
			} else {
				final IShape shape = file.getGeometry(scope);
				if (shape == null) return;
				try {
					drawSimpleGeometry(gl, shape.getInnerGeometry());
				} catch (final ExecutionException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Draw simple geometry. Applies the {@link #drawer} consumer to each sub-geometry.
	 *
	 * @param gl   the {@link OpenGL} helper
	 * @param geom the JTS geometry to draw
	 * @throws ExecutionException if the geometry cannot be drawn
	 */
	void drawSimpleGeometry(final OpenGL gl, final Geometry geom) throws ExecutionException {
		geom.apply((GeometryFilter) g -> drawer.accept(g));
	}

	/**
	 * Dispose. Clears all caches and releases the GAMA scope. Also removes all built-in geometry Runnable entries
	 * from the {@link OpenGL} list cache to prevent memory leaks.
	 */
	public void dispose() {
		// Release all built-in geometry Runnables from the OpenGL list cache.
		if (!renderer.isDisposed()) {
			final OpenGL openGL = renderer.getOpenGLHelper();
			builtInCache.asMap().values().forEach(bg -> {
				for (final Integer id : bg.listIds()) { openGL.deleteList(id); }
			});
		}
		builtInCache.invalidateAll();
		fileMap.clear();
		GAMA.releaseScope(scope);
	}

	/**
	 * Process unloaded. Loads and uploads all geometry files that have been queued via
	 * {@link #process(GamaGeometryFile)}.
	 */
	public void processUnloaded() {
		for (final GamaGeometryFile object : geometriesToProcess.values()) { get(object); }
		geometriesToProcess.clear();
	}

	/**
	 * Process. Queues a geometry file for loading on the next call to {@link #processUnloaded()}.
	 *
	 * @param file the file to queue
	 */
	public void process(final GamaGeometryFile file) {
		if (file == null) return;
		final String path = file.getPath(scope);
		if (fileCache.getIfPresent(path) != null) return;
		fileMap.putIfAbsent(path, file);
		if (!geometriesToProcess.containsKey(path)) { geometriesToProcess.put(path, file); }
	}

	/**
	 * Gets the bounding envelope for a geometry file.
	 *
	 * @param file the geometry file
	 * @return the {@link IEnvelope}, or {@link GamaEnvelopeFactory#EMPTY} if unavailable
	 */
	public IEnvelope getEnvelope(final GamaGeometryFile file) {
		try {
			return envelopes.get(file.getPath(scope), () -> file.computeEnvelope(scope));
		} catch (final ExecutionException e) {
			return GamaEnvelopeFactory.EMPTY;
		}
	}

	/**
	 * Put. Registers a {@link BuiltInGeometry} under the given type in the built-in cache.
	 *
	 * @param key   the shape type
	 * @param value the geometry to register
	 */
	public void put(final Type key, final BuiltInGeometry value) {
		builtInCache.put(key, value);
	}

	/**
	 * Initialize. Registers Runnable-based geometry for all built-in primitive types (sphere, cylinder, cone, cube,
	 * point, rounded rectangle, square, circle, pyramid). Each Runnable operates on its own private copy of any
	 * coordinate data so that it is safe to replay from multiple frames without mutation races.
	 *
	 * <p>
	 * The {@code textured} flag on the {@link OpenGL} helper is set to {@code true} for the duration of the
	 * registration so that texture-coordinate outputs in the primitive-drawing methods are active.
	 * </p>
	 *
	 * @param gl the {@link OpenGL} helper
	 */
	public void initialize(final OpenGL gl) {
		final int slices = GamaPreferences.Displays.DISPLAY_SLICE_NUMBER.getValue();
		final int stacks = slices;

		put(SPHERE, BuiltInGeometry.assemble().faces(gl.compileAsList(() -> {
			gl.translateBy(0d, 0d, 1d);
			drawSphere(gl, 1.0, slices, stacks);
			gl.translateBy(0, 0, -1d);
		})));

		put(CYLINDER,
				BuiltInGeometry.assemble()
						.bottom(gl.compileAsList(() -> drawDisk(gl, 0d, 1d, slices, slices / 3)))
						.top(gl.compileAsList(() -> {
							gl.translateBy(0d, 0d, 1d);
							drawDisk(gl, 0d, 1d, slices, slices / 3);
							gl.translateBy(0d, 0d, -1d);
						}))
						.faces(gl.compileAsList(() -> drawCylinder(gl, 1.0d, 1.0d, 1.0d, slices, stacks))));

		put(CONE,
				BuiltInGeometry.assemble()
						.bottom(gl.compileAsList(() -> drawDisk(gl, 0d, 1d, slices, slices / 3)))
						.faces(gl.compileAsList(() -> drawCylinder(gl, 1.0, 0.0, 1.0, slices, stacks))));

		// --- CUBE ---
		// Each lambda captures its own defensive copy of the coordinate data it needs,
		// so replaying from any frame is safe even if initialize() has long returned.
		final double[] cubeBase = { -0.5, 0.5, 0, 0.5, 0.5, 0, 0.5, -0.5, 0, -0.5, -0.5, 0, -0.5, 0.5, 0 };
		put(CUBE, BuiltInGeometry.assemble()
				.bottom(gl.compileAsList(() -> {
					final ICoordinates v = GamaCoordinateSequenceFactory.ofLength(5);
					v.setTo(cubeBase);
					gl.drawSimpleShape(v, 4, false, true, null);
				}))
				.top(gl.compileAsList(() -> {
					final ICoordinates v = GamaCoordinateSequenceFactory.ofLength(5);
					v.setTo(cubeBase);
					v.translateBy(0, 0, 1);
					gl.drawSimpleShape(v, 4, true, true, null);
				}))
				.faces(gl.compileAsList(() -> {
					final ICoordinates v = GamaCoordinateSequenceFactory.ofLength(5);
					v.setTo(cubeBase);
					final ICoordinates face = GamaCoordinateSequenceFactory.ofLength(5);
					v.visit((pj, pk) -> {
						face.setTo(pk.getX(), pk.getY(), pk.getZ(),
								pk.getX(), pk.getY(), pk.getZ() + 1,
								pj.getX(), pj.getY(), pj.getZ() + 1,
								pj.getX(), pj.getY(), pj.getZ(),
								pk.getX(), pk.getY(), pk.getZ());
						gl.drawSimpleShape(face, 4, true, true, null);
					});
				})));

		put(POINT, BuiltInGeometry.assemble().faces(gl.compileAsList(() -> drawSphere(gl, 1.0, 5, 5))));

		put(IShape.Type.ROUNDED, BuiltInGeometry.assemble().bottom(gl.compileAsList(() -> {
			// Align with SQUARE; see issue #3542
			gl.translateBy(-.5d, -.5d);
			drawRoundedRectangle(gl.getGL());
			gl.translateBy(.5d, .5d);
		})));

		// SQUARE: its own private copy of baseVertices
		final double[] squareBase = { -0.5, 0.5, 0, 0.5, 0.5, 0, 0.5, -0.5, 0, -0.5, -0.5, 0, -0.5, 0.5, 0 };
		put(SQUARE, BuiltInGeometry.assemble().bottom(gl.compileAsList(() -> {
			final ICoordinates v = GamaCoordinateSequenceFactory.ofLength(5);
			v.setTo(squareBase);
			gl.drawSimpleShape(v, 4, true, true, null);
		})));

		put(CIRCLE, BuiltInGeometry.assemble().bottom(gl.compileAsList(() -> drawDisk(gl, 0.0, 1.0, slices, 1))));

		// --- PYRAMID ---
		final double[] pyrBase = { -0.5, -0.5, 0, -0.5, 0.5, 0, 0.5, 0.5, 0, 0.5, -0.5, 0, -0.5, -0.5, 0 };
		put(PYRAMID, BuiltInGeometry.assemble()
				.bottom(gl.compileAsList(() -> {
					final ICoordinates v = GamaCoordinateSequenceFactory.ofLength(5);
					v.setTo(pyrBase);
					gl.drawSimpleShape(v, 4, false, true, null);
				}))
				.faces(gl.compileAsList(() -> {
					final ICoordinates v = GamaCoordinateSequenceFactory.ofLength(5);
					v.setTo(pyrBase);
					final IPoint top = GamaPointFactory.create(0, 0, 1);
					final ICoordinates tri = GamaCoordinateSequenceFactory.ofLength(4);
					v.visit((pj, pk) -> {
						tri.setTo(pj.getX(), pj.getY(), pj.getZ(),
								top.getX(), top.getY(), top.getZ(),
								pk.getX(), pk.getY(), pk.getZ(),
								pj.getX(), pj.getY(), pj.getZ());
						gl.drawSimpleShape(tri, 3, true, true, null);
					});
				})));
	}

	/**
	 * Draw rounded rectangle. Uploads the pre-computed 2-D vertices into a temporary VBO and draws them as a
	 * {@code GL_TRIANGLE_FAN}. The buffer ({@link #db}) is instance-local so that concurrent calls from different
	 * GL contexts do not race on its position. The old {@code glEnableClientState}/{@code glVertexPointer} path has
	 * been removed in the GL4 core profile.
	 *
	 * @param gl the {@link GL4} context
	 */
	public void drawRoundedRectangle(final GL4 gl) {
		final int[] tmpVbo = new int[1];
		gl.glGenBuffers(1, tmpVbo, 0);
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, tmpVbo[0]);
		db.rewind();
		gl.glBufferData(GL.GL_ARRAY_BUFFER, (long) roundRect.length * Double.BYTES, db, GL.GL_STATIC_DRAW);
		gl.glVertexAttribPointer(0, 2, GL2GL3.GL_DOUBLE, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		gl.glDrawArrays(GL.GL_TRIANGLE_FAN, 0, 40);
		gl.glDisableVertexAttribArray(0);
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
		gl.glDeleteBuffers(1, tmpVbo, 0);
	}

	/**
	 * Draw disk. Tessellates an annular disk as a series of {@code GL_TRIANGLE_STRIP} rings. Texture coordinates
	 * are only emitted when the {@link OpenGL} helper reports {@link OpenGL#isTextured()}.
	 *
	 * @param gl     the {@link OpenGL} helper
	 * @param inner  the inner radius (0 for a solid disk)
	 * @param outer  the outer radius
	 * @param slices the number of circumferential subdivisions
	 * @param loops  the number of radial subdivisions
	 */
	public void drawDisk(final OpenGL gl, final double inner, final double outer, final int slices, final int loops) {
		final double da = PI_2 / slices;
		final double dr = (outer - inner) / loops;
		final double dtc = 2.0 * outer;
		final boolean tex = gl.isTextured();
		double r1 = inner;
		gl.getGL().glFrontFace(GL.GL_CCW);
		for (int l = 0; l < loops; l++) {
			final double r2 = r1 + dr;
			gl.beginDrawing(GL4.GL_TRIANGLE_STRIP);
			for (int s = 0; s <= slices; s++) {
				final double a = (s == slices) ? 0.0 : s * da;
				final double sa = Math.sin(a);
				final double ca = Math.cos(a);
				gl.outputNormal(0.0, 0.0, 1.0);
				if (tex) { gl.outputTexCoord(0.5 + sa * r2 / dtc, 0.5 + ca * r2 / dtc); }
				gl.outputVertex(r2 * sa, r2 * ca, 0);
				gl.outputNormal(0.0, 0.0, 1.0);
				if (tex) { gl.outputTexCoord(0.5 + sa * r1 / dtc, 0.5 + ca * r1 / dtc); }
				gl.outputVertex(r1 * sa, r1 * ca, 0);
			}
			gl.endDrawing();
			r1 = r2;
		}
		gl.getGL().glFrontFace(GL.GL_CW);
	}

	/**
	 * Draw sphere. Tessellates a UV sphere as a series of {@code GL_TRIANGLE_STRIP} stacks. Texture coordinates
	 * are only emitted when the {@link OpenGL} helper reports {@link OpenGL#isTextured()}.
	 *
	 * @param gl     the {@link OpenGL} helper
	 * @param radius the sphere radius
	 * @param slices the number of longitudinal subdivisions
	 * @param stacks the number of latitudinal subdivisions
	 */
	public void drawSphere(final OpenGL gl, final double radius, final int slices, final int stacks) {
		final double drho = Math.PI / stacks;
		final double dtheta = PI_2 / slices;
		final double ds = 1.0 / slices;
		final double dt = 1.0 / stacks;
		final boolean tex = gl.isTextured();
		double t = 1.0;
		gl.getGL().glFrontFace(GL.GL_CCW);
		for (int i = 0; i < stacks; i++) {
			final double rho = i * drho;
			gl.beginDrawing(GL4.GL_TRIANGLE_STRIP);
			double s = 0.0;
			for (int j = 0; j <= slices; j++) {
				final double theta = (j == slices) ? 0.0 : j * dtheta;
				double x = -Math.sin(theta) * Math.sin(rho);
				double y = Math.cos(theta) * Math.sin(rho);
				double z = Math.cos(rho);
				gl.outputNormal(x, y, z);
				if (tex) { gl.outputTexCoord(s, t); }
				gl.outputVertex(x * radius, y * radius, z * radius);
				x = -Math.sin(theta) * Math.sin(rho + drho);
				y = Math.cos(theta) * Math.sin(rho + drho);
				z = Math.cos(rho + drho);
				gl.outputNormal(x, y, z);
				if (tex) { gl.outputTexCoord(s, t - dt); }
				s += ds;
				gl.outputVertex(x * radius, y * radius, z * radius);
			}
			gl.endDrawing();
			t -= dt;
		}
		gl.getGL().glFrontFace(GL.GL_CW);
	}

	/**
	 * Draw cylinder. Tessellates a (possibly tapered) cylinder as a series of {@code GL_TRIANGLE_STRIP} stacks.
	 * Texture coordinates are only emitted when the {@link OpenGL} helper reports {@link OpenGL#isTextured()}.
	 *
	 * @param gl     the {@link OpenGL} helper
	 * @param base   the radius at the bottom
	 * @param top    the radius at the top (0 for a cone)
	 * @param height the height
	 * @param slices the number of circumferential subdivisions
	 * @param stacks the number of height subdivisions
	 */
	public void drawCylinder(final OpenGL gl, final double base, final double top, final double height,
			final int slices, final int stacks) {
		final double da = PI_2 / slices;
		final double dr = (top - base) / stacks;
		final double dz = height / stacks;
		final double nz = (base - top) / height;
		final double ds = 1.0 / slices;
		final double dt = 1.0 / stacks;
		final boolean tex = gl.isTextured();
		double z = 0.0;
		double r = base;
		gl.getGL().glFrontFace(GL.GL_CCW);
		for (int j = 0; j < stacks; j++) {
			float t = (float) (j * dt);
			float s = 0.0f;
			gl.beginDrawing(GL4.GL_TRIANGLE_STRIP);
			for (int i = 0; i <= slices; i++) {
				final double x = (i == slices) ? 0.0 : Math.sin(i * da);
				final double y = (i == slices) ? 1.0 : Math.cos(i * da);
				gl.outputNormal(x, y, nz);
				if (tex) { gl.outputTexCoord(s, t); }
				gl.outputVertex(x * r, y * r, z);
				gl.outputNormal(x, y, nz);
				if (tex) { gl.outputTexCoord(s, t + dt); }
				gl.outputVertex(x * (r + dr), y * (r + dr), z + dz);
				s += ds;
			}
			gl.endDrawing();
			r += dr;
			z += dz;
		}
		gl.getGL().glFrontFace(GL.GL_CW);
	}

}
