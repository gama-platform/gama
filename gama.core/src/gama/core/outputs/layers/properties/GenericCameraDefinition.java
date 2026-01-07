/*******************************************************************************************************
 *
 * GenericCameraDefinition.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.properties;

import gama.core.metamodel.shape.GamaPointFactory;
import gama.core.metamodel.shape.IPoint;

/**
 * The Class GenericCameraDefinition. A simple holder of position, target and lens of the camera. All coordinates are
 * supposed to be stored with the Y ordinate negated (the reverse of the value in GAML)
 */
public class GenericCameraDefinition implements ICameraDefinition {

	/** The current target and location. */
	final IPoint currentLocation, currentTarget;

	/** The initial target and location. */
	final IPoint initialLocation, initialTarget;

	/** The lens. */
	Double lens = 45.0;

	/** The is interactive. */
	Boolean isLocked = false;

	/** The name. */
	final String name;

	/**
	 * Instantiates a new generic camera definition.
	 *
	 * @param loc
	 *            the loc
	 * @param target
	 *            the target
	 */
	public GenericCameraDefinition(final String name, final IPoint loc, final IPoint target) {
		initialLocation = GamaPointFactory.create(loc);
		currentLocation = GamaPointFactory.create(loc);
		initialTarget = GamaPointFactory.create(target);
		currentTarget = GamaPointFactory.create(target);
		this.name = name;
	}

	/**
	 * Instantiates a new generic camera definition.
	 *
	 * @param name
	 *            the name.
	 * @param target
	 *            the target
	 * @param w
	 *            the w
	 * @param h
	 *            the h
	 * @param max
	 *            the max
	 */
	public GenericCameraDefinition(final String name, final IPoint target, final double w, final double h,
			final double max) {
		IPoint loc = computeLocation(name, target, w, h, max);
		initialLocation = GamaPointFactory.create(loc);
		currentLocation = GamaPointFactory.create(loc);
		initialTarget = GamaPointFactory.create(target);
		currentTarget = GamaPointFactory.create(target);
		this.name = name;
	}

	@Override
	public IPoint getLocation() { return currentLocation; }

	@Override
	public IPoint getTarget() { return currentTarget; }

	@Override
	public Double getLens() { return lens; }

	@Override
	public Boolean isLocked() { return isLocked; }

	@Override
	public Boolean isDynamic() { return false; }

	@Override
	public void setLocked(final Boolean b) {
		isLocked = b;

	}

	@Override
	public boolean setLocation(final IPoint point) {
		if (currentLocation.equals(point)) return false;
		currentLocation.setLocation(point);
		return true;
	}

	@Override
	public boolean setTarget(final IPoint point) {
		if (currentTarget.equals(point)) return false;
		currentTarget.setLocation(point);
		return true;
	}

	@Override
	public void setLens(final Double cameraLens) { lens = cameraLens; }

	@Override
	public void reset() {
		currentLocation.setLocation(initialLocation);
		currentTarget.setLocation(initialTarget);
		isLocked = false;
	}

	@Override
	public boolean setDistance(final Double distance) {
		if (distance.equals(currentLocation.distance3D(currentTarget))) return false;
		IPoint vector = currentLocation.minus(currentTarget).normalized().times(distance);
		currentLocation.setLocation(currentTarget.plus(vector));
		return true;
	}

	@Override
	public Double getDistance() { return currentLocation.distance3D(currentTarget); }

	@Override
	public String getName() { return name; }

}
