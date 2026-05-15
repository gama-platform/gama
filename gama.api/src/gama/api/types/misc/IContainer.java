/*******************************************************************************************************
 *
 * IContainer.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.misc;

import java.util.Collection;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.usage;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.annotations.support.ITypeProvider;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IContainerType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.GamaExecutorService;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.list.IList;
import gama.api.types.map.IMap;
import gama.api.types.matrix.IMatrix;
import one.util.streamex.StreamEx;

/**
 * The fundamental interface for indexed and sequential container structures in GAMA.
 * 
 * <p>
 * IContainer provides the GAML {@code container} branch abstraction over indexed and sequential structures such as
 * lists, matrices, graphs, populations (species), files, and pairs. Maps now remain outside this inheritance branch in
 * the GAML type system, while still sharing the extracted Java-level runtime contract defined by
 * {@link IRuntimeContainer}.
 * </p>
 * 
 * <h2>Type Parameters</h2>
 * <ul>
 * <li><strong>KeyType:</strong> The type used to address/index elements (int for lists, point for matrices, any type
 * for maps)</li>
 * <li><strong>ValueType:</strong> The type of elements stored in the container</li>
 * </ul>
 * 
 * <h2>Core Capabilities</h2>
 * <p>
 * All containers support:
 * </p>
 * <ul>
 * <li><strong>Containment checking:</strong> contains(), containsKey()</li>
 * <li><strong>Size queries:</strong> length(), isEmpty()</li>
 * <li><strong>Element access:</strong> firstValue(), lastValue(), anyValue()</li>
 * <li><strong>Iteration:</strong> iterable(), stream(), parallelStream()</li>
 * <li><strong>Conversion:</strong> listValue(), mapValue(), matrixValue()</li>
 * <li><strong>Manipulation:</strong> reverse(), copy()</li>
 * </ul>
 * 
 * <h2>Nested Interfaces</h2>
 * <p>
 * IContainer defines specialized capabilities through nested interfaces:
 * </p>
 * <ul>
 * <li><strong>{@link ToGet}:</strong> Support for element retrieval by key/index</li>
 * <li><strong>{@link ToSet}:</strong> Support for element addition, modification, and removal</li>
 * <li><strong>{@link Addressable}:</strong> Combines IContainer with ToGet (readable containers)</li>
 * <li><strong>{@link Modifiable}:</strong> Combines IContainer with ToSet (writable containers)</li>
 * </ul>
 * 
 * <h2>Container Types</h2>
 * <p>
 * Different GAMA types implement IContainer with different key/value semantics:
 * </p>
 * <ul>
 * <li><strong>Lists:</strong> KeyType=Integer, ordered sequences indexed from 0</li>
 * <li><strong>Maps:</strong> now handled through {@link IRuntimeContainer} and {@code map}, not through the GAML
 * {@code container} inheritance branch</li>
 * <li><strong>Matrices:</strong> KeyType=IPoint, 2D grids indexed by {column, row}</li>
 * <li><strong>Graphs:</strong> KeyType=node, ValueType=edge (or vice versa)</li>
 * <li><strong>Populations:</strong> KeyType=Integer/String, ValueType=IAgent</li>
 * <li><strong>Files:</strong> Delegate to their content container</li>
 * <li><strong>Pairs:</strong> KeyType=key type, ValueType=value type (single entry)</li>
 * </ul>
 * 
 * <h2>Conversion Operations</h2>
 * <p>
 * Every container can be converted to other container types:
 * </p>
 * <ul>
 * <li><strong>To list:</strong> listValue() - preserves order where applicable</li>
 * <li><strong>To map:</strong> mapValue() - creates key-value pairs</li>
 * <li><strong>To matrix:</strong> matrixValue() - reshapes into 2D grid</li>
 * </ul>
 * 
 * <h2>Streaming and Iteration</h2>
 * <p>
 * Containers support Java 8 streams for functional-style operations:
 * </p>
 * <ul>
 * <li>{@link #stream(IScope)} - Sequential stream of values</li>
 * <li>{@link #parallelStream(IScope)} - Parallel stream for concurrent processing</li>
 * <li>{@link #iterable(IScope)} - Java iterable for for-each loops</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <pre>
 * // Querying containers
 * IContainer&lt;?, ?&gt; container = ...;
 * int size = container.length(scope);
 * boolean empty = container.isEmpty(scope);
 * boolean hasElement = container.contains(scope, someValue);
 * 
 * // Accessing elements
 * Object first = container.firstValue(scope);
 * Object last = container.lastValue(scope);
 * Object random = container.anyValue(scope);
 * 
 * // Converting between types
 * IList&lt;?&gt; list = container.listValue(scope, Types.NO_TYPE, false);
 * IMap&lt;?, ?&gt; map = container.mapValue(scope, Types.NO_TYPE, Types.NO_TYPE, false);
 * IMatrix&lt;?&gt; matrix = container.matrixValue(scope, Types.NO_TYPE, false);
 * 
 * // Streaming
 * container.stream(scope)
 *          .filter(v -&gt; condition(v))
 *          .map(v -&gt; transform(v))
 *          .forEach(v -&gt; process(v));
 * 
 * // Modifiable containers
 * if (container instanceof IContainer.Modifiable) {
 *     IContainer.Modifiable&lt;?, ?, ?, ?&gt; modifiable = (IContainer.Modifiable&lt;?, ?, ?, ?&gt;) container;
 *     modifiable.addValue(scope, newValue);
 * }
 * </pre>
 * 
 * @param <KeyType>
 *            the type used to address elements in the container
 * @param <ValueType>
 *            the type of elements stored in the container
 * 
 * @author drogoul
 * @since GAMA 1.0
 */
