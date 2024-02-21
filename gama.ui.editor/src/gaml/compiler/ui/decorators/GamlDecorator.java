/*******************************************************************************************************
 *
 * GamlDecorator.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.decorators;

import static gama.ui.navigator.view.contents.VirtualContent.DESCRIPTORS;
import static org.eclipse.core.resources.IMarker.PROBLEM;
import static org.eclipse.core.resources.IResource.DEPTH_INFINITE;
import static org.eclipse.jface.viewers.IDecoration.BOTTOM_LEFT;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

import gama.core.common.GamlFileExtension;
import gama.ui.navigator.view.contents.VirtualContent;

/**
 * Simple decorator for error and warning
 *
 */
public class GamlDecorator implements ILightweightLabelDecorator {

	/** The decorator id. */
	public static String decoratorId = "gama.light.decorator";

	@Override
	public void addListener(final ILabelProviderListener listener) {}

	@Override
	public void decorate(final Object element, final IDecoration deco) {
		if (element instanceof VirtualContent) {
			deco.addOverlay(((VirtualContent<?>) element).getOverlay(), BOTTOM_LEFT);
		} else if (element instanceof final IFile r) {
			if (GamlFileExtension.isAny(r.getName())) {
				try {
					deco.addOverlay(DESCRIPTORS.get(r.findMaxProblemSeverity(PROBLEM, true, DEPTH_INFINITE)),
							BOTTOM_LEFT);
				} catch (final CoreException e) {}
			}
		}
	}

	@Override
	public void dispose() {}

	@Override
	public boolean isLabelProperty(final Object element, final String property) {
		return false;
	}

	@Override
	public void removeListener(final ILabelProviderListener listener) {}

}