/*******************************************************************************************************
 *
 * GamaObjFile.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.file;
import gama.core.common.geometry.Envelope3D;
import gama.core.common.util.FileUtils;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaPair;
import gama.core.util.IList;
import gama.dev.DEBUG;
import gama.gaml.types.GamaGeometryType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * Class GamaObjFile.
 *
 * @author drogoul
 * @since 30 déc. 2013
 *
 */
@file (
		name = "obj",
		extensions = { "obj", "OBJ" },
		buffer_type = IType.LIST,
		buffer_content = IType.GEOMETRY,
		doc = @doc ("'.obj' files are files containing 3D geometries. The internal representation is a list of one geometry"))
public class GamaObjFile extends Gama3DGeometryFile {

	/** The set of vertex. */
	public final ArrayList<double[]> setOfVertex = new ArrayList<>();

	/** The set of vertex normals. */
	public final ArrayList<double[]> setOfVertexNormals = new ArrayList<>();

	/** The set of vertex textures. */
	public final ArrayList<double[]> setOfVertexTextures = new ArrayList<>();

	/** The faces. */
	public final ArrayList<int[]> faces = new ArrayList<>();

	/** The faces texs. */
	public final ArrayList<int[]> facesTexs = new ArrayList<>();

	/** The faces norms. */
	public final ArrayList<int[]> facesNorms = new ArrayList<>();

	/** The mat timings. */
	public final ArrayList<String[]> matTimings = new ArrayList<>();

	/** The materials. */
	public MtlLoader materials;
	// private int objectList;
	/** The toppoint. */
	// private int numPolys = 0;
	public double toppoint = 0f;

	/** The bottompoint. */
	public double bottompoint = 0f;

	/** The leftpoint. */
	public double leftpoint = 0f;

	/** The rightpoint. */
	public double rightpoint = 0f;

	/** The farpoint. */
	public double farpoint = 0f;

	/** The nearpoint. */
	public double nearpoint = 0f;

	/** The mtl path. */
	public final String mtlPath;

	/** The loaded. */
	boolean loaded = false;

	/**
	 * @param scope
	 * @param pathName
	 * @throws GamaRuntimeException
	 */
	@doc (
			value = "This file constructor allows to read an obj file. The associated mlt file have to have the same name as the file to be read.",
			examples = { @example (
					value = "file f <- obj_file(\"file.obj\");",
					isExecutable = false) })

