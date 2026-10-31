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
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
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

	/** The missing-artwork descriptor, distinct from the GAML unknown type. */
	private static final ImageDescriptor MISSING_DESC = GamaIcon.named(GamaIcon.MISSING).descriptor();

	/** The registry. */
	private final Map<ImageDescriptor, Image> registry = Maps.newHashMapWithExpectedSize(10);

	private final Map<String, ImageDescriptor> descriptors = new ConcurrentHashMap<>();

	/**
	 * @see org.eclipse.xtext.ui.IImageHelper.IImageDescriptorHelper#getImageDescriptor(java.lang.String)
	 */
	@Override
	public ImageDescriptor getImageDescriptor(final String name) {
		if (name == null) return MISSING_DESC;
		return descriptors.computeIfAbsent(name, key -> {
			final String[] icons = key.replace(".png", "").split("\\+");
			final ImageDescriptor base = iconDescriptor(icons[0]);
			if (icons.length == 1) return base;
			return new CompositeImageDescriptor() {

				@Override
				protected void drawCompositeImage(final int width, final int height) {
					drawImage(createCachedImageDataProvider(base), 0, 0);
					for (int i = 1; i < icons.length; i++) {
						drawImage(createCachedImageDataProvider(iconDescriptor(icons[i])), 16,
								icons.length == 2 ? 10 : (i - 1) * 10);
					}
				}

				@Override
				protected Point getSize() { return new Point(24, 18); }
			};
		});
	}

	private static ImageDescriptor iconDescriptor(final String name) {
		final String code = IGamaIcons.GAML_PATH + name;
		return GamaIcon.exist(code) ? GamaIcon.named(code).descriptor() : MISSING_DESC;
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
		if (name != null && name.contains("+")) return getImage(getImageDescriptor(name));
		final String code = name == null ? IGamaIcons.MISSING : IGamaIcons.GAML_PATH + name.replace(".png", "");
		return GamaIcon.named(GamaIcon.exist(code) ? code : IGamaIcons.MISSING).image();
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
