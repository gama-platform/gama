/*******************************************************************************************************
 *
 * TextDisplayer.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.parameters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import gama.core.kernel.experiment.InputParameter;
import gama.core.kernel.experiment.TextStatement;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.core.util.GamaFont;
import gama.ui.application.workbench.ThemeHelper;
import gama.ui.shared.controls.XmlText;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.IGamaColors;
import gama.ui.shared.utils.WebHelper;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class TextDisplayer.
 */
public class TextDisplayer extends AbstractEditor<TextStatement> {

	/** The statement. */
	TextStatement statement;

	/** The front. */
	final Color back, front;

	/** The font. */
	final Font font;

	/** The is XML. */
	// boolean isHtml;

	/**
	 * Instantiates a new command editor.
	 *
	 * @param scope
	 *            the scope
	 * @param command
	 *            the command
	 * @param l
	 *            the l
	 */
	public TextDisplayer(final IScope scope, final TextStatement command) {
		super(scope.getAgent(), new InputParameter(command.getName(), null), null);
		statement = command;
		GamaColor c = command.getColor(scope);
		GamaColor b = command.getBackground(scope);
		front = c == null ? null : GamaColors.toSwtColor(c);
		back = b == null ? null : GamaColors.toSwtColor(b);
		GamaFont f = command.getFont(scope);
		font = f == null ? null : new Font(WorkbenchHelper.getDisplay(), f.getFontName(), f.getSize(), f.getStyle());
	}

	/**
	 * Checks if is html.
	 *
	 * @param message
	 *            the message
	 */
	public boolean isHtml(final String message) {
		return message.contains("<html>");
	}

	@Override
	public void createControls(final EditorsGroup parent) {
		this.parent = parent;
		internalModification = true;
		// Create the label of the value editor
		editorLabel = createEditorLabel();
		// Create the composite that will hold the value editor and the toolbar
		createValueComposite();
		// Create and initialize the value editor
		editorControl = createEditorControl();

		// Create and initialize the toolbar associated with the value editor
		editorToolbar = null;
		internalModification = false;
		parent.requestLayout();
	}

	@Override
	Composite createValueComposite() {
		composite = new Composite(parent, SWT.NONE);
		GamaColors.setBackground(parent.getBackground(), composite);
		final var data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.minimumWidth = 100;
		data.horizontalSpan = 3;
		composite.setLayoutData(data);
		// Layout layout = isXML ? new FormLayout() : new FillLayout();
		Layout layout = new FillLayout();
		composite.setLayout(layout);
		return composite;
	}

	@Override
	protected Control createCustomParameterControl(final Composite composite) throws GamaRuntimeException {
		String text = statement.getText(getScope());
		if (text == null) return new Text(composite, SWT.None);
		Control result = text.contains("<html>") ? buildBrowser(composite, text) : buildForm(composite, text);
		GamaColors.setBackAndForeground(back, front, result);
		result.setFont(font);
		composite.requestLayout();
		return result;

	}

	/**
	 * Builds the form.
	 *
	 * @param composite
	 *            the composite
	 * @param text
	 *            the text
	 * @return the control
	 */
	private Control buildForm(final Composite composite, final String text) {

		XmlText form = new XmlText(composite, SWT.NONE | SWT.READ_ONLY);
		form.setText("<form>" + text + "</form>", true, true);
		form.setHyperlinkSettings(getHyperlinkSettings());
		form.addHyperlinkListener(new HyperlinkAdapter() {

			@Override
			public void linkActivated(final HyperlinkEvent e) {
				WebHelper.openPage(e.getHref().toString());
			}

		});
		return form;
	}

	/**
	 * @return
	 */
	private HyperlinkSettings getHyperlinkSettings() {
		HyperlinkSettings settings = new HyperlinkSettings(WorkbenchHelper.getDisplay());
		settings.setActiveForeground(
				ThemeHelper.isDark() ? IGamaColors.TOOLTIP.color() : IGamaColors.DARK_ORANGE.color());
		settings.setActiveBackground(back);
		settings.setForeground(ThemeHelper.isDark() ? IGamaColors.TOOLTIP.color() : IGamaColors.DARK_ORANGE.color());
		settings.setBackground(back);
		settings.setHyperlinkUnderlineMode(HyperlinkSettings.UNDERLINE_HOVER);
		settings.setHyperlinkCursor(new Cursor(WorkbenchHelper.getDisplay(), SWT.CURSOR_ARROW));
		settings.setBusyCursor(new Cursor(WorkbenchHelper.getDisplay(), SWT.CURSOR_ARROW));
		settings.setTextCursor(new Cursor(WorkbenchHelper.getDisplay(), SWT.CURSOR_ARROW));
		return settings;
	}

	/**
	 * Builds the browser.
	 *
	 * @param composite
	 *            the composite
	 * @param text
	 *            the text
	 * @return the control
	 */
	private Control buildBrowser(final Composite composite, final String text) {
		Browser browser = new Browser(composite, SWT.NONE | SWT.READ_ONLY);
		browser.setText(text);
		browser.addLocationListener(new LocationAdapter() {

			@Override
			public void changing(final LocationEvent event) {
				WebHelper.openPage(event.location);
				event.doit = false;
			}

		});
		return browser;
	}

	@Override
	EditorLabel createEditorLabel() {
		return null;
	}

	@Override
	Color getEditorControlBackground() { return back == null ? super.getEditorControlBackground() : back; }

	@Override
	Color getEditorControlForeground() { return front == null ? super.getEditorControlForeground() : front; }

	@Override
	protected int[] getToolItems() { return new int[0]; }

	@Override
	protected void displayParameterValue() {

	}

	// @Override
	// protected Object getEditorControlGridData() { return isXML ? new FormData() : super.getEditorControlGridData(); }

}
