/*******************************************************************************************************
 *
 * GamlActivator.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import gama.dev.BANNER_CATEGORY;
import gama.dev.DEBUG;
import gama.gaml.compilation.GAML;
import gama.gaml.expressions.GamlExpressionFactory;
import gaml.compiler.gaml.EGaml;
import gaml.compiler.gaml.expression.GamlExpressionCompiler;
import gaml.compiler.gaml.resource.GamlResourceInfoProvider;
import gaml.compiler.gaml.validation.GamlModelBuilder;
import gaml.compiler.gaml.validation.GamlTextValidator;

/**
 * The Class GamlActivator.
 */
public class GamlActivator implements BundleActivator {

	@Override
	public void start(final BundleContext context) throws Exception {
		// Spawns a new thread in order to escape the "activator/osgi" thread as soon as possible (see #3636) -- seems
		// not necessary anymore
		DEBUG.TIMER_WITH_EXCEPTIONS(BANNER_CATEGORY.GAML, "Initializing parser", "done in", () -> {
			GamlExpressionFactory.registerParserProvider(GamlExpressionCompiler::new);
			GAML.registerInfoProvider(GamlResourceInfoProvider.INSTANCE);
			GAML.registerGamlEcoreUtils(EGaml.getInstance());
			GAML.registerGamlModelBuilder(GamlModelBuilder.getDefaultInstance());
			GAML.registerGamlTextValidator(new GamlTextValidator());
		});

	}

	@Override
	public void stop(final BundleContext context) throws Exception {

	}

}
