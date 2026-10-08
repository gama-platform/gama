/*******************************************************************************************************
 *
 * GamaObjectType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.object;

import gama.annotations.support.ISymbolKind;
import gama.api.compilation.descriptions.IClassDescription;
import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITypesManager;
import gama.api.gaml.types.Types;
import gama.api.kernel.object.IClass;
import gama.api.kernel.object.IObject;
import gama.api.runtime.scope.IScope;

/**
 * The type used to represent an object of a class. Should be used by the class for all the operations relative to
 * casting, etc.
 *
 * Written by drogoul Modified on 1 ao�t 2010
 *
 * @todo Description
 *
 */
@SuppressWarnings ("unchecked")
public class GamaObjectType<T extends IObject> extends GamaType<T> {

	/** The species. */
	final IClassDescription species;

	/**
	 * Instantiates a new gama agent type.
	 *
	 * @param species
	 *            the species
	 * @param name
	 *            the name
	 * @param speciesId
	 *            the species id
	 * @param base
	 *            the base
	 */
	public GamaObjectType(final ITypesManager manager, final IClassDescription species, final String name,
			final int speciesId, final Class<T> base) {
		super(manager);
		this.species = species;
		this.name = name;
		id = speciesId;
		this.varKind = ISymbolKind.REGULAR;
		support = base;
		if (species != null) { setDefiningPlugin(species.getDefiningPlugin()); }
	}

	/**
	 * Performs type-specific initialization.
	 * <p>
	 * Agent types do not require additional initialization beyond the constructor.
	 * </p>
	 */
	@Override
	protected void init() {}

	/**
	 * Gets the defining plugin.
	 *
	 * @return the defining plugin
	 */
	@Override
	public String getDefiningPlugin() { return getSpecies().getDefiningPlugin(); }

	/**
	 * Cast.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @param param
	 *            the param
	 * @param copy
	 *            the copy
	 * @return the i object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public T cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		if (obj == null || scope == null || scope.getModel() == null) return null;
		IClass species = (IClass) param;
		if (species == null) { species = scope.getModel().getClass(getSpecies().getName()); }
		if (species == null) return (T) Types.OBJECT.cast(scope, obj, param, copy);
		if (obj instanceof IObject result) return result.isInstanceOf(species, false) ? (T) result : null;
		return null;
	}

	/**
	 * Gets the default.
	 *
	 * @return the default
	 */
	@Override
	public T getDefault() { return null; }

	/**
	 * Checks if is object type.
	 *
	 * @return true, if is object type
	 */
	@Override
	public boolean isObjectType() { return true; }

	/**
	 * Gets the species name.
	 *
	 * @return the species name
	 */
	@Override
	public String getSpeciesName() { return name; }

	/**
	 * Gets the species.
	 *
	 * @return the species
	 */
	@Override
	public IClassDescription getSpecies() { return species; }

	/**
	 * Can cast to const.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean canCastToConst() {
		return false;
	}

	/**
	 * Can be type of.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @return true, if successful
	 */
	@Override
	public boolean canBeTypeOf(final IScope scope, final Object obj) {
		final boolean b = super.canBeTypeOf(scope, obj);
		if (b) return true;
		if (obj instanceof IObject io) {
			final IClass s = scope.getModel().getClass(getSpeciesName());
			return io.isInstanceOf(s, false);
		}
		return false;
	}

	/**
	 * Gets the documentation.
	 *
	 * @return the documentation
	 */
	@Override
	public IGamlDocumentation getDocumentation() {
		IGamlDocumentation result =
				new GamlRegularDocumentation("Represents instances of species " + getSpecies().getName());
		getSpecies().documentAttributes(result);
		return result;
	}

	/**
	 * Gets the key type.
	 *
	 * @return the key type
	 */
	@Override
	public IType<String> getKeyType() { return Types.STRING; }

	/**
	 * Checks if is fixed length.
	 *
	 * @return true, if is fixed length
	 */
	@Override
	public boolean isFixedLength() { return false; }

	/**
	 * Checks if is drawable.
	 *
	 * @return true, if is drawable
	 */
	@Override
	public boolean isDrawable() { return true; }

}