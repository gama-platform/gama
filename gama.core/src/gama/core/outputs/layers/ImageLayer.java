/*******************************************************************************************************
 *
 * ImageLayer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import java.awt.image.BufferedImage;

import gama.core.common.geometry.Envelope3D;
import gama.core.common.geometry.Scaling3D;
import gama.core.common.interfaces.IGraphics;
import gama.core.common.interfaces.IImageProvider;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.runtime.IScope;
import gama.core.runtime.IScope.IGraphicsScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.runtime.exceptions.GamaRuntimeException.GamaRuntimeFileException;
import gama.core.util.file.IGamaFile;
import gama.core.util.matrix.GamaIntMatrix;
import gama.core.util.matrix.IMatrix;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.statements.draw.AssetDrawingAttributes;
import gama.gaml.types.GamaFileType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * Written by drogoul Modified on 9 nov. 2009
 *
 * @todo Description
 *
 */
public class ImageLayer extends AbstractLayer {

	/** The env. */
	// Cache a copy of both to avoid reloading them each time.
	Envelope3D env;

	/** The cached file. */
	IImageProvider cachedImageProvider;

	/** The file. */
	IExpression provider;

	/** The matrix. */
	IExpression matrix;

	/** The is potentially variable. */
	boolean isFilePotentiallyVariable;

	/** The is potentially variable. */
	boolean isMatrixPotentiallyVariable;

	/** The is file. */
	boolean isImageProvider;

	/** whether it's a matrix or not **/

	/** cached copy to avoid reloading **/
	BufferedImage cachedBufferedImage;

	/**
	 * Instantiates a new image layer.
	 *
	 * @param scope
	 *            the scope
	 * @param layer
	 *            the layer
	 */
	public ImageLayer(final IScope scope, final ILayerStatement layer) {
		super(layer);
		provider = ((ImageLayerStatement) definition).file;
		isImageProvider = isImageProvider();
		isFilePotentiallyVariable = !provider.isContextIndependant();
		matrix = ((ImageLayerStatement) definition).matrix;
		isMatrixPotentiallyVariable = matrix == null ? false : !matrix.isContextIndependant();
		if (matrix == null) {
			if (!isImageProvider) {
				if (provider.isConst() || !isFilePotentiallyVariable) {
					Object value = provider.value(scope);
					if (value instanceof String s) {
						cachedImageProvider = createFileFromString(scope, s);
					} else if (value instanceof IImageProvider p) {
						cachedImageProvider = p;
					} else {
						final String s = Cast.asString(scope, value);
						cachedImageProvider = createFileFromString(scope, s);
					}
					isImageProvider = true;
				}
			} else if (!isFilePotentiallyVariable) {
				cachedImageProvider = createImageProviderFromFileExpression(scope);
				isImageProvider = true;
			}
		} else {
			cachedBufferedImage = GamaIntMatrix.from(scope, Cast.asMatrix(scope, matrix.value(scope))).getImage(scope);
		}

	}

	/**
	 * Checks if is image provider.
	 *
	 * @return true, if is image provider
	 */
	private boolean isImageProvider() {
		IType providerType = provider.getGamlType();
		if (IImageProvider.class.isAssignableFrom(providerType.toClass())
				|| IMatrix.class.isAssignableFrom(providerType.toClass()) && providerType.getContentType() == Types.INT)
			return true;
		return false;
	}

	@Override
	protected ILayerData createData() {
		return new ImageLayerData(definition);
	}

	/**
	 * Creates the file from file expression.
	 *
	 * @param scope
	 *            the scope
	 * @return the gama image file
	 */
	private IImageProvider createImageProviderFromFileExpression(final IScope scope) {
		return verifyFile(scope, provider.value(scope));
	}

	/**
	 * Creates the file from string.
	 *
	 * @param scope
	 *            the scope
	 * @param imageFileName
	 *            the image file name
	 * @return the gama image file
	 */
	private IImageProvider createFileFromString(final IScope scope, final String imageFileName) {
		final IGamaFile result = GamaFileType.createFile(scope, imageFileName, false, null);
		return verifyFile(scope, result);
	}

	/**
	 * Verify file.
	 *
	 * @param scope
	 *            the scope
	 * @param input
	 *            the input
	 * @return the gama image file
	 */
	private IImageProvider verifyFile(final IScope scope, final Object input) throws GamaRuntimeFileException, GamaRuntimeException {
		if (input == cachedImageProvider) return cachedImageProvider;
		if (!(input instanceof IImageProvider result))
			throw GamaRuntimeException.error("Not a provider of images: " + provider.serializeToGaml(false), scope);
		try {
			result.getImage(scope, !getData().getRefresh());
		} catch (final GamaRuntimeFileException ex) {
			throw ex;
		} catch (final Throwable e) {
			throw GamaRuntimeException.create(e, scope);
		}
		cachedImageProvider = result;
		env = computeEnvelope(scope, result);
		return result;
	}

	/**
	 * Compute envelope.
	 *
	 * @param scope
	 *            the scope
	 * @param file
	 *            the file
	 * @return the envelope 3 D
	 */
	private Envelope3D computeEnvelope(final IScope scope, final IImageProvider file) {
		if (file instanceof IGamaFile gf && gf.hasGeoDataAvailable(scope)) return file.computeEnvelope(scope);
		return scope.getSimulation().getEnvelope();
	}

	/**
	 * Builds the image.
	 *
	 * @param scope
	 *            the scope
	 * @return the gama image file
	 */
	protected IImageProvider buildImage(final IScope scope) {
		if (!isFilePotentiallyVariable) return cachedImageProvider;
		return isImageProvider ? createImageProviderFromFileExpression(scope)
				: createFileFromString(scope, Cast.asString(scope, provider.value(scope)));
	}

	/**
	 * Builds the image from matrix.
	 *
	 * @param scope
	 *            the scope
	 * @return the buffered image
	 */
	protected BufferedImage buildImageFromMatrix(final IScope scope) {
		if (!isMatrixPotentiallyVariable) return cachedBufferedImage;
		return GamaIntMatrix.from(scope, Cast.asMatrix(scope, matrix.value(scope))).getImage(scope);
	}

	@Override
	public void privateDraw(final IGraphicsScope scope, final IGraphics dg) {

		// getting the drawing attributes
		final AssetDrawingAttributes attributes = new AssetDrawingAttributes(null, true);
		attributes.setUseCache(!getData().getRefresh());

		final IImageProvider file = buildImage(scope);
		if (env != null) {
			final GamaPoint loc;
			if (dg.is2D()) {
				loc = new GamaPoint(env.getMinX(), env.getMinY());
			} else {
				loc = new GamaPoint(env.getWidth() / 2 + env.getMinX(), env.getHeight() / 2 + env.getMinY());
			}
			attributes.setLocation(loc);
			attributes.setSize(Scaling3D.of(env.getWidth(), env.getHeight(), 0));
		}

		if (file != null) {
			dg.drawAsset(file, attributes);
		} else {
			final BufferedImage img = buildImageFromMatrix(scope);
			if (img != null) { dg.drawImage(img, attributes); }
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		cachedImageProvider = null;
		env = null;
	}

	@Override
	public String getType() { return "Image layer"; }

}
