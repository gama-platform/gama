/*******************************************************************************************************
 *
 * EditorToolbar.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.parameters;

import static gama.ui.shared.interfaces.IParameterEditor.BROWSE;
import static gama.ui.shared.interfaces.IParameterEditor.CHANGE;
import static gama.ui.shared.interfaces.IParameterEditor.DEFINE;
import static gama.ui.shared.interfaces.IParameterEditor.EDIT;
import static gama.ui.shared.interfaces.IParameterEditor.INSPECT;
import static gama.ui.shared.interfaces.IParameterEditor.MINUS;
import static gama.ui.shared.interfaces.IParameterEditor.PLUS;
import static gama.ui.shared.interfaces.IParameterEditor.REVERT;
import static gama.ui.shared.interfaces.IParameterEditor.VALUE;
import static gama.ui.shared.resources.IGamaIcons.SMALL_BROWSE;
import static gama.ui.shared.resources.IGamaIcons.SMALL_CHANGE;
import static gama.ui.shared.resources.IGamaIcons.SMALL_EDIT;
import static gama.ui.shared.resources.IGamaIcons.SMALL_INSPECT;
import static gama.ui.shared.resources.IGamaIcons.SMALL_MINUS;
import static gama.ui.shared.resources.IGamaIcons.SMALL_PLUS;
import static gama.ui.shared.resources.IGamaIcons.SMALL_REVERT;
import static gama.ui.shared.resources.IGamaIcons.SMALL_UNDEFINE;
import static gama.ui.shared.views.toolbar.GamaCommand.build;

import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import gama.core.kernel.experiment.IParameter;
import gama.ui.shared.interfaces.IParameterEditor;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.views.toolbar.GamaCommand;

/**
 * The Class EditorToolbar.
 */
public class EditorToolbar<T> {

	/**
	 * The Class Item.
	 */
	static class Item {

		/** The label. */
		final Label label;

		/** The listener. */
		final MouseListener listener;

		/** The command. */
		final GamaCommand command;

		/**
		 * Instantiates a new item.
		 *
		 * @param parent
		 *            the parent
		 * @param c
		 *            the c
		 * @param l
		 *            the l
		 */
		Item(final Composite parent, final GamaCommand c, final MouseListener l) {
			command = c;
			listener = l;
			label = new Label(parent, SWT.NONE);
			if (c.text() != null) { label.setText(c.text()); }
			label.setToolTipText(c.tooltip());
			enable(true);
		}

		/**
		 * Enable.
		 *
		 * @param enable
		 *            the enable
		 */
		void enable(final boolean enable) {

			if (command.image() != null) {
				label.setImage(
						enable ? GamaIcon.named(command.image()).image() : GamaIcon.named(command.image()).disabled());
			}
			label.removeMouseListener(listener);
			if (enable) { label.addMouseListener(listener); }
		}

		/**
		 * Checks if is disposed.
		 *
		 * @return true, if is disposed
		 */
		public boolean isDisposed() { return label != null && label.isDisposed(); }
	}

	/** The editor. */
	final AbstractEditor<T> editor;

	/** The active. */
	boolean active = true;

	/** The Constant commands. */
	protected static final GamaCommand[] commands = new GamaCommand[10];

	/** The items. */
	protected final Item[] items = new Item[9]; /* 10 */

	/** The group. */
	final Composite group;

	static {
		commands[REVERT] = build(SMALL_REVERT, null, "Revert to original value", null);
		commands[PLUS] = build(SMALL_PLUS, null, "Increment the value", null);
		commands[MINUS] = build(SMALL_MINUS, null, "Decrement the value ", null);
		commands[EDIT] = build(SMALL_EDIT, null, "Edit the parameter", null);
		commands[INSPECT] = build(SMALL_INSPECT, null, "Inspect the agent", null);
		commands[BROWSE] = build(SMALL_BROWSE, null, "Browse the list of agents", null);
		commands[CHANGE] = build(SMALL_CHANGE, null, "Choose another agent", null);
		commands[DEFINE] = build(SMALL_UNDEFINE, null, "Set the parameter to undefined", null);
		commands[VALUE] = build(null, "", "Value of the parameter", null);
		// commands[SAVE] = build("small.save", null, "Save the values", null);
	}

