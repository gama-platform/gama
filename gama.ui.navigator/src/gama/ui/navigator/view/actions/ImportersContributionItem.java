/*******************************************************************************************************
 *
 * ImportersContributionItem.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.actions;

import static gaml.compiler.gaml.indexer.GamlResourceIndexer.directImportersOf;
import static org.eclipse.emf.common.util.URI.createPlatformResourceURI;

import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import gama.core.runtime.GAMA;
import gama.ui.navigator.view.contents.WrappedGamaFile;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.Selector;

/**
 * The Class ImportersContributionItem.
 */
public class ImportersContributionItem extends ContributionItem {

	/**
	 * Instantiates a new importers contribution item.
	 */
	public ImportersContributionItem() {}

	/**
	 * Instantiates a new importers contribution item.
	 *
	 * @param id
	 *            the id
	 */
	public ImportersContributionItem(final String id) {
		super(id);
	}

	@Override
	public void fill(final Menu parentMenu, final int index) {
		IStructuredSelection sel = WorkbenchHelper.getSelection();
		if (sel == null || sel.isEmpty() || !(sel.getFirstElement() instanceof WrappedGamaFile file)) return;
		final Set<URI> imp =
				directImportersOf(createPlatformResourceURI(file.getResource().getFullPath().toString(), true));
		if (imp.isEmpty()) return;
		MenuItem item = new MenuItem(parentMenu, SWT.CASCADE);
		final Menu menu = new Menu(item);
		item.setMenu(menu);
		item.setText("Imported by...");
		item.setImage(GamaIcon.named(IGamaIcons.IMPORTED_IN).image());
		item.setToolTipText("Lists all the models that directly import this model.");
		for (final URI uri : imp) {
			item = new MenuItem(menu, SWT.PUSH);
			item.setText(URI.decode(uri.lastSegment()));
			item.setImage(GamaIcon.named(IGamaIcons.FILE_ICON).image());
			item.addSelectionListener((Selector) e -> GAMA.getGui().editModel(uri));
		}
	}

	@Override
	public boolean isDynamic() { return true; }

}
