/*******************************************************************************************************
 *
 * IDescription.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.descriptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import com.google.common.base.Function;

import gama.annotations.constants.IKeyword;
import gama.annotations.support.ISymbolKind;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.serialization.ISymbolSerializer;
import gama.api.compilation.serialization.ModelSerializer;
import gama.api.compilation.serialization.SpeciesSerializer;
import gama.api.compilation.serialization.StatementSerializer;
import gama.api.compilation.serialization.VarSerializer;
import gama.api.compilation.validation.IDocumentationContext;
import gama.api.compilation.validation.IValidationContext;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITyped;
import gama.api.utils.benchmark.IBenchmarkable;
import gama.api.utils.collections.ICollector;
import gama.api.utils.interfaces.BiConsumerWithPruning;
import gama.api.utils.interfaces.ConsumerWithPruning;
import gama.api.utils.interfaces.IDisposable;

/**
 * Central abstraction for every element of a GAML model that has been parsed and turned into an in-memory
 * representation. An {@code IDescription} sits at the heart of the GAML compilation pipeline: it is produced during
 * parsing, carries structural and type information, is validated against the GAML grammar and type system, and finally
 * compiled into a runnable {@link ISymbol}.
 *
 * <p>
 * Descriptions form a tree that mirrors the syntactic structure of a model: a model description contains species
 * descriptions, which in turn contain variable and action descriptions, and so on. Each node in the tree knows its
 * {@linkplain #getEnclosingDescription() enclosing description} and provides navigational helpers to walk up and down
 * the tree.
 * </p>
 *
 * <p>
 * A description also acts as a facet container: every GAML keyword can have a set of named
 * {@linkplain #getFacet(String) facets} (attributes), each backed by an {@link IExpressionDescription} that is resolved
 * and type-checked during validation.
 * </p>
 *
 * <p>
 * Descriptions are disposable: once no longer needed they must be {@linkplain #dispose() disposed} to release their
 * internal state. The {@link #DISPOSING_VISITOR} constant provides a ready-to-use visitor for that purpose.
 * </p>
 *
 * <p>
 * This interface extends:
 * </p>
 * <ul>
 * <li>{@link IGamlDescription} – provides name and documentation access.</li>
 * <li>{@link ITyped} – exposes the GAML type of the described element.</li>
 * <li>{@link IDisposable} – lifecycle management.</li>
 * <li>{@link IVarDescriptionProvider} – ability to declare and look up variable descriptions.</li>
 * <li>{@link IVarDescriptionUser} – ability to reference variables declared elsewhere.</li>
 * <li>{@link IBenchmarkable} – support for performance benchmarking.</li>
 * </ul>
 *
 * @author Alexis Drogoul, IRD/SU UMMISCO – initial design and implementation
 * @see IModelDescription
 * @see ISpeciesDescription
 * @see IVariableDescription
 * @see IActionDescription
 */
