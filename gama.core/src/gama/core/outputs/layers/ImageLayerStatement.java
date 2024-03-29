/*******************************************************************************************************
 *
 * ImageLayerStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.core.common.interfaces.IKeyword;
import gama.core.outputs.LayeredDisplayOutput;
import gama.core.outputs.layers.ImageLayerStatement.ImageLayerValidator;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.compilation.IDescriptionValidator;
import gama.gaml.compilation.annotations.validator;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.StatementDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.expressions.IExpressionFactory;
import gama.gaml.interfaces.IGamlIssue;
import gama.gaml.operators.Cast;
import gama.gaml.types.IType;

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
		value = {
				@facet (
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
	public static class ImageLayerValidator implements IDescriptionValidator<StatementDescription> {

		@Override
		public void validate(final StatementDescription description) {
			if (!description.hasFacet(GIS)) {
				if (!description.hasFacet(NAME)) {
					description.error(
							"Missing facet " + IKeyword.NAME,
							IGamlIssue.MISSING_FACET, description.getUnderlyingElement(), FILE, "\"\"");
				}
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
		if (exp == null) { exp = IExpressionFactory.FALSE_EXPR; }
		return exp;
	}

	@Override
	public LayerType getType(final LayeredDisplayOutput output) {
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
