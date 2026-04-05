/*******************************************************************************************************
 *
 * Gama3DGeometryFile.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.pair.IPair;
import gama.api.utils.geometry.AxisAngle;
import gama.api.utils.geometry.GeometryUtils;
import gama.api.utils.geometry.IEnvelope;

/**
 * The Class Gama3DGeometryFile.
 */
public abstract class Gama3DGeometryFile extends GamaGeometryFile {

	/** The init rotation. */
	protected AxisAngle initRotation;

	/** The envelope. */
	protected IEnvelope envelope;

	/**
	 * Instantiates a new gama 3 D geometry file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public Gama3DGeometryFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName);
	}

	/**
	 * Instantiates a new gama 3 D geometry file.
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
	public Gama3DGeometryFile(final IScope scope, final String pathName, final IPair<Double, IPoint> initRotation)
			throws GamaRuntimeException {
		super(scope, pathName);
		if (initRotation != null) {
			final Double angle = Cast.asFloat(null, initRotation.key());
			final IPoint axis = initRotation.value();
			this.initRotation = new AxisAngle(axis, angle);
		} else {
			this.initRotation = null;
		}
	}

	@Override
	protected IShape buildGeometry(final IScope scope) {
		final List<Geometry> faces = new ArrayList<>();
		for (final IShape shape : getBuffer().iterable(scope)) { faces.add(shape.getInnerGeometry()); }
		return GamaShapeFactory.createFrom(GeometryUtils.getGeometryFactory().buildGeometry(faces));
	}

	@Override
	public AxisAngle getInitRotation() { return initRotation; }

	/**
	 * Sets the inits the rotation.
	 *
	 * @param initRotation
	 *            the new inits the rotation
	 */
	public void setInitRotation(final AxisAngle initRotation) { this.initRotation = initRotation; }

	@Override
	public IEnvelope computeEnvelope(final IScope scope) {
		if (envelope == null) {
			fillBuffer(scope);
			if (initRotation != null && initRotation.getAngle() != 0.0) { envelope = envelope.rotate(initRotation); }
		}
		return envelope;
	}

	@Override
	public boolean is2D() {
		return false;
	}

}