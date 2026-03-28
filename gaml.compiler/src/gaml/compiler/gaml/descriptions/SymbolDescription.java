/*******************************************************************************************************
 *
 * SymbolDescription.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.descriptions;

import static gama.api.compilation.IInternalFacets.NO_TYPE_INFERENCE;
import static gama.api.compilation.IInternalFacets.ORIGIN;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.constants.IKeyword;
import gama.annotations.support.ISymbolKind;
import gama.api.additions.registries.ArtefactRegistry;
import gama.api.compilation.IInternalFacets;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.descriptions.IActionDescription;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.descriptions.IVarDescriptionProvider;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.compilation.serialization.ISymbolSerializer;
import gama.api.compilation.validation.IDocumentationContext;
import gama.api.compilation.validation.IValidationContext;
import gama.api.constants.IGamlIssue;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.utils.GamlProperties;
import gama.dev.DEBUG;
import gaml.compiler.gaml.EGaml;
import gaml.compiler.gaml.prototypes.SymbolArtefact;

/**
 * Abstract base class for all GAML symbol descriptions in the compilation pipeline.
 *
 * <p>
 * SymbolDescription serves as the core intermediary representation between parsed GAML source code and executable
 * runtime objects. It bridges the gap between the Abstract Syntax Tree (AST) produced by the parser and the runtime
 * {@link ISymbol} instances that execute during simulation.
 * </p>
 *
 * <p>
 * <strong>Architectural Role:</strong>
 * </p>
 *
 * <pre>
 * Source Code (GAML)
 *   → Parser → ISyntacticElement (AST)
 *   → DescriptionFactory → SymbolDescription (Semantic Model)  ← THIS CLASS
 *   → Compiler → ISymbol (Runtime Objects)
 *   → Execution
 * </pre>
 *
 * <p>
 * <strong>Key Responsibilities:</strong>
 * </p>
 * <ul>
 * <li><strong>Semantic Validation:</strong> Validates syntax correctness, type compatibility, and semantic rules</li>
 * <li><strong>Type Management:</strong> Resolves and manages GAML types for expressions and symbols</li>
 * <li><strong>Facet Handling:</strong> Manages attributes (facets) like name, type, value, etc.</li>
 * <li><strong>Hierarchy Management:</strong> Maintains parent-child relationships in the model structure</li>
 * <li><strong>Symbol Compilation:</strong> Compiles descriptions into executable runtime symbols</li>
 * <li><strong>Error Reporting:</strong> Provides detailed error messages with source location context</li>
 * </ul>
 *
 * <p>
 * <strong>Description Hierarchy:</strong>
 * </p>
 *
 * <pre>
 * SymbolDescription (abstract base)
 *   ├── StatementDescription → Simple statements (create, write, etc.)
 *   ├── StatementWithChildrenDescription → Compound statements (if, loop, etc.)
 *   │   ├── ActionDescription → User-defined actions
 *   │   └── DoDescription → Action invocations
 *   ├── VariableDescription → Variable declarations
 *   ├── TypeDescription → Type definitions
 *   ├── SpeciesDescription → Agent type definitions
 *   │   ├── ExperimentDescription → Simulation experiments
 *   │   ├── ModelDescription → Complete models
 *   │   └── PlatformSpeciesDescription → Built-in platform species
 *   ├── SkillDescription → Reusable behavior modules
 *   └── PrimitiveDescription → Built-in operators and functions
 * </pre>
 *
 * <p>
 * <strong>State Management with Flags:</strong>
 * </p>
 * <p>
 * Uses an efficient {@link EnumSet} to track boolean states. Common flags include:
 * </p>
 * <ul>
 * <li>{@link Flag#BuiltIn} - Symbol is part of the GAML standard library</li>
 * <li>{@link Flag#Validated} - Symbol has passed validation</li>
 * <li>{@link Flag#Synthetic} - Symbol was generated programmatically</li>
 * <li>{@link Flag#Abstract} - Symbol is abstract and cannot be instantiated</li>
 * <li>{@link Flag#NoTypeInference} - Disable automatic type inference</li>
 * </ul>
 *
 * <p>
 * <strong>Facets (Attributes):</strong>
 * </p>
 * <p>
 * Facets are key-value pairs representing symbol attributes. Examples:
 * </p>
 *
 * <pre>{@code
 * species Bird {              // keyword="species", name facet="Bird"
 *   float speed <- 1.0;       // keyword="float", name="speed", init="1.0"
 *   reflex move when: true {  // keyword="reflex", name="move", when="true"
 *     // ...
 *   }
 * }
 * }</pre>
 *
 * <p>
 * <strong>Memory Optimization:</strong>
 * </p>
 * <ul>
 * <li>Facets are lazily initialized and nullified when empty</li>
 * <li>EnumSet provides compact flag storage (single long for ≤64 flags)</li>
 * <li>Weak references used for model description to prevent circular retention</li>
 * <li>Artefact information shared across instances of same symbol type</li>
 * </ul>
 *
 * <p>
 * <strong>Thread Safety:</strong>
 * </p>
 * <p>
 * NOT thread-safe. Descriptions are created and validated sequentially during compilation. The compilation process is
 * single-threaded by design for error reporting consistency.
 * </p>
 *
 * <p>
 * <strong>Validation Process:</strong>
 * </p>
 * <ol>
 * <li>Parse facets and validate required/optional attributes</li>
 * <li>Resolve types for expressions and variables</li>
 * <li>Check semantic rules (e.g., variable name conflicts)</li>
 * <li>Validate child descriptions recursively</li>
 * <li>Mark as validated if successful</li>
 * </ol>
 *
 * <p>
 * <strong>Performance Considerations:</strong>
 * </p>
 * <ul>
 * <li>Description creation is O(n) where n is the number of symbols in the model</li>
 * <li>Validation is also O(n) but with higher constant factors due to type resolution</li>
 * <li>Typical large models have 1000-10000 descriptions</li>
 * <li>Memory usage: ~200-500 bytes per description depending on facets</li>
 * </ul>
 *
 * <p>
 * <strong>Design Patterns:</strong>
 * </p>
 * <ul>
 * <li><strong>Factory Method:</strong> {@link #compile()} creates runtime symbols</li>
 * <li><strong>Template Method:</strong> {@link #validate()} defines validation algorithm</li>
 * <li><strong>Visitor:</strong> {@link #visitFacets} and {@link #visitChildren} support traversal</li>
 * <li><strong>Artefact:</strong> {@link SymbolArtefact} stores shared metadata</li>
 * </ul>
 *
 * @author Alexis Drogoul
 * @since 16 Mar 2010
 * @see IDescription
 * @see ISymbol
 * @see DescriptionFactory
 * @see SymbolArtefact
 */
