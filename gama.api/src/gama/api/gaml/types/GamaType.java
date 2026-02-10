/*******************************************************************************************************
 *
 * GamaType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.compilation.prototypes.IArtefactProto;
import gama.api.data.objects.IContainer;
import gama.api.data.objects.IMap;
import gama.api.data.objects.IValue;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.utils.GamlProperties;
import gama.dev.DEBUG;

/**
 * Written by drogoul Modified on 25 aout 2010
 *
 * The superclass of all types descriptions in GAMA. Provides convenience methods, as well as some basic definitions.
 * Types allow to manipulate any Java class as a type in GAML. To be recognized by GAML, subclasses must be annotated
 * with the @type annotation (see GamlAnnotations).
 *
 * Types are primarily used for conversions between values. They are also intended to support the operators specific to
 * the objects they encompass (but this is not mandatory, as these operators need to be defined as static ones (and thus
 * can be defined anywhere)
 *
 * Primary (simple) types also serve as the basis of parametric types (see ParametricType).
 *
 */
public abstract class GamaType<Support> implements IType<Support> {

	static {
		DEBUG.OFF();
	}

	/** The id. */
	protected int id;

	/** The name. */
	protected String name;

	/** The support. */
	protected Class<Support> support;

	/** The getters. */
	Map<String, IArtefactProto.Operator> getters;

	/** The parent. */
	protected IType<? super Support> parent;

	/** The parented. */
	// protected boolean parented;

	/** The var kind. */
	protected int varKind;

	/** The plugin. */
	protected String plugin;

	/** The expression. */
	IExpression expression;

	/** Vowel characters for asPattern pattern generation (lowercase and uppercase). */
	private static final String VOWELS = "aeiouyAEIOUY";

	/** The support simple name. */
	// Cached simple name of the support class to avoid repeated reflection
	private transient String supportSimpleName;

	@Override
	public String getTitle() { return "Data type " + getName(); }

	@Override
	public int getNumberOfParameters() { return 0; }

	@Override
	public String getDefiningPlugin() { return plugin; }

	@Override
	public IGamlDocumentation getDocumentation() {
		IGamlDocumentation result = new GamlRegularDocumentation();
		doc documentation = getClass().getAnnotation(doc.class);
		if (documentation == null) {
			final type t = getClass().getAnnotation(type.class);
			if (t != null) {
				final doc[] docs = t.doc();
				if (docs != null && docs.length > 0) { documentation = docs[0]; }
			}
		}
		result.append("<i>").append("Wraps Java objects of type ").append(getSupportName()).append("</i>");
		if (documentation != null) { result.append("<p>").append(documentation.value()).append("</p>"); }

		documentFields(result);

		return result;
	}

	@Override
	public void documentFields(final IGamlDocumentation result) {
		if (getters != null) {
			if (getters.isEmpty()) { result.append("<p>").append("No fields accessible").append("</p>"); }
			// sb.append("<b><br/>Fields :</b><ul>");
			for (final IArtefactProto.Operator f : getters.values()) { getFieldDocumentation(result, f); }

			// result.append("</ul>");
		}
	}

	/**
	 * Gets the support name, with caching to avoid repeated reflection calls.
	 *
	 * @return the simple name of the support class, or "null" if support is undefined
	 */
	public String getSupportName() {
		if (supportSimpleName != null) return supportSimpleName;
		if (support == null) return "null";
		return supportSimpleName = support.getSimpleName();
	}

	/**
	 * Gets the field documentation.
	 *
	 * @param prototype
	 *            the prototype
	 * @return the field documentation
	 */
	void getFieldDocumentation(final IGamlDocumentation sb, final IArtefactProto.Operator prototype) {

		final vars annot = prototype.getJavaBase().getAnnotation(vars.class);
		if (annot != null) {
			final variable[] allVars = annot.value();
			for (final variable v : allVars) {
				if (v.name().equals(prototype.getName())) {
					if (v.doc().length > 0) {
						sb.set("Accessible fields: ", v.name(), new GamlConstantDocumentation(v.doc()[0].value()));
					}
					break;
				}
			}
		}
	}

