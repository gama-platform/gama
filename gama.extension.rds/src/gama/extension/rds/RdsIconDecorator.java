/*******************************************************************************************************
 *
 * RdsIconDecorator.java, in gama.extension.rds, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.rds;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.osgi.framework.FrameworkUtil;

/** Loaded only by the optional workbench decoration service, never by headless RDS I/O. */
public final class RdsIconDecorator implements ILightweightLabelDecorator {

	private final ImageDescriptor icon =
			ImageDescriptor.createFromURL(FrameworkUtil.getBundle(RdsIconDecorator.class).getEntry("icons/rds.svg"));

	@Override
	public void decorate(final Object element, final IDecoration decoration) {
		if (decoration.getDecorationContext() instanceof DecorationContext context) {
			context.putProperty(IDecoration.ENABLE_REPLACE, Boolean.TRUE);
			decoration.addOverlay(icon, IDecoration.REPLACE);
		}
	}

	@Override
	public void addListener(final ILabelProviderListener listener) {}

	@Override
	public void removeListener(final ILabelProviderListener listener) {}

	@Override
	public boolean isLabelProperty(final Object element, final String property) { return false; }

	@Override
	public void dispose() {}
}
