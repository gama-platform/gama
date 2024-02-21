/*******************************************************************************************************
 *
 * GamlRuntimeModule.java, in gaml.compiler.gaml, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler;

import org.eclipse.xtext.linking.ILinkingDiagnosticMessageProvider;
import org.eclipse.xtext.linking.ILinkingService;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.parser.IEncodingProvider;
import org.eclipse.xtext.parser.antlr.ISyntaxErrorMessageProvider;
import org.eclipse.xtext.resource.IDefaultResourceDescriptionStrategy;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.service.DispatchingProvider;
import org.eclipse.xtext.service.SingletonBinding;
import org.eclipse.xtext.validation.IResourceValidator;

import com.google.inject.Binder;

import gama.dev.DEBUG;
import gama.gaml.expressions.IExpressionCompiler;
import gaml.compiler.gaml.expression.GamlExpressionCompiler;
import gaml.compiler.gaml.linking.GamlLinkingErrorMessageProvider;
import gaml.compiler.gaml.linking.GamlLinkingService;
import gaml.compiler.gaml.naming.GamlNameConverter;
import gaml.compiler.gaml.naming.GamlQualifiedNameProvider;
import gaml.compiler.gaml.parsing.GamlSyntaxErrorMessageProvider;
import gaml.compiler.gaml.resource.GamlEncodingProvider;
import gaml.compiler.gaml.resource.GamlResource;
import gaml.compiler.gaml.resource.GamlResourceDescriptionManager;
import gaml.compiler.gaml.resource.GamlResourceDescriptionStrategy;
import gaml.compiler.gaml.validation.ErrorToDiagnoticTranslator;
import gaml.compiler.gaml.validation.GamlResourceValidator;

/**
 * Use this class to register components to be used at runtime / without the Equinox extension registry.
 */
public class GamlRuntimeModule extends gaml.compiler.AbstractGamlRuntimeModule {

	static {
		DEBUG.OFF();
	}

	/** The initialized. */
	// private static boolean initialized;

	// Disabled for the moment
	// public static Pref<Boolean> ENABLE_FAST_COMPIL = GamaPreferences
	//// .create("pref_optimize_fast_compilation",
	//// "Enable faster validation (but less accurate error reporting in nagivator)", false, IType.BOOL)
	//// .in(GamaPreferences.Modeling.NAME, GamaPreferences.Modeling.OPTIONS).hidden();

	/**
	 * Static initialize.
	 */
	// public static void staticInitialize() {

	// if (!initialized) {
	// GamlExpressionFactory.registerParserProvider(GamlExpressionCompiler::new);
	// GAML.registerInfoProvider(GamlResourceInfoProvider.INSTANCE);
	// GAML.registerGamlEcoreUtils(EGaml.getInstance());
	// initialized = true;
	//
	// }

	// }

	@Override
	public void configure(final Binder binder) {
		DEBUG.OUT("Initialization of GAML XText runtime module begins");
		super.configure(binder);
		// staticInitialize();
		// binder.bind(ExpressionDescriptionBuilder.class);
		// binder.bind(IDocManager.class).to(GamlResourceDocumenter.class);
		// binder.bind(GamlSyntacticConverter.class);
		binder.bind(IDefaultResourceDescriptionStrategy.class).to(GamlResourceDescriptionStrategy.class);
		binder.bind(IQualifiedNameConverter.class).to(GamlNameConverter.class);
		// binder.bind(IResourceDescription.Manager.class).to(GamlResourceDescriptionManager.class);
		// binder.bind(IOutputConfigurationProvider.class).to(GamlOutputConfigurationProvider.class);
		binder.bind(IResourceValidator.class).to(GamlResourceValidator.class);
		binder.bind(ErrorToDiagnoticTranslator.class);
		// binder.bind(org.eclipse.xtext.scoping.IGlobalScopeProvider.class)
		// .toInstance(new gaml.compiler.gaml.scoping.BuiltinGlobalScopeProvider());
		DEBUG.OUT("Initialization of GAML XText runtime module finished");
	}

