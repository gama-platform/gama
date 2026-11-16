package gama.ui.navigator.view.contents;

import org.eclipse.jface.resource.ImageDescriptor;

import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.PreferencesHelper;

/** Resolves the selected set at label refresh time, preserving existing node identities. */
final class NavigatorIcons {

	private NavigatorIcons() {}

	static ImageDescriptor descriptor(final VirtualContent<?> content) {
		final ImageDescriptor current = content.getImageDescriptor();
		if (current == null) return null;
		final String symbol = switch (content.getType()) {
			case PROJECT -> current == GamaIcon.named(IGamaIcons.FOLDER_PROJECT).descriptor() ? "project" : null;
			case FOLDER -> current == GamaIcon.named(IGamaIcons.FOLDER_MODEL).descriptor() ? "model-folder"
					: current == GamaIcon.named(IGamaIcons.FOLDER_RESOURCES).descriptor() ? "resource-folder" : null;
			default -> null;
		};
		return symbol == null ? current : descriptor(symbol, current);
	}

	static ImageDescriptor descriptor(final String symbol, final ImageDescriptor current) {
		final String style = switch (PreferencesHelper.NAVIGATOR_ICON_SET.getValue()) {
			case "Duotone" -> "duotone";
			case "Retro" -> "retro";
			case "Petrol" -> "petrol";
			case "Outline" -> "outline";
			case "Mosaic" -> "mosaic";
			case "Orbit" -> "orbit";
			case "Orbit Outline" -> "orbit-outline";
			case "Folio" -> "folio";
			case "Folio Color" -> "folio-color";
			default -> null;
		};
		return style == null ? current : GamaIcon.named("navigator/categories/" + style + "/" + symbol).descriptor();
	}
}
