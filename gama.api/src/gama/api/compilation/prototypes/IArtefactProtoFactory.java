/*******************************************************************************************************
 *
 * IArtefactProtoFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.prototypes;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;

import gama.api.additions.IGamaGetter;
import gama.api.compilation.factories.ISymbolFactory;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Signature;

/**
 *
 */
public interface IArtefactProtoFactory {

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
	IArtefactProto.Symbol createSymbolProto(Class c, boolean isBreakable, boolean isContinuable, boolean isSequence,
			boolean hasArguments, int sKind, boolean b, IArtefactProto.Facet[] fmd, String omissible,
			String[] contextKeywords, int[] contextKinds, boolean isRemoteContext, boolean isUnique,
			boolean name_unique, ISymbolFactory sc, String name, String plugin);

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
	IArtefactProto.Operator createOperatorProto(String name, AnnotatedElement object, IGamaGetter helper, boolean b,
			boolean c, int returnType, Class signature, int typeProvider, int contentTypeProvider, int keyTypeProvider,
			int[] expectedContentType);

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
	IArtefactProto.Operator createOperatorProto(String name, Executable method, String constantDoc, IGamaGetter helper,
			boolean c, boolean b, IType rt, Signature signature, int t, int content, int index, int contentContentType,
			int[] expectedContentTypes, String plugin);

	/**
	 * @param name
	 * @param types
	 * @param ct
	 * @param kt
	 * @param values
	 * @param optional
	 * @param internal
	 * @param isRemote
	 * @return
	 */
	IArtefactProto.Facet createFacetProto(String name, int[] types, int ct, int kt, String[] values, boolean optional,
			boolean internal, boolean isRemote);

}