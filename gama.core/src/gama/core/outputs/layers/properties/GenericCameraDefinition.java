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

import gama.api.data.objects.IPoint;
import gama.api.ui.layers.ICameraDefinition;
import gama.api.utils.geometry.GamaPointFactory;

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

	/**
	 * Gets the location.
	 *
	 * @return the location
	 */
	@Override
	public IPoint getLocation() { return currentLocation; }

	/**
	 * Gets the target.
	 *
	 * @return the target
	 */
	@Override
	public IPoint getTarget() { return currentTarget; }

	/**
	 * Gets the lens.
	 *
	 * @return the lens
	 */
	@Override
	public Double getLens() { return lens; }

	/**
	 * Checks if is locked.
	 *
	 * @return the boolean
	 */
	@Override
	public Boolean isLocked() { return isLocked; }

	/**
	 * Checks if is dynamic.
	 *
	 * @return the boolean
	 */
	@Override
	public Boolean isDynamic() { return false; }

	/**
	 * Sets the locked.
	 *
	 * @param b
	 *            the new locked
	 */
	@Override
	public void setLocked(final Boolean b) {
		isLocked = b;

	}

	/**
	 * Sets the location.
	 *
	 * @param point
	 *            the point
	 * @return true, if successful
	 */
	@Override
	public boolean setLocation(final IPoint point) {
		if (currentLocation.equals(point)) return false;
		currentLocation.setLocation(point);
		return true;
	}

	/**
	 * Sets the target.
	 *
	 * @param point
	 *            the point
	 * @return true, if successful
	 */
	@Override
	public boolean setTarget(final IPoint point) {
		if (currentTarget.equals(point)) return false;
		currentTarget.setLocation(point);
		return true;
	}

	/**
	 * Sets the lens.
	 *
	 * @param cameraLens
	 *            the new lens
	 */
	@Override
	public void setLens(final Double cameraLens) { lens = cameraLens; }

	/**
	 * Reset.
	 */
	@Override
	public void reset() {
		currentLocation.setLocation(initialLocation);
		currentTarget.setLocation(initialTarget);
		isLocked = false;
	}

	/**
	 * Sets the distance.
	 *
	 * @param distance
	 *            the distance
	 * @return true, if successful
	 */
	@Override
	public boolean setDistance(final Double distance) {
		if (distance.equals(currentLocation.distance3D(currentTarget))) return false;
		IPoint vector = currentLocation.minus(currentTarget).normalized().times(distance);
		currentLocation.setLocation(currentTarget.plus(vector));
		return true;
	}

	/**
	 * Gets the distance.
	 *
	 * @return the distance
	 */
	@Override
	public Double getDistance() { return currentLocation.distance3D(currentTarget); }

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	@Override
	public String getName() { return name; }

	@Override
	public IPoint computeLocation(final String pos, final IPoint target, final double maxX, final double maxY,
			final double maxZ) {

		double tx = target.getX();
		double ty = target.getY();
		return switch (pos) {
			case from_above -> GamaPointFactory.create(tx, ty, maxZ);
			case from_left -> GamaPointFactory.create(tx - maxX, ty, 0);
			case from_up_left -> GamaPointFactory.create(tx - maxX, ty, maxZ);
			case from_right -> GamaPointFactory.create(tx + maxX, ty - maxY / 1000, 0);
			case from_up_right -> GamaPointFactory.create(tx + maxX, ty - maxY / 1000, maxZ);
			case from_front -> GamaPointFactory.create(tx, ty - maxY, 0);
			case from_up_front -> GamaPointFactory.create(tx, ty - maxY, maxZ);
			case isometric -> GamaPointFactory.create(tx + maxZ, -maxZ + ty, maxZ / 1.2);
			default -> GamaPointFactory.create(tx, ty, maxZ); // FROM_ABOVE
		};

	}

}
