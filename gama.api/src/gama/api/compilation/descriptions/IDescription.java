/*******************************************************************************************************
 *
 * IDescription.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.descriptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import com.google.common.base.Function;

import gama.annotations.constants.IKeyword;
import gama.annotations.support.ISymbolKind;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.serialization.ISymbolSerializer;
import gama.api.compilation.serialization.ModelSerializer;
import gama.api.compilation.serialization.SpeciesSerializer;
import gama.api.compilation.serialization.StatementSerializer;
import gama.api.compilation.serialization.VarSerializer;
import gama.api.compilation.validation.IDocumentationContext;
import gama.api.compilation.validation.IValidationContext;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITyped;
import gama.api.utils.benchmark.IBenchmarkable;
import gama.api.utils.collections.ICollector;
import gama.api.utils.interfaces.BiConsumerWithPruning;
import gama.api.utils.interfaces.ConsumerWithPruning;
import gama.api.utils.interfaces.IDisposable;

/**
 * Written by drogoul Modified on 31 ao�t 2010.
 *
 * @todo Description
 */

/**
 * The Interface IDescription.
 */

/**
 * The Interface IDescription.
 */

/**
 * The Interface IDescription.
 */
@SuppressWarnings ({ "rawtypes" })
public interface IDescription
		extends IGamlDescription, ITyped, IDisposable, IVarDescriptionProvider, IVarDescriptionUser, IBenchmarkable {

	/**
	 * The Constant SYMBOL_SERIALIZER.
	 */
	ISymbolSerializer SYMBOL_SERIALIZER = new ISymbolSerializer() {};

	/**
	 * The Constant VAR_SERIALIZER.
	 */
	ISymbolSerializer VAR_SERIALIZER = new VarSerializer();

	/**
	 * The Constant SPECIES_SERIALIZER.
	 */
	ISymbolSerializer SPECIES_SERIALIZER = new SpeciesSerializer();

	/**
	 * The Constant MODEL_SERIALIZER.
	 */
	ISymbolSerializer MODEL_SERIALIZER = new ModelSerializer();

	/**
	 * The Constant STATEMENT_SERIALIZER.
	 */
	ISymbolSerializer STATEMENT_SERIALIZER = new StatementSerializer();

	/** Set of facet names that can provide type information for the symbol. */
	Set<String> typeProviderFacets = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(IKeyword.VALUE,
			IKeyword.TYPE, IKeyword.AS, IKeyword.SPECIES, IKeyword.OF, IKeyword.OVER, IKeyword.FROM, IKeyword.INDEX,
			IKeyword.FUNCTION, IKeyword.UPDATE, IKeyword.INIT, IKeyword.DEFAULT)));

	/**
	 * The Enum Flag.
	 */
	enum Flag {

		/** The Abstract. */
		Abstract,

		/** The Validated. */
		Validated,

		/** The Synthetic. */
		Synthetic,

		/** The Starting date defined. */
		StartingDateDefined,

		/** The Is control. */
		IsControl,

		/** The Control finalized. */
		ControlFinalized,

		/** The Can use minimal agents. */
		CanUseMinimalAgents,

		/** The Is super invocation. */
		IsSuperInvocation,

		/** The Breakable. */
		Breakable,

		/** The Continuable. */
		Continuable,

		/** The Built in. */
		BuiltIn,

		/** The Globat. */
		Global,

		/** The Unmodifiable. */
		Unmodifiable,

		/** The Updatable. */
		Updatable,

		/** The Is parameter. */
		IsParameter,

		/** The is mirror. */
		isMirror,

		/** The is grid. */
		isGrid,

		/** The is contextual type. */
		isContextualType,

		/** The is function. */
		isFunction,

		/** The is memorize. */
		isMemorize,

		/** The is batch. */
		isBatch,
		/** The Is create. */
		IsCreate,

		/**
		 * The No type inference. A flag that signifies that type inference should not be used when computing the type
		 * of the description. Type inference is useful when the type of the description is not known at the time of
		 * parsing, but it should not be used when the type is known. See #385
		 */
		NoTypeInference

	}

	/**
	 * The Constant TO_NAME.
	 */
	Function<? super IDescription, ? extends String> TO_NAME = IDescription::getName;

	/**
	 * The Interface DescriptionVisitor.
	 *
	 * @param <T>
	 *            the generic type
	 */
	@FunctionalInterface
	public interface DescriptionVisitor<T extends IDescription> extends ConsumerWithPruning<T> {}

	/**
	 * The Interface IFacetVisitor.
	 */
	@FunctionalInterface
	public interface IFacetVisitor extends BiConsumerWithPruning<String, IExpressionDescription> {}

	/**
	 * The Constant VALIDATING_VISITOR.
	 */
	DescriptionVisitor<IDescription> VALIDATING_VISITOR = desc -> (desc.validate() != null);

	/**
	 * The Constant DISPOSING_VISITOR.
	 */
	DescriptionVisitor<IDescription> DISPOSING_VISITOR = desc -> {
		desc.dispose();
		return true;

	};

	/**
	 * Error.
	 *
	 * @param message
	 *            the message
	 */
	void error(final String message);

	/**
	 * Error.
	 *
	 * @param message
	 *            the message
	 * @param code
	 *            the code
	 */
	void error(final String message, String code);

	/**
	 * Error.
	 *
	 * @param message
	 *            the message
	 * @param code
	 *            the code
	 * @param element
	 *            the element
	 * @param data
	 *            the data
	 */
	void error(final String message, String code, String element, String... data);

	/**
	 * Error.
	 *
	 * @param message
	 *            the message
	 * @param code
	 *            the code
	 * @param element
	 *            the element
	 * @param data
	 *            the data
	 */
	void error(final String message, String code, EObject element, String... data);

	/**
	 * Warning.
	 *
	 * @param message
	 *            the message
	 * @param code
	 *            the code
	 */
	void warning(final String message, String code);

	/**
	 * Warning.
	 *
	 * @param message
	 *            the message
	 * @param code
	 *            the code
	 * @param element
	 *            the element
	 * @param data
	 *            the data
	 */
	void warning(final String message, String code, String element, String... data);

	/**
	 * Warning.
	 *
	 * @param message
	 *            the message
	 * @param code
	 *            the code
	 * @param element
	 *            the element
	 * @param data
	 *            the data
	 */
	void warning(final String message, String code, EObject element, String... data);

	/**
	 * Gets the keyword.
	 *
	 * @return the keyword
	 */
	String getKeyword();

	/**
	 * Gets the model description.
	 *
	 * @return the model description
	 */
	IModelDescription getModelDescription();

	/**
	 * Gets the species context.
	 *
	 * @return the species context
	 */
	ISpeciesDescription getSpeciesContext();

	/**
	 * Sets the enclosing description.
	 *
	 * @param desc
	 *            the new enclosing description
	 */
	void setEnclosingDescription(final IDescription desc);

	/**
	 * Gets the underlying element.
	 *
	 * @param facet
	 *            the facet
	 * @return the underlying element
	 */
	EObject getUnderlyingElement(Object facet, boolean returnFacet);

	/**
	 * Gets the underlying element.
	 *
	 * @return the underlying element
	 */
	default EObject getUnderlyingElement() { return getUnderlyingElement(null, false); }

	/**
	 * Gets the meta.
	 *
	 * @return the meta
	 */
	IArtefact.Symbol getArtefact();

	/**
	 * Gets the enclosing description.
	 *
	 * @return the enclosing description
	 */
	IDescription getEnclosingDescription();

	/**
	 * Gets the description declaring var.
	 *
	 * @param name
	 *            the name
	 * @return the description declaring var
	 */
	IVarDescriptionProvider getDescriptionDeclaringVar(final String name);

	/**
	 * Gets the description declaring action.
	 *
	 * @param name
	 *            the name
	 * @param superInvocation
	 *            the super invocation
	 * @return the description declaring action
	 */
	ITypeDescription getDescriptionDeclaringAction(final String name, boolean superInvocation);

	/**
	 * Gets the children with keyword.
	 *
	 * @param keyword
	 *            the keyword
	 * @return the children with keyword
	 */
	Iterable<IDescription> getChildrenWithKeyword(String keyword);

	/**
	 * Gets the own children.
	 *
	 * @return the own children
	 */
	Iterable<IDescription> getOwnChildren();

	/**
	 * Gets the child with keyword.
	 *
	 * @param keyword
	 *            the keyword
	 * @return the child with keyword
	 */
	IDescription getChildWithKeyword(String keyword);

	/**
	 * Gets the type named.
	 *
	 * @param s
	 *            the s
	 * @return the type named
	 */
	IType getTypeNamed(String s);

	/**
	 * Gets the species description.
	 *
	 * @param actualSpecies
	 *            the actual species
	 * @return the species description
	 */
	ISpeciesDescription getSpeciesDescription(String actualSpecies);

	/**
	 * Gets the action.
	 *
	 * @param name
	 *            the name
	 * @return the action
	 */
	IActionDescription getAction(String name);

	/**
	 * Gets the validation context.
	 *
	 * @return the validation context
	 */
	IValidationContext getValidationContext();

	/**
	 * Gets the documentation context.
	 *
	 * @return the documentation context
	 */
	IDocumentationContext getDocumentationContext();

	/**
	 * Copy.
	 *
	 * @param into
	 *            the into
	 * @return the i description
	 */
	IDescription copy(IDescription into);

	/**
	 * Validate.
	 *
	 * @return the i description
	 */
	IDescription validate();

	/**
	 * Compile.
	 *
	 * @return the i symbol
	 */
	ISymbol compile();

	/**
	 * Gets the kind.
	 *
	 * @return the kind
	 */
	ISymbolKind getKind();

	/**
	 * Checks if is built in.
	 *
	 * @return true, if is built in
	 */
	boolean isBuiltIn();

	/**
	 * Gets the origin name.
	 *
	 * @return the origin name
	 */
	String getOriginName();

	/**
	 * Sets the origin name.
	 *
	 * @param name
	 *            the new origin name
	 */
	void setOriginName(String name);

	/**
	 * Sets the defining plugin.
	 *
	 * @param plugin
	 *            the new defining plugin
	 */
	void setDefiningPlugin(String plugin);

	/**
	 * Info.
	 *
	 * @param s
	 *            the s
	 * @param code
	 *            the code
	 * @param facet
	 *            the facet
	 * @param data
	 *            the data
	 */
	void info(final String s, final String code, final String facet, final String... data);

	/**
	 * Info.
	 *
	 * @param s
	 *            the s
	 * @param code
	 *            the code
	 * @param facet
	 *            the facet
	 * @param data
	 *            the data
	 */
	void info(final String s, final String code, final EObject facet, final String... data);

	/**
	 * Info.
	 *
	 * @param message
	 *            the message
	 * @param code
	 *            the code
	 */
	void info(final String message, final String code);

	/**
	 * Reset origin name.
	 */
	void resetOriginName();

	/**
	 * Manipulates var.
	 *
	 * @param name
	 *            the name
	 * @return true, if successful
	 */
	boolean manipulatesVar(final String name);

	/**
	 * Gets the litteral value of the facet or null if not defined
	 *
	 * @param name
	 *            the name
	 * @return the litteral
	 */
	String getLitteral(String name);

	/**
	 * Gets the facet expr.
	 *
	 * @param strings
	 *            the strings
	 * @return the facet expr
	 */
	IExpression getFacetExpr(final String... strings);

	/**
	 * Checks for facet.
	 *
	 * @param until
	 *            the until
	 * @return true, if successful
	 */
	boolean hasFacet(String until);

	/**
	 * Gets the facet.
	 *
	 * @param string
	 *            the string
	 * @return the facet
	 */
	IExpressionDescription getFacet(String string);

	/**
	 * Gets the facet.
	 *
	 * @param strings
	 *            the strings
	 * @return the facet
	 */
	IExpressionDescription getFacet(String... strings);

	/**
	 * Returns the first facet key found in this symbol or null
	 *
	 * @param strings
	 * @return a facet key or null
	 */
	String firstFacetFoundAmong(final String... strings);

	/**
	 * Sets the facet.
	 *
	 * @param string
	 *            the string
	 * @param exp
	 *            the exp
	 */
	void setFacetExprDescription(String string, IExpressionDescription exp);

	/**
	 * Sets the facet.
	 *
	 * @param item
	 *            the item
	 * @param exp
	 *            the exp
	 */
	void setFacet(String item, IExpression exp);

	/**
	 * Removes the facets.
	 *
	 * @param strings
	 *            the strings
	 */
	void removeFacets(String... strings);

	/**
	 * Returns whether or not the visit has been completed.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	default boolean visitFacets(final IFacetVisitor visitor) {
		return visitFacets(null, visitor);
	}

	/**
	 * Gets the name for benchmarks.
	 *
	 * @return the name for benchmarks
	 */
	/*
	 * (non-Javadoc)
	 *
	 * @see gama.core.common.interfaces.IBenchmarkable#getNameForBenchmarks()
	 */
	@Override
	default String getNameForBenchmarks() {
		final StringBuilder sb = new StringBuilder();
		getSerializer().serializeNoRecursion(sb, this, false);
		return sb.toString();
	}

	/**
	 * Collect used vars of.
	 *
	 * @param species
	 *            the species
	 * @param result
	 *            the result
	 */
	@Override
	default void collectUsedVarsOf(final ISpeciesDescription species,
			final ICollector<IVarDescriptionUser> alreadyProcessed, final ICollector<IVariableDescription> result) {
		if (alreadyProcessed.contains(this)) return;
		alreadyProcessed.add(this);
		this.visitFacets((name, exp) -> {
			final IExpression expression = exp.getExpression();
			if (expression != null) { expression.collectUsedVarsOf(species, alreadyProcessed, result); }
			return true;
		});
		this.visitOwnChildren(desc -> {
			desc.collectUsedVarsOf(species, alreadyProcessed, result);
			return true;
		});
	}

	/**
	 * Visit facets.
	 *
	 * @param facets
	 *            the facets
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	boolean visitFacets(Set<String> facets, IFacetVisitor visitor);

	/**
	 * Visit children.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	boolean visitChildren(DescriptionVisitor<IDescription> visitor);

	/**
	 * Visit own children recursively.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	boolean visitOwnChildrenRecursively(DescriptionVisitor<IDescription> visitor);

	/**
	 * Visit own children.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	boolean visitOwnChildren(DescriptionVisitor<IDescription> visitor);

	/**
	 * Document.
	 *
	 * @param s
	 *            the s
	 * @param desc
	 *            the desc
	 */
	// void document(EObject s, IGamlDescription desc);

	/**
	 * Gets the facets.
	 *
	 * @return the facets
	 */
	Facets getFacets();

	/**
	 * Attach alternate var description provider.
	 *
	 * @param vp
	 *            the vp
	 */
	void attachAlternateVarDescriptionProvider(final IVarDescriptionProvider vp);

	/**
	 * Replace children with.
	 *
	 * @param array
	 *            the array
	 */
	void replaceChildrenWith(Iterable<IDescription> array);

	/**
	 * Gets the serializer.
	 *
	 * @return the serializer
	 */
	ISymbolSerializer getSerializer();

	/**
	 * Checks if this description is in (directly or indirectly) a description with the keyword passed in argument.
	 *
	 * @param ancestor
	 *            the ancestor
	 * @return true, if is in
	 */
	default boolean isIn(final String ancestor) {
		IDescription d = this.getEnclosingDescription();
		if (d == null || d == this) return false;
		if (d.getKeyword().equals(ancestor)) return true;
		return d.isIn(ancestor);
	}

	/**
	 * Gets the parent with keyword.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param ancestor
	 *            the ancestor
	 * @return the parent with keyword
	 * @date 12 déc. 2023
	 */
	default IDescription getParentWithKeyword(final String ancestor) {
		IDescription d = this.getEnclosingDescription();
		if (d == null || d == this) return null;
		if (d.getKeyword().equals(ancestor)) return d;
		return d.getParentWithKeyword(ancestor);
	}

	/**
	 * Checks if is id.
	 *
	 * @return true, if is id
	 */
	default boolean isID() {
		return IKeyword.ARG.equals(this.getKeyword()) && "true".equals(getLitteral(IKeyword.ID));
	}

	/**
	 * Checks if is invocation. Either do or invoke
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is invocation
	 * @date 10 janv. 2024
	 */
	boolean isInvocation();

	/**
	 * @return
	 */
	boolean isAbstract();

}