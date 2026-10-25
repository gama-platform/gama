/*******************************************************************************************************
 *
 * ProcessorWarning.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.quickfix;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;

/**
 * A warning emitted by the GAML annotation processor (prefixed with "GAML: ") that can be fixed automatically.
 *
 * @param kind
 *            whether the element is undocumented or untested
 * @param displayedName
 *            the name of the element as displayed by the processor (e.g. "operator foo", "attribute bar")
 * @param nested
 *            for undocumented attributes, arguments and facets, the annotation (variable, arg or facet) that should
 *            hold the documentation; null otherwise
 * @param name
 *            the GAML name of the element (e.g. "foo" for "operator foo")
 */
public record ProcessorWarning(Kind kind, String displayedName, String nested, String name) {

	/** The kinds of fixable warnings. */
	public enum Kind {
		/** "documentation missing for ..." */
		UNDOCUMENTED,
		/** "operator '...' is not tested" */
		UNTESTED
	}

	private static final Pattern UNDOCUMENTED = Pattern.compile("documentation missing for (.*)");

	private static final Pattern UNTESTED = Pattern.compile("operator '(.*)' is not tested");

	/**
	 * Parses the message of a marker, returns null if it is not a fixable GAML processor warning located in a Java file.
	 */
	public static ProcessorWarning of(final IMarker marker) {
		if (marker == null || !marker.exists() || !(marker.getResource() instanceof IFile f)
				|| !"java".equals(f.getFileExtension())) {
			return null;
		}
		return parse(marker.getAttribute(IMarker.MESSAGE, ""));
	}

	/**
	 * Parses a processor message, returns null if it is not a fixable GAML processor warning.
	 */
	public static ProcessorWarning parse(final String message) {
		int i = message.indexOf("GAML: ");
		if (i < 0) { return null; }
		String msg = message.substring(i + 6).strip();
		Matcher t = UNTESTED.matcher(msg);
		if (t.matches()) { return new ProcessorWarning(Kind.UNTESTED, "operator " + t.group(1), null, t.group(1)); }
		Matcher d = UNDOCUMENTED.matcher(msg);
		if (!d.matches()) { return null; }
		String displayed = d.group(1);
		String nested = null;
		if (displayed.startsWith("attribute ")) {
			nested = "variable";
		} else if (displayed.startsWith("argument ")) {
			nested = "arg";
		} else if (displayed.startsWith("facet ")) { nested = "facet"; }
		int space = displayed.lastIndexOf(' ');
		return new ProcessorWarning(Kind.UNDOCUMENTED, displayed, nested,
				space >= 0 ? displayed.substring(space + 1) : displayed);
	}

}
