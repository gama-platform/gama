/*******************************************************************************************************
 *
 * DisplayDescription.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui.displays;

import gama.api.additions.GamlAddition;
import gama.api.ui.IOutput;

/**
 * The Class DisplayDescription.
 */
public class DisplayDescription extends GamlAddition implements IDisplayCreator {

	/** The original. */
	private final IDisplayCreator delegate;

	/**
	 * Instantiates a new display description.
	 *
	 * @param original
	 *            the original
	 * @param name
	 *            the name
	 * @param plugin
	 *            the plugin
	 */
	public DisplayDescription(final IDisplayCreator original, final Class<? extends IDisplaySurface> support,
			final String name, final String plugin) {
		super(name, support, plugin);
		this.delegate = original;
	}

	/**
	 * Method create()
	 *
	 * @see gama.api.ui.displays.IDisplayCreator#create(java.lang.Object[])
	 */
	@Override
	public IDisplaySurface create(final IOutput.Display output, final Object uiComponent) {
		if (delegate != null) return delegate.create(output, uiComponent);
		return IDisplaySurface.NULL;
	}

	/**
	 * Method getTitle()
	 *
	 * @see gama.api.compilation.descriptions.IGamlDescription#getTitle()
	 */
	@Override
	public String getTitle() { return "Display supported by " + getName() + ""; }

}