/*******************************************************************************************************
 *
 * EditorPresentationMenu.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.editor;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

import gama.core.common.preferences.GamaPreferences;
import gama.core.common.preferences.Pref;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.utils.WorkbenchHelper;
import gaml.compiler.ui.editor.GamlEditor;

/**
 * The Class EditorMenu.
 */
public class EditorPresentationMenu extends ContributionItem implements IWorkbenchContribution {

	/** The mark pref. */
	public Pref<Boolean> markPref;

	@Override
	public void initialize(final IServiceLocator serviceLocator) {}

	@Override
	public void fill(final Menu m, final int index) {
		final MenuItem menuItem = new MenuItem(m, SWT.CASCADE);
		menuItem.setText("Presentation");
		menuItem.setImage(GamaIcon.named("display/menu.presentation").image());
		final Menu menu = new Menu(menuItem);
		if (menuItem.getMenu() != null) { menuItem.getMenu().dispose(); }
		menuItem.setMenu(menu);
		menu.addListener(SWT.Show, e -> {
			markPref = GamaPreferences.get("pref_editor_mark_occurrences", Boolean.class);
			for (final MenuItem item : menu.getItems()) { item.dispose(); }
			if (getEditor() != null) {
				createLineToggle(menu);
				createFoldingToggle(menu);
				createMarkToggle(menu);
				createOverviewToggle(menu);
				createWordWrapToggle(menu);
				// createWhiteSpaceToggle(menu);
			}
		});

	}

	/**
	 * Creates the mark toggle.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param menu
	 *            the menu
	 * @date 26 juin 2023
	 */
	public void createMarkToggle(final Menu menu) {
		final MenuItem mark = new MenuItem(menu, SWT.PUSH);
		boolean selected = markPref.getValue();
		mark.setText(selected ? " Do not mark symbols occurences" : " Mark occurences of symbols");
		// mark.setSelection(markPref.getValue());
		mark.setImage(GamaIcon.named("editor/toggle.mark").image());

		mark.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				markPref.set(mark.getSelection()).save();
			}
		});

	}

	/**
	 * Creates the mark toggle.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param menu
	 *            the menu
	 * @date 26 juin 2023
	 */
	public void createWordWrapToggle(final Menu menu) {
		final MenuItem mark = new MenuItem(menu, SWT.PUSH);
		boolean selected = getEditor().isWordWrapEnabled();
		mark.setText(selected ? " Turn Word Wrap off" : " Turn Word Wrap on");
		mark.setImage(GamaIcon.named("editor/menu.delimiter").image());

		mark.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				getEditor().setWordWrap(mark.getSelection());
			}
		});

	}

	/**
	 * Creates the overview toggle.
	 *
	 * @param menu
	 *            the menu
	 */
	public void createOverviewToggle(final Menu menu) {
		final MenuItem overview = new MenuItem(menu, SWT.PUSH);
		boolean selected = getEditor().isOverviewRulerVisible();
		overview.setText(selected ? " Hide markers overview" : " Show markers overview");
		// overview.setSelection(selected);
		overview.setImage(GamaIcon.named("editor/toggle.overview").image());
		overview.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				final boolean shown = getEditor().isOverviewRulerVisible();
				if (shown) {
					getEditor().hideOverviewRuler();
				} else {
					getEditor().showOverviewRuler();
				}
			}
		});

	}

	/**
	 *
	 */
	public void createFoldingToggle(final Menu menu) {
		final MenuItem folding = new MenuItem(menu, SWT.PUSH);
		boolean selected = getEditor().isRangeIndicatorEnabled();
		folding.setText(selected ? " Unfold code sections" : " Fold code sections");

		folding.setSelection(selected);
		folding.setImage(GamaIcon.named("editor/toggle.folding").image());
		folding.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				getEditor().getAction("FoldingToggle").run();
			}
		});

	}

	/**
	 *
	 */
	public void createLineToggle(final Menu menu) {
		final MenuItem line = new MenuItem(menu, SWT.PUSH);
		boolean selected = getEditor().isLineNumberRulerVisible();
		line.setText(selected ? " Hide line numbers" : " Display line numbers");

		// line.setSelection(selected);
		line.setImage(GamaIcon.named("editor/toggle.numbers").image());
		line.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				getEditor().getAction(ITextEditorActionConstants.LINENUMBERS_TOGGLE).run();
			}
		});

	}

	/**
	 * Gets the editor.
	 *
	 * @return the editor
	 */
	protected GamlEditor getEditor() { return (GamlEditor) WorkbenchHelper.getActiveEditor(); }
}
