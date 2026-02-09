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
	public IDisplaySurface create(final Object... args) {
		if (delegate != null) return delegate.create(args);
		return IDisplaySurface.NULL;
	}

	/**
	 * Creates the.
	 *
	 * @param output
	 *            the output
	 * @param args
	 *            the args
	 * @return the i display surface
	 */
	public IDisplaySurface create(final IOutput output, final Object... args) {
		final Object[] params = new Object[args.length + 1];
		params[0] = output;
		for (int i = 0; i < args.length; i++) { params[i + 1] = args[i]; }
		return create(params);
	}

	/**
	 * Method getTitle()
	 *
	 * @see gama.api.compilation.descriptions.IGamlDescription#getTitle()
	 */
	@Override
	public String getTitle() { return "Display supported by " + getName() + ""; }

}