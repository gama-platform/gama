/*******************************************************************************************************
 *
 * GamaCommand.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.views.toolbar;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import gama.ui.shared.resources.GamaIcon;

/**
 * The Class GamaCommand.
 */
public record GamaCommand(String image, String text, String tooltip, Selector runner) {

    /**
     * Builds the.
     *
     * @param image
     *            the image
     * @param text
     *            the text
     * @param tooltip
     *            the tooltip
     * @param runner
     *            the runner
     * @return the gama command
     */
    public static GamaCommand build(final String image, final String text, final String tooltip,
	    final Selector runner) {
	return new GamaCommand(image, text, tooltip, runner);
    }

    /**
     * Instantiates a new gama command.
     *
     * @param image
     *            the image
     * @param text
     *            the text
     * @param runner
     *            the runner
     */
    public GamaCommand(final String image, final String text, final Selector runner) {
	this(image, text, text, runner);
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId() {
	return image;
    }

    /**
     * To action.
     *
     * @return the action
     */
    public Action toAction() {
	final Action result = new Action(text) {

	    @Override
	    public void runWithEvent(final Event e) {
		runner.widgetSelected(new SelectionEvent(e));
	    }
	};

	result.setImageDescriptor(GamaIcon.named(image).descriptor());
	result.setToolTipText(tooltip);
	result.setId(image);
	return result;

    }

    /**
     * To check action.
     *
     * @return the action
     */
    public Action toCheckAction() {
	final Action result = new Action(text, IAction.AS_CHECK_BOX) {

	    @Override
	    public void runWithEvent(final Event e) {
		runner.widgetSelected(new SelectionEvent(e));
	    }
	};

	result.setImageDescriptor(GamaIcon.named(image).descriptor());
	result.setToolTipText(tooltip);
	result.setId(image);
	return result;

    }

    /**
     * To item.
     *
     * @param t
     *            the t
     * @return the tool item
     */
    public ToolItem toItem(final ToolBar t) {
	return toItem(t, runner);
    }

    /**
     * To check item.
     *
     * @param t
     *            the t
     * @return the tool item
     */
    public ToolItem toCheckItem(final ToolBar t) {
	final var i = new ToolItem(t, SWT.FLAT | SWT.CHECK);
	i.setToolTipText(tooltip);
	if (image != null) {
	    GamaIcon icon = GamaIcon.named(image);
	    i.setImage(icon.image());
	    i.setDisabledImage(icon.disabled());
	}
	if (runner != null) {
	    i.addSelectionListener(runner);
	}
	return i;

    }

    /**
     * To item.
     *
     * @param t
     *            the t
     * @param sel
     *            the sel
     * @return the tool item
     */
    public ToolItem toItem(final ToolBar t, final Selector sel) {
	final var i = new ToolItem(t, SWT.FLAT | SWT.PUSH);
	// By default without text
	// if (text != null) { i.setText(text); }
	i.setToolTipText(tooltip);
	if (image != null) {
	    GamaIcon icon = GamaIcon.named(image);
	    i.setImage(icon.image());
	    i.setDisabledImage(icon.disabled());
	}
	if (sel != null) {
	    i.addSelectionListener(sel);
	}
	return i;
    }

    /**
     * To item.
     *
     * @param m
     *            the m
     * @return the menu item
     */
    public MenuItem toItem(final Menu m) {
	final var i = new MenuItem(m, SWT.PUSH);
	if (text != null) {
	    i.setText(text);
	}
	i.setToolTipText(tooltip);
	if (image != null) {
	    i.setImage(GamaIcon.named(image).image());
	}
	if (runner != null) {
	    i.addSelectionListener(runner);
	}
	return i;
    }

    /**
     * To check item.
     *
     * @param m
     *            the m
     * @return the menu item
     */
    public MenuItem toCheckItem(final Menu m) {
	final var i = new MenuItem(m, SWT.CHECK);
	if (text != null) {
	    i.setText(text);
	}
	i.setToolTipText(tooltip);
	if (image != null) {
	    i.setImage(GamaIcon.named(image).image());
	}
	if (runner != null) {
	    i.addSelectionListener(runner);
	}
	return i;
    }

    /**
     * To button.
     *
     * @param t
     *            the t
     * @param selector2
     *            the selector 2
     * @return the button
     */
    public Button toButton(final Composite t, final Selector sel) {
	final Button i = new Button(t, SWT.FLAT | SWT.TRANSPARENT | SWT.PUSH);

	if (text != null) {
	    i.setText(text);
	}
	i.setToolTipText(tooltip);
	if (image != null) {
	    i.setImage(GamaIcon.named(image).image());
	}
	if (sel != null) {
	    i.addSelectionListener(sel);
	}
	return i;
    }

    /**
     * To button.
     *
     * @param t
     *            the t
     * @return the button
     */
    public Button toButton(final Composite t) {
	return toButton(t, runner);
    }

    /**
     * To label.
     *
     * @param t
     *            the t
     * @param selector2
     *            the selector 2
     * @return the label
     */
    public Label toLabel(final Composite t, final MouseListener sel) {
	final Label i = new Label(t, SWT.NONE);
	if (text != null) {
	    i.setText(text);
	}
	i.setToolTipText(tooltip);
	if (image != null) {
	    i.setImage(GamaIcon.named(image).image());
	}
	if (sel != null) {
	    i.addMouseListener(sel);
	}
	return i;
    }

}
