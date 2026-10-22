package gaml.compiler.ui.wizards.species;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import gaml.compiler.ui.wizards.species.SpeciesWizardContext.InheritedAttribute;

/**
 * Shared attribute editor used by the species, class and experiment wizards: a sortable Name/Type/From table of
 * inherited and local attributes, with detail fields (name, type, initial value, update, minimum and maximum) and
 * Add/Remove/Redefine. It edits a detached list; {@link #values()} returns the local declarations.
 */
public final class AttributeTable {
	private final List<SpeciesDraft.Variable> locals = new ArrayList<>();
	private final Map<String, InheritedAttribute> inherited = new LinkedHashMap<>();
	private final List<InheritedAttribute> shown = new ArrayList<>();
	private final List<Integer> localIndices = new ArrayList<>();
	private final Table table;
	private final Button add;
	private final Button remove;
	private final Button redefine;
	private final Map<Integer, Control> fields = new LinkedHashMap<>();
	private final Map<Integer, Label> fieldLabels = new LinkedHashMap<>();
	private final List<String> types;
	private final boolean editBody;
	private final Supplier<String> ownerName;
	private final Runnable onChange;
	private final Composite control;
	private boolean loading;
	private boolean filteringTypes;
	private int sortColumn = -1;
	private boolean ascending = true;

	/** Editable table of attributes with all local values; {@code initial} are the local declarations. */
	public AttributeTable(final Composite parent, final List<SpeciesDraft.Variable> initial,
			final Supplier<List<String>> typeNames, final Supplier<String> ownerName, final Runnable onChange) {
		this(parent, initial, typeNames, true, ownerName, onChange);
	}

