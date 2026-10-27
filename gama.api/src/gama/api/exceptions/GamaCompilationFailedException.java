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

import gama.api.GAMA;
import gama.api.compilation.GamlCompilationError;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonArray;
import gama.api.utils.json.IJsonObject;

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

	/** 
	 * The list of compilation errors that prevented successful compilation.
	 * <p>
	 * Each error in this list contains detailed information including the error type, message,
	 * source code location (file URI, line number, offset), and any associated data. This list
	 * is immutable after construction to preserve the compilation error state.
	 * </p>
	 */
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
	 * compilation errors with their types, messages, codes, and source locations. Lines and columns are
	 * 1-based; offsets are 0-based. An unavailable location is represented by zeros.
	 * </p>
	 *
	 * @return a JSON string representation of the compilation errors
	 */
	public String toJsonString() {
		IJson json = GAMA.getJsonEncoder();
		IJsonArray errors = json.array();
		for (var error : errorList) {
			IJsonArray data = json.array();
			if (error.data() != null) {
				for (String value : error.data()) { data.add(value); }
			}
			var location = error.location();
			IJsonObject item = json.object("type", error.errorType().name(), "message", error.message())
					.add("code", error.code()).add("data", data)
					.add("source", error.uri() == null ? "" : error.uri().toFileString())
					.add("uri", error.uri() == null ? null : error.uri().toString())
					.add("starting_at_line", location.line()).add("column", location.column())
					.add("offset", location.offset());
			errors.add(item);
		}
		return json.object("exception", getClass().getSimpleName(), "message", getMessage()).add("errors", errors)
				.toString();
	}
}
