/*******************************************************************************************************
 *
 * ObjFileDrawer.java, in gama.ui.display.opengl4, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl4.files;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.util.texture.Texture;

import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.runtime.scope.IScope;
import gama.api.utils.geometry.GamaCoordinateSequenceFactory;
import gama.api.utils.geometry.ICoordinates;
import gama.core.util.file.GamaObjFile;
import gama.extension.image.GamaImageFile;
import gama.ui.display.opengl4.OpenGL;

/**
 * The Class ObjFileDrawer.
 */
public class ObjFileDrawer implements Runnable {

	private static final int MAX_BATCH_VERTICES = 12000;
	private static final int[] QUAD_TRIANGLES = { 0, 1, 2, 0, 2, 3 };
	private final GamaObjFile file;
	private final OpenGL gl;
	private final IScope scope;
	private long contentsRevision;
	private final List<OpenGL.StoredDrawing> drawings = new ArrayList<>();
	private final Map<String, Material> materials = new HashMap<>();
	private List<Runnable> filledCommands;
	private List<Runnable> wireframeCommands;

	public ObjFileDrawer(final GamaObjFile file, final OpenGL gl, final IScope scope) {
		this.file = file;
		this.gl = gl;
		this.scope = scope;
		contentsRevision = file.getContentsRevision();
	}

	@Override
	public void run() {
		if (contentsRevision != file.getContentsRevision()) {
			dispose();
			filledCommands = null;
			wireframeCommands = null;
			materials.clear();
			file.loadObject(scope, true);
			contentsRevision = file.getContentsRevision();
		}
		final boolean wireframe = gl.isWireframe();
		List<Runnable> commands = wireframe ? wireframeCommands : filledCommands;
		if (commands == null) {
			commands = new ArrayList<>();
			final int firstDrawing = drawings.size();
			try {
				drawToOpenGL(file, gl, commands, !wireframe);
			} catch (RuntimeException | Error e) {
				while (drawings.size() > firstDrawing) { drawings.remove(drawings.size() - 1).dispose(); }
				throw e;
			}
			if (wireframe) { wireframeCommands = commands; } else { filledCommands = commands; }
		} else {
			for (final Runnable command : commands) { command.run(); }
		}
	}

	public void dispose() {
		for (final OpenGL.StoredDrawing drawing : drawings) { drawing.dispose(); }
		drawings.clear();
	}

	/**
	 * Helper class to encapsulate reusable drawing objects
	 */
	private static class DrawContext {
		final OpenGL gl;
		final GamaObjFile file;
		final IPoint tex = GamaPointFactory.create();
		final IPoint normal = GamaPointFactory.create();
		final IPoint vertex = GamaPointFactory.create();

		DrawContext(GamaObjFile file, OpenGL gl) {
			this.file = file;
			this.gl = gl;
		}
	}

	/**
	 * Handle material loading and texturing
	 */
	private final class Material implements Runnable {
		private final float red, green, blue, alpha;
		private final GamaImageFile image;

		Material(final String name) {
			final float[] color = file.materials.getKd(name);
			red = color[0];
			green = color[1];
			blue = color[2];
			alpha = file.materials.getd(name);
			final String mapKa = file.materials.getMapKa(name);
			final String mapKd = file.materials.getMapKd(name);
			final String mapd = file.materials.getMapd(name);
			final String map = mapd != null ? mapd : mapKa != null ? mapKa : mapKd;
			if (map == null) {
				image = null;
			} else {
				final File f = new File(file.mtlPath);
				final String path = f.getAbsolutePath().replace(f.getName(), "") + map;
				image = new GamaImageFile(null, path);
			}
		}

