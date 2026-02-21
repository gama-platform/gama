/**
 * Exception thrown when a file I/O error occurs during model execution.
 * <p>
 * This specialized exception is used to represent file-related errors that occur during
 * GAMA model execution, such as:
 * </p>
 * <ul>
 * <li>File not found errors</li>
 * <li>Permission denied errors</li>
 * <li>I/O errors during file read/write operations</li>
 * <li>Invalid file format errors</li>
 * </ul>
 * <p>
 * It extends {@link GamaRuntimeException} and provides specialized handling for file-related
 * exceptions, ensuring they are properly contextualized within the GAMA error reporting system.
 * </p>
 */
package gama.api.exceptions;

import gama.api.runtime.scope.IScope;

/**
 * Runtime exception for file I/O errors in GAMA.
 * 
 * @author GAMA Development Team
 */
public class GamaRuntimeFileException extends GamaRuntimeException {

	/**
	 * Constructs a file exception from a Throwable.
	 *
	 * @param scope the execution scope
	 * @param ex the underlying exception (typically an IOException)
	 */
	public GamaRuntimeFileException(final IScope scope, final Throwable ex) {
		super(scope, ex);
	}

	/**
	 * Constructs a file exception with a custom message.
	 *
	 * @param scope the execution scope
	 * @param s the error message
	 */
	public GamaRuntimeFileException(final IScope scope, final String s) {
		super(scope, s, false);
	}

}