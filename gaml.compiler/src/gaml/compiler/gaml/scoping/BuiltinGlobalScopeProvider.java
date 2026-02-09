/*******************************************************************************************************
 *
 * BuiltinGlobalScopeProvider.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
// (c) Vincent Simonet, 2011
package gaml.compiler.gaml.scoping;

import static com.google.common.collect.Iterables.addAll;
import static gama.api.gaml.types.Types.getBuiltInSpecies;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.impl.ImportUriGlobalScopeProvider;
import org.eclipse.xtext.scoping.impl.SelectableBasedScope;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.google.inject.Singleton;

import gama.api.additions.GamaBundleLoader;
import gama.api.additions.registries.GamaSkillRegistry;
import gama.api.compilation.descriptions.IActionDescription;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescription.DescriptionVisitor;
import gama.api.compilation.descriptions.ISkillDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.factories.IExpressionFactory;
import gama.api.data.factories.GamaMapFactory;
import gama.api.data.objects.IMap;
import gama.api.gaml.GAML;
import gama.api.gaml.types.Types;
import gama.dev.BANNER_CATEGORY;
import gama.dev.DEBUG;
import gaml.compiler.gaml.EGaml;
import gaml.compiler.gaml.GamlDefinition;
import gaml.compiler.gaml.GamlPackage;
import gaml.compiler.gaml.indexer.GamlResourceIndexer;
import gaml.compiler.gaml.resource.GamlResource;

/**
 * Global GAML scope provider supporting built-in definitions.
 * <p>
 * This global provider generates a global scope which consists in:
 * </p>
 * <ul>
 * <li>Built-in definitions which are defined in the diffents plug-in bundles providing contributions to GAML,</li>
 * <li>A global scope, which is computed by a ImportURI global scope provider.</li>
 * </ul>
 *
 * @author Vincent Simonet, adapted for GAML by Alexis Drogoul, 2012
 */

/**
 * The Class BuiltinGlobalScopeProvider.
 */
