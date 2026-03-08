package gama.ui.display.opengl4.renderer.shaders;

import com.jogamp.opengl.GL4;
import java.nio.FloatBuffer;
import org.joml.Matrix4f;

public class BasicShader extends AbstractShader {

	private int location_model;
	private int location_view;
	private int location_projection;
	private int location_useTexture;

	public BasicShader(GL4 gl) {
		super(gl, "glsl/basic.vert", "glsl/basic.frag");
	}

	@Override
	protected void bindAttributes() {
		bindAttribute(0, "aPos");
		bindAttribute(1, "aColor");
		bindAttribute(2, "aTexCoord");
	}

	@Override
	protected void getAllUniformLocations() {
		super.getAllUniformLocations();
		location_model = getUniformLocation("model");
		location_view = getUniformLocation("view");
		location_projection = getUniformLocation("projection");
		location_useTexture = getUniformLocation("useTexture");
	}

	public void loadModelMatrix(Matrix4f matrix) {
		loadMatrix(location_model, matrix);
	}

	public void loadViewMatrix(Matrix4f matrix) {
		loadMatrix(location_view, matrix);
	}

	public void loadProjectionMatrix(Matrix4f matrix) {
		loadMatrix(location_projection, matrix);
	}

	public void loadUseTexture(boolean useTexture) {
		gl.glUniform1i(location_useTexture, useTexture ? 1 : 0);
	}

	protected void loadMatrix(int location, Matrix4f matrix) {
		float[] matArray = new float[16];
		matrix.get(matArray);
		gl.glUniformMatrix4fv(location, 1, false, matArray, 0);
	}

	@Override
	public boolean useNormal() {
		return false;
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
