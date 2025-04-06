/*******************************************************************************************************
 *
 * EditorMenu.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.editor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

import gama.core.runtime.GAMA;
import gama.dev.DEBUG;
import gama.gaml.compilation.ast.ISyntacticElement;
import gama.ui.application.workbench.ThemeHelper;
import gama.ui.shared.access.ModelsFinder;
import gama.ui.shared.menus.GamaMenu;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.WorkbenchHelper;
import gaml.compiler.gaml.indexer.GamlResourceIndexer;
import gaml.compiler.gaml.resource.GamlResource;
import gaml.compiler.ui.editor.GamlEditor;
import gaml.compiler.ui.reference.BuiltinReferenceMenu;
import gaml.compiler.ui.reference.ColorReferenceMenu;
import gaml.compiler.ui.reference.OperatorsReferenceMenu;
import gaml.compiler.ui.reference.TemplateReferenceMenu;

/**
 * The Class EditorMenu.
 */
public class EditorMenu extends ContributionItem implements IWorkbenchContribution {

	static {
		DEBUG.ON();
	}

	@Override
	public void initialize(final IServiceLocator serviceLocator) {}

	@Override
	public void fill(final Menu m, final int index) {
		final MenuItem menuItem = new MenuItem(m, SWT.CASCADE);
		menuItem.setText("Model");
		final Menu menu = new Menu(menuItem);
		if (menuItem.getMenu() != null) { menuItem.getMenu().dispose(); }
		menuItem.setMenu(menu);
		menu.addListener(SWT.Show, e -> {
			for (final MenuItem item : menu.getItems()) { item.dispose(); }
			if (getEditor() != null) {
				GamaMenu.separate(menu, "Insert");
				GamaMenu.separate(menu);
				createReferenceMenus(menu);
				GamaMenu.separate(menu);
				GamaMenu.separate(menu, "Model");
				GamaMenu.separate(menu);
				createUsedIn(menu);
				createOtherExperiments(menu);
				GamaMenu.separate(menu);
				createValidate(menu);
			}
		});

	}

	/**
	 * @param menu
	 */
	private void createReferenceMenus(final Menu menu) {
		new TemplateReferenceMenu().installSubMenuIn(menu);
		new BuiltinReferenceMenu().installSubMenuIn(menu);
		new OperatorsReferenceMenu().installSubMenuIn(menu);
		new ColorReferenceMenu().installSubMenuIn(menu);
	}

	/**
	 * @param menu
	 */
	private void createOtherExperiments(final Menu menu) {

		final MenuItem usedIn = new MenuItem(menu, SWT.CASCADE);
		usedIn.setText(" Other experiments...");
		usedIn.setImage(GamaIcon.named(IGamaIcons.OTHER_EXPERIMENTS).image());
		final Menu sub = new Menu(usedIn);
		usedIn.setMenu(sub);
		sub.addListener(SWT.Show, e -> {
			for (final MenuItem item : sub.getItems()) { item.dispose(); }
			createOtherSubMenu(sub, getEditor());
		});

	}

	/**
	 * Creates the other sub menu.
	 *
	 * @param parentMenu
	 *            the parent menu
	 * @param editor
	 *            the editor
	 * @return the menu
	 */
	public static Menu createOtherSubMenu(final Menu parentMenu, final GamlEditor editor) {
		final Map<URI, List<String>> map = grabProjectModelsAndExperiments(editor);
		if (map.isEmpty()) {
			final MenuItem nothing = new MenuItem(parentMenu, SWT.PUSH);
			nothing.setText("No experiments defined");
			nothing.setEnabled(false);
			return parentMenu;
		}
		for (final URI uri : map.keySet()) {
			final MenuItem modelItem = new MenuItem(parentMenu, SWT.CASCADE);
			modelItem.setText(URI.decode(uri.lastSegment()));
			modelItem.setImage(GamaIcon.named(IGamaIcons.FILE_ICON).image());
			final Menu expMenu = new Menu(modelItem);
			modelItem.setMenu(expMenu);
			final List<String> expNames = map.get(uri);
			for (final String name : expNames) {
				final MenuItem expItem = new MenuItem(expMenu, SWT.PUSH);
				expItem.setText(name);
				expItem.setData("uri", uri);
				expItem.setData("exp", name);
				expItem.setImage(
						GamaIcon.named(ThemeHelper.isDark() ? IGamaIcons.BUTTON_GUI : IGamaIcons.MENU_GUI).image());
				expItem.addSelectionListener(OtherAdapter);
			}
		}
		return parentMenu;
	}