	public GamaObjFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		this(scope, pathName, (GamaPair<Double, GamaPoint>) null);
	}

	/**
	 * Instantiates a new gama obj file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param initRotation
	 *            the init rotation
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@doc (
			value = "This file constructor allows to read an obj file and apply an init rotation to it. The rotation"
					+ "is a pair angle::rotation vector. The associated mlt file has to have the same name as the file to be read.",
			examples = { @example (
					value = "file f <- obj_file(\"file.obj\", 90.0::{-1,0,0});",
					isExecutable = false) })

	public GamaObjFile(final IScope scope, final String pathName, final GamaPair<Double, GamaPoint> initRotation)
			throws GamaRuntimeException {
		this(scope, pathName, pathName.replace(".obj", ".mtl"), initRotation);
	}

	/**
	 * Instantiates a new gama obj file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param mtlPath
	 *            the mtl path
	 */
	@doc (
			value = "This file constructor allows to read an obj file, using a specific texture file (.mtl)",
			examples = { @example (
					value = "file f <- obj_file(\"file.obj\",\"file.mlt\");",
					isExecutable = false) })
	public GamaObjFile(final IScope scope, final String pathName, final String mtlPath) {
		this(scope, pathName, mtlPath, null);
	}

	/**
	 * Instantiates a new gama obj file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param mtlPath
	 *            the mtl path
	 * @param initRotation
	 *            the init rotation
	 */
	@doc (
			value = "This file constructor allows to read an obj file, using a specific material file (.mtl), and apply an init rotation to it. The rotation"
					+ "is a pair angle::rotation vector",
			examples = { @example (
					value = "file f <- obj_file(\"file.obj\",\"file.mlt\", 90.0::{-1,0,0});",
					isExecutable = false) })

	public GamaObjFile(final IScope scope, final String pathName, final String mtlPath,
			final GamaPair<Double, GamaPoint> initRotation) {
		super(scope, pathName, initRotation);
		if (mtlPath != null) {
			this.mtlPath = FileUtils.constructAbsoluteFilePath(scope, mtlPath, false);
		} else {
			this.mtlPath = null;
		}

	}

	/**
	 * Centerit.
	 */
	private void centerit() {
		final double xshift = (rightpoint - leftpoint) / 2.0d;
		final double yshift = (toppoint - bottompoint) / 2.0d;
		final double zshift = (nearpoint - farpoint) / 2.0d;
		for (int i = 0; i < setOfVertex.size(); i++) {
			final double coords[] = new double[4];
			coords[0] = setOfVertex.get(i)[0] - leftpoint - xshift;
			coords[1] = setOfVertex.get(i)[1] - bottompoint - yshift;
			coords[2] = setOfVertex.get(i)[2] - farpoint - zshift;
			setOfVertex.set(i, coords);
		}

	}

	/**
	 * Load object.
	 *
	 * @param scope
	 *            the scope
	 * @param forDrawing
	 *            the for drawing
	 */
	public void loadObject(final IScope scope, final boolean forDrawing) {
		try (BufferedReader br = new BufferedReader(new FileReader(getFile(scope)))) {
			loadObject(br);
		} catch (final IOException e) {
			DEBUG.ERR("Failed to read file: " /* + br.toString() */);
		} catch (final NumberFormatException e) {
			DEBUG.ERR("Malformed OBJ file: "/* + br.toString() */ + "\r \r" + e.getMessage());
		}

	}

	/**
	 * Load object.
	 *
	 * @param br
	 *            the br
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void loadObject(final BufferedReader br) throws IOException {
		if (loaded) return;
		int facecounter = 0;
		boolean firstpass = true;
		String newline;
		while ((newline = br.readLine()) != null) {
			if (newline.length() > 0) {
				newline = newline.trim();

				// LOADS VERTEX COORDINATES
				if (newline.startsWith("v ")) {
					newline = newline.substring(2);
					final StringTokenizer st = new StringTokenizer(newline, " ");
					final double coords[] = new double[st.countTokens()];
					for (int i = 0; st.hasMoreTokens(); i++) { coords[i] = Double.parseDouble(st.nextToken()); }

					if (firstpass) {
						rightpoint = coords[0];
						leftpoint = coords[0];
						toppoint = coords[1];
						bottompoint = coords[1];
						nearpoint = coords[2];
						farpoint = coords[2];
						firstpass = false;
					}
					if (coords[0] > rightpoint) { rightpoint = coords[0]; }
					if (coords[0] < leftpoint) { leftpoint = coords[0]; }
					if (coords[1] > toppoint) { toppoint = coords[1]; }
					if (coords[1] < bottompoint) { bottompoint = coords[1]; }
					if (coords[2] > nearpoint) { nearpoint = coords[2]; }
					if (coords[2] < farpoint) { farpoint = coords[2]; }
					setOfVertex.add(coords);
				} else

				// LOADS VERTEX TEXTURE COORDINATES
				if (newline.startsWith("vt")) {
					final double coords[] = new double[4];
					// final String coordstext[] = new String[4];
					newline = newline.substring(3);
					final StringTokenizer st = new StringTokenizer(newline, " ");
					for (int i = 0; st.hasMoreTokens(); i++) { coords[i] = Double.parseDouble(st.nextToken()); }

					setOfVertexTextures.add(coords);
				} else

				// LOADS VERTEX NORMALS COORDINATES
				if (newline.startsWith("vn")) {
					final double coords[] = new double[4];
					// final String coordstext[] = new String[4];
					newline = newline.substring(3);
					final StringTokenizer st = new StringTokenizer(newline, " ");
					for (int i = 0; st.hasMoreTokens(); i++) { coords[i] = Double.parseDouble(st.nextToken()); }
					setOfVertexNormals.add(coords);
				} else
				// LOADS FACES COORDINATES
				if (newline.startsWith("f ")) {
					facecounter++;
					newline = newline.substring(2);
					final StringTokenizer st = new StringTokenizer(newline, " ");
					final int count = st.countTokens();
					final int v[] = new int[count];
					final int vt[] = new int[count];
					final int vn[] = new int[count];
					for (int i = 0; i < count; i++) {
						final char chars[] = st.nextToken().toCharArray();
						final StringBuilder sb = new StringBuilder();
						char lc = 'x';
						for (final char c : chars) {
							if (c == '/' && lc == '/') { sb.append('0'); }
							lc = c;
							sb.append(lc);
						}

						final StringTokenizer st2 = new StringTokenizer(sb.toString(), "/");
						final int num = st2.countTokens();
						v[i] = Integer.parseInt(st2.nextToken());
						if (num > 1) {
							vt[i] = Integer.parseInt(st2.nextToken());
						} else {
							vt[i] = 0;
						}
						if (num > 2) {
							vn[i] = Integer.parseInt(st2.nextToken());
						} else {
							vn[i] = 0;
						}
					}

					faces.add(v);
					facesTexs.add(vt);
					facesNorms.add(vn);
				} else

				// LOADS MATERIALS
				if (newline.charAt(0) == 'm' && newline.charAt(1) == 't' && newline.charAt(2) == 'l'
						&& newline.charAt(3) == 'l' && newline.charAt(4) == 'i' && newline.charAt(5) == 'b') {
					// String[] coordstext = new String[3];
					// coordstext = newline.split("\\s+");
					if (mtlPath != null) { loadMaterials(); }
				} else

				// USES MATERIALS
				if (newline.charAt(0) == 'u' && newline.charAt(1) == 's' && newline.charAt(2) == 'e'
						&& newline.charAt(3) == 'm' && newline.charAt(4) == 't' && newline.charAt(5) == 'l') {
					final String[] coords = new String[2];
					final String[] coordstext = newline.split("\\s+");
					coords[0] = coordstext[1];
					coords[1] = facecounter + "";
					matTimings.add(coords);
				}
			}
		}
		centerit();
		// numPolys = faces.size();
		loaded = true;
	}

	/**
	 * Method fillBuffer(). Fills the buffer with the polygons built from the .obj vertices + faces
	 *
	 * @see gama.core.util.file.GamaFile#fillBuffer(gama.core.runtime.IScope)
	 */
	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		loadObject(scope, false);
		setBuffer(GamaListFactory.<IShape> create(Types.GEOMETRY));
		final IList<IShape> vertices = GamaListFactory.create(Types.POINT);
		for (final double[] coords : setOfVertex) {
			final GamaPoint pt = new GamaPoint(coords[0], -coords[1], coords[2]);
			vertices.add(pt);
		}
		for (final int[] vertexRefs : faces) {
			final IList<IShape> face = GamaListFactory.<IShape> create(Types.POINT);
			for (final int vertex : vertexRefs) {
				face.add(vertices.get(vertex - 1));
				getBuffer().add(GamaGeometryType.buildPolygon(face));
			}
		}
		envelope = Envelope3D.of(leftpoint, rightpoint, bottompoint, toppoint, nearpoint, farpoint);

	}

	@Override
	public IList<String> getAttributes(final IScope scope) {
		// TODO what to return ?
		return GamaListFactory.create();
	}

	/**
	 * Load materials.
	 */
	private void loadMaterials() {
		final String refm = mtlPath;
		try (FileReader frm = new FileReader(refm); final BufferedReader brm = new BufferedReader(frm);) {
			materials = new MtlLoader(brm, mtlPath);
		} catch (final IOException e) {
			DEBUG.ERR("Could not open file: " + refm);
			materials = null;
		}
	}

	/**
	 * Return the key to use for the cache in OpenGL (see #644). Default is the path.
	 *
	 * @param scope
	 * @return
	 */
	@Override
	public String getKey(final IScope scope) {
		return super.getKey(scope) + (mtlPath == null ? "" : mtlPath);
	}

}
