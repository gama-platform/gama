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

import gama.annotations.support.ISymbolKind;
import gama.api.additions.IGamaGetter;
import gama.api.compilation.factories.ISymbolFactory;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Signature;

/**
 * Factory interface for creating artefact prototypes from annotations and metadata.
 *
 * <p>
 * This interface defines the contract for factories that create {@link IArtefactProto} instances during platform
 * initialization. Prototypes are constructed from Java annotations on classes and methods that define GAML symbols and
 * operators.
 * </p>
 *
 * <h2>Purpose</h2>
 *
 * <p>
 * The prototype factory enables:
 * </p>
 * <ul>
 * <li><strong>Annotation Processing:</strong> Converting {@code @symbol} and {@code @operator} annotations to
 * prototypes</li>
 * <li><strong>Metadata Extraction:</strong> Collecting structural and behavioral information from Java code</li>
 * <li><strong>Registration:</strong> Creating prototypes that are registered with the platform registry</li>
 * <li><strong>Extensibility:</strong> Allowing plugins to contribute new GAML language elements</li>
 * </ul>
 *
 * <h2>Creation Flow</h2>
 *
 * <ol>
 * <li><strong>Annotation Scanning:</strong> Platform scans for {@code @symbol} and {@code @operator} annotations</li>
 * <li><strong>Metadata Extraction:</strong> Annotation processor extracts metadata (facets, signatures, etc.)</li>
 * <li><strong>Prototype Creation:</strong> Factory creates appropriate prototype type</li>
 * <li><strong>Registration:</strong> Prototype is registered in {@code ArtefactProtoRegistry}</li>
 * </ol>
 *
 * <h2>Usage Example</h2>
 *
 * <h3>Creating Symbol Prototype:</h3>
 *
 * <pre>{@code
 * // From @symbol annotation
 * &#64;symbol(name = "my_statement", kind = ISymbolKind.SINGLE_STATEMENT, ...)
 * public class MyStatement extends AbstractStatement { }
 *
 * // Factory creates prototype
 * IArtefactProto.Symbol proto = factory.createSymbolProto(
 *     MyStatement.class,
 *     false,  // not breakable
 *     false,  // not continuable
 *     false,  // not a sequence
 *     false,  // no arguments
 *     ISymbolKind.SINGLE_STATEMENT,
 *     false,  // not primitive
 *     facetProtos,
 *     null,   // no omissible facet
 *     new String[] {"action", "reflex"},  // can appear in actions/reflexes
 *     new int[] {ISymbolKind.BEHAVIOR},
 *     false,  // not remote context
 *     false,  // not unique
 *     false,  // name not unique
 *     symbolFactory,
 *     "my_statement",
 *     "my.plugin"
 * );
 * }</pre>
 *
 * <h3>Creating Operator Prototype:</h3>
 *
 * <pre>{@code
 * // From @operator annotation
 * &#64;operator(value = "my_op", ...)
 * public static Object myOperation(IScope scope, Object arg1, Object arg2) { }
 *
 * // Factory creates prototype
 * IArtefactProto.Operator proto = factory.createOperatorProto(
 *     "my_op",
 *     method,
 *     "Performs my operation",
 *     helper,
 *     false,  // can't be constant
 *     false,  // not an iterator
 *     Types.NO_TYPE,
 *     signature,
 *     ITypeProvider.NONE,
 *     ITypeProvider.NONE,
 *     ITypeProvider.NONE,
 *     ITypeProvider.NONE,
 *     new int[0],
 *     "my.plugin"
 * );
 * }</pre>
 *
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 *
 * @see IArtefactProto
 * @see gama.api.additions.registries.ArtefactProtoRegistry
 * @see gama.annotations.symbol
 * @see gama.annotations.operator
 */
public interface IArtefactProtoFactory {

