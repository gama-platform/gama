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
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.map.IMap;
import gama.api.types.misc.IContainer;
import gama.api.types.misc.IValue;
import gama.api.utils.GamlProperties;
import gama.dev.DEBUG;

/**
 * Base class for all type descriptions in the GAMA modeling platform. This class provides the foundational
 * infrastructure for the type system, enabling type conversions, type checking, and type-specific operations.
 * 
 * <p>
 * <b>Key Responsibilities:</b>
 * <ul>
 * <li>Type conversions and casting between GAML and Java types</li>
 * <li>Type hierarchy management (parent-child relationships)</li>
 * <li>Type compatibility checking (assignability, translatability)</li>
 * <li>Documentation and metadata generation</li>
 * <li>Field accessor management for type-specific attributes</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Type Registration:</b> To be recognized by GAML, subclasses must be annotated with the {@code @type} annotation,
 * which specifies the type's ID, name, Java support class, and other metadata.
 * </p>
 * 
 * <p>
 * <b>Type System Features:</b>
 * <ul>
 * <li><b>Type Hierarchy:</b> Types can have parent types, forming an inheritance hierarchy</li>
 * <li><b>Type Caching:</b> Uses TypesManager for cached type relations to improve performance</li>
 * <li><b>Parametric Types:</b> Primary types serve as basis for parametric types (e.g., list&lt;int&gt;)</li>
 * <li><b>Type Distance:</b> Computes type compatibility distance for method resolution</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Example Implementation:</b>
 * 
 * <pre>
 * {@code
 * @type(name = "int", id = IType.INT, wraps = {Integer.class})
 * public class GamaIntegerType extends GamaType<Integer> {
 *     public Integer cast(IScope scope, Object obj, Object param, boolean copy) {
 *         // Conversion logic here
 *     }
 * }
 * }
 * </pre>
 * </p>
 * 
 * @param <Support>
 *            the Java class that this type wraps
 * 
 * @author drogoul
 * @since 2010
 * 
 * @see IType
 * @see ParametricType
 * @see TypesManager
 */
public abstract class GamaType<Support> implements IType<Support> {

	static {
		DEBUG.OFF();
	}

	/** The unique identifier for this type in the GAMA type system. */
	protected int id;

	/** The GAML name of this type (e.g., "int", "float", "agent"). */
	protected String name;

	/** The Java class that this type wraps (e.g., Integer.class for int type). */
	protected Class<Support> support;

	/** 
	 * Map of field getter operators that provide access to type-specific attributes.
	 * For example, a point type might have "x" and "y" getters.
	 */
	Map<String, IArtefactProto.Operator> getters;

	/** The parent type in the type hierarchy, or null if this is a root type. */
	protected IType<? super Support> parent;

	/** The variable kind identifier, used for categorizing variables of this type. */
	protected int varKind;

	/** The name of the plugin that defines this type. */
	protected String plugin;

	/** An optional expression associated with this type instance. */
	IExpression expression;

	/** The types manager that owns this type - used for cached type relations. */
	protected final ITypesManager typesManager;

	/** Vowel characters for asPattern pattern generation (lowercase and uppercase). */
	private static final String VOWELS = "aeiouyAEIOUY";

	/** Cached simple name of the support class to avoid repeated reflection calls. */
	private transient String supportSimpleName;

	/**
	 * Constructs a new GAMA type with the specified types manager. The type's metadata (id, name, support class, etc.)
	 * is extracted from the {@code @type} annotation on the subclass during initialization.
	 *
	 * @param typesManager
	 *            the types manager that owns this type, used for cached type relation queries
	 */
	@SuppressWarnings ("unchecked")
	public GamaType(final ITypesManager typesManager) {
		this.typesManager = typesManager;
		init();
	}

	/**
	 * Initializes this type by extracting metadata from the {@code @type} annotation. This method must be called
	 * during construction and will throw an IllegalStateException if the annotation is missing.
	 * 
	 * <p>
	 * The following metadata is extracted:
	 * <ul>
	 * <li>Variable kind (kind field)</li>
	 * <li>Type ID (id field)</li>
	 * <li>Type name (name field)</li>
	 * <li>Support class (wraps field)</li>
	 * </ul>
	 * </p>
	 *
	 * @throws IllegalStateException
	 *             if the {@code @type} annotation is missing
	 */
	@SuppressWarnings ("unchecked")
	protected void init() {
		type annotation = getClass().getAnnotation(type.class);
		if (annotation == null) throw new IllegalStateException("Missing @type annotation on " + getClass().getName());
		this.varKind = annotation.kind();
		this.id = annotation.id();
		this.name = annotation.name();
		this.support = annotation.wraps()[0];
	}

