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
import org.eclipse.swt.widgets.Composite;
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
