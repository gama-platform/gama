/*******************************************************************************************************
 *
 * IArtefact.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.artefacts;

import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import gama.annotations.usage;
import gama.annotations.support.ISymbolKind;
import gama.api.additions.IGamaGetter;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IGamlDescription;
import gama.api.compilation.descriptions.IVarDescriptionUser;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Signature;

/**
 * Base interface for GAML artefact prototypes (metadata descriptors).
 *
 * <p>
 * This interface defines the contract for prototypes - compile-time metadata objects that describe GAML language
 * artefacts. Prototypes contain structural information, validation rules, and documentation for symbols (statements,
 * species, actions) and operators, enabling the GAMA platform to compile, validate, and document GAML models.
 * </p>
 *
 * <h2>Purpose</h2>
 *
 * <p>
 * Artefact prototypes serve as templates and metadata containers that:
 * </p>
 * <ul>
 * <li><strong>Define Structure:</strong> Specify which facets/parameters are valid for an artefact</li>
 * <li><strong>Enable Validation:</strong> Provide rules for checking correctness during compilation</li>
 * <li><strong>Support Documentation:</strong> Store help text, examples, and usage patterns</li>
 * <li><strong>Guide Compilation:</strong> Direct the creation of descriptions and symbols</li>
 * <li><strong>Facilitate Code Generation:</strong> Provide metadata for serialization and code completion</li>
 * </ul>
 *
 * <h2>Prototype Types</h2>
 *
 * <h3>Symbol Prototypes ({@link Symbol}):</h3>
 * <p>
 * Describe GAML symbols (statements, species, actions, etc.):
 * </p>
 * <ul>
 * <li>Define allowed facets and their types</li>
 * <li>Specify context requirements (where the symbol can appear)</li>
 * <li>Indicate whether the symbol is a sequence, has arguments, etc.</li>
 * </ul>
 *
 * <h3>Operator Prototypes ({@link Operator}):</h3>
 * <p>
 * Describe GAML operators (functions and mathematical/logical operations):
 * </p>
 * <ul>
 * <li>Define parameter signature (number and types of arguments)</li>
 * <li>Specify return type and type providers</li>
 * <li>Indicate whether the operator can be constant-folded</li>
 * </ul>
 *
 * <h3>Facet Prototypes ({@link Facet}):</h3>
 * <p>
 * Describe individual facets (properties/parameters) of symbols:
 * </p>
 * <ul>
 * <li>Define the facet's type and allowed values</li>
 * <li>Specify whether the facet is optional or required</li>
 * <li>Indicate special behaviors (remote, temp, etc.)</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <h3>Symbol Prototype:</h3>
 *
 * <pre>{@code
 * // Prototype for "species" statement
 * IArtefact.Symbol speciesProto = registry.getStatementProto("species");
 *
 * // Check allowed facets
 * Map<String, IArtefact.Facet> facets = speciesProto.getPossibleFacets();
 * if (facets.containsKey("name")) {
 * 	// "name" facet is valid for species
 * }
 *
 * // Check context requirements
 * if (speciesProto.shouldBeDefinedIn("model")) {
 * 	// Species must be defined in model context
 * }
 * }</pre>
 *
 * <h3>Operator Prototype:</h3>
 *
 * <pre>{@code
 * // Prototype for "+" operator
 * IArtefact.Operator plusProto = registry.getOperator("+", signature);
 *
 * // Get return type
 * IType<?> returnType = plusProto.getReturnType();
 *
 * // Check if can be constant
 * if (plusProto.canBeConst()) {
 * 	// Can compute at compile time if args are constant
 * }
 * }</pre>
 *
 * <h2>Lifecycle</h2>
 *
 * <ol>
 * <li><strong>Registration:</strong> Prototypes are created from annotations and registered at platform startup</li>
 * <li><strong>Compilation:</strong> Prototypes guide the creation of descriptions during model compilation</li>
 * <li><strong>Validation:</strong> Prototypes provide rules for validating descriptions</li>
 * <li><strong>Documentation:</strong> Prototypes supply help text for UI components</li>
 * </ol>
 *
 * <h2>Metadata Sources</h2>
 *
 * <p>
 * Prototypes are typically created from:
 * </p>
 * <ul>
 * <li>Java annotations on classes and methods ({@code @symbol}, {@code @operator})</li>
 * <li>Programmatic registration for platform built-ins</li>
 * <li>Plugin contributions for extensions</li>
 * </ul>
 *
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 *
 * @see IArtefactFactory
 * @see gama.api.additions.registries.ArtefactRegistry
 * @see gama.api.compilation.descriptions.IDescription
 */
public interface IArtefact extends IGamlDescription {