@SuppressWarnings ({ "rawtypes" })
public interface IDescription
		extends IGamlDescription, ITyped, IDisposable, IVarDescriptionProvider, IVarDescriptionUser, IBenchmarkable {

	// =========================================================================
	// Constants – serializers, type-provider facets, utility functions
	// =========================================================================

	/**
	 * Default (no-op) symbol serializer used as a fallback when no specialised serializer is available for a given
	 * description kind.
	 */
	ISymbolSerializer SYMBOL_SERIALIZER = new ISymbolSerializer() {};

	/**
	 * Serializer dedicated to variable (attribute) descriptions. It handles the {@code var}, {@code let} and related
	 * keywords.
	 */
	ISymbolSerializer VAR_SERIALIZER = new VarSerializer();

	/**
	 * Serializer dedicated to species descriptions. It handles the {@code species} keyword and its sub-elements.
	 */
	ISymbolSerializer SPECIES_SERIALIZER = new SpeciesSerializer();

	/**
	 * Serializer dedicated to model descriptions. It handles the top-level {@code model} keyword and all its structural
	 * children.
	 */
	ISymbolSerializer MODEL_SERIALIZER = new ModelSerializer();

	/**
	 * Serializer dedicated to statement descriptions. It handles action bodies and general statement blocks.
	 */
	ISymbolSerializer STATEMENT_SERIALIZER = new StatementSerializer();

	/**
	 * Immutable set of facet names whose value can influence the GAML type of the described symbol. When any of these
	 * facets is present on a description, the type inference engine will consult it during type resolution.
	 *
	 * <p>
	 * Current members: {@code value}, {@code type}, {@code as}, {@code species}, {@code of}, {@code over},
	 * {@code from}, {@code index}, {@code function}, {@code update}, {@code init}, {@code default}.
	 * </p>
	 */
	Set<String> typeProviderFacets = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(IKeyword.VALUE,
			IKeyword.TYPE, IKeyword.AS, IKeyword.SPECIES, IKeyword.OF, IKeyword.OVER, IKeyword.FROM, IKeyword.INDEX,
			IKeyword.FUNCTION, IKeyword.UPDATE, IKeyword.INIT, IKeyword.DEFAULT)));

	/**
	 * A {@link Function} that extracts the {@linkplain IGamlDescription#getName() name} of an {@code IDescription}.
	 * Useful as a key extractor when collecting descriptions into maps or sorted structures.
	 */
	Function<? super IDescription, ? extends String> TO_NAME = IDescription::getName;

	// =========================================================================
	// Nested types – visitor and functional interfaces
	// =========================================================================

	/**
	 * A visitor that can be applied to a tree of {@link IDescription} nodes. Implementations return {@code true} to
	 * continue the traversal and {@code false} to prune the current branch (stop descending into children).
	 *
	 * <p>
	 * This is a specialisation of {@link ConsumerWithPruning} typed to {@code IDescription} sub-types.
	 * </p>
	 *
	 * @param <T>
	 *            the concrete {@code IDescription} sub-type accepted by this visitor
	 */
	@FunctionalInterface
	public interface DescriptionVisitor<T extends IDescription> extends ConsumerWithPruning<T> {}

	/**
	 * A visitor that is applied to the facets of a description. Each call receives the facet name and its
	 * {@link IExpressionDescription}. Returning {@code false} stops the iteration.
	 *
	 * <p>
	 * This is a specialisation of {@link BiConsumerWithPruning} for {@code (String, IExpressionDescription)} pairs.
	 * </p>
	 */
	@FunctionalInterface
	public interface IFacetVisitor extends BiConsumerWithPruning<String, IExpressionDescription> {}

	// =========================================================================
	// Constants – pre-built visitors
	// =========================================================================

	/**
	 * A ready-to-use {@link DescriptionVisitor} that {@linkplain #validate() validates} each description it visits. The
	 * traversal continues as long as validation succeeds (returns a non-{@code null} description).
	 */
	DescriptionVisitor<IDescription> VALIDATING_VISITOR = desc -> (desc.validate() != null);

	/**
	 * A ready-to-use {@link DescriptionVisitor} that {@linkplain #dispose() disposes} each description it visits and
	 * always continues the traversal ({@code true}) so that the entire subtree is cleaned up.
	 */
	DescriptionVisitor<IDescription> DISPOSING_VISITOR = desc -> {
		desc.dispose();
		return true;
	};

	// =========================================================================
	// Identification and classification
	// =========================================================================

	/**
	 * Returns the GAML keyword that introduced this description in the source model (e.g. {@code "species"},
	 * {@code "var"}, {@code "do"}, …).
	 *
	 * @return the keyword string; never {@code null}
	 */
	String getKeyword();

	/**
	 * Returns the {@link ISymbolKind} that categorises this description within the GAML grammar hierarchy (e.g.
	 * {@code SPECIES}, {@code STATEMENT}, {@code VARIABLE}, …).
	 *
	 * @return the kind of this description; never {@code null}
	 */
	ISymbolKind getKind();

	/**
	 * Returns {@code true} if this description represents a built-in (platform-provided) construct rather than a
	 * user-defined one. Built-in descriptions are never serialized back to source.
	 *
	 * @return {@code true} if built-in
	 */
	boolean isBuiltIn();

	/**
	 * Returns {@code true} if this description represents a species ({@code species} keyword).
	 *
	 * @return {@code true} if this is a species description
	 */
	boolean isSpecies();

	/**
	 * Returns {@code true} if this description represents the top-level {@code model} block.
	 *
	 * @return {@code true} if this is a model description
	 */
	boolean isModel();

	/**
	 * Returns {@code true} if this description represents an {@code experiment} block.
	 *
	 * @return {@code true} if this is an experiment description
	 */
	boolean isExperiment();

	/**
	 * Returns {@code true} if this description represents a Java-level class wrapper (a "class" species).
	 *
	 * @return {@code true} if this is a class description
	 */
	boolean isClass();

	/**
	 * Returns {@code true} if this description is abstract, i.e. it declares at least one abstract action that must be
	 * overridden by sub-species.
	 *
	 * @return {@code true} if abstract
	 */
	boolean isAbstract();

	/**
	 * Returns {@code true} if this description represents an action invocation ({@code do} or {@code invoke}
	 * statement).
	 *
	 * @return {@code true} if this is an invocation
	 */
	boolean isInvocation();

	/**
	 * Returns {@code true} if this description is an argument ({@code arg} keyword) that has been declared with the
	 * {@code id: true} facet, marking it as an identifier-style argument.
	 *
	 * @return {@code true} if this is an identifier argument
	 */
	default boolean isID() {
		return IKeyword.ARG.equals(this.getKeyword()) && "true".equals(getLitteral(IKeyword.ID));
	}

	// =========================================================================
	// Origin and plugin metadata
	// =========================================================================

	/**
	 * Returns the name of the model or resource from which this description originates. For built-in descriptions this
	 * is typically the name of the defining plug-in.
	 *
	 * @return the origin name; may be {@code null} for descriptions that have not yet been assigned one
	 */
	String getOriginName();

	/**
	 * Sets the origin name of this description.
	 *
	 * @param name
	 *            the new origin name
	 */
	void setOriginName(String name);

	/**
	 * Resets the origin name to its default value (usually derived from the enclosing model).
	 */
	void resetOriginName();

	/**
	 * Records the identifier of the Eclipse plug-in that contributed this description. This is used by the
	 * documentation and serialization frameworks to locate the source of built-in constructs.
	 *
	 * @param plugin
	 *            the bundle symbolic name of the defining plug-in
	 */
	void setDefiningPlugin(String plugin);

	// =========================================================================
	// Tree navigation – enclosing and contextual descriptions
	// =========================================================================

	/**
	 * Returns the description that immediately encloses this one in the description tree, or {@code null} if this is a
	 * root description.
	 *
	 * @return the enclosing description, or {@code null}
	 */
	IDescription getEnclosingDescription();

	/**
	 * Sets the enclosing description of this node, linking it into the description tree.
	 *
	 * @param desc
	 *            the new enclosing description
	 */
	void setEnclosingDescription(final IDescription desc);

	/**
	 * Returns the nearest enclosing {@link IModelDescription}, traversing the tree upward. Returns {@code null} if no
	 * model description is found in the ancestry chain.
	 *
	 * @return the nearest model description, or {@code null}
	 */
	IModelDescription getModelDescription();

	/**
	 * Returns the nearest enclosing {@link ISpeciesDescription}, traversing the tree upward. Statements and variables
	 * use this to resolve species-relative type information and variable scopes.
	 *
	 * @return the nearest species context, or {@code null}
	 */
	ITypeDescription getTypeContext();

	/**
	 * Returns {@code true} if this description is a direct or indirect child of a description whose keyword equals
	 * {@code ancestor}.
	 *
	 * @param ancestor
	 *            the keyword to look for among ancestors
	 * @return {@code true} if such an ancestor exists in the enclosing chain
	 */
	default boolean isIn(final String ancestor) {
		IDescription d = this.getEnclosingDescription();
		if (d == null || d == this) return false;
		if (d.getKeyword().equals(ancestor)) return true;
		return d.isIn(ancestor);
	}

	/**
	 * Walks up the enclosing chain and returns the first {@link IDescription} whose keyword equals {@code ancestor}, or
	 * {@code null} if no such description exists.
	 *
	 * @param ancestor
	 *            the keyword to search for
	 * @return the first enclosing description with the given keyword, or {@code null}
	 */
	default IDescription getParentWithKeyword(final String ancestor) {
		IDescription d = this.getEnclosingDescription();
		if (d == null || d == this) return null;
		if (d.getKeyword().equals(ancestor)) return d;
		return d.getParentWithKeyword(ancestor);
	}

	// =========================================================================
	// Tree navigation – children
	// =========================================================================

	/**
	 * Returns all direct children of this description. The returned iterable is a snapshot; modifications to the
	 * description tree after this call are not reflected in it.
	 *
	 * @return an {@link Iterable} of direct child descriptions; never {@code null}
	 */
	Iterable<IDescription> getOwnChildren();

	/**
	 * Returns all direct children whose keyword matches the given string.
	 *
	 * @param keyword
	 *            the keyword to filter by
	 * @return an {@link Iterable} of matching children; never {@code null}
	 */
	Iterable<IDescription> getChildrenWithKeyword(String keyword);

	/**
	 * Returns the first direct child whose keyword matches the given string, or {@code null} if none exists.
	 *
	 * @param keyword
	 *            the keyword to search for
	 * @return the first matching child, or {@code null}
	 */
	IDescription getChildWithKeyword(String keyword);

	/**
	 * Adds a child description to this node. Default implementation is a no-op; override in concrete classes that
	 * support children.
	 *
	 * @param child
	 *            the child description to add
	 */
	default void addChild(final IDescription child) {}

	/**
	 * Replaces all current children of this description with the given collection. Existing children are discarded (but
	 * <em>not</em> automatically disposed).
	 *
	 * @param array
	 *            the new children; must not be {@code null}
	 */
	void replaceChildrenWith(Iterable<IDescription> array);

	// =========================================================================
	// Tree navigation – variable and action scope
	// =========================================================================

	/**
	 * Walks up the description tree to find the nearest {@link IVarDescriptionProvider} that declares a variable with
	 * the given name.
	 *
	 * @param name
	 *            the variable name to look up
	 * @return the provider that declares the variable, or {@code null} if not found
	 */
	IVarDescriptionProvider getDescriptionDeclaringVar(final String name);

	/**
	 * Walks up the description tree to find the nearest {@link ITypeDescription} that declares an action with the given
	 * name.
	 *
	 * @param name
	 *            the action name to look up
	 * @param superInvocation
	 *            {@code true} if the lookup should start one level above the current description (used for
	 *            {@code super} calls)
	 * @return the type description that declares the action, or {@code null} if not found
	 */
	ITypeDescription getDescriptionDeclaringAction(final String name, boolean superInvocation);

	/**
	 * Looks up a species description by name, searching first in the enclosing model and then in known types.
	 *
	 * @param actualSpecies
	 *            the species name
	 * @return the matching {@link ISpeciesDescription}, or {@code null} if not found
	 */
	ISpeciesDescription getSpeciesDescription(String actualSpecies);

	/**
	 * Returns the action description with the given name that is visible from within this description, or {@code null}
	 * if no such action exists.
	 *
	 * @param name
	 *            the action name
	 * @return the {@link IActionDescription}, or {@code null}
	 */
	IActionDescription getAction(String name);

	/**
	 * Returns a named {@link IType} that is visible from within this description. The search follows the scoping rules
	 * of GAML (current species → model → built-in types).
	 *
	 * @param s
	 *            the type name
	 * @return the corresponding {@link IType}, or {@code null} if not found
	 */
	IType getTypeNamed(String s);

	/**
	 * Returns {@code true} if this description (or any scope it delegates to) provides a variable named {@code name}.
	 *
	 * @param name
	 *            the variable name to test
	 * @return {@code true} if the variable is visible from this description
	 */
	boolean manipulatesVar(final String name);

	/**
	 * Attaches an alternate {@link IVarDescriptionProvider} to this description. The alternate provider is consulted
	 * after the description itself when resolving variable references.
	 *
	 * @param vp
	 *            the provider to attach; must not be {@code null}
	 */
	void attachAlternateVarDescriptionProvider(final IVarDescriptionProvider vp);

	// =========================================================================
	// Facet access and modification
	// =========================================================================

	/**
	 * Returns the {@link Facets} map that holds all facets of this description. The map is live: changes to it are
	 * reflected in this description.
	 *
	 * @return the facets map; never {@code null}
	 */
	Facets getFacets();

	/**
	 * Returns {@code true} if this description has a facet with the given name.
	 *
	 * @param name
	 *            the facet name to test
	 * @return {@code true} if the facet is present
	 */
	boolean hasFacet(String name);

	/**
	 * Returns the {@link IExpressionDescription} associated with the given facet name, or {@code null} if the facet is
	 * absent.
	 *
	 * @param name
	 *            the facet name
	 * @return the expression description, or {@code null}
	 */
	IExpressionDescription getFacet(String name);

	/**
	 * Returns the {@link IExpressionDescription} associated with the first facet name (in argument order) that is
	 * present on this description, or {@code null} if none is found.
	 *
	 * @param names
	 *            one or more facet names to test, in priority order
	 * @return the first matching expression description, or {@code null}
	 */
	IExpressionDescription getFacet(String... names);

	/**
	 * Returns the name of the first facet (among those provided) that is present on this description, or {@code null}
	 * if none is found.
	 *
	 * @param names
	 *            one or more facet names to test, in priority order
	 * @return the first present facet name, or {@code null}
	 */
	String firstFacetFoundAmong(final String... names);

	/**
	 * Returns the compiled {@link IExpression} for the first facet name (in argument order) that is present and has
	 * been compiled, or {@code null} if no such facet exists.
	 *
	 * @param names
	 *            one or more facet names to test, in priority order
	 * @return the first compiled expression, or {@code null}
	 */
	IExpression getFacetExpr(final String... names);

	/**
	 * Returns the literal string value of the facet with the given name (as written in the source model), or
	 * {@code null} if the facet is absent or does not have a plain literal value.
	 *
	 * @param name
	 *            the facet name
	 * @return the literal value string, or {@code null}
	 */
	String getLitteral(String name);

	/**
	 * Assigns an {@link IExpressionDescription} to the named facet, replacing any existing value.
	 *
	 * @param name
	 *            the facet name
	 * @param exp
	 *            the new expression description; must not be {@code null}
	 */
	void setFacetExprDescription(String name, IExpressionDescription exp);

	/**
	 * Assigns a compiled {@link IExpression} to the named facet, wrapping it in an expression description
	 * automatically.
	 *
	 * @param name
	 *            the facet name
	 * @param exp
	 *            the compiled expression; must not be {@code null}
	 */
	void setFacet(String name, IExpression exp);

	/**
	 * Removes one or more facets from this description. Has no effect for facet names that are not present.
	 *
	 * @param names
	 *            the names of the facets to remove
	 */
	void removeFacets(String... names);

	// =========================================================================
	// Facet and child traversal (visitor pattern)
	// =========================================================================

	/**
	 * Visits all facets of this description. Equivalent to {@code visitFacets(null, visitor)}.
	 *
	 * @param visitor
	 *            the facet visitor to apply; returning {@code false} stops the iteration
	 * @return {@code true} if all facets were visited, {@code false} if iteration was stopped early
	 */
	default boolean visitFacets(final IFacetVisitor visitor) {
		return visitFacets(null, visitor);
	}

	/**
	 * Visits a restricted set of facets on this description. Only facets whose names are contained in {@code facets}
	 * are passed to the visitor. If {@code facets} is {@code null} all facets are visited.
	 *
	 * @param facets
	 *            the set of facet names to restrict the visit to, or {@code null} to visit all facets
	 * @param visitor
	 *            the facet visitor to apply; returning {@code false} stops the iteration
	 * @return {@code true} if all targeted facets were visited, {@code false} if iteration was stopped early
	 */
	boolean visitFacets(Set<String> facets, IFacetVisitor visitor);

	/**
	 * Visits the direct children of this description that have been explicitly declared by the user (i.e. the "own"
	 * children, as opposed to inherited ones).
	 *
	 * @param visitor
	 *            the visitor to apply; returning {@code false} prunes the subtree
	 * @return {@code true} if all own children were visited, {@code false} if the visit was pruned
	 */
	boolean visitOwnChildren(DescriptionVisitor<IDescription> visitor);

	/**
	 * Recursively visits all own children of this description (depth-first). The visitor is called on each node;
	 * returning {@code false} prunes that node's subtree.
	 *
	 * @param visitor
	 *            the visitor to apply
	 * @return {@code true} if all nodes were visited, {@code false} if the visit was pruned somewhere
	 */
	boolean visitOwnChildrenRecursively(DescriptionVisitor<IDescription> visitor);

	/**
	 * Visits all children of this description, including those contributed by other mechanisms (e.g. inherited
	 * members). For most descriptions this is equivalent to {@link #visitOwnChildren}, but species and model
	 * descriptions may include additional members.
	 *
	 * @param visitor
	 *            the visitor to apply; returning {@code false} prunes the traversal
	 * @return {@code true} if all children were visited, {@code false} if the visit was pruned
	 */
	boolean visitChildren(DescriptionVisitor<IDescription> visitor);

	/**
	 * Collects variables of the given species that are referenced (used) by expressions in this description or any of
	 * its children. Results are accumulated in {@code result}.
	 *
	 * <p>
	 * Overrides the default implementation in {@link IVarDescriptionUser} by walking both facets and own children.
	 * </p>
	 *
	 * @param species
	 *            the species whose variables are of interest
	 * @param alreadyProcessed
	 *            a collector used to avoid infinite recursion; descriptions already in this set are skipped
	 * @param result
	 *            accumulator for the variable descriptions found
	 */
	@Override
	default void collectUsedVarsOf(final ITypeDescription species,
			final ICollector<IVarDescriptionUser> alreadyProcessed, final ICollector<IVariableDescription> result) {
		if (alreadyProcessed.contains(this)) return;
		alreadyProcessed.add(this);
		this.visitFacets((name, exp) -> {
			final IExpression expression = exp.getExpression();
			if (expression != null) { expression.collectUsedVarsOf(species, alreadyProcessed, result); }
			return true;
		});
		this.visitOwnChildren(desc -> {
			desc.collectUsedVarsOf(species, alreadyProcessed, result);
			return true;
		});
	}

	// =========================================================================
	// Compilation and validation
	// =========================================================================

	/**
	 * Validates this description against the GAML grammar and type system. Validation may produce errors, warnings or
	 * informational messages (recorded on the {@link IValidationContext}). Returns this description if validation
	 * succeeds, or {@code null} if the description is structurally invalid and cannot be compiled.
	 *
	 * @return this description after validation, or {@code null} on structural failure
	 */
	IDescription validate();

	/**
	 * Compiles this (already-validated) description into a runnable {@link ISymbol}. Must only be called after a
	 * successful {@link #validate()}.
	 *
	 * @return the compiled symbol; never {@code null} on success
	 */
	ISymbol compile();

	/**
	 * Copies this description into the given target description. The copy is a deep structural clone whose enclosing
	 * description is set to {@code into}.
	 *
	 * @param into
	 *            the new enclosing description for the copy
	 * @return a new {@code IDescription} that is a copy of this one, enclosed in {@code into}
	 */
	IDescription copy(IDescription into);

	/**
	 * Returns the {@link IValidationContext} that collects all validation markers (errors, warnings, infos) produced
	 * while compiling the model that contains this description.
	 *
	 * @return the validation context; never {@code null} after the description has been linked into a model
	 */
	IValidationContext getValidationContext();

	/**
	 * Returns the {@link IDocumentationContext} used to accumulate documentation items for this description and the
	 * model it belongs to.
	 *
	 * @return the documentation context; may be {@code null} if documentation is not being generated
	 */
	IDocumentationContext getDocumentationContext();

	// =========================================================================
	// Diagnostic messages (errors, warnings, infos)
	// =========================================================================

	/**
	 * Reports an error on the primary element of this description using no specific error code.
	 *
	 * @param message
	 *            the human-readable error message
	 */
	void error(final String message);

	/**
	 * Reports an error on the primary element of this description.
	 *
	 * @param message
	 *            the human-readable error message
	 * @param code
	 *            a symbolic error code (used by the IDE for quick-fixes and filtering)
	 */
	void error(final String message, String code);

	/**
	 * Reports an error on a named sub-element (facet) of this description.
	 *
	 * @param message
	 *            the human-readable error message
	 * @param code
	 *            a symbolic error code
	 * @param element
	 *            the name of the facet or sub-element on which the error should be placed
	 * @param data
	 *            optional additional data passed to the error processor (e.g. quick-fix hints)
	 */
	void error(final String message, String code, String element, String... data);

	/**
	 * Reports an error on a specific {@link EObject} node inside this description's underlying EMF resource.
	 *
	 * @param message
	 *            the human-readable error message
	 * @param code
	 *            a symbolic error code
	 * @param element
	 *            the EMF object on which the error should be placed
	 * @param data
	 *            optional additional data passed to the error processor
	 */
	void error(final String message, String code, EObject element, String... data);

	/**
	 * Reports a warning on the primary element of this description.
	 *
	 * @param message
	 *            the human-readable warning message
	 * @param code
	 *            a symbolic warning code
	 */
	void warning(final String message, String code);

	/**
	 * Reports a warning on a named sub-element (facet) of this description.
	 *
	 * @param message
	 *            the human-readable warning message
	 * @param code
	 *            a symbolic warning code
	 * @param element
	 *            the name of the facet or sub-element on which the warning should be placed
	 * @param data
	 *            optional additional data passed to the warning processor
	 */
	void warning(final String message, String code, String element, String... data);

	/**
	 * Reports a warning on a specific {@link EObject} node inside this description's underlying EMF resource.
	 *
	 * @param message
	 *            the human-readable warning message
	 * @param code
	 *            a symbolic warning code
	 * @param element
	 *            the EMF object on which the warning should be placed
	 * @param data
	 *            optional additional data passed to the warning processor
	 */
	void warning(final String message, String code, EObject element, String... data);

	/**
	 * Reports an informational message associated with a named facet of this description.
	 *
	 * @param message
	 *            the informational text
	 * @param code
	 *            a symbolic info code
	 * @param facet
	 *            the name of the facet the info is related to
	 * @param data
	 *            optional additional data
	 */
	void info(final String message, final String code, final String facet, final String... data);

	/**
	 * Reports an informational message associated with a specific {@link EObject} node inside this description's
	 * underlying EMF resource.
	 *
	 * @param message
	 *            the informational text
	 * @param code
	 *            a symbolic info code
	 * @param facet
	 *            the EMF object the info is related to
	 * @param data
	 *            optional additional data
	 */
	void info(final String message, final String code, final EObject facet, final String... data);

	/**
	 * Reports an informational message on the primary element of this description.
	 *
	 * @param message
	 *            the informational text
	 * @param code
	 *            a symbolic info code
	 */
	void info(final String message, final String code);

	// =========================================================================
	// EMF / artefact linkage
	// =========================================================================

	/**
	 * Returns the {@link EObject} in the EMF resource that underlies this description. An optional facet name or
	 * expression object can be provided to retrieve the element that corresponds to a specific facet rather than the
	 * root element of the description.
	 *
	 * @param facet
	 *            a facet name ({@link String}) or expression object whose underlying element is requested; {@code null}
	 *            returns the root element
	 * @param returnFacet
	 *            if {@code true}, return the element that represents the facet key itself rather than its value
	 * @return the underlying {@link EObject}; may be {@code null} for built-in descriptions
	 */
	EObject getUnderlyingElement(Object facet, boolean returnFacet);

	/**
	 * Convenience overload that returns the root underlying {@link EObject} for this description, without targeting any
	 * specific facet.
	 *
	 * @return the root underlying EMF element; may be {@code null} for built-in descriptions
	 */
	default EObject getUnderlyingElement() { return getUnderlyingElement(null, false); }

	/**
	 * Returns the {@link IArtefact.Symbol} that represents this description in the compiled artefact model. This is the
	 * bridge between the description tree and the platform's artefact representation.
	 *
	 * @return the artefact symbol; may be {@code null} before compilation
	 */
	IArtefact.Symbol getArtefact();

	// =========================================================================
	// Serialization
	// =========================================================================

	/**
	 * Returns the {@link ISymbolSerializer} that knows how to serialize this description back into GAML source text.
	 * The appropriate serializer is selected based on the kind of description (model, species, variable, statement, …).
	 *
	 * @return the serializer; never {@code null}
	 */
	ISymbolSerializer getSerializer();

	/**
	 * Returns a compact string representation of this description suitable for performance benchmarking reports. The
	 * default implementation delegates to the serializer to produce a single-line, non-recursive rendering.
	 *
	 * @return a benchmark-friendly name string
	 */
	@Override
	default String getNameForBenchmarks() {
		final StringBuilder sb = new StringBuilder();
		getSerializer().serializeNoRecursion(sb, this, false);
		return sb.toString();
	}
}
