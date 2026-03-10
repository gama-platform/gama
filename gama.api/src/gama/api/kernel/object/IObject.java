/*******************************************************************************************************
 *
 * IObject.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.object;

import gama.api.compilation.IVarAndActionSupport;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;
import gama.api.types.misc.IContainer;
import gama.api.types.misc.IValue;
import gama.api.utils.interfaces.IAttributed;

/**
 *
 */
public interface IObject extends IAttributed, IContainer.ToGet<String, Object>, IVarAndActionSupport, IValue {

	/**
	 * Gets the species.
	 *
	 * @return the species
	 */
	IClass getSpecies();

	/**
	 * Gets the species name.
	 *
	 * @return the species name
	 */
	default String getSpeciesName() { return getSpecies().getName(); }

	/**
	 * Gets the gaml type. This is the type supported by the species/class of this object. Not the type of the
	 * species/class used as a variable (i.e. typically a list<type supported>)
	 *
	 * @return the gaml type
	 */
	@Override
	default IType<?> getGamlType() { return getSpecies().getDescription().getGamlType(); }

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
	<T extends IClass> boolean isInstanceOf(final T s, boolean direct);

	/**
	 * Gets the from indices list.
	 *
	 * @param scope
	 *            the scope
	 * @param indices
	 *            the indices
	 * @return the from indices list
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	default Object getFromIndicesList(final IScope scope, final IList<String> indices) throws GamaRuntimeException {
		if (indices == null || indices.isEmpty()) return null;
		String key = indices.getFirst();
		return get(scope, key);
	}
}