/*******************************************************************************************************
 *
 * ParametricType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import java.util.Map;

import org.apache.commons.lang3.Strings;

import gama.annotations.support.ISymbolKind;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.compilation.prototypes.IArtefactProto;
import gama.api.data.objects.IContainer;
import gama.api.data.objects.IMap;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.utils.GamlProperties;
import gama.dev.DEBUG;

/**
 * Class ParametricType. A class that allows to build composed types with a content type and a key type
 *
 * @author drogoul
 * @since 19 janv. 2014
 *
 */
public class ParametricType implements IContainerType<IContainer<?, ?>> {

	static {
		DEBUG.OFF();
	}

	/** The type. */
	private final IContainerType<IContainer<?, ?>> type;

	/** The contents type. */
	private final IType<?> contentsType;

	/** The key type. */
	private final IType<?> keyType;

	/** The expression. */
	private final IExpression expression;

	/** The types manager inherited from the base type */
	private final ITypesManager typesManager;

	/** The name. */
	private String name;

	/**
	 * Instantiates a new parametric type.
	 *
	 * @param t
	 *            the t
	 * @param kt
	 *            the kt
	 * @param ct
	 *            the ct
	 */
	protected ParametricType(final ITypesManager manager, final IContainerType<IContainer<?, ?>> t, final IType<?> kt,
			final IType<?> ct) {
		type = t;
		contentsType = ct;
		keyType = kt;
		expression = GAML.getExpressionFactory().createTypeExpression(this);
		// Inherit the typesManager from the base type
		this.typesManager = Types.findMoreSpecificTypesManagerAmong(manager, t.getTypesManager(), kt.getTypesManager(),
				ct.getTypesManager());
	}

	@Override
	public int getNumberOfParameters() { return type.getNumberOfParameters(); }

	@Override
	public boolean isCompoundType() { return true; }

	@Override
	public boolean equals(final Object other) {
		if (other instanceof ParametricType) return type.equals(((ParametricType) other).getGamlType())
				&& keyType.equals(((ParametricType) other).getKeyType())
				&& contentsType.equals(((ParametricType) other).getContentType());
		return false;
	}

	@Override
	public int hashCode() {
		return 31 * (31 * (31 + type.hashCode()) + keyType.hashCode()) + contentsType.hashCode();
	}

	@Override
	public String getDefiningPlugin() { return type.getDefiningPlugin(); }

	/**
	 * Method getType()
	 *
	 * @see gama.api.gaml.types.ITyped#getGamlType()
	 */
	@Override
	public IContainerType<IContainer<?, ?>> getGamlType() { return type; }

	/**
	 * Method cast()
	 *
	 * @see gama.api.gaml.types.IType#cast(gama.api.runtime.scope.IScope, java.lang.Object, java.lang.Object,
	 *      gama.api.gaml.types.IType, gama.api.gaml.types.IType)
	 */
	@Override
	public IContainer<?, ?> cast(final IScope scope, final Object obj, final Object param, final IType<?> kt,
			final IType<?> ct, final boolean copy) throws GamaRuntimeException {
		return type.cast(scope, obj, param, keyType, contentsType, copy);
	}

	/**
	 * Method id()
	 *
	 * @see gama.api.gaml.types.IType#id()
	 */
	@Override
	public int id() {
		return type.id();
	}

	/**
	 * Method toClass()
	 *
	 * @see gama.api.gaml.types.IType#toClass()
	 */
	@Override
	public Class<? extends IContainer<?, ?>> toClass() {
		return type.toClass();
	}

	/**
	 * Method getDefault()
	 *
	 * @see gama.api.gaml.types.IType#getDefault()
	 */
	@Override
	public IContainer<?, ?> getDefault() { return type.getDefault(); }

	/**
	 * Method getVarKind()
	 *
	 * @see gama.api.gaml.types.IType#getVarKind()
	 */
	@Override
	public int getVarKind() { return ISymbolKind.Variable.CONTAINER; }

