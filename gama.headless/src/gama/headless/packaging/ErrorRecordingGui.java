/*******************************************************************************************************
 *
 * ErrorRecordingGui.java, in gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.packaging;

import java.util.function.Consumer;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.ui.NullGuiHandler;

/**
 * The headless GUI handler of an archive run: it behaves like {@link NullGuiHandler}, and also passes the runtime
 * errors (not the warnings) of the simulations to a listener, as they are reported on simulation threads instead of
 * being thrown to the runner.
 */
final class ErrorRecordingGui extends NullGuiHandler {

	/** Receives the runtime errors. */
	private final Consumer<GamaRuntimeException> listener;

	/** The number of errors reported. */
	private volatile int errors;

	ErrorRecordingGui(final Consumer<GamaRuntimeException> listener) {
		this.listener = listener;
	}

	@Override
	public void runtimeError(final IScope scope, final GamaRuntimeException g) {
		super.runtimeError(scope, g);
		if (g == null || g.isWarning()) return;
		errors++;
		listener.accept(g);
	}

	/** The number of runtime errors reported. */
	int errors() {
		return errors;
	}
}