		@Override
		public void run() {
			gl.setCurrentColor(red, green, blue, alpha);
			if (image != null && image.exists(null)) {
				final Texture texture = gl.getTexture(image, false, true);
				gl.setCurrentTextures(texture.getTextureObject(), texture.getTextureObject());
				texture.setTexParameteri(gl.getGL(), GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
				texture.setTexParameteri(gl.getGL(), GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
			}
		}
	}

	/**
	 * Helper method to draw a single face of the object
	 */
	private static void drawFaceVertices(final DrawContext ctx, final int faceIndex, final boolean triangulateQuad) {
		final int[] tempfaces = ctx.file.faces.get(faceIndex);
		final int[] norms = ctx.file.facesNorms.get(faceIndex);
		final int[] texs = ctx.file.facesTexs.get(faceIndex);

		boolean hasNormals = true;
		for (int w = 0; w < tempfaces.length; w++) {
			if (norms[w] == 0) {
				hasNormals = false;
				break;
			}
		}

		if (!hasNormals) {
			final double[] arrayOfVertices = new double[tempfaces.length * 3];
			for (int w = 0; w < tempfaces.length; w++) {
				final double[] ordinates = ctx.file.setOfVertex.get(tempfaces[w] - 1);
				for (int k = 0; k < 3; k++) { arrayOfVertices[w * 3 + k] = ordinates[k]; }
			}
			final ICoordinates coords = GamaCoordinateSequenceFactory.ofLength(tempfaces.length + 1);
			coords.setTo(arrayOfVertices);
			coords.completeRing();
			ctx.gl.setNormal(coords, !coords.isClockwise());
		}
		for (int corner = 0; corner < (triangulateQuad ? 6 : tempfaces.length); corner++) {
			final int w = triangulateQuad ? QUAD_TRIANGLES[corner] : corner;
			if (tempfaces[w] == 0) { continue; }
			final boolean hasNormal = norms[w] != 0;
			final boolean hasTex = texs[w] != 0;
			if (hasNormal) {
				final double[] temp_coords = ctx.file.setOfVertexNormals.get(norms[w] - 1);
				ctx.normal.setLocation(temp_coords[0], temp_coords[1], temp_coords[2]);
			}
			if (hasTex) {
				final double[] ordinates = ctx.file.setOfVertexTextures.get(texs[w] - 1);
				ctx.tex.setLocation(ordinates[0], ordinates[1], ordinates[2]);
				if (1d >= ctx.tex.getY() && -ctx.tex.getY() <= 0) {
					ctx.tex.setY(1d - ctx.tex.getY());
				} else {
					ctx.tex.setY(Math.abs(ctx.tex.getY()));
				}
			}
			final double[] temp_coords = ctx.file.setOfVertex.get(tempfaces[w] - 1);
			ctx.vertex.setLocation(temp_coords[0], temp_coords[1], temp_coords[2]);
			ctx.gl.drawVertex(ctx.vertex, hasNormal ? ctx.normal : null, hasTex ? ctx.tex : null);
		}
	}

	private static int triangleTextureKind(final GamaObjFile file, final int index, final boolean triangulateQuads) {
		final int size = file.faces.get(index).length;
		if (size != 3 && !(triangulateQuads && size == 4)) return -1;
		if (size == 4) {
			final int[] normals = file.facesNorms.get(index);
			final boolean normalPresent = normals[0] != 0;
			for (int i = 1; i < size; i++) {
				if ((normals[i] != 0) != normalPresent) return -1;
			}
		}
		final int[] tex = file.facesTexs.get(index);
		final boolean present = tex[0] != 0;
		for (int i = 1; i < size; i++) {
			if ((tex[i] != 0) != present) return -1;
		}
		return present ? 1 : 0;
	}

	/**
	 * Draw to open GL.
	 *
	 * @param file
	 *            the file
	 * @param gl
	 *            the gl
	 */
	private void drawToOpenGL(final GamaObjFile file, final OpenGL gl, final List<Runnable> commands,
			final boolean triangulateQuads) {

		int nextmat = -1;
		int matcount = 0;
		final int totalmats = file.matTimings.size();
		String[] nextmatnamearray = null;
		String nextmatname = null;

		if (totalmats > 0 && file.materials != null) {
			nextmatnamearray = file.matTimings.get(matcount);
			nextmatname = nextmatnamearray[0];
			nextmat = Integer.parseInt(nextmatnamearray[1]);
		}
		DrawContext ctx = new DrawContext(file, gl);

		for (int i = 0; i < file.faces.size();) {
			while (i == nextmat) {
				final Material material = materials.computeIfAbsent(nextmatname, Material::new);
				material.run();
				commands.add(material);

				matcount++;
				if (matcount < totalmats) {
					nextmatnamearray = file.matTimings.get(matcount);
					nextmatname = nextmatnamearray[0];
					nextmat = Integer.parseInt(nextmatnamearray[1]);
				} else {
					nextmat = -1;
				}
			}

			final int textureKind = triangleTextureKind(file, i, triangulateQuads);
			if (textureKind < 0) {
				final int face = i++;
				final Runnable drawFace = () -> {
					gl.beginDrawing(file.faces.get(face).length == 3 ? GL.GL_TRIANGLES : GL.GL_TRIANGLE_FAN);
					drawFaceVertices(ctx, face, false);
					gl.endDrawing();
				};
				commands.add(drawFace);
				drawFace.run();
				continue;
			}
			gl.beginDrawing(GL.GL_TRIANGLES);
			int count = 0;
			do {
				final boolean quad = file.faces.get(i).length == 4;
				drawFaceVertices(ctx, i++, quad);
				count += quad ? 6 : 3;
			} while (i < file.faces.size() && i != nextmat && count + (file.faces.get(i).length == 4 ? 6 : 3)
					<= MAX_BATCH_VERTICES && triangleTextureKind(file, i, triangulateQuads) == textureKind);
			storeAndDraw(gl, commands);
		}
	}

	private void storeAndDraw(final OpenGL gl, final List<Runnable> commands) {
		final OpenGL.StoredDrawing drawing = gl.storeDrawing();
		drawings.add(drawing);
		commands.add(drawing::draw);
		gl.endDrawing();
	}
}
