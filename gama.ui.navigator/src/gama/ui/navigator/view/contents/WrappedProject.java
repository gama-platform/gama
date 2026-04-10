/*******************************************************************************************************
 *
 * WrappedProject.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.contents;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.xml.type.internal.DataValue.URI;
import org.eclipse.jface.resource.ImageDescriptor;

import gama.api.utils.tests.AbstractSummary;
import gama.api.utils.tests.CompoundSummary;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;

/**
 * The Class WrappedProject.
 */
public class WrappedProject extends WrappedContainer<IProject> {

	static {
		// DEBUG.OFF();
	}

	/** The plugin. */
	private String plugin;

	/** The is test. */
	final boolean isTest;

	/**
	 * Instantiates a new wrapped project.
	 *
	 * @param parent
	 *            the parent
	 * @param wrapped
	 *            the wrapped
	 */
	public WrappedProject(final TopLevelFolder parent, final IProject wrapped) {
		super(parent, wrapped);
		isTest = parent instanceof TestModelsFolder;
	}

	@Override
	public boolean canBeDecorated() {
		return true;
	}

	@Override
	public boolean isOpen() { return super.isOpen() && getResource().isOpen(); }

	@Override
	public boolean handleDoubleClick() {
		if (!isOpen()) {
			try {
				getResource().open(null);
			} catch (final CoreException e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	@Override
	public Object[] getNavigatorChildren() { return isOpen() ? super.getNavigatorChildren() : EMPTY; }

	@Override
	public ImageDescriptor getImageDescriptor() { return GamaIcon.named(IGamaIcons.FOLDER_PROJECT).descriptor(); }

	@Override
	public void getSuffix(final StringBuilder sb) {
		if (!isOpen()) {
			sb.append("closed");
			return;
		}

		if (getPlugin() != null && !getPlugin().isEmpty()) { sb.append(getPlugin()).append(", "); }
		if (isTestProject()) {
			getTestSuffix(sb);
		} else {
			super.getSuffix(sb);
		}
	}

	/**
	 * Gets the test suffix.
	 *
	 * @param sb
	 *            the sb
	 * @return the test suffix
	 */
	private void getTestSuffix(final StringBuilder sb) {
		final var emfURI = org.eclipse.emf.common.util.URI.createPlatformResourceURI(URI.encode(getName()), false);
		final var result = getSuffixOfTestSummary(emfURI);
		if (result.isEmpty()) {
			super.getSuffix(sb);
		} else {
			sb.append(result);
		}
	}

	/**
	 * Gets the suffix of test summary.
	 *
	 * @param uri
	 *            the uri
	 * @return the suffix of test summary
	 */
	public String getSuffixOfTestSummary(final org.eclipse.emf.common.util.URI uri) {
		final CompoundSummary<?, ?> summary = getManager().getTestsSummary();
		if (summary == null) return "";
		final List<AbstractSummary<?>> list = new ArrayList<>();
		summary.getSubSummariesBelongingTo(uri, list);
		final CompoundSummary<?, ?> result = new CompoundSummary<>(list);
		return result.getStringSummary();
	}

	/**
	 * Checks if is test project.
	 *
	 * @return true, if is test project
	 */
	private boolean isTestProject() { return isTest; }

	@Override
	public VirtualContentType getType() { return VirtualContentType.PROJECT; }

	/**
	 * Gets the plugin.
	 *
	 * @return the plugin
	 */
	String getPlugin() {
		if (plugin == null) {
			final var data = getMetaDataProvider().getMetaData(getResource(), false, false);
			if (data != null) {
				setPlugin(data.getSuffix());
			} else {
				setPlugin("");
			}
		}
		return plugin;
	}

	/**
	 * Sets the plugin.
	 *
	 * @param plugin
	 *            the new plugin
	 */
	void setPlugin(final String plugin) { this.plugin = plugin; }

	@Override
	public WrappedProject getProject() { return this; }

	@Override
	public int findMaxProblemSeverity() {
		if (severity != NOT_COMPUTED) return severity;
		if (!isOpen()) {
			severity = CLOSED;
			return severity;
		}
		// children may be null during the super-constructor call that precedes
		// initializeChildren(); in that case defer the computation.
		if (children == null) return NO_PROBLEM;
		// Aggregate severity from child resources only.
		// This intentionally excludes markers placed directly on the IProject
		// resource itself (e.g. Eclipse's charset-encoding warning marker) which
		// must not be surfaced as a folder warning when the individual files are
		// problem-free.
		severity = NO_PROBLEM;
		for (final Object child : children) {
			// Only aggregate from children that are actually shown with a severity overlay
			// (i.e. GAML files and folders that contain GAML files). This intentionally
			// excludes non-GAML resource files, hidden Eclipse metadata files (.project,
			// .settings, etc.) and other WrappedFile objects whose canBeDecorated()
			// returns false, because problem markers placed on those by Eclipse or Xtext
			// must not surface as phantom warnings when every visible file is clean.
			if (child instanceof WrappedResource<?, ?> wr && wr.canBeDecorated()) {
				final int s = wr.findMaxProblemSeverity();
				if (s > severity) { severity = s; }
				if (severity == IMarker.SEVERITY_ERROR) { break; }
			}
		}
		return severity;
	}
}