	/**
	 * Method getGetter()
	 *
	 * @see gama.api.gaml.types.IType#getGetter(java.lang.String)
	 */
	@Override
	public IArtefactProto getGetter(final String name) {
		return type.getGetter(name);
	}

	/**
	 * Method isSpeciesType()
	 *
	 * @see gama.api.gaml.types.IType#isSpeciesType()
	 */
	@Override
	public boolean isAgentType() {
		// Verify this
		return type.isAgentType();
	}

	/**
	 * Method isSkillType()
	 *
	 * @see gama.api.gaml.types.IType#isSkillType()
	 */
	@Override
	public boolean isSkillType() { return false; }

	/**
	 * Method defaultContentType()
	 *
	 * @see gama.api.gaml.types.IType#defaultContentType()
	 */
	@Override
	public IType<?> getContentType() { return contentsType; }

	/**
	 * Method defaultKeyType()
	 *
	 * @see gama.api.gaml.types.IType#defaultKeyType()
	 */
	@Override
	public IType<?> getKeyType() { return keyType; }

	/**
	 * Method getSpeciesName()
	 *
	 * @see gama.api.gaml.types.IType#getSpeciesName()
	 */
	@Override
	public String getSpeciesName() { return type.getSpeciesName(); }

	/**
	 * Method getSpecies()
	 *
	 * @see gama.api.gaml.types.IType#getSpecies()
	 */
	@Override
	public ISpeciesDescription getSpecies() {

		// if (result != null)
		return type.getSpecies();
		// return contentsType.getSpecies();
	}

	@Override
	public ISpeciesDescription getDenotedSpecies() {
		final ISpeciesDescription result = type.getSpecies();
		if (result != null) return result;
		return contentsType.getSpecies();
	}

	/**
	 * Method isAssignableFrom()
	 *
	 * @see gama.api.gaml.types.IType#isAssignableFrom(gama.api.gaml.types.IType)
	 */
	@Override
	public boolean computeIsAssignableFrom(final IType<?> l) {
		// Use polymorphic internal methods to avoid recursion
		return type.computeIsAssignableFrom(l.getGamlType()) && contentsType.computeIsAssignableFrom(l.getContentType())
				&& keyType.computeIsAssignableFrom(l.getKeyType());
	}

	@Override
	public boolean isAssignableFrom(final IType<?> l) {
		// Use cached version if typesManager is available
		if (typesManager != null) return typesManager.checkAssignability(this, l);
		// Fallback to direct computation
		return computeIsAssignableFrom(l);
	}

	/**
	 * Method isTranslatableInto()
	 *
	 * @see gama.api.gaml.types.IType#isTranslatableInto(gama.api.gaml.types.IType)
	 */
	@Override
	public boolean computeIsTranslatableInto(final IType<?> l) {
		// Use polymorphic internal methods to avoid recursion
		return type.computeIsTranslatableInto(l.getGamlType())
				&& contentsType.computeIsTranslatableInto(l.getContentType())
				&& keyType.computeIsTranslatableInto(l.getKeyType());
	}

	@Override
	public boolean isTranslatableInto(final IType<?> l) {
		// Use cached version if typesManager is available
		if (typesManager != null) return typesManager.checkTranslatability(this, l);
		// Fallback to direct computation
		return computeIsTranslatableInto(l);
	}

	@Override
	public boolean isParametricFormOf(final IType<?> l) {
		return !l.isParametricType() && type.equals(l);
	}

	@Override
	public boolean isParametricType() { return true; }

	/**
	 * Method setParent()
	 *
	 * @see gama.api.gaml.types.IType#setParent(gama.api.gaml.types.IType)
	 */
	@Override
	public void setParent(final IType<? super IContainer<?, ?>> p) {}

	/**
	 * Method getParent()
	 *
	 * @see gama.api.gaml.types.IType#getParent()
	 */
	@Override
	public IType<?> getParent() { return type; }

	/**
	 * Method coerce()
	 *
	 * @see gama.api.gaml.types.IType#coerce(gama.api.gaml.types.IType, gama.api.compilation.descriptions.IDescription)
	 */
	@Override
	public IType<?> coerce(final IType<?> expr, final IDescription context) {
		return null;
	}