public interface IContainer<KeyType, ValueType> extends IRuntimeContainer<KeyType, ValueType> {

	/**
	 * Returns a copy of this container.
	 * 
	 * <p>
	 * The copy semantics depend on the container type:
	 * </p>
	 * <ul>
	 * <li><strong>Lists, maps, matrices:</strong> create a new container with the same elements (shallow copy)</li>
	 * <li><strong>Graphs:</strong> create a new graph with copies of vertices and edges</li>
	 * <li><strong>Populations:</strong> typically return the same population (agents cannot be copied)</li>
	 * </ul>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @return a copy of this container
	 * @throws GamaRuntimeException
	 *             if copying fails
	 */
	@Override
	IContainer<KeyType, ValueType> copy(IScope scope) throws GamaRuntimeException;

	/**
	 * Gets the GAMA container type of this container.
	 * 
	 * <p>
	 * Returns a {@link IContainerType} which includes information about both the container kind (list, map, matrix,
	 * etc.) and the types of keys and values it contains.
	 * </p>
	 *
	 * @return the GAMA container type
	 */
	@Override
	IContainerType<?> getGamlType();

	/**
	 * Converts this container to a list.
	 * 
	 * <p>
	 * The conversion semantics depend on the container type:
	 * </p>
	 * <ul>
	 * <li><strong>Lists:</strong> return this or a copy</li>
	 * <li><strong>Maps:</strong> return list of values (in insertion order)</li>
	 * <li><strong>Matrices:</strong> return list of all elements in row-major order</li>
	 * <li><strong>Graphs:</strong> return list of edges or vertices</li>
	 * <li><strong>Populations:</strong> return list of agents</li>
	 * </ul>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @param contentType
	 *            the desired element type of the resulting list
	 * @param copy
	 *            whether to force a copy even if this is already a list
	 * @return a list containing the container's elements
	 */
	IList<ValueType> listValue(IScope scope, IType<?> contentType, boolean copy);

