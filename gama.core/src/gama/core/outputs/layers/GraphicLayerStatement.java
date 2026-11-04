/*******************************************************************************************************
 *
 * GraphicLayerStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.usage;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.compilation.descriptions.IDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IOutput;
import gama.api.annotations.validator;
import gama.dev.COUNTER;
import gama.gaml.statements.AspectStatement;

/**
 * The Class GraphicLayerStatement.
 */
@symbol (
		name = "graphics",
		kind = ISymbolKind.LAYER,
		with_sequence = true,
		concept = { IConcept.DISPLAY })
@inside (
		symbols = IKeyword.DISPLAY)
@facets (
		value = { @facet (
				name = LayerData.COORDINATES, type = IType.STRING,
				optional = true, doc = @doc (LayerData.COORDINATES_DOC)),
				@facet (
						name = LayerData.REFERENTIAL, type = IType.STRING,
						optional = true, doc = @doc (LayerData.REFERENTIAL_DOC)),
				@facet (
				name = IKeyword.POSITION,
				type = IType.POINT,
				optional = true,
				doc = @doc (LayerData.POSITION_DOC)),
				@facet (
						name = IKeyword.BACKGROUND,
						type = IType.COLOR,
						optional = true,
						doc = @doc ("the background color of the layer. Default is none")),
				@facet (
						name = IKeyword.BORDER,
						type = IType.COLOR,
						optional = true,
						doc = @doc ("Color to apply to the border of the rectangular shape of the layer. Default is none")),

				@facet (
						name = IKeyword.ROTATE,
						type = { IType.FLOAT },
						optional = true,
						doc = @doc ("Defines the angle of rotation of this layer, in degrees, around the z-axis.")),
				@facet (
						name = IKeyword.TRACE,
						type = { IType.BOOL, IType.INT },
						optional = true,
						doc = @doc ("Allows to aggregate the visualization at each timestep on the display. Default is false. If set to an int value, only the last n-th steps will be visualized. If set to true, no limit of timesteps is applied. ")),
				@facet (
						name = IKeyword.FADING,
						type = { IType.BOOL },
						optional = true,
						doc = @doc ("Used in conjunction with 'trace:', allows to apply a fading effect to the previous traces. Default is false")),
				@facet (
						name = IKeyword.SIZE,
						type = IType.POINT,
						optional = true,
						doc = @doc (LayerData.SIZE_DOC)),
				@facet (
						name = IKeyword.TRANSPARENCY,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("the transparency level of the layer (between 0 -- opaque -- and 1 -- fully transparent)")),
				@facet (
						name = IKeyword.VISIBLE,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Defines whether this layer is visible or not")),
				@facet (
						name = IKeyword.NAME,
						type = IType.LABEL,
						optional = true,
						doc = @doc ("the human readable title of the graphics")),
				@facet (
						name = IKeyword.REFRESH,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("(openGL only) specify whether the display of the species is refreshed. (true by default, usefull in case of agents that do not move)")) },
		omissible = IKeyword.NAME)
@doc (
		value = "`graphics` allows the modeler to freely draw shapes/geometries/texts without having to define a species. It works exactly like a species [Aspect161 aspect]: the draw statement can be used in the same way.",
		usages = { @usage (
				value = "The general syntax is:",
				examples = { @example (
						value = "display my_display {",
						isExecutable = false),
						@example (
								value = "   graphics \"my new layer\" {",
								isExecutable = false),
						@example (
								value = "      draw circle(5) at: {10,10} color: #red;",
								isExecutable = false),
						@example (
								value = "      draw \"test\" at: {10,10} size: 20 color: #black;",
								isExecutable = false),
						@example (
								value = "   }",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false) }) },
		see = { IKeyword.DISPLAY, IKeyword.AGENTS, IKeyword.CHART, IKeyword.EVENT, "graphics", IKeyword.GRID_LAYER,
				IKeyword.IMAGE_LAYER, IKeyword.OVERLAY, IKeyword.SPECIES_LAYER })
@validator (AbstractLayerStatement.CoordinatesValidator.class)
public class GraphicLayerStatement extends AbstractLayerStatement {

	/** The aspect. */
	AspectStatement aspect;

	/**
	 * Instantiates a new graphic layer statement.
	 *
	 * @param desc
	 *            the desc
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public GraphicLayerStatement(final IDescription desc) throws GamaRuntimeException {
		super(desc);
		final IDescription d = GAML.getDescriptionFactory().create(IKeyword.ASPECT, desc, IKeyword.NAME,
				"graphic_aspect" + COUNTER.GET_UNIQUE());
		aspect = new AspectStatement(d);
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> children) {
		aspect.setChildren(children);
	}

	/**
	 * Gets the aspect.
	 *
	 * @return the aspect
	 */
	public AspectStatement getAspect() { return aspect; }

	@Override
	public LayerType getType(final IOutput output) {
		return LayerType.GRAPHICS;
	}

	@Override
	protected boolean _init(final IScope scope) {
		return true;
	}

	@Override
	protected boolean _step(final IScope scope) {
		return true;
	}

}
