/*******************************************************************************************************
 *
 * IDisplayLayoutManager.java, in gama.ui.shared.shared, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.shared.interfaces;

/**
 * The Interface IDisplayLayoutManager.
 */
public interface IDisplayLayoutManager {

	/**
	 * Apply layout.
	 *
	 * @param layout the layout
	 */
	void applyLayout(Object layout);

	/**
	 * Applies the layout synchronously on the current thread. Must be called from the UI thread. Used by
	 * {@link gama.ui.shared.utils.SwtGui#openAndApplyLayout} which already holds the UI thread inside a
	 * {@code syncExec} under {@code shell.setRedraw(false)}.
	 *
	 * <p>
	 * The default implementation falls back to {@link #applyLayout(Object)}, which schedules a UIJob. Override this to
	 * call {@link gama.ui.experiment.commands.ArrangeDisplayViews#execute(Object)} directly.
	 * </p>
	 *
	 * @param layout
	 *            the layout
	 */
	default void applyLayoutNow(final Object layout) {
		applyLayout(layout);
	}

}