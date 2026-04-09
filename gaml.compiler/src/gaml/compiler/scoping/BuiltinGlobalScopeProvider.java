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
package gaml.compiler.scoping;

import static com.google.common.collect.Iterables.addAll;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
import gama.api.gaml.GAML;
import gama.api.gaml.types.Types;
import gama.api.kernel.GamaMetaModel;
import gama.dev.BANNER_CATEGORY;
import gama.dev.DEBUG;
import gaml.compiler.EGaml;
import gaml.compiler.gaml.GamlDefinition;
import gaml.compiler.gaml.GamlPackage;
import gaml.compiler.indexer.GamlResourceIndexer;
import gaml.compiler.resource.GamlResource;

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

		/** The elements - using HashMap for better performance. */
		final Map<QualifiedName, IEObjectDescription> elements = new HashMap<>();

		/** Cache for URI to description lookups to avoid repeated iterations. */
		private final Map<URI, IEObjectDescription> uriCache = new HashMap<>();

		/** Cached immutable view of elements values. */
		private Collection<IEObjectDescription> cachedValues;

		/**
		 * Instantiates a new e class based scope.
		 *
		 * @param uri
		 *            the uri
		 */
		public EClassBasedScope(final String uri) {
			final URI resourceUri = URI.createURI(uri, false);
			Resource r = rs.getResource(resourceUri, false);
			if (r == null) { r = rs.createResource(resourceUri); }
			resource = r;
		}

		@Override
		public IEObjectDescription getSingleElement(final QualifiedName name) {
			return elements.get(name);
		}

		@Override
		public Iterable<IEObjectDescription> getAllElements() {
			// Return cached immutable collection to avoid creating new collection on each call
			if (cachedValues == null) { cachedValues = Collections.unmodifiableCollection(elements.values()); }
			return cachedValues;
		}

		@Override
		public Iterable<IEObjectDescription> getElements(final QualifiedName name) {
			final IEObjectDescription result = elements.get(name);
			if (result == null) return Collections.emptyList();
			return Collections.singleton(result);
		}

		@Override
		public IEObjectDescription getSingleElement(final EObject object) {
			// Use cached URI lookup for better performance
			final URI uri = EcoreUtil2.getPlatformResourceOrNormalizedURI(object);
			IEObjectDescription cached = uriCache.get(uri);
			if (cached != null && (cached.getEObjectOrProxy() == object || uri.equals(cached.getEObjectURI())))
				return cached;

			// Fall back to iteration if not in cache
			for (IEObjectDescription input : elements.values()) {
				if (input.getEObjectOrProxy() == object || uri.equals(input.getEObjectURI())) {
					uriCache.put(uri, input);
					return input;
				}
			}
			return null;
		}

		@Override
		public List<IEObjectDescription> getElements(final EObject object) {
			final IEObjectDescription result = getSingleElement(object);
			return result != null ? Collections.singletonList(result) : Collections.emptyList();
		}

		/**
		 * Adds the.
		 *
		 * @param stub
		 *            the stub
		 */
		public void add(final QualifiedName name, final GamlDefinition stub) {
			resource.getContents().add(stub);
			final IEObjectDescription desc = EObjectDescription.create(name, stub);
			elements.put(name, desc);
			// Pre-populate URI cache
			uriCache.put(EcoreUtil2.getPlatformResourceOrNormalizedURI(stub), desc);
			// Invalidate cached values since we're adding
			cachedValues = null;
		}

	}

	/** The global scopes. */
	private final Map<EClass, EClassBasedScope> scopes = new HashMap<>();

	/** The all names. */
	private final Set<QualifiedName> allQualifiedNames = new HashSet<>();

	/** The e equation. */
	private final EClass eType, eVar, eSkill, eAction, eUnit, eEquation;

	/** The rs. */
	private final XtextResourceSet rs = new XtextResourceSet();

	/**
	 * Guards {@link #initialize()} so it is executed at most once, regardless of which code path triggers it first
	 * (explicit call from {@link gaml.compiler.GamlStandaloneSetup#initializeAfterPlatformReady} in headless mode, or
	 * lazy call from {@link #getScope} / {@link #contains} in the GUI).
	 */
	private volatile boolean initialized = false;

	static {
		DEBUG.OFF();
	}

	/**
	 * Creates the descriptions.
	 * <p>
	 * The constructor only sets up the {@link EClass} references and allocates the empty per-EClass scopes. All
	 * GAMA-platform-dependent work (populating types, constants, units, fields, variables, skills, actions and
	 * operators) is intentionally deferred to {@link #initialize()}, which must be called once
	 * {@code GamaBundleLoader.buildContributions()} has completed (i.e. after {@code CoreActivator} has run).
	 * </p>
	 * <p>
	 * The split is necessary because Xtext creates this singleton <em>eagerly</em> — via the {@code GamlValidator}
	 * eager-singleton chain — during {@code GamlStandaloneSetup.doSetup()}, well before the GAMA metamodel, type
	 * hierarchy and skill registry are ready.
	 * </p>
	 */
	public BuiltinGlobalScopeProvider() {
		eType = GamlPackage.eINSTANCE.getTypeDefinition();
		eVar = GamlPackage.eINSTANCE.getVarDefinition();
		eSkill = GamlPackage.eINSTANCE.getSkillFakeDefinition();
		eAction = GamlPackage.eINSTANCE.getActionDefinition();
		eUnit = GamlPackage.eINSTANCE.getUnitFakeDefinition();
		eEquation = GamlPackage.eINSTANCE.getEquationDefinition();
		scopes.put(eType, new EClassBasedScope("types.xmi"));
		scopes.put(eVar, new EClassBasedScope("vars.xmi"));
		scopes.put(eSkill, new EClassBasedScope("skills.xmi"));
		scopes.put(eAction, new EClassBasedScope("units.xmi"));
		scopes.put(eUnit, new EClassBasedScope("actions.xmi"));
		scopes.put(eEquation, new EClassBasedScope("equations.xmi"));
	}

	/**
	 * Populates the built-in scope with all GAMA platform contributions.
	 *
	 * <p>
	 * This method is <em>idempotent</em>: the first call performs the full initialisation; subsequent calls return
	 * immediately. It is safe to call from any thread.
	 * </p>
	 *
	 * <p>
	 * Two code paths call this method:
	 * </p>
	 * <ol>
	 * <li><strong>Headless</strong> — called explicitly by
	 * {@link gaml.compiler.GamlStandaloneSetup#initializeAfterPlatformReady(com.google.inject.Injector)} once
	 * {@code CoreActivator.load()} has triggered {@code GamaBundleLoader.buildContributions()}. This is the preferred
	 * path: it ensures the scope is fully populated before any model validation begins.</li>
	 * <li><strong>GUI</strong> — called lazily on the first invocation of {@link #getScope} or {@link #contains}, via
	 * the internal {@link #ensureInitialized()} guard. By the time any GAML editor opens, the GAMA core bundle is
	 * already fully active, so the platform is always ready at this point.</li>
	 * </ol>
	 */
	public synchronized void initialize() {
		if (initialized) return;
		initialized = true;
		DEBUG.TIMER(BANNER_CATEGORY.GAML, "Language artifacts", "built in", () -> {
			add(IExpressionFactory.TEMPORARY_ACTION_NAME, eAction);
			Types.getTypeNames().forEach(t -> add(t, eType, eVar, eAction));

			GAML.getConstants().forEach(t -> {
				try {
					add(t, eType, eVar);
				} catch (Exception ex) {
					GamaBundleLoader.ERROR("Error when building constant artefact " + t, ex);
				}
			});
			GAML.getUnits().forEach((t, u) -> {
				try {
					DEBUG.LOG("Adding unit artefact: " + t);
					add(t, eUnit);
				} catch (Exception ex) {
					GamaBundleLoader.ERROR("Error when building unit artefact " + t, ex);
				}
			});
			GAML.getFields().forEach(a -> {
				try {
					add(a.getName(), eVar);
				} catch (Exception e) {
					GamaBundleLoader.ERROR("Error when building field artefact " + a, e);
				}
			});
			getAllVars().forEach(t -> {
				try {
					add(t.getName(), eVar);
				} catch (Exception ex) {
					GamaBundleLoader.ERROR("Error when building var artefact " + t.getName(), ex);
				}
			});
			GamaSkillRegistry.INSTANCE.getAllSkillNames().forEach(t -> {
				try {
					add(t, eSkill, eVar);
				} catch (Exception ex) {
					GamaBundleLoader.ERROR("Error when building skill artefact " + t, ex);
				}
			});
			getAllActions().forEach(t -> {
				try {
					add(t.getName(), eAction, eVar);
				} catch (Exception ex) {
					GamaBundleLoader.ERROR("Error when building action artefact " + t.getName(), ex);
				}
			});
			GAML.getOperatorsNames().forEach(a -> {
				try {
					add(a, eAction);
				} catch (Exception ex) {
					GamaBundleLoader.ERROR("Error when building action artefact " + a, ex);
				}
			});
		});
	}

	/**
	 * Ensures {@link #initialize()} has been called. Used as a lazy safety net inside {@link #getScope} and
	 * {@link #contains} for the GUI code path, where no explicit initialisation call is made.
	 */
	private void ensureInitialized() {
		if (!initialized) { initialize(); }
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

		for (final ITypeDescription s : GamaMetaModel.getAllSpeciesDescriptions()) { s.visitOwnActions(visitor); }
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

		for (final ITypeDescription desc : GamaMetaModel.getAllSpeciesDescriptions()) {
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
		ensureInitialized();
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
		final QualifiedName qName = QualifiedName.create(t);
		allQualifiedNames.add(qName);
		final EGaml eGaml = EGaml.getInstance();
		for (final EClass eClass : classes) { scopes.get(eClass).add(qName, eGaml.createGamlDefinition(t, eClass)); }
	}

	@Override
	protected IScope getScope(final Resource resource, final boolean ignoreCase, final EClass type,
			final Predicate<IEObjectDescription> filter) {
		ensureInitialized();
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
