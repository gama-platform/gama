/*******************************************************************************************************
 *
 * GamaRuntimeException.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.exceptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EObject;

import gama.api.gaml.symbols.ISymbol;
import gama.api.kernel.simulation.IClock;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.runtime.scope.IScope;

/**
 * Base exception class for all runtime exceptions in GAMA.
 * <p>
 * This exception is thrown when an abnormal situation occurs during model execution. It provides
 * comprehensive context information including:
 * </p>
 * <ul>
 * <li>The simulation cycle at which the error occurred</li>
 * <li>The agents involved in the error</li>
 * <li>The execution context (scope, symbols, etc.)</li>
 * <li>Whether the exception represents an error or warning</li>
 * <li>Stack trace and contextual information for debugging</li>
 * </ul>
 * <p>
 * GamaRuntimeException can be configured to track multiple occurrences of the same error across
 * different agents, providing aggregated error reporting to avoid flooding the user with duplicate
 * messages.
 * </p>
 * 
 * @author drogoul
 * @since 7 janv. 2011
 */

public class GamaRuntimeException extends RuntimeException {

	/** The simulation cycle at which this exception occurred. */
	private final long cycle;

	/** The names of agents involved in this exception. */
	protected final List<String> agentsNames = new ArrayList<>();

	/** Flag indicating whether this exception represents a warning (true) or an error (false). */
	private boolean isWarning;

	/** Additional context information describing where and how the exception occurred. */
	protected final List<String> context = new ArrayList<>();

	/** Reference to the GAML model element (EObject) where the exception originated. */
	protected EObject editorContext;

	/** Number of times this exception has occurred across different agents. */
	protected int occurrences = 0;

	/** Flag indicating whether this exception has been reported to the user. */
	protected boolean reported = false;

	/** The execution scope in which this exception occurred. */
	protected final IScope scope;

	// Factory methods
	/**
	 * Creates a GamaRuntimeException from a generic Throwable.
	 * <p>
	 * This factory method wraps different types of exceptions into appropriate GAMA exception types,
	 * such as {@link GamaRuntimeFileException} for I/O errors.
	 * </p>
	 *
	 * @param ex the throwable to wrap
	 * @param scope the execution scope in which the exception occurred
	 * @return a GamaRuntimeException wrapping the throwable
	 */
	public static GamaRuntimeException create(final Throwable ex, final IScope scope) {
		return switch (ex) {
			case GamaRuntimeException gre -> gre;
			case IOException io -> new GamaRuntimeFileException(scope, io);
			default -> new GamaRuntimeException(scope, ex);
		};
	}

	/**
	 * Creates a GamaRuntimeException representing an error.
	 *
	 * @param s the error message
	 * @param scope the execution scope
	 * @return a new error exception
	 */
	public static GamaRuntimeException error(final String s, final IScope scope) {
		return new GamaRuntimeException(scope, s, false);
	}

	/**
	 * Creates a GamaRuntimeException representing a warning.
	 *
	 * @param s the warning message
	 * @param scope the execution scope
	 * @return a new warning exception
	 */
	public static GamaRuntimeException warning(final String s, final IScope scope) {
		return new GamaRuntimeException(scope, s, true);
	}

	// Constructors

	/**
	 * Extracts a user-friendly exception name from a Throwable.
	 * <p>
	 * This method provides specialized names for common exception types and library-specific
	 * exceptions to improve error message clarity for users.
	 * </p>
	 *
	 * @param ex the exception
	 * @return a user-friendly name for the exception
	 */
	@SuppressWarnings ("unused")
	protected static String getExceptionName(final Throwable ex) {
		final String s = ex.getClass().getName();
		if (s.contains("geotools") || s.contains("opengis")) return "exception in GeoTools library";
		if (s.contains("jts")) return "exception in JTS library";
		if (s.contains("rcaller")) return "exception in RCaller library";
		if (s.contains("jogamp")) return "exception in JOGL library";
		if (s.contains("weka")) return "exception in Weka library";
		if (s.contains("math3")) return "exception in Math library";
		return switch (ex) {
			case NullPointerException npe -> "nil value detected";
			case IndexOutOfBoundsException ioobe -> "index out of bounds";
			case IOException ioe -> "I/O error";
			case CoreException ce -> "exception in Eclipse";
			case ClassCastException cce -> "wrong casting";
			case IllegalArgumentException iae -> "illegal argument";
			default -> ex.getClass().getSimpleName();
		};

	}

