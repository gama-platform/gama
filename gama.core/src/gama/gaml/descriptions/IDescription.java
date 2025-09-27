/*******************************************************************************************************
 *
 * IDescription.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.descriptions;

import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import com.google.common.base.Function;

import gama.core.common.interfaces.IBenchmarkable;
import gama.core.common.interfaces.IDisposable;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.interfaces.ISkill;
import gama.core.common.interfaces.ITyped;
import gama.core.util.BiConsumerWithPruning;
import gama.core.util.ConsumerWithPruning;
import gama.core.util.ICollector;
import gama.gaml.compilation.ISymbol;
import gama.gaml.descriptions.SymbolSerializer.ModelSerializer;
import gama.gaml.descriptions.SymbolSerializer.SpeciesSerializer;
import gama.gaml.descriptions.SymbolSerializer.StatementSerializer;
import gama.gaml.descriptions.SymbolSerializer.VarSerializer;
import gama.gaml.expressions.IExpression;
import gama.gaml.interfaces.IGamlDescription;
import gama.gaml.statements.Facets;
import gama.gaml.types.IType;

/**
 * Written by drogoul Modified on 31 ao�t 2010.
 *
 * @todo Description
 */
@SuppressWarnings ({ "rawtypes" })
public interface IDescription extends IGamlDescription, IKeyword, ITyped, IDisposable, IVarDescriptionProvider,
		IVarDescriptionUser, IBenchmarkable {

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
	 * The Constant SYMBOL_SERIALIZER.
	 */
	SymbolSerializer<SymbolDescription> SYMBOL_SERIALIZER = new SymbolSerializer<>();

	/**
	 * The Constant VAR_SERIALIZER.
	 */
	VarSerializer VAR_SERIALIZER = new VarSerializer();

	/**
	 * The Constant SPECIES_SERIALIZER.
	 */
	SpeciesSerializer SPECIES_SERIALIZER = new SpeciesSerializer();

	/**
	 * The Constant MODEL_SERIALIZER.
	 */
	ModelSerializer MODEL_SERIALIZER = new ModelSerializer();

	/**
	 * The Constant STATEMENT_SERIALIZER.
	 */
	StatementSerializer STATEMENT_SERIALIZER = new StatementSerializer();

	/**
	 * The Constant TO_NAME.
	 */
	Function<? super IDescription, ? extends String> TO_NAME = IDescription::getName;

	/**
	 * The Constant TO_CLASS.
	 */
	Function<TypeDescription, Class<? extends ISkill>> TO_CLASS = TypeDescription::getJavaBase;

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
	ModelDescription getModelDescription();

	/**
	 * Gets the species context.
	 *
	 * @return the species context
	 */
	SpeciesDescription getSpeciesContext();

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
	SymbolProto getMeta();

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
	IDescription getDescriptionDeclaringAction(final String name, boolean superInvocation);

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
	SpeciesDescription getSpeciesDescription(String actualSpecies);

	/**
	 * Gets the action.
	 *
	 * @param name
	 *            the name
	 * @return the action
	 */
	ActionDescription getAction(String name);

	/**
	 * Gets the validation context.
	 *
	 * @return the validation context
	 */
	ValidationContext getValidationContext();

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
	int getKind();

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
	default void collectUsedVarsOf(final SpeciesDescription species,
			final ICollector<IVarDescriptionUser> alreadyProcessed, final ICollector<VariableDescription> result) {
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
	void document(EObject s, IGamlDescription desc);

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
	SymbolSerializer getSerializer();

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
	default boolean isID() { return ARG.equals(this.getKeyword()) && "true".equals(getLitteral(ID)); }

	/**
	 * Checks if is invocation. Either do or invoke
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is invocation
	 * @date 10 janv. 2024
	 */
	boolean isInvocation();

	/**
	 * @param classDescription
	 * @return
	 */
	boolean canBeDefinedIn(IDescription desc);

}