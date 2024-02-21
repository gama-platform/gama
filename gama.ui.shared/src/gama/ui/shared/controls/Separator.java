/*******************************************************************************************************
 *
 * Separator.java, in gama.ui.shared.shared, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.shared.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

/**
 * The Class Separator.
 */
public class Separator extends WorkbenchWindowControlContribution {

	/**
	 * Instantiates a new separator.
	 */
	public Separator() {}

	/**
	 * Instantiates a new separator.
	 *
	 * @param id the id
	 */
	public Separator(final String id) {
		super(id);
	}

	@Override
	protected Control createControl(final Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setSize(18, 32);
		return label;
	}

	@Override
	protected int computeWidth(final Control control) {
		return control.computeSize(18, 32).x;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isGroupMarker() {
		return false;
	}

	@Override
	public boolean isSeparator() {
		return true;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

}
