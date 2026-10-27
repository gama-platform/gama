/*******************************************************************************************************
 *
 * EditorsGroup.java, in gama.ui.shared.shared, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.shared.parameters;

import static org.eclipse.jface.layout.GridLayoutFactory.fillDefaults;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * The Class EditorsGroup.
 */
public class EditorsGroup extends Composite {

	/**
	 * Instantiates a new editors group.
	 *
	 * @param parent
	 *            the parent
	 */
	public EditorsGroup(final Composite parent) {
		this(parent, SWT.NONE);
	}

	/**
	 * Instantiates a new editors group.
	 *
	 * @param parent
	 *            the parent
	 * @param style
	 *            the style
	 */
	public EditorsGroup(final Composite parent, final int style) {
		super(parent, style | SWT.INHERIT_DEFAULT);
		if (parent.getLayout() instanceof GridLayout) { GridDataFactory.fillDefaults().grab(true, true).applyTo(this); }
		fillDefaults().numColumns(3).spacing(0, 0).extendedMargins(5, 5, 5, 5).equalWidth(false).applyTo(this);
		// GamaColors.setBackground(this, parent.getBackground());
		// Necessary to force SWT to "reskin" and give the right background to the composite (issue in the CSS engine)
		computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
	}

	/** Value labels and buttons of the editors that display a value, sized identically so that values align. */
	private final List<ValueColumn> valueColumns = new ArrayList<>();

	/** The value column of one editor. */
	private record ValueColumn(Composite owner, GridData value, GridData buttons) {}

	/** The widths shared by all the value columns of this group. */
	private int valueWidth, buttonsWidth;

	/**
	 * Registers the layout data of a value label and of its buttons, and makes all registered columns share the
	 * largest widths. Only done when editors are created, so value updates never relayout the group.
	 */
	void alignValueColumn(final Composite owner, final GridData value, final int valueHint, final GridData buttons,
			final int buttonsHint) {
		final ValueColumn column = new ValueColumn(owner, value, buttons);
		valueColumns.add(column);
		owner.addDisposeListener(e -> valueColumns.remove(column));
		final boolean grown = valueHint > valueWidth || buttonsHint > buttonsWidth;
		valueWidth = Math.max(valueWidth, valueHint);
		buttonsWidth = Math.max(buttonsWidth, buttonsHint);
		for (final ValueColumn c : valueColumns) {
			c.value.widthHint = valueWidth;
			c.buttons.widthHint = buttonsWidth;
			if (grown && c.owner != owner && !c.owner.isDisposed()) { c.owner.requestLayout(); }
		}
	}

}
