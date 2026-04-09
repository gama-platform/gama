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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;

import gama.api.GAMA;
import gama.api.additions.GamaBundleLoader;
import gama.ui.shared.resources.GamaColors.GamaUIColor;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaColors;
import gama.ui.shared.resources.IGamaIcons;
import one.util.streamex.StreamEx;

/**
 * Class TopLevelFolder.
 *
 * @author drogoul
 * @since 30 déc. 2015
 *
 */
public class TopLevelFolder extends VirtualContent<NavigatorRoot> implements IGamaIcons, IGamaColors {

	/**
	 * The Enum Location.
	 *
	 * <p>
	 * Describes where a project physically lives so that closed projects (whose description cannot be read) can be
	 * assigned to the correct virtual folder.
	 * </p>
	 */
	public enum Location {

		/** The Core models – projects inside the {@code gama.library} bundle's {@code models/} directory. */
		CoreModels,
		/** The Plugins – projects inside any non-core bundle's {@code models/} directory. */
		Plugins,
		/** The Other – user-created projects outside any known plugin layout. */
		Other,
		/** The Unknown – could not be determined. */
		Unknown,
		/** The Tests – projects inside a {@code tests/} or {@code gaml/tests/} directory. */
		Tests,
		/** Tutorials – projects inside a {@code tutorials/} directory of any plugin bundle. */
		Tutorials,
		/** Recipes – projects inside a {@code recipes/} directory of any plugin bundle. */
		Recipes
	}

	/** The children. */
	WrappedProject[] children;

	/** The status icon. */
	final ImageDescriptor icon;

	/** The nature. */
	final String statusMessage, nature;

	/** The status color. */
	final GamaUIColor statusColor;

	/** The location. */
	final Location location;

	/**
	 * @param root
	 * @param name
	 */
	public TopLevelFolder(final NavigatorRoot root, final String name, final String iconName,
			final String statusMessage, final GamaUIColor statusColor, final String nature, final Location location) {
		super(root, name);
		this.statusColor = statusColor;
		this.statusMessage = statusMessage;
		this.nature = nature;
		this.location = location;
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
			final var s = p.findMaxProblemSeverity();
			if (s > severity) { severity = s; }
			if (severity == IMarker.SEVERITY_ERROR) { break; }
		}
		return severity;
	}

	/**
	 * @param desc
	 * @return
	 */
	public final boolean privateAccepts(final IProject project) {
		if (project == null || !project.exists()) return false;
		// TODO This one is clearly a hack. Should be replaced by a proper way
		// to track persistently the closed projects
		if (!project.isOpen()) return estimateLocation(project.getLocation()) == location;
		try {
			return accepts(project.getDescription());
		} catch (final CoreException e) {
			return false;
		}
	}

	/**
	 * Estimate location.
	 *
	 * @param location
	 *            the location
	 * @return the location
	 */
	protected Location estimateLocation(final IPath location) {
		try {
			final var old_url = new URL("platform:/plugin/" + GamaBundleLoader.CORE_MODELS.getSymbolicName() + "/");
			final var new_url = FileLocator.toFileURL(old_url);
			// windows URL formating
			final var resolvedURI = new URI(new_url.getProtocol(), new_url.getPath(), null).normalize();
			final var urlRep = resolvedURI.toURL();
			final var osString = location.toOSString();
			final var isTest = osString.contains(GamaBundleLoader.REGULAR_TESTS_LAYOUT);
			final var isTutorial = osString.contains("/" + GamaBundleLoader.REGULAR_TUTORIALS_LAYOUT + "/");
			final var isRecipe = osString.contains("/" + GamaBundleLoader.REGULAR_RECIPES_LAYOUT + "/");
			if (isTutorial) return Location.Tutorials;
			if (isRecipe) return Location.Recipes;
			if (!isTest && osString.startsWith(urlRep.getPath())) return Location.CoreModels;
			if (osString
					.startsWith(urlRep.getPath().replace(GamaBundleLoader.CORE_MODELS.getSymbolicName() + "/", ""))) {
				if (isTest) return Location.Tests;
				return Location.Plugins;
			}
			return Location.Other;
		} catch (final IOException | URISyntaxException e) {
			e.printStackTrace();
			return Location.Unknown;
		}
	}

	/**
	 * Accepts.
	 *
	 * <p>
	 * Returns {@code true} if the given project description belongs to this virtual folder. Subclasses may override
	 * this method to perform custom nature-matching logic (e.g., accepting multiple natures).
	 * </p>
	 *
	 * @param desc
	 *            the project description to test
	 * @return {@code true} if the project should appear in this folder
	 */
	public boolean accepts(final IProjectDescription desc) {
		if (nature != null) return desc.hasNature(nature);
		return desc.getNatureIds().length < 3;
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
	 * Gets the nature.
	 *
	 * @return the nature
	 */
	public String getNature() { return nature; }

	@Override
	public VirtualContentType getType() { return VirtualContentType.VIRTUAL_FOLDER; }

}
