/*******************************************************************************************************
 *
 * GamaSourceViewer.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.editors.text.codemining.annotation.AnnotationCodeMiningProvider;
import org.eclipse.xtext.ui.editor.XtextSourceViewer;

/**
 * The class GamaSourceViewer.
 *
 * <p>
 * Extends {@link XtextSourceViewer} with the following enhancements:
 * <ul>
 * <li>Tracks whether the overview (annotation overview) ruler is currently visible.</li>
 * <li>Provides additive code-mining provider management so that GAML-specific providers can coexist with
 * providers registered by external plugins (e.g. GitHub Copilot) instead of replacing them.</li>
 * <li>Exposes a simple text-search helper ({@link #find(String)}).</li>
 * <li>Prevents unexpected horizontal scrolling during vertical keyboard navigation (↑, ↓, Page Up, Page Down)
 * so that the viewport stays stable when the cursor moves to lines of different widths — matching the
 * behaviour of most modern text editors.</li>
 * </ul>
 * </p>
 *
 * @author drogoul
 * @since 12 août 2016
 */
public class GamaSourceViewer extends XtextSourceViewer {

	/**
	 * Whether the overview (annotation overview) ruler is currently shown. Kept in sync by
	 * {@link #showAnnotationsOverview(boolean)}.
	 */
	private boolean isOverviewVisible;

	/**
	 * Tracks all currently installed {@link ICodeMiningProvider} instances so that
	 * {@link #addCodeMiningProvider(ICodeMiningProvider)} can merge rather than replace them.
	 */
	private final List<ICodeMiningProvider> managedProviders = new ArrayList<>();

