/*******************************************************************************************************
 *
 * GamlStandaloneSetup.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package gaml.compiler;

import org.eclipse.xtext.scoping.IGlobalScopeProvider;

import com.google.inject.Injector;

import gaml.compiler.scoping.BuiltinGlobalScopeProvider;

/**
 * Initialization support for running Xtext languages without the Equinox extension registry.
 *
 * <p>
 * In addition to the standard Xtext stand-alone setup (see {@link #doSetup()}), this class provides
 * {@link #initializeAfterPlatformReady(Injector)}, which must be called <em>after</em>
 * {@code GamaBundleLoader.buildContributions()} has finished (i.e. after {@code CoreActivator.load()} in the headless
 * entry point). It eagerly instantiates Xtext singletons whose constructors query the fully-loaded GAMA platform —
 * most notably {@link gaml.compiler.scoping.BuiltinGlobalScopeProvider} — so that model validation never starts with
 * an empty or partially-populated type/skill/action registry.
 * </p>
 *
 * <p>
 * This mirrors what the GUI's {@code EditorActivator} achieves implicitly: in the GUI, {@code getInjector()} is only
 * called when the first GAML editor opens, long after all bundle activators have run and the metamodel is complete. In
 * the headless case there is no such natural delay, so this explicit initialisation step fills the gap.
 * </p>
 */
public class GamlStandaloneSetup extends GamlStandaloneSetupGenerated {

	/**
	 * Creates the Guice injector, registers EMF resource factories and Xtext service providers for the {@code .gaml}
	 * and {@code .experiment} extensions, and returns the injector.
	 *
	 * <p>
	 * <strong>Note for headless callers:</strong> call {@link #initializeAfterPlatformReady(Injector)} with the
	 * returned injector once {@code GamaBundleLoader.buildContributions()} has completed.
	 * </p>
	 *
	 * @return the Guice injector for the GAML language
	 */
	public static Injector doSetup() {
		return new GamlStandaloneSetupGenerated().createInjectorAndDoEMFRegistration();
	}

	/**
	 * Eagerly instantiates Xtext singletons that depend on the fully-loaded GAMA platform.
	 *
	 * <p>
	 * This method must be called <em>after</em> {@code GamaBundleLoader.buildContributions()} has fully completed
	 * (i.e. after {@code CoreActivator.load()} in the headless entry point). It forces Guice to create every Xtext
	 * singleton whose constructor queries metamodel data ({@link org.eclipse.xtext.scoping.IGlobalScopeProvider} /
	 * {@link gaml.compiler.scoping.BuiltinGlobalScopeProvider} being the primary one), so that model validation never
	 * starts before the type hierarchy, the metamodel and the skill/action registries are complete.
	 * </p>
	 *
	 * <p>
	 * Add further {@code injector.getInstance(...)} calls here whenever a new Xtext singleton is introduced that reads
	 * from the GAMA metamodel at construction time.
	 * </p>
	 *
	 * @param injector
	 *            the injector returned by {@link #doSetup()}
	 */
	public static void initializeAfterPlatformReady(final Injector injector) {
		// BuiltinGlobalScopeProvider is created eagerly by Guice during doSetup() — before
		// CoreActivator runs — because GamlValidator (@SingletonBinding eager=true) injects
		// IScopeProvider (GamlScopeProvider extends AbstractDeclarativeScopeProvider) which
		// itself has @Inject IGlobalScopeProvider.  The constructor therefore intentionally does
		// no GAMA-platform work.  We call initialize() here, now that buildContributions() has
		// completed and Types / GamaMetaModel / GamaSkillRegistry are fully populated.
		((BuiltinGlobalScopeProvider) injector.getInstance(IGlobalScopeProvider.class)).initialize();
	}
}
