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
import org.eclipse.xtext.parser.IParser;
import org.eclipse.xtext.parser.antlr.ISyntaxErrorMessageProvider;
import org.eclipse.xtext.resource.IDefaultResourceDescriptionStrategy;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.service.DispatchingProvider;
import org.eclipse.xtext.service.SingletonBinding;
import org.eclipse.xtext.validation.IResourceValidator;

import com.google.inject.Binder;

import gama.dev.DEBUG;
import gaml.compiler.linking.GamlLinkingErrorMessageProvider;
import gaml.compiler.linking.GamlLinkingService;
import gaml.compiler.naming.GamlNameConverter;
import gaml.compiler.naming.GamlQualifiedNameProvider;
import gaml.compiler.parsing.GamlSyntaxErrorMessageProvider;
import gaml.compiler.preprocessor.PreprocessingGamlParser;
import gaml.compiler.resource.GamlEncodingProvider;
import gaml.compiler.resource.GamlResource;
import gaml.compiler.resource.GamlResourceDescriptionManager;
import gaml.compiler.resource.GamlResourceDescriptionStrategy;
import gaml.compiler.validation.ErrorToDiagnoticTranslator;
import gaml.compiler.validation.GamlResourceValidator;

/**
 * Use this class to register components to be used at runtime / without the Equinox extension registry.
 */
public class GamlRuntimeModule extends gaml.compiler.AbstractGamlRuntimeModule {

	static {
		DEBUG.OFF();
	}

	@Override
	public void configure(final Binder binder) {
		super.configure(binder);
		binder.bind(IDefaultResourceDescriptionStrategy.class).to(GamlResourceDescriptionStrategy.class);
		binder.bind(IQualifiedNameConverter.class).to(GamlNameConverter.class);
		binder.bind(IResourceValidator.class).to(GamlResourceValidator.class);
		binder.bind(ErrorToDiagnoticTranslator.class);
	}

	@Override
	public Class<? extends IQualifiedNameProvider> bindIQualifiedNameProvider() {
		return GamlQualifiedNameProvider.class;
	}

	@Override
	@SingletonBinding ()
	public Class<? extends org.eclipse.xtext.scoping.IGlobalScopeProvider> bindIGlobalScopeProvider() {
		return gaml.compiler.scoping.BuiltinGlobalScopeProvider.class;
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

	@Override
	public Class<? extends IParser> bindIParser() {
		return PreprocessingGamlParser.class;
	}

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

}
