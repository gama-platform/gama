/*******************************************************************************************************
 *
 * SymbolDescription.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.descriptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.precompiler.GamlProperties;
import gama.core.common.preferences.GamaPreferences;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.dev.DEBUG;
import gama.gaml.compilation.GAML;
import gama.gaml.compilation.GamlCompilationError;
import gama.gaml.compilation.GamlCompilationError.GamlCompilationErrorType;
import gama.gaml.compilation.ISymbol;
import gama.gaml.expressions.IExpression;
import gama.gaml.factories.DescriptionFactory;
import gama.gaml.interfaces.IGamlDescription;
import gama.gaml.interfaces.IGamlIssue;
import gama.gaml.statements.Facets;
import gama.gaml.statements.IStatement;
import gama.gaml.types.GamaType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * Abstract base class for all GAML symbol descriptions. Provides the core functionality for describing, validating, and
 * compiling GAML symbols (statements, variables, species, etc.) during the model parsing phase.
 * <p>
 * SymbolDescription serves as an intermediary representation between the raw text/AST of a GAML model and the
 * runtime objects that execute during simulation. It handles validation of syntax, type checking, and compilation
 * of symbols into executable elements.
 * 
 * @author Alexis Drogoul
 * @since 16 Mar 2010
 */
public abstract class SymbolDescription implements IDescription {

	static {
		DEBUG.OFF();
	}

	/** Set of facet names that can provide type information for the symbol. */
	protected static final Set<String> typeProviderFacets = Collections.unmodifiableSet(new HashSet<>(
			Arrays.asList(VALUE, TYPE, AS, SPECIES, OF, OVER, FROM, INDEX, FUNCTION, UPDATE, INIT, DEFAULT)));

	/** 
	 * Stores the state flags of this description.
	 * Flags represent boolean attributes like BuiltIn, Validated, etc.
	 */
	private final EnumSet<Flag> state = EnumSet.noneOf(Flag.class);

	/** The facets associated with this symbol. */
	private Facets facets;

	/** The underlying EMF object from the parser. */
	protected final EObject element;

	/** The enclosing description that contains this description. */
	private IDescription enclosingDescription;

	/** The model description this symbol belongs to. */
	private ModelDescription modelDescription;

	/** The name of the symbol that originated this description. */
	protected String originName;

	/** The name of this symbol. */
	protected String name;

	/** The keyword that defines this symbol type. */
	protected final String keyword;

	/** The GAML type of this symbol. */
	private IType<?> type;

	/** The prototype information for this symbol type. */
	final SymbolProto proto;

	/**
	 * Creates a new symbol description.
	 *
	 * @param keyword the GAML keyword for this symbol (e.g., "species", "action", "var")
	 * @param superDesc the parent description containing this symbol
	 * @param source the EMF AST node representing this symbol
	 * @param facets the facets defined for this symbol
	 */
	public SymbolDescription(final String keyword, final IDescription superDesc, final EObject source,
			final Facets facets) {
		this.keyword = keyword;
		this.facets = facets;
		element = source;
		setIf(Flag.BuiltIn, element == null);
		// See #385 -- we need to remove the NO_TYPE_INFERENCE facet from the list of facets if it is present, after
		// having set the flag
		if (facets != null && facets.containsKey(NO_TYPE_INFERENCE)) {
			set(Flag.NoTypeInference);
			facets.remove(NO_TYPE_INFERENCE);
		}
		if (facets != null && facets.containsKey(ORIGIN)) {
			originName = facets.getLabel(ORIGIN);
			facets.remove(ORIGIN);
		} else if (superDesc != null) { originName = superDesc.getName(); }
		setEnclosingDescription(superDesc);
		proto = DescriptionFactory.getProto(getKeyword(), getSpeciesContext());

	}

	// ---- State management

	/**
	 * Sets a state flag for this description.
	 *
	 * @param flag the flag to set
	 */
	protected void set(final Flag flag) {
		state.add(flag);
	}

	/**
	 * Sets a state flag conditionally based on the given condition.
	 *
	 * @param flag the flag to set
	 * @param condition if true, the flag is set; otherwise, it is unset
	 */
	protected void setIf(final Flag flag, final boolean condition) {
		if (condition) {
			set(flag);
		} else {
			unSet(flag);
		}
	}

	/**
	 * Removes a state flag from this description.
	 *
	 * @param flag the flag to unset
	 */
	protected void unSet(final Flag flag) {
		state.remove(flag);
	}

	/**
	 * Checks if a specific flag is set for this description.
	 *
	 * @param flag the flag to check
	 * @return true if the flag is set, false otherwise
	 */
	protected boolean isSet(final Flag flag) {
		return state.contains(flag);
	}

	/**
	 * Checks if this description has any facets.
	 *
	 * @return true if the description has facets, false otherwise
	 */
	protected boolean hasFacets() {
		return facets != null;
	}

	/**
	 * Checks if this description has facets that are not in the given set.
	 *
	 * @param others a set of facet names
	 * @return true if the description has facets not included in the set, false otherwise
	 */
	protected boolean hasFacetsNotIn(final Set<String> others) {
		if (facets == null) return false;
		return !visitFacets((facetName, exp) -> others.contains(facetName));
	}

	/**
	 * Gets the serializer for this symbol description.
	 * Creates one if it doesn't already exist.
	 *
	 * @return the symbol serializer
	 */
	@Override
	public final SymbolSerializer<? extends SymbolDescription> getSerializer() {
		final SymbolProto p = getMeta();
		SymbolSerializer<? extends SymbolDescription> d = p.getSerializer();
		if (d == null) {
			d = createSerializer();
			p.setSerializer(d);
		}
		return d;
	}