	/**
	 * Constructs a GamaRuntimeException from a Throwable.
	 * <p>
	 * This constructor wraps a Java exception and captures the current execution context
	 * including the symbol being executed and the simulation cycle. It also includes
	 * stack trace information from the original exception.
	 * </p>
	 *
	 * @param scope the execution scope
	 * @param ex the underlying exception
	 */
	protected GamaRuntimeException(final IScope scope, final Throwable ex) {
		super(ex == null ? "Error" : "Java error: " + getExceptionName(ex), ex);
		// AD: 18/01/16 Adding this to address Issue #1411
		this.scope = scope;
		if (scope != null) {
			final ISymbol symbol = scope.getCurrentSymbol();
			if (symbol != null) { addContext(symbol); }
		}
		if (ex != null) {
			ex.printStackTrace();
			addContext(ex.getClass().getSimpleName() + ": " + ex.getMessage());
			int i = 0;
			for (final StackTraceElement element : ex.getStackTrace()) {
				addContext(element.toString());
				if (i++ > 5) { break; }
			}
		}
		cycle = computeCycle(scope);

	}

	/**
	 * Constructs a GamaRuntimeException with a custom message.
	 * <p>
	 * This constructor creates an exception with a user-specified message and allows
	 * configuring whether it represents a warning or error.
	 * </p>
	 *
	 * @param scope the execution scope
	 * @param s the exception message
	 * @param warning true if this is a warning, false if it's an error
	 */
	protected GamaRuntimeException(final IScope scope, final String s, final boolean warning) {
		super(s);
		// AD: 18/01/16 Adding this to address Issue #1411
		this.scope = scope;
		if (scope != null) {
			final ISymbol symbol = scope.getCurrentSymbol();
			if (symbol != null) { addContext(symbol); }
		}
		cycle = computeCycle(scope);
		isWarning = warning;

	}

	/**
	 * Adds a context string to this exception.
	 * <p>
	 * Context strings provide additional information about where and how the exception occurred,
	 * helping with debugging and error reporting.
	 * </p>
	 *
	 * @param c the context string to add
	 */
	public void addContext(final String c) {
		context.add(c);
	}

	/**
	 * Adds context information from a GAML symbol.
	 * <p>
	 * This method extracts contextual information from a symbol (such as its GAML representation)
	 * and adds it to the exception's context. It also captures the underlying model element
	 * for editor integration.
	 * </p>
	 *
	 * @param s the symbol to extract context from
	 */
	public void addContext(final ISymbol s) {
		String serial = s.serializeToGaml(false);
		if (serial != null && !serial.isBlank()) { addContext("in " + serial); }
		final EObject e = s.getDescription().getUnderlyingElement();
		if (e != null && editorContext == null) { editorContext = e; }
	}

	/**
	 * Gets the editor context (model element) where this exception originated.
	 *
	 * @return the EObject representing the model element, or null if not available
	 */
	public EObject getEditorContext() { return editorContext; }

	/**
	 * Adds an agent name to the list of agents affected by this exception.
	 * <p>
	 * This method is used to track which agents encountered this exception. It prevents
	 * duplicate entries and increments the occurrence counter.
	 * </p>
	 *
	 * @param agent the name of the agent to add
	 */
	public void addAgent(final String agent) {
		occurrences++;
		if (agentsNames.contains(agent)) return;
		agentsNames.add(agent);
	}

	/**
	 * Adds multiple agent names to the list of affected agents.
	 *
	 * @param agents the list of agent names to add
	 */
	public void addAgents(final List<String> agents) {
		for (final String agent : agents) { addAgent(agent); }
	}

	/**
	 * Gets the simulation cycle at which this exception occurred.
	 *
	 * @return the cycle number
	 */
	public long getCycle() { return cycle; }

	/**
	 * Gets a summary string describing the agents affected by this exception.
	 * <p>
	 * The summary includes the number of occurrences and the affected agents,
	 * formatted for user-friendly display.
	 * </p>
	 *
	 * @return a formatted string summarizing affected agents
	 */
	public String getAgentSummary() {
		final int size = agentsNames.size();
		final String agents = size == 0 ? "" : size == 1 ? agentsNames.get(0) : String.valueOf(size) + " agents";
		final String occurence = occurrences == 0 ? "" : occurrences == 1 ? "1 occurence in "
				: String.valueOf(occurrences) + " occurrences in ";
		return occurence + agents;
	}

