/*******************************************************************************************************
 *
 * GamaImageType.java, in gama.extension.image, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.image;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.type;
import gama.annotations.usage;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.constants.IKeyword;
import gama.api.data.factories.GamaMatrixFactory;
import gama.api.data.objects.IField;
import gama.api.data.objects.IMap;
import gama.api.data.objects.IPoint;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.GamaFileType;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITypesManager;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.core.topology.grid.GamaSpatialMatrix;
import gama.core.util.matrix.GamaIntMatrix;
import gama.extension.image.svg.GamaSVGFile;

/**
 * Written by drogoul Modified on 1 ao�t 2010
 *
 * @todo Description
 *
 */

/**
 * The Class GamaImageType.
 */

/**
 * The Class GamaImageType.
 */

@SuppressWarnings ("unchecked")
@type (
		name = IKeyword.IMAGE,
		id = GamaImageType.ID,
		wraps = { GamaImage.class },
		kind = ISymbolKind.Variable.REGULAR,
		doc = { @doc ("Represents ARGB image objects that can be passed directly as arguments to draw statements and other similar functions. "
				+ "An image can be created from many different sources : a field, a grid, a file containing an image, and a number of operators allow to apply filters or to combine them. They can of course be saved on disk") },
		concept = { IConcept.TYPE, IConcept.IMAGE, IConcept.DISPLAY })
public class GamaImageType extends GamaType<GamaImage> {

	/**
	 * @param typesManager
	 * @param varKind
	 * @param id
	 * @param name
	 * @param support
	 */
	public GamaImageType(final ITypesManager typesManager) {
		super(typesManager);
	}

	/** The Constant ID. */
	public static final int ID = IType.BEGINNING_OF_CUSTOM_TYPES + 30;

	/**
	 * Cast.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @param param
	 *            the param
	 * @param copy
	 *            the copy
	 * @return the gama font
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@doc (
			value = "Cast any object to an image",
			usages = { @usage (
					value = "if the operand is a point, returns an empty image of dimensions x and y",
					examples = { @example ("image i <- image({100,100}); // equivalent to image(100, 100)") }),
					@usage (
							value = "if the operand is a grid species, returns an image of the same dimension where each cell gives its color to the corresponding pixel",
							examples = { @example ("image f <- image(my_grid);") }),
					@usage (
							value = "if the operand is a field (or a matrix of float), return an image, where each cell gives a gray value to the corresponding pixel (after normalization)",
							examples = { @example ("image f <- image(my_field);") }),
					@usage (
							value = "if the operand is a string, tries to load the corresponding file (if any) as an image_file and returns its contents, otherwise returns nil if it doesnt exist or if the path does not represent an image file",
							examples = { @example ("image f <- image('/images/image.png');") }),
					@usage (
							value = "in all other cases, return nil",
							examples = { @example ("image f <- image(12); // --> nil") }) })
	@Override
	public GamaImage cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return staticCast(scope, obj, copy);
	}

	/**
	 * Static cast.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @param copy
	 *            the copy
	 * @return the gama font
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static GamaImage staticCast(final IScope scope, final Object obj, final boolean copy)
			throws GamaRuntimeException {
		switch (obj) {
			case GamaImage im -> {
				if (copy) return GamaImage.from(im, im.getAlpha(scope));
				return im;
			}
			case GamaIntMatrix mat -> {
				return GamaImage.from(scope, mat);
			}
			case BufferedImage im -> {
				return GamaImage.from(im, true);
			}
			case Image im -> {
				return ImageHelper.copyToOptimalImage(im);
			}
			case GamaSVGFile g -> {
				return GamaImage.from(g.getImage(scope, true), true, g.getOriginalPath());
			}
			case GamaImageFile f -> {
				return GamaImage.from(f.getImage(scope, true), true, f.getOriginalPath());
			}
			case IPoint p -> {
				return ImageOperators.image((int) p.getX(), (int) p.getY());
			}
			case String s -> {
				return staticCast(scope, GamaFileType.createFile(scope, s, false, null), false);
			}
			case IField f -> {
				return GamaImage.from(scope, f);
			}
			case null, default -> {
			}
		}
		if (obj instanceof ISpecies s && s.isGrid())
			return GamaImage.from((GamaSpatialMatrix) s.getPopulation(scope).getTopology().getPlaces());
		return null;
	}

	/**
	 * Gets the default.
	 *
	 * @return the default
	 */
	@Override
	public GamaImage getDefault() { return null; }

	/**
	 * Can cast to const.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	public boolean isDrawable() { return true; }

	@Override
	public GamaImage copyFromClipboard(final IScope scope) {
		if (ImageConstants.clipboard == null) return null;
		Transferable content = ImageConstants.clipboard.getContents(null);
		if (content == null || !content.isDataFlavorSupported(DataFlavor.imageFlavor)) return null;
		try {
			return staticCast(scope, content.getTransferData(DataFlavor.imageFlavor), false);
		} catch (UnsupportedFlavorException | IOException e) {
			return null;
		}

	}

	@Override
	public GamaImage deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		return GamaImage.from(scope,
				GamaIntMatrix.from(scope, GamaMatrixFactory.createFrom(scope, map2.get("pixels"))));
	}

}