	/**
	 * Creates a symbol prototype from metadata extracted from annotations.
	 *
	 * <p>
	 * This method constructs a complete symbol prototype containing all structural and behavioral information needed
	 * for compilation and validation.
	 * </p>
	 *
	 * @param c
	 *            the Java class implementing the symbol
	 * @param isBreakable
	 *            whether the symbol can contain break statements
	 * @param isContinuable
	 *            whether the symbol can contain continue statements
	 * @param isSequence
	 *            whether the symbol is a sequence (contains sub-statements)
	 * @param hasArguments
	 *            whether the symbol has formal arguments
	 * @param sKind
	 *            the symbol kind (from {@code ISymbolKind})
	 * @param isPrimitive
	 *            whether this is a primitive (built-in) symbol
	 * @param fmd
	 *            array of facet prototypes describing allowed facets
	 * @param omissible
	 *            the name of the facet that can be omitted, or null
	 * @param contextKeywords
	 *            keywords of contexts where this symbol can appear
	 * @param contextKinds
	 *            kinds of contexts where this symbol can appear
	 * @param isRemoteContext
	 *            whether expressions in this symbol are evaluated remotely
	 * @param isUnique
	 *            whether only one instance can exist in the model
	 * @param name_unique
	 *            whether the name must be unique
	 * @param sc
	 *            the symbol factory for creating instances
	 * @param name
	 *            the symbol name/keyword
	 * @param plugin
	 *            the plugin defining this symbol
	 * @return a new symbol prototype
	 */
	IArtefactProto.Symbol createSymbolProto(Class c, boolean isBreakable, boolean isContinuable, boolean isSequence,
			boolean hasArguments, ISymbolKind sKind, boolean isPrimitive, IArtefactProto.Facet[] fmd, String omissible,
			String[] contextKeywords, int[] contextKinds, boolean isRemoteContext, boolean isUnique,
			boolean name_unique, ISymbolFactory sc, String name, String plugin);

	/**
	 * Creates an operator prototype with class-based signature.
	 *
	 * <p>
	 * This overload is used when the operator signature is specified as a Class object rather than a full
	 * {@code Signature} instance.
	 * </p>
	 *
	 * @param name
	 *            the operator name
	 * @param object
	 *            the annotated element (method or class)
	 * @param helper
	 *            the helper for executing the operator
	 * @param canBeConst
	 *            whether the operator can be constant-folded
	 * @param isIterator
	 *            whether the operator is an iterator
	 * @param returnType
	 *            the return type code
	 * @param signature
	 *            the signature as a Class
	 * @param typeProvider
	 *            type provider code for return type
	 * @param contentTypeProvider
	 *            type provider for content type
	 * @param keyTypeProvider
	 *            type provider for key type
	 * @param expectedContentType
	 *            expected content types array
	 * @return a new operator prototype
	 */
	IArtefactProto.Operator createOperatorProto(String name, AnnotatedElement object, IGamaGetter helper,
			boolean canBeConst, boolean isIterator, int returnType, Class signature, int typeProvider,
			int contentTypeProvider, int keyTypeProvider, int[] expectedContentType);

	/**
	 * Creates an operator prototype with full signature and documentation.
	 *
	 * <p>
	 * This is the complete factory method providing all operator metadata including documentation and a full type
	 * signature.
	 * </p>
	 *
	 * @param name
	 *            the operator name
	 * @param method
	 *            the Java method implementing the operator
	 * @param constantDoc
	 *            constant documentation string
	 * @param helper
	 *            the helper for executing the operator
	 * @param canBeConst
	 *            whether the operator can be constant-folded
	 * @param isIterator
	 *            whether the operator is an iterator
	 * @param rt
	 *            the return type
	 * @param signature
	 *            the full signature specifying parameter types
	 * @param typeProvider
	 *            type provider code for return type
	 * @param contentTypeProvider
	 *            type provider for content type
	 * @param keyTypeProvider
	 *            type provider for key type
	 * @param contentContentTypeProvider
	 *            type provider for content's content type
	 * @param expectedContentTypes
	 *            expected content types array
	 * @param plugin
	 *            the plugin defining this operator
	 * @return a new operator prototype
	 */
	IArtefactProto.Operator createOperatorProto(String name, Executable method, String constantDoc, IGamaGetter helper,
			boolean canBeConst, boolean isIterator, IType rt, Signature signature, int typeProvider,
			int contentTypeProvider, int keyTypeProvider, int contentContentTypeProvider, int[] expectedContentTypes,
			String plugin);

	/**
	 * Creates a facet prototype describing a symbol property.
	 *
	 * <p>
	 * Facet prototypes define the allowed properties that can be attached to symbols, including their types, allowed
	 * values, and behavioral flags.
	 * </p>
	 *
	 * @param name
	 *            the facet name
	 * @param types
	 *            array of type codes for allowed expression types
	 * @param contentType
	 *            content type code (for container types)
	 * @param keyType
	 *            key type code (for map types)
	 * @param values
	 *            restricted set of allowed literal values, or null
	 * @param optional
	 *            whether the facet is optional
	 * @param internal
	 *            whether the facet is internal (not user-facing)
	 * @param isRemote
	 *            whether expressions are evaluated in remote context
	 * @return a new facet prototype
	 */
	IArtefactProto.Facet createFacetProto(String name, int[] types, int contentType, int keyType, String[] values,
			boolean optional, boolean internal, boolean isRemote);

}