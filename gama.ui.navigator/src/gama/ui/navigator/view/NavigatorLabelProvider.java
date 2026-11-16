/*******************************************************************************************************
 *
 * NavigatorLabelProvider.java, in gama.ui.navigator.view, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import gama.ui.application.workbench.ThemeHelper;
import gama.ui.navigator.view.contents.TopLevelFolder;
import gama.ui.navigator.view.contents.VirtualContent;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaFonts;

/**
 * The Class NavigatorLabelProvider.
 */
public class NavigatorLabelProvider extends CellLabelProvider
		implements ILabelProvider, IColorProvider, IFontProvider {

	@Override
	public String getText(final Object element) {
		if (element instanceof VirtualContent) return ((VirtualContent<?>) element).getName();
		return null;
	}

	/** Images are owned by the label provider and shared per descriptor, then disposed with the provider. */
	private final ResourceManager images = new LocalResourceManager(JFaceResources.getResources());

	/** Descriptors whose image is already held by {@link #images}. */
	private final Set<ImageDescriptor> created = new HashSet<>();

	@Override
	public Image getImage(final Object element) {
		if (element instanceof VirtualContent vc) {
			ImageDescriptor id = vc.getNavigatorImageDescriptor();
			if (id != null) {
				if (created.add(id)) return images.create(id);
				return (Image) images.find(id);
			}
		}
		return null;
	}

	@Override
	public void dispose() {
		images.dispose();
		created.clear();
		super.dispose();
	}

	@Override
	public boolean isLabelProperty(final Object element, final String property) {
		return false;
	}

	@Override
	public Font getFont(final Object element) {
		return element instanceof TopLevelFolder ? GamaFonts.inBold(JFaceResources.getDefaultFont()) : null;
	}

	@Override
	public Color getForeground(final Object element) {
		if (element instanceof TopLevelFolder) {
			return ThemeHelper.isDark() ? GamaColors.get(165, 191, 194).color() : GamaColors.get(63, 101, 112).color();
		}
		if (element instanceof VirtualContent) return ((VirtualContent<?>) element).getColor();
		return null;
	}

	@Override
	public Color getBackground(final Object element) {
		return null;
	}

	@Override
	public void update(final ViewerCell cell) {}

}
