/*******************************************************************************************************
 *
 * GamaGenericObjectType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.object;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.compilation.descriptions.IClassDescription;
import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITypesManager;
import gama.api.kernel.GamaMetaModel;
import gama.api.kernel.object.IObject;
import gama.api.runtime.scope.IScope;

/**
 * The "generic" agent type.
 *
 * Written by drogoul Modified on 1 ao�t 2010
 *
 * @todo Description
 * @modified 08 juin 2012
 *
 */
@type (
		name = IKeyword.OBJECT,
		id = IType.OBJECT,
		wraps = { IObject.class },
		kind = ISymbolKind.REGULAR,
		concept = { IConcept.TYPE, IConcept.SPECIES },
		doc = @doc ("The basic and default type of objects in GAML"))
public class GamaGenericObjectType extends GamaObjectType<IObject> {

	/**
	 * Instantiates a new gama generic agent type.
	 */
	public GamaGenericObjectType(final ITypesManager manager) {
		super(manager, null, IKeyword.OBJECT, IType.OBJECT, IObject.class);
	}

	/**
	 * Gets the species.
	 *
	 * @return the species
	 */
	@Override
	public IClassDescription getSpecies() { return GamaMetaModel.getObjectClassDescription(); }

	/**
	 * Cast.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @param param
	 *            the param
	 * @param keyType
	 *            the key type
	 * @param contentsType
	 *            the contents type
	 * @param copy
	 *            the copy
	 * @return the i agent
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public IObject cast(final IScope scope, final Object obj, final Object param, final IType<?> keyType,
			final IType<?> contentsType, final boolean copy) throws GamaRuntimeException {
		return cast(scope, obj, param, copy);
	}

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
	 * @return the i agent
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	@doc ("Returns an object if the argument is already an object, otherwise returns null")
	public IObject cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		if (obj == null) return getDefault();
		if (obj instanceof IObject o) return o;
		return getDefault();
	}

	/**
	 * Gets the documentation.
	 *
	 * @return the documentation
	 */
	@Override
	public IGamlDocumentation getDocumentation() {
		return new GamlConstantDocumentation("Generic type of all agents in a model");
	}

	/**
	 * Checks if is super type of.
	 *
	 * @param type
	 *            the type
	 * @return true, if is super type of
	 */
	@Override
	public boolean isSuperTypeOf(final IType<?> type) {
		return type != this && type instanceof GamaObjectType;
	}

}