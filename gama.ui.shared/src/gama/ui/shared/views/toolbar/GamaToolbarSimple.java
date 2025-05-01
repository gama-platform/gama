/*******************************************************************************************************
 *
 * GamaToolbarSimple.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.views.toolbar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaIcon;

/**
 * Class GamaToolbar. A declarative wrapper around toolbars
 *
 * @author drogoul
 * @since 3 déc. 2014
 *
 */
public class GamaToolbarSimple extends ToolBar {

	/**
	 * Instantiates a new gama toolbar simple.
	 *
	 * @param parent
	 *            the parent
	 * @param style
	 *            the style
	 * @param c
	 *            the c
	 */
	public GamaToolbarSimple(final Composite parent, final int style) {
		super(parent, style);
	}

	@Override
	protected void checkSubclass() {}

	/**
	 * Button.
	 *
	 * @param image
	 *            the image
	 * @param text
	 *            the text
	 * @param tip
	 *            the tip
	 * @param listener
	 *            the listener
	 * @return the tool item
	 */
	public ToolItem button(final String image, final String text, final String tip, final Selector listener) {
		return create(image, text, tip, listener, SWT.PUSH);
	}

	/**
	 * Menu.
	 *
	 * @param image
	 *            the image
	 * @param text
	 *            the text
	 * @param tip
	 *            the tip
	 * @param listener
	 *            the listener
	 * @return the tool item
	 */
	public ToolItem menu(final String image, final String text, final String tip, final Selector listener) {
		return create(image, text, tip, listener, SWT.DROP_DOWN);
	}

	/**
	 * Control.
	 *
	 * @param c
	 *            the c
	 * @param width
	 *            the width
	 * @return the tool item
	 */
	public ToolItem control(final Control c, final int width) {
		final ToolItem control = create(null, null, null, null, SWT.SEPARATOR);
		control.setControl(c);
		if (width == SWT.DEFAULT) {
			control.setWidth(c.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
		} else {
			control.setWidth(width);
		}
		return control;
	}

	/**
	 * Space.
	 *
	 * @param width
	 *            the width
	 * @return the tool item
	 */
	public ToolItem space(final int width) {
		Label label = new Label(this, SWT.NONE);
		GamaColors.setBackground(this.getBackground(), label);
		return control(label, width);
	}

	/**
	 * Creates the.
	 *
	 * @param i
	 *            the i
	 * @param text
	 *            the text
	 * @param tip
	 *            the tip
	 * @param listener
	 *            the listener
	 * @param style
	 *            the style
	 * @return the tool item
	 */
	private ToolItem create(final String i, final String text, final String tip, final Selector listener,
			final int style) {
		final ToolItem button = new ToolItem(this, style, getItems().length);
		if (tip != null) { button.setToolTipText(tip); }
		if (i != null) {
			GamaIcon icon = GamaIcon.named(i);
			button.setImage(icon.image());
			button.setDisabledImage(icon.disabled());
		}
		if (listener != null) { button.addSelectionListener(listener); }
		return button;
	}

}
