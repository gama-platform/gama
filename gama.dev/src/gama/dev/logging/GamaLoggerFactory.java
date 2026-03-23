/*******************************************************************************************************
 *
 * GamaLoggerFactory.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dev.logging;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.ILoggerFactory;

import gama.dev.DEBUG;

/**
 * An implementation of {@link ILoggerFactory} which always returns {@link GamaLogger} instances.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class GamaLoggerFactory implements ILoggerFactory {

	/** The logger map. */
	ConcurrentMap<String, GamaLogger> loggerMap = new ConcurrentHashMap<>();

	/**
	 * Return an appropriate {@link GamaLogger} instance by name.
	 *
	 */
	@Override
	public GamaLogger getLogger(final String name) {
		DEBUG.ON(name);
		return loggerMap.computeIfAbsent(name, GamaLogger::new);
	}

}
