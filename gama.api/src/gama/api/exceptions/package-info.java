/*******************************************************************************************************
 *
 * package-info.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

/**
 * The exceptions package defines the hierarchy of exceptions used throughout the GAMA platform.
 * 
 * <p>This package provides specialized exception types for different error scenarios in GAMA,
 * including compilation errors, runtime errors, assertion failures, and command execution errors.</p>
 * 
 * <h2>Exception Hierarchy</h2>
 * 
 * <ul>
 *   <li>{@link gama.api.exceptions.GamaRuntimeException} - Base runtime exception for GAMA
 *       <ul>
 *         <li>{@link gama.api.exceptions.GamaRuntimeFileException} - File-related runtime errors</li>
 *         <li>{@link gama.api.exceptions.GamaAssertException} - Assertion failures in models</li>
 *       </ul>
 *   </li>
 *   <li>{@link gama.api.exceptions.GamaCompilationFailedException} - Compilation failures</li>
 *   <li>{@link gama.api.exceptions.CommandException} - Command execution errors</li>
 *   <li>{@link gama.api.exceptions.FlushBufferException} - Buffer flushing errors</li>
 * </ul>
 * 
 * <h2>Usage</h2>
 * 
 * <h3>Runtime Exceptions:</h3>
 * <pre>{@code
 * throw GamaRuntimeException.error("Invalid operation", scope);
 * }</pre>
 * 
 * <h3>Compilation Exceptions:</h3>
 * <pre>{@code
 * if (hasErrors) {
 *     throw new GamaCompilationFailedException(errorList);
 * }
 * }</pre>
 * 
 * <h2>Error Handling</h2>
 * 
 * <p>GAMA exceptions typically include:</p>
 * <ul>
 *   <li>Detailed error messages</li>
 *   <li>Source location information (for compilation errors)</li>
 *   <li>Runtime context (scope, agent) for runtime errors</li>
 *   <li>Stack traces for debugging</li>
 * </ul>
 * 
 * @author The GAMA Development Team
 * @version 2025-03
 * 
 * @see gama.api.exceptions.GamaRuntimeException
 * @see gama.api.exceptions.GamaCompilationFailedException
 */
package gama.api.exceptions;