public abstract class SymbolDescription extends DescriptionStateManager {

	static {
		DEBUG.OFF();
	}

	/**
	 * The facets (attributes) associated with this symbol.
	 *
	 * <p>
	 * Facets are key-value pairs where keys are facet names (e.g., "name", "type", "value") and values are
	 * {@link IExpressionDescription} objects that can be compiled to expressions.
	 * </p>
	 *
	 * <p>
	 * <strong>Lazy Initialization:</strong> Null until first facet is set, saving memory for symbols without facets.
	 * Nullified again if all facets are removed.
	 * </p>
	 *
	 * <p>
	 * <strong>Example:</strong> For {@code int age <- 10;}, facets would contain: {@code {name: "age", init: "10"}}
	 * </p>
	 */
	private Facets facets;

	/**
	 * The underlying EMF EObject from the parser's Abstract Syntax Tree.
	 *
	 * <p>
	 * Provides access to source code location for error reporting and debugging. Null for built-in symbols defined
	 * programmatically rather than parsed from source.
	 * </p>
	 *
	 * <p>
	 * <strong>Usage:</strong> Used by {@link #error(String)} and {@link #info(String)} to generate error messages with
	 * file/line information.
	 * </p>
	 */
	protected final EObject element;

	/**
	 * The description that encloses/contains this description.
	 *
	 * <p>
	 * Forms the parent-child hierarchy of the model structure. For example: A variable's enclosing description is its
	 * species, a species' enclosing description is the model or parent species.
	 * </p>
	 *
	 * <p>
	 * <strong>Null for:</strong> Top-level model descriptions which have no parent.
	 * </p>
	 */
	private IDescription enclosingDescription;

	/**
	 * The model description this symbol belongs to.
	 *
	 * <p>
	 * Provides access to global model context including other species, global variables, and model-level configuration.
	 * All descriptions in a model share the same model description.
	 * </p>
	 *
	 * <p>
	 * <strong>Phase 2 Optimization:</strong> Uses {@link WeakReference} to prevent circular retention. Since model
	 * descriptions can hold references to all their children, and children hold references back to the model, this
	 * creates a circular reference that can prevent garbage collection. WeakReference allows the model to be collected
	 * when no longer in use.
	 * </p>
	 *
	 * <p>
	 * <strong>Memory Impact:</strong> Reduces retained heap in scenarios where models are frequently loaded/unloaded
	 * (e.g., batch processing, testing).
	 * </p>
	 */
	private WeakReference<IModelDescription> modelDescriptionRef;

	/**
	 * The origin name of the symbol that created this description.
	 *
	 * <p>
	 * Used for tracing inheritance and imports. For example, if a species inherits an action from a parent species,
	 * originName would be the parent species name.
	 * </p>
	 *
	 * <p>
	 * <strong>Set from:</strong> Either the {@link IKeyword#ORIGIN} facet or the enclosing description's name during
	 * construction.
	 * </p>
	 */
	protected String originName;

	/**
	 * The name of this symbol.
	 *
	 * <p>
	 * Typically extracted from the "name" facet during construction. Used as the identifier for the symbol in its
	 * containing scope.
	 * </p>
	 *
	 * <p>
	 * <strong>Caching:</strong> Cached here for performance rather than repeatedly accessing facets.
	 * </p>
	 */
	protected String name;

	/**
	 * The GAML keyword that defines this symbol's type.
	 *
	 * <p>
	 * Examples: "species", "action", "reflex", "int", "float", "create", "if", "loop"
	 * </p>
	 *
	 * <p>
	 * <strong>Immutable:</strong> Set at construction and never changes. Used to look up the symbol's prototype
	 * information.
	 * </p>
	 */
	protected final String keyword;