	/**
	 * Returns a human-readable title for this type, used in UI displays and documentation.
	 * 
	 * @return a string in the format "Data type [name]"
	 */
	@Override
	public String getTitle() { return "Data type " + getName(); }

	/**
	 * Returns the number of type parameters for this type. Base types have 0 parameters, while parametric types
	 * (like list&lt;int&gt;) may have 1 or more.
	 * 
	 * @return 0 for non-parametric types
	 */
	@Override
	public int getNumberOfParameters() { return 0; }

	/**
	 * Returns the name of the plugin that defines this type.
	 * 
	 * @return the plugin name, or null if not set
	 */
	@Override
	public String getDefiningPlugin() { return plugin; }

	/**
	 * Generates comprehensive documentation for this type, including its Java support class, description from
	 * annotations, and accessible fields.
	 * 
	 * <p>
	 * The documentation is built from:
	 * <ul>
	 * <li>The Java class this type wraps</li>
	 * <li>The {@code @doc} annotation on the type class</li>
	 * <li>Documentation for all accessible fields/attributes</li>
	 * </ul>
	 * </p>
	 * 
	 * @return an IGamlDocumentation object containing the formatted documentation
	 */
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

	/**
	 * Appends documentation for all accessible fields of this type to the provided documentation object.
	 * 
	 * @param result
	 *            the documentation object to append field information to
	 */
	@Override
	public void documentFields(final IGamlDocumentation result) {
		if (getters != null) {
			if (getters.isEmpty()) { result.append("<p>").append("No fields accessible").append("</p>"); }
			for (final IArtefactProto.Operator f : getters.values()) { getFieldDocumentation(result, f); }
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
	 * Extracts and appends documentation for a specific field getter to the documentation object. Searches for
	 * {@code @variable} annotations matching the field name and includes their documentation.
	 *
	 * @param sb
	 *            the documentation object to append to
	 * @param prototype
	 *            the field getter operator prototype
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

	/**
	 * Returns the GAML name of this type.
	 * 
	 * @return the type name (e.g., "int", "float", "agent")
	 */
	@Override
	public String getName() { return name; }

	/**
	 * Serializes this type to its GAML representation.
	 * 
	 * @param includingBuiltIn
	 *            whether to include built-in types in serialization
	 * @return the GAML type name
	 */
	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return name;
	}

	/**
	 * Returns the variable kind identifier for this type, used for categorizing variables.
	 * 
	 * @return the variable kind constant
	 */
	@Override
	public int getVarKind() { return varKind; }

	/**
	 * Sets the parent type in the type hierarchy.
	 * 
	 * @param p
	 *            the parent type, or null for root types
	 */
	@Override
	public void setParent(final IType<? super Support> p) { parent = p; }

	/**
	 * Returns the parent type in the type hierarchy.
	 * 
	 * @return the parent type, or null if this is a root type
	 */
	@Override
	public IType<? super Support> getParent() { return parent; }

	/**
	 * Sets the map of field getter operators for this type. Each getter is copied with this type's signature to ensure
	 * proper type binding.
	 * 
	 * <p>
	 * Field getters provide access to type-specific attributes. For example, a point type might have "x" and "y"
	 * getters.
	 * </p>
	 *
	 * @param map
	 *            the map of field names to getter operators
	 */
	@Override
	public void setFieldGetters(final Map<String, IArtefactProto.Operator> map) {
		map.replaceAll((final String key, final IArtefactProto.Operator each) -> each.copyWithSignature(this));

		getters = map;
		// AD 20/09/13 Added the initialization of the type containing the fields

	}

	/**
	 * Returns the getter operator for a specific field name.
	 * 
	 * @param field
	 *            the field name to look up
	 * @return the getter operator, or null if the field doesn't exist
	 */
	@Override
	public IArtefactProto getGetter(final String field) {
		return getters == null ? null : getters.get(field);
	}

	/**
	 * Returns all field getter operators for this type.
	 * 
	 * @return an unmodifiable map of field names to getter operators, or an empty map if no getters are defined
	 */
	@Override
	public Map<String, IArtefactProto.Operator> getFieldGetters() {
		return getters == null ? Collections.emptyMap() : getters;
	}

	/**
	 * Converts an object to this type's support class. This is the primary type conversion method that must be
	 * implemented by all type subclasses.
	 * 
	 * <p>
	 * This method handles the conversion logic from arbitrary Java objects to the specific type this class represents.
	 * The conversion may or may not create a copy depending on the copy parameter.
	 * </p>
	 * 
	 * @param scope
	 *            the execution scope for the conversion
	 * @param obj
	 *            the object to convert
	 * @param param
	 *            optional parameter for conversion (usage varies by type)
	 * @param copy
	 *            whether to create a copy or reuse the object
	 * @return the converted object of type Support
	 * @throws GamaRuntimeException
	 *             if the conversion fails
	 */
	@Override
	public abstract Support cast(IScope scope, final Object obj, final Object param, boolean copy)
			throws GamaRuntimeException;

	/**
	 * Converts an object to this type with additional type parameters for container types. For non-container types,
	 * this delegates to the simpler cast method.
	 * 
	 * @param scope
	 *            the execution scope
	 * @param obj
	 *            the object to convert
	 * @param param
	 *            optional conversion parameter
	 * @param keyType
	 *            the desired key type for container types
	 * @param contentType
	 *            the desired content type for container types
	 * @param copy
	 *            whether to create a copy
	 * @return the converted object
	 * @throws GamaRuntimeException
	 *             if the conversion fails
	 */
	@Override
	public Support cast(final IScope scope, final Object obj, final Object param, final IType<?> keyType,
			final IType<?> contentType, final boolean copy) throws GamaRuntimeException {
		// by default
		return cast(scope, obj, param, copy);
	}

	/**
	 * Returns the unique identifier for this type.
	 * 
	 * @return the type ID constant (e.g., IType.INT, IType.FLOAT)
	 */
	@Override
	public int id() {
		return id;
	}

	/**
	 * Returns the hash code for this type, based on its unique ID.
	 * 
	 * @return the type ID as hash code
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * Checks equality based on type ID. Two types are equal if they have the same ID.
	 * 
	 * @param c
	 *            the object to compare with
	 * @return true if c is an IType with the same ID
	 */
	@Override
	public boolean equals(final Object c) {
		return c instanceof IType && ((IType<?>) c).id() == id;
	}

	/**
	 * Returns the string representation of this type, which is its GAML name.
	 * 
	 * @return the type name
	 */
	@Override
	public String toString() {
		return name;
	}

	/**
	 * Generates a pattern string for use in templates and code generation. The pattern includes proper article ("a" or
	 * "an") based on whether the type name starts with a vowel.
	 * 
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>"int" → "${a_int}"</li>
	 * <li>"agent" → "${an_agent}"</li>
	 * </ul>
	 * </p>
	 * 
	 * @return the pattern string in the format "${a_name}" or "${an_name}"
	 */
	@Override
	public String asPattern() {
		final boolean vowel = name != null && !name.isEmpty() && VOWELS.indexOf(name.charAt(0)) >= 0;
		return "${" + (vowel ? "an_" : "a_") + name + "}";
	}

	/**
	 * Returns the Java class that this type wraps.
	 * 
	 * @return the support class (e.g., Integer.class for int type)
	 */
	@Override
	public Class<? extends Support> toClass() {
		return support;
	}

	/**
	 * Checks whether this is an agent type.
	 * 
	 * @return true for agent types, false otherwise
	 */
	@Override
	public boolean isAgentType() { return false; }

	/**
	 * Checks whether this is a skill type.
	 * 
	 * @return true for skill types, false otherwise
	 */
	@Override
	public boolean isSkillType() { return false; }

	/**
	 * Returns the content type for container types.
	 * 
	 * @return the content type, or NO_TYPE for non-container types
	 */
	@Override
	public IType<?> getContentType() { return Types.NO_TYPE; }

	/**
	 * Returns the key type for map-like container types.
	 * 
	 * @return the key type, or NO_TYPE for non-map types
	 */
	@Override
	public IType<?> getKeyType() { return Types.NO_TYPE; }

	/**
	 * Returns the species name for agent types.
	 * 
	 * @return the species name, or null for non-agent types
	 */
	@Override
	public String getSpeciesName() { return null; }

	/**
	 * Returns the species description for agent types.
	 * 
	 * @return the species description, or null for non-agent types
	 */
	@Override
	public ISpeciesDescription getSpecies() { return null; }

	/**
	 * Returns the denoted species description for agent types.
	 * 
	 * @return the denoted species description, delegates to getSpecies() by default
	 */
	@Override
	public ISpeciesDescription getDenotedSpecies() { return getSpecies(); }

	/**
	 * Checks if this type is a supertype of another type by walking up the parent chain.
	 * 
	 * <p>
	 * This is a helper method used internally to determine type hierarchy relationships. It traverses the parent chain
	 * of the given type to see if this type appears as an ancestor.
	 * </p>
	 *
	 * @param type
	 *            the type to check
	 * @return true if this type is a supertype of the given type
	 */
	protected boolean isSuperTypeOf(final IType<?> type) {
		if (type == null) return false;
		for (IType<?> t = type.getParent(); t != null; t = t.getParent()) { if (this == t) return true; }
		return false;
	}

	/**
	 * Internal computation of isAssignableFrom without cache lookup. This method should only be called by TypesManager
	 * to avoid infinite recursion when building the type compatibility cache.
	 * 
	 * <p>
	 * A type A is assignable from type B if:
	 * <ul>
	 * <li>A and B are the same type, or</li>
	 * <li>A is a supertype of B in the type hierarchy</li>
	 * </ul>
	 * </p>
	 *
	 * @param t
	 *            the type to check
	 * @return true if this type is assignable from t
	 */
	@Override
	public boolean computeIsAssignableFrom(final IType<?> t) {
		return this == t || isSuperTypeOf(t);
	}

	/**
	 * Checks whether a value of the given type can be assigned to a variable of this type without explicit casting.
	 * This method uses cached type relations from TypesManager for performance.
	 * 
	 * <p>
	 * This is the primary method for checking type compatibility in assignments and parameter passing.
	 * </p>
	 *
	 * @param t
	 *            the type to check compatibility with
	 * @return true if values of type t can be assigned to this type
	 */
	@Override
	public boolean isAssignableFrom(final IType<?> t) {
		// Use cached version if typesManager is available
		if (typesManager != null) return typesManager.checkAssignability(this, t);
		// Fallback to direct computation
		return computeIsAssignableFrom(t);
	}

	/**
	 * Internal computation of isTranslatableInto without cache lookup. Used by TypesManager to avoid infinite
	 * recursion when building the cache.
	 * 
	 * <p>
	 * A type A is translatable into type B if B is assignable from A. This is the inverse relationship of
	 * isAssignableFrom.
	 * </p>
	 *
	 * @param t
	 *            the target type
	 * @return true if this type is translatable into t
	 */
	@Override
	public boolean computeIsTranslatableInto(final IType<?> t) {
		return t.isAssignableFrom(this);
	}

	/**
	 * Checks whether a value of this type can be converted to the target type. This method uses cached type relations
	 * for performance.
	 * 
	 * <p>
	 * This is equivalent to asking whether the target type is assignable from this type.
	 * </p>
	 *
	 * @param t
	 *            the target type
	 * @return true if this type can be converted to t
	 */
	@Override
	public final boolean isTranslatableInto(final IType<?> t) {
		// Use cached version if typesManager is available
		if (typesManager != null) return typesManager.checkTranslatability(this, t);
		// Fallback to direct computation
		return computeIsTranslatableInto(t);
	}

	/**
	 * Checks whether an object can be an instance of this type by verifying Java class compatibility.
	 * 
	 * @param scope
	 *            the execution scope
	 * @param c
	 *            the object to check
	 * @return true if the object is compatible with this type's support class
	 */
	@Override
	public boolean canBeTypeOf(final IScope scope, final Object c) {
		if (c == null) return acceptNullInstances();
		if (support == null) return false;
		return support.isInstance(c);
	}

	/**
	 * Checks whether this is a parametric type (e.g., list&lt;int&gt;, map&lt;string,float&gt;).
	 * 
	 * @return false for base types, true for parametric types
	 */
	@Override
	public boolean isParametricType() { return false; }

	/**
	 * Checks whether this type is a parametric form of another type.
	 * 
	 * <p>
	 * For example, list&lt;int&gt; is a parametric form of list.
	 * </p>
	 * 
	 * @param l
	 *            the type to compare with
	 * @return true if this is a parametric form of l
	 */
	@Override
	public boolean isParametricFormOf(final IType<?> l) {
		return false;
	}

	/**
	 * Determines whether this type accepts null/nil as a valid instance.
	 * 
	 * @return true if the default value is null, false otherwise
	 */
	protected boolean acceptNullInstances() {
		return getDefault() == null;
	}

	/**
	 * Attempts to coerce an expression type to be compatible with this type. Returns a coerced type if needed, or
	 * null if no coercion is required.
	 * 
	 * @param expr
	 *            the expression type to coerce
	 * @param context
	 *            the description context
	 * @return the coerced type, or null if no coercion needed
	 */
	@Override
	public IType<?> coerce(final IType<?> expr, final IDescription context) {
		// Nothing to do in the general case : we rely on Java polymorphism.
		return null;
	}

	/**
	 * Internal computation of the "distance" between this type and another type without cache lookup. Used by
	 * TypesManager to build the distance cache.
	 * 
	 * <p>
	 * Type distance is used for method overload resolution and type compatibility scoring. The distance is calculated
	 * as the number of steps in the type hierarchy needed to reach a common ancestor. A distance of 0 means the types
	 * are identical.
	 * </p>
	 * 
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>distance(int, int) = 0 (same type)</li>
	 * <li>distance(supertype, subtype) = number of inheritance levels</li>
	 * <li>distance(unrelated, type) = Integer.MAX_VALUE</li>
	 * </ul>
	 * </p>
	 *
	 * @param type
	 *            the target type to compute distance to
	 * @return the distance to the type, or Integer.MAX_VALUE if types are incompatible
	 */
	@Override
	public int computeDistanceTo(final IType<?> type) {
		if (type == this) return 0;
		if (type == null) return Integer.MAX_VALUE;
		if (isSuperTypeOf(type)) {
			IType<?> parent = type.getParent();
			return 1 + distanceTo(parent);
		}
		return 1 + getParent().distanceTo(type);
	}

	/**
	 * Computes the type compatibility distance to another type. Uses cached distances from TypesManager when
	 * available.
	 * 
	 * <p>
	 * The distance metric is used to resolve method overloading and determine the "best" type conversion when multiple
	 * options are available.
	 * </p>
	 *
	 * @param type
	 *            the target type
	 * @return the distance value, with 0 being identical types and Integer.MAX_VALUE being incompatible types
	 */
	@Override
	public final int distanceTo(final IType<?> type) {
		// Use cached version if typesManager is available
		if (typesManager != null) return typesManager.computeDistance(this, type);
		// Fallback to direct computation
		return computeDistanceTo(type);
	}

	/**
	 * Internal computation of the common supertype without cache lookup. Used by TypesManager to build the common
	 * supertype cache.
	 * 
	 * <p>
	 * The common supertype is the most specific type that both types can be assigned to. This is used for type
	 * inference in expressions like [1, 2.5] which needs to infer list&lt;float&gt; as the common type.
	 * </p>
	 * 
	 * <p>
	 * Algorithm:
	 * <ul>
	 * <li>If types are identical, return this type</li>
	 * <li>If other type is NO_TYPE, return this type (or NO_TYPE if default is null)</li>
	 * <li>If other type is translatable into this, return this type</li>
	 * <li>If this is translatable into other, return other type</li>
	 * <li>Otherwise, recursively find common supertype of parents</li>
	 * </ul>
	 * </p>
	 *
	 * @param type
	 *            the other type
	 * @return the common supertype
	 */
	@SuppressWarnings ({ "rawtypes", "unchecked" })
	@Override
	public IType<? super Support> computeFindCommonSupertypeWith(final IType<?> type) {
		if (type == this) return this;
		if (type == Types.NO_TYPE) return getDefault() == null ? this : (GamaNoType) type;
		if (type.isTranslatableInto(this)) return this;
		if (this.isTranslatableInto(type)) return (IType) type;
		return getParent().findCommonSupertypeWith(type.getParent());
	}

	/**
	 * Finds the most specific common supertype between this type and another type. Uses cached results from
	 * TypesManager when available.
	 * 
	 * <p>
	 * This method is crucial for type inference in collections and expressions where multiple types need to be unified
	 * under a single common type.
	 * </p>
	 * 
	 * <p>
	 * Example: findCommonSupertypeWith(int, float) returns float
	 * </p>
	 *
	 * @param type
	 *            the other type to find common supertype with
	 * @return the most specific type that both types are assignable to
	 */
	@SuppressWarnings ({ "rawtypes", "unchecked" })
	@Override
	public final IType<? super Support> findCommonSupertypeWith(final IType<?> type) {
		// Use cached version if typesManager is available
		if (typesManager != null) return (IType<? super Support>) typesManager.computeCommonSupertype(this, type);
		// Fallback to direct computation
		return computeFindCommonSupertypeWith(type);
	}

	/**
	 * Checks whether this type represents a container (list, map, matrix, etc.).
	 * 
	 * @return true for container types, false otherwise
	 */
	@Override
	public boolean isContainer() { return false; }

	/**
	 * Checks whether this type represents a numeric type (int, float).
	 * 
	 * @return true for numeric types, false otherwise
	 */
	@Override
	public boolean isNumber() { return false; }

	/**
	 * Checks whether this type has a fixed length/size.
	 * 
	 * @return true for fixed-length types (default), false for variable-length containers
	 */
	@Override
	public boolean isFixedLength() { return true; }

	/**
	 * Utility method to convert a value to a specific type. If the type is null or NONE, the value is returned
	 * unchanged.
	 * 
	 * @param scope
	 *            the execution scope
	 * @param value
	 *            the value to convert
	 * @param type
	 *            the target type
	 * @param copy
	 *            whether to create a copy
	 * @return the converted value, or the original value if no type specified
	 */
	public static Object toType(final IScope scope, final Object value, final IType<?> type, final boolean copy) {
		if (type == null || type.id() == IType.NONE) return value;
		return type.cast(scope, value, null, copy);
	}

	/**
	 * Returns the key type that should be used when casting an expression to this type. Default implementation returns
	 * this type's key type.
	 * 
	 * <p>
	 * Can be overridden by parametric types to provide dynamic key type inference.
	 * </p>
	 *
	 * @param exp
	 *            the expression being cast
	 * @return the key type for this type
	 */
	public IType<?> keyTypeIfCasting(final IExpression exp) {
		return getKeyType();
	}

	/**
	 * Returns the content type that should be used when casting an expression to this type. Default implementation
	 * returns this type's content type.
	 * 
	 * <p>
	 * Can be overridden by parametric types to provide dynamic content type inference.
	 * </p>
	 *
	 * @param exp
	 *            the expression being cast
	 * @return the content type for this type
	 */
	public IType<?> contentsTypeIfCasting(final IExpression exp) {
		return getContentType();
	}

	/**
	 * Returns the GAML type representation of this type (returns itself).
	 * 
	 * @return this type
	 */
	@Override
	public IType<Support> getGamlType() { return this; }

	/**
	 * Returns the type that would result from casting an expression to this type, potentially with specific key and
	 * content types for containers.
	 * 
	 * @param exp
	 *            the expression being cast
	 * @return the resulting type after casting
	 */
	@Override
	public IType<?> typeIfCasting(final IExpression exp) {
		return from(this, keyTypeIfCasting(exp), contentsTypeIfCasting(exp));
	}

	/**
	 * Creates a type from a species description.
	 * 
	 * @param species
	 *            the species description
	 * @return a species type with the appropriate content type
	 */
	public static IType<?> from(final ITypeDescription species) {
		return from(Types.SPECIES, Types.INT, species.getGamlType());
	}

	/**
	 * Creates a parametric container type with specific key and content types. If both key and content types are
	 * NO_TYPE, returns the original container type unchanged.
	 * 
	 * <p>
	 * This method is used to construct specialized container types like list&lt;int&gt; from the base list type.
	 * </p>
	 *
	 * @param t
	 *            the base container type
	 * @param keyType
	 *            the desired key type (or NO_TYPE to use default)
	 * @param contentType
	 *            the desired content type (or NO_TYPE to use default)
	 * @return a parametric container type with the specified key and content types
	 */
	public static IContainerType<?> from(final IContainerType<IContainer<?, ?>> t, final IType<?> keyType,
			final IType<?> contentType) {
		if ((keyType == null || keyType == Types.NO_TYPE) && (contentType == null || contentType == Types.NO_TYPE))
			return t;
		final IType<?> kt = keyType == Types.NO_TYPE ? t.getGamlType().getKeyType() : keyType;
		final IType<?> ct = contentType == Types.NO_TYPE ? t.getGamlType().getContentType() : contentType;
		return new ParametricType(t.getTypesManager(), t.getGamlType(), kt, ct);
	}

	/**
	 * Creates a type from a base type with specific key and content types. This is the general-purpose factory method
	 * for creating parametric types.
	 * 
	 * <p>
	 * For container types, this creates a parametric type if the key or content types differ from the base type's
	 * defaults. For non-container types, the base type is returned unchanged.
	 * </p>
	 *
	 * @param t
	 *            the base type
	 * @param keyType
	 *            the desired key type
	 * @param contentType
	 *            the desired content type
	 * @return a type with the specified key and content types, or the original type if not applicable
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

	/** Constant indicating extraction of the type itself in findCommonType. */
	public static final int TYPE = 0;

	/** Constant indicating extraction of the content type in findCommonType. */
	public static final int CONTENT = 1;

	/** Constant indicating extraction of the key type in findCommonType. */
	public static final int KEY = 2;

	/** Constant indicating extraction of the denoted type in findCommonType. */
	public static final int DENOTED = 3;

	/** Constant indicating a pair of types in findCommonType. */
	public static final int PAIR_OF_TYPES = 4;

	/**
	 * Finds the common type among an array of expressions by extracting a specific type aspect (TYPE, CONTENT, or
	 * KEY) from each expression.
	 * 
	 * <p>
	 * This method is useful for type inference in collections where you need to find a common content type or key
	 * type.
	 * </p>
	 * 
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>findCommonType([1, 2, 3], TYPE) → int</li>
	 * <li>findCommonType([list&lt;int&gt;, list&lt;float&gt;], CONTENT) → float</li>
	 * </ul>
	 * </p>
	 *
	 * @param elements
	 *            the array of expressions to analyze
	 * @param kind
	 *            the kind of type to extract (TYPE, CONTENT, or KEY)
	 * @return the common type among the extracted types
	 */
	public static IType<?> findCommonType(final IExpression[] elements, final int kind) {
		if (elements.length == 0) return Types.NO_TYPE;
		Set<IType<?>> types = new LinkedHashSet<>();
		for (final IExpression e : elements) {
			if (e == null) { continue; }
			final IType<?> eType = e.getGamlType();
			types.add(kind == TYPE ? eType : kind == CONTENT ? eType.getContentType() : eType.getKeyType());
		}
		return findCommonType(types.toArray(new IType[0]));
	}

	/**
	 * Finds the common type among an array of expressions (convenience method that uses TYPE extraction).
	 * 
	 * @param elements
	 *            the expressions to analyze
	 * @return the common type
	 */
	public static IType<?> findCommonType(final IExpression... elements) {
		return findCommonType(elements, TYPE);
	}

	/**
	 * Finds the most specific common supertype that all given types can be assigned to.
	 * 
	 * <p>
	 * This method iteratively combines types using findCommonSupertypeWith to determine the overall common type. The
	 * result is the most specific type that can hold values of all input types.
	 * </p>
	 * 
	 * <p>
	 * Special handling for NO_TYPE:
	 * <ul>
	 * <li>If a type has a non-null default, NO_TYPE becomes the result</li>
	 * <li>Otherwise, NO_TYPE is treated as compatible with any type</li>
	 * </ul>
	 * </p>
	 *
	 * @param types
	 *            the types to find common supertype for
	 * @return the common supertype, or NO_TYPE if the array is empty
	 */
	public static IType<?> findCommonType(final IType<?>... types) {
		if (types.length == 0) return Types.NO_TYPE;
		IType<?> result = types[0];
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
	 * Returns the GAML type of an object by examining its runtime characteristics.
	 * 
	 * <p>
	 * Priority order:
	 * <ol>
	 * <li>If object implements IValue, use its getGamlType()</li>
	 * <li>If object is IExpression, use its getGamlType()</li>
	 * <li>If object is null, return NO_TYPE</li>
	 * <li>Otherwise, look up type by Java class</li>
	 * </ol>
	 * </p>
	 *
	 * @param obj
	 *            the object to get the type of
	 * @return the GAML type of the object
	 */
	public static IType<?> of(final Object obj) {
		if (obj instanceof IValue) return ((IValue) obj).getGamlType();
		if (obj instanceof IExpression) return ((IExpression) obj).getGamlType();
		if (obj == null) return Types.NO_TYPE;
		return Types.get(obj.getClass());
	}

	/**
	 * Determines the actual runtime type of an object, which may be more specific than its compile-time type.
	 * 
	 * <p>
	 * For IValue objects, this calls computeRuntimeType() which may return a more specific type based on the object's
	 * runtime state. For other objects, this delegates to of().
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @param obj
	 *            the object to analyze
	 * @return the runtime type of the object
	 */
	public static IType<?> actualTypeOf(final IScope scope, final Object obj) {
		if (obj instanceof IValue v) return v.computeRuntimeType(scope);
		return of(obj);
	}

	/**
	 * Determines the more specific type between a casting type and an original type, returning the casting type if it
	 * requires explicit conversion.
	 * 
	 * @param castingType
	 *            the target type for conversion
	 * @param originalType
	 *            the source type
	 * @return the casting type if conversion is needed, otherwise the original type
	 */
	public static IType<?> findSpecificType(final IType<?> castingType, final IType<?> originalType) {
		return requiresCasting(castingType, originalType) ? castingType : originalType;
	}

	/**
	 * Checks if explicit type casting is required to convert from the original type to the casting type.
	 * 
	 * <p>
	 * Casting is required if the casting type is specified, not NO_TYPE, and the original type is not assignable to
	 * it.
	 * </p>
	 *
	 * @param castingType
	 *            the target type for conversion
	 * @param originalType
	 *            the source type
	 * @return true if explicit casting is needed, false if assignment is valid
	 */
	public static boolean requiresCasting(final IType<?> castingType, final IType<?> originalType) {
		return castingType != null && castingType != Types.NO_TYPE && !castingType.isAssignableFrom(originalType);
	}

	/**
	 * Sets the name of the plugin that defines this type.
	 * 
	 * @param plugin
	 *            the plugin name
	 */
	@Override
	public void setDefiningPlugin(final String plugin) { this.plugin = plugin; }

	/**
	 * Collects metadata information about this type for documentation and analysis purposes.
	 * 
	 * <p>
	 * Adds the defining plugin name and type name to the metadata properties if a plugin is defined.
	 * </p>
	 * 
	 * @param meta
	 *            the properties object to add metadata to
	 */
	@Override
	public void collectMetaInformation(final GamlProperties meta) {
		if (plugin != null) {
			meta.put(GamlProperties.PLUGINS, this.plugin);
			meta.put(GamlProperties.TYPES, this.name);
		}
	}

	/**
	 * Checks whether values of this type can be drawn/displayed graphically.
	 * 
	 * @return true for drawable types (geometries, images, etc.), false otherwise
	 */
	@Override
	public boolean isDrawable() { return false; }

	/**
	 * Returns the wrapped type for compound types, or NO_TYPE for simple types.
	 * 
	 * @return the wrapped type, or NO_TYPE if not a compound type
	 */
	@Override
	public IType<?> getWrappedType() { return Types.NO_TYPE; }

	/**
	 * Checks whether this is a compound type (a type that wraps another type).
	 * 
	 * @return true for compound types, false for simple types
	 */
	@Override
	public boolean isCompoundType() { return false; }

	/**
	 * Deserializes an object of this type from a JSON map representation.
	 * 
	 * <p>
	 * This default implementation throws an exception. Types that support JSON deserialization should override this
	 * method.
	 * </p>
	 * 
	 * @param scope
	 *            the execution scope
	 * @param map2
	 *            the JSON map containing the serialized data
	 * @return the deserialized object
	 * @throws GamaRuntimeException
	 *             if deserialization is not implemented for this type
	 */
	@Override
	public Support deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		throw GamaRuntimeException
				.error("The deserialization of " + getName() + " objects has not yet been implemented", scope);
	}

	/**
	 * Returns the optional expression associated with this type instance.
	 * 
	 * @return the expression, or null if not set
	 */
	@Override
	public IExpression getExpression() { return expression; }

	/**
	 * Sets an optional expression to be associated with this type instance.
	 * 
	 * @param exp
	 *            the expression to set
	 */
	@Override
	public void setExpression(final IExpression exp) { this.expression = exp; }

	/**
	 * Returns the types manager that owns this type, used for cached type relation queries.
	 * 
	 * @return the types manager
	 */
	@Override
	public ITypesManager getTypesManager() { return typesManager; }

}