	/**
	 * Prototype interface for GAML symbols (statements, species, actions, etc.).
	 *
	 * <p>
	 * Symbol prototypes describe the metadata for language constructs that can appear in GAML code. They define which
	 * facets are allowed, where the symbol can be used, and how it behaves.
	 * </p>
	 *
	 * <h3>Examples of Symbols:</h3>
	 * <ul>
	 * <li><strong>Statements:</strong> if, loop, create, ask, write</li>
	 * <li><strong>Declarations:</strong> species, action, reflex, aspect</li>
	 * <li><strong>Variables:</strong> int, float, string, list</li>
	 * </ul>
	 *
	 * <h3>Usage:</h3>
	 *
	 * <pre>{@code
	 * IArtefact.Symbol proto = registry.getStatementProto("if");
	 *
	 * // Check facets
	 * Map<String, IArtefact.Facet> facets = proto.getPossibleFacets();
	 * IArtefact.Facet conditionFacet = facets.get("condition");
	 *
	 * // Check context
	 * if (proto.shouldBeDefinedIn("action")) {
	 * 	// Symbol can appear in actions
	 * }
	 * }</pre>
	 */
	interface Symbol extends IArtefact {

		/**
		 * @return
		 */
		String getOmissible();

		/**
		 * @param a
		 * @return
		 */
		boolean isLabel(String a);

		/**
		 * @param key
		 * @return
		 */
		boolean isId(String key);

		/**
		 * Gets the possible facets.
		 *
		 * @return the possible facets
		 */
		Map<String, IArtefact.Facet> getPossibleFacets();

		/**
		 * Checks for args.
		 *
		 * @return true, if successful
		 */
		boolean hasArgs();

		/**
		 * Checks if is primitive.
		 *
		 * @return true, if is primitive
		 */
		boolean isPrimitive();

		/**
		 * Checks for sequence.
		 *
		 * @return true, if successful
		 */
		boolean hasSequence();

		/**
		 * Checks if is remote context.
		 *
		 * @return true, if is remote context
		 */
		boolean isRemoteContext();

		/**
		 * Checks if is top level.
		 *
		 * @return true, if is top level
		 */
		boolean isTopLevel();

		/**
		 * @param s
		 * @return
		 */
		boolean shouldBeDefinedIn(String s);

	}

	/**
	 * Prototype interface for GAML facets (symbol properties/parameters).
	 *
	 * <p>
	 * Facet prototypes describe individual properties that can be attached to symbols. Each facet has a name, type, and
	 * constraints governing its use.
	 * </p>
	 *
	 * <h3>Examples of Facets:</h3>
	 * <ul>
	 * <li><strong>name:</strong> Identifier for species, actions, variables</li>
	 * <li><strong>condition:</strong> Boolean expression for if, loop, reflex</li>
	 * <li><strong>init:</strong> Initial value for variables</li>
	 * <li><strong>type:</strong> Type specification for variables and actions</li>
	 * </ul>
	 *
	 * <h3>Facet Properties:</h3>
	 * <ul>
	 * <li><strong>Types:</strong> Accepted expression types (int, string, etc.)</li>
	 * <li><strong>Values:</strong> Restricted set of allowed literal values</li>
	 * <li><strong>Optional:</strong> Whether the facet is required or optional</li>
	 * <li><strong>Remote:</strong> Whether expressions are evaluated in remote context</li>
	 * </ul>
	 *
	 * <h3>Usage:</h3>
	 *
	 * <pre>{@code
	 * IArtefact.Facet facet = symbolProto.getPossibleFacets().get("condition");
	 *
	 * // Check type
	 * IType<?>[] types = facet.getTypes();
	 *
	 * // Check if optional
	 * if (facet.isOptional()) {
	 * 	// Facet can be omitted
	 * }
	 * }</pre>
	 */
	interface Facet extends IArtefact {

		/**
		 * Gets the support class for this facet.
		 *
		 * @return the support class
		 */
		Class<?> getSupport();

		/**
		 * Gets the owner.
		 *
		 * @return the owner
		 */
		String getOwner();

		/**
		 * Gets the values.
		 *
		 * @return the values
		 */
		Set<String> getValues();

		/**
		 * Checks if is new temp.
		 *
		 * @return true, if is new temp
		 */
		boolean isNewTemp();

		/**
		 * Checks if is remote.
		 *
		 * @return true, if is remote
		 */
		boolean isRemote();

		/**
		 * Checks if is optional.
		 *
		 * @return the optional
		 */
		boolean isInternal();

		/**
		 * Checks if is optional.
		 *
		 * @return the optional
		 */
		boolean isOptional();

		/**
		 * Gets the key type.
		 *
		 * @return the key type
		 */
		IType<?> getKeyType();

		/**
		 * Gets the content type.
		 *
		 * @return the content type
		 */
		IType<?> getContentType();

