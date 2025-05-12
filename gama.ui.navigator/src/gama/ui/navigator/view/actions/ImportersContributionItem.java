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

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import gama.core.runtime.GAMA;
import gama.dev.DEBUG;
import gama.ui.navigator.view.contents.ResourceManager;
import gama.ui.navigator.view.contents.WrappedGamaFile;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.Selector;
import gaml.compiler.gaml.indexer.GamlResourceIndexer;

/**
 * The Class ImportersContributionItem.
 */
public class ImportersContributionItem extends ContributionItem {

	static {
		DEBUG.ON();
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
			final Set<URI> imp =
					directImportersOf(createPlatformResourceURI(file.getResource().getFullPath().toString(), true));
			if (imp.isEmpty()) {
				MenuItem item2 = new MenuItem(menu, SWT.PUSH);
				item2.setText("No importers");
				item2.setEnabled(false);
				return;
			}
			if (!ResourceManager.getInstance().hasBeenBuiltOnce()) {
				if (DEBUG.IS_ON()) { DEBUG.OUT("The workspace is not already built. Proceeding with building"); }
				final IWorkspace workspace = ResourcesPlugin.getWorkspace();
				try {
					GamlResourceIndexer.eraseIndex();
					workspace.build(IncrementalProjectBuilder.CLEAN_BUILD, new NullProgressMonitor());
				} catch (final CoreException ex) {
					ex.printStackTrace();
				}
			} else if (DEBUG.IS_ON()) { DEBUG.OUT("The workspace is already built. Proceeding with imports"); }
			while (!ResourceManager.getInstance().hasBeenBuiltOnce()) {}
			for (final URI uri : imp) {
				MenuItem item2 = new MenuItem(menu, SWT.PUSH);
				item2.setText(URI.decode(uri.lastSegment()));
				item2.setImage(GamaIcon.named(IGamaIcons.FILE_ICON).image());
				item2.addSelectionListener((Selector) eee -> GAMA.getGui().editModel(uri));
			}

		});

	}

	@Override
	public boolean isDynamic() { return true; }

}
