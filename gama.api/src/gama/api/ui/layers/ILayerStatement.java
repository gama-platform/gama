/*******************************************************************************************************
 *
 * ILayerStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui.layers;

import gama.api.constants.IKeyword;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.symbols.ISymbol;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.IExecutable;
import gama.api.runtime.IStepable;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IOutput;

/**
 * The class ILayerStatement. Supports the GAML definition of layers in a display
 *
 * @author drogoul
 * @since 14 d�c. 2011
 *
 */
public interface ILayerStatement extends IStepable, ISymbol, Comparable<ILayerStatement> {

	/**
	 * Marker interface for event handling statements.
	 *
	 * <p>
	 * Event statements respond to user interactions (mouse clicks, key presses) in displays.
	 * </p>
	 */
	public interface Event extends ILayerStatement {

		/** The trigger. */
		String TRIGGER = "trigger";

		/**
		 * @return
		 */
		String getTrigger();

		/**
		 * @param executionScope
		 * @return
		 */
		IAgent getExecuter(IScope executionScope);

		/**
		 * @param executionScope
		 * @return
		 */
		IExecutable getExecutable(IScope executionScope);

	}

	/**
	 * The Enum LayerType.
	 */
	public enum LayerType {

		/** The grid. */
		GRID(IKeyword.GRID),

		/** The agents. */
		AGENTS(IKeyword.AGENTS),

		/** The grid agents. */
		GRID_AGENTS("grid_agents"),

		/** The species. */
		SPECIES(IKeyword.SPECIES),

		/** The image. */
		IMAGE(IKeyword.IMAGE),

		/** The gis. */
		GIS(IKeyword.GIS),

		/** The chart. */
		CHART(IKeyword.CHART),

		/** The event. */
		EVENT(IKeyword.EVENT),

		/** The graphics. */
		GRAPHICS(IKeyword.GRAPHICS),

		/** The overlay. */
		OVERLAY(IKeyword.OVERLAY),

		/** The camera. */
		CAMERA(IKeyword.CAMERA),

		/** The light. */
		LIGHT("light"),

		/** The mesh. */
		MESH(IKeyword.MESH),

		/** The rotation. */
		ROTATION(IKeyword.ROTATION);

		/** The name. */
		private final String name;

		/**
		 * Instantiates a new layer type.
		 *
		 * @param s
		 *            the s
		 */
		LayerType(final String s) {
			name = s;
		}

		@Override
		public String toString() {
			return name;
		}

	}

	/**
	 * Gets the type.
	 *
	 * @param output
	 *            the output
	 * @return the type
	 */
	LayerType getType(IOutput output);

	/**
	 * Sets the display output.
	 *
	 * @param output
	 *            the new display output
	 */
	void setDisplayOutput(IOutput output);

	/**
	 * Gets the refresh facet.
	 *
	 * @return the refresh facet
	 */
	IExpression getRefreshFacet();

	/**
	 * @return
	 */
	boolean isToCreate();

}