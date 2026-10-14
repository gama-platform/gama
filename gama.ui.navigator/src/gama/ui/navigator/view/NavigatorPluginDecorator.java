package gama.ui.navigator.view;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import gama.ui.navigator.view.contents.WrappedProject;
import gama.ui.shared.utils.PreferencesHelper;

/**
 * Displays project provenance independently of file metadata.
 */
public class NavigatorPluginDecorator extends LabelProvider implements ILightweightLabelDecorator {

	@Override
	public void decorate(final Object element, final IDecoration decoration) {
		if (!PreferencesHelper.NAVIGATOR_PLUGIN.getValue() || !(element instanceof WrappedProject project)) return;
		final String plugin = project.getPlugin();
		if (plugin != null && !plugin.isBlank()) { decoration.addSuffix(" | in " + plugin); }
	}
}
