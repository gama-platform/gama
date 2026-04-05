/*******************************************************************************************************
 *
 * IValue.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.misc;


import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITyped;
import gama.api.runtime.scope.IScope;
import gama.api.utils.interfaces.IGamlable;
import gama.api.utils.json.IJsonable;

/**
 * The root interface for all values that can be manipulated in GAML.
 * 
 * <p>
 * IValue is the fundamental abstraction in GAMA's type system. Every object that can be used in GAML expressions,
 * stored in variables, passed as parameters, or returned from operations must implement this interface. It combines
 * several capabilities: having a type, being serializable to GAML syntax and JSON, and being copyable.
 * </p>
 * 
 * <h2>Core Capabilities</h2>
 * <p>
 * IValue brings together three essential interfaces:
 * </p>
 * <ul>
 * <li><strong>{@link IGamlable}:</strong> Can be serialized to valid GAML code via {@link #toGaml(boolean)}</li>
 * <li><strong>{@link ITyped}:</strong> Has a GAMA type accessible via {@link #getGamlType()}</li>
 * <li><strong>{@link IJsonable}:</strong> Can be serialized to JSON via
 * {@link #serializeToJson(gama.api.utils.json.IJson)}</li>
 * </ul>
 * 
 * <h2>Required Operations</h2>
 * <ul>
 * <li>{@link #stringValue(IScope)} - Provides a human-readable string representation</li>
 * <li>{@link #copy(IScope)} - Creates a copy of the value (shallow or deep depending on type)</li>
 * <li>{@link #intValue(IScope)} - Provides an integer representation (default: 0)</li>
 * <li>{@link #floatValue(IScope)} - Provides a floating-point representation (default: intValue)</li>
 * <li>{@link #computeRuntimeType(IScope)} - Computes the actual runtime type (may differ from compile-time type)</li>
 * </ul>
 * 
 * <h2>Type System Integration</h2>
 * <p>
 * Every IValue has both a compile-time type (returned by {@link #getGamlType()}) and potentially a more specific
 * runtime type (computed by {@link #computeRuntimeType(IScope)}). For example, a list declared as {@code list} has the
 * generic list type at compile-time, but at runtime might be a {@code list<int>}.
 * </p>
 * 
 * <h2>Implementations</h2>
 * <p>
 * IValue is implemented by all GAMA types including:
 * </p>
 * <ul>
 * <li>Primitive types (int, float, bool, string)</li>
 * <li>Collections ({@link gama.api.types.list.IList}, {@link gama.api.types.map.IMap},
 * {@link gama.api.types.matrix.IMatrix})</li>
 * <li>Spatial types ({@link gama.api.types.geometry.IShape}, {@link gama.api.types.geometry.IPoint})</li>
 * <li>Agent types ({@link gama.api.kernel.agent.IAgent})</li>
 * <li>Special types (graphs, paths, files, pairs, colors, etc.)</li>
 * </ul>
 * 
 * <h2>Copy Semantics</h2>
 * <p>
 * The {@link #copy(IScope)} method's behavior depends on the type:
 * </p>
 * <ul>
 * <li><strong>Immutable types</strong> (primitives): may return this</li>
 * <li><strong>Collections:</strong> typically create shallow copies (copying the container but not elements)</li>
 * <li><strong>Agents and geometries:</strong> may create deep copies depending on context</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <pre>
 * // Getting type information
 * IValue value = ...;
 * IType&lt;?&gt; type = value.getGamlType();
 * IType&lt;?&gt; runtimeType = value.computeRuntimeType(scope);
 * 
 * // String representation
 * String str = value.stringValue(scope); // Human-readable
 * String gaml = value.toGaml(false);     // Valid GAML code
 * 
 * // Numeric conversions
 * int asInt = value.intValue(scope);
 * double asFloat = value.floatValue(scope);
 * 
 * // Copying
 * IValue copy = value.copy(scope);
 * 
 * // JSON serialization
 * IJsonValue json = value.serializeToJson(jsonContext);
 * </pre>
 * 
 * @author drogoul
 * @since GAMA 1.0
 */
public interface IValue extends IGamlable, ITyped, IJsonable {

	/**
	 * Returns the string representation of this value.
	 * 
	 * <p>
	 * This method provides a human-readable string representation of the value, which is not necessarily valid GAML
	 * code. For GAML serialization, use {@link #toGaml(boolean)} instead.
	 * </p>
	 * 
	 * <p>
	 * Examples:
	 * </p>
	 * <ul>
	 * <li>An integer 42 returns "42"</li>
	 * <li>A list [1, 2, 3] might return "[1,2,3]"</li>
	 * <li>An agent might return "agent0" or its name</li>
	 * </ul>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @return a string representing this value
	 */
	String stringValue(IScope scope);

	/**
	 * Returns the integer representation of this value.
	 * 
	 * <p>
	 * The conversion semantics depend on the value type:
	 * </p>
	 * <ul>
	 * <li><strong>Numbers:</strong> truncated to integer</li>
	 * <li><strong>Booleans:</strong> 1 for true, 0 for false</li>
	 * <li><strong>Containers:</strong> their length/size</li>
	 * <li><strong>Other types:</strong> implementation-specific (default: 0)</li>
	 * </ul>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @return the integer representation of this value
	 */
	default int intValue(final IScope scope) {
		return 0;
	}

	/**
	 * Returns the floating-point representation of this value.
	 * 
	 * <p>
	 * The conversion semantics depend on the value type:
	 * </p>
	 * <ul>
	 * <li><strong>Numbers:</strong> converted to double</li>
	 * <li><strong>Booleans:</strong> 1.0 for true, 0.0 for false</li>
	 * <li><strong>Containers:</strong> their length/size as double</li>
	 * <li><strong>Other types:</strong> intValue() cast to double (default)</li>
	 * </ul>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @return the floating-point representation of this value
	 */
	default double floatValue(final IScope scope) {
		return intValue(scope);
	}

	/**
	 * Returns a copy of this value.
	 * 
	 * <p>
	 * The definition of "copy" (shallow, deep, or identity) depends on the type:
	 * </p>
	 * <ul>
	 * <li><strong>Immutable values</strong> (primitives, strings): may return this</li>
	 * <li><strong>Mutable collections:</strong> create a new container with the same elements (shallow copy)</li>
	 * <li><strong>Agents:</strong> typically cannot be copied (may throw exception or return this)</li>
	 * <li><strong>Geometries:</strong> create a new geometry with the same shape</li>
	 * </ul>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @return a copy of this value
	 */
	IValue copy(IScope scope);

	/**
	 * Computes the actual runtime type of this value.
	 * 
	 * <p>
	 * The runtime type may be more specific than the compile-time type returned by {@link #getGamlType()}. For example:
	 * </p>
	 * <ul>
	 * <li>A list&lt;?&gt; might actually be a list&lt;int&gt; at runtime</li>
	 * <li>A map&lt;?,?&gt; might be a map&lt;string,agent&gt; at runtime</li>
	 * <li>A container might discover its content type by inspecting elements</li>
	 * </ul>
	 * 
	 * <p>
	 * The default implementation simply returns the compile-time type.
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @return the runtime type of this value (may be the same as getGamlType())
	 */
	default IType<?> computeRuntimeType(final IScope scope) {
		return getGamlType();
	}

}
