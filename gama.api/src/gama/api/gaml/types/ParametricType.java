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
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.map.IMap;
import gama.api.types.misc.IContainer;
import gama.api.types.misc.IRuntimeContainer;
import gama.api.utils.GamlProperties;
import gama.dev.DEBUG;

/**
 * Represents a parameterized container type in GAML.
 * <p>
 * Parametric types extend base container types (like list, map, matrix) with specific key and content type parameters.
 * For example:
 * <ul>
 * <li>{@code list<int>} - a list parameterized with integer content</li>
 * <li>{@code map<string, float>} - a map with string keys and float values</li>
 * <li>{@code matrix<bool>} - a matrix of booleans</li>
 * </ul>
 * </p>
 * <p>
 * Parametric types provide:
 * <ul>
 * <li>Type-safe containers with known element types</li>
 * <li>Better compile-time type checking</li>
 * <li>Proper type coercion and casting</li>
 * <li>Enhanced documentation and IDE support</li>
 * </ul>
 * </p>
 * <p>
 * This class wraps a base container type with key and content type parameters, delegating most operations to the base
 * type while maintaining type parameter information. Instances are immutable.
 * </p>
 *
 * @author drogoul
 * @since GAMA 1.0
 * @see IContainerType
 * @see GamaContainerType
 * @see IType
 */
public class ParametricType implements IContainerType<IRuntimeContainer<?, ?>> {

	static {
		DEBUG.OFF();
	}

	/** The base container type (e.g., list, map, matrix). */
	private final IContainerType<IRuntimeContainer<?, ?>> type;

	/** The content/value type parameter. */
	private final IType<?> contentsType;

	/** The key/index type parameter. */
	private final IType<?> keyType;

	/** The type expression representing this parametric type. */
	private final IExpression expression;

	/** The types manager inherited from the base type and parameters. */
	private final ITypesManager typesManager;

	/** The cached name of this parametric type. */
	private String name;

	/**
	 * Constructs a new parametric type.
	 * <p>
	 * The types manager is determined by finding the most specific manager among the base type and the parameter types,
	 * ensuring proper type resolution in multi-model contexts.
	 * </p>
	 *
	 * @param manager
	 *            the types manager context
	 * @param t
	 *            the base container type
	 * @param kt
	 *            the key type parameter
	 * @param ct
	 *            the content type parameter
	 */
	protected ParametricType(final ITypesManager manager, final IContainerType<IRuntimeContainer<?, ?>> t,
			final IType<?> kt,
			final IType<?> ct) {
		type = t;
		contentsType = ct;
		keyType = kt;
		expression = GAML.getExpressionFactory().createTypeExpression(this);
		// Inherit the typesManager from the base type
		this.typesManager = Types.findMoreSpecificTypesManagerAmong(manager, t.getTypesManager(), kt.getTypesManager(),
				ct.getTypesManager());
	}

	/**
	 * Returns the number of type parameters for this type.
	 * <p>
	 * Delegates to the base container type.
	 * </p>
	 *
	 * @return the number of type parameters
	 */
	@Override
	public int getNumberOfParameters() { return type.getNumberOfParameters(); }

	/**
	 * Indicates whether this is a compound type.
	 * <p>
	 * Parametric types are always compound as they have accessible components.
	 * </p>
	 *
	 * @return true
	 */
	@Override
	public boolean isCompoundType() { return true; }

	/**
	 * Checks equality with another object.
	 * <p>
	 * Two parametric types are equal if their base type, key type, and content type are all equal.
	 * </p>
	 *
	 * @param other
	 *            the object to compare with
	 * @return true if equal, false otherwise
	 */
	@Override
	public boolean equals(final Object other) {
		if (other instanceof ParametricType) return type.equals(((ParametricType) other).getGamlType())
				&& keyType.equals(((ParametricType) other).getKeyType())
				&& contentsType.equals(((ParametricType) other).getContentType());
		return false;
	}

