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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

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
 * The superclass of all types descriptions in GAMA. Provides convenience methods, as well as some basic definitions.
 * Types allow to manipulate any Java class as a type in GAML. To be recognized by GAML, subclasses must be annotated
 * with the @type annotation (see GamlAnnotations).
 *
 * <p>Types are primarily used for conversions between values. They are also intended to support the operators specific to
 * the objects they encompass (but this is not mandatory, as these operators need to be defined as static ones (and thus
 * can be defined anywhere))</p>
 *
 * <p>Primary (simple) types also serve as the basis of parametric types (see ParametricType).</p>
 *
 * @param <Support> The Java class that this type wraps and manipulates
 * 
 * @author drogoul
 * @since 25 August 2010
 * @version 2025-03
 */
public abstract class GamaType<Support> implements IType<Support> {

	/** Debug flag for the class */
	static {
		DEBUG.OFF();
	}

	// Constants for pattern matching and documentation
	/** Vowels used for article selection in pattern generation */
	private static final String[] VOWELS = { "a", "e", "i", "o", "u", "A", "E", "I", "O", "U" };
	
	/** Data type prefix for title generation */
	private static final String DATA_TYPE_PREFIX = "Data type ";
	
	/** Java objects wrapper description */
	private static final String JAVA_WRAPPER_DESC = "Wraps Java objects of type ";
	
	/** Field access message when no fields are available */
	private static final String NO_FIELDS_MSG = "No fields accessible";

	/** Type comparison constants */
	public static final int TYPE = 0;
	public static final int CONTENT = 1;
	public static final int KEY = 2;
	public static final int DENOTED = 3;
	public static final int PAIR_OF_TYPES = 4;

	// ===========================================
	// Instance Fields
	// ===========================================

	/** The unique identifier of this type */
	protected int id;

	/** The name of this type */
	protected String name;

	/** The Java class that this type supports */
	protected Class<Support> support;

	/** The getters for field access */
	private Map<String, IArtefactProto.Operator> getters;

	/** The parent type */
	protected IType<? super Support> parent;

	/** The variable kind for this type */
	protected int varKind;

	/** The defining plugin name */
	protected String plugin;

	/** The expression associated with this type */
	private IExpression expression;

	// ===========================================
	// Basic Type Information Methods
	// ===========================================

	@Override
	public String getTitle() { 
		return DATA_TYPE_PREFIX + getName(); 
	}

	@Override
	public int getNumberOfParameters() { 
		return 0; 
	}

	@Override
	public String getDefiningPlugin() { 
		return plugin; 
	}

	@Override
	public String getName() { 
		return name; 
	}

	@Override
	public void setName(final String name) {
		// Name is immutable for types
	}

	@Override
	public int getVarKind() { 
		return varKind; 
	}

	@Override
	public int id() {
		return id;
	}

	@Override
	public Class<? extends Support> toClass() {
		return support;
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return name;
	}

	// ===========================================
	// Documentation Methods
	// ===========================================

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
		result.append("<i>").append(JAVA_WRAPPER_DESC).append(getSupportName()).append("</i>");
		if (documentation != null) { result.append("<p>").append(documentation.value()).append("</p>"); }

		documentFields(result);