	/**
	 * Converts this container to a matrix.
	 * 
	 * <p>
	 * The conversion semantics depend on the container type:
	 * </p>
	 * <ul>
	 * <li><strong>Matrices:</strong> return this or a copy</li>
	 * <li><strong>Lists:</strong> create a matrix with default dimensions</li>
	 * <li><strong>Maps:</strong> create a matrix from values</li>
	 * <li><strong>Images/Fields:</strong> return the underlying matrix data</li>
	 * </ul>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @param contentType
	 *            the desired element type of the resulting matrix
	 * @param copy
	 *            whether to force a copy even if this is already a matrix
	 * @return a matrix containing the container's elements
	 */
	IMatrix<?> matrixValue(IScope scope, IType<?> contentType, boolean copy);

	/**
	 * Converts this container to a matrix with specific dimensions.
	 * 
	 * <p>
	 * Elements are taken from this container and arranged into a matrix of the specified size. If there are fewer
	 * elements than cells, remaining cells are filled with default values. If there are more elements, excess elements
	 * are ignored.
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @param contentType
	 *            the desired element type of the resulting matrix
	 * @param size
	 *            the dimensions {columns, rows} of the resulting matrix
	 * @param copy
	 *            whether to force a copy
	 * @return a matrix of the specified size containing elements from this container
	 */
	IMatrix<?> matrixValue(IScope scope, IType<?> contentType, IPoint size, boolean copy);

	/**
	 * Converts this container to a map.
	 * 
	 * <p>
	 * The conversion semantics depend on the container type:
	 * </p>
	 * <ul>
	 * <li><strong>Maps:</strong> return this or a copy</li>
	 * <li><strong>Lists:</strong> create a map with indices as keys and elements as values</li>
	 * <li><strong>Matrices:</strong> create a map with points as keys and cell values as values</li>
	 * <li><strong>Pairs:</strong> create a single-entry map</li>
	 * </ul>
	 *
	 * @param <D>
	 *            the type of map values
	 * @param <C>
	 *            the type of map keys
	 * @param scope
	 *            the current GAMA execution scope
	 * @param keyType
	 *            the desired key type of the resulting map
	 * @param contentType
	 *            the desired value type of the resulting map
	 * @param copy
	 *            whether to force a copy even if this is already a map
	 * @return a map representing this container's data
	 */
	<D, C> IMap<C, D> mapValue(IScope scope, IType<C> keyType, IType<D> contentType, boolean copy);

	/**
	 * Returns an iterable over the container's values.
	 * 
	 * <p>
	 * This method allows containers to be used in Java for-each loops and other iteration contexts. The order of
	 * iteration depends on the container type (insertion order for maps, index order for lists, etc.).
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @return an iterable over the container's values
	 */
	java.lang.Iterable<? extends ValueType> iterable(final IScope scope);

	/**
	 * Returns a sequential stream of the container's values.
	 * 
	 * <p>
	 * This method provides access to Java 8 stream operations for functional-style processing. The stream is optimized
	 * to minimize overhead - for containers that are already Java collections, it returns their native stream.
	 * </p>
	 * 
	 * <p>
	 * The default implementation handles both Collection-based containers and other types by converting to a list if
	 * necessary.
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @return a StreamEx (enhanced stream) over the container's values
	 */
	@SuppressWarnings ("unchecked")
	default StreamEx<ValueType> stream(final IScope scope) {
		// #creates an explicit copy (see #3626) -- maybe too expensive though...
		// if (this instanceof Collection)
		// return StreamEx.of(ImmutableList.<ValueType> builder().addAll(this.iterable(scope)).build());
		if (this instanceof Collection) return StreamEx.of(((Collection<ValueType>) this).stream());
		return StreamEx.of(listValue(scope, Types.NO_TYPE, false));
	}

	/**
	 * Returns a parallel stream of the container's values.
	 * 
	 * <p>
	 * This method creates a stream that can process elements concurrently, which can significantly improve performance
	 * for large containers and CPU-intensive operations. The stream uses GAMA's agent parallel executor for proper
	 * thread management.
	 * </p>
	 * 
	 * <p>
	 * <strong>Note:</strong> Be cautious when using parallel streams with operations that modify shared state or use
	 * random number generators, as these may not be thread-safe.
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @return a parallel StreamEx over the container's values
	 */
	default StreamEx<ValueType> parallelStream(final IScope scope) {
		return stream(scope).parallel(GamaExecutorService.AGENT_PARALLEL_EXECUTOR);
	}