	/**
	 * The GAML type of this symbol.
	 *
	 * <p>
	 * For variables and expressions, this is their data type (int, float, agent, etc.). For statements, this is
	 * typically the return type if applicable.
	 * </p>
	 *
	 * <p>
	 * <strong>Type Inference:</strong> May be inferred from facets like "init", "value", "function", unless
	 * {@link Flag#NoTypeInference} is set.
	 * </p>
	 */
	private IType<?> type;

	/**
	 * The artefact (meta-information) for this symbol type.
	 *
	 * <p>
	 * Shared across all instances of the same symbol type. Contains information like: required/optional facets, allowed
	 * contexts, serializer, validator, etc.
	 * </p>
	 *
	 * <p>
	 * <strong>Flyweight Pattern:</strong> Shared immutable metadata reduces memory per instance.
	 * </p>
	 *
	 * <p>
	 * <strong>Lookup:</strong> Retrieved from {@link ArtefactRegistry} based on keyword and species context.
	 * </p>
	 */
	final SymbolArtefact artefact;

	/**
	 * Creates a new symbol description.
	 *
	 * @param keyword
	 *            the GAML keyword for this symbol (e.g., "species", "action", "var")
	 * @param superDesc
	 *            the parent description containing this symbol
	 * @param source
	 *            the EMF AST node representing this symbol
	 * @param facets
	 *            the facets defined for this symbol
	 */
	public SymbolDescription(final String keyword, final IDescription superDesc, final EObject source,
			final Facets facets) {
		this.keyword = keyword;
		this.facets = facets;

		element = source;
		setIf(Flag.IsBuiltIn, element == null);
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
		artefact = (SymbolArtefact) ArtefactRegistry.getArtefact(getKeyword(), getTypeContext());

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
	 * @param others
	 *            a set of facet names
	 * @return true if the description has facets not included in the set, false otherwise
	 */
	protected boolean hasFacetsNotIn(final Set<String> others) {
		if (!hasFacets()) return false;
		return !visitFacets((facetName, exp) -> others.contains(facetName));
	}

	/**
	 * Gets the serializer for this symbol description. Creates one if it doesn't already exist.
	 *
	 * @return the symbol serializer
	 */
	@Override
	public final ISymbolSerializer getSerializer() {
		final SymbolArtefact p = getArtefact();
		ISymbolSerializer d = p.getSerializer();
		if (d == null) {
			d = createSerializer();
			p.setSerializer(d);
		}
		return d;
	}

	/**
	 * Gets the expression description for the specified facet.
	 *
	 * @param string
	 *            the facet name
	 * @return the expression description, or null if the facet doesn't exist
	 */
	@Override
	public IExpressionDescription getFacet(final String string) {
		return !hasFacets() ? null : facets.get(string);
	}

	/**
	 * Gets the compiled expression for the first matching facet name.
	 *
	 * @param strings
	 *            one or more facet names to check
	 * @return the first found expression, or null if none are found
	 */
	@Override
	public IExpression getFacetExpr(final String... strings) {
		return !hasFacets() ? null : facets.getExpr(strings);
	}

	/**
	 * Gets the expression description for the first matching facet name.
	 *
	 * @param strings
	 *            one or more facet names to check
	 * @return the first found expression description, or null if none are found
	 */
	@Override
	public IExpressionDescription getFacet(final String... strings) {
		return !hasFacets() ? null : facets.getDescr(strings);
	}

	/**
	 * Checks if a specific facet exists in this description.
	 *
	 * @param string
	 *            the facet name to check
	 * @return true if the facet exists, false otherwise
	 */
	@Override
	public boolean hasFacet(final String string) {
		return hasFacets() && facets.containsKey(string);
	}

	/**
	 * Gets the literal value of a facet.
	 *
	 * @param string
	 *            the facet name
	 * @return the literal value as a string, or null if the facet doesn't exist
	 */
	@Override
	public String getLitteral(final String string) {
		return !hasFacets() ? null : facets.getLabel(string);
	}

	/**
	 * Sets a facet with the given expression description.
	 *
	 * @param name
	 *            the facet name
	 * @param desc
	 *            the expression description
	 */
	@Override
	public void setFacetExprDescription(final String name, final IExpressionDescription desc) {
		if (!hasFacets()) { facets = new Facets(); }
		facets.put(name, desc);
	}

	/**
	 * Sets a facet with a pre-compiled expression.
	 *
	 * @param string
	 *            the facet name
	 * @param exp
	 *            the compiled expression
	 */
	@Override
	public void setFacet(final String string, final IExpression exp) {
		if (!hasFacets()) { facets = new Facets(); }
		facets.putExpression(string, exp);
	}

	/**
	 * Removes the specified facets from this description.
	 *
	 * @param strings
	 *            the facet names to remove
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
	 * @param names
	 *            the set of facet names to visit
	 * @param visitor
	 *            the visitor to apply
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
	 * @param visitor
	 *            the visitor to apply
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
	 * @param s
	 *            one or more facet names to check
	 * @return the type denoted by the first matching facet, or NO_TYPE if none match
	 */
	public IType<?> getTypeDenotedByFacet(final String... s) {
		if (!hasFacets()) return Types.NO_TYPE;
		return getTypeDenotedByFacet(facets.getFirstExistingAmong(s), Types.NO_TYPE);
	}

	/**
	 * Returns the first facet name found among the specified names.
	 *
	 * @param strings
	 *            the facet names to check
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
	 * @param s
	 *            the facet name
	 * @param defaultType
	 *            the default type to return if the facet doesn't exist
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
	public Facets getFacetsCopy() { return !hasFacets() ? null : facets.cleanCopy(); }

	/**
	 * Creates a serializer for this symbol description.
	 *
	 * @return a new serializer
	 */
	protected ISymbolSerializer createSerializer() {
		return SYMBOL_SERIALIZER;
	}

	/**
	 * Serializes this description to GAML code.
	 *
	 * @param includingBuiltIn
	 *            whether to include built-in elements in the serialization
	 * @return the GAML code representation of this symbol
	 */
	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return getSerializer().serialize(this, includingBuiltIn);
	}

