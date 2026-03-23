/*******************************************************************************************************
 *
 * ImageLayerStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
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
import gama.annotations.symbol;
import gama.annotations.usage;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.compilation.descriptions.IStatementDescription;
import gama.api.constants.IGamlIssue;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IOutput;
import gama.core.outputs.layers.ImageLayerStatement.ImageLayerValidator;

/**
 * Written by drogoul Modified on 9 nov. 2009
 *
 * @todo Description
 *
 */
@symbol (
		name = IKeyword.IMAGE_LAYER,
		kind = ISymbolKind.LAYER,
		with_sequence = false,
		concept = { IConcept.DISPLAY, IConcept.FILE, IConcept.LOAD_FILE })

@facets (
		value = { @facet (
				name = IKeyword.ROTATE,
				type = { IType.FLOAT },
				optional = true,
				doc = @doc ("Defines the angle of rotation of this layer, in degrees, around the z-axis.")),
				@facet (
						name = IKeyword.POSITION,
						type = IType.POINT,
						optional = true,
						doc = @doc ("position of the upper-left corner of the layer. Note that if coordinates are in [0,1[, the position is relative to the size of the environment (e.g. {0.5,0.5} refers to the middle of the display) whereas it is absolute when coordinates are greater than 1 for x and y. The z-ordinate can only be defined between 0 and 1. The position can only be a 3D point {0.5, 0.5, 0.5}, the last coordinate specifying the elevation of the layer. In case of negative value OpenGl will position the layer out of the environment.")),
				@facet (
						name = IKeyword.SIZE,
						type = IType.POINT,
						optional = true,
						doc = @doc ("extent of the layer in the screen from its position. Coordinates in [0,1[ are treated as percentages of the total surface, while coordinates > 1 are treated as absolute sizes in model units (i.e. considering the model occupies the entire view). Like in 'position', an elevation can be provided with the z coordinate, allowing to scale the layer in the 3 directions ")),
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
						type = { IType.NONE },
						optional = true,
						doc = @doc ("the name/path of the image (in the case of a raster image), a matrix of int, an image file")),
				@facet (
						name = IKeyword.GIS,
						type = { IType.FILE, IType.STRING },
						optional = true,
						doc = @doc ("the name/path of the shape file (to display a shapefile as background, without creating agents from it)")),
				@facet (
						name = IKeyword.COLOR,
						type = IType.COLOR,
						optional = true,
						doc = @doc ("in the case of a shapefile, this the color used to fill in geometries of the shapefile. In the case of an image, it is used to tint the image")),
				@facet (
						name = IKeyword.REFRESH,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("(openGL only) specify whether the image display is refreshed or not. (false by default, true should be used in cases of images that are modified over the simulation)")) },
		omissible = IKeyword.NAME)
@doc (
		value = "`" + IKeyword.IMAGE_LAYER
				+ "` allows modeler to display an image (e.g. as background of a simulation). Note that this image will not be dynamically changed or moved in OpenGL, unless the refresh: facet is set to true.",
		usages = { @usage (
				value = "The general syntax is:",
				examples = { @example (
						value = "display my_display {",
						isExecutable = false),
						@example (
								value = "   image image_file [additional options];",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false) }),
				@usage (
						value = "For instance, in the case of a bitmap image",
						examples = { @example (
								value = "display my_display {",
								isExecutable = false),
								@example (
										value = "   image \"../images/my_backgound.jpg\";",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }),
				@usage (
						value = "If you already have your image stored in a matrix",
						examples = { @example (
								value = "display my_display {",
								isExecutable = false),
								@example (
										value = "   image my_image_matrix;",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }),
				@usage (
						value = "Or in the case of a shapefile:",
						examples = { @example (
								value = "display my_display {",
								isExecutable = false),
								@example (
										value = "   image testGIS gis: \"../includes/building.shp\" color: rgb('blue');",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }),
				@usage (
						value = "It is also possible to superpose images on different layers in the same way as for species using opengl display:",
						examples = { @example (
								value = "display my_display {",
								isExecutable = false),
								@example (
										value = "  image \"../images/image1.jpg\";",
										isExecutable = false),
								@example (
										value = "  image \"../images/image2.jpg\";",
										isExecutable = false),
								@example (
										value = "  image \"../images/image3.jpg\" position: {0,0,0.5};",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }) },
		see = { IKeyword.DISPLAY, IKeyword.AGENTS, IKeyword.CHART, IKeyword.EVENT, "graphics", IKeyword.GRID_LAYER,
				IKeyword.OVERLAY, IKeyword.SPECIES_LAYER })
@validator (ImageLayerValidator.class)
public class ImageLayerStatement extends AbstractLayerStatement {

	/**
	 * The Class ImageLayerValidator.
	 */
	public static class ImageLayerValidator implements IDescriptionValidator<IStatementDescription> {

		@Override
		public void validate(final IStatementDescription description) {
			if (!description.hasFacet(GIS) && !description.hasFacet(NAME)) {
				description.error("Missing facet " + IKeyword.NAME, IGamlIssue.MISSING_FACET,
						description.getUnderlyingElement(), FILE, "\"\"");
			}
		}

	}

	/** The expression to get the image (file or matrix) */
	IExpression imageExpression;

	/**
	 * Instantiates a new image layer statement.
	 *
	 * @param desc
	 *            the desc
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public ImageLayerStatement(final IDescription desc) throws GamaRuntimeException {
		super(desc);
		imageExpression = getFacet(IKeyword.NAME);
	}

	/**
	 * In this particular case, returns false by default;
	 */
	@Override
	public IExpression getRefreshFacet() {
		IExpression exp = super.getRefreshFacet();
		if (exp == null) { exp = GAML.getExpressionFactory().getFalse(); }
		return exp;
	}

	@Override
	public LayerType getType(final IOutput output) {
		if (hasFacet(IKeyword.GIS)) return LayerType.GIS;
		return LayerType.IMAGE;
	}

	// FIXME Use GamaImageFile
	@Override
	public boolean _init(final IScope scope) throws GamaRuntimeException {
		if (imageExpression.isConst()) {
			setName(Cast.asString(scope, imageExpression.value(scope)));
		} else {
			setName(imageExpression.serializeToGaml(false));
		}
		return true;
	}

	@Override
	public boolean _step(final IScope scope) throws GamaRuntimeException {
		return true;
	}

}
