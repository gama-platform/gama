/*******************************************************************************************************
 *
 * RuntimeExceptionHandlerFactory.java, in gama.ui.shared.experiment, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.experiment.factories;

import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

import gama.core.common.interfaces.IRuntimeExceptionHandler;
import gama.ui.experiment.commands.RuntimeExceptionHandler;

/**
 * A factory for creating RuntimeExceptionHandler objects.
 */
public class RuntimeExceptionHandlerFactory extends AbstractServiceFactory {

	/** The handler. */
	IRuntimeExceptionHandler handler;

	/**
	 * Gets the handler.
	 *
	 * @return the handler
	 */
	public IRuntimeExceptionHandler getHandler() {
		if (handler == null) {
			handler = new RuntimeExceptionHandler();
		}
		return handler;
	}

	@Override
	public Object create(final Class serviceInterface, final IServiceLocator parentLocator,
			final IServiceLocator locator) {
		return getHandler();
	}

}