	@Override
	public String getName() { return name; }

	@Override
	public void setName(final String name) {
		// Nothing
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return name;
	}

	@SuppressWarnings ("unchecked")
	@Override
	public void init(final int varKind, final int id, final String name, final Class<Support> support) {
		this.varKind = varKind;
		this.id = id;
		this.name = name;
		this.support = support;
	}

	@Override
	public void setSupport(final Class<Support> clazz) {
		support = clazz;
		// supports = new Class[] { clazz };
	}

	@Override
	public int getVarKind() { return varKind; }

	@Override
	public void setParent(final IType<? super Support> p) { parent = p; }

	@Override
	public IType<? super Support> getParent() { return parent; }

	/**
	 * Sets the field getters.
	 *
	 * @param map
	 *            the map
	 */
	@Override
	public void setFieldGetters(final Map<String, IArtefactProto.Operator> map) {
		map.replaceAll((final String key, final IArtefactProto.Operator each) -> each.copyWithSignature(this));

		getters = map;
		// AD 20/09/13 Added the initialization of the type containing the fields

	}

	@Override
	public IArtefactProto getGetter(final String field) {
		return getters == null ? null : getters.get(field);
	}

	@Override
	public Map<String, IArtefactProto.Operator> getFieldGetters() {
		return getters == null ? Collections.emptyMap() : getters;
	}

	@Override
	public abstract Support cast(IScope scope, final Object obj, final Object param, boolean copy)
			throws GamaRuntimeException;

	@Override
	public Support cast(final IScope scope, final Object obj, final Object param, final IType<?> keyType,
			final IType<?> contentType, final boolean copy) throws GamaRuntimeException {
		// by default
		return cast(scope, obj, param, copy);
	}