	/**
	 * Interface for containers that support element retrieval by key or index.
	 * 
	 * <p>
	 * This interface defines the read operations for addressable containers. Implementations can retrieve individual
	 * elements using a key/index, or multiple elements using a list of keys/indices.
	 * </p>
	 *
	 * @param <KeyType>
	 *            the type used to address elements
	 * @param <ValueType>
	 *            the type of elements returned
	 */
	public interface ToGet<KeyType, ValueType> extends IRuntimeContainer.ToGet<KeyType, ValueType> {}

	/**
	 * Interface combining IContainer with read capabilities.
	 * 
	 * <p>
	 * Addressable containers can be read from using keys/indices. This interface simply combines {@link IContainer}
	 * with {@link ToGet}, potentially with different type parameters for the addressing operations.
	 * </p>
	 *
	 * @param <Key>
	 *            the container's key type
	 * @param <Value>
	 *            the container's value type
	 * @param <AddressableKey>
	 *            the type used for addressing (may differ from Key)
	 * @param <AddressableValue>
	 *            the type returned by addressing (may differ from Value)
	 */
	public interface Addressable<Key, Value, AddressableKey, AddressableValue>
			extends IContainer<Key, Value>, IRuntimeContainer.Addressable<Key, Value, AddressableKey, AddressableValue>,
				IContainer.ToGet<AddressableKey, AddressableValue> {}

	/**
	 * Interface combining IContainer with write capabilities.
	 * 
	 * <p>
	 * Modifiable containers support adding, setting, and removing elements. This interface simply combines
	 * {@link IContainer} with {@link ToSet}, potentially with different type parameters for the modification
	 * operations.
	 * </p>
	 *
	 * @param <K>
	 *            the container's key type
	 * @param <V>
	 *            the container's value type
	 * @param <KeyToAdd>
	 *            the type used when adding/setting elements (may differ from K)
	 * @param <ValueToAdd>
	 *            the type of values to add/set (may differ from V)
	 */
	public interface Modifiable<K, V, KeyToAdd, ValueToAdd>
			extends IContainer<K, V>, IRuntimeContainer.Modifiable<K, V, KeyToAdd, ValueToAdd>,
				IContainer.ToSet<KeyToAdd, ValueToAdd> {}

	/**
	 * Interface for containers that support element modification and removal.
	 * 
	 * <p>
	 * This interface defines all write operations for mutable containers: adding elements (with or without an index),
	 * setting elements at specific indices, and removing elements (by value, by index, or in bulk).
	 * </p>
	 *
	 * @param <KeyType>
	 *            the type used to address elements
	 * @param <ValueType>
	 *            the type of elements to add/set
	 */
	public interface ToSet<KeyType, ValueType> extends IRuntimeContainer.ToSet<KeyType, ValueType> {

		void addValues(IScope scope, Object index, IContainer<?, ?> values);

		default void addValues(final IScope scope, final IContainer<?, ?> values) {
			addValues(scope, null, values);
		}

		@Override
		default void addValues(final IScope scope, final Object index, final IRuntimeContainer<?, ?> values) {
			addValues(scope, index,
					values instanceof IContainer<?, ?> c ? c : values.listValue(scope, Types.NO_TYPE, false));
		}

		@Override
		default void addValues(final IScope scope, final IRuntimeContainer<?, ?> values) {
			addValues(scope, null, values);
		}

		void removeIndexes(IScope scope, IContainer<?, ?> index);

		@Override
		default void removeIndexes(final IScope scope, final IRuntimeContainer<?, ?> index) {
			removeIndexes(scope,
					index instanceof IContainer<?, ?> c ? c : index.listValue(scope, Types.NO_TYPE, false));
		}

		void removeValues(IScope scope, IContainer<?, ?> values);

		@Override
		default void removeValues(final IScope scope, final IRuntimeContainer<?, ?> values) {
			removeValues(scope,
					values instanceof IContainer<?, ?> c ? c : values.listValue(scope, Types.NO_TYPE, false));
		}

	}