	/**
	 * Collects metadata information about this symbol.
	 *
	 * @param meta
	 *            the properties object to populate with metadata
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
	public ISymbolKind getKind() { return getArtefact().getKind(); }

	/**
	 * Compiles all facets that can provide type information. This ensures type provider facets are compiled before they
	 * are needed.
	 */
	protected void compileTypeProviderFacets() {
		visitFacets((facetName, exp) -> {
			if (typeProviderFacets.contains(facetName)) { exp.compile(SymbolDescription.this); }
			return true;
		});
	}

	/**
	 * Compiles specific facets that may provide type information.
	 *
	 * @param names
	 *            the names of the facets to compile
	 */
	protected void compileTypeProviderFacets(final String... names) {
		for (final String s : names) {
			final IExpressionDescription exp = getFacet(s);
			if (exp != null) { exp.compile(this); }
		}
	}

	/**
	 * Gets the metadata prototype for this symbol.
	 *
	 * @return the symbol prototype
	 */
	@Override
	public final SymbolArtefact getArtefact() { return artefact; }

	/**
	 * Gets the keyword that defines this symbol.
	 *
	 * @return the keyword string
	 */
	@Override
	public String getKeyword() { return keyword; }

	/**
	 * Gets the name of this symbol. If the name is not set, attempts to get it from the NAME facet. The result is
	 * cached to avoid repeated lookups.
	 *
	 * @return the symbol name
	 */
	@Override
	public String getName() {
		if (name == null) { name = getLitteral(IKeyword.NAME); }
		return name;
	}

	/**
	 * Sets the name of this symbol and updates the NAME facet if available.
	 *
	 * @param name
	 *            the new name for the symbol
	 */
	@Override
	public void setName(final String name) {
		this.name = name;
		if (getArtefact().getPossibleFacets().containsKey(IKeyword.NAME)) {
			setFacetExprDescription(IKeyword.NAME, GAML.getExpressionDescriptionFactory().createLabel(name));
		}
	}

	/**
	 * Cleans up resources used by this description. Recursively disposes of children first.
	 */
	@Override
	public void dispose() {
		// DEBUG.LOG("Disposing " + getKeyword() + " " + getName());
		if (isBuiltIn()) return;
		visitOwnChildren(DISPOSING_VISITOR);
		if (hasFacets()) { facets.dispose(); }
		facets = null;
		enclosingDescription = null;
		modelDescriptionRef = null; // Clear WeakReference
		setType(null);
	}

	/**
	 * Gets the model description this symbol belongs to.
	 *
	 * <p>
	 * <strong>Phase 2 Optimization:</strong> Unwraps the {@link WeakReference} to get the actual model description.
	 * Returns null if the model has been garbage collected.
	 * </p>
	 *
	 * @return the model description, or null if collected or not set
	 */
	@Override
	public IModelDescription getModelDescription() {
		return modelDescriptionRef == null ? null : modelDescriptionRef.get();
	}

	/**
	 * Adds multiple child descriptions to this description.
	 *
	 * @param originalChildren
	 *            the children to add
	 */
	// @Override
	public final void addChildren(final Iterable<? extends IDescription> originalChildren) {
		if (originalChildren == null) return;
		for (final IDescription c : originalChildren) {
			if (c != null) {
				c.setEnclosingDescription(this);
				addChild(c);
			}
		}
	}

	/**
	 * Sets the enclosing description and updates the model description accordingly.
	 *
	 * <p>
	 * <strong>Phase 2 Optimization:</strong> Wraps the model description in a {@link WeakReference} to prevent circular
	 * retention issues.
	 * </p>
	 *
	 * @param desc
	 *            the new enclosing description
	 */
	@Override
	public void setEnclosingDescription(final IDescription desc) {
		enclosingDescription = desc;
		if (enclosingDescription == null) return;
		IModelDescription md = enclosingDescription.getModelDescription();
		if (md != null && md.isBuiltIn() && !this.isBuiltIn()) { md = null; }
		modelDescriptionRef = md == null ? null : new WeakReference<>(md);
	}

