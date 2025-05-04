/*******************************************************************************************************
 *
 * FontSizer.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.views.toolbar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Control;

import gama.ui.shared.resources.GamaFonts;
import gama.ui.shared.resources.IGamaIcons;

/**
 * Class FontSizer.
 *
 * @author drogoul
 * @since 9 févr. 2015
 *
 */
public class FontSizer {

	/**
	 * Change font size.
	 *
	 * @param delta
	 *            the delta
	 */
	public static void changeFontSize(final int delta, final Control c) {
		if (c != null) { c.setFont(GamaFonts.withMagnification(c.getFont(), delta)); }
	}

	/**
	 * @param tb
	 */
	public static void install(final IToolbarDecoratedView.Sizable view, final GamaToolbar2 tb) {

		// We add a control listener to the toolbar in order to install the
		// gesture once the control to resize have been created.
		tb.addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(final ControlEvent e) {
				Control c = view.getSizableFontControl();

				if (c != null) {
					c.addGestureListener(ge -> {
						if (ge.detail == SWT.GESTURE_MAGNIFY) {
							changeFontSize((int) (2 * Math.signum(ge.magnification - 1.0)), c);
						}
					});
					// once installed the listener removes itself from the
					// toolbar
					tb.removeControlListener(this);
				}
			}

		});
		tb.button(IGamaIcons.FONT_INCREASE, "Increase font size", "Increase font size",
				e -> changeFontSize(2, view.getSizableFontControl()), SWT.RIGHT);
		tb.button(IGamaIcons.FONT_DECREASE, "Decrease font size", "Decrease font size",
				e -> changeFontSize(-2, view.getSizableFontControl()), SWT.RIGHT);

	}

}
