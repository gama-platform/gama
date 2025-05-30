/*******************************************************************************************************
 *
 * GlobalToolbarCompoundItem.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.toolbar;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolItem;

import gama.ui.shared.views.toolbar.GamaToolbarSimple;

/**
 *
 */
public abstract class GlobalToolbarCompoundItem extends GlobalToolbarItem {

	/** The items. */
	final List<GlobalToolbarItem> items = new ArrayList<>();

	/** The composite that is used to show/hide the control with a gridLayout */
	Composite composite;

	/** The control that contains relevant information. */
	Control innerControl;

	/** The control that is used to "hide" . */
	Control blankControl;

	/** The blank data. */
	GridData blankData, normalData;

	/** The hidden. */
	boolean hidden;

	/**
	 * @param toolbar
	 */
	GlobalToolbarCompoundItem(final GamaToolbarSimple toolbar) {
		super(toolbar);
	}

	@Override
	protected ToolItem createItem(final GamaToolbarSimple parent) {
		composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(composite);
		blankControl = new Label(composite, SWT.NONE);
		blankData = GridDataFactory.fillDefaults().grab(true, true).create();
		blankControl.setLayoutData(blankData);
		innerControl = createInnerControl(composite);
		normalData = GridDataFactory.fillDefaults().grab(true, true).create();
		innerControl.setLayoutData(normalData);
		return parent.control(composite, SWT.DEFAULT);
	}

	@Override
	public void update() {
		if (hidden) return;
		items.forEach(GlobalToolbarItem::update);
		setWidth(innerControl.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
	}

	@Override
	public void reinit() {
		items.forEach(GlobalToolbarItem::reinit);
	}

	/**
	 * Gets the control that contains relevant information.
	 *
	 * @return the control that contains relevant information
	 */
	public Control getInnerControl() { return innerControl; }

	/**
	 * Creates the inner control.
	 *
	 * @param composite2
	 *
	 * @return the control
	 */
	protected abstract Control createInnerControl(Composite composite);

	@Override
	public void hide() {
		if (hidden || innerControl == null || item.isDisposed()) return;
		if (blankData != null) { blankData.exclude = false; }
		if (blankControl != null && !blankControl.isDisposed()) { blankControl.setVisible(true); }
		if (normalData != null) { normalData.exclude = true; }
		if (innerControl != null && !innerControl.isDisposed()) { innerControl.setVisible(false); }
		hidden = true;
		composite.requestLayout();
	}

	@Override
	public void show() {
		if (!hidden || innerControl == null || item.isDisposed()) return;
		if (blankData != null) { blankData.exclude = true; }
		if (blankControl != null && !blankControl.isDisposed()) { blankControl.setVisible(false); }
		if (normalData != null) { normalData.exclude = false; }
		if (innerControl != null && !innerControl.isDisposed()) { innerControl.setVisible(true); }
		hidden = false;
		composite.requestLayout();
	}

	@Override
	public int getDefaultWidth() {
		return hidden ? 0 : items.stream().mapToInt(GlobalToolbarItem::getDefaultWidth).sum();
	}

	@Override
	public void setWidth(final int width) {
		normalData.widthHint = width;
		blankData.minimumWidth = width;
		item.setWidth(width);
		composite.requestLayout();
	}

}