	/**
	 * Gets the expression description for the specified facet.
	 *
	 * @param string the facet name
	 * @return the expression description, or null if the facet doesn't exist
	 */
	@Override
	public IExpressionDescription getFacet(final String string) {
		return !hasFacets() ? null : facets.get(string);
	}

	/**
	 * Gets the compiled expression for the first matching facet name.
	 *
	 * @param strings one or more facet names to check
	 * @return the first found expression, or null if none are found
	 */
	@Override
	public IExpression getFacetExpr(final String... strings) {
		return !hasFacets() ? null : facets.getExpr(strings);
	}

	/**
	 * Gets the expression description for the first matching facet name.
	 *
	 * @param strings one or more facet names to check
	 * @return the first found expression description, or null if none are found
	 */
	@Override
	public IExpressionDescription getFacet(final String... strings) {
		return !hasFacets() ? null : facets.getDescr(strings);
	}

	/**
	 * Checks if a specific facet exists in this description.
	 *
	 * @param string the facet name to check
	 * @return true if the facet exists, false otherwise
	 */
	@Override
	public boolean hasFacet(final String string) {
		return hasFacets() && facets.containsKey(string);
	}

	/**
	 * Gets the literal value of a facet.
	 *
	 * @param string the facet name
	 * @return the literal value as a string, or null if the facet doesn't exist
	 */
	@Override
	public String getLitteral(final String string) {
		return !hasFacets() ? null : facets.getLabel(string);
	}

	/**
	 * Sets a facet with the given expression description.
	 *
	 * @param name the facet name
	 * @param desc the expression description
	 */
	@Override
	public void setFacetExprDescription(final String name, final IExpressionDescription desc) {
		if (!hasFacets()) { facets = new Facets(); }
		facets.put(name, desc);
	}

	/**
	 * Sets a facet with a pre-compiled expression.
	 *
	 * @param string the facet name
	 * @param exp the compiled expression
	 */
	@Override
	public void setFacet(final String string, final IExpression exp) {
		if (!hasFacets()) { facets = new Facets(); }
		facets.putExpression(string, exp);
	}

	/**
	 * Removes the specified facets from this description.
	 *
	 * @param strings the facet names to remove
	 */
	@Override
	public void removeFacets(final String... strings) {
		if (!hasFacets()) return;
		for (final String s : strings) { facets.remove(s); }
		if (facets.isEmpty()) { facets = null; }
	}

	/**
	 * Visits the facets in the specified set, applying the visitor to each.
	 *
	 * @param names the set of facet names to visit
	 * @param visitor the visitor to apply
	 * @return true if all visits succeeded, false if any visit returned false
	 */
	@Override
	public final boolean visitFacets(final Set<String> names, final IFacetVisitor visitor) {
		if (!hasFacets()) return true;
		return facets.forEachFacetIn(names, visitor);
	}

	/**
	 * Visits all facets in this description, applying the visitor to each.
	 *
	 * @param visitor the visitor to apply
	 * @return true if all visits succeeded, false if any visit returned false
	 */
	@Override
	public final boolean visitFacets(final IFacetVisitor visitor) {
		if (!hasFacets()) return true;
		return facets.forEachFacet(visitor);
	}

	/**
	 * Gets the type denoted by the first matching facet name.
	 *
	 * @param s one or more facet names to check
	 * @return the type denoted by the first matching facet, or NO_TYPE if none match
	 */
	public IType<?> getTypeDenotedByFacet(final String... s) {
		if (!hasFacets()) return Types.NO_TYPE;
		return getTypeDenotedByFacet(facets.getFirstExistingAmong(s), Types.NO_TYPE);
	}

	/**
	 * Returns the first facet name found among the specified names.
	 *
	 * @param strings the facet names to check
	 * @return the first matching facet name, or null if none match
	 */
	@Override
	public String firstFacetFoundAmong(final String... strings) {
		if (!hasFacets()) return null;
		return facets.getFirstExistingAmong(strings);
	}

	/**
	 * Gets the type denoted by a specific facet, with a default type if not found.
	 *
	 * @param s the facet name
	 * @param defaultType the default type to return if the facet doesn't exist
	 * @return the type denoted by the facet, or the default type
	 */
	public IType<?> getTypeDenotedByFacet(final String s, final IType<?> defaultType) {
		if (!hasFacets()) return defaultType;
		return facets.getTypeDenotedBy(s, this, defaultType);
	}

	/**
	 * Creates a clean copy of the facets.
	 *
	 * @return a copy of the facets, or null if there are no facets
	 */
	public Facets getFacetsCopy() { 
		return !hasFacets() ? null : facets.cleanCopy(); 
	}

	/**
	 * Creates a serializer for this symbol description.
	 *
	 * @return a new serializer
	 */
	protected SymbolSerializer<? extends SymbolDescription> createSerializer() {
		return SYMBOL_SERIALIZER;
	}

