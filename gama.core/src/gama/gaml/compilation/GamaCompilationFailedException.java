/*******************************************************************************************************
 *
 * GamaCompilationFailedException.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.compilation;

import java.util.List;

import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import gama.core.common.util.FileUtils;

/**
 * The Class GamaCompilationFailedException.
 */
public class GamaCompilationFailedException extends RuntimeException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1392647532622819498L;

	/** The error list. */
	public final List<GamlCompilationError> errorList;

	/**
	 * Instantiates a new gama compilation failed exception.
	 *
	 * @param errorList
	 *            the error list
	 */
	public GamaCompilationFailedException(final List<GamlCompilationError> errorList) {
		this.errorList = errorList;
	}

	@Override
	public String getMessage() { return "The model couldn't be compiled because of compilation errors"; }

	/**
	 * To json string.
	 *
	 * @return the string
	 */
	public String toJsonString() {
		StringBuilder sb = new StringBuilder("{");
		sb.append("\"exception\":\"").append(getClass().getSimpleName()).append("\",");
		sb.append("\"message\":\"").append(getMessage()).append("\",");
		sb.append("\"errors\":[");

		boolean atLeastSecond = false;
		for (var error : errorList) {
			if (atLeastSecond) { sb.append(","); }
			sb.append("{");
			sb.append("\"type\":\"").append(error.errorType.name()).append("\",");
			sb.append("\"message\":\"").append(error.message).append("\",");
			sb.append("\"code\":\"").append(error.code).append("\",");
			sb.append("\"data\":[\"").append(String.join("\",\"", error.data != null ? error.data : new String[0]))
					.append("\"],");
			sb.append("\"source\":\"")
					.append(FileUtils.escapeFilePath(error.uri != null ? error.uri.toFileString() : "")).append("\",");
			sb.append("\"uri\":\"").append(error.uri).append("\"");
			var node = error.source == null ? null : NodeModelUtils.getNode(error.source);
			if (node != null) {
				sb.append(",\"starting_at_line\":").append(node.getStartLine()).append(",");
				sb.append("\"offset\":").append(node.getTotalOffset()).append("");
			}
			sb.append("}");
			atLeastSecond = true;
		}
		sb.append("]");
		sb.append('}');
		return sb.toString();
	}
}