	/**
	 * Grab project models and experiments.
	 *
	 * @param editor
	 *            the editor
	 * @return the map
	 */
	private static Map<URI, List<String>> grabProjectModelsAndExperiments(final GamlEditor editor) {
		final Map<URI, List<String>> map = new TreeMap<>(Comparator.comparing(URI::lastSegment));
		editor.getDocument().readOnly(new IUnitOfWork.Void<XtextResource>() {

			@Override
			public void process(final XtextResource resource) throws Exception {
				final String platformString = resource.getURI().toPlatformString(true);
				final IFile myFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(platformString));
				final IProject proj = myFile.getProject();
				// AD Addresses Issue 796 by passing null to the "without"
				// parameter
				final List<URI> resources = ModelsFinder.getAllGamaURIsInProject(proj);
				final ResourceSet rs = editor.resourceSetProvider.get(proj);
				for (final URI uri : resources) {
					final GamlResource xr = (GamlResource) rs.getResource(uri, true);
					if (!xr.hasErrors()) {
						final ISyntacticElement el = xr.getSyntacticContents();
						if (el != null) {
							el.visitExperiments(element -> {

								if (!map.containsKey(uri)) { map.put(uri, new ArrayList<>()); }
								map.get(uri).add(element.getName());

							});
						}
					}
				}

			}
		});
		return map;
	}

	/**
	 * @param menu
	 */
	private void createUsedIn(final Menu menu) {
		final MenuItem usedIn = new MenuItem(menu, SWT.CASCADE);
		usedIn.setText(" Imported in...");
		usedIn.setImage(GamaIcon.named(IGamaIcons.IMPORTED_IN).image());
		final Menu sub = new Menu(usedIn);
		usedIn.setMenu(sub);
		sub.addListener(SWT.Show, e -> {
			for (final MenuItem item : sub.getItems()) { item.dispose(); }
			createImportedSubMenu(sub, getEditor());
		});
	}

	/** The Constant UsedInAdapter. */
	private static final SelectionAdapter UsedInAdapter = new SelectionAdapter() {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			final MenuItem mi = (MenuItem) e.widget;
			final URI uri = (URI) mi.getData("uri");
			GAMA.getGui().editModel(uri);
		}
	};

	/** The Constant OtherAdapter. */
	private static final SelectionAdapter OtherAdapter = new SelectionAdapter() {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			final MenuItem mi = (MenuItem) e.widget;
			final URI uri = (URI) mi.getData("uri");
			final String exp = (String) mi.getData("exp");
			if (uri != null && exp != null) { GAMA.getGui().runModel(uri, exp); }
		}
	};

	/**
	 * Creates the imported sub menu.
	 *
	 * @param parentMenu
	 *            the parent menu
	 * @param editor
	 *            the editor
	 * @return the menu
	 */
	public static Menu createImportedSubMenu(final Menu parentMenu, final GamlEditor editor) {
		final Set<URI> importers = getImporters(editor);
		if (importers.isEmpty()) {
			final MenuItem nothing = new MenuItem(parentMenu, SWT.PUSH);
			nothing.setText("No importers");
			nothing.setEnabled(false);
			return parentMenu;
		}
		for (final URI uri : importers) {
			final MenuItem modelItem = new MenuItem(parentMenu, SWT.PUSH);
			modelItem.setText(URI.decode(uri.lastSegment()));
			modelItem.setImage(GamaIcon.named(IGamaIcons.FILE_ICON).image());
			modelItem.setData("uri", uri);
			modelItem.addSelectionListener(UsedInAdapter);
		}
		return parentMenu;
	}

	/**
	 * Gets the importers.
	 *
	 * @param editor
	 *            the editor
	 * @return the importers
	 */
	private static Set<URI> getImporters(final GamlEditor editor) {
		final Set<URI> result = new HashSet<>();
		editor.getDocument().readOnly(new IUnitOfWork.Void<XtextResource>() {

			@Override
			public void process(final XtextResource resource) throws Exception {
				Set<URI> uris = GamlResourceIndexer.directImportersOf(resource.getURI());
				// DEBUG.OUT("Importers of " + resource.getURI().lastSegment() + ": " + uris);
				result.addAll(uris);
			}
		});
		return result;
	}

	/**
	 * @param menu
	 */
	private void createValidate(final Menu menu) {
		final MenuItem validate = new MenuItem(menu, SWT.PUSH);
		validate.setText(" Validate");
		validate.setImage(GamaIcon.named("build/build.project").image());
		validate.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				WorkbenchHelper.runCommand("gaml.compiler.gaml.Gaml.validate");
			}
		});

	}

	/**
	 * Gets the editor.
	 *
	 * @return the editor
	 */
	GamlEditor getEditor() { return (GamlEditor) WorkbenchHelper.getActiveEditor(); }
}
