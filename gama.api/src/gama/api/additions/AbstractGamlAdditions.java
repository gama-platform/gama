/*******************************************************************************************************
 *
 * AbstractGamlAdditions.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.additions;

import static gama.api.additions.GamaBundleLoader.CURRENT_PLUGIN_NAME;
import static gama.api.constants.IKeyword.OF;
import static gama.api.constants.IKeyword.SPECIES;
import static gama.api.constants.IKeyword._DOT;
import static gama.api.gaml.types.Types.builtInTypes;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import gama.annotations.support.ISymbolKind;
import gama.annotations.support.ITypeProvider;
import gama.api.additions.registries.ArtefactProtoRegistry;
import gama.api.additions.registries.GamaAdditionRegistry;
import gama.api.additions.registries.GamaSkillRegistry;
import gama.api.compilation.descriptions.IActionDescription;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IExperimentDescription;
import gama.api.compilation.descriptions.ISkillDescription;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.compilation.factories.ISymbolFactory;
import gama.api.compilation.prototypes.IArtefactProto;
import gama.api.constants.IKeyword;
import gama.api.gaml.GAML;
import gama.api.gaml.symbols.IVariable;
import gama.api.gaml.types.GamaFileType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ParametricFileType;
import gama.api.gaml.types.Signature;
import gama.api.gaml.types.Types;
import gama.api.kernel.GamaMetaModel;
import gama.api.kernel.agent.IAgentConstructor;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.IExperimentAgentCreator;
import gama.api.kernel.simulation.IExperimentAgentCreator.ExperimentAgentDescription;
import gama.api.ui.displays.DisplayDescription;
import gama.api.ui.displays.IDisplayCreator;
import gama.api.ui.displays.IDisplaySurface;
import gama.api.utils.files.IGamaFile;

/**
 *
 * The class AbstractGamlAdditions. Default base implementation for plugins' gaml additions.
 *
 * @author drogoul
 * @since 17 mai 2012
 *
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public abstract class AbstractGamlAdditions extends UtilsForGamlAdditions implements IGamlAdditions {

	/**
	 * Display.
	 *
	 * @param string
	 *            the string
	 * @param d
	 *            the d
	 */
	public void _display(final String string, final Class<? extends IDisplaySurface> support, final IDisplayCreator d) {
		GAML.addConstants(string);
		GamaAdditionRegistry.addDelegate(string, new DisplayDescription(d, support, string, CURRENT_PLUGIN_NAME));
		GamaBundleLoader.addDisplayPlugin(CURRENT_PLUGIN_NAME);
	}

	/**
	 * Experiment.
	 *
	 * @param string
	 *            the string
	 * @param d
	 *            the d
	 */
	public void _experiment(final String string, final IExperimentAgentCreator d,
			final Class<? extends IExperimentAgent> clazz) {
		GAML.addConstants(string);
		IExperimentDescription.addExperimentAgentCreator(string,
				new ExperimentAgentDescription(d, clazz, string, CURRENT_PLUGIN_NAME));
	}

	/**
	 * Species.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param helper
	 *            the helper
	 * @param skills
	 *            the skills
	 */
	public void _species(final String name, final Class clazz, final IAgentConstructor helper, final String... skills) {
		GamaMetaModel.INSTANCE.addSpecies(name, clazz, helper, skills);
	}

	/**
	 * Type.
	 *
	 * @param keyword
	 *            the keyword
	 * @param typeInstance
	 *            the type instance
	 * @param id
	 *            the id
	 * @param varKind
	 *            the var kind
	 * @param wraps
	 *            the wraps
	 */
	protected void _type(final String keyword, final IType typeInstance, final int id, final int varKind,
			final Class... wraps) {
		final IType<?> type = builtInTypes.initType(keyword, typeInstance, id, varKind, wraps[0], CURRENT_PLUGIN_NAME);
		for (final Class cc : wraps) { Types.addClassTypeCorrespondance(cc, type.getName()); }
		Types.cache(id, typeInstance);
	}

	/**
	 * File.
	 *
	 * @param string
	 *            the string
	 * @param clazz
	 *            the clazz
	 * @param helper
	 *            the helper
	 * @param innerType
	 *            the inner type
	 * @param keyType
	 *            the key type
	 * @param contentType
	 *            the content type
	 * @param s
	 *            the s
	 */
	protected void _file(final String string, final Class clazz, final IGamaGetter<IGamaFile<?, ?>> helper,
			final int innerType, final int keyType, final int contentType, final String[] s) {
		GamaFileType.addFileTypeDefinition(string, Types.get(innerType), Types.get(keyType), Types.get(contentType),
				clazz, helper, s, CURRENT_PLUGIN_NAME);
		ArtefactProtoRegistry.addNewVarKeyword(string + "_file", ISymbolKind.Variable.CONTAINER);
	}

	/**
	 * Skill.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param species
	 *            the species
	 */
	protected void _skill(final String name, final Class clazz, final String... species) {
		Iterable<IDescription> children = GAML.getAdditions(clazz);
		if (children != null) {
			for (final IDescription d : children) {
				d.setOriginName("skill " + name);
				d.setDefiningPlugin(CURRENT_PLUGIN_NAME);
			}
		}
		final ISkillDescription sd =
				GAML.getDescriptionFactory().createBuiltInSkillDescription(name, clazz, children, CURRENT_PLUGIN_NAME);

		GamaSkillRegistry.INSTANCE.register(sd, clazz);
		for (final String spec : species) { GamaMetaModel.INSTANCE.addSpeciesSkill(spec, name); }
	}

	/**
	 * Symbol.
	 *
	 * @param names
	 *            the names
	 * @param c
	 *            the c
	 * @param sKind
	 *            the s kind
	 * @param isRemoteContext
	 *            the remote
	 * @param hasArguments
	 *            the args
	 * @param scope
	 *            the scope
	 * @param isSequence
	 *            the sequence
	 * @param isUnique
	 *            the unique
	 * @param name_unique
	 *            the name unique
	 * @param contextKeywords
	 *            the context keywords
	 * @param contextKinds
	 *            the context kinds
	 * @param fmd
	 *            the fmd
	 * @param omissible
	 *            the omissible
	 * @param sc
	 *            the sc
	 */
	protected void _symbol(final String[] names, final Class c, final int sKind, final boolean isBreakable,
			final boolean isContinuable, final boolean isRemoteContext, final boolean hasArguments, final boolean scope,
			final boolean isSequence, final boolean isUnique, final boolean name_unique, final String[] contextKeywords,
			final int[] contextKinds, final IArtefactProto.Facet[] fmd, final String omissible,
			final ISymbolFactory sc) {
		final Collection<String> keywords;
		if (ISymbolKind.Variable.KINDS.contains(sKind)) {
			keywords = ArtefactProtoRegistry.VARKIND2KEYWORDS.get(sKind);
			keywords.remove(SPECIES);
		} else {
			keywords = Arrays.asList(names);
		}
		final IArtefactProto.Symbol md = GAML.getArtefactProtoFactory().createSymbolProto(c, isBreakable, isContinuable,
				isSequence, hasArguments, sKind, !scope, fmd, omissible, contextKeywords, contextKinds, isRemoteContext,
				isUnique, name_unique, sc, names == null || names.length == 0 ? "variable declaration" : names[0],
				CURRENT_PLUGIN_NAME);
		ArtefactProtoRegistry.addProto(md, keywords);
	}

	/**
	 * Operator.
	 *
	 * @param keywords
	 *            the keywords
	 * @param method
	 *            the method
	 * @param doc
	 *            an optional string that contains the constant doc of the operator (can be null)
	 * @param expectedContentTypes
	 *            the expected content types
	 * @param returnClassOrType
	 *            the return class or type
	 * @param c
	 *            the c
	 * @param t
	 *            the t
	 * @param content
	 *            the content
	 * @param index
	 *            the index
	 * @param contentContentType
	 *            the content content type
	 * @param helper
	 *            the helper
	 */
	public void _operator(final String[] keywords, final Executable method, final String doc,
			final int[] expectedContentTypes, final Object returnClassOrType, final boolean c, final int t,
			final int content, final int index, final int contentContentType, final IGamaGetter helper,
			final boolean isIterator) {

		if (isIterator) { GAML.addIterators(keywords); }
		final Signature signature = method == null ? new Signature(Types.NO_TYPE) : new Signature(method);
		int nbParameters = signature.size();
		final String plugin = GamaBundleLoader.CURRENT_PLUGIN_NAME;
		final IType rt;
		if (returnClassOrType instanceof Class) {
			rt = Types.get((Class) returnClassOrType);
		} else {
			rt = (IType) returnClassOrType;
		}
		for (final String keyword : keywords) {
			final String kw = keyword;
			if (!GAML.OPERATORS.containsKey(kw)) { GAML.OPERATORS.put(kw, new HashMap()); }
			final Map<Signature, IArtefactProto.Operator> map = GAML.OPERATORS.get(kw);
			if (!map.containsKey(signature)) {
				IArtefactProto.Operator proto;
				if (nbParameters == 2 && (OF.equals(kw) || _DOT.equals(kw)) && signature.get(0).isAgentType()) {
					proto = GAML.getArtefactProtoFactory().createOperatorProto(kw, method, null, helper, c, true, rt,
							signature, t, content, index, contentContentType, expectedContentTypes, plugin);
				} else {
					proto = GAML.getArtefactProtoFactory().createOperatorProto(kw, method, doc, helper, c, false, rt,
							signature, t, content, index, contentContentType, expectedContentTypes, plugin);
				}
				map.put(signature, proto);
			}
		}

	}

	/**
	 * Listener.
	 *
	 * @param varName
	 *            the var name
	 * @param clazz
	 *            the clazz
	 * @param helper
	 *            the helper
	 */
	public void _listener(final String varName, final Class clazz, final IGamaHelper helper) {
		GamaHelper gh = new GamaHelper(varName, clazz, helper);
		IVariable.addListenerByClass(clazz, gh);
		IVariable.addListenerByName(varName, clazz);
	}

	/**
	 * Operator.
	 *
	 * @param keywords
	 *            the keywords
	 * @param method
	 *            the method
	 * @param classes
	 *            the classes
	 * @param expectedContentTypes
	 *            the expected content types
	 * @param ret
	 *            the ret
	 * @param c
	 *            the c
	 * @param typeAlias
	 *            the type alias
	 * @param helper
	 *            the helper
	 */
	// For files
	public void _operator(final String[] keywords, final Executable method, final int content,
			final int[] expectedContentTypes, final Class ret, final boolean c, final String typeAlias,
			final IGamaGetter helper) {
		final ParametricFileType fileType = GamaFileType.getTypeFromAlias(typeAlias);
		this._operator(keywords, method, null, expectedContentTypes, fileType, c, ITypeProvider.NONE, content,
				ITypeProvider.NONE, ITypeProvider.NONE, helper, false);
	}

	/**
	 * Var.
	 *
	 * @param clazz
	 *            the clazz
	 * @param desc
	 *            the desc
	 * @param get
	 *            the get
	 * @param init
	 *            the init
	 * @param set
	 *            the set
	 */
	protected void _var(final Class clazz, final IDescription desc, final IGamaHelper get, final IGamaHelper init,
			final IGamaHelper set) {
		GAML.addAddition(clazz, desc);
		IVariableDescription vd = (IVariableDescription) desc;
		vd.addHelpers(clazz, get, init, set);
		vd.setDefiningPlugin(GamaBundleLoader.CURRENT_PLUGIN_NAME);
		vd.setDefinitionClass(clazz);
	}

	/**
	 * Facet.
	 *
	 * @param name
	 *            the name
	 * @param types
	 *            the types
	 * @param ct
	 *            the ct
	 * @param kt
	 *            the kt
	 * @param values
	 *            the values
	 * @param optional
	 *            the optional
	 * @param internal
	 *            the internal
	 * @param isRemote
	 *            the is remote
	 * @return the facet proto
	 */
	protected IArtefactProto.Facet _facet(final String name, final int[] types, final int ct, final int kt,
			final String[] values, final boolean optional, final boolean internal, final boolean isRemote) {
		return GAML.getArtefactProtoFactory().createFacetProto(name, types, ct, kt, values, optional, internal,
				isRemote);
	}

	/**
	 * Field.
	 *
	 * @param clazz
	 *            the clazz
	 * @param getter
	 *            the getter
	 */
	protected void _field(final Class clazz, final String name, final IGamaGetter helper, final int returnType,
			final Class signature, final int typeProvider, final int contentTypeProvider, final int keyTypeProvider) {
		IArtefactProto proto = GAML.getArtefactProtoFactory().createOperatorProto(name, null, helper, false, true,
				returnType, signature, typeProvider, contentTypeProvider, keyTypeProvider, AI);
		GAML.addField(clazz, proto);
	}

	/**
	 * Constants.
	 *
	 * @param strings
	 *            the strings
	 */
	public static void _constants(final String[]... strings) {
		for (final String[] s : strings) { GAML.addConstants(s); }
	}

	/**
	 * Arg.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param optional
	 *            the optional
	 * @return the i description
	 */
	public IDescription _arg(final String name, final int type, final boolean optional) {
		// For IDs and labels, we add a specific facet (ID) -- see #3627
		if (type <= IType.LABEL) return desc(IKeyword.ARG, IKeyword.NAME, name, IKeyword.ID, "true", IKeyword.TYPE,
				String.valueOf(type), IKeyword.OPTIONAL, optional ? "true" : "false");
		return desc(IKeyword.ARG, IKeyword.NAME, name, IKeyword.TYPE, String.valueOf(type), IKeyword.OPTIONAL,
				optional ? "true" : "false");
	}

	/**
	 * Action.
	 *
	 * @param e
	 *            the e
	 * @param desc
	 *            the desc
	 * @param method
	 *            the method
	 */
	protected void _action(final IGamaHelper e, final IDescription desc, final Method method) {
		((IActionDescription) desc).setHelper(e, method);
		desc.setDefiningPlugin(GamaBundleLoader.CURRENT_PLUGIN_NAME);
		GAML.addAddition(method.getDeclaringClass(), desc);
	}

}
