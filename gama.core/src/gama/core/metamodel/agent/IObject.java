/*******************************************************************************************************
 *
 * IObject.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.metamodel.agent;

import gama.core.common.interfaces.IVarAndActionSupport;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IContainer;
import gama.gaml.interfaces.IAttributed;
import gama.gaml.species.IClass;

/**
 *
 */
public interface IObject<ClassOrSpecies extends IClass>
		extends IAttributed, IContainer.Addressable<String, Object>, IVarAndActionSupport {

	/**
	 * Gets the species.
	 *
	 * @return the species
	 */
	ClassOrSpecies getSpecies();

	/**
	 * Gets the species name.
	 *
	 * @return the species name
	 */
	default String getSpeciesName() { return getSpecies().getName(); }

	/**
	 * Gets the direct var value.
	 *
	 * @param scope
	 *            the scope
	 * @param s
	 *            the s
	 * @return the direct var value
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	Object getDirectVarValue(IScope scope, String s) throws GamaRuntimeException;

	/**
	 * Sets the direct var value.
	 *
	 * @param scope
	 *            the scope
	 * @param s
	 *            the s
	 * @param v
	 *            the v
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	void setDirectVarValue(IScope scope, String s, Object v) throws GamaRuntimeException;

	/**
	 * Checks if is instance of.
	 *
	 * @param s
	 *            the s
	 * @param direct
	 *            the direct
	 * @return true, if is instance of
	 */
	boolean isInstanceOf(final ClassOrSpecies s, boolean direct);

}
