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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import gama.core.runtime.GAMA;
import gama.dev.DEBUG;
import gama.ui.navigator.view.contents.VirtualContent.IContentVisitor;
import gama.ui.navigator.view.contents.WrappedGamaFile;
import gama.ui.navigator.view.contents.WrappedProject;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.Selector;

/**
 * The Class ImportersContributionItem.
 */
public class ImportersContributionItem extends ContributionItem {

	static {
		DEBUG.OFF();
	}

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
	public void fill(final Menu menu, final int index) {
		IStructuredSelection sel = WorkbenchHelper.getSelection();
		if (sel == null || sel.isEmpty() || !(sel.getFirstElement() instanceof WrappedGamaFile file)) return;
		menu.addListener(SWT.Show, e -> {
			for (final MenuItem i : menu.getItems()) { i.dispose(); }
			WrappedProject project = file.getProject();
			final List<WrappedGamaFile> imp = new ArrayList<>();
			project.accept((IContentVisitor) wf -> {
				if (wf instanceof WrappedGamaFile wgf && wgf.hasImport(file)) { imp.add(wgf); }
				return true;
			});
			if (imp.isEmpty()) {
				MenuItem item2 = new MenuItem(menu, SWT.PUSH);
				item2.setText("No importers");
				item2.setEnabled(false);
			} else {
				for (final WrappedGamaFile r : imp) {
					MenuItem item2 = new MenuItem(menu, SWT.PUSH);
					item2.setText(r.getName());
					item2.setImage(GamaIcon.named(IGamaIcons.FILE_ICON).image());
					item2.addSelectionListener((Selector) eee -> GAMA.getGui().editModel(r));
				}
			}

		});

	}

	@Override
	public boolean isDynamic() { return true; }

}