	/** @param editable false shows the attributes read-only (no add, remove, redefine or edits) */
	public AttributeTable(final Composite parent, final List<SpeciesDraft.Variable> initial,
			final Supplier<List<String>> typeNames, final boolean editable, final Supplier<String> ownerName,
			final Runnable onChange) {
		locals.addAll(initial);
		types = List.copyOf(typeNames.get());
		editBody = editable;
		this.ownerName = Objects.requireNonNull(ownerName);
		this.onChange = Objects.requireNonNull(onChange);
		Composite body = composite(parent, 2);
		control = body;
		((GridLayout) body.getLayout()).makeColumnsEqualWidth = false;
		body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Composite list = composite(body, 1);
		((GridLayout) list.getLayout()).marginWidth = 0;
		list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		table = new Table(list, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL);
		table.setData("species.attributes", true);
		table.setHeaderVisible(true);
		table.setToolTipText("Attributes: Name identifies the attribute, Type defines its values, and From identifies its origin.");
		accessible(table, "Attributes");
		GridData tableData = new GridData(SWT.FILL, SWT.FILL, false, true);
		tableData.widthHint = 300;
		tableData.heightHint = 200;
		table.setLayoutData(tableData);
		for (String name : List.of("Name", "Type", "From")) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(WizardWidgets.labelText(name));
			column.setToolTipText(switch (name) {
				case "Name" -> "Attribute identifier.";
				case "Type" -> "Type of values stored in the attribute.";
				default -> "Species or skill providing the attribute; Redefined indicates a local override.";
			});
			column.setWidth("Name".equals(name) ? 125 : "Type".equals(name) ? 75 : 100);
			int index = table.getColumnCount() - 1;
			if (index == 0 || index == 2) {
				column.setToolTipText(column.getToolTipText() + " Click to sort; click again to reverse.");
				column.addListener(SWT.Selection, event -> {
					ascending = sortColumn != index || !ascending;
					sortColumn = index;
					table.setSortColumn(column);
					table.setSortDirection(ascending ? SWT.UP : SWT.DOWN);
					renderRows(selected() == null ? "" : selected().variable().name());
				});
			}
		}
		Composite detail = composite(body, 2);
		detail.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		Composite actions = composite(detail, 2);
		((GridLayout) actions.getLayout()).marginWidth = 0;
		actions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		add = action(actions, "Add...", "Add attribute");
		remove = action(actions, "Remove", "Remove selected attribute");
		redefine = new Button(detail, SWT.CHECK);
		redefine.setText(WizardWidgets.labelText("Redefine"));
		redefine.setToolTipText("Declare a local override of this inherited attribute. Uncheck to use its inherited values.");
		redefine.setData("species.redefine", true);
		redefine.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		List<String> labels = List.of("Name", "Type", "Initial value", "Update", "Function", "Minimum", "Maximum");
		List<String> descriptions = List.of(
				"Attribute name. Enter an existing name to select that attribute instead of creating a duplicate.",
				"Attribute type. Type its first letters to filter the available types.",
				"GAML expression providing the initial attribute value.",
				"GAML expression recomputing the attribute value on each simulation cycle.",
				"",
				"Lower bound expression for this int or float attribute.",
				"Upper bound expression for this int or float attribute.");
		for (int i = 0; i < labels.size(); i++) {
			int index = i;
			if (index == 4) continue;
			Label label = label(detail, labels.get(i), descriptions.get(i));
			label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			fieldLabels.put(index, label);
			Control control;
			if (index == 1) {
				Combo type = new Combo(detail, SWT.DROP_DOWN | SWT.V_SCROLL);
				type.setItems(types.toArray(String[]::new));
				type.addModifyListener(event -> {
					if (loading || filteringTypes) return;
					filterTypes(type);
					edit(index, type.getText());
				});
				type.addListener(SWT.Selection, event -> edit(index, type.getText()));
				control = type;
			} else {
				Text text = new Text(detail, SWT.BORDER | SWT.SINGLE);
				text.addModifyListener(event -> edit(index, text.getText()));
				control = text;
			}
			control.setData("species.attribute.field", i);
			control.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			accessible(control, labels.get(i));
			control.setToolTipText(descriptions.get(i));
			fields.put(index, control);
		}
		table.addListener(SWT.Selection, event -> select());
		redefine.addListener(SWT.Selection, event -> {
			InheritedAttribute item = selected();
			if (item == null) return;
			int index = localIndex(item.variable().name());
			if (redefine.getSelection() && index < 0) locals.add(item.variable());
			else if (!redefine.getSelection() && index >= 0) locals.remove(index);
			refresh(List.copyOf(inherited.values()));
			onChange.run();
		});
		add.setEnabled(editBody);
		add.addListener(SWT.Selection, event -> {
			String name = "my_attribute";
			int suffix = 2;
			while (localIndex(name) >= 0 || inherited.containsKey(name)) name = "my_attribute_" + suffix++;
			locals.add(new SpeciesDraft.Variable(name, "float", "", "", "", "", ""));
			refresh(List.copyOf(inherited.values()));
			for (int row = 0; row < localIndices.size(); row++)
				if (localIndices.get(row) == locals.size() - 1) table.setSelection(row);
			select();
			fields.get(0).setFocus();
			onChange.run();
		});
		remove.addListener(SWT.Selection, event -> {
			InheritedAttribute item = selected();
			if (item == null) return;
			int index = localIndices.get(table.getSelectionIndex());
			if (index >= 0) locals.remove(index);
			refresh(List.copyOf(inherited.values()));
			onChange.run();
		});
		table.addListener(SWT.KeyDown, event -> {
			if (event.keyCode == SWT.INSERT && add.getEnabled()) add.notifyListeners(SWT.Selection, null);
			if (event.keyCode == SWT.DEL && remove.getEnabled()) remove.notifyListeners(SWT.Selection, null);
		});
	}

	private void filterTypes(final Combo type) {
		filteringTypes = true;
		try {
			WizardWidgets.filterByPrefix(type, types);
		} finally {
			filteringTypes = false;
		}
	}

	private void updateOrdinalFields(final String type) {
		boolean ordinal = "int".equalsIgnoreCase(type) || "float".equalsIgnoreCase(type);
		for (int index = 5; index <= 6; index++) {
			Label label = fieldLabels.get(index);
			Control control = fields.get(index);
			label.setVisible(ordinal);
			((GridData) label.getLayoutData()).exclude = !ordinal;
			control.setVisible(ordinal);
			((GridData) control.getLayoutData()).exclude = !ordinal;
		}
		fields.get(0).getParent().layout(true, true);
	}

	private int localIndex(final String name) {
		for (int i = 0; i < locals.size(); i++) if (locals.get(i).name().equals(name)) return i;
		return -1;
	}

	private InheritedAttribute selected() {
		int index = table.getSelectionIndex();
		return index < 0 ? null : shown.get(index);
	}

	/** Shows {@code metadata} (inherited attributes) followed by the local ones not redefining them. */
	public void refresh(final List<InheritedAttribute> metadata) {
		InheritedAttribute previous = selected();
		String name = previous == null ? "" : previous.variable().name();
		inherited.clear();
		for (var item : metadata) inherited.put(item.variable().name(), item);
		shown.clear();
		localIndices.clear();
		shown.addAll(inherited.values());
		for (var item : shown) localIndices.add(localIndex(item.variable().name()));
		for (int i = 0; i < locals.size(); i++) {
			var local = locals.get(i);
			if (!inherited.containsKey(local.name())) {
				shown.add(new InheritedAttribute(local, ownerName.get().trim(), true));
				localIndices.add(i);
			}
		}
		renderRows(name);
		select();
	}

	private String source(final int row) {
		var item = shown.get(row);
		return localIndices.get(row) >= 0 && inherited.containsKey(item.variable().name())
				? "Redefined" : item.origin();
	}

	private void renderRows(final String name) {
		if (sortColumn >= 0) {
			List<Integer> order = new ArrayList<>();
			for (int row = 0; row < shown.size(); row++) order.add(row);
			java.util.Comparator<Integer> comparator = java.util.Comparator.comparing(
					row -> sortColumn == 0 ? shown.get(row).variable().name() : source(row),
					String.CASE_INSENSITIVE_ORDER);
			comparator = comparator.thenComparing(row -> shown.get(row).variable().name());
			order.sort(ascending ? comparator : comparator.reversed());
			var sorted = order.stream().map(shown::get).toList();
			var indices = order.stream().map(localIndices::get).toList();
			shown.clear();
			shown.addAll(sorted);
			localIndices.clear();
			localIndices.addAll(indices);
		}
		table.removeAll();
		int selected = 0;
		for (int i = 0; i < shown.size(); i++) {
			var item = shown.get(i);
			int index = localIndices.get(i);
			var variable = index < 0 ? item.variable() : locals.get(index);
			TableItem row = new TableItem(table, SWT.NONE);
			row.setText(new String[] { variable.name(), variable.type(), source(i) });
			if (name.equals(variable.name())) selected = i;
		}
		if (!shown.isEmpty()) table.setSelection(selected);
	}

	private void select() {
		loading = true;
		try {
			var item = selected();
			boolean builtIn = item != null && inherited.containsKey(item.variable().name());
			int index = item == null ? -1 : localIndices.get(table.getSelectionIndex());
			boolean editable = editBody && index >= 0;
			redefine.setVisible(builtIn);
			((GridData) redefine.getLayoutData()).exclude = !builtIn;
			redefine.setEnabled(editBody && builtIn && item.redefinable());
			redefine.setSelection(builtIn && index >= 0);
			remove.setEnabled(editable);
			SpeciesDraft.Variable variable = item == null
					? new SpeciesDraft.Variable("", "", "", "", "", "", "")
					: index >= 0 ? locals.get(index) : item.variable();
			String[] values = { variable.name(), variable.type(), variable.init(), variable.update(), variable.function(),
					variable.min(), variable.max() };
			Combo type = (Combo) fields.get(1);
			List<String> choices = withCurrent(types, values[1]);
			type.setItems(choices.toArray(String[]::new));
			for (int i : fields.keySet()) {
				Control field = fields.get(i);
				if (field instanceof Text text) text.setText(values[i]);
				else if (field instanceof Combo combo) combo.setText(values[i]);
				field.setEnabled(editable && (!builtIn || i >= 2));
			}
			updateOrdinalFields(values[1]);
			redefine.getParent().layout(true, true);
		} finally { loading = false; }
	}

	private void edit(final int field, final String value) {
		if (loading) return;
		var item = selected();
		if (item == null) return;
		int index = localIndices.get(table.getSelectionIndex());
		if (index < 0) return;
		if (field == 0) {
			for (int row = 0; row < shown.size(); row++) {
				if (row != table.getSelectionIndex() && table.getItem(row).getText(0).equals(value)) {
					table.setSelection(row);
					table.showSelection();
					select();
					table.setFocus();
					onChange.run();
					return;
				}
			}
		}
		var v = locals.get(index);
		String[] values = { v.name(), v.type(), v.init(), v.update(), v.function(), v.min(), v.max() };
		values[field] = value;
		SpeciesDraft.Variable updated = new SpeciesDraft.Variable(values[0], values[1], values[2], values[3],
				values[4], values[5], values[6]);
		locals.set(index, updated);
		shown.set(table.getSelectionIndex(), new InheritedAttribute(updated, item.origin(), item.redefinable()));
		table.getSelection()[0].setText(0, updated.name());
		table.getSelection()[0].setText(1, updated.type());
		if (field == 0 && sortColumn >= 0) renderRows(updated.name());
		if (field == 1) updateOrdinalFields(value);
		onChange.run();
	}

	/** Local (declared or redefined) attributes, in declaration order. */
	public List<SpeciesDraft.Variable> values() { return List.copyOf(locals); }

	/** The root control of the table and its detail fields. */
	public Composite control() { return control; }

	private static Composite composite(final Composite parent, final int columns) {
		return WizardWidgets.composite(parent, columns);
	}

	private static Label label(final Composite parent, final String text, final String description) {
		return WizardWidgets.label(parent, text, description);
	}

	private static Button action(final Composite parent, final String text, final String description) {
		return WizardWidgets.button(parent, text, description);
	}

	private static void accessible(final Control control, final String label) {
		WizardWidgets.accessible(control, label);
	}

	private static List<String> withCurrent(final List<String> choices, final String current) {
		var result = new ArrayList<>(choices);
		if (!result.contains(current)) result.add(current);
		return List.copyOf(result);
	}
}
