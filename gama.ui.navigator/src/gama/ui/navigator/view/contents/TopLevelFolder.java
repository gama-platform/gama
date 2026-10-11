/*******************************************************************************************************
 *
 * TopLevelFolder.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.contents;


import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;

import gama.api.GAMA;
import gama.ui.shared.resources.GamaColors.GamaUIColor;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaColors;
import gama.ui.shared.resources.IGamaIcons;
import gama.workspace.manager.ProjectCategory;
import one.util.streamex.StreamEx;

/**
 * Class TopLevelFolder.
 *
 * @author drogoul
 * @since 30 déc. 2015
 *
 */
public class TopLevelFolder extends VirtualContent<NavigatorRoot> implements IGamaIcons, IGamaColors {

	/** The children. */
	WrappedProject[] children;

	/** The status icon. */
	final ImageDescriptor icon;

	/** The status message. */
	final String statusMessage;

	/** The status color. */
	final GamaUIColor statusColor;

	/** The category of the projects displayed in this folder. */
	final ProjectCategory category;

	/**
	 * @param root
	 * @param name
	 */
	public TopLevelFolder(final NavigatorRoot root, final String name, final String iconName,
			final String statusMessage, final GamaUIColor statusColor, final ProjectCategory category) {
		super(root, name);
		this.statusColor = statusColor;
		this.statusMessage = statusMessage;
		this.category = category;
		icon = GamaIcon.named(iconName).descriptor();
		initializeChildren();
	}

	/**
	 * Initialize children.
	 */
	public void initializeChildren() {
		children = StreamEx.of(GAMA.getWorkspaceManager().getRoot().getProjects()).filter(this::privateAccepts)
				.map(p -> (WrappedProject) getManager().wrap(this, p)).toArray(WrappedProject.class);
	}

	@Override
	public boolean hasChildren() {
		return children.length > 0;
	}

	// @Override
	// public Font getFont() {
	// return GamaFonts.getNavigHeaderFont();
	// }

	@Override
	public Object[] getNavigatorChildren() { return children; }

	@Override
	public int findMaxProblemSeverity() {
		var severity = NO_PROBLEM;
		for (final WrappedProject p : children) {
			final int s = p.findMaxProblemSeverity();
			if (s > severity) { severity = s; }
			if (severity == IMarker.SEVERITY_ERROR) { break; }
		}
		return severity;
	}

	/**
	 * Checks whether a project belongs to this folder. Projects are classified by their location (see
	 * {@link ProjectCategory}).
	 *
	 * @param project
	 *            the project to test
	 * @return {@code true} if the project should appear in this folder
	 */
	public final boolean privateAccepts(final IProject project) {
		if (project == null || !project.exists()) return false;
		return ProjectCategory.of(project) == category;
	}

	@Override
	public final ImageDescriptor getImageDescriptor() { return icon; }

	@Override
	public String getStatusMessage() { return statusMessage; }

	// @Override
	// public Color getColor() {
	// return ThemeHelper.isDark() ? IGamaColors.VERY_LIGHT_GRAY.color() : IGamaColors.GRAY_LABEL.color();
	// }

	@Override
	public void getSuffix(final StringBuilder sb) {
		final var projectCount = children.length;
		sb.append(projectCount).append(" project");
		if (projectCount > 1) { sb.append("s"); }
	}

	@Override
	public ImageDescriptor getOverlay() { return DESCRIPTORS.get(findMaxProblemSeverity()); }

	@Override
	public TopLevelFolder getTopLevelFolder() { return this; }

	/**
	 * Gets the category of the projects displayed in this folder.
	 *
	 * @return the category
	 */
	public ProjectCategory getCategory() { return category; }

	@Override
	public VirtualContentType getType() { return VirtualContentType.VIRTUAL_FOLDER; }

}
