/*******************************************************************************************************
 *
 * WrappedFolder.java, in gama.ui.navigator.view, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.contents;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;

/**
 * The Class WrappedFolder.
 */
public class WrappedFolder extends WrappedContainer<IFolder> {

	/** The image. */
	ImageDescriptor image;

	/** The can be decorated. */
	// Font font;
	boolean canBeDecorated;

	/**
	 * Instantiates a new wrapped folder.
	 *
	 * @param root
	 *            the root
	 * @param wrapped
	 *            the wrapped
	 */
	public WrappedFolder(final WrappedContainer<?> root, final IFolder wrapped) {
		super(root, wrapped);
	}

	@Override
	public WrappedContainer<?> getParent() { return (WrappedContainer<?>) super.getParent(); }

	@Override
	public int countModels() {
		if (modelsCount == NOT_COMPUTED) {
			super.countModels();
			final var isExternal = "external".equals(getName());
			image = GamaIcon.named(isExternal ? "navigator/files/file.cloud"
					: modelsCount == 0 ? IGamaIcons.FOLDER_RESOURCES : IGamaIcons.FOLDER_MODEL).descriptor();
			canBeDecorated = modelsCount > 0;
		}
		return modelsCount;
	}

	@Override
	public boolean canBeDecorated() {
		countModels();
		return canBeDecorated;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		countModels();
		return image;
	}

	/**
	 * Aggregates the maximum problem severity from this folder's decoratable children only, intentionally ignoring any
	 * markers placed on the {@code IFolder} resource itself (e.g. charset-encoding warnings added by Eclipse or Xtext
	 * directly on a folder node) and any non-GAML resource files whose {@link #canBeDecorated()} returns {@code false}.
	 * This mirrors the same strategy used by {@link WrappedProject} for project-level markers and prevents phantom
	 * warnings from surfacing on virtual folders when every individual visible file inside is problem-free.
	 *
	 * @return the maximum {@link IMarker} severity found among this folder's decoratable children, or
	 *         {@link VirtualContent#NO_PROBLEM} if the folder is empty or all visible children are clean.
	 */
	@Override
	public int findMaxProblemSeverity() {
		if (severity != NOT_COMPUTED) return severity;
		if (!isOpen()) {
			severity = CLOSED;
			return severity;
		}
		// Children may be null if called from the super-constructor before initializeChildren(); defer in that case.
		if (children == null) return NO_PROBLEM;
		severity = NO_PROBLEM;
		for (final Object child : children) {
			// Only consider children that can show a severity overlay (GAML files and
			// sub-folders that contain GAML files). Non-GAML resource files and hidden
			// Eclipse metadata files are excluded so their markers do not produce
			// phantom warnings on visible-but-clean content.
			if (child instanceof WrappedResource<?, ?> wr && wr.canBeDecorated()) {
				final int s = wr.findMaxProblemSeverity();
				if (s > severity) { severity = s; }
				if (severity == IMarker.SEVERITY_ERROR) { break; }
			}
		}
		return severity;
	}

	@Override
	public VirtualContentType getType() { return VirtualContentType.FOLDER; }

}
