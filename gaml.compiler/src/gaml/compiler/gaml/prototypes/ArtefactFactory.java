/*******************************************************************************************************
 *
 * ArtefactFactory.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
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

import gama.annotations.support.ISymbolKind;
import gama.api.additions.IGamaGetter;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.artefacts.IArtefactFactory;
import gama.api.compilation.factories.ISymbolFactory;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Signature;

/**
 *
 */
public class ArtefactFactory implements IArtefactFactory {

	/** The Constant INSTANCE. */
	static final ArtefactFactory INSTANCE = new ArtefactFactory();

	/**
	 * Gets the single instance of ArtefactFactory.
	 *
	 * @return single instance of ArtefactFactory
	 */
	public static ArtefactFactory getInstance() { return INSTANCE; }

	/**
	 * Instantiates a new artefact artefact factory.
	 */
	protected ArtefactFactory() {
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
	public IArtefact.Symbol createSymbolArtefact(final Class c, final boolean isBreakable, final boolean isContinuable,
			final boolean isSequence, final boolean hasArguments, final ISymbolKind sKind, final boolean b,
			final IArtefact.Facet[] fmd, final String omissible, final String[] contextKeywords,
			final int[] contextKinds, final boolean isRemoteContext, final boolean isUnique, final boolean name_unique,
			final ISymbolFactory sc, final String name, final String plugin) {
		return new SymbolArtefact(c, isBreakable, isContinuable, isSequence, hasArguments, sKind, b, fmd, omissible,
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
	public IArtefact.Operator createOperatorArtefact(final String name, final AnnotatedElement object,
			final IGamaGetter helper, final boolean b, final boolean c, final int returnType, final Class signature,
			final int typeProvider, final int contentTypeProvider, final int keyTypeProvider,
			final int[] expectedContentType) {
		return new OperatorArtefact(name, object, helper, b, c, returnType, signature, typeProvider,
				contentTypeProvider, keyTypeProvider, expectedContentType);
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
	public IArtefact.Operator createOperatorArtefact(final String name, final Executable method,
			final String constantDoc, final IGamaGetter helper, final boolean c, final boolean b, final IType rt,
			final Signature signature, final int t, final int content, final int index, final int contentContentType,
			final int[] expectedContentTypes, final String plugin) {
		return new OperatorArtefact(name, method, constantDoc, helper, c, b, rt, signature, t, content, index,
				contentContentType, expectedContentTypes, plugin);
	}

	@Override
	public IArtefact.Facet createFacetArtefact(final String name, final int[] types, final int ct, final int kt,
			final String[] values, final boolean optional, final boolean internal, final boolean isRemote) {
		return new FacetArtefact(name, types, ct, kt, values, optional, internal, isRemote);
	}

}
