/*******************************************************************************************************
 *
 * GamlOutlinePage.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.outline;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.xtext.ui.editor.outline.impl.OutlinePage;

import gama.ui.shared.views.toolbar.GamaToolbar2;
import gama.ui.shared.views.toolbar.GamaToolbarFactory;
import gama.ui.shared.views.toolbar.IToolbarDecoratedView;

/**
 * The class GamlOutlinePage.
 *
 * @author drogoul
 * @since 24 nov. 2014
 *
 */
public class GamlOutlinePage extends OutlinePage implements IToolbarDecoratedView.Expandable {

	/** The toolbar. */
	GamaToolbar2 toolbar;

	/** The intermediate. */
	protected Composite intermediate;

	/**
	 * Instantiates a new gaml outline page.
	 */
	public GamlOutlinePage() {}

	@Override
	protected void configureActions() {
		super.configureActions();
		IToolBarManager tbm = getSite().getActionBars().getToolBarManager();
		// to have the link to the right
		List<IContributionItem> items = Arrays.asList(tbm.getItems()).reversed();
		for (IContributionItem item : items) { toolbar.item(item, SWT.RIGHT); }
		toolbar.requestLayout();
		tbm.removeAll();
		tbm.update(true);
	}

	@Override
	public Control getControl() { return intermediate; }

	@Override
	public void createControl(final Composite compo) {
		intermediate = new Composite(compo, SWT.NONE);
		Composite parent = GamaToolbarFactory.createToolbars(this, intermediate);
		super.createControl(parent);
	}

	@Override
	protected int getDefaultExpansionLevel() { return 2; }

	/**
	 * @see gama.ui.shared.views.toolbar.IToolbarDecoratedView#createToolItem(int,
	 *      gama.ui.shared.views.toolbar.GamaToolbar2)
	 */
	@Override
	public void createToolItems(final GamaToolbar2 tb) {
		this.toolbar = tb;
	}

	/**
	 * @see gama.ui.shared.views.toolbar.IToolbarDecoratedView.Expandable#expandAll()
	 */
	@Override
	public void expandAll() {
		getTreeViewer().expandAll();
	}

	/**
	 * @see gama.ui.shared.views.toolbar.IToolbarDecoratedView.Expandable#collapseAll()
	 */
	@Override
	public void collapseAll() {
		getTreeViewer().collapseAll();
	}

}
