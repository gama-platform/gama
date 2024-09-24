package gama.ui.shared.access;

import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

public class GamlBrowserPane {

	static FontData fontData = JFaceResources.getFontRegistry().getFontData("org.eclipse.jdt.ui.javadocfont")[0];
	static String rawCSS =
			"""
								/* Font definitions */
					html         { font-family: sans-serif; font-size: 9pt; font-style: normal; font-weight: normal; }
					body, h1, h2, h3, h4, h5, h6, p, table, td, caption, th, ul, ol, dl, li, dd, dt { font-size: 1em; }
					pre          { font-family: monospace; }

					/* Margins */
					body	     { overflow: auto; margin-top: 0px; margin-bottom: 0.5em; margin-left: 0.3em; margin-right: 0px; }
					h1           { margin-top: 0.3em; margin-bottom: 0.04em; }
					h2           { margin-top: 2em; margin-bottom: 0.25em; }
					h3           { margin-top: 1.7em; margin-bottom: 0.25em; }
					h4           { margin-top: 2em; margin-bottom: 0.3em; }
					h5           { margin-top: 0px; margin-bottom: 0px; }
					p            { margin-top: 1em; margin-bottom: 1em; }
					pre          { margin-left: 0.6em; }
					ul	         { margin-top: 0px; margin-bottom: 1em; margin-left: 1em; padding-left: 1em;}
					li	         { margin-top: 0px; margin-bottom: 0px; }
					li p	     { margin-top: 0px; margin-bottom: 0px; }
					ol	         { margin-top: 0px; margin-bottom: 1em; margin-left: 1em; padding-left: 1em; }
					dl	         { margin-top: 0px; margin-bottom: 1em; }
					dt	         { margin-top: 0px; margin-bottom: 0px; font-weight: bold; }
					dd	         { margin-top: 0px; margin-bottom: 0px; }

					/* Styles and colors */
					a:link	     { color: #0000FF; }
					a:hover	     { color: #000080; }
					a:visited    { text-decoration: underline; }
					a.header:link    { text-decoration: none; color: InfoText }
					a.header:visited { text-decoration: none; color: InfoText }
					a.header:hover   { text-decoration: underline; color: #000080; }
					h4           { font-style: italic; }
					strong	     { font-weight: bold; }
					em	         { font-style: italic; }
					var	         { font-style: italic; }
					th	         { font-weight: bold; }
								""";
	static String css = HTMLPrinter.convertTopLevelFont(rawCSS, fontData);

	/** The control's browser widget */
	private Browser fBrowser;

	public void setInput(final String msg) {
		StringBuilder builder = new StringBuilder(msg);
		ColorRegistry registry = JFaceResources.getColorRegistry();
		RGB fgRGB = registry.getRGB("org.eclipse.ui.workbench.HOVER_FOREGROUND"); //$NON-NLS-1$
		RGB bgRGB = registry.getRGB("org.eclipse.ui.workbench.HOVER_BACKGROUND"); //$NON-NLS-1$
		if (fgRGB != null && bgRGB != null) {
			HTMLPrinter.insertPageProlog(builder, 0, fgRGB, bgRGB, css);
		} else {
			HTMLPrinter.insertPageProlog(builder, 0, css);
		}
		HTMLPrinter.addPageEpilog(builder);
		String content = builder.toString();
		fBrowser.setText(content);
	}

	protected Control createContent(final Composite parent) {
		fBrowser = new Browser(parent, SWT.NONE);
		fBrowser.setJavascriptEnabled(false);
		// Cancel opening of new windows
		fBrowser.addOpenWindowListener(event -> event.required = true);
		fBrowser.setFont(parent.getFont());
		// Replace browser's built-in context menu with none
		fBrowser.setMenu(new Menu(parent.getShell(), SWT.NONE));
		return fBrowser;
	}

	public Control getControl() { return fBrowser; }

}