		/**
		 * Gets the types describers.
		 *
		 * @return the types describers
		 */
		int[] getTypesDescribers();

		/**
		 * Gets the types.
		 *
		 * @return the types
		 */
		IType<?>[] getTypes();

		/**
		 * Sets the class.
		 *
		 * @param c
		 *            the new class
		 */
		void setClass(final Class c);

		/**
		 * Checks if is id.
		 *
		 * @return true, if is id
		 */
		boolean isId();

		/**
		 * Sets the owner.
		 *
		 * @param s
		 *            the new owner
		 */
		void setOwner(final String s);

		/**
		 * Checks if this facet is a label facet.
		 *
		 * @return true if this facet is a label
		 */
		boolean isLabel();

		/**
		 * Gets the kind.
		 *
		 * @return the kind
		 */
		@Override
		default ISymbolKind getKind() { return ISymbolKind.FACET; }

	}

	/**
	 * Prototype interface for GAML operators (functions and operations).
	 *
	 * <p>
	 * Operator prototypes describe functions and operations available in GAML expressions. They define parameter
	 * signatures, return types, and execution characteristics.
	 * </p>
	 *
	 * <h3>Examples of Operators:</h3>
	 * <ul>
	 * <li><strong>Arithmetic:</strong> +, -, *, /, ^</li>
	 * <li><strong>Comparison:</strong> =, !=, <, >, <=, >=</li>
	 * <li><strong>Logical:</strong> and, or, not</li>
	 * <li><strong>Functions:</strong> sqrt, cos, length, distance_to</li>
	 * <li><strong>Iterators:</strong> where, collect, count, sum</li>
	 * </ul>
	 *
	 * <h3>Operator Characteristics:</h3>
	 * <ul>
	 * <li><strong>Signature:</strong> Parameter types and count</li>
	 * <li><strong>Return Type:</strong> Result type (may depend on arguments)</li>
	 * <li><strong>Type Providers:</strong> Dynamic type calculation based on context</li>
	 * <li><strong>Lazy Evaluation:</strong> Which arguments are evaluated lazily</li>
	 * <li><strong>Constant Folding:</strong> Whether the operator can be evaluated at compile-time</li>
	 * </ul>
	 *
	 * <h3>Usage:</h3>
	 *
	 * <pre>{@code
	 * IArtefact.Operator plusOp = registry.getOperator("+", signature);
	 *
	 * // Get return type
	 * IType<?> returnType = plusOp.getReturnType();
	 *
	 * // Check if can be constant
	 * if (plusOp.canBeConst()) {
	 * 	// 2 + 3 can be computed at compile time
	 * }
	 *
	 * // Get signature
	 * Signature sig = plusOp.getSignature();
	 * }</pre>
	 */
	interface Operator extends IArtefact, IVarDescriptionUser {

		/**
		 * @return
		 */
		IType getReturnType();

		/**
		 * @return
		 */
		boolean canBeConst();

		/**
		 * @return
		 */
		Signature getSignature();

		/**
		 * @return
		 */
		boolean[] getLazyness();

		/**
		 * @return
		 */
		boolean isIterator();

		/**
		 * @param context
		 * @param gamlType
		 */
		void verifyExpectedTypes(IDescription context, IType<?> gamlType);

		/**
		 * @return
		 */
		int getTypeProvider();

		/**
		 * @return
		 */
		int getContentTypeContentTypeProvider();

		/**
		 * @return
		 */
		int getContentTypeProvider();

		/**
		 * @return
		 */
		int getKeyTypeProvider();

		/**
		 * @return
		 */
		String getCategory();

		/**
		 * @param b
		 * @return
		 */
		String getPattern(boolean b);

		/**
		 * Copy with signature.
		 *
		 * @param gamaType
		 *            the gama type
		 * @return the operator
		 */
		Operator copyWithSignature(final IType gamaType);

		/**
		 * @return
		 */
		AnnotatedElement getJavaBase();

		/**
		 * Gets the deprecated.
		 *
		 * @return the deprecated
		 */

		IGamaGetter getHelper();
	}

	/**
	 * Returns deprecation message if this artefact is deprecated.
	 *
	 * @return deprecation message, or null if not deprecated
	 */
	String getDeprecated();

	/**
	 * Returns the main documentation text for this artefact.
	 *
	 * @return the main documentation string
	 */
	String getMainDoc();

	/**
	 * Returns usage examples for this artefact.
	 *
	 * @return an iterable of usage examples, or empty list if none
	 */
	default Iterable<usage> getUsages() { return Collections.EMPTY_LIST; }

	/**
	 * Returns the kind/category of this artefact.
	 *
	 * @return the kind code from {@code ISymbolKind}
	 */
	ISymbolKind getKind();

}