	/**
	 * Method distanceTo()
	 *
	 * @see gama.api.gaml.types.IType#distanceTo(gama.api.gaml.types.IType)
	 */
	@Override
	public int computeDistanceTo(final IType<?> t) {
		// Use polymorphic internal methods to avoid recursion
		return t.getGamlType().computeDistanceTo(type) + t.getContentType().computeDistanceTo(contentsType)
				+ t.getKeyType().computeDistanceTo(keyType);
	}

	@Override
	public int distanceTo(final IType<?> t) {
		// Use cached version if typesManager is available
		if (typesManager != null) return typesManager.computeDistance(this, t);
		// Fallback to direct computation
		return computeDistanceTo(t);
	}

	/**
	 * Method setFieldGetters()
	 *
	 * @see gama.api.gaml.types.IType#setFieldGetters(java.util.Map)
	 */
	@Override
	public void setFieldGetters(final Map<String, IArtefactProto.Operator> map) {}

	/**
	 * Method canBeTypeOf()
	 *
	 * @see gama.api.gaml.types.IType#canBeTypeOf(gama.api.runtime.scope.IScope, java.lang.Object)
	 */
	@Override
	public boolean canBeTypeOf(final IScope s, final Object c) {
		return type.canBeTypeOf(s, c);
	}

	/**
	 * Method isContainer()
	 *
	 * @see gama.api.gaml.types.IType#isContainer()
	 */
	@Override
	public boolean isContainer() {
		return true; // ???
	}

	/**
	 * Method isFixedLength()
	 *
	 * @see gama.api.gaml.types.IType#isFixedLength()
	 */
	@Override
	public boolean isFixedLength() { return type.isFixedLength(); }

	/**
	 * Method findCommonSupertypeWith()
	 *
	 * @see gama.api.gaml.types.IType#findCommonSupertypeWith(gama.api.gaml.types.IType)
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public IType<? super IContainer<?, ?>> computeFindCommonSupertypeWith(final IType<?> iType) {
		// Use polymorphic internal methods to avoid recursion
		if (iType instanceof ParametricType) {
			final IType<?> pType = iType;
			final IType<?> cType = type.computeFindCommonSupertypeWith(pType.getGamlType());
			if (cType.isContainer()) {
				final IType<?> kt = keyType.computeFindCommonSupertypeWith(pType.getKeyType());
				final IType<?> ct = contentsType.computeFindCommonSupertypeWith(pType.getContentType());
				return (IType<? super IContainer<?, ?>>) GamaType.from(cType, kt, ct);
			}
			return (IType<? super IContainer<?, ?>>) cType;
		}
		return type;
	}

	@SuppressWarnings ("unchecked")
	@Override
	public IType<? super IContainer<?, ?>> findCommonSupertypeWith(final IType<?> iType) {
		// Use cached version if typesManager is available
		if (typesManager != null)
			return (IType<? super IContainer<?, ?>>) typesManager.computeCommonSupertype(this, iType);
		// Fallback to direct computation
		return computeFindCommonSupertypeWith(iType);
	}

	@Override
	public boolean isDrawable() { return type.isDrawable(); }

	@Override
	public IContainer<?, ?> cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return cast(scope, obj, param, keyType, contentsType, copy);
	}

	@Override
	public String toString() {
		if (type.id() == IType.LIST || type.id() == IType.MATRIX
				|| type.id() == IType.CONTAINER && keyType == Types.NO_TYPE)
			return type.toString() + "<" + contentsType.toString() + ">";
		if (type.id() == IType.SPECIES) return type.toString() + "<" + contentsType.toString() + ">";
		return type.toString() + "<" + keyType.toString() + ", " + contentsType.toString() + ">";
	}

	@Override
	public String asPattern() {
		final boolean vowel = Strings.CS.startsWithAny(type.getName(), vowels);
		return "${" + (vowel ? "an_" : "a_") + serializeToGaml(true) + "}";
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		if (type.id() == IType.LIST || type.id() == IType.MATRIX
				|| type.id() == IType.CONTAINER && keyType == Types.NO_TYPE)
			return type.toString() + "<" + contentsType.toString() + ">";
		if (type.id() == IType.SPECIES) return type.toString() + "<" + contentsType.toString() + ">";
		return type.toString() + "<" + keyType.serializeToGaml(includingBuiltIn) + ", "
				+ contentsType.serializeToGaml(includingBuiltIn) + ">";
	}

	@Override
	public IContainerType<?> typeIfCasting(final IExpression exp) {
		if (contentsType == Types.NO_TYPE || keyType == Types.NO_TYPE) {
			final IType<?> genericCast = type.typeIfCasting(exp);
			final IType<?> ct = contentsType == Types.NO_TYPE ? genericCast.getContentType() : contentsType;
			final IType<?> kt = keyType == Types.NO_TYPE ? genericCast.getKeyType() : keyType;
			return new ParametricType(typesManager, type, kt, ct);
		}
		return this;
	}

	/**
	 * Method getTitle()
	 *
	 * @see gama.api.compilation.descriptions.IGamlDescription#getTitle()
	 */
	@Override
	public String getTitle() { return "Parametric data type " + toString().replace('<', '[').replace('>', ']'); }

