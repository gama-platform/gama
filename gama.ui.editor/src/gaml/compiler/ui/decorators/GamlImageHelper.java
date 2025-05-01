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
import org.eclipse.xtext.ui.IImageHelper;
import org.eclipse.xtext.ui.IImageHelper.IImageDescriptorHelper;

import com.google.common.collect.Maps;
import com.google.inject.Singleton;

import gama.ui.shared.resources.GamaIcon;

/**
 * The class GamlImageHelper.
 *
 * @author drogoul
 * @since 5 févr. 2015
 *
 */
@Singleton
public class GamlImageHelper implements IImageHelper, IImageDescriptorHelper {

	/** The Constant UNKNOWN_DESC. */
	private static final ImageDescriptor UNKNOWN_DESC = GamaIcon.named(GamaIcon.MISSING).descriptor();

	/** The Constant UNKNOWN_DESC. */
	private static final Image UNKNOWN_IMG = GamaIcon.named(GamaIcon.MISSING).image();

	/** The registry. */
	private final Map<ImageDescriptor, Image> registry = Maps.newHashMapWithExpectedSize(10);

	/**
	 * @see org.eclipse.xtext.ui.IImageHelper.IImageDescriptorHelper#getImageDescriptor(java.lang.String)
	 */
	@Override
	public ImageDescriptor getImageDescriptor(final String name) {
		String code = GamaIcon.GAML_PATH + name.replace(".png", "");
		return GamaIcon.exist(code) ? GamaIcon.named(code).descriptor() : UNKNOWN_DESC;

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
		String code = GamaIcon.GAML_PATH + name.replace(".png", "");
		return GamaIcon.exist(code) ? GamaIcon.named(code).image() : UNKNOWN_IMG;
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
