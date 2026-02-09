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
import gama.api.additions.registries.ArtefactProtoRegistry;
import gama.api.compilation.ast.ISyntacticElement;
import gama.api.compilation.ast.ISyntacticElement.SyntacticVisitor;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionFactory;
import gama.api.compilation.descriptions.IExperimentDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.descriptions.ISkillDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.ISpeciesDescription.Platform;
import gama.api.compilation.factories.ISymbolDescriptionFactory;
import gama.api.compilation.factories.ISymbolDescriptionFactory.Species;
import gama.api.compilation.prototypes.IArtefactProto;
import gama.api.constants.IGamlIssue;
import gama.api.gaml.GAML;
import gama.api.gaml.symbols.Facets;
import gama.api.kernel.agent.IAgentConstructor;
import gama.dev.DEBUG;
import gaml.compiler.gaml.ast.SyntacticFactory;
import gaml.compiler.gaml.factories.ModelFactory;
import gaml.compiler.gaml.factories.PlatformFactory;

/**
 * Written by drogoul Modified on 7 janv. 2011
 *
 * @todo Description
 *
 */

/**
 * A factory for creating Description objects.
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
	private ISymbolDescriptionFactory getFactory(final int kind) {
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
		final IArtefactProto.Symbol p = ArtefactProtoRegistry.getProto(keyword, null);
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
	@Override
	public synchronized IDescription create(final ISymbolDescriptionFactory factory, final String keyword,
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
	public synchronized IDescription create(final String keyword, final IDescription superDesc,
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
	public synchronized IDescription create(final String keyword, final IDescription superDesc,
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
	public synchronized IDescription create(final String keyword, final IDescription superDesc,
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
	public synchronized IDescription create(final String keyword, final IDescription superDescription,
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
	public synchronized IDescription create(final String keyword, final String... facets) {
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
		IArtefactProto.Symbol md = ArtefactProtoRegistry.getProto(keyword, superDesc);
		if (md == null) {
			if (superDesc == null) throw new RuntimeException("Description of " + keyword + " cannot be built");
			md = ArtefactProtoRegistry.getProto(keyword, superDesc);
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
