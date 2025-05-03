/*******************************************************************************************************
 *
 * BackgroundChooser.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.views.toolbar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;

import gama.core.util.GamaColor;
import gama.ui.shared.menus.GamaColorMenu;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaColors.GamaUIColor;
import gama.ui.shared.resources.GamaIcon;

/**
 * Class FontSizer.
 *
 * @author drogoul
 * @since 9 févr. 2015
 *
 */
public class BackgroundChooser {

	/**
	 * @param tb
	 */
	public static void install(final IToolbarDecoratedView.Colorizable view, final GamaToolbar2 tb) {
		String[] labels = view.getColorLabels();
		GamaUIColor[] colors = new GamaUIColor[labels.length];
		for (int i = 0; i < labels.length; i++) { colors[i] = view.getColor(i); }
		for (int i = 0; i < labels.length; i++) {
			final int index = i;
			final ToolItem item = tb.button(null, labels[index], labels[index], null, SWT.RIGHT);
			GamaColor color = colors[index].gamaColor();
			item.setImage(GamaIcon.ofColor(color).image());
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
					item.setImage(GamaIcon.ofColor(color).image());
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

	/**
	 * Install.
	 *
	 * @param view
	 *            the view
	 * @param mainMenu
	 *            the main menu
	 */
	public static void install(final IToolbarDecoratedView.Colorizable view, final Menu mainMenu) {
		String[] labels = view.getColorLabels();
		GamaUIColor[] colors = new GamaUIColor[labels.length];
		for (int i = 0; i < labels.length; i++) { colors[i] = view.getColor(i); }
		for (int i = 0; i < labels.length; i++) {
			final int index = i;
			GamaColorMenu.addColorSubmenuTo(mainMenu, labels[i], c -> {
				view.setColor(index, GamaColors.get(c.red(), c.green(), c.blue()));
			});
		}

	}

}