	/**
	 * Gets the underlying EMF element for a facet or the entire description.
	 *
	 * @param facet
	 *            the facet to find, or null for the main element
	 * @param returnFacet
	 *            whether to return the facet element itself or its expression
	 * @return the EMF element
	 */
	@Override
	public EObject getUnderlyingElement(final Object facet, final boolean returnFacet) {
		switch (facet) {
			case null -> {
				return element;
			}
			case EObject e -> {
				return e;
			}
			case IExpressionDescription f -> {
				final EObject result = f.getTarget();
				if (result != null) return result;
			}
			default -> {
			}
		}
		if (facet instanceof String) {
			if (getArtefact() != null && !returnFacet && facet.equals(getArtefact().getOmissible())) {
				final EObject o = EGaml.getInstance().getExprOf(element);
				if (o != null) return o;
			}
			if (returnFacet) {
				final EObject facetObject = EGaml.getInstance().getFacetsMapOf(element).get(facet);
				if (facetObject != null) return facetObject;
			}
			final IExpressionDescription f = getFacet((String) facet);
			if (f != null) {
				final EObject facetObject = EGaml.getInstance().getFacetsMapOf(element).get(facet);
				if (facetObject != null) return facetObject;
				final EObject result = f.getTarget();
				if (result != null) return result;
			}
			// Last chance if the expression is a constant (no information on EObjects), see Issue #2760)
			final EObject facetExpr = EGaml.getInstance().getExpressionAtKey(element, (String) facet);
			if (facetExpr != null) return facetExpr;
		}
		return null;
	}

	/**
	 * Creates a copy of this description. Default implementation just returns this; subclasses may override.
	 *
	 * @param into
	 *            the target description to copy into
	 * @return the copied description
	 */
	@Override
	public IDescription copy(final IDescription into) {
		return this;
	}

	/**
	 * Visits all children recursively with the given visitor. Default implementation returns true; subclasses may
	 * override.
	 *
	 * @param visitor
	 *            the visitor to apply to each child
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
	public IDescription getEnclosingDescription() { return enclosingDescription; }

	/**
	 * Checks if this description has an attribute with the given name. Default implementation returns false; subclasses
	 * may override.
	 *
	 * @param aName
	 *            the attribute name to check
	 * @return true if the attribute exists, false otherwise
	 */
	@Override
	public boolean hasAttribute(final String aName) {
		return false;
	}

	/**
	 * Checks if this description manipulates a variable with the given name. Default implementation returns false;
	 * subclasses may override.
	 *
	 * @param aName
	 *            the variable name to check
	 * @return true if the variable is manipulated, false otherwise
	 */
	@Override
	public boolean manipulatesVar(final String aName) {
		return false;
	}

	/**
	 * Checks if this description has an action with the given name. Default implementation returns false; subclasses
	 * may override.
	 *
	 * @param aName
	 *            the action name to check
	 * @param superInvocation
	 *            whether to check super types
	 * @return true if the action exists, false otherwise
	 */
	protected boolean hasAction(final String aName, final boolean superInvocation) {
		return false;
	}

	/**
	 * Gets the description that declares a variable with the given name. Searches up the enclosing description
	 * hierarchy.
	 *
	 * @param aName
	 *            the variable name to find
	 * @return the description that declares the variable, or null if not found
	 */
	@Override
	public IVarDescriptionProvider getDescriptionDeclaringVar(final String aName) {
		IDescription enc = getEnclosingDescription();
		return enc == null ? null : enc.getDescriptionDeclaringVar(aName);
	}

	/**
	 * Gets the description that declares an action with the given name. Searches up the enclosing description
	 * hierarchy.
	 *
	 * @param aName
	 *            the action name to find
	 * @param superInvocation
	 *            whether to check super types
	 * @return the description that declares the action, or null if not found
	 */
	@Override
	public ITypeDescription getDescriptionDeclaringAction(final String aName, final boolean superInvocation) {
		IDescription enc = getEnclosingDescription();
		return enc == null ? null : enc.getDescriptionDeclaringAction(aName, superInvocation);
	}

	/**
	 * Gets the expression for a variable with the given name. Default implementation returns null; subclasses may
	 * override.
	 *
	 * @param aName
	 *            the variable name
	 * @param asField
	 *            whether to treat it as a field access
	 * @return the variable expression, or null if not found
	 */
	@Override
	public IExpression getVarExpr(final String aName, final boolean asField) {
		return null;
	}

	/**
	 * Gets a type by name from the model context.
	 *
	 * @param s
	 *            the type name
	 * @return the type, or a default type if not found
	 */
	@Override
	public IType<?> getTypeNamed(final String s) {
		final IModelDescription m = getModelDescription();
		if (m == null) return Types.get(s);
		return m.getTypeNamed(s);
	}

	/**
	 * Gets the GAML type of this symbol. Computes it if not already set.
	 *
	 * <p>
	 * <strong>Optimization:</strong> Type is computed once on first access and cached in the {@code type} field.
	 * Subsequent calls return the cached value immediately.
	 * </p>
	 *
	 * <p>
	 * Type computation can be expensive as it may involve:
	 * <ul>
	 * <li>Facet compilation and expression evaluation</li>
	 * <li>Type inference from multiple facets</li>
	 * <li>Generic type parameter resolution</li>
	 * </ul>
	 * Caching ensures this expensive operation happens only once per description.
	 * </p>
	 *
	 * @return the GAML type
	 */
	@Override
	public IType<?> getGamlType() {
		if (type == null) { setType(computeType()); }
		return type;
	}

