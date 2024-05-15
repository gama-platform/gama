/*******************************************************************************************************
 *
 * VirtualContent.java, in gama.ui.navigator.view, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.contents;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;

/**
 * The Class VirtualContent.
 *
 * @param <P>
 *            the generic type
 */
public abstract class VirtualContent<P extends VirtualContent<?>> {

	/**
	 * The Enum VirtualContentType.
	 */
	public enum VirtualContentType {

		/** The root. */
		ROOT,
		/** The virtual folder. */
		VIRTUAL_FOLDER,
		/** The project. */
		PROJECT,
		/** The folder. */
		FOLDER,
		/** The file. */
		FILE,
		/** The file reference. */
		FILE_REFERENCE,
		/** The category. */
		CATEGORY,
		/** The gaml element. */
		GAML_ELEMENT
	}

	/** The default label provider. */
	public static final ILabelProvider DEFAULT_LABEL_PROVIDER = WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider();

	/** The Constant DESCRIPTORS. */
	public static final Map<Integer, ImageDescriptor> DESCRIPTORS = new HashMap<>() {
		{

			put(CLOSED, GamaIcon.named(IGamaIcons.OVERLAY_CLOSED).descriptor());
			put(NO_PROBLEM, GamaIcon.named(IGamaIcons.OVERLAY_OK).descriptor());
			put(IMarker.SEVERITY_INFO, GamaIcon.named(IGamaIcons.OVERLAY_OK).descriptor());
			put(IMarker.SEVERITY_WARNING, GamaIcon.named(IGamaIcons.OVERLAY_WARNING).descriptor());
			put(IMarker.SEVERITY_ERROR, GamaIcon.named(IGamaIcons.OVERLAY_ERROR).descriptor());
			put(LINK_OK, GamaIcon.named(IGamaIcons.OVERLAY_OK).descriptor());
			put(LINK_BROKEN, GamaIcon.named(IGamaIcons.OVERLAY_ERROR).descriptor());
			// put(WEBLINK_OK, GamaIcon.named(IGamaIcons.OVERLAY_CLOUD).descriptor());
			// put(WEBLINK_BROKEN, GamaIcon.named(IGamaIcons.OVERLAY_LINK_BROKEN).descriptor());
		}
	};

	/** The Constant NO_PROBLEM. */
	public static final int NO_PROBLEM = -1;

	/** The Constant CLOSED. */
	public static final int CLOSED = -2;

	/** The Constant LINK_OK. */
	public static final int LINK_OK = -3;

	/** The Constant LINK_BROKEN. */
	public static final int LINK_BROKEN = -4;

	/** The Constant WEBLINK_OK. */
	public static final int WEBLINK_OK = -5;

	/** The Constant WEBLINK_BROKEN. */
	public static final int WEBLINK_BROKEN = -6;

	/** The empty. */
	public static final Object[] EMPTY = {};

	/** The root. */
	private final P root;

	/** The name. */
	private final String name;

	/**
	 * Instantiates a new virtual content.
	 *
	 * @param root
	 *            the root
	 * @param name
	 *            the name
	 */
	public VirtualContent(final P root, final String name) {
		this.root = root;
		this.name = name;
	}

	/**
	 * Gets the manager.
	 *
	 * @return the manager
	 */
	public ResourceManager getManager() { return NavigatorRoot.getInstance().getManager(); }

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public abstract VirtualContentType getType();

	/**
	 * Should both perform something and answer whether or not it has performed it, so that the navigator knows whether
	 * it should handle double-clicks itself
	 *
	 * @return
	 */
	public boolean handleDoubleClick() {
		return false;
	}

	/**
	 * Handle single click.
	 *
	 * @return true, if successful
	 */
	public boolean handleSingleClick() {
		return false;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() { return name; }

	/**
	 * Gets the parent.
	 *
	 * @return the parent
	 */
	public P getParent() { return root; }

	/**
	 * Checks for children.
	 *
	 * @return true, if successful
	 */
	public abstract boolean hasChildren();

	/**
	 * Gets the navigator children.
	 *
	 * @return the navigator children
	 */
	public abstract Object[] getNavigatorChildren();

	/**
	 * Gets the image.
	 *
	 * @return the image
	 */
	public abstract ImageDescriptor getImageDescriptor();

	/**
	 * Gets the color.
	 *
	 * @return the color
	 */
	public Color getColor() { return null; }

	/**
	 * Gets the suffix.
	 *
	 * @param sb
	 *            the sb
	 * @return the suffix
	 */
	public abstract void getSuffix(StringBuilder sb);


	/**
	 * Find max problem severity.
	 *
	 * @return the int
	 */
	public abstract int findMaxProblemSeverity();

	/**
	 * Gets the overlay.
	 *
	 * @return the overlay
	 */
	public abstract ImageDescriptor getOverlay();

	/**
	 * Gets the top level folder.
	 *
	 * @return the top level folder
	 */
	public TopLevelFolder getTopLevelFolder() {
		final P p = getParent();
		if (p != null) return p.getTopLevelFolder();
		return null;
	}

	/**
	 * Gets the project.
	 *
	 * @return the project
	 */
	public WrappedProject getProject() {
		final P p = getParent();
		if (p != null) return p.getProject();
		return null;
	}

	/**
	 * Gets the status message.
	 *
	 * @return the status message
	 */
	public String getStatusMessage() { return getName(); }

	/**
	 * Checks if is contained in.
	 *
	 * @param current
	 *            the current
	 * @return true, if is contained in
	 */
	public boolean isContainedIn(final VirtualContent<?> current) {
		if (root == null) return false;
		return root == current || root.isContainedIn(current);
	}

}