	/**
	 * Method getDocumentation()
	 *
	 * @see gama.api.compilation.descriptions.IGamlDescription#getDocumentation()
	 */
	@Override
	public IGamlDocumentation getDocumentation() { return type.getDocumentation(); }

	/**
	 * Method getName()
	 *
	 * @see gama.api.compilation.descriptions.IGamlDescription#getName()
	 */
	@Override
	public String getName() {
		if (name == null) { name = toString(); }
		return name;
	}

	@Override
	public boolean canCastToConst() {
		return type.canCastToConst() && contentsType.canCastToConst() && keyType.canCastToConst();
	}

	@Override
	public IContainerType<?> of(final IType<?> sub1) {
		IType<?> kt = getKeyType();
		IType<?> ct = sub1;
		if (ct == Types.NO_TYPE) {
			if (kt == Types.NO_TYPE) return this;
			ct = getContentType();
		}
		if (kt == Types.NO_TYPE) { kt = getKeyType(); }
		return new ParametricType(typesManager, this, kt, ct);

	}

	/**
	 * Of.
	 *
	 * @param typesManager
	 *            the types manager
	 * @param sub1
	 *            the sub 1
	 * @param sub2
	 *            the sub 2
	 * @return the i container type
	 */
	@Override
	public IContainerType<?> of(final IType<?> sub1, final IType<?> sub2) {
		IType<?> kt = sub1;
		IType<?> ct = sub2;
		if (ct == Types.NO_TYPE) {
			if (kt == Types.NO_TYPE) return this;
			ct = getContentType();
		}
		if (kt == Types.NO_TYPE) { kt = getKeyType(); }
		return new ParametricType(typesManager, this, kt, ct);

	}

	/**
	 * Method setDefiningPlugin()
	 *
	 * @see gama.api.gaml.types.IType#setDefiningPlugin(java.lang.String)
	 */
	@Override
	public void setDefiningPlugin(final String plugin) {}

	@Override
	public void collectMetaInformation(final GamlProperties meta) {
		type.collectMetaInformation(meta);
		contentsType.collectMetaInformation(meta);
		keyType.collectMetaInformation(meta);
	}

	@Override
	public boolean isNumber() { return false; }

	@Override
	public IType<?> getWrappedType() { return Types.NO_TYPE; }

	@Override
	public Map<String, IArtefactProto.Operator> getFieldGetters() { return type.getFieldGetters(); }

	@Override
	public IContainer<?, ?> deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		map2.put("requested_type", this);
		return type.deserializeFromJson(scope, map2);
	}

	@Override
	public IExpression getExpression() { return expression; }

	@Override
	public void setExpression(final IExpression exp) {}

	@Override
	public ITypesManager getTypesManager() { return typesManager; }

}