	/**
	 * Computes the hash code for this parametric type.
	 * <p>
	 * Based on the hash codes of the base type, key type, and content type.
	 * </p>
	 *
	 * @return the hash code
	 */
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
	public IContainerType<IRuntimeContainer<?, ?>> getGamlType() { return type; }

	/**
	 * Method cast()
	 *
	 * @see gama.api.gaml.types.IType#cast(gama.api.runtime.scope.IScope, java.lang.Object, java.lang.Object,
	 *      gama.api.gaml.types.IType, gama.api.gaml.types.IType)
	 */
	@Override
	public IRuntimeContainer<?, ?> cast(final IScope scope, final Object obj, final Object param, final IType<?> kt,
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
	public Class<? extends IRuntimeContainer<?, ?>> toClass() {
		return type.toClass();
	}

	/**
	 * Method getDefault()
	 *
	 * @see gama.api.gaml.types.IType#getDefault()
	 */
	@Override
	public IRuntimeContainer<?, ?> getDefault() { return type.getDefault(); }

	/**
	 * Method getVarKind()
	 *
	 * @see gama.api.gaml.types.IType#getVarKind()
	 */
	@Override
	public ISymbolKind getVarKind() { return ISymbolKind.REGULAR; }

	/**
	 * Method getGetter()
	 *
	 * @see gama.api.gaml.types.IType#getGetter(java.lang.String)
	 */
	@Override
	public IArtefact getGetter(final String name) {
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
	public ITypeDescription getSpecies() {

		// if (result != null)
		return type.getSpecies();
		// return contentsType.getSpecies();
	}

	@Override
	public ITypeDescription getDenotedSpecies() {
		final ITypeDescription result = type.getSpecies();
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
	public void setParent(final IType<? super IRuntimeContainer<?, ?>> p) {}

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
		final int typeDistance = t.getGamlType().computeDistanceTo(type);
		if (typeDistance == Integer.MAX_VALUE) return Integer.MAX_VALUE;
		final int contentDistance = t.getContentType().computeDistanceTo(contentsType);
		if (contentDistance == Integer.MAX_VALUE) return Integer.MAX_VALUE;
		final int keyDistance = t.getKeyType().computeDistanceTo(keyType);
		if (keyDistance == Integer.MAX_VALUE) return Integer.MAX_VALUE;
		return typeDistance + contentDistance + keyDistance;
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
	public void setFieldGetters(final Map<String, IArtefact.Operator> map) {}

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
	public IType<? super IRuntimeContainer<?, ?>> computeFindCommonSupertypeWith(final IType<?> iType) {
		// Use polymorphic internal methods to avoid recursion
		if (iType instanceof ParametricType) {
			final IType<?> pType = iType;
			final IType<?> cType = type.computeFindCommonSupertypeWith(pType.getGamlType());
			if (cType.isContainer()) {
				final IType<?> kt = keyType.computeFindCommonSupertypeWith(pType.getKeyType());
				final IType<?> ct = contentsType.computeFindCommonSupertypeWith(pType.getContentType());
				return (IType<? super IRuntimeContainer<?, ?>>) GamaType.from(cType, kt, ct);
			}
			return (IType<? super IRuntimeContainer<?, ?>>) cType;
		}
		return (IType<? super IRuntimeContainer<?, ?>>) type.computeFindCommonSupertypeWith(iType);
	}

	@SuppressWarnings ("unchecked")
	@Override
	public IType<? super IRuntimeContainer<?, ?>> findCommonSupertypeWith(final IType<?> iType) {
		// Use cached version if typesManager is available
		if (typesManager != null)
			return (IType<? super IRuntimeContainer<?, ?>>) typesManager.computeCommonSupertype(this, iType);
		// Fallback to direct computation
		return computeFindCommonSupertypeWith(iType);
	}

	@Override
	public boolean isDrawable() { return type.isDrawable(); }

	@Override
	public IRuntimeContainer<?, ?> cast(final IScope scope, final Object obj, final Object param, final boolean copy)
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
	public Map<String, IArtefact.Operator> getFieldGetters() { return type.getFieldGetters(); }

	@Override
	public IRuntimeContainer<?, ?> deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
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