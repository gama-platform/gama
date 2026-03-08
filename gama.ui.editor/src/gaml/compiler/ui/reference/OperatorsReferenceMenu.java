/*******************************************************************************************************
 *
 * OperatorsReferenceMenu.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.templates.Template;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import gama.api.compilation.artefacts.IArtefact;
import gama.api.gaml.GAML;
import gama.api.gaml.types.Signature;
import gama.api.utils.prefs.GamaPreferences;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import gaml.compiler.gaml.prototypes.OperatorArtefact;
import gaml.compiler.ui.templates.GamlTemplateFactory;

/**
 * The class EditToolbarTemplateMenu.
 *
 * @author drogoul
 * @since 5 déc. 2014
 *
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class OperatorsReferenceMenu extends GamlReferenceMenu {

	/** The by name. */

	@Override
	protected void fillMenu() {
		final boolean byCategory = GamaPreferences.Modeling.OPERATORS_MENU_SORT.getValue();
		action(mainMenu, byCategory ? "Sort by name" : "Sort by category",
				e -> GamaPreferences.Modeling.OPERATORS_MENU_SORT.setValue(null, !byCategory));
		sep();
		if (byCategory) {
			fillMenuByCategory();
		} else {
			fillMenuByName();
		}
	}

	/**
	 * Fill menu by name.
	 */
	protected void fillMenuByName() {
		final List<String> nn = new ArrayList(GAML.getOperatorsNames());
		Collections.sort(nn, IGNORE_CASE);
		for (final String name : nn) {
			final List<IArtefact.Operator> protos = new ArrayList<>();
			for (final Signature sig : GAML.getOperatorsNamed(name).keySet()) {
				final IArtefact.Operator proto = GAML.getOperatorsNamed(name).get(sig);
				if (proto.getDeprecated() == null) { protos.add(proto); }
			}
			if (protos.isEmpty()) { continue; }
			final Menu name_menu = sub(name);
			for (final IArtefact.Operator proto : protos) {
				final Template t = GamlTemplateFactory.from(proto);
				final MenuItem item = action(name_menu, "(" + proto.getSignature().asPattern(false) + ") -> "
						+ proto.getReturnType().serializeToGaml(true), new SelectionAdapter() {

							@Override
							public void widgetSelected(final SelectionEvent event) {
								applyTemplate(t);
							}
						});
				item.setToolTipText(t.getDescription());
			}
		}
	}

	/**
	 * Fill menu by category.
	 */
	protected void fillMenuByCategory() {
		final Map<String, Map<String, Map<IArtefact.Operator, Template>>> categories = new LinkedHashMap();
		final List<String> nn = new ArrayList(GAML.getOperatorsNames());
		Collections.sort(nn, IGNORE_CASE);
		for (final String name : nn) {
			final Map<Signature, IArtefact.Operator> ops = GAML.getOperatorsNamed(name);
			for (final Signature sig : ops.keySet()) {
				final IArtefact.Operator proto = ops.get(sig);
				if (proto.getDeprecated() != null) { continue; }
				final String category = proto.getCategory().replace("-related", "");
				Map<String, Map<IArtefact.Operator, Template>> names = categories.get(category);
				if (names == null) {
					names = new LinkedHashMap();
					categories.put(category, names);
				}
				Map<IArtefact.Operator, Template> templates = names.get(name);
				if (templates == null) {
					templates = new LinkedHashMap();
					names.put(name, templates);
				}
				templates.put(proto, GamlTemplateFactory.from(proto));
			}
		}
		final List<String> cc = new ArrayList(categories.keySet());
		Collections.sort(cc, IGNORE_CASE);
		for (final String category : cc) {
			final Menu category_menu = sub(category);
			final List<String> nn2 = new ArrayList(categories.get(category).keySet());
			Collections.sort(nn2, IGNORE_CASE);
			for (final String name : nn2) {
				final List<OperatorArtefact> protos = new ArrayList(categories.get(category).get(name).keySet());
				//
				final Menu name_menu = sub(category_menu, name);
				for (final OperatorArtefact proto : protos) {
					final Template t = categories.get(category).get(name).get(proto);
					final MenuItem item = action(name_menu,
							"(" + proto.signature.asPattern(false) + ") -> " + proto.returnType.serializeToGaml(true),
							new SelectionAdapter() {

								@Override
								public void widgetSelected(final SelectionEvent event) {
									applyTemplate(t);
								}
							});
					item.setToolTipText(t.getDescription());
				}

			}
		}

	}

	@Override
	protected void openView() {}

	/**
	 * @see gaml.compiler.gaml.ui.reference.GamlReferenceMenu#getImage()
	 */
	@Override
	protected Image getImage() { return GamaIcon.named(IGamaIcons.REFERENCE_OPERATORS).image(); }

	/**
	 * @see gaml.compiler.gaml.ui.reference.GamlReferenceMenu#getTitle()
	 */
	@Override
	protected String getTitle() { return "Operators"; }

	/**
	 * @see gaml.compiler.ui.reference.GamlReferenceMenu#isDynamic()
	 */
	@Override
	protected boolean isDynamic() { return false; }

}
