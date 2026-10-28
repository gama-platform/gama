package gama.ui.display.opengl.scene.layers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL;

import gama.api.ui.layers.IDrawingAttributes;
import gama.ui.display.opengl.IOpenGL;
import gama.ui.display.opengl.renderer.IOpenGLRenderer;
import gama.ui.display.opengl.renderer.helpers.IPickingHelper;
import gama.ui.display.opengl.scene.AbstractObject;

/** Standalone scene regression checks; run with the OpenGL bundle's runtime classpath. No GL context is needed. */
public final class SharedSceneTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static <T> T proxy(final Class<T> type, final InvocationHandler handler) {
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler));
	}

	private record Vertex(double x, double y) {}

	private record Primitive(int mode, List<Vertex> vertices) {}

	private static final class Backend {
		final boolean stored;
		final List<Primitive> primitives = new ArrayList<>();
		List<Vertex> vertices;
		int mode, compilations, replays;
		Runnable cached;
		double[] projection;

		final IOpenGL gl;

		Backend(final boolean stored) {
			this.stored = stored;
			gl = proxy(IOpenGL.class, this::invoke);
		}

		private Object invoke(final Object proxy, final java.lang.reflect.Method method, final Object[] args) {
			switch (method.getName()) {
				case "isDrawingStored": return stored;
				case "compileAsList":
					compilations++;
					cached = (Runnable) args[0];
					if (stored) { cached.run(); }
					return 1;
				case "drawList":
					replays++;
					if (!stored) { cached.run(); }
					return null;
				case "beginDrawing":
					mode = (int) args[0];
					vertices = new ArrayList<>();
					return null;
				case "outputVertex":
					check((double) args[2] == 0, "Overlay vertices must stay in the XY plane");
					vertices.add(new Vertex((double) args[0], (double) args[1]));
					return null;
				case "endDrawing":
					primitives.add(new Primitive(mode, vertices));
					return null;
				case "getViewWidth": return 200;
				case "getViewHeight": return 100;
				case "getWorldWidth", "getWorldHeight": return 100d;
				case "ortho":
					projection = new double[6];
					for (int i = 0; i < 6; i++) { projection[i] = (double) args[i]; }
					return null;
				case "pushIdentity", "translateBy", "scaleBy", "rotateBy": return null;
				default: throw new AssertionError("Unexpected backend operation: " + method);
			}
		}
	}

	private static final IPickingHelper PICKING = proxy(IPickingHelper.class, (p, method, args) -> {
		if ("isPicking".equals(method.getName())) return false;
		throw new AssertionError("Unexpected picking operation: " + method);
	});

	private static final IOpenGLRenderer RENDERER = proxy(IOpenGLRenderer.class, (p, method, args) -> {
		return switch (method.getName()) {
			case "getPickingHelper" -> PICKING;
			case "getEnvWidth", "getEnvHeight" -> 100d;
			default -> throw new AssertionError("Unexpected renderer operation: " + method);
		};
	});

	private static final class TestLayer extends LayerObject {
		int drawings, textureChecks;
		boolean texturesAvailable;

		TestLayer() {
			super(RENDERER, null);
			final IDrawingAttributes attributes = proxy(IDrawingAttributes.class, (p, method, args) -> {
				if ("getTextures".equals(method.getName())) return null;
				throw new AssertionError("Unexpected attribute operation: " + method);
			});
			currentList.add(new AbstractObject<>(new Object(), attributes, IDrawingAttributes.DrawerType.RESOURCE) {
				@Override
				public boolean areTexturesAvailable(final IOpenGL gl) {
					textureChecks++;
					return texturesAvailable;
				}
			});
		}

		@Override
		public void computeOffset(final Trace trace) {}

		@Override
		public void computeScale(final Trace trace) {}

		@Override
		public void computeRotation(final Trace trace) {}

		@Override
		protected void drawAllObjects(final IOpenGL gl, final boolean picking) { drawings++; }
	}

	private static final class TestOverlay extends OverlayLayerObject {
		TestOverlay() { super(RENDERER, null); }

		@Override
		public void computeOffset(final Trace trace) {}

		@Override
		protected void addFrame(final IOpenGL gl) {}

		void prepare(final IOpenGL gl) { prepareDrawing(gl, currentList); }
	}

	private static void checkCachedLayers() {
		final Backend recorded = new Backend(true);
		final TestLayer legacy = new TestLayer();
		legacy.doDrawing(recorded.gl);
		check(recorded.compilations == 0 && legacy.drawings == 1,
				"GL2 must draw directly rather than record a missing texture");
		legacy.texturesAvailable = true;
		legacy.doDrawing(recorded.gl);
		legacy.doDrawing(recorded.gl);
		check(recorded.compilations == 1 && recorded.replays == 2 && legacy.drawings == 2,
				"GL2 must record once after textures arrive and reuse the display list");

		final Backend replayed = new Backend(false);
		final TestLayer core = new TestLayer();
		core.doDrawing(replayed.gl);
		core.doDrawing(replayed.gl);
		check(replayed.compilations == 1 && replayed.replays == 2 && core.drawings == 2,
				"GL4 must cache once and replay Java drawing on every frame");
		check(core.textureChecks == 0, "GL4 must not require textures to be ready before caching Java drawing");

		final var original = legacy.currentList.asArray();
		legacy.currentList.set(0, core.currentList.getFirst());
		check(legacy.currentList.asArray() == original && original[0] == core.currentList.getFirst(),
				"Reusing a trace array must refresh objects even when the trace size is unchanged");
	}

	private static double area(final Vertex a, final Vertex b, final Vertex c) {
		return Math.abs((b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)) / 2;
	}

	private static double area(final Primitive primitive) {
		final List<Vertex> v = primitive.vertices;
		double area = 0;
		if (primitive.mode == GL.GL_TRIANGLES) {
			check(v.size() % 3 == 0, "Triangles need complete vertex triples");
			for (int i = 0; i < v.size(); i += 3) { area += area(v.get(i), v.get(i + 1), v.get(i + 2)); }
		} else {
			check(primitive.mode == GL.GL_TRIANGLE_FAN, "Overlays must use core-profile-compatible primitives");
			for (int i = 1; i + 1 < v.size(); i++) { area += area(v.getFirst(), v.get(i), v.get(i + 1)); }
		}
		return area;
	}

	private static void checkOverlay(final boolean stored) {
		final Backend backend = new Backend(stored);
		final TestOverlay overlay = new TestOverlay();
		overlay.prepare(backend.gl);
		check(java.util.Arrays.equals(backend.projection, new double[] { 0, 200, -100, 0, -1, 1 }),
				"Shared overlays must delegate the aspect-correct orthographic projection to the backend");
		overlay.drawRoundedRectangle(backend.gl, 10, 20, 40, 20, 0, 40);
		check(backend.primitives.size() == 1 && backend.primitives.getFirst().vertices.size() == 6,
				"A square-cornered frame must draw two triangles, not GL_QUADS");
		check(area(backend.primitives.getFirst()) == 800, "Frame triangles must cover the rectangle exactly");
		backend.primitives.clear();
		overlay.drawRoundedRectangle(backend.gl, 10, 20, 40, 20, 2, 40);
		check(backend.primitives.size() == 5 && backend.primitives.getFirst().vertices.size() == 30,
				"Rounded frames need five rectangular sections and four independent corner fans");
		final double area = backend.primitives.stream().mapToDouble(SharedSceneTest::area).sum();
		check(Math.abs(area - (800 - (4 - Math.PI) * 4)) < .01,
				"Rounded sections must cover the intended area without missing or overlapping rectangles");
	}

	public static void main(final String[] args) {
		final var display = gama.ui.display.opengl.view.SWTOpenGLDisplaySurface.class
				.getAnnotation(gama.annotations.display.class);
		check(display.is3D() && java.util.Arrays.asList(display.value()).containsAll(List.of("opengl", "3d", "3D")),
				"Both 3d and 3D aliases must register the OpenGL factory and its 3D metadata");
		checkCachedLayers();
		checkOverlay(true);
		checkOverlay(false);
		System.out.println("Shared scene checks passed");
	}
}