	// Operators available in GAML

	/**
	 * Contains.
	 *
	 * @param scope
	 *            the scope
	 * @param o
	 *            the o
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "contains", "contains_value" },
			can_be_const = true,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "true, if the container contains the right operand, false otherwise. 'contains' can also be written 'contains_value'. On graphs, it is equivalent to calling 'contains_edge'",
			masterDoc = true,
			comment = "the contains operator behavior depends on the nature of the operand",
			usages = { @usage (
					value = "if it is a list or a matrix, contains returns true if the list or matrix contains the right operand",
					examples = { @example (
							value = "[1, 2, 3] contains 2",
							equals = "true"),
							@example (
									value = "[{1,2}, {3,4}, {5,6}] contains {3,4}",
									equals = "true") }),
					@usage ("if it is a map, contains, which can also be written 'contains_value', returns true if the operand is a value of the map"),
					@usage ("if it is a pair, contains_key returns true if the operand is equal to the value of the pair"),
					@usage ("if it is a file, contains returns true it the operand is contained in the file content"),
					@usage ("if it is a population, contains returns true if the operand is an agent of the population, false otherwise"),
					@usage ("if it is a graph, contains can be written 'contains_edge' and  returns true if the operand is an edge of the graph, false otherwise (use 'contains_node' for testing the presence of a node)") },
			see = { "contains_all", "contains_any", "contains_key" })
	@test ("['aa'::'bb', 13::14] contains 'bb'")
	boolean contains(IScope scope, Object o) throws GamaRuntimeException;

	/**
	 * Contains key.
	 *
	 * @param scope
	 *            the scope
	 * @param o
	 *            the o
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "contains_key", "contains_node" },
			can_be_const = true,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })

	@doc (
			value = "true, if the left-hand operand -- the container -- contains a key -- or an index -- equal to the right-hand operand, false otherwise. On graphs, 'contains_key' is equivalent to calling 'contains_vertex' ",
			masterDoc = true,
			comment = "the behavior of contains_key depends on the nature of the container",
			usages = { @usage (
					value = "if it is a list, contains_key returns true if the right-hand operand is an integer and if it is a valid index (i.e. >= 0 and < length)",
					examples = { @example (
							isExecutable = true,
							value = "[1, 2, 3] contains_key 3",
							equals = "false"),
							@example (
									isExecutable = true,
									value = "[{1,2}, {3,4}, {5,6}] contains_key 0",
									equals = "true") }),
					@usage ("if it is a map, contains_key returns true if the operand is a key of the map"),
					@usage ("if it is a pair, contains_key returns true if the operand is equal to the key of the pair"),
					@usage ("if it is a matrix, contains_key returns true if the point operand is a valid index of the matrix (i.e. >= {0,0} and < {rows, col})"),
					@usage ("if it is a file, contains_key is applied to the file contents -- a container"),
					@usage ("if it is a graph, contains_key returns true if the graph contains the corresponding vertex") },
			see = { "contains_all", "contains", "contains_any" })
	@test ("['aa'::'bb', 13::14] contains_key 'aa'")
	boolean containsKey(IScope scope, Object o) throws GamaRuntimeException;

	/**
	 * First value.
	 *
	 * @param scope
	 *            the scope
	 * @return the value type
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "first",
			can_be_const = true,
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "the first value of the operand",
			masterDoc = true,
			comment = "the first operator behavior depends on the nature of the operand",
			usages = { @usage (
					value = "if it is a list, first returns the first element of the list, or nil if the list is empty",
					examples = { @example (
							value = "first ([1, 2, 3])",
							returnType = IKeyword.INT,
							equals = "1") }),
					@usage (
							value = "if it is a map, first returns the first value of the first pair (in insertion order)"),
					@usage (
							value = "if it is a file, first returns the first element of the content of the file (that is also a container)"),
					@usage (
							value = "if it is a population, first returns the first agent of the population"),
					@usage (
							value = "if it is a graph, first returns the first edge (in creation order)"),
					@usage (
							value = "if it is a matrix, first returns the element at {0,0} in the matrix"),
					@usage (
							value = "for a matrix of int or float, it will return 0 if the matrix is empty"),
					@usage (
							value = "for a matrix of object or geometry, it will return nil if the matrix is empty") },
			see = { "last" })
	ValueType firstValue(IScope scope) throws GamaRuntimeException;

	/**
	 * Last value.
	 *
	 * @param scope
	 *            the scope
	 * @return the value type
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "last",
			can_be_const = true,
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "the last element of the operand",
			masterDoc = true,
			comment = "the last operator behavior depends on the nature of the operand",
			usages = { @usage (
					value = "if it is a list, last returns the last element of the list, or nil if the list is empty",
					examples = { @example (
							value = "last ([1, 2, 3])",
							returnType = IKeyword.INT,
							equals = "3") }),
					@usage (
							value = "if it is a map, last returns the value of the last pair (in insertion order)"),
					@usage (
							value = "if it is a file, last returns the last element of the content of the file (that is also a container)"),
					@usage (
							value = "if it is a population, last returns the last agent of the population"),
					@usage (
							value = "if it is a graph, last returns a list containing the last edge created"),
					@usage (
							value = "if it is a matrix, last returns the element at {length-1,length-1} in the matrix"),
					@usage (
							value = "for a matrix of int or float, it will return 0 if the matrix is empty"),
					@usage (
							value = "for a matrix of object or geometry, it will return nil if the matrix is empty") },
			see = { "first" })
	ValueType lastValue(IScope scope) throws GamaRuntimeException;

	/**
	 * Length.
	 *
	 * @param scope
	 *            the scope
	 * @return the int
	 */
	@operator (
			value = "length",
			can_be_const = true,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "the number of elements contained in the operand",
			masterDoc = true,
			comment = "the length operator behavior depends on the nature of the operand",
			usages = { @usage (
					value = "if it is a list or a map, length returns the number of elements in the list or map",
					examples = { @example (
							value = "length([12,13])",
							equals = "2"),
							@example (
									value = "length([])",
									equals = "0") }),
					@usage ("if it is a population, length returns number of agents of the population"),
					@usage ("if it is a graph, length returns the number of vertexes or of edges (depending on the way it was created)"),
					@usage (
							value = "if it is a matrix, length returns the number of cells",
							examples = { @example (
									value = "length(matrix([[\"c11\",\"c12\",\"c13\"],[\"c21\",\"c22\",\"c23\"]]))",
									equals = "6") }) })
	int length(IScope scope);