	/**
	 * Instantiates a new editor toolbar.
	 *
	 * @param editor
	 *            the editor
	 * @param composite
	 *            the composite
	 */
	EditorToolbar(final AbstractEditor<T> editor, final Composite composite) {
		this.editor = editor;
		group = new Composite(composite, SWT.NONE);
		final GridData d = new GridData(SWT.END, SWT.CENTER, false, false);
		group.setLayoutData(d);
		final RowLayout id = RowLayoutFactory.fillDefaults().center(true).spacing(2).margins(0, 0).type(SWT.HORIZONTAL)
				.fill(true).pack(true).extendedMargins(0, 0, 0, 0).justify(false).wrap(false).create();
		final RowData gd = RowDataFactory.swtDefaults().create();
		group.setLayout(id);
		IParameter p = editor.getParam();
		if (p == null || p.isEditable()) {
			for (final int i : editor.getToolItems()) {
				MouseListener listener = new MouseAdapter() {

					@Override
					public void mouseDown(final MouseEvent e) {
						execute(i, 0);
					}
				};
				items[i] = new Item(group, commands[i], listener);
				items[i].label.setLayoutData(RowDataFactory.copyData(gd));
			}
		}
		Color color = editor.parent.getBackground();
		GamaColors.setBackground(color, group);
		GamaColors.setBackground(color, group.getChildren());
	}

	/**
	 * Execute.
	 *
	 * @param code
	 *            the code
	 * @param detail
	 *            the detail
	 */
	private void execute(final int code, final int detail) {
		switch (code) {
			case IParameterEditor.REVERT:
				editor.modifyAndDisplayValue(editor.applyRevert());
				break;
			case IParameterEditor.PLUS:
				editor.modifyAndDisplayValue(editor.applyPlus());
				break;
			case IParameterEditor.MINUS:
				editor.modifyAndDisplayValue(editor.applyMinus());
				break;
			case IParameterEditor.EDIT:
				editor.applyEdit();
				break;
			case IParameterEditor.INSPECT:
				editor.applyInspect();
				break;
			case IParameterEditor.BROWSE:
				editor.applyBrowse();
				break;
			case IParameterEditor.CHANGE:
				// if (detail != SWT.ARROW) return;
				editor.applyChange();
				break;
			case IParameterEditor.DEFINE:
				editor.applyDefine();
				break;
			// case IParameterEditor.SAVE:
			// editor.applySave();
		}
	}

	/**
	 * Enable.
	 *
	 * @param i
	 *            the i
	 * @param enable
	 *            the enable
	 */
	public void enable(final int i, final boolean enable) {
		if (!active && enable) return;
		final var c = items[i];
		if (c == null) return;
		c.enable(enable);
	}

	/**
	 * Update.
	 */
	protected void update() {
		final var c = items[IParameterEditor.REVERT];
		if (c != null && !c.isDisposed()) { c.enable(editor.isValueModified()); }
	}

	/**
	 * Update value.
	 *
	 * @param s
	 *            the s
	 */
	public void updateValue(final String s) {
		final var c = items[IParameterEditor.VALUE];
		if (c != null && !c.isDisposed()) { c.label.setText(s); }
	}

	/**
	 * Sets the active.
	 *
	 * @param active
	 *            the new active
	 */
	public void setActive(final Boolean active) {
		this.active = active;
		for (final Item t : items) {
			if (t == null) { continue; }
			t.enable(active);
		}
		if (active) { update(); }
	}

	/**
	 * Gets the item.
	 *
	 * @param item
	 *            the item
	 * @return the item
	 */
	public Label getItem(final int item) {
		final var c = items[item];
		if (c == null) return null;
		return c.label;
	}

	/**
	 * Sets the horizontal alignment.
	 *
	 * @param lead
	 *            the new horizontal alignment
	 */
	public void setHorizontalAlignment(final int lead) {
		if (group.isDisposed()) return;
		((GridData) group.getLayoutData()).horizontalAlignment = lead;
	}

	/**
	 * Checks if is disposed.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is disposed
	 * @date 21 févr. 2024
	 */
	public boolean isDisposed() {
		for (Item i : items) { if (i != null && i.isDisposed()) return true; }
		return false;
	}

}