	/**
	 * Serializes this description to GAML code.
	 *
	 * @param includingBuiltIn whether to include built-in elements in the serialization
	 * @return the GAML code representation of this symbol
	 */
	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return getSerializer().serialize(this, includingBuiltIn);
	}

	/**
	 * Collects metadata information about this symbol.
	 *
	 * @param meta the properties object to populate with metadata
	 */
	@Override
	public void collectMetaInformation(final GamlProperties meta) {
		getSerializer().collectMetaInformation(this, meta);
	}

	/**
	 * Gets the kind of this symbol.
	 *
	 * @return the symbol kind as defined in its prototype
	 */
	@Override
	public int getKind() { 
		return getMeta().getKind(); 
	}

	/**
	 * Compiles all facets that can provide type information.
	 * This ensures type provider facets are compiled before they are needed.
	 */
	protected void compileTypeProviderFacets() {
		visitFacets((facetName, exp) -> {
			if (typeProviderFacets.contains(facetName)) { 
				exp.compile(SymbolDescription.this); 
			}
			return true;
		});
	}

	/**
	 * Compiles specific facets that may provide type information.
	 *
	 * @param names the names of the facets to compile
	 */
	protected void compileTypeProviderFacets(final String... names) {
		for (String s : names) {
			IExpressionDescription exp = getFacet(s);
			if (exp != null) { 
				exp.compile(this); 
			}
		}
	}

	/**
	 * Gets the metadata prototype for this symbol.
	 *
	 * @return the symbol prototype
	 */
	@Override
	public final SymbolProto getMeta() { 
		return proto; 
	}

	/**
	 * Internal method to handle error, warning, and info flags during validation.
	 * Determines the proper reporting method based on the error type and context.
	 *
	 * @param s the message text
	 * @param code the issue code
	 * @param type the error type (Error, Warning, or Info)
	 * @param source the source object where the issue occurred
	 * @param data additional data for the issue
	 * @throws GamaRuntimeException if there's no way to report the error in compile time
	 */
	protected void flagError(final String s, final String code, final GamlCompilationErrorType type,
			final EObject source, final String... data) throws GamaRuntimeException {

		if (type == GamlCompilationErrorType.Warning && !GamaPreferences.Modeling.WARNINGS_ENABLED.getValue()
				|| type == GamlCompilationErrorType.Info && !GamaPreferences.Modeling.INFO_ENABLED.getValue())
			return;

		IDescription desc = this;
		EObject e = source;
		if (e == null) { e = getUnderlyingElement(); }
		while (e == null && desc != null) {
			desc = desc.getEnclosingDescription();
			if (desc != null) { e = desc.getUnderlyingElement(); }
		}
		// throws a runtime exception if there is no way to signal the error in the source
		// (i.e. we are probably in a runtime scenario)
		if (e == null || e.eResource() == null || e.eResource().getURI().path().contains(SYNTHETIC_RESOURCES_PREFIX)) {
			if (type == GamlCompilationErrorType.Error)
				throw GamaRuntimeException.error(s, gama.core.runtime.GAMA.getRuntimeScope());
			return;

		}
		final ValidationContext c = getValidationContext();
		if (c == null) {
			DEBUG.ERR((type == GamlCompilationErrorType.Warning ? "Warning" : "Error") + ": " + s);
			return;
		}
		c.add(new GamlCompilationError(s, code, e, type, data));
	}

	/**
	 * Checks if the given EMF object is synthetic (not from an actual source file).
	 *
	 * @param e the EMF object to check
	 * @return true if the object is synthetic, false otherwise
	 */
	private boolean isSynthetic(final EObject e) {
		return e == null || e.eResource() == null || e.eResource().getURI().path().contains(SYNTHETIC_RESOURCES_PREFIX);
	}

	/**
	 * Associates documentation with an EMF object in the current context.
	 *
	 * @param e the EMF object to document
	 * @param desc the documentation to associate with the object
	 */
	@Override
	public void document(final EObject e, final IGamlDescription desc) {
		final ValidationContext c = getValidationContext();
		if (c != null) { 
			c.setGamlDocumentation(e, desc); 
		}
	}

	/**
	 * Reports a general error with this description.
	 *
	 * @param message the error message
	 */
	@Override
	public void error(final String message) {
		error(message, IGamlIssue.GENERAL);
	}

	/** Constant for empty data arrays. */
	final static String[] EMPTY_DATA = {};

	/**
	 * Reports an error with a specific issue code.
	 *
	 * @param message the error message
	 * @param code the issue code
	 */
	@Override
	public void error(final String message, final String code) {
		flagError(message, code, GamlCompilationErrorType.Error, getUnderlyingElement(), EMPTY_DATA);
	}

	/**
	 * Reports an error related to a specific EMF object.
	 *
	 * @param s the error message
	 * @param code the issue code
	 * @param facet the EMF object associated with the error
	 * @param data additional data for the error
	 */
	@Override
	public void error(final String s, final String code, final EObject facet, final String... data) {
		flagError(s, code, GamlCompilationErrorType.Error, facet, data);
	}

	/**
	 * Reports an error related to a specific facet.
	 *
	 * @param s the error message
	 * @param code the issue code
	 * @param facet the name of the facet with the error
	 * @param data additional data for the error
	 */
	@Override
	public void error(final String s, final String code, final String facet, final String... data) {
		flagError(s, code, GamlCompilationErrorType.Error,
				this.getUnderlyingElement(facet, IGamlIssue.UNKNOWN_FACET.equals(code)),
				data == null || data.length == 0 ? new String[] { facet } : data);
	}

	/**
	 * Reports an informational message with a specific issue code.
	 *
	 * @param message the info message
	 * @param code the issue code
	 */
	@Override
	public void info(final String message, final String code) {
		flagError(message, code, GamlCompilationErrorType.Info, getUnderlyingElement(), EMPTY_DATA);
	}

	/**
	 * Reports an informational message related to a specific EMF object.
	 *
	 * @param s the info message
	 * @param code the issue code
	 * @param facet the EMF object associated with the info
	 * @param data additional data for the info
	 */
	@Override
	public void info(final String s, final String code, final EObject facet, final String... data) {
		flagError(s, code, GamlCompilationErrorType.Info, facet, data);
	}

	/**
	 * Reports an informational message related to a specific facet.
	 *
	 * @param s the info message
	 * @param code the issue code
	 * @param facet the name of the facet
	 * @param data additional data for the info
	 */
	@Override
	public void info(final String s, final String code, final String facet, final String... data) {
		flagError(s, code, GamlCompilationErrorType.Info, this.getUnderlyingElement(facet, false),
				data == null || data.length == 0 ? new String[] { facet } : data);
	}

	/**
	 * Reports a warning with a specific issue code.
	 *
	 * @param message the warning message
	 * @param code the issue code
	 */
	@Override
	public void warning(final String message, final String code) {
		flagError(message, code, GamlCompilationErrorType.Warning, null, EMPTY_DATA);
	}

	/**
	 * Reports a warning related to a specific EMF object.
	 *
	 * @param s the warning message
	 * @param code the issue code
	 * @param object the EMF object associated with the warning
	 * @param data additional data for the warning
	 */
	@Override
	public void warning(final String s, final String code, final EObject object, final String... data) {
		flagError(s, code, GamlCompilationErrorType.Warning, object, data);
	}

	/**
	 * Reports a warning related to a specific facet.
	 *
	 * @param s the warning message
	 * @param code the issue code
	 * @param facet the name of the facet
	 * @param data additional data for the warning
	 */
	@Override
	public void warning(final String s, final String code, final String facet, final String... data) {
		flagError(s, code, GamlCompilationErrorType.Warning,
				this.getUnderlyingElement(facet, IGamlIssue.UNKNOWN_FACET.equals(code)),
				data == null || data.length == 0 ? new String[] { facet } : data);
	}

	/**
	 * Gets the keyword that defines this symbol.
	 *
	 * @return the keyword string
	 */
	@Override
	public String getKeyword() { 
		return keyword; 
	}

	/**
	 * Gets the name of this symbol.
	 * If the name is not set, attempts to get it from the NAME facet.
	 *
	 * @return the symbol name
	 */
	@Override
	public String getName() {
		if (name == null) { 
			name = getLitteral(NAME); 
		}
		return name;
	}

	/**
	 * Sets the name of this symbol and updates the NAME facet if available.
	 *
	 * @param name the new name for the symbol
	 */
	@Override
	public void setName(final String name) {
		this.name = name;
		if (getMeta().getPossibleFacets().containsKey(NAME)) {
			setFacetExprDescription(NAME, LabelExpressionDescription.create(name));
		}
	}

	/**
	 * Cleans up resources used by this description.
	 * Recursively disposes of children first.
	 */
	@Override
	public void dispose() {
		// DEBUG.LOG("Disposing " + getKeyword() + " " + getName());
		if (isBuiltIn()) return;
		visitOwnChildren(DISPOSING_VISITOR);
		if (hasFacets()) { 
			facets.dispose(); 
		}
		facets = null;
		enclosingDescription = null;
		modelDescription = null;
		setType(null);
	}

	/**
	 * Gets the model description this symbol belongs to.
	 *
	 * @return the model description
	 */
	@Override
	public ModelDescription getModelDescription() { 
		return modelDescription; 
	}

	/**
	 * Adds multiple child descriptions to this description.
	 *
	 * @param originalChildren the children to add
	 */
	// @Override
	public final void addChildren(final Iterable<? extends IDescription> originalChildren) {
		if (originalChildren == null) return;
		for (final IDescription c : originalChildren) { 
			addChild(c); 
		}
	}

	/**
	 * Adds a single child description to this description.
	 *
	 * @param child the child to add
	 * @return the added child description
	 */
	// @Override
	public IDescription addChild(final IDescription child) {
		if (child == null) return null;
		child.setEnclosingDescription(this);
		return child;
	}

	/**
	 * Sets the enclosing description and updates the model description accordingly.
	 *
	 * @param desc the new enclosing description
	 */
	@Override
	public void setEnclosingDescription(final IDescription desc) {
		enclosingDescription = desc;
		if (enclosingDescription == null) return;
		modelDescription = enclosingDescription.getModelDescription();
		if (modelDescription != null && modelDescription.isBuiltIn() && !this.isBuiltIn()) { 
			modelDescription = null; 
		}
	}

	/**
	 * Gets the underlying EMF element for a facet or the entire description.
	 *
	 * @param facet the facet to find, or null for the main element
	 * @param returnFacet whether to return the facet element itself or its expression
	 * @return the EMF element
	 */
	@Override
	public EObject getUnderlyingElement(final Object facet, final boolean returnFacet) {
		if (facet == null) return element;
		if (facet instanceof EObject e) return e;
		if (facet instanceof IExpressionDescription f) {
			final EObject result = f.getTarget();
			if (result != null) return result;
		}
		if (facet instanceof String) {
			if (getMeta() != null && !returnFacet && facet.equals(getMeta().getOmissible())) {
				final EObject o = GAML.getEcoreUtils().getExprOf(element);
				if (o != null) return o;
			}
			if (returnFacet) {
				final EObject facetObject = GAML.getEcoreUtils().getFacetsMapOf(element).get(facet);
				if (facetObject != null) return facetObject;
			}
			final IExpressionDescription f = getFacet((String) facet);
			if (f != null) {
				final EObject facetObject = GAML.getEcoreUtils().getFacetsMapOf(element).get(facet);
				if (facetObject != null) return facetObject;
				final EObject result = f.getTarget();
				if (result != null) return result;
			}
			// Last chance if the expression is a constant (no information on EObjects), see Issue #2760)
			final EObject facetExpr = GAML.getEcoreUtils().getExpressionAtKey(element, (String) facet);
			if (facetExpr != null) return facetExpr;
		}
		return null;
	}

	/**
	 * Creates a copy of this description.
	 * Default implementation just returns this; subclasses may override.
	 *
	 * @param into the target description to copy into
	 * @return the copied description
	 */
	@Override
	public IDescription copy(final IDescription into) {
		return this;
	}

	/**
	 * Visits all children recursively with the given visitor.
	 * Default implementation returns true; subclasses may override.
	 *
	 * @param visitor the visitor to apply to each child
	 * @return true if all visits succeeded, false if any visit returned false
	 */
	@Override
	public boolean visitOwnChildrenRecursively(final DescriptionVisitor<IDescription> visitor) {
		return true;
	}

	/**
	 * Gets the enclosing description that contains this description.
	 *
	 * @return the enclosing description
	 */
	@Override
	public IDescription getEnclosingDescription() { 
		return enclosingDescription; 
	}

	/**
	 * Checks if this description has an attribute with the given name.
	 * Default implementation returns false; subclasses may override.
	 *
	 * @param aName the attribute name to check
	 * @return true if the attribute exists, false otherwise
	 */
	@Override
	public boolean hasAttribute(final String aName) {
		return false;
	}

	/**
	 * Checks if this description manipulates a variable with the given name.
	 * Default implementation returns false; subclasses may override.
	 *
	 * @param aName the variable name to check
	 * @return true if the variable is manipulated, false otherwise
	 */
	@Override
	public boolean manipulatesVar(final String aName) {
		return false;
	}

	/**
	 * Checks if this description has an action with the given name.
	 * Default implementation returns false; subclasses may override.
	 *
	 * @param aName the action name to check
	 * @param superInvocation whether to check super types
	 * @return true if the action exists, false otherwise
	 */
	protected boolean hasAction(final String aName, final boolean superInvocation) {
		return false;
	}

	/**
	 * Gets the description that declares a variable with the given name.
	 * Searches up the enclosing description hierarchy.
	 *
	 * @param aName the variable name to find
	 * @return the description that declares the variable, or null if not found
	 */
	@Override
	public IVarDescriptionProvider getDescriptionDeclaringVar(final String aName) {
		IDescription enc = getEnclosingDescription();
		return enc == null ? null : enc.getDescriptionDeclaringVar(aName);
	}

	/**
	 * Gets the description that declares an action with the given name.
	 * Searches up the enclosing description hierarchy.
	 *
	 * @param aName the action name to find
	 * @param superInvocation whether to check super types
	 * @return the description that declares the action, or null if not found
	 */
	@Override
	public IDescription getDescriptionDeclaringAction(final String aName, final boolean superInvocation) {
		IDescription enc = getEnclosingDescription();
		return enc == null ? null : enc.getDescriptionDeclaringAction(aName, superInvocation);
	}

	/**
	 * Gets the expression for a variable with the given name.
	 * Default implementation returns null; subclasses may override.
	 *
	 * @param aName the variable name
	 * @param asField whether to treat it as a field access
	 * @return the variable expression, or null if not found
	 */
	@Override
	public IExpression getVarExpr(final String aName, final boolean asField) {
		return null;
	}

	/**
	 * Gets a type by name from the model context.
	 *
	 * @param s the type name
	 * @return the type, or a default type if not found
	 */
	@Override
	public IType<?> getTypeNamed(final String s) {
		final ModelDescription m = getModelDescription();
		if (m == null) return Types.get(s);
		return m.getTypeNamed(s);
	}

	/**
	 * Gets the GAML type of this symbol.
	 * Computes it if not already set.
	 *
	 * @return the GAML type
	 */
	@Override
	public IType<?> getGamlType() {
		if (type == null) { 
			setType(computeType()); 
		}
		return type;
	}

	/** Facets that can provide static type information. */
	static final String[] staticTypeProviders = { DATA, TYPE, SPECIES, AS, TARGET /* , ON */ };

	/** Facets that can provide dynamic type information. */
	static final String[] dynamicTypeProviders = { INIT, VALUE, UPDATE, FUNCTION, DEFAULT };

	/**
	 * Computes the type of this symbol. If the type is not defined, it will try by default to infer it from
	 * the facets, unless the flag NoTypeInference is set.
	 *
	 * @return the computed type
	 */
	protected IType<?> computeType() {
		return computeType(!isSet(Flag.NoTypeInference));
	}

	/**
	 * Computes the type of this symbol with control over type inference.
	 *
	 * @param doTypeInference whether to attempt type inference from facets
	 * @return the computed type
	 */
	protected IType<?> computeType(final boolean doTypeInference) {
		// Get type information from facets
		IType<?> tt = getTypeDenotedByFacet(staticTypeProviders);
		IType<?> kt = getTypeDenotedByFacet(INDEX, tt.getKeyType());
		IType<?> ct = getTypeDenotedByFacet(OF, tt.getContentType());
		return doTypeInference ? inferTypesOf(tt, kt, ct) : GamaType.from(tt, kt, ct);
	}

	/**
	 * Infers types from facets when they are not explicitly defined.
	 * This method attempts to determine the type, key type, and content type
	 * by examining expressions in the facets.
	 *
	 * @param tt the initial type
	 * @param kt the initial key type
	 * @param ct the initial content type
	 * @return the inferred type
	 */
	protected IType<?> inferTypesOf(IType<?> tt, IType<?> kt, IType<?> ct) {
		// If the initial type is NO_TYPE, try to find it in dynamic type providers
		if (tt == Types.NO_TYPE) { 
			tt = findInDynamicTypeProviders(tt); 
		}
		// If the type is not a container, return it as is
		if (!tt.isContainer()) return tt;
		// If the content type or key type is NO_TYPE, try to infer them
		if (ct == Types.NO_TYPE || kt == Types.NO_TYPE) {
			IExpressionDescription ed = getFacet(dynamicTypeProviders);
			if (ed != null) {
				IExpression expr = ed.compile(this);
				IType<?> exprType = expr == null ? Types.NO_TYPE : expr.getGamlType();
				// If the initial type is assignable from the expression type, use the expression type
				if (tt.isAssignableFrom(exprType)) {
					tt = exprType;
				} else {
					// Otherwise, infer the key type and content type from the expression type
					if (kt == Types.NO_TYPE) { 
						kt = exprType.getKeyType(); 
					}
					if (ct == Types.NO_TYPE) { 
						ct = exprType.getContentType(); 
					}
				}
			}
		}
		// Return the combined type
		return GamaType.from(tt, kt, ct);
	}

	/**
	 * Attempts to find a type from dynamic type provider facets.
	 *
	 * @param tt the default type to return if no type is found
	 * @return the found type, or the default if none is found
	 */
	private IType<?> findInDynamicTypeProviders(final IType<?> tt) {
		IExpressionDescription ed = getFacet(dynamicTypeProviders);
		if (ed != null) {
			IExpression expr = ed.compile(this);
			if (expr != null) return expr.getGamlType();
		}
		return tt;
	}

	/**
	 * Gets the species context for this description.
	 *
	 * @return the species context, or null if none exists
	 */
	@Override
	public SpeciesDescription getSpeciesContext() {
		IDescription desc = getEnclosingDescription();
		if (desc == null) return null;
		return desc.getSpeciesContext();
	}

	/**
	 * Gets a species description by name from the model.
	 *
	 * @param actualSpecies the species name
	 * @return the species description, or null if not found
	 */
	@Override
	public SpeciesDescription getSpeciesDescription(final String actualSpecies) {
		final ModelDescription model = getModelDescription();
		if (model == null) return null;
		return model.getSpeciesDescription(actualSpecies);
	}

	/**
	 * Gets an action description by name.
	 * Default implementation returns null; subclasses may override.
	 *
	 * @param aName the action name
	 * @return the action description, or null if not found
	 */
	@Override
	public ActionDescription getAction(final String aName) {
		return null;
	}

	/**
	 * Gets a title for this description, used in UI elements.
	 *
	 * @return a title string
	 */
	@Override
	public String getTitle() { 
		return "Statement " + getKeyword(); 
	}

	/**
	 * Gets the documentation for this symbol.
	 *
	 * @return the documentation object
	 */
	@Override
	public Doc getDocumentation() { 
		return getMeta().getDocumentation(); 
	}

	/**
	 * Gets the plugin that defined this symbol.
	 *
	 * @return the plugin ID
	 */
	@Override
	public String getDefiningPlugin() { 
		return getMeta().getDefiningPlugin(); 
	}

	/**
	 * Sets the defining plugin for this symbol.
	 * Default implementation does nothing; subclasses may override.
	 *
	 * @param plugin the plugin ID
	 */
	@Override
	public void setDefiningPlugin(final String plugin) {
		// Nothing to do here
	}

	/**
	 * Gets the validation context for this description.
	 *
	 * @return the validation context, or null if no model is available
	 */
	@Override
	public ValidationContext getValidationContext() {
		final ModelDescription model = getModelDescription();
		if (model == null) return null;
		return model.getValidationContext();
	}

	/**
	 * Checks if this description is for a built-in symbol.
	 *
	 * @return true if this is a built-in symbol, false otherwise
	 */
	@Override
	public boolean isBuiltIn() { 
		return state.contains(Flag.BuiltIn); 
	}

	/**
	 * Checks if this description is synthetic (generated programmatically).
	 *
	 * @return true if this is a synthetic description, false otherwise
	 */
	protected boolean isSynthetic() { 
		return state.contains(Flag.Synthetic); 
	}

	/**
	 * Gets the origin name of this symbol.
	 *
	 * @return the origin name
	 */
	@Override
	public String getOriginName() { 
		return originName; 
	}

	/**
	 * Sets the origin name if not already set.
	 *
	 * @param name the new origin name
	 */
	@Override
	public void setOriginName(final String name) {
		if (originName == null) { 
			originName = name; 
		}
	}

	/**
	 * Resets the origin name to null.
	 */
	@Override
	public void resetOriginName() {
		originName = null;
	}

	/**
	 * Validates this description, checking that it is correctly defined
	 * within its context and that its facets are valid.
	 *
	 * @return this description if validation passed, null if it failed
	 */
	@Override
	public IDescription validate() {
		if (isSet(Flag.Validated)) return this;
		set(Flag.Validated);

		if (isBuiltIn()) {
			// We simply make sure that the facets are correctly compiled
			validateFacets();
			return this;
		}
		final IDescription enclosing = getEnclosingDescription();
		if (enclosing != null) {
			String kw = getKeyword();
			String ekw = enclosing.getKeyword();
			// We first verify that the description is at the right place
			if (!proto.canBeDefinedIn(enclosing)) {
				error(kw + " cannot be defined in " + ekw, IGamlIssue.WRONG_CONTEXT);
				return null;
			}
			// If it is supposed to be unique, we verify this
			if (proto.isUniqueInContext()) {
				final boolean hasError = !enclosing.visitOwnChildren(child -> {
					if (child != SymbolDescription.this && child.getKeyword().equals(kw)) {
						final String error = kw + " is defined twice. Only one definition is allowed in " + ekw;
						child.error(error, IGamlIssue.DUPLICATE_KEYWORD, child.getUnderlyingElement(), kw);
						error(error, IGamlIssue.DUPLICATE_KEYWORD, getUnderlyingElement(), kw);
						return false;
					}
					return true;

				});
				if (hasError) return null;
			}
		}

		// We then validate its facets and children
		if (!validateFacets() || !validateChildren()) return null;
		if (proto.getDeprecated() != null) {
			warning("'" + getKeyword() + "' is deprecated. " + proto.getDeprecated(), IGamlIssue.DEPRECATED);
		}

		// If a custom validator has been defined, run it
		if (!proto.getValidator().validate(this, element)) return null;
		return this;
	}

	/**
	 * Validates the facets of this description, checking for missing required facets,
	 * deprecated facets, type compatibility, etc.
	 *
	 * @return true if all facets are valid, false otherwise
	 */
	private final boolean validateFacets() {
		// Special case for "do", which can accept (at parsing time) any facet
		final boolean isDo = isInvocation();
		final boolean isBuiltIn = isBuiltIn();
		List<String> mandatory = proto.getMandatoryFacets();
		if (mandatory != null) {
			for (String facet : mandatory) {
				if (!facets.containsKey(facet)) {
					error("Missing facet " + facet, IGamlIssue.MISSING_FACET, getUnderlyingElement(), facet, "nil");
					return false;
				}
			}
		}

		return visitFacets((facet, expr) -> {
			final FacetProto fp = proto.getFacet(facet);
			if (fp == null) return processUnknowFacet(isDo, facet);
			if (fp.getDeprecated() != null) {
				warning("Facet '" + facet + "' is deprecated: " + fp.getDeprecated(), IGamlIssue.DEPRECATED, facet);
			}
			if (fp.values != null) {
				if (!processMultiValuedFacet(facet, expr, fp)) return false;
			} else {
				// Some expressions might not be compiled
				IExpression exp = compileExpression(facet, expr, fp);
				if (exp != null && !isBuiltIn) {
					final IType<?> actualType = exp.getGamlType();
					// Special case for init. Temporary solution before we can pass ITypeProvider.OWNER_TYPE to the init
					// facet. Concerned types are point and date, which belong to "NumberVariable" and can accept nil,
					// while int and float cannot
					if (INIT.equals(fp.name)) {
						IType<?> requestedType = SymbolDescription.this.getGamlType();
						if ((Types.POINT == requestedType || Types.DATE == requestedType)
								&& actualType == Types.NO_TYPE)
							return true;
					}
					final IType<?> contentType = fp.contentType;
					final IType<?> keyType = fp.keyType;
					boolean compatible = verifyFacetTypesCompatibility(fp, exp, actualType, contentType, keyType);
					if (!compatible) {
						emitFacetTypesIncompatibilityWarning(facet, fp, actualType, contentType, keyType);
					}
				}
			}
			return true;
		});

	}

	/**
	 * Compiles an expression for a facet, handling various special cases.
	 *
	 * @param facet the facet name
	 * @param expr the expression description
	 * @param fp the facet prototype
	 * @return the compiled expression
	 */
	private IExpression compileExpression(final String facet, final IExpressionDescription expr, final FacetProto fp) {
		IExpression exp;
		if (fp.isNewTemp) {
			exp = createVarWithTypes(facet);
			// DEBUG.OUT("Type of IExpressionDescription is " + expr.getClass().getSimpleName());
			expr.setExpression(exp);
		} else if (!fp.isLabel()) {
			if (fp.isRemote && this instanceof StatementRemoteWithChildrenDescription srwc) {
				IDescription previousEnclosingDescription = srwc.pushRemoteContext();
				exp = expr.compile(SymbolDescription.this);
				srwc.popRemoteContext(previousEnclosingDescription);
			} else {
				exp = expr.compile(SymbolDescription.this);
			}
		} else {
			exp = expr.getExpression();
		}
		return exp;
	}

	/**
	 * Emits a warning when a facet's type is incompatible with its expected type.
	 *
	 * @param facet the facet name
	 * @param fp the facet prototype
	 * @param actualType the actual type of the expression
	 * @param contentType the expected content type
	 * @param keyType the expected key type
	 */
	private void emitFacetTypesIncompatibilityWarning(final String facet, final FacetProto fp,
			final IType<?> actualType, final IType<?> contentType, final IType<?> keyType) {
		final String[] strings = new String[fp.types.length];
		for (int i = 0; i < fp.types.length; i++) {
			IType<?> requestedType2 = fp.types[i];
			if (requestedType2.isContainer()) { 
				requestedType2 = GamaType.from(requestedType2, keyType, contentType); 
			}
			strings[i] = requestedType2.toString();
		}

		warning("Facet '" + facet + "' is expecting " + Arrays.toString(strings) + " instead of " + actualType,
				IGamlIssue.SHOULD_CAST, facet, fp.types[0].toString());
	}

	/**
	 * Verifies that a facet's expression type is compatible with its expected types.
	 *
	 * @param fp the facet prototype
	 * @param exp the compiled expression
	 * @param actualType the actual type of the expression
	 * @param contentType the expected content type
	 * @param keyType the expected key type
	 * @return true if the types are compatible, false otherwise
	 */
	private boolean verifyFacetTypesCompatibility(final FacetProto fp, final IExpression exp, final IType<?> actualType,
			final IType<?> contentType, final IType<?> keyType) {
		boolean compatible = false;
		for (final IType<?> definedType : fp.types) {
			if (definedType == Types.NO_TYPE) return true;
			boolean isNone = actualType == Types.NO_TYPE;

			if (definedType.isContainer()) {
				compatible = actualType.equals(definedType) && actualType.getKeyType().equals(keyType)
						&& actualType.getContentType().equals(contentType)
						|| !isNone && actualType.isTranslatableInto(definedType)
								&& actualType.getKeyType().isTranslatableInto(keyType)
								&& actualType.getContentType().isTranslatableInto(contentType);
			} else {
				compatible = actualType.equals(definedType) || !isNone && actualType.isTranslatableInto(definedType);
			}
			compatible |= Types.isEmptyContainerCase(definedType, exp);
			if (compatible) { 
				break; 
			}
		}

		return compatible;
	}

	/**
	 * Processes a multi-valued facet, checking that its value is among the accepted values.
	 *
	 * @param facet the facet name
	 * @param expr the expression description
	 * @param fp the facet prototype
	 * @return true if the value is valid, false otherwise
	 */
	private boolean processMultiValuedFacet(final String facet, final IExpressionDescription expr,
			final FacetProto fp) {
		final String val = expr.getExpression().literalValue();
		// We have a multi-valued facet
		if (!fp.values.contains(val)) {
			error("Facet '" + facet + "' is expecting a value among " + fp.values + " instead of " + val, facet);
			return false;
		}
		return true;
	}

	/**
	 * Processes an unknown facet, handling special cases and reporting errors.
	 *
	 * @param isDo whether this is a "do" statement, which allows arbitrary facets
	 * @param facet the facet name
	 * @return true if the facet can be accepted, false otherwise
	 */
	private boolean processUnknowFacet(final boolean isDo, final String facet) {
		if (facet.contains(IGamlIssue.DOUBLED_CODE)) {
			final String correct = facet.replace(IGamlIssue.DOUBLED_CODE, "");
			final String error = "Facet " + correct + " is declared twice. Please correct.";
			error(error, IGamlIssue.DUPLICATE_DEFINITION, facet, "1");
			error(error, IGamlIssue.DUPLICATE_DEFINITION, correct, "2");
			return false;
		}
		if (!isDo) {
			error("Unknown facet " + facet, IGamlIssue.UNKNOWN_FACET, facet);
			return false;
		}
		return true;
	}

	/**
	 * Creates a variable expression with types for a facet.
	 * Default implementation returns null; subclasses may override.
	 *
	 * @param tag the facet tag
	 * @return the created expression, or null
	 */
	protected IExpression createVarWithTypes(final String tag) {
		return null;
	}

	/**
	 * Validates the children of this description.
	 *
	 * @return true if all children are valid, false otherwise
	 */
	protected boolean validateChildren() {
		return visitOwnChildren(VALIDATING_VISITOR);
	}

	/**
	 * Compiles this description into a runtime symbol.
	 * This method validates the description first, then creates and initializes the symbol.
	 *
	 * @return the compiled symbol, or null if compilation failed
	 */
	@Override
	public final ISymbol compile() {
		validate();
		final ISymbol cs = proto.create(this);
		if (cs == null) return null;
		if (proto.hasArgs()) {
			((IStatement.WithArgs) cs).setFormalArgs(((StatementDescription) this).createCompiledArgs());
		}
		if (proto.hasSequence() && !proto.isPrimitive()) { 
			cs.setChildren(compileChildren()); 
		}
		return cs;
	}

	/**
	 * Compiles the children of this description into runtime symbols.
	 *
	 * @return an iterable of compiled symbols
	 */
	protected Iterable<? extends ISymbol> compileChildren() {
		final List<ISymbol> lce = new ArrayList<>();
		visitChildren(desc -> {
			final ISymbol s = desc.compile();
			if (s != null) { 
				lce.add(s); 
			}
			return true;
		});
		return lce;
	}

	/**
	 * Gets all child descriptions with the specified keyword.
	 *
	 * @param aKeyword the keyword to filter by
	 * @return an iterable of matching descriptions
	 */
	@Override
	public Iterable<IDescription> getChildrenWithKeyword(final String aKeyword) {
		return StreamSupport.stream(getOwnChildren().spliterator(), false)
				.filter(d -> d.getKeyword().equals(aKeyword))
				.toList();
	}

	/**
	 * Gets the first child description with the specified keyword.
	 *
	 * @param aKeyword the keyword to search for
	 * @return the first matching description, or null if none is found
	 */
	@Override
	public IDescription getChildWithKeyword(final String aKeyword) {
		IDescription[] result = new IDescription[1];
		visitChildren(desc -> {
			if (desc.getKeyword().equals(aKeyword)) {
				result[0] = desc;
				return false;
			}
			return true;
		});
		return result[0];
	}

	/**
	 * Gets all facets for this description.
	 * Note: When possible, prefer using visitFacets() for better performance.
	 *
	 * @return the facets, or NULL if none exist
	 */
	@Override
	public Facets getFacets() { 
		return facets == null ? Facets.NULL : facets; 
	}

	/**
	 * Attaches an alternate variable description provider.
	 * Default implementation does nothing; subclasses may override.
	 *
	 * @param vp the variable description provider
	 */
	@Override
	public void attachAlternateVarDescriptionProvider(final IVarDescriptionProvider vp) {}

	/**
	 * Finds a child description in a container that matches the keyword and name of the given description.
	 *
	 * @param container the container to search in
	 * @param desc the description to match
	 * @return the matching description, or null if none is found
	 */
	public static IDescription getSimilarChild(final IDescription container, final IDescription desc) {
		final IDescription[] found = new IDescription[1];
		container.visitChildren(d -> {
			if (d != null && d.getKeyword().equals(desc.getKeyword()) && d.getName().equals(desc.getName())) {
				found[0] = d;
				return false;
			}
			return true;
		});
		return found[0];
	}

	/**
	 * Replaces the children of this description with the given descriptions.
	 * Default implementation does nothing; subclasses may override.
	 *
	 * @param array the new children
	 */
	@Override
	public void replaceChildrenWith(final Iterable<IDescription> array) {}

	/**
	 * Sets the type of this symbol.
	 *
	 * @param type the new type
	 */
	private void setType(final IType<?> type) { 
		this.type = type; 
	}

	/**
	 * Checks if this description represents an action invocation (e.g., a "do" statement).
	 * Default implementation returns false; subclasses may override.
	 *
	 * @return true if this is an invocation, false otherwise
	 */
	@Override
	public boolean isInvocation() { 
		return false; 
	}

	/**
	 * Checks if this description represents a "create" statement.
	 *
	 * @return true if this is a create statement, false otherwise
	 */
	public boolean isCreate() { 
		return isSet(Flag.IsCreate); 
	}
}