	/**
	 * Int value.
	 *
	 * @param scope
	 *            the scope
	 * @return the int
	 */
	@Override
	default int intValue(final IScope scope) {
		return length(scope);
	}

	/**
	 * Checks if is empty.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if is empty
	 */
	@operator (
			value = "empty",
			can_be_const = true,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "true if the operand is empty, false otherwise.",
			masterDoc = true,
			comment = "the empty operator behavior depends on the nature of the operand",
			usages = { @usage (
					value = "if it is a list, empty returns true if there is no element in the list, and false otherwise",
					examples = { @example (
							value = "empty([])",
							equals = "true") }),
					@usage (
							value = "if it is a map, empty returns true if the map contains no key-value mappings, and false otherwise"),
					@usage (
							value = "if it is a file, empty returns true if the content of the file (that is also a container) is empty, and false otherwise"),
					@usage (
							value = "if it is a population, empty returns true if there is no agent in the population, and false otherwise"),
					@usage (
							value = "if it is a graph, empty returns true if it contains no vertex and no edge, and false otherwise"),
					@usage (
							value = "if it is a matrix of int, float or object, it will return true if all elements are respectively 0, 0.0 or null, and false otherwise"),
					@usage (
							value = "if it is a matrix of geometry, it will return true if the matrix contains no cell, and false otherwise") })
	boolean isEmpty(IScope scope);

