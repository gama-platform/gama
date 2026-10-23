/*******************************************************************************************************
 *
 * LightHelper.java, in gama.ui.display.opengl4, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl4.renderer.helpers;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.jogamp.opengl.GL;

import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.ui.layers.ILightDefinition;
import gama.dev.DEBUG;
import gama.ui.display.opengl4.OpenGL;
import gama.ui.display.opengl4.renderer.IOpenGLRenderer;
import gama.ui.display.opengl4.renderer.shaders.BasicShader;

/**
 * The Class LightHelper.
 */
public class LightHelper extends AbstractRendererHelper {

	/**
	 * Instantiates a new light helper.
	 *
	 * @param renderer
	 *            the renderer
	 */
	public LightHelper(final IOpenGLRenderer renderer) {
		super(renderer);
	}

	/**
	 * Sets the ambient light. Pushes the ambient colour to the BasicShader's {@code ambientColor} uniform.
	 *
	 * @param light
	 *            the ambient light definition
	 */
	public void setAmbientLight(final ILightDefinition light) {
		IColor c = !light.isActive() ? GamaColorFactory.BLACK : light.getIntensity();
		final float r = c.red() / 255.0f;
		final float g = c.green() / 255.0f;
		final float b = c.blue() / 255.0f;
		BasicShader shader = getOpenGL().getBasicShader();
		if (shader != null) {
			shader.start();
			shader.loadAmbientColor(r, g, b);
			shader.stop();
		}
	}

	@Override
	public void initialize() {
		setAmbientLight(data.getLights().get(ILightDefinition.ambient));
	}

	/**
	 * Updates all active lights in eye space, before any layer or object transform is applied.
	 *
	 * @param openGL
	 *            the open GL
	 */
	public void updateDiffuseLightValue(final OpenGL openGL) {
		setAmbientLight(data.getLights().get(ILightDefinition.ambient));
		final double size = getMaxEnvDim() / 20;
		final double worldWidth = getRenderer().getEnvWidth();
		final double worldHeight = getRenderer().getEnvHeight();

		BasicShader shader = openGL.getBasicShader();
		if (shader != null) {
			Matrix4f view = new Matrix4f(openGL.getModelViewMatrix());
			shader.start();
			try {
				int count = 0;
				for (ILightDefinition light : data.getLights().values()) {
					if (ILightDefinition.ambient.equals(light.getName()) || !light.isActive()) continue;
					if (count == BasicShader.MAX_LIGHTS) {
						DEBUG.ERR("OpenGL4 supports at most " + BasicShader.MAX_LIGHTS + " active non-ambient lights");
						break;
					}
					boolean directional = ILightDefinition.direction.equals(light.getType());
					IPoint p = directional ? light.getDirection() : light.getLocation();
					Vector4f position = directional
							? new Vector4f(-(float) p.getX(), (float) p.getY(), -(float) p.getZ(), 0)
							: new Vector4f((float) p.getX(), -(float) p.getY(), (float) p.getZ(), 1);
					view.transform(position);
					IPoint direction = light.getDirection();
					Vector3f spotDirection = new Vector3f((float) direction.getX(), -(float) direction.getY(),
							(float) direction.getZ());
					view.transformDirection(spotDirection);
					IColor c = light.getIntensity();
					Vector3f color = new Vector3f(c.red() / 255f, c.green() / 255f, c.blue() / 255f);
					Vector3f attenuation = new Vector3f((float) light.getConstantAttenuation(),
							(float) light.getLinearAttenuation(), (float) light.getQuadraticAttenuation());
					float cutoff = ILightDefinition.spot.equals(light.getType()) && light.getAngle() != 180
							? (float) Math.cos(Math.toRadians(light.getAngle())) : -1f;
					shader.loadLight(count++, position, color, attenuation, spotDirection, cutoff);
				}
				shader.loadLightCount(count);
			} finally {
				shader.stop();
			}
		}

		for (ILightDefinition light : data.getLights().values()) {
			if (!ILightDefinition.ambient.equals(light.getName()) && light.isActive() && light.isDrawing()) {
				final boolean previous = openGL.setObjectLighting(false);
				try {
					IPoint p = light.getLocation();
					drawLight(openGL, size, worldWidth, worldHeight, light,
							new float[] { (float) p.getX(), -(float) p.getY(), (float) p.getZ(), 1 });
				} finally {
					openGL.setObjectLighting(previous);
				}
			}
		}
	}

	/** The Constant UP_VECTOR. */
	private final static IPoint UP_VECTOR_PLUS_Y = GamaPointFactory.createImmutable(0, 1, 0);

