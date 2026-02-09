/*******************************************************************************************************
 *
 * ArtefactProtoFactory.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.prototypes;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;

import gama.api.additions.IGamaGetter;
import gama.api.compilation.factories.ISymbolFactory;
import gama.api.compilation.prototypes.IArtefactProto;
import gama.api.compilation.prototypes.IArtefactProtoFactory;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Signature;

/**
 *
 */
public class ArtefactProtoFactory implements IArtefactProtoFactory {

	/** The Constant INSTANCE. */
	static final ArtefactProtoFactory INSTANCE = new ArtefactProtoFactory();

	/**
	 * Gets the single instance of ArtefactProtoFactory.
	 *
	 * @return single instance of ArtefactProtoFactory
	 */
	public static ArtefactProtoFactory getInstance() { return INSTANCE; }

	/**
	 * Instantiates a new artefact proto factory.
	 */
	protected ArtefactProtoFactory() {
		// Prevent instantiation
	}

	/**
	 * @param c
	 * @param isBreakable
	 * @param isContinuable
	 * @param isSequence
	 * @param hasArguments
	 * @param sKind
	 * @param b
	 * @param fmd
	 * @param omissible
	 * @param contextKeywords
	 * @param contextKinds
	 * @param isRemoteContext
	 * @param isUnique
	 * @param name_unique
	 * @param sc
	 * @param object
	 * @param cURRENT_PLUGIN_NAME
	 * @return
	 */
	@Override
	public IArtefactProto.Symbol createSymbolProto(final Class c, final boolean isBreakable,
			final boolean isContinuable, final boolean isSequence, final boolean hasArguments, final int sKind,
			final boolean b, final IArtefactProto.Facet[] fmd, final String omissible, final String[] contextKeywords,
			final int[] contextKinds, final boolean isRemoteContext, final boolean isUnique, final boolean name_unique,
			final ISymbolFactory sc, final String name, final String plugin) {
		return new SymbolProto(c, isBreakable, isContinuable, isSequence, hasArguments, sKind, b, fmd, omissible,
				contextKeywords, contextKinds, isRemoteContext, isUnique, name_unique, sc, name, plugin);
	}

	/**
	 * @param name
	 * @param object
	 * @param helper
	 * @param b
	 * @param c
	 * @param returnType
	 * @param signature
	 * @param typeProvider
	 * @param contentTypeProvider
	 * @param keyTypeProvider
	 * @param aI
	 * @return
	 */
	@Override
	public IArtefactProto.Operator createOperatorProto(final String name, final AnnotatedElement object,
			final IGamaGetter helper, final boolean b, final boolean c, final int returnType, final Class signature,
			final int typeProvider, final int contentTypeProvider, final int keyTypeProvider,
			final int[] expectedContentType) {
		return new OperatorProto(name, object, helper, b, c, returnType, signature, typeProvider, contentTypeProvider,
				keyTypeProvider, expectedContentType);
	}

	/**
	 * @param kw
	 * @param method
	 * @param object
	 * @param helper
	 * @param c
	 * @param b
	 * @param rt
	 * @param signature
	 * @param t
	 * @param content
	 * @param index
	 * @param contentContentType
	 * @param expectedContentTypes
	 * @param plugin
	 * @return
	 */
	@Override
	public IArtefactProto.Operator createOperatorProto(final String name, final Executable method,
			final String constantDoc, final IGamaGetter helper, final boolean c, final boolean b, final IType rt,
			final Signature signature, final int t, final int content, final int index, final int contentContentType,
			final int[] expectedContentTypes, final String plugin) {
		return new OperatorProto(name, method, constantDoc, helper, c, b, rt, signature, t, content, index,
				contentContentType, expectedContentTypes, plugin);
	}

	@Override
	public IArtefactProto.Facet createFacetProto(final String name, final int[] types, final int ct, final int kt,
			final String[] values, final boolean optional, final boolean internal, final boolean isRemote) {
		return new FacetProto(name, types, ct, kt, values, optional, internal, isRemote);
	}

}
