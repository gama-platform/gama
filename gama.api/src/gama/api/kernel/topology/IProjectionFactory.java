/*******************************************************************************************************
 *
 * IProjectionFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.topology;

import java.util.Map;

import javax.measure.UnitConverter;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.utils.geometry.IEnvelope;

/**
 */
public interface IProjectionFactory {

	/**
	 * Sets the world projection env.
	 *
	 * @param scope
	 *            the scope
	 * @param env
	 *            the env
	 */
	void setWorldProjectionEnv(IScope scope, IEnvelope env);

	/**
	 * Gets the target CRS.
	 *
	 * @param scope
	 *            the scope
	 * @return the target CRS
	 */
	ICoordinateReferenceSystem getTargetCRS(IScope scope);

	/**
	 * Gets the save CRS.
	 *
	 * @param scope
	 *            the scope
	 * @return the save CRS
	 */
	ICoordinateReferenceSystem getSaveCRS(IScope scope);

	/**
	 * Gets the crs.
	 *
	 * @param scope
	 *            the scope
	 * @param code
	 *            the code
	 * @return the crs
	 */
	ICoordinateReferenceSystem getCRS(IScope scope, int code);

	/**
	 * Gets the crs.
	 *
	 * @param scope
	 *            the scope
	 * @param code
	 *            the code
	 * @param longitudeFirst
	 *            the longitude first
	 * @return the crs
	 */
	ICoordinateReferenceSystem getCRS(IScope scope, int code, boolean longitudeFirst);

	/**
	 * Gets the crs.
	 *
	 * @param scope
	 *            the scope
	 * @param code
	 *            the code
	 * @return the crs
	 */
	ICoordinateReferenceSystem getCRS(IScope scope, String code);

	/**
	 * Gets the crs.
	 *
	 * @param scope
	 *            the scope
	 * @param code
	 *            the code
	 * @param longitudeFirst
	 *            the longitude first
	 * @return the crs
	 */
	ICoordinateReferenceSystem getCRS(IScope scope, String code, boolean longitudeFirst) throws GamaRuntimeException;

	/**
	 * Gets the world.
	 *
	 * @return the world
	 */
	/*
	 * Thai.truongming@gmail.com ---------------begin date: 03-01-2014
	 */
	IProjection getWorld();

	/**
	 * Compute default CRS.
	 *
	 * @param scope
	 *            the scope
	 * @param code
	 *            the code
	 * @param target
	 *            the target
	 * @return the coordinate reference system
	 */
	/*
	 * thai.truongming@gmail.com -----------------end
	 */
	ICoordinateReferenceSystem computeDefaultCRS(IScope scope, int code, boolean target);

	/**
	 * From params.
	 *
	 * @param scope
	 *            the scope
	 * @param params
	 *            the params
	 * @param env
	 *            the env
	 * @return the i projection
	 */
	IProjection fromParams(IScope scope, Map<String, Object> params, IEnvelope env);

	/**
	 * From CRS.
	 *
	 * @param scope
	 *            the scope
	 * @param crs
	 *            the crs
	 * @param env
	 *            the env
	 * @return the i projection
	 */
	IProjection fromCRS(IScope scope, ICoordinateReferenceSystem crs, IEnvelope env);

	/**
	 * For saving with.
	 *
	 * @param scope
	 *            the scope
	 * @param epsgCode
	 *            the epsg code
	 * @return the i projection
	 */
	IProjection forSavingWith(IScope scope, Integer epsgCode);

	/**
	 * For saving with.
	 *
	 * @param scope
	 *            the scope
	 * @param epsgCode
	 *            the epsg code
	 * @param lonFirst
	 *            the lon first
	 * @return the i projection
	 */
	IProjection forSavingWith(IScope scope, Integer epsgCode, boolean lonFirst);

	/**
	 * For saving with.
	 *
	 * @param scope
	 *            the scope
	 * @param code
	 *            the code
	 * @return the i projection
	 */
	IProjection forSavingWith(IScope scope, String code);

	/**
	 * For saving with.
	 *
	 * @param scope
	 *            the scope
	 * @param crs
	 *            the crs
	 * @return the i projection
	 */
	IProjection forSavingWith(IScope scope, ICoordinateReferenceSystem crs);

	/**
	 * For saving with.
	 *
	 * @param scope
	 *            the scope
	 * @param code
	 *            the code
	 * @param lonFirst
	 *            the lon first
	 * @return the i projection
	 */
	IProjection forSavingWith(IScope scope, String code, boolean lonFirst);

	/**
	 * Gets the default initial CRS.
	 *
	 * @param scope
	 *            the scope
	 * @return the default initial CRS
	 */
	ICoordinateReferenceSystem getDefaultInitialCRS(IScope scope);

	/**
	 * Test consistency.
	 *
	 * @param scope
	 *            the scope
	 * @param crs
	 *            the crs
	 * @param env
	 *            the env
	 */
	void testConsistency(IScope scope, ICoordinateReferenceSystem crs, IEnvelope env);

	/**
	 * Gets the unit converter.
	 *
	 * @return the unit converter
	 */
	UnitConverter getUnitConverter();

}