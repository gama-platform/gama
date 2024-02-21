/*******************************************************************************************************
 *
 * Tags.java, in gama.ui.navigator.view, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.contents;

import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import one.util.streamex.StreamEx;

/**
 * Class ImportFolder.
 *
 * @author drogoul
 * @since 5 févr. 2015
 *
 */
public class Tags extends VirtualContent<WrappedFile> {

	/** The tags. */
	final Map<String, String> tags;

	/** The search. */
	final boolean search;

	/**
	 * @param root
	 * @param name
	 */
	public Tags(final WrappedFile root, final Map<String, String> object, final String name,
			final boolean doubleClickForSearching) {
		super(root, name);
		tags = object;
		search = doubleClickForSearching;
	}

	/**
	 * Method hasChildren()
	 *
	 * @see gama.ui.navigator.view.contents.VirtualContent#hasChildren()
	 */
	@Override
	public boolean hasChildren() {
		return !tags.isEmpty();
	}

	/**
	 * Method getNavigatorChildren()
	 *
	 * @see gama.ui.navigator.view.contents.VirtualContent#getNavigatorChildren()
	 */
	@Override
	public Object[] getNavigatorChildren() {
		if (tags.isEmpty()) return EMPTY;
		return StreamEx.ofKeys(tags).map(each -> new Tag(this, each, tags.get(each), search)).toArray();
	}

	/**
	 * Method getImage()
	 *
	 * @see gama.ui.navigator.view.contents.VirtualContent#getImage()
	 */
	@Override
	public ImageDescriptor getImageDescriptor() { return GamaIcon.named(IGamaIcons.ATTRIBUTES).descriptor(); }

	/**
	 * Method getColor()
	 *
	 * @see gama.ui.navigator.view.contents.VirtualContent#getColor()
	 */
	@Override
	public Color getColor() {
		for (String s : tags.values()) {
			if (s.contains("built-in attribute")) return GamaColors.system(SWT.COLOR_DARK_RED);
		}
		return null;
	}

	@Override
	public void getSuffix(final StringBuilder sb) {}

	@Override
	public int findMaxProblemSeverity() {
		return 0;
	}

	@Override
	public ImageDescriptor getOverlay() { return null; }

	@Override
	public VirtualContentType getType() { return VirtualContentType.CATEGORY; }

	@Override
	public String getStatusMessage() { return "Tags"; }

}
