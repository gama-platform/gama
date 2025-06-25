/*******************************************************************************************************
 *
 * MeshLayerStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.outputs.LayeredDisplayOutput;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.descriptions.IDescription;
import gama.gaml.types.IType;

/**
 * Written by drogoul Modified on 9 nov. 2009
 *
 * @todo Description
 *
 */

/**
 * The Class MeshLayerStatement.
 */

/**
 * The Class MeshLayerStatement.
 */

/**
 * The Class MeshLayerStatement.
 */

/**
 * The Class MeshLayerStatement.
 */
@symbol (
		name = IKeyword.MESH,
		kind = ISymbolKind.LAYER,
		with_sequence = false,
		concept = { IConcept.GRID, IConcept.DISPLAY, IConcept.LAYER })
@inside (
		symbols = IKeyword.DISPLAY)
@facets (
		value = { @facet (
				name = IKeyword.POSITION,
				type = IType.POINT,
				optional = true,
				doc = @doc ("position of the upper-left corner of the layer. Note that if coordinates are in [0,1[, the position is relative to the size of the environment (e.g. {0.5,0.5} refers to the middle of the display) whereas it is absolute when coordinates are greater than 1 for x and y. The z-ordinate can only be defined between 0 and 1. The position can only be a 3D point {0.5, 0.5, 0.5}, the last coordinate specifying the elevation of the layer.")),
				@facet (
						name = IKeyword.ROTATE,
						type = { IType.FLOAT },
						optional = true,
						doc = @doc ("Defines the angle of rotation of this layer, in degrees, around the z-axis.")),
				@facet (
						name = "above",
						type = { IType.FLOAT },
						optional = true,
						doc = @doc ("Can be used to specify a 'minimal' value under which the render will not render the cells with this value")),
				@facet (
						name = "no_data",
						type = { IType.FLOAT },
						optional = true,
						doc = @doc ("Can be used to specify a 'no_data' value, forcing the renderer to not render the cells with this value. If not specified, that value will be searched in the field to display")),
				@facet (
						name = IKeyword.SCALE,
						type = { IType.FLOAT },
						optional = true,
						doc = @doc ("Represents the z-scaling factor, which allows to scale all values of the field. ")),
				@facet (
						name = IKeyword.SIZE,
						type = { IType.POINT, IType.FLOAT },
						optional = true,
						doc = @doc ("Represents the extent of the layer in the screen from its position. Coordinates in [0,1[ are treated as percentages of the total surface, while coordinates > 1 are treated as absolute sizes in model units (i.e. considering the model occupies the entire view). Like in 'position', an elevation can be provided with the z coordinate, allowing to scale the layer in the 3 directions. This latter possibility allows to limit the height of the field. If only a flat value is provided, it is considered implicitly as the z maximal amplitude (or z scaling factor if < 1)")),
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
						name = IKeyword.BORDER,
						type = IType.COLOR,
						optional = true,
						doc = @doc ("the color to draw lines (borders of cells)")),
				@facet (
						name = IKeyword.WIREFRAME,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("if true displays the field in wireframe using the lines color")),
				@facet (
						name = IKeyword.SMOOTH,
						type = { IType.BOOL, IType.INT },
						optional = true,
						doc = @doc ("Applies a simple convolution (box filter) to smooth out the terrain produced by this field. If true, one pass is done with a simple 3x3 kernel. Otherwise, the user can specify the number of successive passes (up to 4). Specifying 0 is equivalent to passing false")),
				@facet (
						name = IKeyword.SOURCE,
						type = { IType.FILE, IType.MATRIX, IType.SPECIES },
						optional = false,
						doc = @doc ("Allows to specify the elevation/value of each cell by passing a grid, a raster, image or csv file or directly a matrix of int/float. The dimensions of the field are those of the file or matrix.")),
				@facet (
						name = IKeyword.TEXTURE,
						type = { IType.FILE },
						optional = true,
						doc = @doc ("A file  containing the texture image to be applied to the field. If not specified, the field will be displayed either in color or grayscale, depending on the other facets. Supersedes both `grayscale` and `color`")),
				@facet (
						name = IKeyword.GRAYSCALE,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("if true, paints each cell with a value of grey depending on its value. Supersedes 'color' if it is defined (it is actually equivalent to passing '#gray' to `color:`). False by default")),
				@facet (
						name = IKeyword.COLOR,
						type = { IType.COLOR, IType.CONTAINER, IType.MAP },
						optional = true,
						doc = @doc ("""
								displays the field using the given color or colors. \
								When a simple color is provided, paints each cell with this color, with a brightness depending on the value of the cell.\
								When a list of colors is provided, they are used in a cyclic manner to color each cell, independently from their value. \
								When this list is casted to a `palette` (using the corresponding operator), it is used to color each cell based on its value (with interpolation between the colors). \
								When a `gradient` (see the corresponding operator) is passed, the interpolation between the two extreme colors is computed by GAMA.\
								When a `scale` (see the corresponding operator) is passed, cells are colored depending on where their value fits in the scale, with no interpolation""")),
				@facet (
						name = IKeyword.TRIANGULATION,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("specifies wether the cells of th field will be triangulated: if it is false, they will be displayed as horizontal squares at a given elevation, whereas if it is true, cells will be triangulated and linked to neighbors in order to have a continuous surface (false by default)")),
				@facet (
						name = IKeyword.TEXT,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("specify whether the value that represents the elevation is displayed on each cell (false by default)")),
				@facet (
						name = IKeyword.REFRESH,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("(openGL only) specify whether the display of the species is refreshed. (true by default, but should be deactivated if the field is static)")) },
		omissible = IKeyword.SOURCE)
@doc (
		value = "Allows the modeler to display in an optimized way a field of values, optionally using elevation. Useful for displaying DEMs, for instance, without having to load them into a grid. Can be fed with a matrix of int/float, a grid, a csv/raster/image file and supports many visualisation options",
		usages = { @usage (
				value = "The general syntax is:",
				examples = { @example (
						value = "display my_display {",
						isExecutable = false),
						@example (
								value = "   field a_filename lines: #black position: { 0.5, 0 } size: {0.5,0.5} triangulated: true texture: anothe_file;",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false) }), },
		see = { IKeyword.DISPLAY, IKeyword.AGENTS, IKeyword.GRID, IKeyword.EVENT, "graphics", IKeyword.IMAGE,
				IKeyword.OVERLAY, IKeyword.SPECIES_LAYER })
// @validator (OpenGLSpecificLayerValidator.class)
public class MeshLayerStatement extends AbstractLayerStatement {

	/**
	 * Instantiates a new mesh layer statement.
	 *
	 * @param desc
	 *            the desc
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public MeshLayerStatement(final IDescription desc) throws GamaRuntimeException {
		super(desc);
		setName(getFacet(IKeyword.SOURCE).literalValue());
	}

	@Override
	public boolean _init(final IScope scope) throws GamaRuntimeException {
		return true;
	}

	@Override
	public LayerType getType(final LayeredDisplayOutput out) {
		return LayerType.MESH;
	}

	@Override
	public boolean _step(final IScope sim) throws GamaRuntimeException {
		return true;
	}

}