	/**
	 * Instantiates a new GamaSourceViewer.
	 *
	 * @param parent
	 *            the parent composite
	 * @param ruler
	 *            the vertical ruler
	 * @param overviewRuler
	 *            the overview ruler
	 * @param showsAnnotationOverview
	 *            whether the annotation overview should be shown initially
	 * @param styles
	 *            the SWT style bits
	 */
	public GamaSourceViewer(final Composite parent, final IVerticalRuler ruler, final IOverviewRuler overviewRuler,
			final boolean showsAnnotationOverview, final int styles) {
		super(parent, ruler, overviewRuler, showsAnnotationOverview, styles);
		isOverviewVisible = showsAnnotationOverview && overviewRuler != null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Installs the vertical-navigation horizontal-scroll lock after the underlying {@link StyledText} widget has
	 * been created by the superclass.
	 * </p>
	 */
	@Override
	protected void createControl(final Composite parent, final int styles) {
		super.createControl(parent, styles);
		installVerticalNavigationScrollLock();
	}

	/**
	 * Installs a {@link VerifyKeyListener} on the underlying {@link StyledText} widget that prevents the horizontal
	 * scroll position from changing during pure vertical keyboard navigation (↑, ↓, Page Up, Page Down — with or
	 * without Shift for selection extension).
	 *
	 * <p>
	 * By default, SWT's {@link StyledText} calls {@code showCaret()} after every caret movement, which can cause the
	 * viewport to scroll horizontally when the caret lands on a line that is wider than the visible area. This
	 * matches the default Eclipse/SWT behaviour, but differs from most modern text editors (VS Code, Sublime,
	 * IntelliJ) that keep the horizontal scroll position stable during vertical navigation.
	 * </p>
	 *
	 * <p>
	 * The fix works by capturing the horizontal pixel offset just before each vertical navigation key is processed
	 * and restoring it asynchronously after the caret has been moved. The restore is scheduled via
	 * {@link org.eclipse.swt.widgets.Display#asyncExec(Runnable)} so that it runs after SWT has finished processing
	 * the key event (and after any internal {@code showCaret()} call), but before the next paint cycle — avoiding
	 * any visible flicker.
	 * </p>
	 */
	private void installVerticalNavigationScrollLock() {
		final StyledText st = getTextWidget();
		if (st == null) return;
		st.addVerifyKeyListener(new VerifyKeyListener() {
			@Override
			public void verifyKey(final VerifyEvent event) {
				switch (event.keyCode) {
					case SWT.ARROW_UP:
					case SWT.ARROW_DOWN:
					case SWT.PAGE_UP:
					case SWT.PAGE_DOWN: {
						final int hPixel = st.getHorizontalPixel();
						st.getDisplay().asyncExec(() -> {
							if (!st.isDisposed()) { st.setHorizontalPixel(hPixel); }
						});
						break;
					}
					default:
						break;
				}
			}
		});
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Also updates the {@link #isOverviewVisible()} flag.
	 * </p>
	 */
	@Override
	public void showAnnotationsOverview(final boolean show) {
		super.showAnnotationsOverview(show);
		isOverviewVisible = show;
	}

	/**
	 * Returns whether the annotation overview ruler is currently visible.
	 *
	 * @return {@code true} if the overview ruler is shown, {@code false} otherwise
	 */
	public boolean isOverviewVisible() { return isOverviewVisible; }

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Overridden to keep {@link #managedProviders} in sync so that subsequent calls to
	 * {@link #addCodeMiningProvider(ICodeMiningProvider)} can merge providers rather than blindly replacing them.
	 * </p>
	 */
	@Override
	public void setCodeMiningProviders(final ICodeMiningProvider[] providers) {
		managedProviders.clear();
		if (providers != null) { Collections.addAll(managedProviders, providers); }
		super.setCodeMiningProviders(providers);
	}

	/**
	 * Adds a single {@link ICodeMiningProvider} to the set of currently active providers without replacing the ones
	 * already installed (e.g. those registered by external plugins such as GitHub Copilot). If {@code provider} is
	 * already present (by reference) it will not be added again.
	 *
	 * @param provider
	 *            the provider to add; ignored if {@code null}
	 */
	public void addCodeMiningProvider(final ICodeMiningProvider provider) {
		if (provider != null && !managedProviders.contains(provider)) {
			managedProviders.add(provider);
			super.setCodeMiningProviders(managedProviders.toArray(new ICodeMiningProvider[0]));
		}
	}

	/**
	 * Replaces all currently installed {@link AnnotationCodeMiningProvider} instances with the given {@code replacement}
	 * provider, leaving all other providers (e.g. GitHub Copilot) untouched.
	 *
	 * <p>
	 * This is the correct way to install the GAML-specific {@link AnnotationCodeMiningProvider} subclass: calling
	 * {@code super.installCodeMiningProviders()} in {@link gaml.compiler.ui.editor.GamlEditor} already installs the
	 * default Eclipse {@link AnnotationCodeMiningProvider} via the extension point. Simply appending the GAML one
	 * with {@link #addCodeMiningProvider(ICodeMiningProvider)} would result in two annotation mining providers being
	 * active simultaneously and code minings being rendered twice. This method removes any existing
	 * {@link AnnotationCodeMiningProvider} instances before adding {@code replacement}.
	 * </p>
	 *
	 * @param replacement
	 *            the {@link AnnotationCodeMiningProvider} subclass to install; ignored if {@code null}
	 */
	public void replaceAnnotationCodeMiningProvider(final AnnotationCodeMiningProvider replacement) {
		if (replacement == null) return;
		managedProviders.removeIf(p -> p instanceof AnnotationCodeMiningProvider);
		managedProviders.add(replacement);
		super.setCodeMiningProviders(managedProviders.toArray(new ICodeMiningProvider[0]));
	}

	/**
	 * Returns a snapshot of the currently installed {@link ICodeMiningProvider} instances.
	 *
	 * @return array of active providers; never {@code null}
	 */
	public ICodeMiningProvider[] getCodeMiningProviders() {
		return managedProviders.toArray(new ICodeMiningProvider[0]);
	}

	/**
	 * Performs a forward text search starting from the beginning of the document.
	 *
	 * @param string
	 *            the text to search for
	 * @return the offset of the first occurrence, or {@code -1} if not found
	 */
	public int find(final String string) {
		return super.findAndSelect(0, string, true, true, false, false);
	}

}
