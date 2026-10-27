/*******************************************************************************************************
 *
 * ConsoleContextMenuHelper.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.access;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import gama.ui.shared.commands.SearchGamlReferenceHandler;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;

/**
 * Installs a context menu on console viewers (both execution console and interactive console) providing "Search in
 * GAML Reference", Copy, Paste, Select All, and Clear actions.
 */
public class ConsoleContextMenuHelper {

	/**
	 * Installs the context menu on the given StyledText control.
	 *
	 * @param styledText
	 *            the console StyledText widget
	 * @param clearAction
	 *            the clear action, or null if clearing is not supported
	 * @param allowPaste
	 *            whether paste should be included (e.g. true for interactive console, false for read-only console)
	 * @return the created Menu
	 */
	public static Menu install(final StyledText styledText, final Runnable clearAction, final boolean allowPaste) {
		if (styledText == null || styledText.isDisposed()) return null;
		final Menu menu = new Menu(styledText.getShell(), SWT.POP_UP);
		styledText.setMenu(menu);
		styledText.addDisposeListener(e -> {
			if (!menu.isDisposed()) { menu.dispose(); }
		});
		menu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(final MenuEvent e) {
				for (final MenuItem item : menu.getItems()) { item.dispose(); }

				String query = styledText.getSelectionText();
				if (query == null || query.isBlank()) {
					final Point cursorLoc = styledText.getDisplay().getCursorLocation();
					final Point pt = styledText.toControl(cursorLoc);
					try {
						final int offset = styledText.getOffsetAtPoint(pt);
						if (offset >= 0) {
							query = getWordAtOffset(styledText, offset);
						}
					} catch (final IllegalArgumentException ex) {
						// point is not directly over a character
					}
				}
				if (query == null || query.isBlank()) {
					query = SearchGamlReferenceHandler.getWordAtCaret(styledText);
				}

				final String finalQuery = query == null ? "" : query.trim();

				// "Search in GAML Reference" item
				final MenuItem searchItem = new MenuItem(menu, SWT.PUSH);
				searchItem.setText("Search in GAML Reference\tF1");
				searchItem.setImage(GamaIcon.named("editor/command.find").image());
				searchItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent se) {
						GamlAccessContents2.open(finalQuery);
					}
				});

				new MenuItem(menu, SWT.SEPARATOR);

				// Copy item
				final MenuItem copyItem = new MenuItem(menu, SWT.PUSH);
				copyItem.setText("Copy");
				copyItem.setImage(GamaIcon.named(IGamaIcons.COPY).image());
				copyItem.setEnabled(styledText.getSelectionCount() > 0);
				copyItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent se) {
						styledText.copy();
					}
				});

				if (allowPaste) {
					final MenuItem pasteItem = new MenuItem(menu, SWT.PUSH);
					pasteItem.setText("Paste");
					pasteItem.setImage(GamaIcon.named(IGamaIcons.PASTE).image());
					pasteItem.setEnabled(styledText.getEditable());
					pasteItem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(final SelectionEvent se) {
							styledText.paste();
						}
					});
				}

				final MenuItem selectAllItem = new MenuItem(menu, SWT.PUSH);
				selectAllItem.setText("Select All");
				selectAllItem.setEnabled(styledText.getCharCount() > 0);
				selectAllItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent se) {
						styledText.selectAll();
					}
				});

				if (clearAction != null) {
					new MenuItem(menu, SWT.SEPARATOR);
					final MenuItem clearItem = new MenuItem(menu, SWT.PUSH);
					clearItem.setText("Clear");
					clearItem.setImage(GamaIcon.named(IGamaIcons.ACTION_CLEAR).image());
					clearItem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(final SelectionEvent se) {
							clearAction.run();
						}
					});
				}
			}
		});
		return menu;
	}

	/**
	 * Gets the word at the specified character offset.
	 *
	 * @param st
	 *            the StyledText widget
	 * @param offset
	 *            the offset
	 * @return the word, or an empty string
	 */
	public static String getWordAtOffset(final StyledText st, final int offset) {
		if (st == null || st.isDisposed()) return "";
		final String text = st.getText();
		if (text == null || text.isEmpty()) return "";
		final int len = text.length();
		if (offset < 0 || offset > len) return "";
		int start = offset;
		int end = offset;
		while (start > 0 && SearchGamlReferenceHandler.isWordChar(text.charAt(start - 1))) {
			start--;
		}
		while (end < len && SearchGamlReferenceHandler.isWordChar(text.charAt(end))) {
			end++;
		}
		return start < end ? text.substring(start, end).trim() : "";
	}

}
