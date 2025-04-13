/*******************************************************************************************************
 *
 * GamlImageHelper.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.decorators;

import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.xtext.ui.IImageHelper;
import org.eclipse.xtext.ui.IImageHelper.IImageDescriptorHelper;

import com.google.common.collect.Maps;
import com.google.inject.Singleton;

import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;

/**
 * The class GamlImageHelper.
 *
 * @author drogoul
 * @since 5 févr. 2015
 *
 */
@Singleton
public class GamlImageHelper implements IImageHelper, IImageDescriptorHelper {

	/** The Constant path. */
	private static final String path = "gaml";

	/** The Constant defaultIcon. */
	private static final String defaultIcon = "/_unknown";

	/** The registry. */
	private final Map<ImageDescriptor, Image> registry = Maps.newHashMapWithExpectedSize(10);

	/**
	 * Exist.
	 *
	 * @param name
	 *            the name
	 * @return true, if successful
	 */
	public boolean exist(final String name) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(IGamaIcons.PLUGIN_ID,
				IGamaIcons.ICONS_PATH + name + ".png") != null;
	}

	/**
	 * @see org.eclipse.xtext.ui.IImageHelper.IImageDescriptorHelper#getImageDescriptor(java.lang.String)
	 */
	@Override
	public ImageDescriptor getImageDescriptor(final String name) {
		String s = name;
		if (s.endsWith(".png")) { s = s.replace(".png", ""); }
		if (exist(path + "/" + s)) return GamaIcon.named(path + "/" + s).descriptor();
		return GamaIcon.named(path + defaultIcon).descriptor();

	}

	/**
	 * @see org.eclipse.xtext.ui.IImageHelper.IImageDescriptorHelper#getImageDescriptor(org.eclipse.swt.graphics.Image)
	 */
	@Override
	public ImageDescriptor getImageDescriptor(final Image image) {
		for (final Map.Entry<ImageDescriptor, Image> entry : registry.entrySet()) {
			if (entry.getValue().equals(image)) return entry.getKey();
		}
		final ImageDescriptor newDescriptor = ImageDescriptor.createFromImage(image);
		registry.put(newDescriptor, image);
		return newDescriptor;

	}

	/**
	 * @see org.eclipse.xtext.ui.IImageHelper#getImage(java.lang.String)
	 */
	@Override
	public Image getImage(final String name) {
		String s = name;
		if (s.endsWith(".png")) { s = s.replace(".png", ""); }
		if (exist(path + "/" + s)) return GamaIcon.named(path + "/" + s).image();
		return GamaIcon.named(path + defaultIcon).image();
	}

	/**
	 * @see org.eclipse.xtext.ui.IImageHelper#getImage(org.eclipse.jface.resource.ImageDescriptor)
	 */
	@Override
	public Image getImage(final ImageDescriptor d) {
		ImageDescriptor descriptor = d;
		if (descriptor == null) { descriptor = ImageDescriptor.getMissingImageDescriptor(); }

		Image result = registry.get(descriptor);
		if (result != null) return result;
		result = descriptor.createImage();
		if (result != null) { registry.put(descriptor, result); }
		return result;
	}

}
