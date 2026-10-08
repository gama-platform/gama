/*******************************************************************************************************
 *
 * IPreferenceChangeListener.java, in gama.api, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.prefs;

/**
 * The interface {@code IPreferenceChangeListener} defines a listener for GAMA preference value changes. It is a
 * generic interface parameterized by the type {@code T} of the preference value being monitored.
 * <p>
 * Classes interested in reacting to preference changes implement this interface and register an instance using
 * {@link Pref#addChangeListener(IPreferenceChangeListener)} or {@link Pref#onChange(IPreferenceAfterChangeListener)}.
 * </p>
 * <p>
 * The interface provides two callback methods:
 * <ul>
 * <li>{@link #beforeValueChange(Object)} — called before the value is committed, giving the listener an opportunity to
 * veto the change by returning {@code false}.</li>
 * <li>{@link #afterValueChange(Object)} — called after the new value has been committed, allowing the listener to
 * react accordingly.</li>
 * </ul>
 * Two convenience sub-interfaces are provided for listeners that only care about one phase of the lifecycle:
 * {@link IPreferenceAfterChangeListener} and {@link IPreferenceBeforeChangeListener}.
 * </p>
 *
 * @param <T>
 *            the type of the preference value being observed
 */
public interface IPreferenceChangeListener<T> {

	/**
	 * A specialization of {@link IPreferenceChangeListener} whose only concern is reacting <em>after</em> a preference
	 * value has been committed. It provides a no-op default implementation of {@link #beforeValueChange(Object)} that
	 * always returns {@code true} (i.e., never vetoes the change), so implementing classes only need to override
	 * {@link #afterValueChange(Object)}.
	 *
	 * @param <T>
	 *            the type of the preference value being observed
	 */
	public interface IPreferenceAfterChangeListener<T> extends IPreferenceChangeListener<T> {

		/**
		 * Default implementation that always accepts the incoming value change without any veto. Implementing classes
		 * do not need to override this method.
		 *
		 * @param newValue
		 *            the candidate new value of the preference
		 * @return {@code true} always, unconditionally accepting the change
		 */
		@Override
		default boolean beforeValueChange(final T newValue) {
			return true;
		}

	}

	/**
	 * A specialization of {@link IPreferenceChangeListener} whose only concern is inspecting or vetoing a preference
	 * value change <em>before</em> it is committed. It provides a no-op default implementation of
	 * {@link #afterValueChange(Object)}, so implementing classes only need to override
	 * {@link #beforeValueChange(Object)}.
	 *
	 * @param <T>
	 *            the type of the preference value being observed
	 */
	public interface IPreferenceBeforeChangeListener<T> extends IPreferenceChangeListener<T> {

		/**
		 * Default no-op implementation invoked after the new value has been committed. Implementing classes do not need
		 * to override this method.
		 *
		 * @param newValue
		 *            the new value that has just been set on the preference
		 */
		@Override
		default void afterValueChange(final T newValue) {}

	}

	/**
	 * Called by the preference system <em>before</em> a new value is committed to a {@link Pref}. The listener can
	 * veto the change by returning {@code false}. If at least one registered listener returns {@code false}, the
	 * change is cancelled and the preference retains its current value.
	 *
	 * @param newValue
	 *            the candidate new value that is about to be assigned to the preference
	 * @return {@code true} to accept the change, {@code false} to veto it
	 */
	public boolean beforeValueChange(T newValue);

	/**
	 * Called by the preference system <em>after</em> a new value has been successfully committed to a {@link Pref}.
	 * This is the appropriate place to trigger any side-effects that depend on the updated preference value.
	 *
	 * @param newValue
	 *            the new value that has just been assigned to the preference
	 */
	public void afterValueChange(T newValue);
}