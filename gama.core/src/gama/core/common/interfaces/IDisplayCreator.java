/*******************************************************************************************************
 *
 * IDisplayCreator.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.interfaces;

import gama.core.outputs.IOutput;
import gama.core.outputs.display.NullDisplaySurface;
import gama.gaml.compilation.GamlAddition;

/**
 * The Interface IDisplayCreator.
 */
@FunctionalInterface
public interface IDisplayCreator {

	/**
	 * The Class DisplayDescription.
	 */
	public static class DisplayDescription extends GamlAddition implements IDisplayCreator {

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
		 * @see gama.core.common.interfaces.IDisplayCreator#create(java.lang.Object[])
		 */
		@Override
		public IDisplaySurface create(final Object... args) {
			if (delegate != null) return delegate.create(args);
			return new NullDisplaySurface();
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
		 * @see gama.gaml.interfaces.IGamlDescription#getTitle()
		 */
		@Override
		public String getTitle() { return "Display supported by " + getName() + ""; }

	}

	/**
	 * Creates the.
	 *
	 * @param args
	 *            the args
	 * @return the i display surface
	 */
	IDisplaySurface create(Object... args);

}
