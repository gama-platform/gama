/*******************************************************************************************************
 *
 * GamlActivator.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler;

import static gama.dev.DEBUG.TIMER_WITH_EXCEPTIONS;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import gama.api.gaml.GAML;
import gama.dev.BANNER_CATEGORY;
import gama.dev.DEBUG;
import gaml.compiler.gaml.descriptions.DescriptionFactory;
import gaml.compiler.gaml.expression.GamlExpressionFactory;
import gaml.compiler.gaml.factories.ExperimentFactory;
import gaml.compiler.gaml.factories.ExpressionDescriptionFactory;
import gaml.compiler.gaml.factories.ModelFactory;
import gaml.compiler.gaml.factories.PlatformFactory;
import gaml.compiler.gaml.factories.SkillFactory;
import gaml.compiler.gaml.factories.SpeciesFactory;
import gaml.compiler.gaml.factories.StatementFactory;
import gaml.compiler.gaml.factories.VariableFactory;
import gaml.compiler.gaml.prototypes.ArtefactFactory;
import gaml.compiler.gaml.resource.GamlResourceServices;
import gaml.compiler.gaml.validation.GamlModelBuilder;
import gaml.compiler.gaml.validation.GamlTextValidator;

/**
 * The GAML Compiler bundle activator that initializes and registers all the core components required for GAML language
 * compilation and validation.
 *
 * <p>
 * This activator is responsible for bootstrapping the GAML compiler infrastructure when the bundle starts, including
 * registering various factories, parsers, and services that enable GAML model compilation, expression parsing, and
 * validation.
 * </p>
 *
 * <p>
 * The class implements the OSGi {@link BundleActivator} interface to participate in the bundle lifecycle and ensure
 * proper initialization of GAML compiler components.
 * </p>
 *
 * @author GAMA Development Team
 * @since 2.0
 */
public class GamlActivator implements BundleActivator {

	/**
	 * Static initialization block that disables debug output during class loading. This ensures that the GAML compiler
	 * initialization process doesn't generate unnecessary debug messages during bundle startup.
	 */
	static {
		DEBUG.OFF();
	}

	/**
	 * Starts the GAML compiler bundle and initializes all necessary components.
	 *
	 * <p>
	 * This method performs the following key initialization tasks:
	 * </p>
	 * <ul>
	 * <li>Registers artifact prototype factory for GAML artefacts</li>
	 * <li>Registers description factory for model element descriptions</li>
	 * <li>Registers symbol factories for experiments, models, platforms, species, statements, variables, and
	 * skills</li>
	 * <li>Configures expression parsing and compilation infrastructure</li>
	 * <li>Sets up GAML content providers and model builders</li>
	 * <li>Initializes text validation services</li>
	 * </ul>
	 *
	 * <p>
	 * All initialization is performed within a timed block for performance monitoring.
	 * </p>
	 *
	 * @param context
	 *            the bundle context provided by the OSGi framework
	 * @throws Exception
	 *             if any initialization step fails
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		DEBUG.OUT("Starting GAML Compiler bundle");

		TIMER_WITH_EXCEPTIONS(BANNER_CATEGORY.GAMA, "Initialization of " + context.getBundle().getSymbolicName(),
				"done in", () -> {
					// Register the stateless singleton expression compiler
					// GamlExpressionFactory.registerParser(GamlExpressionCompiler.getInstance());

					// Register factory for creating GAML artefact prototypes
					GAML.registerArtefactProtoFactory(ArtefactFactory.getInstance());

					// Register factory for creating model element descriptions
					GAML.registerDescriptionFactory(DescriptionFactory.getInstance());

					// Register symbol factories for different GAML language constructs
					GAML.registerSymbolFactory(ExperimentFactory.getInstance());
					GAML.registerSymbolFactory(ModelFactory.getInstance());
					GAML.registerSymbolFactory(PlatformFactory.getInstance());
					GAML.registerSymbolFactory(SpeciesFactory.getInstance());
					GAML.registerSymbolFactory(StatementFactory.getInstance());
					GAML.registerSymbolFactory(VariableFactory.getInstance());
					GAML.registerSymbolFactory(SkillFactory.getInstance());

					// Configure expression compilation and parsing infrastructure
					GAML.registerExpressionFactory(GamlExpressionFactory.getInstance());
					GAML.registerExpressionDescriptionFactory(ExpressionDescriptionFactory.getInstance());

					// Register GAML-specific services for content processing and validation
					GAML.registerGamlContentProvider(GamlResourceServices::getOrCreateSyntacticContents);
					GAML.registerGamlModelBuilder(GamlModelBuilder.getInstance());
					GAML.registerGamlTextValidator(GamlTextValidator.getInstance());

				});

	}

	/**
	 * Stops the GAML compiler bundle.
	 *
	 * <p>
	 * This method is called when the bundle is being stopped and provides an opportunity to perform cleanup operations.
	 * Currently, no specific cleanup is required for the GAML compiler bundle.
	 * </p>
	 *
	 * @param context
	 *            the bundle context provided by the OSGi framework
	 * @throws Exception
	 *             if any cleanup operation fails
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {}

}
