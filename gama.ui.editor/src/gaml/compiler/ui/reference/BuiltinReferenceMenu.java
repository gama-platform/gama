/*******************************************************************************************************
 *
 * BuiltinReferenceMenu.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;

import com.google.common.collect.Lists;

import gama.gaml.compilation.GAML;
import gama.gaml.compilation.kernel.GamaSkillRegistry;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.ModelDescription;
import gama.gaml.descriptions.OperatorProto;
import gama.gaml.descriptions.StatementDescription;
import gama.gaml.descriptions.SymbolProto;
import gama.gaml.descriptions.TypeDescription;
import gama.gaml.descriptions.VariableDescription;
import gama.gaml.interfaces.INamed;
import gama.gaml.types.Types;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import gaml.compiler.ui.templates.GamlTemplateFactory;

/**
 * The class EditToolbarTemplateMenu.
 *
 * @author drogoul
 * @since 5 déc. 2014
 *
 */
public class BuiltinReferenceMenu extends GamlReferenceMenu {

	@Override
	protected void fillMenu() {
		final List<TypeDescription> list = new ArrayList<>(ModelDescription.ROOT.getMicroSpecies().values());
		final List<String> speciesList = new ArrayList<>();
		Collections.sort(list, INamed.COMPARATOR);
		Menu m = sub("Built-in species");

		for (final TypeDescription species : list) {
			speciesList.add(species.getName());
			fillSpeciesSubmenu(sub(m, species.getName()), species);
		}
		final List<String> skills = new ArrayList<>(GamaSkillRegistry.INSTANCE.getSkillNames());
		Collections.sort(skills, IGNORE_CASE);
		m = sub("Skills");
		for (final String skill : skills) { fillSkillSubmenu(sub(m, skill), skill, false); }
		final List<String> controls = new ArrayList<>(GamaSkillRegistry.INSTANCE.getArchitectureNames());
		Collections.sort(controls, IGNORE_CASE);
		m = sub("Control architectures");
		for (final String skill : controls) { fillSkillSubmenu(sub(m, skill), skill, true); }
		final List<String> types = Lists.newArrayList(Types.getTypeNames());
		types.removeAll(speciesList);
		Collections.sort(types, IGNORE_CASE);
		m = sub("Types");
		final List<String> fileTypes = new ArrayList<>();
		for (final String type : types) { if (type.contains("_file")) { fileTypes.add(type); } }
		types.removeAll(fileTypes);
		for (final String type : types) { fillTypeSubmenu(sub(m, type), type); }
		m = sub("File types");
		for (final String type : fileTypes) { fillTypeSubmenu(sub(m, type), type); }
	}

