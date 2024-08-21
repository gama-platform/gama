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
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.statements.draw.AssetDrawingAttributes;
import gama.gaml.types.GamaFileType;

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

	/** The provider. */
	IExpression providerExpression;

	/** The is potentially variable. */
	boolean isProviderPotentiallyVariable;

	/** The is file. */
	boolean isImageProvider;

//	/** cached copy to avoid reloading **/
//	BufferedImage cachedBufferedImage;

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
		providerExpression = ((ImageLayerStatement) definition).imageExpression;
		isImageProvider = isImageProvider(scope);
		isProviderPotentiallyVariable = !providerExpression.isContextIndependant();
		if (!isImageProvider) {
			if (providerExpression.isConst() || !isProviderPotentiallyVariable) {
				Object value = providerExpression.value(scope);
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
		} else if (!isProviderPotentiallyVariable) {
			cachedImageProvider = getImageFromProvider(scope, providerExpression.value(scope));
			isImageProvider = true;
		}

	}

	/**
	 * Checks if is image provider.
	 *
	 * @return true, if is image provider
	 */
	private boolean isImageProvider(IScope scope) {
		var providerClass = providerExpression.value(scope).getClass(); // Needs to be evaluated else what is GamaIntMatrix at runtime is considered GamaMatrix
		return IImageProvider.class.isAssignableFrom(providerClass); 
	}

	@Override
	protected ILayerData createData() {
		return new ImageLayerData(definition);
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
		final IGamaFile<?, ?> result = GamaFileType.createFile(scope, imageFileName, false, null);
		return getImageFromProvider(scope, result);
	}

	/**
	 * Verify that the provider and the environment is well setup and use it to generate an image (reuse the previous one in case it should be cached)
	 *
	 * @param scope
	 *            the scope
	 * @param input
	 *            the input
	 * @return the gama image provider after the image generation
	 */
	private IImageProvider getImageFromProvider(final IScope scope, final Object input) throws GamaRuntimeFileException, GamaRuntimeException {
		if (input == cachedImageProvider) return cachedImageProvider;
		if (!(input instanceof IImageProvider result))
			throw GamaRuntimeException.error("Not a provider of images: " + providerExpression.serializeToGaml(false), scope);
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
	 * Builds the image and returns the provider
	 *
	 * @param scope
	 *            the scope
	 * @return the gama image provider
	 */
	protected IImageProvider buildImage(final IScope scope) {
		if (!isProviderPotentiallyVariable) return cachedImageProvider;
		return 		isImageProvider 
				? getImageFromProvider(scope, providerExpression.value(scope))
				: createFileFromString(scope, Cast.asString(scope, providerExpression.value(scope)));
	}


	@Override
	public void privateDraw(final IGraphicsScope scope, final IGraphics dg) {

		// getting the drawing attributes
		final AssetDrawingAttributes attributes = new AssetDrawingAttributes(null, true);
		attributes.setUseCache(!getData().getRefresh());

		final IImageProvider actualProvider = buildImage(scope);
		if (env != null) {
			final GamaPoint loc;
			if (dg.is2D()) {
				loc = new GamaPoint(env.getMinX(), env.getMinY());
			} else {
				loc = new GamaPoint(env.getMinX() + env.getWidth() / 2, env.getMinY() + env.getHeight() / 2);
			}
			attributes.setLocation(loc);
			attributes.setSize(Scaling3D.of(env.getWidth(), env.getHeight(), 0));
		}

		if (actualProvider != null) {
			// TODO: possibly drawn a second time ? shouldn't we use drawImage and return a BufferedImage from buildImage instead ?
			dg.drawAsset(actualProvider, attributes);				
		}
		else {
			//TODO: should probably raise some kind of error/warning
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
