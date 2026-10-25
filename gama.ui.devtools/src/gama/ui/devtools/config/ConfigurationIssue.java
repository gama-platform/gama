/*******************************************************************************************************
 *
 * ConfigurationIssue.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.config;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;

/**
 * A configuration problem detected on a GAMA plugin.
 *
 * @param project
 *            the project concerned
 * @param kind
 *            the kind of problem
 * @param message
 *            a human-readable message
 */
public record ConfigurationIssue(IProject project, Kind kind, String message) {

	/**
	 * The kinds of problems, with their severity and whether they can be repaired automatically.
	 */
	public enum Kind {

		/** Annotation processing is disabled. */
		APT_DISABLED(true, IMarker.SEVERITY_ERROR),
		/** The generated source dir is not 'gaml'. */
		APT_GEN_DIR(true, IMarker.SEVERITY_ERROR),
		/** Processing during reconcile is enabled (slows down the editor, may produce partial additions). */
		APT_RECONCILE(true, IMarker.SEVERITY_WARNING),
		/** The .factorypath does not reference the processor jar. */
		FACTORY_PATH(true, IMarker.SEVERITY_ERROR),
		/** 'gaml' is not a source folder of the classpath. */
		CLASSPATH_GAML(true, IMarker.SEVERITY_ERROR),
		/** build.properties does not compile 'src/' and 'gaml/'. */
		BUILD_SOURCES(true, IMarker.SEVERITY_ERROR),
		/** plugin.xml does not declare the 'gaml.extension' extension. */
		PLUGIN_XML(true, IMarker.SEVERITY_ERROR),
		/** The bundle is not a singleton (required to contribute extensions). */
		SINGLETON(true, IMarker.SEVERITY_ERROR),
		/** 'models/' exists but is not exported in bin.includes. */
		MODELS_NOT_EXPORTED(true, IMarker.SEVERITY_WARNING),
		/** A jar of 'libs/' is not declared everywhere it should be. */
		LIB_NOT_DECLARED(true, IMarker.SEVERITY_ERROR),
		/** The project has no explicit encoding. */
		ENCODING(true, IMarker.SEVERITY_WARNING),
		/** The project name is not the Bundle-SymbolicName (the processor relies on it). */
		NAME_MISMATCH(false, IMarker.SEVERITY_ERROR),
		/** Two plugins share the same last segment, hence the same generated package. */
		DUPLICATE_SHORTCUT(false, IMarker.SEVERITY_ERROR),
		/** The processor project is not in the workspace. */
		PROCESSOR_MISSING(false, IMarker.SEVERITY_ERROR),
		/** The processor jar is older than the sources of the processor or of the annotations. */
		PROCESSOR_STALE(false, IMarker.SEVERITY_WARNING),
		/** The generated GamlAdditions class is missing. */
		ADDITIONS_MISSING(false, IMarker.SEVERITY_WARNING);

		/** Whether the "repair" command can fix it. */
		public final boolean fixable;

		/** The marker severity. */
		public final int severity;

		Kind(final boolean fixable, final int severity) {
			this.fixable = fixable;
			this.severity = severity;
		}
	}

	@Override
	public String toString() {
		return project.getName() + ": " + message;
	}

}