	@Override
	public int id() {
		return id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(final Object c) {
		return c instanceof IType && ((IType<?>) c).id() == id;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public String asPattern() {
		final boolean vowel = name != null && !name.isEmpty() && VOWELS.indexOf(name.charAt(0)) >= 0;
		return "${" + (vowel ? "an_" : "a_") + name + "}";
	}

	@Override
	public Class<? extends Support> toClass() {
		return support;
	}

	@Override
	public boolean isAgentType() { return false; }

	@Override
	public boolean isSkillType() { return false; }

	@Override
	public IType<?> getContentType() { return Types.NO_TYPE; }

	@Override
	public IType<?> getKeyType() { return Types.NO_TYPE; }

	@Override
	public String getSpeciesName() { return null; }

	@Override
	public ISpeciesDescription getSpecies() { return null; }

	@Override
	public ISpeciesDescription getDenotedSpecies() { return getSpecies(); }

	/**
	 * Checks if this type is a supertype of another type by walking up the parent chain.
	 *
	 * @param type the type to check
	 * @return true if this type is a supertype of the given type
	 */
	protected boolean isSuperTypeOf(final IType<?> type) {
		if (type == null) return false;
		for (IType<?> t = type.getParent(); t != null; t = t.getParent()) {
			if (this == t) return true;
		}
		return false;
	}

	@Override
	public boolean isAssignableFrom(final IType<?> t) {
		return this == t || isSuperTypeOf(t);
	}

	@Override
	public boolean isTranslatableInto(final IType<?> t) {
		return t.isAssignableFrom(this);
	}

	@Override
	public boolean canBeTypeOf(final IScope scope, final Object c) {
		if (c == null) return acceptNullInstances();
		if (support == null) return false;
		return support.isInstance(c);
	}

	@Override
	public boolean isParametricType() { return false; }

	@Override
	public boolean isParametricFormOf(final IType<?> l) {
		return false;
	}

	/**
	 * @return true if this type can have nil as an instance
	 */
	protected boolean acceptNullInstances() {
		return getDefault() == null;
	}

	@Override
	public IType<?> coerce(final IType<?> expr, final IDescription context) {
		// Nothing to do in the general case : we rely on Java polymorphism.
		return null;
	}

	@Override
	public int distanceTo(final IType<?> type) {
		if (type == this) return 0;
		if (type == null) return Integer.MAX_VALUE;
		if (isSuperTypeOf(type)) return 1 + distanceTo(type.getParent());
		return 1 + getParent().distanceTo(type);
	}

	@SuppressWarnings ({ "rawtypes", "unchecked" })
	@Override
	public IType<? super Support> findCommonSupertypeWith(final IType<?> type) {
		if (type == this) return this;
		if (type == Types.NO_TYPE) return getDefault() == null ? this : (GamaNoType) type;
		if (type.isTranslatableInto(this)) return this;
		if (this.isTranslatableInto(type)) return (IType) type;
		return getParent().findCommonSupertypeWith(type.getParent());
	}

	@Override
	public boolean isContainer() { return false; }

	@Override
	public boolean isNumber() { return false; }

	@Override
	public boolean isFixedLength() { return true; }

	/**
	 * To type.
	 *
	 * @param scope the scope
	 * @param value the value
	 * @param type the type
	 * @param copy the copy
	 * @return the object
	 */
	public static Object toType(final IScope scope, final Object value, final IType<?> type, final boolean copy) {
		if (type == null || type.id() == IType.NONE) return value;
		return type.cast(scope, value, null, copy);
	}

	/**
	 * Key type if casting.
	 *
	 * @param exp the expression
	 * @return the key type for this type (defaults to getKeyType)
	 */
	public IType<?> keyTypeIfCasting(final IExpression exp) {
		return getKeyType();
	}

	/**
	 * Contents type if casting.
	 *
	 * @param exp the expression
	 * @return the content type for this type (defaults to getContentType)
	 */
	public IType<?> contentsTypeIfCasting(final IExpression exp) {
		return getContentType();
	}

	@Override
	public IType<Support> getGamlType() { return this; }

	@Override
	public IType<?> typeIfCasting(final IExpression exp) {
		return from(this, keyTypeIfCasting(exp), contentsTypeIfCasting(exp));
	}

	/**
	 * From.
	 *
	 * @param species
	 *            the species
	 * @return the i type
	 */
	public static IType<?> from(final ITypeDescription species) {
		return from(Types.SPECIES, Types.INT, species.getGamlType());
	}

	/**
	 * From.
	 *
	 * @param t
	 *            the t
	 * @param keyType
	 *            the key type
	 * @param contentType
	 *            the content type
	 * @return the i container type
	 */
	public static IContainerType<?> from(final IContainerType<IContainer<?, ?>> t, final IType<?> keyType,
			final IType<?> contentType) {
		if ((keyType == null || keyType == Types.NO_TYPE) && (contentType == null || contentType == Types.NO_TYPE))
			return t;
		final IType<?> kt = keyType == Types.NO_TYPE ? t.getGamlType().getKeyType() : keyType;
		final IType<?> ct = contentType == Types.NO_TYPE ? t.getGamlType().getContentType() : contentType;
		return ParametricType.createParametricType(t.getGamlType(), kt, ct);
	}

	/**
	 * From.
	 *
	 * @param t
	 *            the t
	 * @param keyType
	 *            the key type
	 * @param contentType
	 *            the content type
	 * @return the i type
	 */

	/**
	 * From.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param t
	 *            the t
	 * @param keyType
	 *            the key type
	 * @param contentType
	 *            the content type
	 * @return the i type
	 * @date 27 déc. 2023
	 */
	@SuppressWarnings ({ "unchecked", "rawtypes" })
	public static IType<?> from(final IType<?> t, final IType<?> keyType, final IType<?> contentType) {
		if (keyType == null || contentType == null) return t;
		if (t instanceof IContainerType) {
			if (!(t instanceof GamaSpeciesType) && contentType.isAssignableFrom(t.getContentType())
					&& keyType.isAssignableFrom(t.getKeyType()))
				return t;
			return from((IContainerType) t, keyType, contentType);
		}
		return t;
	}

	/** The Constant TYPE. */
	public static final int TYPE = 0;

	/** The Constant CONTENT. */
	public static final int CONTENT = 1;

	/** The Constant KEY. */
	public static final int KEY = 2;

	/** The Constant DENOTED. */
	public static final int DENOTED = 3;

	/** The Constant PAIR_OF_TYPES. */
	public static final int PAIR_OF_TYPES = 4;

	/**
	 * Find common type.
	 *
	 * @param elements the expressions
	 * @param kind the kind of type to extract (TYPE, CONTENT, or KEY)
	 * @return the common type among the extracted types
	 */
	public static IType<?> findCommonType(final IExpression[] elements, final int kind) {
		if (elements.length == 0) return Types.NO_TYPE;
		Set<IType<?>> types = new LinkedHashSet<>();
		for (final IExpression e : elements) {
			if (e == null) continue;
			final IType<?> eType = e.getGamlType();
			types.add(kind == TYPE ? eType : kind == CONTENT ? eType.getContentType() : eType.getKeyType());
		}
		return findCommonType(types.toArray(new IType[0]));
	}

	/**
	 * Find common type.
	 *
	 * @param elements the expressions
	 * @return the common type
	 */
	public static IType<?> findCommonType(final IExpression... elements) {
		return findCommonType(elements, TYPE);
	}

	/**
	 * Find common type.
	 *
	 * @param types the types to compare
	 * @return the common supertype
	 */
	public static IType<?> findCommonType(final IType<?>... types) {
		if (types.length == 0) return Types.NO_TYPE;
		IType<?> result = types[0];
		if (types.length == 1) return result;
		for (int i = 1; i < types.length; i++) {
			final IType<?> currentType = types[i];
			if (currentType == Types.NO_TYPE) {
				if (result.getDefault() != null) result = Types.NO_TYPE;
			} else if (result == Types.NO_TYPE) {
				result = currentType.findCommonSupertypeWith(result);
			} else {
				result = result.findCommonSupertypeWith(currentType);
			}
		}
		return result;
	}

	/**
	 * Return the type of the object passed in parameter
	 *
	 * @param obj the object to get the type of
	 * @return the GAML type of the object
	 */
	public static IType<?> of(final Object obj) {
		if (obj instanceof IValue) return ((IValue) obj).getGamlType();
		if (obj instanceof IExpression) return ((IExpression) obj).getGamlType();
		if (obj == null) return Types.NO_TYPE;
		return Types.get(obj.getClass());
	}

	/**
	 * Actual type of.
	 *
	 * @param obj
	 *            the obj
	 * @return the i type
	 */
	public static IType<?> actualTypeOf(final IScope scope, final Object obj) {
		if (obj instanceof IValue v) return v.computeRuntimeType(scope);
		return of(obj);
	}

	/**
	 * @return
	 */
	public static IType<?> findSpecificType(final IType<?> castingType, final IType<?> originalType) {
		return requiresCasting(castingType, originalType) ? castingType : originalType;
	}

	/**
	 * Checks if the casting type requires explicit type conversion from the original type.
	 *
	 * @param castingType the target type for conversion
	 * @param originalType the source type
	 * @return true if casting is needed
	 */
	public static boolean requiresCasting(final IType<?> castingType, final IType<?> originalType) {
		return castingType != null && castingType != Types.NO_TYPE && !castingType.isAssignableFrom(originalType);
	}

	@Override
	public void setDefiningPlugin(final String plugin) { this.plugin = plugin; }

	@Override
	public void collectMetaInformation(final GamlProperties meta) {
		if (plugin != null) {
			meta.put(GamlProperties.PLUGINS, this.plugin);
			meta.put(GamlProperties.TYPES, this.name);
		}
	}

	@Override
	public boolean isDrawable() { return false; }

	@Override
	public IType<?> getWrappedType() { return Types.NO_TYPE; }

	@Override
	public boolean isCompoundType() { return false; }

	@Override
	public Support deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		throw GamaRuntimeException
				.error("The deserialization of " + getName() + " objects has not yet been implemented", scope);
	}

	@Override
	public IExpression getExpression() { return expression; }

	@Override
	public void setExpression(final IExpression exp) { this.expression = exp; }

}