	/** Facets that can provide static type information. */
	static final String[] staticTypeProviders =
			{ IKeyword.DATA, IKeyword.TYPE, IKeyword.SPECIES, IKeyword.AS, IKeyword.TARGET /* , ON */ };

	/** Facets that can provide dynamic type information. */
	static final String[] dynamicTypeProviders =
			{ IKeyword.INIT, IKeyword.VALUE, IKeyword.UPDATE, IKeyword.FUNCTION, IKeyword.DEFAULT };

	/**
	 * Computes the type of this symbol. If the type is not defined, it will try by default to infer it from the facets,
	 * unless the flag NoTypeInference is set.
	 *
	 * @return the computed type
	 */
	protected IType<?> computeType() {
		return computeType(!isSet(Flag.NoTypeInference));
	}

	/**
	 * Computes the type of this symbol with control over type inference.
	 *
	 * @param doTypeInference
	 *            whether to attempt type inference from facets
	 * @return the computed type
	 */
	protected IType<?> computeType(final boolean doTypeInference) {
		// Get type information from facets
		IType<?> tt = getTypeDenotedByFacet(staticTypeProviders);
		IType<?> kt = getTypeDenotedByFacet(IKeyword.INDEX, tt.getKeyType());
		IType<?> ct = getTypeDenotedByFacet(IKeyword.OF, tt.getContentType());
		return doTypeInference ? inferTypesOf(tt, kt, ct) : GamaType.from(tt, kt, ct);
	}