	@Override
	public Class<? extends IQualifiedNameProvider> bindIQualifiedNameProvider() {
		return GamlQualifiedNameProvider.class;
	}

	/**
	 * Bind I gaml expression compiler.
	 *
	 * @return the class<? extends I expression compiler>
	 */
	@SuppressWarnings ("rawtypes")
	public Class<? extends IExpressionCompiler> bindIGamlExpressionCompiler() {
		return GamlExpressionCompiler.class;
	}

	@Override
	@SingletonBinding ()
	public Class<? extends org.eclipse.xtext.scoping.IGlobalScopeProvider> bindIGlobalScopeProvider() {
		// return null;
		return gaml.compiler.gaml.scoping.BuiltinGlobalScopeProvider.class;
	}

	/**
	 * Bind I syntax error message provider.
	 *
	 * @return the class<? extends I syntax error message provider>
	 */
	public Class<? extends ISyntaxErrorMessageProvider> bindISyntaxErrorMessageProvider() {
		return GamlSyntaxErrorMessageProvider.class;
	}

	@Override
	public Class<? extends ILinkingService> bindILinkingService() {
		return GamlLinkingService.class;
	}

	/**
	 * Bind I linking diagnostic message provider.
	 *
	 * @return the class<? extends I linking diagnostic message provider. extended>
	 */
	public Class<? extends ILinkingDiagnosticMessageProvider.Extended> bindILinkingDiagnosticMessageProvider() {
		return GamlLinkingErrorMessageProvider.class;
	}

	@Override
	public Class<? extends XtextResource> bindXtextResource() {
		return GamlResource.class;
	}

	// @Override
	// public Class<? extends IParser> bindIParser() {
	// return GamlSyntacticParser.class;
	// }

	@Override
	public void configureRuntimeEncodingProvider(final Binder binder) {
		binder.bind(IEncodingProvider.class).annotatedWith(DispatchingProvider.Runtime.class)
				.to(GamlEncodingProvider.class);
	}

	// contributed by
	// org.eclipse.xtext.generator.builder.BuilderIntegrationFragment
	@Override
	public Class<? extends org.eclipse.xtext.resource.IContainer.Manager> bindIContainer$Manager() {
		return org.eclipse.xtext.resource.containers.StateBasedContainerManager.class;
	}

	// contributed by
	// org.eclipse.xtext.generator.builder.BuilderIntegrationFragment
	@Override
	public Class<? extends org.eclipse.xtext.resource.containers.IAllContainersState.Provider>
			bindIAllContainersState$Provider() {
		return org.eclipse.xtext.resource.containers.ResourceSetBasedAllContainersStateProvider.class;
	}

	// contributed by
	// org.eclipse.xtext.generator.builder.BuilderIntegrationFragment
	@Override
	public void configureIResourceDescriptions(final com.google.inject.Binder binder) {
		binder.bind(org.eclipse.xtext.resource.IResourceDescriptions.class)
				.to(org.eclipse.xtext.resource.impl.ResourceSetBasedResourceDescriptions.class);
	}

	/**
	 * Bind I resource description$ manager.
	 *
	 * @return the class<? extends I resource description. manager>
	 */
	public Class<? extends IResourceDescription.Manager> bindIResourceDescription$Manager() {
		return GamlResourceDescriptionManager.class;
	}

	// contributed by
	// org.eclipse.xtext.generator.builder.BuilderIntegrationFragment
	@Override
	public void configureIResourceDescriptionsPersisted(final com.google.inject.Binder binder) {
		binder.bind(org.eclipse.xtext.resource.IResourceDescriptions.class)
				.annotatedWith(com.google.inject.name.Names
						.named(org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider.PERSISTED_DESCRIPTIONS))
				.to(org.eclipse.xtext.resource.impl.ResourceSetBasedResourceDescriptions.class);
	}

	// contributed by org.eclipse.xtext.generator.formatting.FormatterFragment
	@Override
	public Class<? extends org.eclipse.xtext.formatting.IFormatter> bindIFormatter() {
		return gaml.compiler.gaml.formatting.GamlFormatter.class;
	}
}