	/**
	 * Reverse.
	 *
	 * @param scope
	 *            the scope
	 * @return the i container
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "reverse",
			can_be_const = true,
			type = ITypeProvider.TYPE_AT_INDEX + 1,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "the operand elements in the reversed order in a copy of the operand.",
			masterDoc = true,
			comment = "the reverse operator behavior depends on the nature of the operand",
			usages = { @usage (
					value = "if it is a list, reverse returns a copy of the operand list with elements in the reversed order",
					examples = { @example (
							value = "reverse ([10,12,14])",
							equals = "[14, 12, 10]",
							returnType = "list<int>") }),
					@usage (
							value = "if it is a map, reverse returns a copy of the operand map with each pair in the reversed order (i.e. all keys become values and values become keys)",
							examples = { @example (
									value = "reverse (['k1'::44, 'k2'::32, 'k3'::12])",
									equals = "[44::'k1', 32::'k2', 12::'k3']",
									returnType = "map<int,string>") }),
					@usage (
							value = "if it is a file, reverse returns a copy of the file with a reversed content"),
					@usage (
							value = "if it is a population, reverse returns a copy of the population with elements in the reversed order"),
					@usage (
							value = "if it is a graph, reverse returns a copy of the graph (with all edges and vertexes), with all of the edges reversed"),
					@usage (
							value = "if it is a matrix, reverse returns a new matrix containing the transpose of the operand.",
							examples = { @example (
									returnType = "matrix<string>",
									value = "reverse(matrix([[\"c11\",\"c12\",\"c13\"],[\"c21\",\"c22\",\"c23\"]]))",
									equals = "matrix([[\"c11\",\"c21\"],[\"c12\",\"c22\"],[\"c13\",\"c23\"]])") }) })
	IContainer<?, ?> reverse(IScope scope) throws GamaRuntimeException;

	/**
	 * @return one of the values stored in this container using GAMA.getRandom()
	 */
	@operator (
			value = { "one_of", "any" },
			can_be_const = false,
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "one of the values stored in this container  at a random key",
			masterDoc = true,
			comment = "the one_of operator behavior depends on the nature of the operand",
			usages = { @usage (
					value = "if the operand is empty, one_of returns nil"),
					@usage (
							value = "if it is a list or a matrix, one_of returns one of the values of the list or of the matrix",
							examples = { @example (
									value = "any ([1,2,3])",
									var = "i",
									equals = "1, 2 or 3",
									returnType = IKeyword.INT,
									test = false),
									@example ("string sMat <- one_of(matrix([[\"c11\",\"c12\",\"c13\"],[\"c21\",\"c22\",\"c23\"]])); 	// sMat equals \"c11\",\"c12\",\"c13\", \"c21\",\"c22\" or \"c23\""), }),
					@usage (
							value = "if it is a map, one_of returns one the value of a random pair of the map",
							examples = { @example ("int im <- one_of ([2::3, 4::5, 6::7]);	// im equals 3, 5 or 7"),
									@example (
											value = "[2::3, 4::5, 6::7].values contains im",
											returnType = IKeyword.BOOL,
											equals = "true") }),
					@usage (
							value = "if it is a graph, one_of returns one of the lists of edges"),
					@usage (
							value = "if it is a file, one_of returns one of the elements of the content of the file (that is also a container)"),
					@usage (
							value = "if it is a population, one_of returns one of the agents of the population",
							examples = { @example (
									value = "bug b <- one_of(bug);  	// Given a previously defined species bug, b is one of the created bugs, e.g. bug3",
									isExecutable = false) }) },
			see = { "contains" })
	@test ("one_of([]) = nil")
	@test ("int i  <- any([1,2,3]); [1,2,3] contains i")
	@test ("string sMat <- one_of(matrix([[\"c11\",\"c12\",\"c13\"],[\"c21\",\"c22\",\"c23\"]])); matrix([[\"c11\",\"c12\",\"c13\"],[\"c21\",\"c22\",\"c23\"]]) contains sMat")
	@test ("agent b <- one_of(agents);")
	ValueType anyValue(IScope scope);

}