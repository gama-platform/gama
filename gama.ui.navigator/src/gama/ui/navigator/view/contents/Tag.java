/*******************************************************************************************************
 *
 * Tag.java, in gama.ui.navigator.view, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.contents;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import gama.ui.shared.resources.GamaColors;

/**
 * Class LinkedFile.
 *
 * @author drogoul
 * @since 5 févr. 2015
 *
 */
public class Tag extends VirtualContent<Tags> {

	/** The suffix. */
	private final String suffix;

	/**
	 * @param root
	 * @param name
	 */
	public Tag(final Tags root, final String wrapped, final String suffix) {
		super(root, wrapped);
		this.suffix = suffix;
	}

	/**
	 * Method hasChildren()
	 *
	 * @see gama.ui.navigator.view.contents.VirtualContent#hasChildren()
	 */
	@Override
	public boolean hasChildren() {
		return false;
	}

	/**
	 * Method getNavigatorChildren()
	 *
	 * @see gama.ui.navigator.view.contents.VirtualContent#getNavigatorChildren()
	 */
	@Override
	public Object[] getNavigatorChildren() { return EMPTY; }

	/**
	 * Method getImage()
	 *
	 * @see gama.ui.navigator.view.contents.VirtualContent#getImage()
	 */
	@Override
	public ImageDescriptor getImageDescriptor() { return null; }

	@Override
	public boolean handleDoubleClick() {
		return true;
	}

	@Override
	public int findMaxProblemSeverity() {
		return 0;
	}

	@Override
	public void getSuffix(final StringBuilder sb) {
		if (suffix != null) { sb.append(suffix); }
	}

	@Override
	public ImageDescriptor getOverlay() { return null; }

	@Override
	public VirtualContentType getType() { return VirtualContentType.CATEGORY; }

	@Override
	public Color getColor() {
		if (suffix.contains("built-in attribute")) return GamaColors.system(SWT.COLOR_DARK_RED);
		return null;
	}

}
