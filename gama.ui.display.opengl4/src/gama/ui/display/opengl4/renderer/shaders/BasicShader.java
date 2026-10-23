package gama.ui.display.opengl4.renderer.shaders;

import com.jogamp.opengl.GL4;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * The Class BasicShader.
 *
 * <p>The main shader program used for rendering all opaque geometry in the OpenGL4 display. It
 * supports texturing, per-vertex colour and two-sided ambient/diffuse lighting driven by uniform
 * variables that are updated every frame by {@link gama.ui.display.opengl4.renderer.helpers.LightHelper}.
 *
 * <h3>Vertex attributes (all bound before linking)</h3>
 * <ul>
 *   <li>{@code aPos}      (location 0) – object-space XYZ position.</li>
 *   <li>{@code aColor}    (location 1) – RGBA vertex colour.</li>
 *   <li>{@code aTexCoord} (location 2) – UV texture coordinates.</li>
 *   <li>{@code aNormal}   (location 3) – object-space surface normal.</li>
 * </ul>
 *
 * <h3>Uniforms</h3>
 * <ul>
 *   <li>{@code model}         – 4×4 model-view matrix.</li>
 *   <li>{@code view}          – 4×4 view matrix (identity in current usage).</li>
 *   <li>{@code projection}    – 4×4 projection matrix.</li>
 *   <li>{@code useTexture}    – whether to sample from {@code texture1}.</li>
 *   <li>{@code useLighting}   – whether lighting is active.</li>
 *   <li>{@code ambientColor}  – RGB ambient light colour.</li>
 *   <li>{@code lightCount}    – number of active lights, up to eight like legacy OpenGL.</li>
 *   <li>{@code lights}        – eye-space positions/directions, colours, attenuation and spot cutoffs.</li>
 * </ul>
 */
public class BasicShader extends AbstractShader {

	/** Must match MAX_LIGHTS in glsl/basic.frag. */
	public static final int MAX_LIGHTS = 8;

	/** Uniform location: model matrix. */
	private int location_model;

	/** Uniform location: view matrix. */
	private int location_view;

	/** Uniform location: projection matrix. */
	private int location_projection;

	/** Uniform location: useTexture flag. */
	private int location_useTexture;

	/** Uniform location: premultiplyTexture flag. */
	private int location_premultiplyTexture;

	/** Uniform location: useLighting flag. */
	private int location_useLighting;

	/** Uniform location: ambient light colour (RGB). */
	private int location_ambientColor;

	/** Uniform location: active light count. */
	private int location_lightCount;

	/** Per-light uniform locations: position, color, attenuation, spot direction, spot cutoff. */
	private int[][] location_lights;

	/**
	 * Instantiates a new BasicShader, compiling and linking {@code glsl/basic.vert} and
	 * {@code glsl/basic.frag}.
	 *
	 * @param gl the GL4 context
	 */
	public BasicShader(final GL4 gl) {
		super(gl, "glsl/basic.vert", "glsl/basic.frag");
	}

	@Override
	protected void bindAttributes() {
		bindAttribute(0, "aPos");
		bindAttribute(1, "aColor");
		bindAttribute(2, "aTexCoord");
		bindAttribute(3, "aNormal");
	}

	@Override
	protected void getAllUniformLocations() {
		super.getAllUniformLocations();
		location_model         = getUniformLocation("model");
		location_view          = getUniformLocation("view");
		location_projection    = getUniformLocation("projection");
		location_useTexture    = getUniformLocation("useTexture");
		location_premultiplyTexture = getUniformLocation("premultiplyTexture");
		location_useLighting   = getUniformLocation("useLighting");
		location_ambientColor  = getUniformLocation("ambientColor");
		location_lightCount    = getUniformLocation("lightCount");
		// This method is called by the superclass constructor, before field initializers run.
		location_lights = new int[MAX_LIGHTS][5];
		for (int i = 0; i < MAX_LIGHTS; i++) {
			String prefix = "lights[" + i + "].";
			location_lights[i][0] = getUniformLocation(prefix + "position");
			location_lights[i][1] = getUniformLocation(prefix + "color");
			location_lights[i][2] = getUniformLocation(prefix + "attenuation");
			location_lights[i][3] = getUniformLocation(prefix + "spotDirection");
			location_lights[i][4] = getUniformLocation(prefix + "spotCutoff");
		}
	}

