/*******************************************************************************************************
 *
 * BackgroundChooser.java, in gama.ui.shared.shared, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.views.toolbar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;

import gama.core.util.GamaColor;
import gama.ui.shared.menus.GamaColorMenu;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.GamaColors.GamaUIColor;

/**
 * Class FontSizer.
 *
 * @author drogoul
 * @since 9 févr. 2015
 *
 */
public class BackgroundChooser {

	/** The view. */
	final IToolbarDecoratedView.Colorizable view;

	/** The labels. */
	final String[] labels;

	/** The colors. */
	final GamaUIColor[] colors;

	/**
	 * Instantiates a new background chooser.
	 *
	 * @param view
	 *            the view
	 */
	public BackgroundChooser(final IToolbarDecoratedView.Colorizable view) {
		// We add a control listener to the toolbar in order to install the
		// gesture once the control to resize have been created.
		this.view = view;
		labels = view.getColorLabels();
		colors = new GamaUIColor[labels.length];
		for (int i = 0; i < labels.length; i++) { colors[i] = view.getColor(i); }
	}

	/**
	 * @param tb
	 */
	public void install(final GamaToolbar2 tb) {
		for (int i = 0; i < labels.length; i++) {
			final int index = i;
			final ToolItem item = tb.button(null, labels[index], labels[index], null, SWT.RIGHT);
			item.setImage(GamaIcon.ofColor(colors[index], true).image());
			item.addSelectionListener(new SelectionAdapter() {

				SelectionListener listener = new SelectionAdapter() {

					@Override
					public void widgetSelected(final SelectionEvent e) {
						final MenuItem i = (MenuItem) e.widget;
						final String color = i.getText().replace("#", "");
						final GamaColor c = GamaColor.colors.get(color);
						if (c == null) return;
						changeColor(c.red(), c.green(), c.blue());
					}

				};

				void changeColor(final int r, final int g, final int b) {
					colors[index] = GamaColors.get(r, g, b);
					// Image temp = item.getImage();
					item.setImage(GamaIcon.ofColor(colors[index], true).image());
					// temp.dispose();
					view.setColor(index, colors[index]);
				}

				@Override
				public void widgetSelected(final SelectionEvent e) {

					new GamaColorMenu(null).open(item.getParent(), e, listener, this::changeColor);

				}
			});

		}
	}

}
