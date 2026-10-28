package gama.ui.display.opengl4.renderer.shaders;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLProfile;

/**
 * Standalone GPU regression checks. Run with this bundle's runtime classpath and shader resources,
 * -Djava.awt.headless=true and --enable-native-access=ALL-UNNAMED (without -XstartOnFirstThread).
 */
public final class LightingRegression {

	private final GL4 gl;
	private final BasicShader shader;
	private final Matrix4f view = new Matrix4f();
	private final Vector3f attenuation = new Vector3f(1, 0, 0);
	private final Vector3f spotDirection = new Vector3f(0, 0, -1);

	private LightingRegression(final GL4 gl) {
		this.gl = gl;
		shader = new BasicShader(gl);
		int[] linked = new int[1];
		gl.glGetProgramiv(shader.getProgramID(), GL4.GL_LINK_STATUS, linked, 0);
		if (linked[0] == 0) throw new AssertionError("Lighting shader failed to link");
		int[] objects = new int[1];
		gl.glGenVertexArrays(1, objects, 0);
		gl.glBindVertexArray(objects[0]);
		gl.glGenBuffers(1, objects, 0);
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, objects[0]);
		FloatBuffer vertices = Buffers.newDirectFloatBuffer(new float[] { -1, -1, 0, 3, -1, 0, -1, 3, 0 });
		gl.glBufferData(GL.GL_ARRAY_BUFFER, vertices.remaining() * Float.BYTES, vertices, GL.GL_STATIC_DRAW);
		gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0L);
		gl.glEnableVertexAttribArray(0);
		gl.glVertexAttrib4f(1, 1, 1, 1, 1);
		gl.glVertexAttrib2f(2, 0, 0);
		gl.glVertexAttrib3f(3, 0, 0, 1);
		gl.glViewport(0, 0, 1, 1);
		gl.glDisable(GL.GL_BLEND);
		gl.glDisable(GL.GL_DITHER);
		gl.glDisable(GL.GL_DEPTH_TEST);
		gl.glDisable(GL.GL_CULL_FACE);
		shader.start();
		shader.loadViewMatrix(new Matrix4f());
		shader.loadUseTexture(false);
		shader.loadUseLighting(true);
		shader.loadAmbientColor(0, 0, 0);
		matrices();
	}

	private void matrices() {
		shader.loadModelMatrix(view);
		// Cancel the camera transform only for rasterization, not for lighting.
		shader.loadProjectionMatrix(new Matrix4f(view).invert());
	}

	private void light(final int index, final Vector4f position, final Vector3f color, final float cutoff) {
		shader.loadLight(index, view.transform(new Vector4f(position)), color, attenuation,
				view.transformDirection(new Vector3f(spotDirection)), cutoff);
	}

	private void pixel(final String name, final float r, final float g, final float b, final float a) {
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glDrawArrays(GL.GL_TRIANGLES, 0, 3);
		FloatBuffer pixel = Buffers.newDirectFloatBuffer(4);
		gl.glReadPixels(0, 0, 1, 1, GL.GL_RGBA, GL.GL_FLOAT, pixel);
		float[] expected = { r, g, b, a };
		for (int i = 0; i < expected.length; i++) {
			if (Math.abs(pixel.get(i) - expected[i]) > 0.012f) {
				throw new AssertionError(name + ": channel " + i + " expected " + expected[i]
						+ ", got " + pixel.get(i));
			}
		}
		int error = gl.glGetError();
		if (error != GL.GL_NO_ERROR) throw new AssertionError(name + ": OpenGL error " + error);
		System.out.println("PASS " + name);
	}

	private void run() {
		Vector4f sun = new Vector4f(0, 0, 1, 0);
		Vector3f gray = new Vector3f(0.5f);
		shader.loadLightCount(0);
		pixel("no fallback diffuse light", 0, 0, 0, 1);
		shader.loadAmbientColor(0.2f, 0.3f, 0.4f);
		pixel("ambient only", 0.2f, 0.3f, 0.4f, 1);
		shader.loadAmbientColor(0, 0, 0);
		shader.loadLightCount(1);
		light(0, sun, gray, -1);
		pixel("directional diffuse without specular", 0.5f, 0.5f, 0.5f, 1);

		view.translation(17, -28, -50).rotateY(1.1f).rotateX(0.7f);
		matrices();
		light(0, sun, gray, -1);
		pixel("camera rotation and translation preserve diffuse", 0.5f, 0.5f, 0.5f, 1);

		Matrix4f objectTransform = new Matrix4f(view).rotateY(0.6f).scale(2, 3, 4);
		shader.loadModelMatrix(objectTransform);
		shader.loadProjectionMatrix(new Matrix4f(objectTransform).invert());
		float rotatedDiffuse = 0.5f * (float) Math.cos(0.6);
		pixel("object rotation and nonuniform scaling", rotatedDiffuse, rotatedDiffuse, rotatedDiffuse, 1);

		view.identity().lookAt(-34.826f, -115.0892f, 54.4789f, 50, -50, 0, 0, 0, 1);
		matrices();
		shader.loadAmbientColor(100 / 255f, 100 / 255f, 100 / 255f);
		light(0, new Vector4f(-0.5f, 0.5f, 1, 0), new Vector3f(160 / 255f), -1);
		float waterLight = (100 + 160 / (float) Math.sqrt(1.5)) / 255;
		pixel("Moving 3D Object camera and default light", waterLight, waterLight, waterLight, 1);
		shader.loadAmbientColor(0, 0, 0);
		view.identity();
		matrices();
		light(0, sun, gray, -1);

		gl.glFrontFace(GL.GL_CW);
		gl.glVertexAttrib3f(3, 0, 0, -1);
		pixel("back faces use reversed normals", 0.5f, 0.5f, 0.5f, 1);
		gl.glFrontFace(GL.GL_CCW);
		gl.glVertexAttrib3f(3, 0, 0, 1);

		shader.loadLightCount(BasicShader.MAX_LIGHTS);
		for (int i = 0; i < BasicShader.MAX_LIGHTS; i++) {
			light(i, sun, new Vector3f(0.1f, 0.05f, 0.025f), -1);
		}
		pixel("all eight lights contribute", 0.8f, 0.4f, 0.2f, 1);
		shader.loadLightCount(0);
		pixel("disabling lights clears previous contributions", 0, 0, 0, 1);

		shader.loadLightCount(1);
		Vector4f point = new Vector4f(0, 0, 2, 1);
		attenuation.set(1, 0.5f, 0.25f);
		light(0, point, new Vector3f(0.6f), -1);
		pixel("point distance attenuation", 0.2f, 0.2f, 0.2f, 1);
		view.translation(17, -28, -50).rotateY(1.1f).rotateX(0.7f);
		matrices();
		light(0, point, new Vector3f(0.6f), -1);
		pixel("point attenuation survives camera transforms", 0.2f, 0.2f, 0.2f, 1);
		view.identity();
		matrices();
		attenuation.set(1, 0, 0);

		float cutoff = (float) Math.cos(Math.toRadians(30));
		light(0, point, gray, cutoff);
		pixel("inside spotlight cone", 0.5f, 0.5f, 0.5f, 1);
		view.translation(17, -28, -50).rotateY(1.1f).rotateX(0.7f);
		matrices();
		light(0, point, gray, cutoff);
		pixel("spotlight direction survives camera transforms", 0.5f, 0.5f, 0.5f, 1);
		view.identity();
		matrices();
		spotDirection.set(1, 0, 0);
		light(0, point, gray, cutoff);
		pixel("outside spotlight cone", 0, 0, 0, 1);
		light(0, point, gray, -1);
		pixel("point light ignores spotlight direction", 0.5f, 0.5f, 0.5f, 1);

		int[] texture = new int[1];
		gl.glGenTextures(1, texture, 0);
		gl.glActiveTexture(GL.GL_TEXTURE0);
		gl.glBindTexture(GL.GL_TEXTURE_2D, texture[0]);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		ByteBuffer texel = Buffers.newDirectByteBuffer(new byte[] { 64, (byte) 128, (byte) 192, (byte) 128 });
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, 1, 1, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, texel);
		shader.loadUseTexture(true);
		shader.loadAmbientColor(1, 1, 1);
		light(0, sun, new Vector3f(1), -1);
		pixel("lighting saturates before texture modulation", 64 / 255f, 128 / 255f, 192 / 255f, 128 / 255f);
		gl.glVertexAttrib4f(1, 0.25f, 0.5f, 0.75f, 0.5f);
		pixel("material tint and alpha preserved", 32 / 255f, 128 / 255f, 192 / 255f, 64 / 255f);
		shader.loadUseLighting(false);
		pixel("unlit textured objects unchanged", 16 / 255f, 64 / 255f, 144 / 255f, 64 / 255f);

		shader.loadPremultiplyTexture(true);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glClearColor(0.2f, 0.3f, 0.1f, 1);
		gl.glVertexAttrib4f(1, 1, 1, 1, 1);
		texel = Buffers.newDirectByteBuffer(new byte[] { (byte) 255, (byte) 255, (byte) 255, 0 });
		gl.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, 1, 1, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, texel);
		pixel("transparent white sprite pixels leave terrain unchanged", 0.2f, 0.3f, 0.1f, 1);
		texel = Buffers.newDirectByteBuffer(new byte[] { (byte) 255, (byte) 255, (byte) 255, 1 });
		gl.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, 1, 1, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, texel);
		pixel("almost transparent edges do not become white spots",
				0.2f + 0.8f / 255, 0.3f + 0.7f / 255, 0.1f + 0.9f / 255, 1);
		texel = Buffers.newDirectByteBuffer(new byte[] { 64, (byte) 128, (byte) 192, (byte) 255 });
		gl.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, 1, 1, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, texel);
		gl.glClearColor(0, 0, 0, 1);
		gl.glVertexAttrib4f(1, 1, 1, 1, 0.6f);
		pixel("layer transparency does not dim opaque textures (legacy parity)",
				64 / 255f, 128 / 255f, 192 / 255f, 1);
		shader.loadPremultiplyTexture(false);
		gl.glDisable(GL.GL_BLEND);
		gl.glDeleteTextures(1, texture, 0);
		shader.stop();
	}

	public static void main(final String[] args) {
		GLProfile profile = GLProfile.get(GLProfile.GL4);
		GLCapabilities caps = new GLCapabilities(profile);
		caps.setAlphaBits(8);
		var drawable = GLDrawableFactory.getFactory(profile).createOffscreenAutoDrawable(null, caps, null, 1, 1);
		try {
			drawable.display();
			drawable.invoke(true, d -> {
				new LightingRegression(d.getGL().getGL4()).run();
				return true;
			});
		} finally {
			drawable.destroy();
		}
	}
}