	/**
	 * Checks whether this exception represents a warning.
	 *
	 * @return true if this is a warning, false if it's an error
	 */
	public boolean isWarning() { return isWarning; }

	/**
	 * Computes the current simulation cycle from a scope.
	 *
	 * @param scope the execution scope
	 * @return the current cycle number, or 0 if not available
	 */
	public long computeCycle(final IScope scope) {
		final IClock clock = scope == null ? null : scope.getClock();
		return clock == null ? 0l : clock.getCycle();
	}

	/**
	 * Gets the full context information as a list of strings.
	 * <p>
	 * This includes the simulation name, affected agents, and all context strings
	 * that have been added to this exception.
	 * </p>
	 *
	 * @return the context as a list of strings
	 */
	public List<String> getContextAsList() {
		final List<String> result = new ArrayList<>();
		if (scope != null && scope.getRoot() != null) { result.add("in " + scope.getRoot().getName()); }

		final int size = agentsNames.size();
		if (size == 0) return result;
		if (size == 1) {
			result.add("in agent " + agentsNames.get(0));
		} else {
			final StringBuilder sb = new StringBuilder();
			sb.append("in agents ").append(agentsNames.get(0));
			for (int i = 1; i < agentsNames.size(); i++) {
				sb.append(", ").append(agentsNames.get(i));
				if (sb.length() > 100) {
					sb.append("...");
					break;
				}
			}
			result.add(sb.toString());
		}
		result.addAll(context);
		return result;
	}

	@Override
	public String toString() {
		final String s = getClass().getName();
		final String message = getLocalizedMessage();
		return message != null ? message : s;
	}

	/**
	 * Checks if this exception is equivalent to another exception.
	 * <p>
	 * Two exceptions are considered equivalent if they have the same message, editor context,
	 * simulation root, and occurred in the same cycle. This is used to avoid reporting
	 * duplicate exceptions.
	 * </p>
	 *
	 * @param ex the exception to compare with
	 * @return true if the exceptions are equivalent
	 */
	public boolean equivalentTo(final GamaRuntimeException ex) {
		return this == ex || editorContext == ex.editorContext && getMessage().equals(ex.getMessage()) && scope != null
				&& ex.scope != null && scope.getRoot() == ex.scope.getRoot() && getCycle() == ex.getCycle();
	}

	/**
	 * Marks this exception as having been reported to the user.
	 */
	public void setReported() {
		reported = true;
	}

	/**
	 * Checks if this exception has been reported to the user.
	 *
	 * @return true if the exception has been reported
	 */
	public boolean isReported() { return reported; }

	/**
	 * Gets the list of names of agents affected by this exception.
	 *
	 * @return the list of agent names
	 */
	public List<String> getAgentsNames() { return agentsNames; }

	/**
	 * Gets the complete exception message including all context information.
	 * <p>
	 * This method returns a formatted string containing the agent summary, cycle number,
	 * exception message, and all context information, suitable for display in error logs
	 * or user interfaces.
	 * </p>
	 *
	 * @return the full exception text with context
	 */
	public String getAllText() {
		final StringBuilder sb = new StringBuilder(300);
		final String a = getAgentSummary();
		sb.append(a).append(" at ");
		sb.append("cycle ").append(getCycle()).append(": ").append(getMessage());
		final List<String> strings = getContextAsList();
		for (final String s : strings) { sb.append(System.lineSeparator()).append(s); }
		return sb.toString();
	}

	/**
	 * Checks if this exception is invalid and should not be reported.
	 * <p>
	 * An exception is considered invalid if the simulation or experiment has been closed
	 * or is no longer valid. Invalid exceptions are typically not shown to the user.
	 * </p>
	 *
	 * @return true if the exception is invalid
	 */
	// If the simulation or experiment is dead, no need to report errors
	public boolean isInvalid() { return scope == null || scope.isClosed(); }

	/**
	 * Gets the top-level agent (simulation or experiment) in which this exception occurred.
	 *
	 * @return the top-level agent, or null if not available
	 */
	public ITopLevelAgent getTopLevelAgent() { return scope == null ? null : scope.getRoot(); }

}
