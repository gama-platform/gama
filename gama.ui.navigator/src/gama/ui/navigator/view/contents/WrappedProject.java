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
	public WrappedProject(final VirtualContent<?> parent, final IProject wrapped) {
		super(parent, wrapped);
		isTest = parent.getTopLevelFolder() instanceof TestModelsFolder;
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
	public String getExplanation() {
		return "project";
	}

	@Override
	public void getSuffix(final StringBuilder sb) {
		if (!isOpen()) {
			sb.append("closed");
			return;
		}

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
	public String getPlugin() {
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

}