	/**
	 * Loads the model matrix into the {@code model} uniform.
	 *
	 * @param matrix the 4×4 model-view matrix
	 */
	public void loadModelMatrix(final Matrix4f matrix) {
		loadMatrix(location_model, matrix);
	}

	/**
	 * Loads the view matrix into the {@code view} uniform.
	 *
	 * @param matrix the 4×4 view matrix
	 */
	public void loadViewMatrix(final Matrix4f matrix) {
		loadMatrix(location_view, matrix);
	}

	/**
	 * Loads the projection matrix into the {@code projection} uniform.
	 *
	 * @param matrix the 4×4 projection matrix
	 */
	public void loadProjectionMatrix(final Matrix4f matrix) {
		loadMatrix(location_projection, matrix);
	}

	/**
	 * Sets the {@code useTexture} boolean uniform.
	 *
	 * @param useTexture {@code true} to sample from {@code texture1}
	 */
	public void loadUseTexture(final boolean useTexture) {
		gl.glUniform1i(location_useTexture, useTexture ? 1 : 0);
	}

	/**
	 * Sets whether textured fragments output colour premultiplied by the texel alpha only, for use with
	 * {@code GL_ONE, GL_ONE_MINUS_SRC_ALPHA} blending (legacy OpenGL behaviour for textured objects).
	 *
	 * @param premultiply {@code true} to premultiply the textured colour by the texel alpha
	 */
	public void loadPremultiplyTexture(final boolean premultiply) {
		gl.glUniform1i(location_premultiplyTexture, premultiply ? 1 : 0);
	}

	/**
	 * Sets the {@code useLighting} boolean uniform. When {@code false} the fragment shader
	 * outputs the raw vertex/texture colour with no lighting computation.
	 *
	 * @param useLighting {@code true} to enable lighting
	 */
	public void loadUseLighting(final boolean useLighting) {
		gl.glUniform1i(location_useLighting, useLighting ? 1 : 0);
	}

	/**
	 * Loads the ambient light colour into the {@code ambientColor} uniform.
	 *
	 * @param r red component   [0..1]
	 * @param g green component [0..1]
	 * @param b blue component  [0..1]
	 */
	public void loadAmbientColor(final float r, final float g, final float b) {
		gl.glUniform3f(location_ambientColor, r, g, b);
	}

	/**
	 * Loads the number of active lights. Zero leaves only ambient illumination.
	 */
	public void loadLightCount(final int count) {
		gl.glUniform1i(location_lightCount, count);
	}

	/**
	 * Loads a light in eye space, matching the combined model-view transform used for geometry.
	 *
	 * @param index light slot
	 * @param position position (w=1) or direction towards a directional light (w=0)
	 * @param color RGB intensity
	 * @param attenuation constant, linear and quadratic attenuation
	 * @param spotDirection direction away from a spotlight
	 * @param spotCutoff cosine of the spotlight cutoff, or -1 for other lights
	 */
	public void loadLight(final int index, final Vector4f position, final Vector3f color,
			final Vector3f attenuation, final Vector3f spotDirection, final float spotCutoff) {
		int[] locations = location_lights[index];
		gl.glUniform4f(locations[0], position.x, position.y, position.z, position.w);
		gl.glUniform3f(locations[1], color.x, color.y, color.z);
		gl.glUniform3f(locations[2], attenuation.x, attenuation.y, attenuation.z);
		gl.glUniform3f(locations[3], spotDirection.x, spotDirection.y, spotDirection.z);
		gl.glUniform1f(locations[4], spotCutoff);
	}

	/**
	 * Uploads a 4×4 column-major matrix to the given uniform location.
	 *
	 * @param location the uniform location
	 * @param matrix   the JOML {@link Matrix4f}
	 */
	protected void loadMatrix(final int location, final Matrix4f matrix) {
		float[] matArray = new float[16];
		matrix.get(matArray);
		gl.glUniformMatrix4fv(location, 1, false, matArray, 0);
	}

	@Override
	public boolean useNormal() {
		return true;
	}

	@Override
	public boolean useTexture() {
		return true;
	}

	@Override
	public int getTextureID() {
		return 0; // Handled directly if needed
	}
}
