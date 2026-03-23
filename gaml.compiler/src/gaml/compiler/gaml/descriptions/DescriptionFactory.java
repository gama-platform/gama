/*******************************************************************************************************
 *
 * DescriptionFactory.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.descriptions;

import static gama.annotations.support.ISymbolKind.EXPERIMENT;
import static gama.annotations.support.ISymbolKind.SPECIES;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.support.ISymbolKind;
import gama.api.additions.registries.ArtefactRegistry;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.ast.ISyntacticElement;
import gama.api.compilation.ast.ISyntacticElement.SyntacticVisitor;
import gama.api.compilation.descriptions.IClassDescription;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionFactory;
import gama.api.compilation.descriptions.IExperimentDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.descriptions.ISkillDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.ISpeciesDescription.Platform;
import gama.api.compilation.factories.ISymbolDescriptionFactory;
import gama.api.compilation.factories.ISymbolDescriptionFactory.Species;
import gama.api.constants.IGamlIssue;
import gama.api.gaml.GAML;
import gama.api.gaml.symbols.Facets;
import gama.api.kernel.agent.IAgentConstructor;
import gama.dev.DEBUG;
import gaml.compiler.gaml.ast.SyntacticFactory;
import gaml.compiler.gaml.factories.ModelFactory;
import gaml.compiler.gaml.factories.PlatformFactory;

/**
 * Central factory for creating description objects from GAML syntactic elements.
 *
 * <p>
 * DescriptionFactory serves as the primary entry point for converting the Abstract Syntax Tree (ISyntacticElement)
 * produced by the parser into semantic description objects (IDescription subclasses). It implements the Factory Method
 * and Singleton patterns to provide centralized, thread-safe description creation.
 * </p>
 *
 * <p>
 * <strong>Architectural Role:</strong>
 * </p>
 *
 * <pre>
 * Parser → ISyntacticElement (AST)
 *   → DescriptionFactory.create() ← THIS CLASS
 *   → Specialized ISymbolDescriptionFactory
 *   → SymbolDescription subclass
 * </pre>
 *
 * <p>
 * <strong>Key Responsibilities:</strong>
 * </p>
 * <ul>
 * <li><strong>Factory Dispatch:</strong> Routes creation requests to specialized factories based on keyword</li>
 * <li><strong>AST Traversal:</strong> Recursively creates descriptions from syntactic element trees</li>
 * <li><strong>Species Building:</strong> Constructs complex species hierarchies with proper inheritance</li>
 * <li><strong>Model Assembly:</strong> Assembles complete model descriptions from parts</li>
 * <li><strong>Validation Coordination:</strong> Triggers validation after description creation</li>
 * </ul>
 *
 * <p>
 * <strong>Factory Registry:</strong>
 * </p>
 * <p>
 * Delegates to specialized factories registered in {@code GAML.DESCRIPTION_FACTORIES}:
 * </p>
 * <ul>
 * <li><strong>StatementFactory:</strong> Creates statement descriptions (if, loop, create, etc.)</li>
 * <li><strong>VariableFactory:</strong> Creates variable descriptions (int, float, string, etc.)</li>
 * <li><strong>SpeciesFactory:</strong> Creates species descriptions (species, grid)</li>
 * <li><strong>ExperimentFactory:</strong> Creates experiment descriptions</li>
 * <li><strong>ActionFactory:</strong> Creates action descriptions</li>
 * <li><strong>TypeFactory:</strong> Creates type descriptions</li>
 * </ul>
 *
 * <p>
 * <strong>Creation Process:</strong>
 * </p>
 * <ol>
 * <li>Receive syntactic element from parser</li>
 * <li>Determine symbol kind from keyword (species, action, statement, etc.)</li>
 * <li>Lookup specialized factory from registry</li>
 * <li>Delegate creation to specialized factory</li>
 * <li>Recursively process children if any</li>
 * <li>Link descriptions into hierarchy (set parent/enclosing relationships)</li>
 * <li>Return completed description</li>
 * </ol>
 *
 * <p>
 * <strong>Thread Safety:</strong>
 * </p>
 * <p>
 * Methods are synchronized to ensure thread-safe creation. However, GAML compilation is typically single-threaded, so
 * this synchronization may be overly conservative. Consider profiling to determine if finer-grained locking or lock
 * removal would improve performance.
 * </p>
 *
 * <p>
 * <strong>Performance Considerations:</strong>
 * </p>
 * <ul>
 * <li><strong>Factory Lookup:</strong> O(1) lookup from ArtefactRegistry</li>
 * <li><strong>Creation Time:</strong> O(n) where n is the number of symbols in the AST</li>
 * <li><strong>Memory:</strong> Transient - factory itself is lightweight singleton</li>
 * <li><strong>Bottleneck:</strong> Recursive child processing can be expensive for deep hierarchies</li>
 * </ul>
 *
 * <p>
 * <strong>Design Patterns:</strong>
 * </p>
 * <ul>
 * <li><strong>Singleton:</strong> Single factory instance for entire compilation process</li>
 * <li><strong>Factory Method:</strong> create() methods delegate to specialized factories</li>
 * <li><strong>Registry:</strong> Factories registered by symbol kind for extensibility</li>
 * <li><strong>Template Method:</strong> Common creation logic with specialized steps</li>
 * </ul>
 *
 * <p>
 * <strong>Usage Example:</strong>
 * </p>
 *
 * <pre>{@code
 * ISyntacticElement speciesElement = ...; // From parser
 * IDescription parentDesc = ...;           // Enclosing model/species
 *
 * // Create species description
 * IDescription speciesDesc = DescriptionFactory.getInstance()
 *     .create(speciesElement, parentDesc, null);
 *
 * // Description is now ready for validation and compilation
 * speciesDesc.validate();
 * ISymbol symbol = speciesDesc.compile();
 * }</pre>
 *
 * <p>
 * <strong>Optimization Opportunities:</strong>
 * </p>
 * <ol>
 * <li><strong>Factory Caching:</strong> Cache factory lookups for common keywords</li>
 * <li><strong>Batch Processing:</strong> Process multiple elements in batches to reduce overhead</li>
 * <li><strong>Lazy Children:</strong> Defer child creation until needed</li>
 * <li><strong>Object Pooling:</strong> Pool description objects for reuse</li>
 * <li><strong>Lock Optimization:</strong> Profile synchronization overhead and optimize</li>
 * </ol>
 *
 * <p>
 * <strong>Special Handling:</strong>
 * </p>
 * <ul>
 * <li><strong>Species:</strong> Complex creation with inheritance, skills, and control architecture</li>
 * <li><strong>Experiments:</strong> Special model assembly with experiment-specific context</li>
 * <li><strong>Actions:</strong> Argument processing and return type inference</li>
 * <li><strong>Variables:</strong> Type inference from multiple facets</li>
 * </ul>
 *
 * @author drogoul
 * @since 7 janv. 2011
 * @see IDescriptionFactory
 * @see SymbolDescription
 * @see ISymbolDescriptionFactory
 * @see ArtefactRegistry
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class DescriptionFactory implements IDescriptionFactory {

	/** The Constant INSTANCE. */
	private static final DescriptionFactory INSTANCE = new DescriptionFactory();

	/**
	 * Gets the single instance of DescriptionFactory.
	 *
	 * @return single instance of DescriptionFactory
	 */
	public static DescriptionFactory getInstance() { return INSTANCE; }

	/**
	 * Instantiates a new description factory.
	 */
	protected DescriptionFactory() {
		// Prevent instantiation
	}

	static {
		DEBUG.OFF();
	}

	/**
	 * Gets the factory.
	 *
	 * @param kind
	 *            the kind
	 * @return the factory
	 */
	private ISymbolDescriptionFactory getFactory(final ISymbolKind kind) {
		return GAML.DESCRIPTION_FACTORIES.get(kind);
	}

	/**
	 * Gets the factory.
	 *
	 * @param keyword
	 *            the keyword
	 * @return the factory
	 */
	private ISymbolDescriptionFactory getFactory(final String keyword) {
		final IArtefact.Symbol p = ArtefactRegistry.getArtefact(keyword, null);
		if (p != null) return getFactory(p.getKind());
		return null;
	}

	/**
	 * Creates the.
	 *
	 * @param factory
	 *            the factory
	 * @param keyword
	 *            the keyword
	 * @param superDesc
	 *            the super desc
	 * @param children
	 *            the children
	 * @param facets
	 *            the facets
	 * @return the i description
	 */
	// NOTE: synchronized keyword may be overly conservative if factory registries are already thread-safe.
	// Consider using more fine-grained locking or removing if compilation is single-threaded.
	@Override
	public /* synchronized */ IDescription create(final ISymbolDescriptionFactory factory, final String keyword,
			final IDescription superDesc, final Iterable<IDescription> children, final Facets facets) {
		return create(SyntacticFactory.getInstance().create(keyword, facets, children != null), superDesc, children);
	}

	/**
	 * Creates the.
	 *
	 * @param keyword
	 *            the keyword
	 * @param superDesc
	 *            the super desc
	 * @param children
	 *            the children
	 * @param facets
	 *            the facets
	 * @return the i description
	 */
	@Override
	public /* synchronized */ IDescription create(final String keyword, final IDescription superDesc,
			final Iterable<IDescription> children, final Facets facets) {
		return create(getFactory(keyword), keyword, superDesc, children, facets);
	}

	/**
	 * Creates the.
	 *
	 * @param keyword
	 *            the keyword
	 * @param superDesc
	 *            the super desc
	 * @param children
	 *            the children
	 * @return the i description
	 */
	@Override
	public /* synchronized */ IDescription create(final String keyword, final IDescription superDesc,
			final Iterable<IDescription> children) {
		return create(getFactory(keyword), keyword, superDesc, children, null);
	}

	/**
	 * Creates the.
	 *
	 * @param keyword
	 *            the keyword
	 * @param superDesc
	 *            the super desc
	 * @param children
	 *            the children
	 * @param facets
	 *            the facets
	 * @return the i description
	 */
	@Override
	public /* synchronized */ IDescription create(final String keyword, final IDescription superDesc,
			final Iterable<IDescription> children, final String... facets) {
		return create(getFactory(keyword), keyword, superDesc, children, new Facets(facets));
	}

	/**
	 * Creates the.
	 *
	 * @param keyword
	 *            the keyword
	 * @param superDescription
	 *            the super description
	 * @param facets
	 *            the facets
	 * @return the i description
	 */
	@Override
	public /* synchronized */ IDescription create(final String keyword, final IDescription superDescription,
			final String... facets) {
		return create(keyword, superDescription, null, facets);
	}

	/**
	 * Creates the.
	 *
	 * @param keyword
	 *            the keyword
	 * @param facets
	 *            the facets
	 * @return the i description
	 */
	@Override
	public /* synchronized */ IDescription create(final String keyword, final String... facets) {
		return create(keyword, GAML.getModelContext(), facets);
	}

	/**
	 * Creates a new Description object.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param superDesc
	 *            the super desc
	 * @param parent
	 *            the parent
	 * @param helper
	 *            the helper
	 * @param skills
	 *            the skills
	 * @param plugin
	 *            the plugin
	 * @return the species description
	 */
	@Override
	public ISpeciesDescription createBuiltInSpeciesDescription(final String name, final Class clazz,
			final ISpeciesDescription macro, final ISpeciesDescription parent, final IAgentConstructor helper,
			final Set<String> skills, final String plugin) {
		ISymbolDescriptionFactory.Species factory = (Species) getFactory(SPECIES);
		return factory.createBuiltInSpeciesDescription(name, clazz, macro, parent, helper, skills, plugin);
	}

	/**
	 * Creates a new Description object.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param superDesc
	 *            the super desc
	 * @param parent
	 *            the parent
	 * @param helper
	 *            the helper
	 * @param skills
	 *            the skills
	 * @param plugin
	 *            the plugin
	 * @return the species description
	 */
	@Override
	public IClassDescription createBuiltInClassDescription(final String name, final String plugin) {
		ISymbolDescriptionFactory.Clazz factory = (ISymbolDescriptionFactory.Clazz) getFactory(ISymbolKind.CLASS);
		return factory.createBuiltInClassDescription(name, plugin);
	}

	/**
	 * Creates a new Description object.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param macro
	 *            the macro
	 * @param parent
	 *            the parent
	 * @param helper
	 *            the helper
	 * @param skills
	 *            the skills
	 * @param plugin
	 *            the plugin
	 * @return the i skill description
	 */
	@Override
	public ISkillDescription createBuiltInSkillDescription(final String name, final Class clazz,
			final Iterable<IDescription> children, final String plugin) {
		ISymbolDescriptionFactory.Skill factory = (ISymbolDescriptionFactory.Skill) getFactory(ISymbolKind.SKILL);
		return factory.createBuiltInSkillDescription(name, clazz, children, plugin);
	}

	/**
	 * Creates a new Description object.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param macro
	 *            the macro
	 * @param parent
	 *            the parent
	 * @param helper
	 *            the helper
	 * @param allSkills
	 *            the all skills
	 * @param plugin
	 *            the plugin
	 * @return the species description
	 */
	@Override
	public ISpeciesDescription.Platform createPlatformSpeciesDescription(final String name, final Class clazz,
			final ISpeciesDescription macro, final ISpeciesDescription parent, final IAgentConstructor helper,
			final Set<String> allSkills, final String plugin) {
		ISymbolDescriptionFactory.Species factory = PlatformFactory.getInstance();
		return (Platform) factory.createBuiltInSpeciesDescription(name, clazz, macro, parent, helper, allSkills,
				plugin);
	}

	/**
	 * Creates a new Description object.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param superDesc
	 *            the super desc
	 * @param parent
	 *            the parent
	 * @param helper
	 *            the helper
	 * @param skills
	 *            the skills
	 * @param plugin
	 *            the plugin
	 * @return the species description
	 */
	@Override
	public IExperimentDescription createBuiltInExperimentDescription(final String name, final Class clazz,
			final ISpeciesDescription macro, final ISpeciesDescription parent, final IAgentConstructor helper,
			final Set<String> skills, final String plugin) {
		ISymbolDescriptionFactory.Species factory = (Species) getFactory(EXPERIMENT);
		return (IExperimentDescription) factory.createBuiltInSpeciesDescription(name, clazz, macro, parent, helper,
				skills, plugin);
	}

	/**
	 * Creates a new Description object.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param macro
	 *            the macro
	 * @param parent
	 *            the parent
	 * @param helper
	 *            the helper
	 * @param skills
	 *            the skills
	 * @param plugin
	 *            the plugin
	 * @return the model description
	 */
	@Override
	public IModelDescription createRootModelDescription(final String name, final Class clazz,
			final ISpeciesDescription macro, final ISpeciesDescription parent, final IAgentConstructor helper,
			final Set<String> skills, final String plugin) {
		ISymbolDescriptionFactory.Species factory = ModelFactory.getInstance();
		return (IModelDescription) factory.createBuiltInSpeciesDescription(name, clazz, macro, parent, helper, skills,
				plugin);
	}

	/**
	 * Creates the.
	 *
	 * @param source
	 *            the source
	 * @param superDesc
	 *            the super desc
	 * @param cp
	 *            the cp
	 * @return the i description
	 */
	@Override
	public final IDescription create(final ISyntacticElement source, final IDescription superDesc,
			final Iterable<IDescription> cp) {
		if (source == null) return null;
		final String keyword = source.getKeyword();
		IArtefact.Symbol md = ArtefactRegistry.getArtefact(keyword, superDesc);
		if (md == null) {
			if (superDesc == null) throw new RuntimeException("Description of " + keyword + " cannot be built");
			md = ArtefactRegistry.getArtefact(keyword, superDesc);
			superDesc.error("Unknown statement " + keyword, IGamlIssue.UNKNOWN_KEYWORD, source.getElement(), keyword);
			return null;
		}
		Iterable<IDescription> children = cp;
		if (children == null) {
			final List<IDescription> childrenList = new ArrayList<>();
			final SyntacticVisitor visitor = element -> {
				final IDescription desc = create(element, superDesc, null);
				if (desc != null) { childrenList.add(desc); }

			};
			source.visitChildren(visitor);
			source.visitGrids(visitor);
			source.visitSpecies(visitor);
			source.visitExperiments(visitor);
			children = childrenList;
		}
		final Facets facets = source.copyFacets(md);
		final EObject element = source.getElement();
		return getFactory(md.getKind()).buildDescription(keyword, facets, element, children, superDesc, md);

	}

}