	/**
	 * @param sub
	 * @param type
	 */
	private void fillTypeSubmenu(final Menu submenu, final String type) {
		action(submenu, "Insert new attribute with this type", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				applyTemplate(GamlTemplateFactory.attributeWithType(type));
			}

		});
		final Map<String, OperatorProto> getters = Types.get(type).getFieldGetters();
		final List<String> names = new ArrayList<>(getters.keySet());
		if (!names.isEmpty()) {
			Collections.sort(names);
			title(submenu, "Attributes");
			for (final String getter : names) { fillProtoSubMenu(sub(submenu, getter), getters.get(getter)); }
		}
	}

	/**
	 * Fill skill submenu.
	 *
	 * @param submenu
	 *            the submenu
	 * @param skill
	 *            the skill
	 * @param isControl
	 *            the is control
	 */
	private void fillSkillSubmenu(final Menu submenu, final String skill, final boolean isControl) {
		action(submenu, "Insert name", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				getEditor().insertText(skill);
			}

		});
		action(submenu, "Insert new species with this " + (isControl ? "control" : "skill"), new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (!isControl) {
					applyTemplate(GamlTemplateFactory.speciesWithSkill(skill));
				} else {
					applyTemplate(GamlTemplateFactory.speciesWithControl(skill));
				}
			}

		});
		final List<IDescription> vars = Lists.newArrayList(GamaSkillRegistry.INSTANCE.getVariablesForSkill(skill));
		Collections.sort(vars, INamed.COMPARATOR);
		if (!vars.isEmpty()) {
			title(submenu, "Attributes");
			for (final IDescription variable : vars) {
				fillIDescriptionSubMenu(sub(submenu, variable.getName() + " (" + variable.getGamlType() + ")"),
						variable);
			}
		}
		final List<IDescription> actions = Lists.newArrayList(GamaSkillRegistry.INSTANCE.getActionsForSkill(skill));
		Collections.sort(actions, INamed.COMPARATOR);
		if (!actions.isEmpty()) {
			title(submenu, "Primitives");
			for (final IDescription action : actions) {
				fillIDescriptionSubMenu(sub(submenu, action.getName()), action);
			}
		}
		if (isControl) {
			final List<SymbolProto> controls = new ArrayList<>(GAML.getStatementsForSkill(skill));
			Collections.sort(controls, INamed.COMPARATOR);
			if (!controls.isEmpty()) {
				title(submenu, "Control statements");
				for (final SymbolProto control : controls) {
					fillProtoSubMenu(sub(submenu, control.getName()), control);
				}
			}
		}
	}

	/**
	 * Fill proto sub menu.
	 *
	 * @param menu
	 *            the menu
	 * @param statement
	 *            the statement
	 */
	private void fillProtoSubMenu(final Menu menu, final SymbolProto statement) {
		action(menu, "Insert statement name", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				getEditor().insertText(statement.getName());
			}

		});

	}

	/**
	 * Fill proto sub menu.
	 *
	 * @param menu
	 *            the menu
	 * @param attribute
	 *            the attribute
	 */
	private void fillProtoSubMenu(final Menu menu, final OperatorProto attribute) {
		action(menu, "Insert attribute name", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				getEditor().insertText(attribute.getName());
			}

		});

	}

	/**
	 * Fill species submenu.
	 *
	 * @param submenu
	 *            the submenu
	 * @param species
	 *            the species
	 */
	private void fillSpeciesSubmenu(final Menu submenu, final TypeDescription species) {
		action(submenu, "Insert name", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				getEditor().insertText(species.getName());
			}

		});
		action(submenu, "Insert new child species", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				applyTemplate(GamlTemplateFactory.speciesWithParent(species));
			}

		});
		final List<String> vars = new ArrayList<>(species.getAttributeNames());
		Collections.sort(vars, IGNORE_CASE);
		if (!vars.isEmpty()) {
			title(submenu, "Attributes");
			for (final String v : vars) {
				final VariableDescription variable = species.getAttribute(v);
				if (!variable.isSyntheticSpeciesContainer() && variable.getOriginName().endsWith(species.getName())) {
					fillIDescriptionSubMenu(sub(submenu, v + " (" + variable.getGamlType() + ")"), variable);
				}
			}
		}
		final List<String> actions = new ArrayList<>(species.getActionNames());
		Collections.sort(actions, IGNORE_CASE);
		if (!actions.isEmpty()) {
			title(submenu, "Primitives");
			for (final String v : actions) {
				final StatementDescription prim = species.getAction(v);
				if (prim.getOriginName().endsWith(species.getName())) {
					fillIDescriptionSubMenu(sub(submenu, v), prim);
				}
			}
		}
	}

	/**
	 * Fill I description sub menu.
	 *
	 * @param submenu
	 *            the submenu
	 * @param v
	 *            the v
	 */
	private void fillIDescriptionSubMenu(final Menu submenu, final IDescription v) {
		final boolean isVar = v instanceof VariableDescription;
		action(submenu, "Insert name", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				getEditor().insertText(v.getName());
			}

		});
		if (isVar) {
			action(submenu, "Insert redefinition", new SelectionAdapter() {

				@Override
				public void widgetSelected(final SelectionEvent e) {
					getEditor().insertText(v.serializeToGaml(true));
				}

			});
		} else {
			action(submenu, "Insert call", new SelectionAdapter() {

				@Override
				public void widgetSelected(final SelectionEvent e) {
					getEditor().applyTemplate(GamlTemplateFactory.callToAction((StatementDescription) v));
				}

			});
		}
	}

	@Override
	protected void openView() {}

	/**
	 * @see gaml.compiler.gaml.ui.reference.GamlReferenceMenu#getImage()
	 */
	@Override
	protected Image getImage() { return GamaIcon.named(IGamaIcons.REFERENCE_BUILTIN).image(); }

	/**
	 * @see gaml.compiler.gaml.ui.reference.GamlReferenceMenu#getTitle()
	 */
	@Override
	protected String getTitle() { return "Built-In structures"; }

	/**
	 * @see gaml.compiler.ui.reference.GamlReferenceMenu#isDynamic()
	 */
	@Override
	protected boolean isDynamic() { return false; }

}
