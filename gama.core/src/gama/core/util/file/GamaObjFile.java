/*******************************************************************************************************
 *
 * GamaObjFile.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
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

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.file;
import gama.annotations.test;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.pair.IPair;
import gama.api.utils.files.FileUtils;
import gama.api.utils.geometry.GamaEnvelopeFactory;
import gama.api.utils.geometry.AxisAngle;
import gama.api.utils.geometry.IEnvelope;
import gama.dev.DEBUG;

/**
 * Class GamaObjFile.
 *
 * @author drogoul
 * @since 30 déc. 2013
 *
 */
@file (
		name = "obj",
		extensions = { "obj" },
		buffer_type = IType.LIST,
		buffer_content = IType.GEOMETRY,
		doc = @doc ("'.obj' files are files containing 3D geometries. The internal representation is a list of one geometry"))
@test ("is_obj(\"features.obj\")")
@test ("is_obj(\"features.OBJ\")")
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

	/** Set only after a complete parse; volatile so readers that see it also see the parsed lists. */
	volatile boolean loaded = false;
	private volatile long contentsRevision;

	public long getContentsRevision() { return contentsRevision; }

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
		this(scope, pathName, (IPair<Double, IPoint>) null);
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
					+ "is a pair angle::rotation vector. The associated mlt file have to have the same name as the file to be read.",
			examples = { @example (
					value = "file f <- obj_file(\"file.obj\", 90.0::{-1,0,0});",
					isExecutable = false) })

	public GamaObjFile(final IScope scope, final String pathName, final IPair<Double, IPoint> initRotation)
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
			value = "This file constructor allows to read an obj file, using a specific mlt file",
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
			value = "This file constructor allows to read an obj file, using a specific mlt file, and apply an init rotation to it. The rotation"
					+ "is a pair angle::rotation vector",
			examples = { @example (
					value = "file f <- obj_file(\"file.obj\",\"file.mlt\", 90.0::{-1,0,0});",
					isExecutable = false) })

	public GamaObjFile(final IScope scope, final String pathName, final String mtlPath,
			final IPair<Double, IPoint> initRotation) {
		super(scope, pathName, initRotation);
		if (mtlPath != null) {
			this.mtlPath = FileUtils.constructAbsoluteFilePath(scope, mtlPath, false);
		} else {
			this.mtlPath = null;
		}

	}

	@Override
	public String getDrawingCacheKey(final IScope scope) {
		// NUL cannot occur in file paths, so distinct OBJ/MTL pairs cannot collide.
		return getPath(scope) + '\0' + (mtlPath == null ? "" : mtlPath);
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
	public synchronized void loadObject(final IScope scope, final boolean forDrawing) {
		if (loaded) return;
		try (BufferedReader br = new BufferedReader(new FileReader(getFile(scope)))) {
			loadObject(br);
		} catch (final IOException | IllegalArgumentException e) {
			clearParsedData();
			throw GamaRuntimeException.create(e, scope);
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
	public synchronized void loadObject(final BufferedReader br) throws IOException {
		if (loaded) return;
		int facecounter = 0;
		boolean firstpass = true;
		String newline;
		while ((newline = br.readLine()) != null) {
			if (newline.length() > 0) {
				newline = newline.trim();
				if (newline.isEmpty() || newline.charAt(0) == '#') { continue; }
				final int comment = newline.indexOf('#');
				if (comment >= 0) { newline = newline.substring(0, comment).trim(); }

				// LOADS VERTEX COORDINATES
				if (hasDirective(newline, "v")) {
					newline = newline.substring(2);
					final StringTokenizer st = new StringTokenizer(newline);
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
				if (hasDirective(newline, "vt")) {
					final double coords[] = new double[4];
					// final String coordstext[] = new String[4];
					newline = newline.substring(3);
					final StringTokenizer st = new StringTokenizer(newline);
					for (int i = 0; st.hasMoreTokens(); i++) { coords[i] = Double.parseDouble(st.nextToken()); }

					setOfVertexTextures.add(coords);
				} else

				// LOADS VERTEX NORMALS COORDINATES
				if (hasDirective(newline, "vn")) {
					final double coords[] = new double[4];
					// final String coordstext[] = new String[4];
					newline = newline.substring(3);
					final StringTokenizer st = new StringTokenizer(newline);
					for (int i = 0; st.hasMoreTokens(); i++) { coords[i] = Double.parseDouble(st.nextToken()); }
					setOfVertexNormals.add(coords);
				} else
				// LOADS FACES COORDINATES
				if (hasDirective(newline, "f")) {
					facecounter++;
					newline = newline.substring(2);
					final StringTokenizer st = new StringTokenizer(newline);
					final int count = st.countTokens();
					final int v[] = new int[count];
					final int vt[] = new int[count];
					final int vn[] = new int[count];
					for (int i = 0; i < count; i++) {
						final String token = st.nextToken();
						final int firstSlash = token.indexOf('/');
						final int secondSlash = firstSlash < 0 ? -1 : token.indexOf('/', firstSlash + 1);
						v[i] = resolveIndex(Integer.parseInt(token, 0,
								firstSlash < 0 ? token.length() : firstSlash, 10), setOfVertex.size());
						if (firstSlash >= 0) {
							final int end = secondSlash < 0 ? token.length() : secondSlash;
							if (end > firstSlash + 1) {
								vt[i] = resolveIndex(Integer.parseInt(token, firstSlash + 1, end, 10),
										setOfVertexTextures.size());
							}
							if (secondSlash >= 0 && secondSlash + 1 < token.length()) {
								vn[i] = resolveIndex(Integer.parseInt(token, secondSlash + 1, token.length(), 10),
										setOfVertexNormals.size());
							}
						}
					}

					faces.add(v);
					facesTexs.add(vt);
					facesNorms.add(vn);
				} else

				// LOADS MATERIALS
				if (hasDirective(newline, "mtllib")) {
					// String[] coordstext = new String[3];
					// coordstext = newline.split("\\s+");
					if (mtlPath != null) { loadMaterials(); }
				} else

				// USES MATERIALS
				if (hasDirective(newline, "usemtl")) {
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

	private static int resolveIndex(final int index, final int size) {
		if (index == 0 || index < -size) throw new IllegalArgumentException("Invalid OBJ index: " + index);
		return index < 0 ? size + index + 1 : index;
	}

	private static boolean hasDirective(final String line, final String directive) {
		return line.startsWith(directive) && line.length() > directive.length()
				&& Character.isWhitespace(line.charAt(directive.length()));
	}

	@Override
	public IEnvelope computeEnvelope(final IScope scope) {
		if (envelope == null) {
			loadObject(scope, false);
			envelope = GamaEnvelopeFactory.of(leftpoint, rightpoint, bottompoint, toppoint, nearpoint, farpoint);
			if (initRotation != null && initRotation.getAngle() != 0.0) { envelope = envelope.rotate(initRotation); }
		}
		return envelope;
	}

	@Override
	public void setInitRotation(final AxisAngle rotation) {
		super.setInitRotation(rotation);
		envelope = null;
	}

	/**
	 * Method fillBuffer(). Fills the buffer with the polygons built from the .obj vertices + faces
	 *
	 * @see gama.api.types.file.GamaFile#fillBuffer(gama.api.runtime.scope.IScope)
	 */
	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		if (getBuffer() != null) return;
		loadObject(scope, false);
		final IList<IShape> result = GamaListFactory.create(Types.GEOMETRY);
		final IList<IShape> vertices = GamaListFactory.create(Types.POINT);
		for (final double[] coords : setOfVertex) {
			final IPoint pt = GamaPointFactory.create(coords[0], -coords[1], coords[2]);
			vertices.add(pt);
		}
		for (final int[] vertexRefs : faces) {
			final IList<IShape> face = GamaListFactory.<IShape> create(Types.POINT);
			for (final int vertex : vertexRefs) {
				face.add(vertices.get(vertex - 1));
			}
			result.add(GamaShapeFactory.buildPolygon(face));
		}
		setBuffer(result);
	}

	@Override
	public void invalidateContents() {
		super.invalidateContents();
		clearParsedData();
		contentsRevision++;
	}

	private synchronized void clearParsedData() {
		setOfVertex.clear();
		setOfVertexNormals.clear();
		setOfVertexTextures.clear();
		faces.clear();
		facesTexs.clear();
		facesNorms.clear();
		matTimings.clear();
		materials = null;
		envelope = null;
		loaded = false;
		toppoint = bottompoint = leftpoint = rightpoint = farpoint = nearpoint = 0;
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

}