		return result;
	}

	@Override
	public void documentFields(final IGamlDocumentation result) {
		if (getters != null) {
			if (getters.isEmpty()) { result.append("<p>").append(NO_FIELDS_MSG).append("</p>"); }
			// sb.append("<b><br/>Fields :</b><ul>");
			for (final IArtefactProto.Operator f : getters.values()) { getFieldDocumentation(result, f); }

			// result.append("</ul>");
		}
	}

	/**
	 * Gets the support name.
	 *
	 * @return the support name
	 */
	public String getSupportName() { return support.getSimpleName(); }

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

	// ===========================================
	// Type Initialization and Configuration
	// ===========================================

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
	public void setParent(final IType<? super Support> p) { parent = p; }

	@Override
	public IType<? super Support> getParent() { return parent; }

	/**
	 * Sets the field getters for this type. Field getters allow access to specific
	 * properties of instances of this type.
	 *
	 * @param map the map containing field name to getter prototype mappings
	 */
	@Override
	public void setFieldGetters(final Map<String, IArtefactProto.Operator> map) {
		map.replaceAll((final String key, final IArtefactProto.Operator each) -> each.copyWithSignature(this));
		getters = map;
		// AD 20/09/13 Added the initialization of the type containing the fields
	}

	/**
	 * Gets the getter for a specific field.
	 *
	 * @param field the field name to get the getter for
	 * @return the artifact prototype for accessing the field, or null if not found
	 */
	@Override
	public IArtefactProto getGetter(final String field) {
		if (getters == null) return null;
		IArtefactProto result = getters.get(field);
		return result;
	}

	/**
	 * Gets all field getters for this type.
	 *
	 * @return an immutable map of field name to getter mappings; empty map if no getters are defined
	 */
	@Override
	public Map<String, IArtefactProto.Operator> getFieldGetters() {
		return getters == null ? Collections.<String, IArtefactProto.Operator>emptyMap() : getters;
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

	// ===========================================
	// Object Lifecycle Methods
	// ===========================================

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(final Object c) {
		if (c instanceof IType) return ((IType<?>) c).id() == id;
		return false;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Returns a pattern representation of this type name for use in documentation.
	 * Uses proper article (a/an) based on whether the type name starts with a vowel.
	 *
	 * @return a pattern string in the format "${a_typename}" or "${an_typename}"
	 */
	@Override
	public String asPattern() {
		final boolean vowel = StringUtils.startsWithAny(name, VOWELS);
		return "${" + (vowel ? "an_" : "a_") + name + "}";
	}

	/**
	 * Tests whether this type represents an agent type.
	 * 
	 * @return false by default; subclasses may override for agent types
	 */
	@Override
	public boolean isAgentType() { return false; }

	/**
	 * Tests whether this type represents a skill type.
	 * 
	 * @return false by default; subclasses may override for skill types
	 */
	@Override
	public boolean isSkillType() { return false; }

	/**
	 * Gets the content type for container types.
	 * 
	 * @return NO_TYPE by default; container types should override this
	 */
	@Override
	public IType<?> getContentType() { return Types.NO_TYPE; }

	/**
	 * Gets the key type for container types.
	 * 
	 * @return NO_TYPE by default; container types should override this
	 */
	@Override
	public IType<?> getKeyType() { return Types.NO_TYPE; }

	/**
	 * Gets the species name for species types.
	 * 
	 * @return null by default; species types should override this
	 */
	@Override
	public String getSpeciesName() { return null; }

	/**
	 * Gets the species description for species types.
	 * 
	 * @return null by default; species types should override this
	 */
	@Override
	public ISpeciesDescription getSpecies() { return null; }

	/**
	 * Gets the denoted species description.
	 * 
	 * @return the result of getSpecies() by default
	 */
	@Override
	public ISpeciesDescription getDenotedSpecies() { return getSpecies(); }

	/**
	 * Checks if this type is a supertype of the specified type.
	 * Traverses the type hierarchy to determine inheritance relationships.
	 *
	 * @param type the type to check
	 * @return true if this type is a supertype of the given type
	 */
	protected boolean isSuperTypeOf(final IType<?> type) {
		if (type == null) return false;
		IType t = type.getParent();
		while (t != null) {
			if (this == t) return true;
			t = t.getParent();
		}
		return false;
	}

	/**
	 * Tests whether values of the specified type can be assigned to variables of this type.
	 * This includes direct equality and subtype relationships.
	 *
	 * @param t the type to test assignability from
	 * @return true if this type can accept values of the specified type
	 */
	@Override
	public boolean isAssignableFrom(final IType<?> t) {
		return this == t || isSuperTypeOf(t);
	}

	/**
	 * Tests whether values of this type can be translated into the specified type.
	 * This is the inverse of isAssignableFrom.
	 *
	 * @param t the target type for translation
	 * @return true if this type can be translated into the target type
	 */
	@Override
	public boolean isTranslatableInto(final IType<?> t) {
		return t.isAssignableFrom(this);
	}

	/**
	 * Tests whether the specified object can be considered an instance of this type.
	 * This performs runtime type checking based on the Java class hierarchy.
	 *
	 * @param scope the scope for context (may be needed for dynamic typing)
	 * @param c the object to test
	 * @return true if the object can be typed as this type
	 */
	@Override
	public boolean canBeTypeOf(final IScope scope, final Object c) {
		if (c == null) return acceptNullInstances();
		return support.isAssignableFrom(c.getClass());
	}

	/**
	 * Tests whether this is a parametric type (e.g., List&lt;String&gt;).
	 *
	 * @return false by default; parametric types should override this
	 */
	@Override
	public boolean isParametricType() { return false; }

	/**
	 * Tests whether this type is a parametric form of the specified type.
	 *
	 * @param l the type to compare against
	 * @return false by default; parametric types should override this
	 */
	@Override
	public boolean isParametricFormOf(final IType<?> l) {
		return false;
	}

	/**
	 * Tests whether this type accepts null instances.
	 * Types that have a null default value typically accept null instances.
	 * 
	 * @return true if this type can have nil as an instance
	 */
	protected boolean acceptNullInstances() {
		return getDefault() == null;
	}

	/**
	 * Attempts to coerce an expression's type to be compatible with this type.
	 * This method allows types to perform custom type coercion logic.
	 *
	 * @param expr the expression type to potentially coerce
	 * @param context the description context for the operation
	 * @return the coerced type, or null if no coercion is needed
	 */
	@Override
	public IType<?> coerce(final IType<?> expr, final IDescription context) {
		// Nothing to do in the general case : we rely on Java polymorphism.
		return null;
	}

	/**
	 * Calculates the inheritance distance between this type and another type.
	 * Used for method resolution and type compatibility checking.
	 *
	 * @param type the target type to measure distance to
	 * @return the inheritance distance (0 for same type, Integer.MAX_VALUE for incompatible types)
	 */
	@Override
	public int distanceTo(final IType<?> type) {
		if (type == this) return 0;
		if (type == null) return Integer.MAX_VALUE;
		if (isSuperTypeOf(type)) return 1 + distanceTo(type.getParent());
		return 1 + getParent().distanceTo(type);
	}

	/**
	 * Finds the most specific common supertype between this type and another type.
	 * This is crucial for type inference in expressions with mixed types.
	 *
	 * @param type the other type to find common supertype with
	 * @return the most specific common supertype
	 */
	@SuppressWarnings ({ "rawtypes", "unchecked" })
	@Override
	public IType<? super Support> findCommonSupertypeWith(final IType<?> type) {
		if (type == this) return this;
		if (type == Types.NO_TYPE) return getDefault() == null ? this : (GamaNoType) type;
		if (type.isTranslatableInto(this)) return this;
		if (this.isTranslatableInto(type)) return (IType) type;
		return getParent().findCommonSupertypeWith(type.getParent());
	}

	/**
	 * Tests whether this type represents a container.
	 *
	 * @return false by default; container types should override this
	 */
	@Override
	public boolean isContainer() { return false; }

	/**
	 * Tests whether this type represents a numeric type.
	 *
	 * @return false by default; numeric types should override this
	 */
	@Override
	public boolean isNumber() { return false; }

	/**
	 * Tests whether this type has a fixed length.
	 * Fixed length types have a predetermined size that cannot change.
	 *
	 * @return true by default; variable-length types should override this
	 */
	@Override
	public boolean isFixedLength() { return true; }

	/**
	 * Converts the given value to the specified type, performing type casting if necessary.
	 * This is a utility method that handles null type checks and delegation to proper casting.
	 *
	 * @param scope the scope for the operation
	 * @param value the value to convert
	 * @param type the target type
	 * @param copy whether to create a copy during conversion
	 * @return the converted object, or the original value if no conversion is needed
	 */
	public static Object toType(final IScope scope, final Object value, final IType<?> type, final boolean copy) {
		if (type == null || type.id() == IType.NONE) return value;
		return type.cast(scope, value, null, copy);
	}

	/**
	 * Determines the key type when casting, used by expression type inference.
	 *
	 * @param exp the expression being processed
	 * @return the appropriate key type for this type
	 */
	public IType<?> keyTypeIfCasting(final IExpression exp) {
		return getKeyType();
	}

	/**
	 * Determines the content type when casting, used by expression type inference.
	 *
	 * @param exp the expression being processed
	 * @return the appropriate content type for this type
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
	 * Creates a type from a species description.
	 *
	 * @param species the species description
	 * @return a species type with the appropriate content type
	 */
	public static IType<?> from(final ITypeDescription species) {
		return from(Types.SPECIES, Types.INT, species.getGamlType());
	}

	/**
	 * Creates a parametric container type with the specified key and content types.
	 * If both types are null or NO_TYPE, returns the original container type.
	 *
	 * @param t the base container type
	 * @param keyType the desired key type
	 * @param contentType the desired content type
	 * @return a parametric container type with the specified type parameters
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
	 * Creates a type from a base type with specified key and content types.
	 * For container types, creates a parametric type. For non-container types,
	 * returns the original type if it's already compatible.
	 *
	 * @param t the base type
	 * @param keyType the desired key type
	 * @param contentType the desired content type
	 * @return a type with the specified type parameters, or the original type if not applicable
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

	/**
	 * Finds the most specific common type among the given expressions based on the specified kind.
	 * This is essential for type inference in complex expressions.
	 *
	 * @param elements the array of expressions to analyze
	 * @param kind the kind of type to compare (TYPE, CONTENT, KEY)
	 * @return the most specific common type among all expressions
	 */
	public static IType<?> findCommonType(final IExpression[] elements, final int kind) {
		final IType<?> result = Types.NO_TYPE;
		if (elements.length == 0) return result;
		Set<IType<?>> types = new LinkedHashSet<>();
		for (final IExpression e : elements) {
			if (e == null) { continue; }
			final IType<?> eType = e.getGamlType();
			types.add(kind == TYPE ? eType : kind == CONTENT ? eType.getContentType() : eType.getKeyType());
		}
		final IType<?>[] array = types.toArray(new IType[0]);
		return findCommonType(array);
	}

	/**
	 * Finds the most specific common type among the given expressions.
	 * This is a convenience method for finding common types without specifying kind.
	 *
	 * @param elements the expressions to analyze
	 * @return the most specific common type among all expressions
	 */
	public static IType<?> findCommonType(final IExpression... elements) {
		return findCommonType(elements, TYPE);
	}

	/**
	 * Finds the most specific common type among the given type array.
	 * Uses inheritance relationships to determine the best common supertype.
	 *
	 * @param types the array of types to compare
	 * @return the most specific common type among all types
	 */
	public static IType<?> findCommonType(final IType<?>... types) {
		IType<?> result = Types.NO_TYPE;
		if (types.length == 0) return result;
		result = types[0];
		if (types.length == 1) return result;
		for (int i = 1; i < types.length; i++) {
			final IType<?> currentType = types[i];
			if (currentType == Types.NO_TYPE) {
				if (result.getDefault() != null) { result = Types.NO_TYPE; }
			} else if (result == Types.NO_TYPE) {
				result = currentType.findCommonSupertypeWith(result);
			} else {
				result = result.findCommonSupertypeWith(currentType);
			}
		}
		return result;
	}

	/**
	 * Returns the GAMA type corresponding to the given object.
	 * This method performs runtime type introspection to determine the appropriate type.
	 *
	 * @param obj the object to get the type for
	 * @return the GAMA type corresponding to the object
	 */
	public static IType<?> of(final Object obj) {
		if (obj instanceof IValue) return ((IValue) obj).getGamlType();
		if (obj instanceof IExpression) return ((IExpression) obj).getGamlType();
		if (obj == null) return Types.NO_TYPE;
		return Types.get(obj.getClass());
	}

	/**
	 * Returns the actual runtime type of an object, considering dynamic typing.
	 * This method is more comprehensive than {@link #of(Object)} as it considers runtime context.
	 *
	 * @param scope the scope for runtime context
	 * @param obj the object to analyze
	 * @return the actual runtime type of the object
	 */
	public static IType<?> actualTypeOf(final IScope scope, final Object obj) {
		if (obj instanceof IValue v) return v.computeRuntimeType(scope);
		return of(obj);
	}

	/**
	 * Determines the most specific type between a casting type and an original type.
	 * Used in type inference to choose the best type when casting is involved.
	 *
	 * @param castingType the target casting type
	 * @param originalType the original type of the expression
	 * @return the more specific type to use
	 */
	public static IType<?> findSpecificType(final IType<?> castingType, final IType<?> originalType) {
		return requiresCasting(castingType, originalType) ? castingType : originalType;
	}

	/**
	 * Tests whether casting is required from the original type to the casting type.
	 * This determines if explicit type conversion is needed.
	 *
	 * @param castingType the target casting type
	 * @param originalType the original type
	 * @return true if casting is required, false otherwise
	 */
	public static boolean requiresCasting(final IType<?> castingType, final IType<?> originalType) {
		if (castingType == null || castingType == Types.NO_TYPE || castingType.isAssignableFrom(originalType))
			return false;
		return true;
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
