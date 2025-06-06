/*******************************************************************************************************
 *
 * AbstractSummary.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.test;

import java.util.Map;

import org.eclipse.emf.common.util.URI;

import gama.core.common.interfaces.IColored;
import gama.core.common.preferences.GamaPreferences;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.util.GamaColor;
import gama.dev.COUNTER;
import gama.gaml.interfaces.INamed;
import one.util.streamex.StreamEx;

/**
 * The Class AbstractSummary.
 *
 * @param <S>
 *            the generic type
 */
public abstract class AbstractSummary<S extends WithTestSummary<?>> implements INamed, IColored {

	/** The uri. */
	private final URI uri;

	/** The title. */
	private final String title;

	/** The index. */
	public final int index = COUNTER.GET_UNIQUE();

	/** The error. */
	public String error;

	/**
	 * Instantiates a new abstract summary.
	 *
	 * @param statement
	 *            the statement
	 */
	public AbstractSummary(final S statement) {
		if (statement != null) {
			title = statement.getTitleForSummary();
			uri = statement.getURI();
		} else {
			title = null;
			uri = null;
		}
	}

	/**
	 * Sets the error.
	 *
	 * @param error
	 *            the new error
	 */
	public void setError(final String error) { this.error = error; }

	/**
	 * Gets the error.
	 *
	 * @return the error
	 */
	public String getError() { return error; }

	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	public int getIndex() { return index; }

	/**
	 * Gets the uri.
	 *
	 * @return the uri
	 */
	public URI getURI() { return uri; }

	/**
	 * Gets the title.
	 *
	 * @return the title
	 */
	public String getTitle() { return title; }

	/**
	 * Gets the time stamp.
	 *
	 * @return the time stamp
	 */
	public abstract long getTimeStamp();

	/**
	 * Gets the color.
	 *
	 * @return the color
	 */
	@Override
	public GamaColor getColor(final IScope scope) {
		return getState().getColor(scope);
	}

	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public abstract TestState getState();

	/**
	 * Sets the state.
	 *
	 * @param state
	 *            the new state
	 */
	public abstract void setState(TestState state);

	/**
	 * Reset.
	 */
	public void reset() {
		error = null;
	}

	/**
	 * Gets the summaries.
	 *
	 * @return the summaries
	 */
	public abstract Map<String, ? extends AbstractSummary<?>> getSummaries();

	/**
	 * Count tests with.
	 *
	 * @param state
	 *            the state
	 * @return the int
	 */
	public abstract int countTestsWith(final TestState state);

	/**
	 * Size.
	 *
	 * @return the int
	 */
	public abstract int size();

	@Override
	public final String toString() {
		final TestState state = getState();
		if (GamaPreferences.Runtime.FAILED_TESTS.getValue() && state != TestState.FAILED && state != TestState.ABORTED) {
			return "";
		}
		final StringBuilder sb = new StringBuilder();
		printHeader(sb);
		sb.append(state).append(": ").append(getTitle()).append(" ");
		if (error != null) { sb.append('[').append(error).append(']'); }
		printFooter(sb);
		for (final AbstractSummary<?> summary : getSummaries().values()) {
			// Prevent being too flooded with tests results
			// Related https://github.com/gama-platform/new.gama/issues/703
			if (GAMA.isInHeadLessMode() && summary.getState() == TestState.PASSED) { continue; }
			final String child = summary.toString();
			if (child.isEmpty()) { continue; }
			sb.append(child);
		}
		return sb.toString();
	}

	/**
	 * Prints the footer.
	 *
	 * @param sb
	 *            the sb
	 */
	protected void printFooter(final StringBuilder sb) {}

	/**
	 * Prints the header.
	 *
	 * @param sb
	 *            the sb
	 */
	protected void printHeader(final StringBuilder sb) {}

	/**
	 * Gets the summary of.
	 *
	 * @param uri
	 *            the uri
	 * @return the summary of
	 */
	public AbstractSummary<?> getSummaryOf(final URI uri) {
		// if (this.uri != null) {
		// DEBUG.OUT("Comparing " + this.uri + " to " + uri);
		// }
		if (uri.equals(this.uri)) {
			return this;
		}
		return StreamEx.ofValues(getSummaries()).findFirst(s -> s.getSummaryOf(uri) != null).orElse(null);
	}

}