	/**
	 * Infers types from facets when they are not explicitly defined. This method attempts to determine the type, key
	 * type, and content type by examining expressions in the facets.
	 *
	 * @param tt
	 *            the initial type
	 * @param kt
	 *            the initial key type
	 * @param ct
	 *            the initial content type
	 * @return the inferred type
	 */
	protected IType<?> inferTypesOf(IType<?> tt, IType<?> kt, IType<?> ct) {
		// If the initial type is NO_TYPE, try to find it in dynamic type providers
		if (tt == Types.NO_TYPE) { tt = findInDynamicTypeProviders(tt); }
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
					if (kt == Types.NO_TYPE) { kt = exprType.getKeyType(); }
					if (ct == Types.NO_TYPE) { ct = exprType.getContentType(); }
				}
			}
		}
		// Return the combined type
		return GamaType.from(tt, kt, ct);
	}

	/**
	 * Attempts to find a type from dynamic type provider facets.
	 *
	 * @param tt
	 *            the default type to return if no type is found
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
	public ITypeDescription getTypeContext() {
		IDescription desc = getEnclosingDescription();
		if (desc == null) return null;
		return desc.getTypeContext();
	}

	/**
	 * Gets a species description by name from the model.
	 *
	 * @param actualSpecies
	 *            the species name
	 * @return the species description, or null if not found
	 */
	@Override
	public ISpeciesDescription getSpeciesDescription(final String actualSpecies) {
		final IModelDescription model = getModelDescription();
		if (model == null) return null;
		return model.getSpeciesDescription(actualSpecies);
	}

	/**
	 * Gets an action description by name. Default implementation returns null; subclasses may override.
	 *
	 * @param aName
	 *            the action name
	 * @return the action description, or null if not found
	 */
	@Override
	public IActionDescription getAction(final String aName) {
		return null;
	}

	/**
	 * Gets a title for this description, used in UI elements.
	 *
	 * @return a title string
	 */
	@Override
	public String getTitle() { return "Statement " + getKeyword(); }

	/**
	 * Gets the documentation for this symbol.
	 *
	 * @return the documentation object
	 */
	@Override
	public IGamlDocumentation getDocumentation() { return getArtefact().getDocumentation(); }

	/**
	 * Gets the plugin that defined this symbol.
	 *
	 * @return the plugin ID
	 */
	@Override
	public String getDefiningPlugin() { return getArtefact().getDefiningPlugin(); }

	/**
	 * Sets the defining plugin for this symbol. Default implementation does nothing; subclasses may override.
	 *
	 * @param plugin
	 *            the plugin ID
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
	public IValidationContext getValidationContext() {
		final IModelDescription model = getModelDescription();
		if (model == null) return null;
		return model.getValidationContext();
	}

	/**
	 * Gets the documentation context.
	 *
	 * @return the documentation context
	 */
	@Override
	public IDocumentationContext getDocumentationContext() {
		final IModelDescription model = getModelDescription();
		if (model == null) return null;
		return model.getDocumentationContext();
	}

	/**
	 * Gets the origin name of this symbol.
	 *
	 * @return the origin name
	 */
	@Override
	public String getOriginName() { return originName; }

	/**
	 * Sets the origin name if not already set.
	 *
	 * @param name
	 *            the new origin name
	 */
	@Override
	public void setOriginName(final String name) {
		if (originName == null) { originName = name; }
	}

	/**
	 * Resets the origin name to null.
	 */
	@Override
	public void resetOriginName() {
		originName = null;
	}

	/**
	 * Validates this description, checking that it is correctly defined within its context and that its facets are
	 * valid.
	 *
	 * @return this description if validation passed, null if it failed
	 */
	@Override
	public IDescription validate() {
		if (isSet(Flag.IsValidated)) return this;
		set(Flag.IsValidated);

		if (isBuiltIn()) {
			// We simply make sure that the facets are correctly compiled
			validateFacets();
			return this;
		}
		final IDescription enclosing = getEnclosingDescription();
		if (enclosing != null) {
			final String kw = getKeyword();
			final String ekw = enclosing.getKeyword();
			// We first verify that the description is at the right place
			if (!artefact.canBeDefinedIn(enclosing)) {
				error(kw + " cannot be defined in " + ekw, IGamlIssue.WRONG_CONTEXT);
				return null;
			}
			// If it is supposed to be unique, we verify this
			if (artefact.isUniqueInContext()) {
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
		if (artefact.getDeprecated() != null) {
			warning("'" + getKeyword() + "' is deprecated. " + artefact.getDeprecated(), IGamlIssue.DEPRECATED);
		}

		// If a custom validator has been defined, run it
		if (!artefact.getValidator().validate(this, element)) return null;
		return this;
	}

	/**
	 * Validates the facets of this description, checking for missing required facets, deprecated facets, type
	 * compatibility, etc.
	 *
	 * @return true if all facets are valid, false otherwise
	 */
	private final boolean validateFacets() {
		if (!visitFacets((facet, b) -> {
			if (IInternalFacets.GAML_ERROR.equals(facet)) {
				error(getLitteral(facet));
				return false;
			}
			if (IInternalFacets.GAML_WARNING.equals(facet)) { warning(getLitteral(facet)); }
			// if (!ArtefactRegistry.getDoFacets().contains(facet)) { args.put(facet, b); }
			return true;
		})) return false;
		removeFacets(IInternalFacets.GAML_ERROR, IInternalFacets.GAML_WARNING);

		// Special case for "do", which can accept (at parsing time) any facet
		final boolean isDo = isInvocation();
		final boolean isBuiltIn = isBuiltIn();
		final List<String> mandatory = artefact.getMandatoryFacets();
		if (mandatory != null) {
			for (final String facet : mandatory) {
				if (!hasFacets() || !facets.containsKey(facet)) {
					error("Missing facet " + facet, IGamlIssue.MISSING_FACET, getUnderlyingElement(), facet, "nil");
					return false;
				}
			}
		}

		return visitFacets((facet, expr) -> {
			final IArtefact.Facet fp = artefact.getFacet(facet);
			if (fp == null) return processUnknowFacet(isDo, facet);
			if (fp.getDeprecated() != null) {
				warning("Facet '" + facet + "' is deprecated: " + fp.getDeprecated(), IGamlIssue.DEPRECATED, facet);
			}
			if (fp.getValues() != null) {
				if (!processMultiValuedFacet(facet, expr, fp)) return false;
			} else {
				// Some expressions might not be compiled
				IExpression exp = compileExpression(facet, expr, fp);
				if (exp != null && !isBuiltIn) {
					final IType<?> actualType = exp.getGamlType();
					// Special case for init. Temporary solution before we can pass ITypeProvider.OWNER_TYPE to the init
					// facet. Concerned types are point and date, which belong to "NumberVariable" and can accept nil,
					// while int and float cannot
					if (IKeyword.INIT.equals(fp.getName())) {
						IType<?> requestedType = SymbolDescription.this.getGamlType();
						if ((Types.POINT == requestedType || Types.DATE == requestedType)
								&& actualType == Types.NO_TYPE)
							return true;
					}
					final IType<?> contentType = fp.getContentType();
					final IType<?> keyType = fp.getKeyType();
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
	 * @param facet
	 *            the facet name
	 * @param expr
	 *            the expression description
	 * @param fp
	 *            the facet prototype
	 * @return the compiled expression
	 */
	private IExpression compileExpression(final String facet, final IExpressionDescription expr,
			final IArtefact.Facet fp) {
		IExpression exp;
		if (fp.isNewTemp()) {
			exp = createVarWithTypes(facet);
			// DEBUG.OUT("Type of IExpressionDescription is " + expr.getClass().getSimpleName());
			expr.setExpression(exp);
		} else if (!fp.isLabel()) {
			if (fp.isRemote() && this instanceof StatementRemoteWithChildrenDescription srwc) {
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
	 * @param facet
	 *            the facet name
	 * @param fp
	 *            the facet prototype
	 * @param actualType
	 *            the actual type of the expression
	 * @param contentType
	 *            the expected content type
	 * @param keyType
	 *            the expected key type
	 */
	private void emitFacetTypesIncompatibilityWarning(final String facet, final IArtefact.Facet fp,
			final IType<?> actualType, final IType<?> contentType, final IType<?> keyType) {
		final String[] strings = new String[fp.getTypes().length];
		for (int i = 0; i < fp.getTypes().length; i++) {
			IType<?> requestedType2 = fp.getTypes()[i];
			if (requestedType2.isContainer()) { requestedType2 = GamaType.from(requestedType2, keyType, contentType); }
			strings[i] = requestedType2.toString();
		}

		warning("Facet '" + facet + "' is expecting " + Arrays.toString(strings) + " instead of " + actualType,
				IGamlIssue.SHOULD_CAST, facet, fp.getTypes()[0].toString());
	}

	/**
	 * Verifies that a facet's expression type is compatible with its expected types.
	 *
	 * @param fp
	 *            the facet prototype
	 * @param exp
	 *            the compiled expression
	 * @param actualType
	 *            the actual type of the expression
	 * @param contentType
	 *            the expected content type
	 * @param keyType
	 *            the expected key type
	 * @return true if the types are compatible, false otherwise
	 */
	private boolean verifyFacetTypesCompatibility(final IArtefact.Facet fp, final IExpression exp,
			final IType<?> actualType, final IType<?> contentType, final IType<?> keyType) {
		boolean compatible = false;
		for (final IType<?> definedType : fp.getTypes()) {
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
			if (compatible) { break; }
		}

		return compatible;
	}

	/**
	 * Processes a multi-valued facet, checking that its value is among the accepted values.
	 *
	 * @param facet
	 *            the facet name
	 * @param expr
	 *            the expression description
	 * @param fp
	 *            the facet prototype
	 * @return true if the value is valid, false otherwise
	 */
	private boolean processMultiValuedFacet(final String facet, final IExpressionDescription expr,
			final IArtefact.Facet fp) {
		final String val = expr.getExpression().literalValue();
		// We have a multi-valued facet
		if (!fp.getValues().contains(val)) {
			error("Facet '" + facet + "' is expecting a value among " + fp.getValues() + " instead of " + val, facet);
			return false;
		}
		return true;
	}

	/**
	 * Processes an unknown facet, handling special cases and reporting errors.
	 *
	 * @param isDo
	 *            whether this is a "do" statement, which allows arbitrary facets
	 * @param facet
	 *            the facet name
	 * @return true if the facet can be accepted, false otherwise
	 */
	private boolean processUnknowFacet(final boolean isDo, final String facet) {
		switch (facet) {
			case IInternalFacets.DUPLICATE_FACET: {
				final String correct = getLitteral(facet);
				final String error = "Facet " + correct + " is declared twice. Please correct.";
				error(error, IGamlIssue.DUPLICATE_DEFINITION, facet, "1");
				error(error, IGamlIssue.DUPLICATE_DEFINITION, correct, "2");
				return false;
			}
			case IInternalFacets.GAML_ERROR:
				this.error(getLitteral(facet));
				return false;
			case IInternalFacets.GAML_WARNING:
				this.warning(getLitteral(facet));
				return true;
			case null:
			default:
				break;
		}
		if (!isDo) {
			error("Unknown facet " + facet, IGamlIssue.UNKNOWN_FACET, facet);
			return false;
		}
		return true;
	}

	/**
	 * Creates a variable expression with types for a facet. Default implementation returns null; subclasses may
	 * override.
	 *
	 * @param tag
	 *            the facet tag
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
	 * Compiles this description into a runtime symbol. This method validates the description first, then creates and
	 * initializes the symbol.
	 *
	 * @return the compiled symbol, or null if compilation failed
	 */
	@Override
	public ISymbol compile() {
		validate();
		final ISymbol cs = artefact.create(this);
		if (cs == null) return null;
		if (artefact.hasArgs()) {
			((IStatement.WithArgs) cs).setFormalArgs(((StatementDescription) this).createCompiledArgs());
		}
		if (artefact.hasSequence() && !artefact.isPrimitive()) { cs.setChildren(compileChildren()); }
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
			if (s != null) { lce.add(s); }
			return true;
		});
		return lce;
	}

	/**
	 * Gets all child descriptions with the specified keyword.
	 *
	 * @param aKeyword
	 *            the keyword to filter by
	 * @return an iterable of matching descriptions
	 */
	@Override
	public Iterable<IDescription> getChildrenWithKeyword(final String aKeyword) {
		return StreamSupport.stream(getOwnChildren().spliterator(), false).filter(d -> d.getKeyword().equals(aKeyword))
				.toList();
	}

	/**
	 * Gets the first child description with the specified keyword.
	 *
	 * @param aKeyword
	 *            the keyword to search for
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
	 * Gets all facets for this description. Note: When possible, prefer using visitFacets() for better performance.
	 *
	 * @return the facets, or NULL if none exist
	 */
	@Override
	public Facets getFacets() { return facets == null ? Facets.NULL : facets; }

	/**
	 * Attaches an alternate variable description provider. Default implementation does nothing; subclasses may
	 * override.
	 *
	 * @param vp
	 *            the variable description provider
	 */
	@Override
	public void attachAlternateVarDescriptionProvider(final IVarDescriptionProvider vp) {}

	/**
	 * Finds a child description in a container that matches the keyword and name of the given description.
	 *
	 * @param container
	 *            the container to search in
	 * @param desc
	 *            the description to match
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
	 * Replaces the children of this description with the given descriptions. Default implementation does nothing;
	 * subclasses may override.
	 *
	 * @param array
	 *            the new children
	 */
	@Override
	public void replaceChildrenWith(final Iterable<IDescription> array) {}

	/**
	 * Sets the type of this symbol.
	 *
	 * @param type
	 *            the new type
	 */
	private void setType(final IType<?> type) { this.type = type; }

}