@Singleton
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class BuiltinGlobalScopeProvider extends ImportUriGlobalScopeProvider {

	/**
	 * The Class EClassBasedScope.
	 */
	class EClassBasedScope implements IScope {

		/** The resource. */
		final Resource resource;

		/** The elements. */
		final IMap<QualifiedName, IEObjectDescription> elements = GamaMapFactory.createUnordered();

		/**
		 * Instantiates a new e class based scope.
		 *
		 * @param uri
		 *            the uri
		 */
		public EClassBasedScope(final String uri) {
			Resource r = rs.getResource(URI.createURI(uri, false), false);
			if (r == null) { r = rs.createResource(URI.createURI(uri, false)); }
			resource = r;
		}

		@Override
		public IEObjectDescription getSingleElement(final QualifiedName name) {
			return elements.get(name);
		}

		@Override
		public Iterable<IEObjectDescription> getAllElements() { return elements.values(); }

		@Override
		public Iterable<IEObjectDescription> getElements(final QualifiedName name) {
			final IEObjectDescription result = elements.get(name);
			if (result == null) return Collections.emptyList();
			return Collections.singleton(result);
		}

		@Override
		public IEObjectDescription getSingleElement(final EObject object) {
			final URI uri = EcoreUtil2.getPlatformResourceOrNormalizedURI(object);
			for (Map.Entry<QualifiedName, IEObjectDescription> entry : elements.entrySet()) {
				IEObjectDescription input = entry.getValue();
				if (input.getEObjectOrProxy() == object || uri.equals(input.getEObjectURI())) return input;
			}
			return null;
		}

		@Override
		public List<IEObjectDescription> getElements(final EObject object) {
			final URI uri = EcoreUtil2.getPlatformResourceOrNormalizedURI(object);
			for (Map.Entry<QualifiedName, IEObjectDescription> entry : elements.entrySet()) {
				IEObjectDescription input = entry.getValue();
				if (input.getEObjectOrProxy() == object || uri.equals(input.getEObjectURI()))
					return Collections.singletonList(input);
			}
			return Collections.EMPTY_LIST;
		}

		/**
		 * Adds the.
		 *
		 * @param stub
		 *            the stub
		 */
		public void add(final QualifiedName name, final GamlDefinition stub) {
			resource.getContents().add(stub);
			elements.put(name, EObjectDescription.create(name, stub));
		}

	}

	/** The global scopes. */
	private final IMap<EClass, EClassBasedScope> scopes = GamaMapFactory.createUnordered();

	/** The all names. */
	private final Set<QualifiedName> allQualifiedNames = new HashSet<>();

	/** The e equation. */
	private final EClass eType, eVar, eSkill, eAction, eUnit, eEquation;

	/** The rs. */
	private final XtextResourceSet rs = new XtextResourceSet();

	static {
		DEBUG.OFF();
	}

	/**
	 * Creates the descriptions.
	 */
	public BuiltinGlobalScopeProvider() {
		eType = GamlPackage.eINSTANCE.getTypeDefinition();
		eVar = GamlPackage.eINSTANCE.getVarDefinition();
		eSkill = GamlPackage.eINSTANCE.getSkillFakeDefinition();
		eAction = GamlPackage.eINSTANCE.getActionDefinition();
		eUnit = GamlPackage.eINSTANCE.getUnitFakeDefinition();
		eEquation = GamlPackage.eINSTANCE.getEquationDefinition();
		DEBUG.TIMER(BANNER_CATEGORY.GAML, "Language artifacts", "built in", () -> {
			scopes.put(eType, new EClassBasedScope("types.xmi"));
			scopes.put(eVar, new EClassBasedScope("vars.xmi"));
			scopes.put(eSkill, new EClassBasedScope("skills.xmi"));
			scopes.put(eAction, new EClassBasedScope("units.xmi"));
			scopes.put(eUnit, new EClassBasedScope("actions.xmi"));
			scopes.put(eEquation, new EClassBasedScope("equations.xmi"));
			add(IExpressionFactory.TEMPORARY_ACTION_NAME, eAction);
			Types.getTypeNames().forEach(t -> add(t, eType, eVar, eAction));

			GAML.CONSTANTS.forEach(t -> {
				try {
					add(t, eType, eVar);
				} catch (Exception ex) {
					GamaBundleLoader.ERROR("Error when bulding constant artefact " + t, ex);
				}
			});
			GAML.UNITS.forEach((t, u) -> {
				try {
					add(t, eUnit);
				} catch (Exception ex) {
					GamaBundleLoader.ERROR("Error when bulding unit artefact " + t, ex);
				}
			});
			GAML.FIELDS.values().forEach(t -> {
				try {
					add(t.getName(), eVar);
				} catch (Exception ex) {
					GamaBundleLoader.ERROR("Error when bulding field artefact " + t, ex);
				}
			});

			getAllVars().forEach(t -> {
				try {
					add(t.getName(), eVar);
				} catch (Exception ex) {
					GamaBundleLoader.ERROR("Error when bulding var artefact " + t.getName(), ex);
				}
			});
			GamaSkillRegistry.INSTANCE.getAllSkillNames().forEach(t -> {
				try {
					add(t, eSkill, eVar);
				} catch (Exception ex) {
					GamaBundleLoader.ERROR("Error when bulding skill artefact " + t, ex);
				}
			});
			getAllActions().forEach(t -> {
				try {
					add(t.getName(), eAction, eVar);
				} catch (Exception ex) {
					GamaBundleLoader.ERROR("Error when bulding action artefact " + t.getName(), ex);
				}
			});
			GAML.OPERATORS.forEach((a, b) -> {
				try {
					add(a, eAction);
				} catch (Exception ex) {
					GamaBundleLoader.ERROR("Error when bulding action artefact " + a, ex);
				}
			});
		});
	}

	/**
	 * Gets the all actions.
	 *
	 * @return the all actions
	 */
	public static Collection<IDescription> getAllActions() {
		SetMultimap<String, IDescription> result = MultimapBuilder.hashKeys().linkedHashSetValues().build();

		final DescriptionVisitor<IDescription> visitor = desc -> {
			result.put(desc.getName(), desc);
			return true;
		};

		for (final ITypeDescription s : getBuiltInSpecies().values()) { s.visitOwnActions(visitor); }
		GamaSkillRegistry.INSTANCE.visitSkills(desc -> {
			((ISkillDescription) desc).visitOwnActions(visitor);
			return true;
		});
		return result.values();
	}

	/**
	 * Gets the all vars.
	 *
	 * @return the all vars
	 */
	private static Collection<IDescription> getAllVars() {
		final HashSet<IDescription> result = new HashSet<>();

		final DescriptionVisitor<IDescription> varVisitor = desc -> {
			result.add(desc);
			return true;
		};

		final DescriptionVisitor<IDescription> actionVisitor = desc -> {
			addAll(result, ((IActionDescription) desc).getFormalArgs());
			return true;
		};

		for (final ITypeDescription desc : Types.getBuiltInSpecies().values()) {
			desc.visitOwnAttributes(varVisitor);
			desc.visitOwnActions(actionVisitor);

		}
		GamaSkillRegistry.INSTANCE.visitSkills(desc -> {
			((ITypeDescription) desc).visitOwnAttributes(varVisitor);
			((ITypeDescription) desc).visitOwnActions(actionVisitor);
			return true;
		});

		return result;
	}

	/**
	 * Contains.
	 *
	 * @param name
	 *            the name
	 * @return true, if successful
	 */
	public boolean contains(final QualifiedName name) {
		return allQualifiedNames.contains(name);
	}

	/**
	 * Adds the var.
	 *
	 * @param t
	 *            the t
	 * @param o
	 *
	 * @return the last stub constructed
	 */
	void add(final String t, final EClass... classes) {
		QualifiedName qName = QualifiedName.create(t);
		allQualifiedNames.add(qName);
		for (EClass eClass : classes) {
			scopes.get(eClass).add(qName, EGaml.getInstance().createGamlDefinition(t, eClass));
		}
	}

	@Override
	protected IScope getScope(final Resource resource, final boolean ignoreCase, final EClass type,
			final Predicate<IEObjectDescription> filter) {
		IScope scope = scopes.get(type);
		Collection<URI> imports = GamlResourceIndexer.allImportsOf((GamlResource) resource).keySet();
		int size = imports.size();
		if (size == 0) return scope;
		if (size > 1) {
			imports = Lists.newArrayList(imports);
			Collections.reverse((List<URI>) imports);
		}
		final IResourceDescriptions descriptions = getResourceDescriptions(resource, imports);
		return SelectableBasedScope.createScope(scope, descriptions, filter, type, false);
	}

}