	/** The Constant UP_VECTOR_MINUS_Z. */
	private final static IPoint UP_VECTOR_MINUS_Z = GamaPointFactory.createImmutable(0, 0, -1);

	/** The Constant UP_VECTOR_PLUS_Z. */
	private final static IPoint UP_VECTOR_PLUS_Z = GamaPointFactory.createImmutable(0, 0, 1);

	/** The Constant arrow. */
	// private final static GamaShape SPOT = ((GamaShape) buildCone3D(2, 1, GamaPoint.Immutable.NULL_POINT))
	// .withRotation(new AxisAngle(UP_VECTOR_PLUS_Y, 90));

	/**
	 * Draw light.
	 *
	 * @param openGL
	 *            the open GL
	 * @param size
	 *            the size
	 * @param worldWidth
	 *            the world width
	 * @param worldHeight
	 *            the world height
	 * @param light
	 *            the light properties
	 * @param pos
	 *            the light position
	 */
	private void drawLight(final OpenGL openGL, final double size, final double worldWidth, final double worldHeight,
			final ILightDefinition light, final float[] pos) {

		final IColor currentColor = openGL.swapCurrentColor(light.getIntensity());
		IPoint dir = light.getDirection().normalized();
		final String type = light.getType();
		if (ILightDefinition.point.equals(type)) {
			// Draw a sphere at the light position using the cached VBO geometry
			openGL.pushMatrix();
			openGL.translateBy(pos[0], pos[1], pos[2]);
			openGL.scaleBy(size, size, size);
			openGL.drawCachedGeometry(gama.api.types.geometry.IShape.Type.SPHERE, null);
			openGL.popMatrix();
		} else if (ILightDefinition.spot.equals(type)) {
			// Draw a cone oriented along the spot direction using the cached VBO geometry
			openGL.pushMatrix();
			dir = GamaPointFactory.create(dir.getX(), -dir.getY(), -dir.getZ());
			double coneRadius = Math.sin(Math.toRadians(light.getAngle())) * size;
			openGL.translateBy(pos[0], pos[1], pos[2]);
			if (dir.norm() > 1e-6) {
				IPoint rotationAxis = UP_VECTOR_PLUS_Z.crossProductWith(dir);
				double rotationAngle = Math.acos(dir.dotProductWith(UP_VECTOR_PLUS_Z));
				if (rotationAxis.getX() != 0 || rotationAxis.getY() != 0) {
					openGL.rotateBy(-Math.toDegrees(rotationAngle), rotationAxis.getX(), rotationAxis.getY(),
							rotationAxis.getZ());
				}
			}
			openGL.scaleBy(coneRadius, coneRadius, size);
			openGL.drawCachedGeometry(gama.api.types.geometry.IShape.Type.CONE, null);
			openGL.popMatrix();
		} else {
			// Directional light: draw a grid of lines with a small sphere at each line end
			final int maxI = 3;
			final int maxJ = 3;
			for (int i = 0; i < maxI; i++) {
				for (int j = 0; j < maxJ; j++) {
					final double[] beginPoint = { i * worldWidth / maxI, -j * worldHeight / maxJ, size * 10 };
					final double[] endPoint = { i * worldWidth / maxI + dir.getX() * size * 3,
							-(j * worldHeight / maxJ) - dir.getY() * size * 3, size * 10 + dir.getZ() * size * 3 };
					openGL.beginDrawing(GL.GL_LINES);
					openGL.drawVertex(0, beginPoint[0], beginPoint[1], beginPoint[2]);
					openGL.drawVertex(0, endPoint[0], endPoint[1], endPoint[2]);
					openGL.endDrawing();
					// Small sphere at the end of each line
					openGL.pushMatrix();
					openGL.translateBy(endPoint[0], endPoint[1], endPoint[2]);
					openGL.scaleBy(size / 5, size / 5, size / 5);
					openGL.drawCachedGeometry(gama.api.types.geometry.IShape.Type.SPHERE, null);
					openGL.popMatrix();
				}
			}
		}
		openGL.setCurrentColor(currentColor);
	}

	/**
	 * Draw.
	 */
	public void draw() {
		if (isActive()) {
			final OpenGL openGL = getOpenGL();
			openGL.pushMatrix();
			updateDiffuseLightValue(openGL);
			openGL.popMatrix();
		}
	}

	/**
	 * Checks if is active.
	 *
	 * @return true, if is active
	 */
	public boolean isActive() { return getData().isLightOn(); }

}
