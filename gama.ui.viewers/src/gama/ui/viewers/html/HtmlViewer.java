/*******************************************************************************************************
 *
 * HtmlViewer.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.html;

import java.net.MalformedURLException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import gama.core.common.interfaces.IGamaView;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.WebHelper;
import gama.ui.shared.views.toolbar.GamaToolbar2;
import gama.ui.shared.views.toolbar.GamaToolbarFactory;
import gama.ui.shared.views.toolbar.IToolbarDecoratedView;

/**
 * Class BrowserEditor.
 *
 * @author drogoul
 * @since 28 avr. 2014
 *
 */
public class HtmlViewer extends EditorPart implements IToolbarDecoratedView, IGamaView.Html {

	/** The browser. */
	Browser browser;

	/** The home. */
	ToolItem back, forward, home;

	/**
	 * Instantiates a new html viewer.
	 */
	public HtmlViewer() {}

	@Override
	public void doSave(final IProgressMonitor monitor) {}

	@Override
	public void doSaveAs() {}

	@Override
	public void init(final IEditorSite site, final IEditorInput in) throws PartInitException {
		setSite(site);
		setInput(in);
		openInput();
	}

	/**
	 * Open input.
	 */
	private void openInput() {
		if (browser == null) return;
		if (getEditorInput() instanceof FileEditorInput) {
			final FileEditorInput input = (FileEditorInput) getEditorInput();
			try {
				this.setUrl(input.getURI().toURL().toString());
			} catch (final MalformedURLException e) {
				e.printStackTrace();
			}
		} else if (getEditorInput() instanceof FileStoreEditorInput) {
			final FileStoreEditorInput input = (FileStoreEditorInput) getEditorInput();
			try {
				this.setUrl(input.getURI().toURL().toString());
			} catch (final MalformedURLException e) {

				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean isDirty() { return false; }

	@Override
	public boolean isSaveAsAllowed() { return false; }

	@Override
	public void createPartControl(final Composite parent) {
		final Composite compo = GamaToolbarFactory.createToolbars(this, parent);
		browser = new Browser(compo, SWT.NONE);
		browser.addProgressListener(new ProgressListener() {

			@Override
			public void changed(final ProgressEvent arg0) {}

			@Override
			public void completed(final ProgressEvent event) {
				checkButtons();
			}
		});
		parent.layout();
		openInput();
	}

	@Override
	public void setUrl(final String url) {
		browser.setUrl(url);
		this.setPartName(url.substring(url.lastIndexOf('/') + 1));
		checkButtons();
	}

	/**
	 *
	 */
	void checkButtons() {
		back.setEnabled(browser.isBackEnabled());
		forward.setEnabled(browser.isForwardEnabled());
	}

	@Override
	public void setFocus() {
		browser.setFocus();
	}

	/**
	 * Gets the sizable font control.
	 *
	 * @return the sizable font control
	 */
	public Control getSizableFontControl() { return browser; }

	/**
	 * Method createToolItem()
	 *
	 * @see gama.ui.shared.views.toolbar.IToolbarDecoratedView#createToolItem(int,
	 *      gama.ui.shared.views.toolbar.GamaToolbar2)
	 */
	@Override
	public void createToolItems(final GamaToolbar2 tb) {

		back = tb.button(IGamaIcons.BROWSER_BACK, "Back", "Go to previous page in history", e -> {
			browser.back();
			checkButtons();
		}, SWT.RIGHT);
		home = tb.button("toolbar/view.home", "Home", "Go back to the welcome page", e -> {
			browser.setUrl(WebHelper.getWelcomePageURL().toString());
			checkButtons();
		}, SWT.RIGHT);
		forward = tb.button(IGamaIcons.BROWSER_FORWARD, "Forward", "Go to next page in history", e -> {
			browser.forward();
			checkButtons();
		}, SWT.RIGHT);
		tb.button("toolbar/view.reload", "Refresh", "Refresh current page", e -> browser.refresh(), SWT.RIGHT);
		tb.button("toolbar/view.stop", "Stop", "Stop loading page", e -> browser.stop(), SWT.RIGHT);

	}

}
