/*******************************************************************************************************
 *
 * GamaProjectTester.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.util;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;

/** Tests whether a project is a GAMA plugin (property {@code gama.ui.devtools.gamaPlugin}). */
public class GamaProjectTester extends PropertyTester {

	@Override
	public boolean test(final Object receiver, final String property, final Object[] args,
			final Object expectedValue) {
		return receiver instanceof IProject project && GamaProjects.isGamaPlugin(project);
	}

}
