/*******************************************************************************************************
 *
 * GamlOutlinePage.java, in gama.ui.shared.modeling, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.outline;

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
public class GamlOutlinePage extends OutlinePage implements IToolbarDecoratedView {

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
		toolbar.wipe(SWT.RIGHT, true);
		for (IContributionItem item : tbm.getItems()) { toolbar.item(item, SWT.RIGHT); }
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
	//
	// @Override
	// public void setToogle(final Action toggle) {}

}
