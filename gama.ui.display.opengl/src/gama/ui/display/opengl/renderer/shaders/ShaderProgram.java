/*******************************************************************************************************
 *
 * ShaderProgram.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.renderer.shaders;

import com.jogamp.opengl.GL3;
import gama.dev.DEBUG;

import java.nio.FloatBuffer;

public class ShaderProgram {
	private int programId;
	private int vertexShaderId;
	private int fragmentShaderId;

	private final FloatBuffer matrixBuffer = com.jogamp.common.nio.Buffers.newDirectFloatBuffer(16);

	public ShaderProgram(GL3 gl) throws Exception {

	programId = gl.glCreateProgram();

		if (programId == 0) {
			throw new Exception("Could not create Shader");
		}
	}

	public void createVertexShader(GL3 gl, String shaderCode) throws Exception {
		vertexShaderId = createShader(gl, shaderCode, GL3.GL_VERTEX_SHADER);
	}

	public void createFragmentShader(GL3 gl, String shaderCode) throws Exception {
		fragmentShaderId = createShader(gl, shaderCode, GL3.GL_FRAGMENT_SHADER);
	}

	protected int createShader(GL3 gl, String shaderCode, int shaderType) throws Exception {

	int shaderId = gl.glCreateShader(shaderType);

		if (shaderId == 0) {
			throw new Exception("Error creating shader. Type: " + shaderType);
		}

		gl.glShaderSource(shaderId, 1, new String[] { shaderCode }, null);
		gl.glCompileShader(shaderId);

		int[] compiled = new int[1];
		gl.glGetShaderiv(shaderId, GL3.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == 0) {
			int[] logLength = new int[1];
			gl.glGetShaderiv(shaderId, GL3.GL_INFO_LOG_LENGTH, logLength, 0);

			byte[] log = new byte[logLength[0]];
			gl.glGetShaderInfoLog(shaderId, logLength[0], (int[]) null, 0, log, 0);
			throw new Exception("Error compiling Shader code: " + new String(log));
		}

		gl.glAttachShader(programId, shaderId);

		return shaderId;
	}

	public void link(GL3 gl) throws Exception {
		gl.glLinkProgram(programId);
		int[] linked = new int[1];
		gl.glGetProgramiv(programId, GL3.GL_LINK_STATUS, linked, 0);
		if (linked[0] == 0) {
			int[] logLength = new int[1];
			gl.glGetProgramiv(programId, GL3.GL_INFO_LOG_LENGTH, logLength, 0);

			byte[] log = new byte[logLength[0]];
			gl.glGetProgramInfoLog(programId, logLength[0], (int[]) null, 0, log, 0);
			throw new Exception("Error linking Shader code: " + new String(log));
		}

		if (vertexShaderId != 0) {
			gl.glDetachShader(programId, vertexShaderId);
			gl.glDeleteShader(vertexShaderId);
		}
		if (fragmentShaderId != 0) {
			gl.glDetachShader(programId, fragmentShaderId);
			gl.glDeleteShader(fragmentShaderId);
		}

		gl.glValidateProgram(programId);
		int[] validated = new int[1];
		gl.glGetProgramiv(programId, GL3.GL_VALIDATE_STATUS, validated, 0);
		if (validated[0] == 0) {
			System.err.println("Warning validating Shader code");
		}
	}

	public void bind(GL3 gl) {
		gl.glUseProgram(programId);
	}

	public void unbind(GL3 gl) {
		gl.glUseProgram(0);
	}

	public void cleanup(GL3 gl) {
		unbind(gl);
		if (programId != 0) {
			gl.glDeleteProgram(programId);
		}
	}

	public int getUniformLocation(GL3 gl, String uniformName) throws Exception {
		int loc = gl.glGetUniformLocation(programId, uniformName);
		if (loc < 0) {
			// Do not throw Exception, just log warning
			// throw new Exception("Could not find uniform:" + uniformName);
			System.err.println("Warning: Could not find uniform: " + uniformName);
		}
		return loc;
	}

	public void setUniform(GL3 gl, int location, org.joml.Matrix4f value) {
		matrixBuffer.clear();
		value.get(matrixBuffer);
		gl.glUniformMatrix4fv(location, 1, false, matrixBuffer);
	}

	public void setUniform(GL3 gl, int location, int value) {
		gl.glUniform1i(location, value);
	}

	public void setUniform(GL3 gl, int location, float x, float y, float z, float w) {
		gl.glUniform4f(location, x, y, z, w);
	}
}
