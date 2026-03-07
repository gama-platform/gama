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

import com.jogamp.opengl.GL;
import com.jogamp.opengl.util.texture.Texture;

import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.utils.geometry.GamaCoordinateSequenceFactory;
import gama.api.utils.geometry.ICoordinates;
import gama.core.util.file.GamaObjFile;
import gama.extension.image.GamaImageFile;
import gama.ui.display.opengl4.OpenGL;

/**
 * The Class ObjFileDrawer.
 */
public class ObjFileDrawer {

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
	private static Texture handleMaterial(GamaObjFile file, OpenGL gl, String nextmatname) {
		gl.setCurrentColor(file.materials.getKd(nextmatname)[0], file.materials.getKd(nextmatname)[1],
				file.materials.getKd(nextmatname)[2], file.materials.getd(nextmatname));

		final String mapKa = file.materials.getMapKa(nextmatname);
		final String mapKd = file.materials.getMapKd(nextmatname);
		final String mapd = file.materials.getMapd(nextmatname);
		if (mapKa != null || mapKd != null || mapd != null) {
			File f = new File(file.mtlPath);
			StringBuilder path = new StringBuilder().append(f.getAbsolutePath().replace(f.getName(), ""));
			if (mapd != null) {
				path.append(mapd);
			} else if (mapKa != null) {
				path.append(mapKa);
			} else if (mapKd != null) { path.append(mapKd); }
			GamaImageFile asset = new GamaImageFile(null, path.toString());
			if (asset.exists(null)) {
				Texture texture = gl.getTexture(asset, false, true);
				gl.setCurrentTextures(texture.getTextureObject(), texture.getTextureObject());
				texture.setTexParameteri(gl.getGL(), GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
				texture.setTexParameteri(gl.getGL(), GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
				return texture;
			}
		}
		return null;
	}

	/**
	 * Helper method to draw a single face of the object
	 */
	private static void drawFace(final DrawContext ctx, final int faceIndex) {
		final int[] tempfaces = ctx.file.faces.get(faceIndex);
		final int[] norms = ctx.file.facesNorms.get(faceIndex);
		final int[] texs = ctx.file.facesTexs.get(faceIndex);

		final int polytype = tempfaces.length == 3 ? GL.GL_TRIANGLES : GL.GL_TRIANGLE_FAN;
		ctx.gl.beginDrawing(polytype);

		boolean hasNormals = true;
		for (int w = 0; w < tempfaces.length; w++) {
			if (norms[w] == 0) {
				hasNormals = false;
				break;
			}
		}

		final double[] arrayOfVertices = new double[tempfaces.length * 3];
		for (int w = 0; w < tempfaces.length; w++) {
			final double[] ordinates = ctx.file.setOfVertex.get(tempfaces[w] - 1);
			for (int k = 0; k < 3; k++) { arrayOfVertices[w * 3 + k] = ordinates[k]; }
		}
		final ICoordinates coords = GamaCoordinateSequenceFactory.ofLength(tempfaces.length + 1);
		coords.setTo(arrayOfVertices);
		coords.completeRing();

		if (!hasNormals) { ctx.gl.setNormal(coords, !coords.isClockwise()); }
		for (int w = 0; w < tempfaces.length; w++) {
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
		ctx.gl.endDrawing();
	}

	/**
	 * Draw to open GL.
	 *
	 * @param file
	 *            the file
	 * @param gl
	 *            the gl
	 */
	public static void drawToOpenGL(final GamaObjFile file, final OpenGL gl) {

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
		Texture texture = null;
		DrawContext ctx = new DrawContext(file, gl);

		for (int i = 0; i < file.faces.size(); i++) {
			if (i == nextmat) {
				if (texture != null) {
					texture.destroy(gl.getGL());
					texture = null;
				}

				texture = handleMaterial(file, gl, nextmatname);

				matcount++;
				if (matcount < totalmats) {
					nextmatnamearray = file.matTimings.get(matcount);
					nextmatname = nextmatnamearray[0];
					nextmat = Integer.parseInt(nextmatnamearray[1]);
				}
			}

			drawFace(ctx, i);
		}
	}

}
