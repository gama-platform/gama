/*******************************************************************************************************
 *
 * SymbolProto.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.prototypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import gama.annotations.action;
import gama.annotations.doc;
import gama.annotations.symbol;
import gama.annotations.support.ISymbolKind;
import gama.api.additions.registries.ArtefactProtoRegistry;
import gama.api.annotations.serializer;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.compilation.factories.ISymbolFactory;
import gama.api.compilation.prototypes.IArtefactProto;
import gama.api.compilation.serialization.ISymbolSerializer;
import gama.api.compilation.validation.IValidator;
import gama.api.constants.IKeyword;
import gama.api.gaml.symbols.ISymbol;
import gama.api.utils.GamlProperties;

/**
 * Written by drogoul Modified on 8 févr. 2010
 *
 * @todo Description
 *
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class SymbolProto extends AbstractProto implements IArtefactProto.Symbol {

	/** The null validator. */
	static IValidator NULL_VALIDATOR = (d, e, i) -> true;

	/** The constructor. */
	private final ISymbolFactory constructor;

	/** The validator. */
	private IValidator validator;

	/** The serializer. */
	private ISymbolSerializer serializer;

	/** The factory. */
	// private final ISymbolDescriptionFactory factory;

	/** The kind. */
	private final int kind;

	/** The is unique in context. */
	private final boolean hasSequence, hasArgs, hasScope, isRemoteContext, isUniqueInContext;

	/** The context keywords. */
	private final ImmutableSet<String> contextKeywords;

	/** The context kinds. */
	private final boolean[] contextKinds = new boolean[ISymbolKind.__NUMBER__];

	/** The possible facets. */
	private final Map<String, IArtefactProto.Facet> possibleFacets;

	/** The mandatory facets. */
	private final ImmutableList<String> mandatoryFacets;

	/** The omissible facet. */
	private final String omissibleFacet;

	/** The is primitive. */
	private final boolean isPrimitive;

	/** The is breakable. */
	private final boolean isBreakable;

	/** The is continuable. */
	private final boolean isContinuable;

	/** The is var. */
	private final boolean isVar;

	/**
	 * Instantiates a new symbol proto.
	 *
	 * @param clazz
	 *            the clazz
	 * @param hasSequence
	 *            the has sequence
	 * @param hasArgs
	 *            the has args
	 * @param kind
	 *            the kind
	 * @param doesNotHaveScope
	 *            the does not have scope
	 * @param possibleFacets
	 *            the possible facets
	 * @param omissible
	 *            the omissible
	 * @param contextKeywords
	 *            the context keywords
	 * @param parentKinds
	 *            the parent kinds
	 * @param isRemoteContext
	 *            the is remote context
	 * @param isUniqueInContext
	 *            the is unique in context
	 * @param nameUniqueInContext
	 *            the name unique in context
	 * @param constr
	 *            the constr
	 * @param name
	 *            the name
	 * @param plugin
	 *            the plugin
	 */
	public SymbolProto(final Class clazz, final boolean isBreakable, final boolean isContinuable,
			final boolean hasSequence, final boolean hasArgs, final int kind, final boolean doesNotHaveScope,
			final IArtefactProto.Facet[] possibleFacets, final String omissible, final String[] contextKeywords,
			final int[] parentKinds, final boolean isRemoteContext, final boolean isUniqueInContext,
			final boolean nameUniqueInContext, final ISymbolFactory constr, final String name, final String plugin) {
		super(name, clazz, plugin);
		// factory = DescriptionFactory.getFactory(kind);
		constructor = constr;
		this.isBreakable = isBreakable;
		this.isContinuable = isContinuable;
		if (isContinuable) { ArtefactProtoRegistry.CONTINUABLE_STATEMENTS.add(name); }
		if (isBreakable) { ArtefactProtoRegistry.BREAKABLE_STATEMENTS.add(name); }
		this.isRemoteContext = isRemoteContext;
		this.hasSequence = hasSequence;
		this.isPrimitive = IKeyword.PRIMITIVE.equals(name);
		this.hasArgs = hasArgs;
		this.omissibleFacet = omissible;
		this.isUniqueInContext = isUniqueInContext;
		this.kind = kind;
		this.isVar = ISymbolKind.Variable.KINDS.contains(kind);
		this.hasScope = !doesNotHaveScope;
		if (possibleFacets != null) {
			final ImmutableList.Builder<String> builder = ImmutableList.builder();
			this.possibleFacets = new HashMap<>();
			for (final IArtefactProto.Facet f : possibleFacets) {
				this.possibleFacets.put(f.getName(), f);
				f.setOwner(getTitle());
				f.setClass(clazz);
				if (!f.isOptional()) { builder.add(f.getName()); }
			}
			mandatoryFacets = builder.build();
		} else {
			this.possibleFacets = null;
			mandatoryFacets = null;
		}
		this.contextKeywords = ImmutableSet.copyOf(contextKeywords);
		Arrays.fill(this.contextKinds, false);
		for (final int i : parentKinds) { contextKinds[i] = true; }
	}

	/**
	 * Gets the factory.
	 *
	 * @return the factory
	 */
	// @Override
	// public ISymbolDescriptionFactory getFactory() { return factory; }

	/**
	 * Checks if is remote context.
	 *
	 * @return true, if is remote context
	 */
	@Override
	public boolean isRemoteContext() { return isRemoteContext; }

	/**
	 * Checks if is label.
	 *
	 * @param s
	 *            the s
	 * @return true, if is label
	 */
	@Override
	public boolean isLabel(final String s) {
		final Facet f = getPossibleFacets().get(s);
		if (f == null) return false;
		return f.isLabel();
	}

	/**
	 * Checks if is id.
	 *
	 * @param s
	 *            the s
	 * @return true, if is id
	 */
	@Override
	public boolean isId(final String s) {
		final Facet f = getPossibleFacets().get(s);
		if (f == null) return false;
		return f.isId();
	}

	/**
	 * Checks for sequence.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean hasSequence() {
		return hasSequence;
	}

	/**
	 * Checks if is primitive.
	 *
	 * @return true, if is primitive
	 */
	@Override
	public boolean isPrimitive() { return isPrimitive; }

	/**
	 * Checks for args.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean hasArgs() {
		return hasArgs;
	}

	/**
	 * Checks for scope.
	 *
	 * @return true, if successful
	 */
	public boolean hasScope() {
		return hasScope;
	}

	/**
	 * Gets the possible facets.
	 *
	 * @return the possible facets
	 */
	@Override
	public Map<String, IArtefactProto.Facet> getPossibleFacets() {
		return possibleFacets == null ? Collections.emptyMap() : possibleFacets;
	}

	/**
	 * Checks if is top level.
	 *
	 * @return true, if is top level
	 */
	@Override
	public boolean isTopLevel() { return kind == ISymbolKind.BEHAVIOR; }

	/**
	 * Gets the factory.
	 *
	 * @return the factory
	 */
	@Override
	public int getKind() { return kind; }

	/**
	 * Gets the constructor.
	 *
	 * @return the constructor
	 */
	public ISymbolFactory getConstructor() { return constructor; }

	/**
	 * @return
	 */
	@Override
	public String getOmissible() { return omissibleFacet; }

	@Override
	public String getTitle() {
		return isVar ? ISymbolKind.Variable.KINDS_AS_STRING.get(kind) + " declaration" : "Statement " + getName();
	}

	@Override
	public doc getDocAnnotation() {
		if (support == null) return null;
		doc d = super.getDocAnnotation();
		if (d == null) {
			if (support.isAnnotationPresent(action.class)) {
				final doc[] docs = support.getAnnotation(action.class).doc();
				if (docs.length > 0) { d = docs[0]; }
			} else if (support.isAnnotationPresent(symbol.class)) {
				final doc[] docs = support.getAnnotation(symbol.class).doc();
				if (docs.length > 0) { d = docs[0]; }
			}
		}

		return d;
	}

	/**
	 * @return
	 */
	@Override
	public IGamlDocumentation getDocumentation() {
		if (documentation == null) {
			documentation = new GamlRegularDocumentation(super.getDocumentation().toString());
			final List<FacetProto> protos = new ArrayList(getPossibleFacets().values());
			Collections.sort(protos);
			for (final FacetProto f : protos) {
				if (!f.isInternal()) { documentation.set("Possible facets: ", f.getName(), f.getDocumentation()); }
			}
		}
		return documentation;
	}

	/**
	 * @return
	 */
	public boolean isBreakable() { return isBreakable; }

	/**
	 * Checks if is continuable.
	 *
	 * @return true, if is continuable
	 */
	public boolean isContinuable() { return isContinuable; }

	/**
	 * Gets the validator.
	 *
	 * @return the validator
	 */
	public IValidator getValidator() {
		if (validator == null) {
			final validator v = support.getAnnotation(validator.class);
			try {
				validator = v != null ? v.value().getConstructor().newInstance() : NULL_VALIDATOR;
			} catch (Exception e) {}
		}

		return validator;
	}

	/**
	 * Gets the serializer.
	 *
	 * @return the serializer
	 */
	public ISymbolSerializer getSerializer() {
		if (serializer == null) {
			final serializer s = support.getAnnotation(serializer.class);
			try {
				if (s != null) { serializer = s.value().getConstructor().newInstance(); }
			} catch (Exception e) {}
		}

		return serializer;
	}

	/**
	 * Sets the serializer.
	 *
	 * @param serializer
	 *            the new serializer
	 */
	public void setSerializer(final ISymbolSerializer serializer) { this.serializer = serializer; }

	/**
	 * @param symbolDescription
	 * @return
	 */
	public ISymbol create(final IDescription description) {
		return constructor.create(description);
	}

	/**
	 * @param sd
	 * @return
	 */
	public boolean canBeDefinedIn(final IDescription sd) {
		return contextKinds[sd.getKind()] || contextKeywords.contains(sd.getKeyword());
	}

	/**
	 * Should be defined in.
	 *
	 * @param context
	 *            the context
	 * @return true, if successful
	 */
	@Override
	public boolean shouldBeDefinedIn(final String context) {
		return contextKeywords.contains(context);
	}

	/**
	 * Checks if is unique in context.
	 *
	 * @return true, if is unique in context
	 */
	public boolean isUniqueInContext() { return isUniqueInContext; }

	/**
	 * @param facet
	 * @return
	 */
	public IArtefactProto.Facet getFacet(final String facet) {
		return possibleFacets == null ? null : possibleFacets.get(facet);
	}

	/**
	 * Method serialize()
	 *
	 * @see gama.api.utils.IGamlable#serializeToGaml(boolean)
	 */
	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		final StringBuilder sb = new StringBuilder();
		for (final Facet f : possibleFacets.values()) {
			final String s = f.serializeToGaml(includingBuiltIn);
			if (!s.isEmpty()) { sb.append(s).append(" "); }
		}
		return getName() + " " + sb.toString();
	}

	@Override
	public void collectMetaInformation(final GamlProperties meta) {
		meta.put(GamlProperties.STATEMENTS, name);
	}

	/**
	 * Gets the mandatory facets.
	 *
	 * @return the mandatory facets
	 */
	public ImmutableList<String> getMandatoryFacets() { return mandatoryFacets; }

}
