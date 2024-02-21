/*******************************************************************************************************
 *
 * NavigatorBaseLighweightDecorator.java, in gama.ui.navigator.view, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.navigator.view;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

import gama.ui.navigator.view.contents.VirtualContent;

/**
 * Class NavigatorBaseLighweightDecorator.
 *
 * @author drogoul
 * @since 11 févr. 2015
 *
 */
public class NavigatorBaseLighweightDecorator implements ILightweightLabelDecorator {

	/** The sb. */
	private final StringBuilder sb = new StringBuilder();

	@Override
	public void decorate(final Object element, final IDecoration decoration) {
		if (element instanceof VirtualContent) {
			((VirtualContent<?>) element).getSuffix(sb);
			if (sb.length() > 0) {
				decoration.addSuffix(" (");
				decoration.addSuffix(sb.toString());
				decoration.addSuffix(")");
				sb.setLength(0);
			}
		}
	}

	@Override
	public void addListener(final ILabelProviderListener listener) {}

	@Override
	public void dispose() {}

	@Override
	public boolean isLabelProperty(final Object element, final String property) {
		return true;
	}

	@Override
	public void removeListener(final ILabelProviderListener listener) {}

}
