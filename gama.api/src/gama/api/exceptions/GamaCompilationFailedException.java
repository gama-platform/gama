/*******************************************************************************************************
 *
 * GamaCompilationFailedException.java, in gama.api, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.exceptions;

import java.util.List;

import gama.api.compilation.GamlCompilationError;
import gama.api.gaml.GAML;

/**
 * Exception thrown when a GAML model fails to compile.
 * <p>
 * This exception is raised when the GAML compiler encounters one or more errors that prevent
 * the model from being successfully compiled. It aggregates all compilation errors into a list,
 * allowing for comprehensive error reporting to the user.
 * </p>
 * <p>
 * The exception provides methods to access the individual compilation errors and to serialize
 * the error information to JSON format for integration with external tools or web interfaces.
 * </p>
 * 
 * @author GAMA Development Team
 */
public class GamaCompilationFailedException extends GamaRuntimeException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1392647532622819498L;

	/** The list of compilation errors that prevented successful compilation. */
	public final List<GamlCompilationError> errorList;

	/**
	 * Instantiates a new compilation failed exception with the given error list.
	 *
	 * @param errorList the list of compilation errors encountered
	 */
	public GamaCompilationFailedException(final List<GamlCompilationError> errorList) {
		super(null, "The model couldn't be compiled because of compilation errors", true);
		this.errorList = errorList;
	}

	/**
	 * Converts this exception and its error list to a JSON string representation.
	 * <p>
	 * The JSON format includes the exception type, message, and a detailed array of all
	 * compilation errors with their types, messages, codes, source locations, and line numbers.
	 * </p>
	 *
	 * @return a JSON string representation of the compilation errors
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
			sb.append("\"type\":\"").append(error.errorType().name()).append("\",");
			sb.append("\"message\":\"").append(error.message()).append("\",");
			sb.append("\"code\":\"").append(error.code()).append("\",");
			sb.append("\"data\":[\"").append(String.join("\",\"", error.data() != null ? error.data() : new String[0]))
					.append("\"],");
			sb.append("\"source\":\"").append(
					(error.uri() != null ? error.uri().toFileString() : "").replace("\\", "\\\\").replace("\"", "\\\""))
					.append("\",");
			sb.append("\"uri\":\"").append(error.uri()).append("\"");
			int[] loc = GAML.getLocationInFileInfo(error.source());
			sb.append(",\"starting_at_line\":").append(loc[0]).append(",");
			sb.append("\"offset\":").append(loc[1]).append("");
			// }
			sb.append("}");
			atLeastSecond = true;
		}
		sb.append("]");
		sb.append('}');
		return sb.toString();
	}
}
