package gama.ui.display.opengl4;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2GL3;

import gama.ui.display.opengl.renderer.helpers.CameraHelper;
import gama.ui.display.opengl.renderer.helpers.SceneHelper;
import gama.ui.display.opengl.renderer.shaders.AbstractShader;
import gama.ui.display.opengl.renderer.shaders.FrameBufferObject;
import gama.ui.display.opengl4.renderer.shaders.BasicShader;

/** Standalone backend wiring and shader/FBO checks with mocked GL entry points; no native window is needed. */
public final class BackendSpecializationTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static <T> T proxy(final Class<T> type, final InvocationHandler handler) {
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler));
	}

	private static final class ShaderCalls implements InvocationHandler {
		final List<String> sources = new ArrayList<>();
		int blends, programs, attachments;

		@Override
		public Object invoke(final Object p, final java.lang.reflect.Method method, final Object[] args) {
			switch (method.getName()) {
				case "glCreateProgram": return ++programs;
				case "glCreateShader": return 1;
				case "glGetUniformLocation": return 0;
				case "glShaderSource":
					sources.add(((String[]) args[2])[0]);
					return null;
				case "glGetShaderiv":
					((int[]) args[2])[(int) args[3]] = 1;
					return null;
				case "glEnable":
					blends++;
					return null;
				case "glAttachShader":
					attachments++;
					return null;
				case "glCompileShader", "glBlendFunc", "glBindAttribLocation", "glLinkProgram",
						"glValidateProgram", "glUseProgram", "glUniformMatrix4fv": return null;
				default: throw new AssertionError("Unexpected shader operation: " + method);
			}
		}
	}

	private static final class Shader extends AbstractShader {
		Shader(final GL2ES2 gl, final boolean legacy) {
			super(gl, "/gama/ui/display/opengl4/renderer/shaders/glsl/basic.vert",
					"/gama/ui/display/opengl4/renderer/shaders/glsl/basic.frag", legacy);
		}

		@Override
		protected void bindAttributes() {}

		@Override
		public boolean useNormal() { return false; }

		@Override
		public boolean useTexture() { return false; }

		@Override
		public int getTextureID() { return -1; }
	}

	private static void checkShaders() throws Exception {
		final ShaderCalls legacy = new ShaderCalls();
		new Shader(proxy(GL2ES2.class, legacy), true);
		check(legacy.programs == 1 && legacy.attachments == 2 && legacy.blends == 2,
				"The shared shader loader must retain legacy shader compilation and blend initialization");

		final ShaderCalls core = new ShaderCalls();
		new Shader(proxy(GL2ES2.class, core), false);
		check(core.programs == 1 && core.attachments == 2 && core.blends == 0,
				"GL4 must reuse shader loading without acquiring legacy blend-state side effects");
		check(core.sources.size() == 2 && core.sources.stream().allMatch(source -> source.contains("#version")),
				"Shared loading must read both shader sources");
		check(AbstractShader.class.isAssignableFrom(BasicShader.class),
				"The GL4 basic shader must use the shared shader infrastructure");
		try (var vertex = BasicShader.class.getResourceAsStream("glsl/basic.vert");
				var fragment = BasicShader.class.getResourceAsStream("glsl/basic.frag");
				var keystone = gama.ui.display.opengl4.renderer.shaders.KeystoneShaderProgram.class
						.getResourceAsStream("keystoneVertexShader")) {
			check(vertex != null && fragment != null && keystone != null,
					"Concrete GL4 subclasses must retain class-relative access to their shader resources");
		}
	}

	private static void checkFramebuffers() {
		final List<String> calls = new ArrayList<>();
		final GL2GL3 gl = proxy(GL2GL3.class, (p, method, args) -> {
			final String name = method.getName();
			calls.add(name);
			if (name.startsWith("glGen")) {
				((int[]) args[1])[(int) args[2]] = 1;
			} else if (name.startsWith("glDelete")) {
				check(((int[]) args[1])[(int) args[2]] != -1,
						"Unallocated GL resource names must not be deleted");
			} else if ("glFramebufferTexture2D".equals(name)) {
				check((int) args[2] == GL2ES2.GL_TEXTURE_2D,
						"Render targets must use the common 2D texture attachment API");
			}
			return null;
		});
		final FrameBufferObject fbo = new FrameBufferObject(gl, 100, 100);
		check(calls.contains("glFramebufferTexture2D") && !calls.contains("glFramebufferTextureEXT"),
				"The shared FBO must avoid GL2-only attachment entry points");
		fbo.bindFrameBuffer();
		fbo.unbindCurrentFrameBuffer();
		fbo.setDisplayDimensions(200, 100);
		fbo.cleanUp();
	}

	public static void main(final String[] args) throws Exception {
		final Class<?> renderer = gama.ui.display.opengl4.renderer.JOGLRenderer.class;
		check(renderer.getSuperclass() == gama.ui.display.opengl.renderer.JOGLRenderer.class,
				"GL4 must specialize the regular renderer");
		check(renderer.getMethod("getCameraHelper").getReturnType() == CameraHelper.class,
				"GL4 must reuse the shared camera");
		check(renderer.getMethod("getSceneHelper").getReturnType() == SceneHelper.class,
				"GL4 must reuse the shared scene lifecycle");
		check(gama.ui.display.opengl4.view.SWTOpenGLDisplaySurface.class.getSuperclass()
				== gama.ui.display.opengl.view.SWTOpenGLDisplaySurface.class,
				"GL4 must reuse the shared display surface");
		check(gama.ui.display.opengl4.view.OpenGLDisplayView.class.getSuperclass()
				== gama.ui.display.opengl.view.OpenGLDisplayView.class,
				"GL4 must reuse the shared view lifecycle");
		checkShaders();
		checkFramebuffers();
		System.out.println("Backend specialization checks passed");
	}
}
