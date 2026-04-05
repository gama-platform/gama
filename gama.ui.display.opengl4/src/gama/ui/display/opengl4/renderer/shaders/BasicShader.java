package gama.ui.display.opengl4.renderer.shaders;

import com.jogamp.opengl.GL4;
import org.joml.Matrix4f;

/**
 * The Class BasicShader.
 *
 * <p>The main shader program used for rendering all opaque geometry in the OpenGL4 display. It
 * supports texturing, per-vertex colour and Phong (Blinn-Phong) lighting driven by uniform
 * variables that are updated every frame by {@link gama.ui.display.opengl4.renderer.helpers.LightHelper}.
 *
 * <h3>Vertex attributes (all bound before linking)</h3>
 * <ul>
 *   <li>{@code aPos}      (location 0) – world-space XYZ position.</li>
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
 *   <li>{@code useLighting}   – whether Phong lighting is active.</li>
 *   <li>{@code ambientColor}  – RGB ambient light colour.</li>
 *   <li>{@code lightPosition} – world-space position of the primary light.</li>
 *   <li>{@code lightColor}    – RGB colour of the primary light.</li>
 *   <li>{@code viewPos}       – world-space camera position for specular computation.</li>
 *   <li>{@code shininess}     – Blinn-Phong specular shininess exponent.</li>
 * </ul>
 */
public class BasicShader extends AbstractShader {

	/** Uniform location: model matrix. */
	private int location_model;

	/** Uniform location: view matrix. */
	private int location_view;

	/** Uniform location: projection matrix. */
	private int location_projection;

	/** Uniform location: useTexture flag. */
	private int location_useTexture;

	/** Uniform location: useLighting flag. */
	private int location_useLighting;

	/** Uniform location: ambient light colour (RGB). */
	private int location_ambientColor;

	/** Uniform location: primary light world-space position. */
	private int location_lightPosition;

	/** Uniform location: primary light RGB colour. */
	private int location_lightColor;

	/** Uniform location: camera world-space position. */
	private int location_viewPos;

	/** Uniform location: Blinn-Phong shininess exponent. */
	private int location_shininess;

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
		location_useLighting   = getUniformLocation("useLighting");
		location_ambientColor  = getUniformLocation("ambientColor");
		location_lightPosition = getUniformLocation("lightPosition");
		location_lightColor    = getUniformLocation("lightColor");
		location_viewPos       = getUniformLocation("viewPos");
		location_shininess     = getUniformLocation("shininess");
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
	 * Sets the {@code useLighting} boolean uniform. When {@code false} the fragment shader
	 * outputs the raw vertex/texture colour with no lighting computation.
	 *
	 * @param useLighting {@code true} to enable Phong lighting
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
	 * Loads the primary light world-space position into the {@code lightPosition} uniform.
	 *
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 */
	public void loadLightPosition(final float x, final float y, final float z) {
		gl.glUniform3f(location_lightPosition, x, y, z);
	}

	/**
	 * Loads the primary light colour into the {@code lightColor} uniform.
	 *
	 * @param r red component   [0..1]
	 * @param g green component [0..1]
	 * @param b blue component  [0..1]
	 */
	public void loadLightColor(final float r, final float g, final float b) {
		gl.glUniform3f(location_lightColor, r, g, b);
	}

	/**
	 * Loads the camera world-space position into the {@code viewPos} uniform (used for
	 * specular reflection computation).
	 *
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 */
	public void loadViewPos(final float x, final float y, final float z) {
		gl.glUniform3f(location_viewPos, x, y, z);
	}

	/**
	 * Loads the Blinn-Phong shininess exponent into the {@code shininess} uniform.
	 *
	 * @param shininess exponent (positive float, typically 8–256)
	 */
	public void loadShininess(final float shininess) {
		gl.glUniform1f(location_shininess, shininess);
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
