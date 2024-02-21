/*******************************************************************************************************
 *
 * Category.java, in gama.ui.navigator.view, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.contents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import gama.core.common.util.FileUtils;
import gama.ui.shared.resources.GamaIcon;

/**
 * Class ImportFolder.
 *
 * @author drogoul
 * @since 5 févr. 2015
 *
 */
public class Category extends VirtualContent<WrappedFile> {

	/** The file names. */
	final Collection<String> fileNames;

	/**
	 * @param root
	 * @param name
	 */
	public Category(final WrappedFile root, final Collection<String> object, final String name) {
		super(root, name);
		fileNames = object;
	}

	/**
	 * Method hasChildren()
	 *
	 * @see gama.ui.navigator.view.contents.VirtualContent#hasChildren()
	 */
	@Override
	public boolean hasChildren() {
		return !fileNames.isEmpty();
	}

	// @Override
	// public Font getFont() {
	// return GamaFonts.getSmallFont(); // by default
	// }

	@Override
	public WrappedFile getParent() { return super.getParent(); }

	/**
	 * Method getNavigatorChildren()
	 *
	 * @see gama.ui.navigator.view.contents.VirtualContent#getNavigatorChildren()
	 */
	@Override
	public Object[] getNavigatorChildren() {
		if (fileNames.isEmpty()) return EMPTY;
		final List<LinkedFile> files = new ArrayList<>();
		final var file = getParent().getResource();
		final var filePath = file.getFullPath().toString();
		final var uri = URI.createURI(filePath, false);
		for (final String fn : fileNames) {
			final var s = URI.decode(fn);
			if (s.startsWith("http")) { continue; }
			final var newFile = FileUtils.getFile(s, uri, true);
			if (newFile != null) {
				final var proxy = new LinkedFile(this, newFile, s);
				files.add(proxy);
			}
		}
		return files.toArray();
	}

	/**
	 * Method getImage()
	 *
	 * @see gama.ui.navigator.view.contents.VirtualContent#getImage()
	 */
	@Override
	public ImageDescriptor getImageDescriptor() { return GamaIcon.named("gaml/_" + getName().toLowerCase()).descriptor(); }

	/**
	 * Method getColor()
	 *
	 * @see gama.ui.navigator.view.contents.VirtualContent#getColor()
	 */
	// @Override
	// public Color getColor() {
	// return ThemeHelper.isDark() ? IGamaColors.WHITE.color() : IGamaColors.BLACK.color();
	// }

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
	public String getStatusMessage() { return "Virtual Folder"; }

}
