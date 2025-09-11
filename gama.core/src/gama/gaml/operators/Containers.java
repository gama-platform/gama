/*******************************************************************************************************
 *
 * Containers.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.operators;

import static gama.gaml.compilation.GAML.notNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;

import com.google.common.base.Predicate;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.no_test;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.GamlAnnotations.test;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IOperatorCategory;
import gama.annotations.precompiler.ITypeProvider;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulationSet;
import gama.core.metamodel.population.MetaPopulation;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.ITopology;
import gama.core.metamodel.topology.grid.IGrid;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaListFactory.GamaListSupplier;
import gama.core.util.GamaMapFactory;
import gama.core.util.GamaMapFactory.GamaMapSupplier;
import gama.core.util.GamaPair;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.core.util.graph.IGraph;
import gama.core.util.matrix.GamaField;
import gama.core.util.matrix.GamaFloatMatrix;
import gama.core.util.matrix.GamaIntMatrix;
import gama.core.util.matrix.GamaObjectMatrix;
import gama.core.util.matrix.IMatrix;
import gama.gaml.compilation.GAML;
import gama.gaml.compilation.IOperatorValidator;
import gama.gaml.compilation.annotations.validator;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.expressions.operators.BinaryOperator;
import gama.gaml.interfaces.IGamlIssue;
import gama.gaml.species.ISpecies;
import gama.gaml.types.GamaFieldType;
import gama.gaml.types.GamaMatrixType;
import gama.gaml.types.GamaType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;

/**
 * Written by drogoul Modified on 31 juil. 2010
 *
 * GAML operators dedicated to containers (list, matrix, graph, etc.)
 *
 * @see also IMatrix, IContainer for other operators
 *
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class Containers {

	/**
	 * The Class InterleavingIterator.
	 */
	private static class InterleavingIterator extends AbstractIterator {

		/** The queue. */
		private final Queue<Iterator> queue = new ArrayDeque<>();

		/**
		 * Instantiates a new interleaving iterator.
		 *
		 * @param scope
		 *            the scope
		 * @param objects
		 *            the objects
		 */
		public InterleavingIterator(final IScope scope, final Object... objects) {
			for (final Object object : objects) {
				if (object instanceof IContainer) {
					queue.add(((IContainer) object).iterable(scope).iterator());
				} else if (object instanceof Iterator) {
					queue.add((Iterator) object);
				} else if (object instanceof Iterable) {
					queue.add(((Iterable) object).iterator());
				} else {
					queue.add(Iterators.singletonIterator(object));
				}
			}
		}

		@Override
		protected Object computeNext() {
			while (!queue.isEmpty()) {
				final Iterator topIter = queue.poll();
				if (topIter.hasNext()) {
					final Object result = topIter.next();
					queue.offer(topIter);
					return result;
				}
			}
			return endOfData();
		}
	}

	/**
	 * With.
	 *
	 * @param <T>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param filter
	 *            the filter
	 * @return the function
	 */
	public static <T> Function<Object, T> with(final IScope scope, final String eachName, final IExpression filter) {
		return t -> {
			scope.setEach(eachName, t);
			return (T) filter.value(scope);
		};
	}

	/**
	 * By.
	 *
	 * @param <T>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param filter
	 *            the filter
	 * @return the predicate
	 */
	public static <T> Predicate<T> by(final IScope scope, final String eachName, final IExpression filter) {
		return (final T t) -> {
			scope.setEach(eachName, t);
			return Cast.asBool(scope, filter.value(scope));
		};
	}

	/**
	 * In container.
	 *
	 * @param <T>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param l
	 *            the l
	 * @return the predicate
	 */
	public static <T> Predicate<T> inContainer(final IScope scope, final IContainer l) {
		final IContainer c = GAML.notNull(scope, l);
		return t -> c.contains(scope, t);
	}

	/** The to lists. */
	private static Function<Object, IList<?>> toLists =
			a -> a instanceof IList ? (IList) a : GamaListFactory.wrap(Types.NO_TYPE, a);

	/**
	 * Stream.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @return the stream ex
	 */
	public static StreamEx stream(final IScope scope, final IContainer c) {
		return notNull(scope, c).stream(scope);
	}

	/**
	 * List of.
	 *
	 * @param t
	 *            the t
	 * @return the gama list supplier
	 */
	public static GamaListSupplier listOf(final IType t) {
		return new GamaListSupplier(t);
	}

	/**
	 * List like.
	 *
	 * @param c
	 *            the c
	 * @return the supplier
	 */
	public static Supplier<IList> listLike(final IContainer c) {
		return new GamaListSupplier(c == null ? Types.NO_TYPE : c.getGamlType().getContentType());
	}

	/**
	 * List like.
	 *
	 * @param c
	 *            the c
	 * @param c1
	 *            the c 1
	 * @return the supplier
	 */
	public static Supplier<IList> listLike(final IContainer c, final IContainer c1) {
		return listOf(c.getGamlType().getContentType().findCommonSupertypeWith(c1.getGamlType().getContentType()));
	}

	/**
	 * As map of.
	 *
	 * @param k
	 *            the k
	 * @param v
	 *            the v
	 * @return the gama map supplier
	 */
	public static GamaMapSupplier asMapOf(final IType k, final IType v) {
		return new GamaMapSupplier(k, v);
	}

	/**
	 * The Class Range.
	 */
	public static abstract class Range {

		/**
		 * Range.
		 *
		 * @param scope
		 *            the scope
		 * @param end
		 *            the end
		 * @return the i list
		 */
		@operator (
				value = IKeyword.RANGE,
				content_type = IType.INT,
				category = { IOperatorCategory.CONTAINER },
				can_be_const = true)
		@doc (
				value = "builds a list of int representing all contiguous values from zero to the argument included. The range can be increasing or decreasing.",
				masterDoc = true,
				special_cases = "Passing 0 will return a singleton list with 0.",
				examples = {
						@example (
								value = "range(2)",
								equals = "[0,1,2]"),
						@example (
								value = "range(-2)",
								equals = "[0,-1,-2]"),
						@example (
								value = "range(1) collect(i: range(1) collect(j: i + j))",
								equals = "[[0,1],[1,2]]")
				})
		@test ("range(2) = [0,1,2]")
		@test ("range(-2) = [0,-1,-2]")
		@test ("range(1) collect(i: range(1) collect(j: i + j)) = [[0,1],[1,2]]")
		public static IList range(final IScope scope, final Integer end) {
			if (end == 0) return GamaListFactory.wrap(Types.INT, 0);
			return range(scope, 0, end);
		}

		/**
		 * Range.
		 *
		 * @param scope
		 *            the scope
		 * @param start
		 *            the start
		 * @param end
		 *            the end
		 * @return the i list
		 */
		@operator (
				value = { IKeyword.RANGE, "to" },
				content_type = IType.INT,
				category = { IOperatorCategory.CONTAINER },
				can_be_const = true)
		@doc (
				value = "the list of int representing all contiguous values from the first to the second argument included.",
				usages = { @usage (
						value = "When passing the same value for both arguments the operator will return a list containing only this value",
						examples = { 
							@example (
								value = "range(0,2)",
								equals = "[0,1,2]"),
							@example (
								value = "range(2,0)",
								equals = "[2,1,0]"),
							@example(
								value = "range(0,0)",
								equals = "[0]")		
						}) })
		@test ("range(0,2) = [0,1,2]")
		@test ("range(2,0) = [2,1,0]")
		@test ("range(0,0) = [0]")
		public static IList range(final IScope scope, final Integer start, final Integer end) {
			final Integer step = start > end ? -1 : 1;
			return range(scope, start, end, step);
		}

		/**
		 * Range.
		 *
		 * @param scope
		 *            the scope
		 * @param start
		 *            the start
		 * @param end
		 *            the end
		 * @param step
		 *            the step
		 * @return the i list
		 */
		@operator (
				value = IKeyword.RANGE,
				content_type = IType.INT,
				category = { IOperatorCategory.CONTAINER },
				can_be_const = true)
		@doc (
				value = "a list of int representing all contiguous values from the first to the second argument, using the step represented by the third argument.",
				usages = { @usage (
						value = "When used with 3 operands, it returns a list of int representing all contiguous values from the first to the second argument, using the step represented by the third argument. The range can be increasing or decreasing. Passing the same value for both will return a singleton list with this value. Passing a step of 0 will result in an exception. Attempting to build infinite ranges (e.g. end > start with a negative step) will similarly not be accepted and yield an exception",
						examples = { @example (
								value = "range(0,6,2)",
								equals = "[0,2,4,6]") }) })
		public static IList range(final IScope scope, final Integer start, final Integer end, final Integer step) {
			if (step == 0) throw GamaRuntimeException.error("The step of a range should not be equal to 0", scope);
			if (start.equals(end)) return GamaListFactory.wrap(Types.INT, start);
			if (end > start) {
				if (step < 0)
					throw GamaRuntimeException.error("Negative step would result in an infinite range", scope);
			} else if (step > 0)
				throw GamaRuntimeException.error("Positive step would result in an infinite range", scope);
			return IntStreamEx.rangeClosed(start, end, step).boxed().toCollection(listOf(Types.INT));

		}

		/**
		 * Every.
		 *
		 * @param scope
		 *            the scope
		 * @param source
		 *            the source
		 * @param step
		 *            the step
		 * @return the i list
		 */
		@operator (
				value = "every",
				content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
				category = { IOperatorCategory.CONTAINER },
				can_be_const = true)
		@doc (value = "Retrieves elements from the first argument every `step` (second argument) elements. Raises an error if the step is negative or equal to zero")
		@test ("[1,2,3,4,5] every 2 = [1,3,5]")
		public static IList every(final IScope scope, final IList source, final Integer step) {
			if (step <= 0)
				throw GamaRuntimeException.error("The step value in `every` should be strictly positive", scope);
			return IntStreamEx.range(0, notNull(scope, source).size(), step).mapToObj(source::get)
					.toCollection(listLike(source));
		}

		/**
		 * Copy between.
		 *
		 * @param scope
		 *            the scope
		 * @param l1
		 *            the l 1
		 * @param begin
		 *            the begin
		 * @param end
		 *            the end
		 * @return the i list
		 */
		@operator (
				value = { "copy_between", "between" },
				can_be_const = true,
				content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
				category = { IOperatorCategory.LIST },
				concept = { IConcept.CONTAINER, IConcept.LIST })
		@doc (
				value = "Returns a copy of the first operand between the indices determined by the second (inclusive) and third operands (exclusive)",
				examples = { @example (
						value = " copy_between ([4, 1, 6, 9 ,7], 1, 3)",
						equals = "[1, 6]") },
				usages = { @usage ("If the first operand is empty, returns an empty object of the same type"),
						@usage ("If the second operand is greater than or equal to the third operand, returns an empty object of the same type"),
						@usage ("If the first operand is nil, raises an error") },
				see = { "slice", "submatrix", "sublist" }
				)
		@test ("copy_between ([4, 1, 6, 9 ,7], 1, 3) = [1,6]")
		public static IList copy_between(final IScope scope, final IList l1, final Integer begin, final Integer end) {
			final int beginIndex = begin < 0 ? 0 : begin;
			final int size = notNull(scope, l1).size();
			final int endIndex = end > size ? size : end;
			final IList result = listLike(l1).get();
			if (beginIndex < endIndex) { result.addAll(l1.subList(beginIndex, endIndex)); }
			return result;
		}
		
		
		/**
		 * Makes sure a given index is valid for a list of given size.
		 * Converts a possibly negative index into a positive one (-1 = size-1, -2 = size - 2 etc.). 
		 * Checks for out of bound values and clamp if needed.
		 *
		 * @param idx
		 *            the idx
		 * @param size
		 *            the size
		 * @return the int
		 */
		protected static int convertToListIndex(final int idx, final int size) {
			int i = idx;
			if (idx < 0) {
				i = size + idx;
			}
			return Math.max(Math.min(i, size - 1),0);
		}
		
		@operator (
				value = { "sublist" },
				can_be_const = true,
				content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
				category = { IOperatorCategory.LIST },
				concept = { IConcept.CONTAINER, IConcept.LIST })
		@doc (
				value = "Returns a copy of the first operand composed by the elements at the indices given in the second operand",
				examples = { @example (
						value = "sublist ([4, 1, 6, 9 ,7], [2, 2, 4])",
						equals = "[6, 6, 7]") },
				usages = { 
						@usage ("If the first operand is empty, returns an empty object of the same type"),
						@usage ("If the first operand is nil, raises an error"),
						@usage ("Indices in the second operand can be negative, in which case they are counted from the end of the list (-1 being the last element, -2 the one before last, etc.)"),
						@usage ("If an index in the second operand is out of bounds (either > size of the first operand or < of - size) it will be clamped to 0 or size-1.") },
				see = { "copy_between", "between", "submatrix", "slice" }
				)
		@test ("sublist ([4, 1, 6, 9 ,7], [2, 2, 4]) = [6, 6, 7]")
		public static IList sublist(final IScope scope, final IList l1, final IList<Integer> indices) {
			
			final IList result = listLike(l1).get();
			
			if (l1.size() == 0) { return result; }
			
			for(int i : indices) {
				result.add(l1.get(convertToListIndex(i, l1.size())));
			}
			return result;
		}
		
		@operator (
				value = { "slice"},
				can_be_const = true,
				content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
				category = { IOperatorCategory.LIST },
				concept = { IConcept.CONTAINER, IConcept.LIST })
		@doc (
				value = "Returns a copy of the first operand between the indices determined by the second (inclusive) and third operands (inclusive) using the increment given by the fourth operand",
				examples = { @example (
						value = " slice ([4, 1, 6, 9 ,7], 1, 5, 2)",
						equals = "[1, 9]") },
				usages = { @usage ("If the first operand is empty, returns an empty object of the same type"),
						@usage ("If the second or third operand is less than 0 it is considered as counting from the end of the list, -1 representing the last element, -2 the one before last, etc."),
						@usage ("If the first operand is nil, raises an error") },
				see = { "copy_between", "between", "sublist", "submatrix" }
				)
		@test ("slice ([4, 1, 6, 9 ,7], 1, 5, 2) = [1,9]")
		public static IList slice(final IScope scope, final IList l1, final Integer begin, final Integer end, final Integer step) {
			//TODO: could generate the list of indices then call sublist but would do one more round of convertToListIndex and create a potentially big intermediate list
			final int size = notNull(scope, l1).size();
			final IList result = listLike(l1).get();
			final boolean positiveStep = step > 0;

			if (step == 0)
				return result;
			
			
			int beginIdx = convertToListIndex(begin, size);
			int endIdx = convertToListIndex(end, size);

			for (int i = beginIdx; positiveStep && i <= endIdx || !positiveStep && i >= endIdx; i += step) {
				result.add(l1.get(i));
			}
			return result;
		}
		

		@operator (
				value = { "slice"},
				can_be_const = true,
				content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
				category = { IOperatorCategory.LIST },
				concept = { IConcept.CONTAINER, IConcept.LIST })
		@doc (
				value = "Returns a copy of the first operand from the index determined by the second (inclusive) to the one determined by the third operand (inclusive).",
				examples = { 
						@example (
							value = " slice ([4, 1, 6, 9 ,7], 1, 5)",
							equals = "[1, 6, 9, 7]"),		
						@example (
								value = " slice ([4, 1, 6, 9 ,7], 5, 1)",
								equals = "[7, 9, 6, 1]")
				},
				usages = { @usage("If the second index is less than the first, the list is built in reverse order"),
						@usage ("If the first operand is empty, returns an empty object of the same type"),
						@usage ("If the second or third operand is less than 0 it is considered as counting from the end of the list, -1 representing the last element, -2 the one before last, etc."),
						@usage ("If the first operand is nil, raises an error") },
				see = { "copy_between", "between", "sublist", "submatrix" }
				)
		@test ("slice ([4, 1, 6, 9 ,7], 1, 5) = [1, 6, 9, 7]")
		@test ("slice ([4, 1, 6, 9 ,7], 5, 1) = [7, 9, 6, 1]")
		public static IList slice(final IScope scope, final IList l1, final Integer begin, final Integer end) {			
			final int size = notNull(scope, l1).size();
			int beginIdx = convertToListIndex(begin, size);
			int endIdx = convertToListIndex(end, size);
			return slice(scope, l1, beginIdx, endIdx, endIdx==beginIdx ? 1 : (int) Math.signum(endIdx-beginIdx));
		}
		
		@operator (
				value = {  "slice", "submatrix" },
				can_be_const = true,
				content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
				category = { IOperatorCategory.MATRIX},
				concept = { IConcept.CONTAINER, IConcept.MATRIX })
		@doc (
				value = "Returns a submatrix of the matrix or field given as first operand for columns and rows with the indices given by the second and third operands, following their order in the lists",
				examples = { @example (
						value = " slice (matrix([[1, 4, 7], [2, 5, 8], [3, 6, 9], [1, 1, 1]]), [2, 1, 3], [0, 1])",
						equals = "matrix([[3, 6], [2, 5], [1, 1]])") },
				usages = { @usage ("If the first operand is empty, returns an empty object of the same type"),
						@usage ("If the second operand is greater than or equal to the third operand, returns an empty object of the same type"),
						@usage ("If the first operand is nil, raises an error") },
				see = { "copy_between", "between", "slice" })
		@test ("submatrix (matrix([[1, 4, 7], [2, 5, 8], [3, 6, 9 ], [1, 1, 1]]), [2, 1, 3], [0, 1]) = matrix([[3, 6], [2, 5], [1, 1]])")
		public static IMatrix submatrix(final IScope scope, final IMatrix m1, final List<Integer> columns, final List<Integer> rows) {

			final GamaPoint aimedDimensions = new GamaPoint(columns.size(), rows.size());
			final IMatrix result = GamaMatrixType.matrixLike(scope, m1, aimedDimensions);
			
			for(int colIdx = 0; colIdx < columns.size(); colIdx ++) {
				for(int rowIdx = 0; rowIdx < rows.size(); rowIdx ++) {
					result.set(scope, colIdx, rowIdx, m1.get(scope, columns.get(colIdx), rows.get(rowIdx)) );
				}
			}
			
			return result;
		}
		

		@operator (
				value = {  "slice", "submatrix" },
				can_be_const = true,
				content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
				category = { IOperatorCategory.MATRIX},
				concept = { IConcept.CONTAINER, IConcept.MATRIX })
		@doc (
				value = "Returns a subfield of the field given as first operand for columns and rows with the indices given by the second and third operands, following their order in the lists",
				examples = { @example (
						value = " slice (field([[1, 4, 7], [2, 5, 8], [3, 6, 9], [1, 1, 1]]), [2, 1, 3], [0, 1])",
						equals = "field([[3, 6], [2, 5], [1, 1]])") },
				usages = { @usage ("If the first operand is empty, returns an empty object of the same type"),
						@usage ("If the second operand is greater than or equal to the third operand, returns an empty object of the same type"),
						@usage ("If the first operand is nil, raises an error") },
				see = { "copy_between", "between", "slice" })
		@test ("submatrix (field([[1, 4, 7], [2, 5, 8], [3, 6, 9 ], [1, 1, 1]]), [2, 1, 3], [0, 1]) = field([[3, 6], [2, 5], [1, 1]])")
		public static GamaField submatrix(final IScope scope, final GamaField f, final List<Integer> columns, final List<Integer> rows) {
			return (GamaField) submatrix(scope, (IMatrix)f, columns, rows);
		}
		
		@operator (
				value = {  "slice", "submatrix" },
				can_be_const = true,
				content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
				category = { IOperatorCategory.MATRIX},
				concept = { IConcept.CONTAINER, IConcept.MATRIX })
		@doc (
				value = "Returns a submatrix of the matrix or field given as first operand for columns between the indices determined by the second and rows between those determined by the third operands using the increment given by the fourth operand",
				examples = { @example (
						value = " slice (matrix([[1, 4, 7], [2, 5, 8], [3, 6, 9], [1, 1, 1]]), 1::3, 0::1, 2::1)",
						equals = "matrix([[2, 5], [1, 1]])") },
				usages = { @usage ("If the first operand is empty, returns an empty object of the same type"),
						@usage ("If the second operand is greater than or equal to the third operand, returns an empty object of the same type"),
						@usage ("If the first operand is nil, raises an error") },
				see = { "copy_between", "between", "slice" })
		@test ("slice (matrix([[1, 4, 7], [2, 5, 8], [3, 6, 9 ], [1, 1, 1]]), 1::3, 0::1, 2::1) = matrix([[2, 5], [1, 1]])")
		public static IMatrix submatrix(final IScope scope, final IMatrix m1, final GamaPair<Integer, Integer> columns, final GamaPair<Integer, Integer> rows, final GamaPair<Integer, Integer> steps) {

			//TODO: this definition relies on the submatrix that uses lists of indices, could be optimized to avoid creating the intermediate lists
			final GamaPoint initialDimensions = notNull(scope, m1).getDimensions();
			final int startCol = convertToListIndex(columns.key, (int)initialDimensions.x);
			final int endCol = convertToListIndex(columns.value, (int)initialDimensions.x);
			final int startRow = convertToListIndex(rows.key, (int)initialDimensions.y);
			final int endRow = convertToListIndex(rows.value, (int)initialDimensions.y);

			final boolean positiveColStep = steps.key > 0;
			final boolean positiveRowStep = steps.value > 0;
			
			// Eliminating nonsensical cases
			if (steps.key == 0 || steps.value == 0) {
				return GamaMatrixType.matrixLike(scope, m1, new GamaPoint(0,0));
			}
			if (positiveColStep && startCol > endCol || !positiveColStep && startCol < endCol) {
				return GamaMatrixType.matrixLike(scope, m1, new GamaPoint(0,0));
			}
			if (positiveRowStep && startRow > endRow || !positiveRowStep && startRow < endRow) {
				return GamaMatrixType.matrixLike(scope, m1, new GamaPoint(0,0));
			}
			
			
			List<Integer> cols = new ArrayList<>();
			List<Integer> rowsList = new ArrayList<>();
			for(int col = startCol; positiveColStep && col <= endCol || !positiveColStep && col >= endCol ; col += steps.key) {
				cols.add(col);
			}
			for(int row = startRow; positiveRowStep && row <= endRow || !positiveRowStep && row >= endRow; row += steps.value) {
				rowsList.add(row);
			}
			
			return submatrix(scope, m1, cols, rowsList);
		}
		
		@operator (
				value = {  "slice", "submatrix" },
				can_be_const = true,
				content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
				category = { IOperatorCategory.MATRIX},
				concept = { IConcept.CONTAINER, IConcept.MATRIX })
		@doc (
				value = "Returns a subfield of the field given as first operand for columns between the indices determined by the second and rows between those determined by the third operands using the increment given by the fourth operand",
				examples = { @example (
						value = " slice (field([[1, 4, 7], [2, 5, 8], [3, 6, 9], [1, 1, 1]]), 1::3, 0::1, 2::1)",
						equals = "field([[2, 5], [1, 1]])") },
				usages = { @usage ("If the first operand is empty, returns an empty object of the same type"),
						@usage ("If the second operand is greater than or equal to the third operand, returns an empty object of the same type"),
						@usage ("If the first operand is nil, raises an error") },
				see = { "copy_between", "between", "slice" })
		@test ("slice (field([[1, 4, 7], [2, 5, 8], [3, 6, 9 ], [1, 1, 1]]), 1::3, 0::1, 2::1) = field([[2, 5], [1, 1]])")
		public static GamaField submatrix(final IScope scope, final GamaField f, final GamaPair<Integer, Integer> columns, final GamaPair<Integer, Integer> rows, final GamaPair<Integer, Integer> steps) {	
			return (GamaField) submatrix(scope, (IMatrix)f, columns, rows, steps);
		}
		
		@operator (
				value = {  "slice", "submatrix" },
				can_be_const = true,
				content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
				category = { IOperatorCategory.MATRIX},
				concept = { IConcept.CONTAINER, IConcept.MATRIX })
		@doc (
				value = "Returns a submatrix of the matrix or field given as first operand for columns between the indices determined by the second and rows between those determined by the third operands.",
				examples = { @example (
						value = " slice (matrix([[1, 4, 7], [2, 5, 8], [3, 6, 9 ], [1, 1, 1]]), 1::3, 0::1)",
						equals = "matrix([2, 5], [3, 6], [1, 1]])") },
				usages = { @usage ("If the first operand is empty, returns an empty object of the same type"),
						@usage ("If the second operand is greater than or equal to the third operand, returns an empty object of the same type"),
						@usage ("If the first operand is nil, raises an error") },
				see = { "copy_between", "between", "slice" })
		@test ("slice (matrix([[1, 4, 7], [2, 5, 8], [3, 6, 9 ], [1, 1, 1]]), 1::3, 0::1) = matrix([[2, 5], [3, 6], [1, 1]])")
		public static IMatrix submatrix(final IScope scope, final IMatrix m1, final GamaPair<Integer, Integer> columns, final GamaPair<Integer, Integer> rows) {
			final int firstCol = convertToListIndex(columns.key,(int) m1.getDimensions().x);
			final int lastCol = convertToListIndex(columns.value,(int) m1.getDimensions().x);
			final int firstRow = convertToListIndex(rows.key,(int) m1.getDimensions().y);
			final int lastRow = convertToListIndex(rows.value,(int) m1.getDimensions().y);
			GamaPair<Integer, Integer> steps = new GamaPair<>(	lastCol == firstCol ? 1 : (int) Math.signum(lastCol-firstCol), 
																lastRow == firstRow ? 1 : (int) Math.signum(lastRow-firstRow), 
																Types.INT, 
																Types.INT);
			return submatrix(scope, m1, columns, rows, steps);
		}
		
		@operator (
				value = {  "slice", "submatrix" },
				can_be_const = true,
				content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
				category = { IOperatorCategory.MATRIX},
				concept = { IConcept.CONTAINER, IConcept.MATRIX })
		@doc (
				value = "Returns a subfield of the field given as first operand for columns between the indices determined by the second and rows between those determined by the third operands.",
				examples = { @example (
						value = " slice (field([[1, 4, 7], [2, 5, 8], [3, 6, 9 ], [1, 1, 1]]), 1::3, 0::1)",
						equals = "field([2, 5], [3, 6], [1, 1]])") },
				usages = { @usage ("If the first operand is empty, returns an empty object of the same type"),
						@usage ("If the second operand is greater than or equal to the third operand, returns an empty object of the same type"),
						@usage ("If the first operand is nil, raises an error") },
				see = { "copy_between", "between", "slice" })
		@test ("slice (field([[1, 4, 7], [2, 5, 8], [3, 6, 9 ], [1, 1, 1]]), 1::3, 0::1) = field([[2, 5], [3, 6], [1, 1]])")
		public static GamaField submatrix(final IScope scope, final GamaField f, final GamaPair<Integer, Integer> columns, final GamaPair<Integer, Integer> rows) {
			return (GamaField) submatrix(scope, (IMatrix) f, columns, rows);
		}
		

		/**
		 * Copy between.
		 *
		 * @param scope
		 *            the scope
		 * @param l1
		 *            the l 1
		 * @param p
		 *            the p
		 * @return the i list
		 */
		@operator (
				internal = true,
				value = { "internal_between" },
				can_be_const = true,
				content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
				category = { IOperatorCategory.LIST },
				concept = { IConcept.CONTAINER, IConcept.LIST })
		@doc (
				value = "For internal use only. Corresponds to the implementation, for containers, of the access with [begin::end]",
				masterDoc = true)
		public static IList copy_between(final IScope scope, final IList l1, final GamaPair p) {
			return copy_between(scope, l1, Cast.asInt(scope, p.key), Cast.asInt(scope, p.value));
		}

	}

	/**
	 * Internal list.
	 *
	 * @param scope
	 *            the scope
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @return the i list
	 */
	@operator (
			internal = true,
			value = { "internal_list" },
			content_type = IType.INT,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER, IConcept.MATRIX })
	@doc (
			value = "For internal use only.Corresponds to the 2 elements list created when accessed matrices with int cols and rows",
			masterDoc = true)
	@no_test
	public static IList internal_list(final IScope scope, final Integer i, final Integer j) {
		return GamaListFactory.create(scope, Types.INT, i, j);
	}

	/**
	 * Internal at.
	 *
	 * @param scope
	 *            the scope
	 * @param shape
	 *            the shape
	 * @param indices
	 *            the indices
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			internal = true,
			value = { "internal_at" },
			content_type = IType.NONE,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER, IConcept.GEOMETRY })
	@doc (
			value = "For internal use only. Corresponds to the implementation, for geometries, of the access to containers with [index]",
			masterDoc = true)
	@no_test
	public static Object internal_at(final IScope scope, final IShape shape, final IList indices)
			throws GamaRuntimeException {
		// TODO How to test if the index is correct ?
		if (shape == null) return null;
		final String key = Cast.asString(scope, indices.get(0));
		return shape.getAttribute(key);
		// final IMap map = (IMap) shape.getAttributes();
		// if (map == null) { return null; }
		// return map.getFromIndicesList(scope, indices);
	}

	/**
	 * Internal at.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @param indices
	 *            the indices
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			internal = true,
			value = { "internal_at" },
			content_type = IType.NONE,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.SPECIES })
	@doc ("For internal use only. Corresponds to the implementation of the access to agents with [index]")
	@no_test
	public static Object internal_at(final IScope scope, final IAgent agent, final IList indices)
			throws GamaRuntimeException {
		if (agent == null) return null;
		return agent.getFromIndicesList(scope, indices);
	}

	/**
	 * Internal at.
	 *
	 * @param scope
	 *            the scope
	 * @param container
	 *            the container
	 * @param indices
	 *            the indices
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			internal = true,
			value = { "internal_at" },
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "For internal use only. Corresponds to the implementation of the access to containers with [index]",
			see = { IKeyword.AT })
	@no_test
	@validator (InternalAtValidator.class)
	public static Object internal_at(final IScope scope, final IContainer container, final IList indices)
			throws GamaRuntimeException {
		if (container instanceof IContainer.Addressable)
			return ((IContainer.Addressable) container).getFromIndicesList(scope, indices);
		throw GamaRuntimeException.error("" + container + " cannot be accessed using " + indices, scope);
	}

	/**
	 * At.
	 *
	 * @param scope
	 *            the scope
	 * @param container
	 *            the container
	 * @param key
	 *            the key
	 * @return the object
	 */
	@operator (
			value = { IKeyword.AT, "@" },
			can_be_const = true,
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "the element at the right operand index of the container",
			masterDoc = true,
			comment = """
					The first element of the container is located at the index 0. \
					In addition, if the user tries to get the element at an index higher or equals than the length of the container, he will get an IndexOutOfBoundException.\
					The at operator behavior depends on the nature of the operand""",
			usages = { @usage (
					value = "if it is a list or a matrix, at returns the element at the index specified by the right operand",
					examples = { @example (
							value = "[1, 2, 3] at 2",
							returnType = IKeyword.INT,
							equals = "3"),
							@example (
									value = "[{1,2}, {3,4}, {5,6}] at 0",
									returnType = IKeyword.POINT,
									equals = "{1.0,2.0}") }),
					@usage ("if it is a file, at returns the element of the file content at the index specified by the right operand"),
					@usage ("if it is a population, at returns the agent at the index specified by the right operand"),
					@usage ("if it is a graph and if the right operand is a node, at returns the in and out edges corresponding to that node"),
					@usage ("if it is a graph and if the right operand is an edge, at returns the pair node_out::node_in of the edge"),
					@usage ("if it is a graph and if the right operand is a pair node1::node2, at returns the edge from node1 to node2 in the graph") },
			see = { "contains_all", "contains_any" })
	@validator (AtValidator.class)
	public static Object at(final IScope scope, final IContainer container, final Object key) {
		if (container instanceof IContainer.Addressable) return ((IContainer.Addressable) container).get(scope, key);
		throw GamaRuntimeException.error("" + container + " cannot be accessed using " + key, scope);
	}

	/**
	 * The Class AtValidator.
	 */
	public static class AtValidator implements IOperatorValidator {

		@Override
		public boolean validate(final IDescription context, final EObject emfContext, final IExpression... arguments) {
			IType type = arguments[0].getGamlType();
			final IType indexType = arguments[1].getGamlType();
			if (Types.FILE.isAssignableFrom(type)) { type = type.getWrappedType(); }
			final IType keyType = type.getKeyType();
			final boolean wrongKey = keyType != Types.NO_TYPE && !indexType.isTranslatableInto(keyType);
			if (wrongKey) {
				context.error("The contents of this " + type.getGamlType().getName() + " can only be accessed with "
						+ type.getKeyType() + " keys", IGamlIssue.WRONG_TYPE, emfContext);
				return false;
			}
			return true;
		}

	}

	/**
	 * The Class InternalAtValidator.
	 */
	public static class InternalAtValidator implements IOperatorValidator {

		@Override
		public boolean validate(final IDescription context, final EObject emfContext, final IExpression... arguments) {
			// Used in remove, for instance
			if (Types.isEmpty(arguments[1])) return true;
			IType type = arguments[0].getGamlType();
			// It is normally a list with 1 or 2 indices
			final IType indexType = arguments[1].getGamlType().getContentType();
			if (Types.FILE.isAssignableFrom(type)) { type = type.getWrappedType(); }
			final IType keyType = type.getKeyType();
			final boolean wrongKey = keyType != Types.NO_TYPE && !indexType.isTranslatableInto(keyType)
					&& (!Types.MATRIX.isAssignableFrom(type) || indexType != Types.INT);
			if (wrongKey) {
				context.error("The contents of this " + type.getGamlType().getName() + " can only be accessed with "
						+ type.getKeyType() + " keys", IGamlIssue.WRONG_TYPE, emfContext);
				return false;
			}
			return true;
		}

	}

	/**
	 * At.
	 *
	 * @param scope
	 *            the scope
	 * @param container
	 *            the container
	 * @param key
	 *            the key
	 * @return the object
	 */
	@operator (
			value = { IKeyword.AT, "@" },
			can_be_const = true,
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc ("the element at the right operand index of the container")
	@no_test
	public static Object at(final IScope scope, final IList container, final Integer key) {
		return container.get(scope, key);
	}

	/**
	 * At.
	 *
	 * @param scope
	 *            the scope
	 * @param container
	 *            the container
	 * @param key
	 *            the key
	 * @return the object
	 */
	@operator (
			value = { IKeyword.AT, "@" },
			can_be_const = true,
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc ("the element at the right (point) operand index of the matrix")
	@no_test
	public static Object at(final IScope scope, final IMatrix container, final GamaPoint key) {
		return container.get(scope, key);
	}

	/**
	 * At.
	 *
	 * @param scope
	 *            the scope
	 * @param species
	 *            the species
	 * @param key
	 *            the key
	 * @return the i agent
	 */
	@operator (
			value = { IKeyword.AT, "@" },
			can_be_const = true,
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc ("the agent at the right operand index of the given species")
	@no_test
	public static IAgent at(final IScope scope, final ISpecies species, final Integer key) {
		return species.get(scope, key);
	}

	/**
	 * Grid at.
	 *
	 * @param scope
	 *            the scope
	 * @param s
	 *            the s
	 * @param val
	 *            the val
	 * @return the i agent
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "grid_at" },
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.POINT, IOperatorCategory.GRID },
			concept = { IConcept.GRID, IConcept.POINT })
	@doc (
			value = "returns the cell of the grid (right-hand operand) at the position given by the right-hand operand",
			comment = "If the left-hand operand is a point of floats, it is used as a point of ints.",
			usages = { @usage ("if the left-hand operand is not a grid cell species, returns nil") },
			examples = { @example (
					value = "grid_cell grid_at {1,2}",
					equals = "the agent grid_cell with grid_x=1 and grid_y = 2",
					isExecutable = false) })
	@no_test
	public static IAgent grid_at(final IScope scope, final ISpecies s, final GamaPoint val)
			throws GamaRuntimeException {
		final ITopology t = scope.getAgent().getPopulationFor(s).getTopology();
		final IContainer<?, IShape> m = t.getPlaces();
		if (m instanceof IGrid) {
			final IShape shp = ((IGrid) m).get(scope, val);
			if (shp != null) return shp.getAgent();
		}
		return null;
	}

	/**
	 * Removes the duplicates.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @return the i list
	 */
	@operator (
			value = { "remove_duplicates", "distinct" },
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			index_type = ITypeProvider.KEY_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "produces a set from the elements of the operand (i.e. a list without duplicated elements)",
			usages = { @usage (
					value = "if the operand is empty, remove_duplicates returns an empty list",
					examples = { @example (
							value = "remove_duplicates([])",
							equals = "[]") }),
					@usage (
							value = "if the operand is a graph, remove_duplicates returns the set of nodes"),
					@usage (
							value = "if the operand is a map, remove_duplicates returns the set of values without duplicate",
							examples = { @example (
									value = "remove_duplicates([1::3,2::4,3::3,5::7])",
									equals = "[3,4,7]") }),
					@usage (
							value = "if the operand is a matrix, remove_duplicates returns a list containing all the elments with duplicated.",
							examples = { @example (
									value = "remove_duplicates([[\"c11\",\"c12\",\"c13\",\"c13\"],[\"c21\",\"c22\",\"c23\",\"c23\"]])",
									equals = "[[\"c11\",\"c12\",\"c13\",\"c21\",\"c22\",\"c23\"]]",
									test = false) }) },
			examples = { @example (
					value = "remove_duplicates([3,2,5,1,2,3,5,5,5])",
					equals = "[3,2,5,1]") })
	@test ("remove_duplicates([3,2,5,1,2,3,5,5,5]) = [3,2,5,1]")
	public static IList remove_duplicates(final IScope scope, final IContainer c) {
		return (IList) stream(scope, c).distinct().toCollection(listLike(c));
	}

	/**
	 * Contains all.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param c2
	 *            the c 2
	 * @return the boolean
	 */
	@operator (
			value = "contains_all",
			can_be_const = true,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "true if the left operand contains all the elements of the right operand, false otherwise",
			comment = "the definition of contains depends on the container",
			usages = { @usage ("if the right operand is nil or empty, contains_all returns true") },
			examples = { @example (
					value = "[1,2,3,4,5,6] contains_all [2,4]",
					equals = "true "),
					@example (
							value = "[1,2,3,4,5,6] contains_all [2,8]",
							equals = "false"),
					@example (
							value = "[1::2, 3::4, 5::6] contains_all [1,3]",
							equals = "false "),
					@example (
							value = "[1::2, 3::4, 5::6] contains_all [2,4]",
							equals = "true") },
			see = { "contains", "contains_any" })
	@test ("[1,2,3,4,5,6] contains_all [2,8] = false")
	@test ("[1::2, 3::4, 5::6] contains_all [1,3] = false")
	@test ("[1::2, 3::4, 5::6] contains_all [2,4] = true")
	public static Boolean contains_all(final IScope scope, final IContainer c, final IContainer c2) {
		return stream(scope, c2).allMatch(inContainer(scope, c));
	}

	/**
	 * Contains any.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param c1
	 *            the c 1
	 * @return the boolean
	 */
	@operator (
			value = "contains_any",
			can_be_const = true,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "true if the left operand contains one of the elements of the right operand, false otherwise",
			comment = "the definition of contains depends on the container",
			special_cases = { "if the right operand is nil or empty, contains_any returns false" },
			examples = { @example (
					value = "[1,2,3,4,5,6] contains_any [2,4]",
					equals = "true "),
					@example (
							value = "[1,2,3,4,5,6] contains_any [2,8]",
							equals = "true"),
					@example (
							value = "[1::2, 3::4, 5::6] contains_any [1,3]",
							equals = "false"),
					@example (
							value = "[1::2, 3::4, 5::6] contains_any [2,4]",
							equals = "true") },
			see = { "contains", "contains_all" })
	@test ("[1,2,3,4,5,6] contains_any [2,4] = true")
	@test ("[1,2,3,4,5,6] contains_any [2,8] = true")
	@test ("[1::2, 3::4, 5::6] contains_any [2,4] = true")
	public static Boolean contains_any(final IScope scope, final IContainer c, final IContainer c1) {
		return stream(scope, c1).anyMatch(inContainer(scope, c));
	}

	/**
	 * First.
	 *
	 * @param scope
	 *            the scope
	 * @param number
	 *            the number
	 * @param c
	 *            the c
	 * @return the i list
	 */
	@operator (
			value = { "first", "first_of" },
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "Returns the nth first elements of the container. If n is greater than the list size, a translation of the container to a list is returned. If it is equal or less than zero, returns an empty list")
	@test ("first(3, [1,2,3,4,5,6]) = [1,2,3]")
	@test ("first(0,[1,2,3,4,5,6]) = []")
	@test ("first_of(3, [1,2,3,4,5,6]) = [1,2,3]")
	@test ("first_of(0,[1,2,3,4,5,6]) = []")
	public static IList first(final IScope scope, final Integer number, final IContainer c) {
		return (IList) stream(scope, c).limit(number < 0 ? 0 : number).toCollection(listLike(c));
	}

	/**
	 * Last.
	 *
	 * @param scope
	 *            the scope
	 * @param number
	 *            the number
	 * @param c
	 *            the c
	 * @return the i list
	 */
	@operator (
			value = { "last", "last_of" },
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "Returns the nth last elements of the container. If n is greater than the list size,  returns the container cast as a list. If it is equal or less than zero, returns an empty list")
	@test ("last(3, [1,2,3,4,5,6]) = [4,5,6]")
	@test ("last(0,[1,2,3,4,5,6]) = []")
	@test ("last(10,[1::2, 3::4]) is list")
	public static IList last(final IScope scope, final Integer number, final IContainer c) {
		int n = number < 0 ? 0 : number;
		return (IList) stream(scope, c).skip(Math.max(0, c.length(scope) - n)).toCollection(listLike(c));
	}

	/**
	 * In.
	 *
	 * @param scope
	 *            the scope
	 * @param o
	 *            the o
	 * @param c
	 *            the c
	 * @return the boolean
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "in",
			can_be_const = true,
			category = { IOperatorCategory.CONTAINER })
	@doc (
			value = "true if the right operand contains the left operand, false otherwise",
			comment = "the definition of in depends on the container",
			usages = { @usage ("if the right operand is nil or empty, in returns false") },
			examples = { @example (
					value = "2 in [1,2,3,4,5,6]",
					equals = "true"),
					@example (
							value = "7 in [1,2,3,4,5,6]",
							equals = "false"),
					@example (
							value = "3 in [1::2, 3::4, 5::6]",
							equals = "false"),
					@example (
							value = "6 in [1::2, 3::4, 5::6]",
							equals = "true") },
			see = { "contains" })
	@test ("2 in [1,2,3,4,5,6] = true")
	@test ("3 in [1::2, 3::4, 5::6] = false")

	public static Boolean in(final IScope scope, final Object o, final IContainer c) throws GamaRuntimeException {
		return notNull(scope, c).contains(scope, o);
	}

	/**
	 * Index of.
	 *
	 * @param scope
	 *            the scope
	 * @param s
	 *            the s
	 * @param o
	 *            the o
	 * @return the integer
	 */
	@operator (
			value = "index_of",
			can_be_const = true,
			category = { IOperatorCategory.SPECIES },
			concept = { IConcept.CONTAINER, IConcept.SPECIES })
	@doc (
			usages = @usage ("if the left operator is a species, returns the index of an agent in a species. "
					+ "If the argument is not an agent of this species, returns -1. Use int(agent) instead"),
			masterDoc = true)
	@no_test
	public static Integer index_of(final IScope scope, final ISpecies s, final Object o) {
		if (!(o instanceof IAgent) || !((IAgent) o).isInstanceOf(notNull(scope, s), true)) return -1;
		return ((IAgent) o).getIndex();
	}

	/**
	 * Index of.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param o
	 *            the o
	 * @return the integer
	 */
	@operator (
			value = "index_of",
			can_be_const = true,
			category = { IOperatorCategory.LIST },
			concept = { IConcept.LIST })
	@doc (
			value = "the index of the first occurence of the right operand in the left operand container",
			masterDoc = true,
			comment = "The definition of index_of and the type of the index depend on the container",
			usages = @usage (
					value = "if the left operand is a list, index_of returns the index as an integer",
					examples = { @example (
							value = "[1,2,3,4,5,6] index_of 4",
							equals = "3"),
							@example (
									value = "[4,2,3,4,5,4] index_of 4",
									equals = "0") }),
			see = { "at", "last_index_of" })
	@test ("[1,2,3,1,2,1,4,5] index_of 4 = 6")
	public static Integer index_of(final IScope scope, final IList c, final Object o) {
		return notNull(scope, c).indexOf(o);
	}

	/**
	 * Index of.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param o
	 *            the o
	 * @return the object
	 */
	@operator (
			value = "index_of",
			can_be_const = true,
			category = { IOperatorCategory.MAP },
			concept = { IConcept.MAP })
	@doc (
			usages = @usage ("if the left operand is a map, index_of returns the index of a value or nil if the value is not mapped"),
			examples = { @example (
					value = "[1::2, 3::4, 5::6] index_of 4",
					equals = "3") })
	@test ("[1::2, 3::4, 5::6] index_of 4 = 3")
	public static Object index_of(final IScope scope, final IMap<?, ?> c, final Object o) {
		for (final Map.Entry<?, ?> k : notNull(scope, c).entrySet()) { if (k.getValue().equals(o)) return k.getKey(); }
		return null;
	}

	/**
	 * Index of.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param o
	 *            the o
	 * @return the gama point
	 */
	@operator (
			value = "index_of",
			can_be_const = true,
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.CONTAINER, IConcept.MATRIX })
	@doc (
			usages = @usage (
					value = "if the left operand is a matrix, index_of returns the index as a point",
					examples = { @example (
							value = "matrix([[1,2,3],[4,5,6]]) index_of 4",
							equals = "{1.0,0.0}") }))
	@test ("matrix([[1,2,3],[4,5,6]]) index_of 4 = {1.0,0.0}")
	public static GamaPoint index_of(final IScope scope, final IMatrix c, final Object o) {
		for (int i = 0; i < notNull(scope, c).getCols(scope); i++) {
			for (int j = 0; j < c.getRows(scope); j++) { if (c.get(scope, i, j).equals(o)) return new GamaPoint(i, j); }
		}
		return null;
	}

	/**
	 * All indexes of 2.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param o
	 *            the o
	 * @return the i list
	 */
	@operator (
			value = "all_indexes_of",
			can_be_const = true,
			content_type = ITypeProvider.KEY_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.LIST },
			concept = { IConcept.LIST })
	@doc (
			value = "all the index of all the occurences of the right operand in the left operand container",
			masterDoc = true,
			comment = "The definition of all_indexes_of and the type of the index depend on the container",
			usages = @usage (
					value = "if the left operand is a list, all_indexes_of returns a list of all the indexes as integers",
					examples = { @example (
							value = "[1,2,3,1,2,3] all_indexes_of 1",
							equals = "[0,3]"),
							@example (
									value = "[1,2,3,1,2,3] all_indexes_of 4",
									equals = "[]") }),
			see = { "index_of", "last_index_of" })
	public static IList all_indexes_of2(final IScope scope, final IList c, final Object o) {
		final IList results = GamaListFactory.create(Types.INT);
		for (int i = 0; i < notNull(scope, c).size(); i++) { if (o.equals(c.get(scope, i))) { results.add(i); } }
		return results;

		// Note: I also tested the following version with streams, but it was around 2 times slower...
		// return (IList) IntStream.range(0, notNull(scope,c).size()).filter(i -> c.get(scope,i) ==
		// o).boxed().toList();
	}

	/**
	 * Last index of.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param o
	 *            the o
	 * @return the integer
	 */
	@operator (
			value = "last_index_of",
			can_be_const = true,
			category = { IOperatorCategory.SPECIES },
			concept = { IConcept.CONTAINER, IConcept.SPECIES })
	@doc (
			value = "the index of the last occurence of the right operand in the left operand container",
			usages = @usage ("if the left operand is a species, the last index of an agent is the same as its index"),
			see = { "at", "index_of" })
	@test ("last_index_of([1,2,2,2,5], 2) = 3")
	public static Integer last_index_of(final IScope scope, final ISpecies c, final Object o) {
		return index_of(scope, notNull(scope, c), o);
	}

	/**
	 * Last index of.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param o
	 *            the o
	 * @return the integer
	 */
	@operator (
			value = "last_index_of",
			can_be_const = true,
			category = { IOperatorCategory.LIST },
			concept = { IConcept.LIST })
	@doc (
			value = "the index of the last occurence of the right operand in the left operand container",
			masterDoc = true,
			comment = "The definition of last_index_of and the type of the index depend on the container",
			usages = { @usage (
					value = "if the left operand is a list, last_index_of returns the index as an integer",
					examples = { @example (
							value = "[1,2,3,4,5,6] last_index_of 4",
							equals = "3"),
							@example (
									value = "[4,2,3,4,5,4] last_index_of 4",
									equals = "5") }) },
			see = { "at", "last_index_of" })
	@test ("[4,2,3,4,5,4] last_index_of 4 = 5")
	public static Integer last_index_of(final IScope scope, final IList c, final Object o) {
		return notNull(scope, c).lastIndexOf(o);
	}

	/**
	 * Last index of.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param o
	 *            the o
	 * @return the gama point
	 */
	@operator (
			value = "last_index_of",
			can_be_const = true,
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.CONTAINER, IConcept.MATRIX })
	@doc (
			value = "the index of the last occurence of the right operand in the left operand container",
			usages = @usage (
					value = "if the left operand is a matrix, last_index_of returns the index as a point",
					examples = { @example (
							value = "matrix([[1,2,3],[4,5,4]]) last_index_of 4",
							equals = "{1.0,2.0}") }))
	@test ("matrix([[1,2,3],[4,5,4]]) last_index_of 4 = {1.0,2.0}")
	public static GamaPoint last_index_of(final IScope scope, final IMatrix c, final Object o) {
		for (int i = notNull(scope, c).getCols(scope) - 1; i > -1; i--) {
			for (int j = c.getRows(scope) - 1; j > -1; j--) {
				if (c.get(scope, i, j).equals(o)) return new GamaPoint(i, j);
			}
		}
		return null;
	}

	/**
	 * Last index of.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param o
	 *            the o
	 * @return the object
	 */
	@operator (
			value = "last_index_of",
			can_be_const = true,
			type = ITypeProvider.KEY_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MAP },
			concept = { IConcept.MAP })
	@doc (
			value = "the index of the last occurence of the right operand in the left operand container",
			usages = @usage (
					value = "if the left operand is a map, last_index_of returns the index as an int (the key of the pair)",
					examples = { @example (
							value = "[1::2, 3::4, 5::4] last_index_of 4",
							equals = "5") }))
	@test ("[1::2, 3::4, 5::4] last_index_of 4 = 5")
	public static Object last_index_of(final IScope scope, final IMap<?, ?> c, final Object o) {
		for (final Map.Entry<?, ?> k : Lists.reverse(new ArrayList<>(notNull(scope, c).entrySet()))) {
			if (k.getValue().equals(o)) return k.getKey();
		}
		return null;
	}

	/**
	 * Inter.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param c1
	 *            the c 1
	 * @return the i list
	 */
	@operator (
			value = "inter",
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER })
	@doc (
			value = "the intersection of the two operands",
			comment = "both containers are transformed into sets (so without duplicated element, cf. remove_deplicates operator) before the set intersection is computed.",
			usages = { @usage (
					value = "if an operand is a graph, it will be transformed into the set of its nodes"),
					@usage (
							value = "if an operand is a map, it will be transformed into the set of its values",
							examples = { @example (
									value = "[1::2, 3::4, 5::6] inter [2,4]",
									equals = "[2,4]"),
									@example (
											value = "[1::2, 3::4, 5::6] inter [1,3]",
											equals = "[]") }),
					@usage (
							value = "if an operand is a matrix, it will be transformed into the set of the lines",
							examples = @example (
									value = "matrix([[3,2,1],[4,5,4]]) inter [3,4]",
									equals = "[3,4]")) },
			examples = { @example (
					value = "[1,2,3,4,5,6] inter [2,4]",
					equals = "[2,4]"),
					@example (
							value = "[1,2,3,4,5,6] inter [0,8]",
							equals = "[]") },
			see = { "remove_duplicates" })
	@test ("[1,2,3,4,5,6] inter [0,8] = []")
	public static IList inter(final IScope scope, final IContainer c, final IContainer c1) {
		return (IList) stream(scope, c).filter(inContainer(scope, c1)).distinct().toCollection(listLike(c, c1));
	}

	/**
	 * Minus.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param l
	 *            the l
	 * @return the i list
	 */
	@operator (
			value = IKeyword.MINUS,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns a new list in which all the elements of the right operand have been removed from the left one",
			comment = "The behavior of the operator depends on the type of the operands.",
			usages = { @usage (
					value = "if both operands are containers and the right operand is empty, " + IKeyword.MINUS
							+ " returns the left operand"),
					@usage (
							value = "if both operands are containers, returns a new list in which all the elements of the right operand have been removed from the left one",
							examples = { @example (
									value = "[1,2,3,4,5,6] - [2,4,9]",
									returnType = "list<int>",
									equals = "[1,3,5,6]"),
									@example (
											value = "[1,2,3,4,5,6] - [0,8]",
											returnType = "list<int>",
											equals = "[1,2,3,4,5,6]") }) },
			see = { "" + IKeyword.PLUS })
	@test ("[1,2,3,4,5,6] - [0,8] = [1,2,3,4,5,6]")
	public static IList minus(final IScope scope, final IContainer source, final IContainer l) {
		final IList result =
				notNull(scope, source).listValue(scope, source.getGamlType().getContentType(), false).copy(scope);
		result.removeAll(notNull(scope, l).listValue(scope, Types.NO_TYPE, false));
		return result;
	}

	/**
	 * Minus.
	 *
	 * @param scope
	 *            the scope
	 * @param l1
	 *            the l 1
	 * @param object
	 *            the object
	 * @return the i list
	 */
	@operator (
			value = IKeyword.MINUS,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER })
	@doc (
			usages = { @usage (
					value = "if the left operand is a list and the right operand is an object of any type (except list), "
							+ IKeyword.MINUS
							+ " returns a list containing the elements of the left operand minus the first occurence of this object",
					examples = { @example (
							value = "[1,2,3,4,5,6,2] - 2",
							returnType = "list<int>",
							equals = "[1,3,4,5,6,2]"),
							@example (
									value = "[1,2,3,4,5,6] - 0",
									returnType = "list<int>",
									equals = "[1,2,3,4,5,6]") }) })
	@test ("[1,2,3,4,5,6] - 0 = [1,2,3,4,5,6]")
	public static IList minus(final IScope scope, final IList l1, final Object object) {
		final IList result = notNull(scope, l1).copy(scope);
		result.remove(object);
		return result;
	}

	/**
	 * Minus.
	 *
	 * @param scope
	 *            the scope
	 * @param l1
	 *            the l 1
	 * @param object
	 *            the object
	 * @return the i list
	 */
	@operator (
			value = IKeyword.MINUS,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = IOperatorCategory.CONTAINER,
			concept = {})
	@doc (
			usages = { @usage (
					value = "if the left operand is a species and the right operand is an agent of the species, "
							+ IKeyword.MINUS
							+ " returns a list containing all the agents of the species minus this agent") })
	@test ("([1,2,2,3,5] - 3) = [1,2,2,5] ")
	public static IList minus(final IScope scope, final ISpecies l1, final IAgent object) {
		return minus(scope, l1.listValue(scope, scope.getType(l1.getName()), false), object);
	}

	/**
	 * Of generic species.
	 *
	 * @param scope
	 *            the scope
	 * @param agents
	 *            the agents
	 * @param s
	 *            the s
	 * @return the i list
	 */
	@operator (
			value = "of_generic_species",
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
			category = IOperatorCategory.SPECIES,
			concept = { IConcept.SPECIES })
	@doc (
			value = "a list, containing the agents of the left-hand operand whose species is that denoted by the right-hand operand "
					+ "and whose species extends the right-hand operand species ",
			examples = { @example (
					value = "// species speciesA {}"),
					@example (
							value = "// species sub_speciesA parent: speciesA {}"),
					@example (
							value = "[sub_speciesA(0),sub_speciesA(1),speciesA(2),speciesA(3)] of_generic_species speciesA",
							equals = "[sub_speciesA0,sub_speciesA1,speciesA0,speciesA1]",
							isExecutable = false),
					@example (
							value = "[sub_speciesA(0),sub_speciesA(1),speciesA(2),speciesA(3)] of_generic_species sous_test",
							equals = "[sub_speciesA0,sub_speciesA1]",
							isExecutable = false),
					@example (
							value = "[sub_speciesA(0),sub_speciesA(1),speciesA(2),speciesA(3)] of_species speciesA",
							equals = "[speciesA0,speciesA1]",
							isExecutable = false),
					@example (
							value = "[sub_speciesA(0),sub_speciesA(1),speciesA(2),speciesA(3)] of_species sous_test",
							equals = "[sub_speciesA0,sub_speciesA1]",
							isExecutable = false) },
			see = { "of_species" })
	public static IList of_generic_species(final IScope scope, final IContainer agents, final ISpecies s) {
		return of_species(scope, notNull(scope, agents), notNull(scope, s), true);
	}

	/**
	 * Of species.
	 *
	 * @param scope
	 *            the scope
	 * @param agents
	 *            the agents
	 * @param s
	 *            the s
	 * @return the i list
	 */
	@operator (
			value = "of_species",
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
			category = IOperatorCategory.SPECIES,
			concept = { IConcept.SPECIES })
	@doc (
			value = """
					a list, containing the agents of the left-hand operand whose species is the one denoted by the right-hand operand.\
					The expression agents of_species (species self) is equivalent to agents where (species each = species self); \
					however, the advantage of using the first syntax is that the resulting list is correctly typed with the right species, \
					whereas, in the second syntax, the parser cannot determine the species of the agents within the list \
					(resulting in the need to cast it explicitly if it is to be used in an ask statement, for instance).""",
			usages = @usage ("if the right operand is nil, of_species returns the right operand"),
			examples = { @example (
					value = "(self neighbors_at 10) of_species (species (self))",
					equals = "all the neighboring agents of the same species.",
					isExecutable = false),
					@example (
							value = "[test(0),test(1),node(1),node(2)] of_species test",
							equals = "[test0,test1]",
							isExecutable = false) },
			see = { "of_generic_species" })
	@no_test
	public static IList of_species(final IScope scope, final IContainer agents, final ISpecies s) {
		return of_species(scope, notNull(scope, agents), notNull(scope, s), false);
	}

	/**
	 * Of species.
	 *
	 * @param scope
	 *            the scope
	 * @param agents
	 *            the agents
	 * @param s
	 *            the s
	 * @param generic
	 *            the generic
	 * @return the i list
	 */
	private static IList of_species(final IScope scope, final IContainer agents, final ISpecies s,
			final boolean generic) {
		return (IList) agents.stream(scope).filter(each -> each instanceof IAgent a && a.isInstanceOf(s, !generic))
				.toCollection(listOf(scope.getType(s.getName())));
	}

	/**
	 * Pair.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the gama pair
	 */
	@operator (
			value = { "::", "pair" },
			can_be_const = true,
			type = IType.PAIR,
			index_type = ITypeProvider.TYPE_AT_INDEX + 1,
			content_type = ITypeProvider.TYPE_AT_INDEX + 2,
			concept = { IConcept.CONTAINER })
	@doc (
			value = "produces a new pair combining the left and the right operands",
			special_cases = "nil is not acceptable as a key (although it is as a value). If such a case happens, :: will throw an appropriate error")
	@test ("string(1::2) = '1::2'")
	public static GamaPair pair(final IScope scope, final IExpression a, final IExpression b) {
		final Object v1 = a.value(scope);
		final Object v2 = b.value(scope);
		return new GamaPair(notNull(scope, v1), v2, a.getGamlType(), b.getGamlType());
	}

	/**
	 * Plus.
	 *
	 * @param scope
	 *            the scope
	 * @param c1
	 *            the c 1
	 * @param c2
	 *            the c 2
	 * @return the i container
	 */
	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			type = ITypeProvider.BOTH,
			content_type = ITypeProvider.BOTH,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns a new list containing all the elements of both operands",
			usages = { @usage (
					value = "if one of the operands is nil, " + IKeyword.PLUS + " throws an error"),
					@usage (
							value = "if both operands are species, returns a special type of list called meta-population"),
					@usage (
							value = "if both operands are list, " + IKeyword.PLUS
									+ "returns the concatenation of both lists.",
							examples = { @example (
									value = "[1,2,3,4,5,6] + [2,4,9]",
									returnType = "list<int>",
									equals = "[1,2,3,4,5,6,2,4,9]"),
									@example (
											value = "[1,2,3,4,5,6] + [0,8]",
											returnType = "list<int>",
											equals = "[1,2,3,4,5,6,0,8]") }) },
			see = { "" + IKeyword.MINUS })
	@test ("[1,2,3,4,5,6] + [2,4,9] = [1,2,3,4,5,6,2,4,9]")
	public static IContainer plus(final IScope scope, final IContainer c1, final IContainer c2) {
		// special case for the addition of two populations or meta-populations
		if (c1 instanceof IPopulationSet && c2 instanceof IPopulationSet) {
			final MetaPopulation mp = new MetaPopulation();
			mp.addPopulationSet((IPopulationSet) c1);
			mp.addPopulationSet((IPopulationSet) c2);
			return mp;
		}
		return (IContainer) stream(scope, c1).append(stream(scope, c2)).toCollection(listLike(c1, c2));
	}

	/**
	 * Plus.
	 *
	 * @param scope
	 *            the scope
	 * @param l1
	 *            the l 1
	 * @param l
	 *            the l
	 * @return the i list
	 */
	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = IOperatorCategory.CONTAINER,
			concept = {})
	@validator (PlusListValidator.class)
	@doc (
			usages = @usage (
					value = "if the right operand is an object of any type (except a container), " + IKeyword.PLUS
							+ " returns a list of the elements of the left operand, to which this object has been added",
					examples = { @example (
							value = "[1,2,3,4,5,6] + 2",
							returnType = "list<int>",
							equals = "[1,2,3,4,5,6,2]"),
							@example (
									value = "[1,2,3,4,5,6] + 0",
									returnType = "list<int>",
									equals = "[1,2,3,4,5,6,0]") }))
	@test ("[1,2,3,4,5,6] + 2 = [1,2,3,4,5,6,2]")
	public static IList plus(final IScope scope, final IContainer l1, final Object l) {
		final IList result = notNull(scope, l1).listValue(scope, Types.NO_TYPE, false).copy(scope);
		result.addValue(scope, l);
		return result;
	}

	/**
	 * The Class PlusListValidator.
	 */
	public static class PlusListValidator implements IOperatorValidator {

		@Override
		public boolean validate(final IDescription context, final EObject emfContext, final IExpression... arguments) {
			IExpression item = arguments[1];
			IExpression list = arguments[0];
			IType valueType = arguments[1].getGamlType();
			IType contentType = arguments[0].getGamlType().getContentType();
			if (contentType != Types.NO_TYPE && !valueType.isTranslatableInto(contentType)
					&& !Types.isEmptyContainerCase(contentType, item)) {
				StringBuilder message =
						new StringBuilder("The type of the elements of ").append(list.serializeToGaml(false))
								.append(" (").append(contentType).append(") does not match with the type of the ");
				message.append("argument");
				message.append(" (").append(valueType).append("). ");
				message.append("The argument will be casted to ").append(contentType).append(". ");
				context.warning(message.toString(), IGamlIssue.WRONG_TYPE, emfContext);
			}
			return true;
		}

	}

	/**
	 * Union.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param c1
	 *            the c 1
	 * @return the i list
	 */
	@operator (
			value = "union",
			can_be_const = true,
			content_type = ITypeProvider.BOTH,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns a new list containing all the elements of both containers without duplicated elements.",
			comment = "",
			usages = { @usage ("if the left or right operand is nil, union throws an error") },
			examples = { @example (
					value = "[1,2,3,4,5,6] union [2,4,9]",
					equals = "[1,2,3,4,5,6,9]"),
					@example (
							value = "[1,2,3,4,5,6] union [0,8]",
							equals = "[1,2,3,4,5,6,0,8]"),
					@example (
							value = "[1,3,2,4,5,6,8,5,6] union [0,8]",
							equals = "[1,3,2,4,5,6,8,0]") },
			see = { "inter", IKeyword.PLUS })
	@test ("[1,2,3,4,5,6] union [2,4,9] = [1,2,3,4,5,6,9]")
	public static IList union(final IScope scope, final IContainer c, final IContainer c1) {
		return (IList) stream(scope, c).append(stream(scope, c1)).distinct().toCollection(listLike(c, c1));
	}

	// ITERATORS

	/**
	 * Group by.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param e
	 *            the e
	 * @return the i map
	 */
	@operator (
			value = { "group_by" },
			iterator = true,
			index_type = ITypeProvider.TYPE_AT_INDEX + 3,
			content_type = IType.LIST,
			content_type_content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
			concept = { IConcept.CONTAINER, IConcept.MAP })
	@doc (
			value = "Returns a map, where the keys take the possible values of the right-hand operand and the map values are the list of elements "
					+ "of the left-hand operand associated to the key value",
			masterDoc = true,
			comment = "in the right-hand operand, the keyword each can be used to represent, in turn, each of the right-hand operand elements. ",
			usages = { @usage ("if the left-hand operand is nil, group_by throws an error") },
			examples = { @example (
					value = "[1,2,3,4,5,6,7,8] group_by (each > 3)",
					equals = "[false::[1, 2, 3], true::[4, 5, 6, 7, 8]]"),
					@example (
							value = "g2 group_by (length(g2 out_edges_of each) )",
							equals = "[ 0::[node9, node7, node10, node8, node11], 1::[node6], 2::[node5], 3::[node4]]",
							isExecutable = false),
					@example (
							value = "(list(node) group_by (round(node(each).location.x))",
							equals = "[32::[node5], 21::[node1], 4::[node0], 66::[node2], 96::[node3]]",
							isExecutable = false),
					@example (
							value = "[1::2, 3::4, 5::6] group_by (each > 4)",
							equals = "[false::[2, 4], true::[6]]",
							returnType = "map<bool,list>") },
			see = { "first_with", "last_with", "where" })
	@test ("[1,2,3,4,5,6,7,8] group_by (each > 3) = [false::[1, 2, 3], true::[4, 5, 6, 7, 8]]")
	@test ("[1::2, 3::4, 5::6] group_by (each > 4) = [false::[2, 4], true::[6]]")
	public static IMap group_by(final IScope scope, final String eachName, final IContainer c, final IExpression e) {
		final IType ct = notNull(scope, c).getGamlType().getContentType();
		return (IMap) stream(scope, c).groupingTo(with(scope, eachName, e), asMapOf(e.getGamlType(), Types.LIST.of(ct)),
				listOf(ct));
	}

	/**
	 * Last with.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param filter
	 *            the filter
	 * @return the object
	 */
	@operator (
			value = { "last_with" },
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
			iterator = true,
			expected_content_type = IType.BOOL,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER, IConcept.FILTER })
	@doc (
			value = "the last element of the left-hand operand that makes the right-hand operand evaluate to true.",
			comment = "in the right-hand operand, the keyword each can be used to represent, in turn, each of the right-hand operand elements. ",
			usages = { @usage ("if the left-hand operand is nil, last_with throws an error."),
					@usage ("If there is no element that satisfies the condition, it returns nil"), @usage (
							value = "if the left-operand is a map, the keyword each will contain each value",
							examples = { @example (
									value = "[1::2, 3::4, 5::6] last_with (each >= 4)",
									equals = "6"),
									@example (
											value = "[1::2, 3::4, 5::6].pairs last_with (each.value >= 4)",
											equals = "(5::6)") }) },
			examples = { @example (
					value = "[1,2,3,4,5,6,7,8] last_with (each > 3)",
					equals = "8",
					returnType = IKeyword.INT),
					@example (
							value = "graph g2 <- graph([]);",
							isTestOnly = true),
					@example (
							value = "g2 last_with (length(g2 out_edges_of each) = 0 )",
							equals = "a node",
							isExecutable = false),
					@example (
							value = "(list(node) last_with (round(node(each).location.x) > 32)",
							equals = "node3",
							isExecutable = false) },
			see = { "group_by", "first_with", "where" })
	@test ("[1,2,3,4,5,6,7,8] last_with (each > 3) = 8")
	public static Object last_with(final IScope scope, final String eachName, final IContainer c,
			final IExpression filter) {
		return stream(scope, c).filter(by(scope, eachName, filter)).reduce((a, b) -> b).orElse(null);
	}

	/**
	 * First with.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param filter
	 *            the filter
	 * @return the object
	 */
	@operator (
			value = { "first_with" },
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
			iterator = true,
			expected_content_type = IType.BOOL,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER, IConcept.FILTER })
	@doc (
			value = "the first element of the left-hand operand that makes the right-hand operand evaluate to true.",
			comment = "in the right-hand operand, the keyword each can be used to represent, in turn, each of the right-hand operand elements. ",
			usages = {
					@usage ("if the left-hand operand is nil, first_with throws an error. If there is no element that satisfies the condition, it returns nil"),
					@usage (
							value = "if the left-operand is a map, the keyword each will contain each value",
							examples = { @example (
									value = "[1::2, 3::4, 5::6] first_with (each >= 4)",
									equals = "4",
									returnType = IKeyword.INT),
									@example (
											value = "[1::2, 3::4, 5::6].pairs first_with (each.value >= 4)",
											equals = "(3::4)",
											returnType = IKeyword.PAIR) }) },
			examples = { @example (
					value = "[1,2,3,4,5,6,7,8] first_with (each > 3)",
					equals = "4"),
					@example (
							value = "graph g2 <- graph([]);",
							isTestOnly = true),
					@example (
							value = "g2 first_with (length(g2 out_edges_of each) = 0)",
							equals = "node9",
							test = false),
					@example (
							value = "(list(node) first_with (round(node(each).location.x) > 32)",
							equals = "node2",
							isExecutable = false) },
			see = { "group_by", "last_with", "where" })
	@test ("[1,2,3,4,5,6,7,8] first_with (each > 3) = 4")
	public static Object first_with(final IScope scope, final String eachName, final IContainer c,
			final IExpression filter) {
		return stream(scope, c).findFirst(by(scope, eachName, filter)).orElse(null);
	}

	/**
	 * Sum.
	 *
	 * @param scope
	 *            the scope
	 * @param l
	 *            the l
	 * @return the object
	 */
	@operator (
			value = "sum",
			can_be_const = true,
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			expected_content_type = { IType.INT, IType.FLOAT, IType.POINT, IType.COLOR, IType.STRING },
			category = { IOperatorCategory.STATISTICAL, IOperatorCategory.CONTAINER, IOperatorCategory.COLOR },
			concept = { IConcept.STATISTIC, IConcept.COLOR })
	@doc (
			value = "the sum of all the elements of the operand",
			masterDoc = true,
			comment = "the behavior depends on the nature of the operand",
			usages = { @usage (
					value = "if it is a list of int or float: sum returns the sum of all the elements",
					examples = { @example (
							value = "sum ([12,10,3])",
							returnType = IKeyword.INT,
							equals = "25") }),
					@usage (
							value = "if it is a list of points: sum returns the sum of all points as a point (each coordinate is the sum of the corresponding coordinate of each element)",
							examples = { @example (
									value = "sum([{1.0,3.0},{3.0,5.0},{9.0,1.0},{7.0,8.0}])",
									equals = "{20.0,17.0}") }),
					@usage (
							value = "if it is a population or a list of other types: sum transforms all elements into float and sums them"),
					@usage (
							value = "if it is a map, sum returns the sum of the value of all elements"),
					@usage (
							value = "if it is a file, sum returns the sum of the content of the file (that is also a container)"),
					@usage (
							value = "if it is a graph, sum returns the total weight of the graph"),
					@usage (
							value = "if it is a matrix of int, float or object, sum returns the sum of all the numerical elements (i.e. all elements for integer and float matrices)"),
					@usage (
							value = "if it is a matrix of other types: sum transforms all elements into float and sums them"),
					@usage (
							value = "if it is a list of colors: sum will sum them and return the blended resulting color") },
			see = { "mul" })
	@test ("sum([{1.0,3.0},{3.0,5.0},{9.0,1.0},{7.0,8.0}]) = {20.0,17.0}")
	@test ("sum ([12,10,3]) = 25")
	public static Object sum(final IScope scope, final IContainer l) {
		return sum_of(scope, IKeyword.EACH, l, null);
	}

	/**
	 * Sum.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @return the double
	 */
	@operator (
			value = "sum",
			can_be_const = true,
			doc = @doc ("Returns the sum of the weights of the graph nodes"),
			category = { IOperatorCategory.GRAPH },
			concept = { IConcept.GRAPH })
	@test ("sum(as_edge_graph(line([{10,10},{30,10}]))) = 20.0")
	public static double sum(final IScope scope, final IGraph g) {
		if (g == null) return 0.0;
		return g.computeTotalWeight();
	}

	/**
	 * Cart prod.
	 *
	 * @param scope
	 *            the scope
	 * @param list
	 *            the list
	 * @return the object
	 */
	@operator (
			value = "cartesian_product",
			// can_be_const = true,
			doc = @doc ("Returns the cartesian product of elements in all given sub-lists"),
			expected_content_type = { IType.LIST },
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@test ("cartesian_product([['A','B'],['C','D']]) = [['A','C'],['A','D'],['B','C'],['B','D']]")
	public static Object cart_prod(final IScope scope, final IList list) {
		IType ct = list.getGamlType().getContentType();
		if (!ct.isContainer()) throw GamaRuntimeException.error("Must be a list of list", scope);

		final IList<IList> l = notNull(scope, list).listValue(scope, list.getGamlType().getContentType(), false);
		List<? extends Set<Object>> setOfSet = l.stream(scope).map(LinkedHashSet::new).collect(Collectors.toList());

		IList<IList> res = GamaListFactory.create(ct);
		Set<List<Object>> cp = Sets.cartesianProduct(setOfSet);
		for (List cartList : cp) { res.add(GamaListFactory.create(scope, ct.getContentType(), cartList)); }

		return res;
	}

	/**
	 * Sum of.
	 *
	 * @param scope
	 *            the scope
	 * @param container
	 *            the container
	 * @param filter
	 *            the filter
	 * @return the object
	 */
	@operator (
			value = { "sum_of" },
			type = ITypeProvider.TYPE_AT_INDEX + 3,
			expected_content_type = { IType.FLOAT, IType.POINT, IType.COLOR, IType.INT, IType.STRING },
			iterator = true,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER, IConcept.FILTER })
	@doc (
			value = "the sum of the right-hand expression evaluated on each of the elements of the left-hand operand",
			comment = "in the right-hand operand, the keyword each can be used to represent, in turn, each of the right-hand operand elements. ",
			usages = { @usage (
					value = "if the left-operand is a map, the keyword each will contain each value",
					examples = { @example (
							value = "[1::2, 3::4, 5::6] sum_of (each + 3)",
							equals = "21") }) },
			examples = { @example (
					value = "[1,2] sum_of (each * 100 )",
					equals = "300") },
			see = { "min_of", "max_of", "product_of", "mean_of" })
	@test ("[1,2] sum_of (each * 100 ) = 300")
	public static Object sum_of(final IScope scope, final String eachName, final IContainer container,
			final IExpression filter) {
		Stream s = stream(scope, container);
		IType t;
		if (filter != null) {
			s = s.map(with(scope, eachName, filter));
			t = filter.getGamlType();
		} else {
			t = container.getGamlType().getContentType();
		}
		s = s.map(each -> t.cast(scope, each, null, false));
		return switch (t.id()) {
			case IType.INT -> ((Stream<Integer>) s).reduce(0, Integer::sum);
			case IType.FLOAT -> ((Stream<Double>) s).reduce(0d, Double::sum);
			case IType.POINT -> ((Stream<GamaPoint>) s).reduce(new GamaPoint(), GamaPoint::plus);
			case IType.COLOR -> ((Stream<GamaColor>) s).reduce(GamaColor.get(0, 0, 0, 0), GamaColor::merge);
			case IType.STRING -> ((Stream<String>) s).reduce("", String::concat);
			default -> throw GamaRuntimeException.error("No sum can be computed for " + container.serializeToGaml(true),
					scope);
		};
	}

	/**
	 * Among.
	 *
	 * @param scope
	 *            the scope
	 * @param number
	 *            the number
	 * @param c
	 *            the c
	 * @return the i list
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "among",
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER, IConcept.FILTER })
	@doc (
			value = "Returns a list of length the value of the left-hand operand, containing random elements from the right-hand operand. As of GAMA 1.6, the order in which the elements are returned can be different than the order in which they appear in the right-hand container",
			special_cases = {
					"if the right-hand operand is empty, among returns a new empty list. If it is nil, it throws an error.",
					"if the left-hand operand is greater than the length of the right-hand operand, among returns the right-hand operand (converted as a list). If it is smaller or equal to zero, it returns an empty list" },
			examples = { @example (
					value = "3 among [1,2,4,3,5,7,6,8]",
					returnType = "list<int>",
					equals = "[1,2,8] (for example)",
					test = false),
					@example (
							value = "3 among g2",
							equals = "[node6,node11,node7]",
							isExecutable = false),
					@example (
							value = "3 among list(node)",
							equals = "[node1,node11,node4]",
							isExecutable = false),
					@example (
							value = "1 among [1::2,3::4]",
							returnType = "list<int>",
							equals = "2 or 4",
							test = false) })
	@no_test
	public static IList among(final IScope scope, final Integer number, final IContainer c)
			throws GamaRuntimeException {
		if (number <= 0) {
			if (number < 0) {
				GAMA.reportAndThrowIfNeeded(scope,
						GamaRuntimeException.warning("'among' expects a positive number (not " + number + ")", scope),
						false);
			}
			return listLike(c).get();
		}
		final IList l = notNull(scope, c).listValue(scope, c.getGamlType().getContentType(), false);
		final int size = l.size();
		if (number >= size) return l;
		final int[] indexes = new int[size];
		for (int i = 0; i < indexes.length; i++) { indexes[i] = i; }
		scope.getRandom().shuffleInPlace(indexes);
		final IList result = listLike(c).get();
		for (int i = 0; i < number; i++) { result.add(l.get(indexes[i])); }
		return result;
	}

	/**
	 * The Class ComparableValidator.
	 */
	public static class ComparableValidator implements IOperatorValidator {

		@Override
		public boolean validate(final IDescription context, final EObject emfContext, final IExpression... arguments) {
			final IExpression filter = arguments[2];
			if (!filter.getGamlType().isComparable()) {
				context.error(
						"Comparisons should operate on values that are comparable with each other (e.g. int, float, string, point, color, etc.)",
						IGamlIssue.UNMATCHED_TYPES, emfContext);
				return false;
			}
			return true;
		}

	}

	/**
	 * Sort.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param filter
	 *            the filter
	 * @return the i list
	 */
	@operator (
			value = { "sort_by", "sort" },
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
			iterator = true,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER })
	@doc (
			value = "Returns a list, containing the elements of the left-hand operand sorted in ascending order by the value of the right-hand operand when it is evaluated on them. ",
			comment = "the left-hand operand is casted to a list before applying the operator. In the right-hand operand, the keyword each can be used to represent, in turn, each of the elements.",
			special_cases = {
					"if the left-hand operand is nil, sort_by throws an error. If the sorting function returns values that cannot be compared, an error will be thrown as well" },
			examples = { @example (
					value = "[1,2,4,3,5,7,6,8] sort_by (each)",
					equals = "[1,2,3,4,5,6,7,8]"),
					@example (
							value = "graph g2 <- graph([]);",
							isTestOnly = true),
					@example (
							value = "g2 sort_by (length(g2 out_edges_of each) )",
							equals = "[node9, node7, node10, node8, node11, node6, node5, node4]",
							test = false),
					@example (
							value = "(list(node) sort_by (round(node(each).location.x))",
							equals = "[node5, node1, node0, node2, node3]",
							isExecutable = false),
					@example (
							value = "[1::2, 5::6, 3::4] sort_by (each)",
							equals = "[2, 4, 6]") },
			see = { "group_by" })
	@test ("[1,2,4,3,5,7,6,8] sort_by (each) = [1,2,3,4,5,6,7,8]")
	@validator (ComparableValidator.class)
	public static IList sort(final IScope scope, final String eachName, final IContainer c, final IExpression filter) {
		try {
			return (IList) stream(scope, c).sortedBy(with(scope, eachName, filter)).toCollection(listLike(c));
		} catch (IllegalArgumentException e) {
			// AD added here to avoid reporting concurrent modifications to the container while sorting it (can happen
			// when relaunching simulations, see #693, with this exception in java.util.TimSort). Instead we return the
			// sort on a copy of the container
			return sort(scope, eachName, c.listValue(scope, c.getGamlType().getContentType(), true), filter);
		}
	}

	/**
	 * Where.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param filter
	 *            the filter
	 * @return the i list
	 */
	@operator (
			value = { "where", "select" },
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
			iterator = true,
			expected_content_type = IType.BOOL,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER, IConcept.FILTER })
	@doc (
			masterDoc = true,
			value = "a list containing all the elements of the left-hand operand that make the right-hand operand evaluate to true. ",
			comment = "in the right-hand operand, the keyword each can be used to represent, in turn, each of the right-hand operand elements. ",
			usages = { @usage (
					value = "if the left-hand operand is nil, where throws an error"),
					@usage (
							value = "if the left-operand is a map, the keyword each will contain each value",
							examples = { @example (
									value = "[1::2, 3::4, 5::6] where (each >= 4)",
									equals = "[4, 6]") 
							}),
					@usage (
							value = "if the left-operand is a matrix, the elements will be traversed and filtered row by row.",
							examples = { @example (
									value = "matrix([1, 2, 3], [4, 5, 6]) where (each > 2)",
									equals = "[4, 5, 3, 6]") 
					}),
					@usage (
							value = "if the right-operand is not a bool, it will be casted into a bool. For numbers, 0 will be interpreted as false and the rest as true.",
							examples = { @example (
									value = "[-2.000001,-2,-1,0,0.0,1,2,3,4,5,6.5] select each",
									equals = "[-2.000001,-2,-1,1,2,3,4,5,6.5]") 
					})
			},
			examples = { @example (
					value = "[1,2,3,4,5,6,7,8] where (each > 3)",
					equals = "[4, 5, 6, 7, 8] "),
					@example (
							value = "graph g2 <- graph([]);",
							isTestOnly = true),
					@example (
							value = "g2 where (length(g2 out_edges_of each) = 0 )",
							equals = "[node9, node7, node10, node8, node11]",
							test = false),
					@example (
							value = "(list(node) where (round(node(each).location.x) > 32)",
							equals = "[node2, node3]",
							isExecutable = false) },
			see = { "first_with", "last_with" })
	@test ("[1,2,3,4,5,6,7,8] where (each > 3) = [4, 5, 6, 7, 8] ")
	@test ("matrix([1, 2, 3], [4, 5, 6]) where (each > 2) = [4, 5, 3, 6] ")
	@test ("[-2.000001,-2,-1,0,0.0,1,2,3,4,5,6.5] select each = [-2.000001,-2,-1,1,2,3,4,5,6.5]")
	public static IList where(final IScope scope, final String eachName, final IContainer c, final IExpression filter) {
		return (IList) stream(scope, c).filter(by(scope, eachName, filter)).toCollection(listLike(c));
	}

	/**
	 * Where. Optimization for a very common case (IList)
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param filter
	 *            the filter
	 * @return the i list
	 */
	@operator (
			value = { "where", "select" },
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
			iterator = true,
			expected_content_type = IType.BOOL,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER, IConcept.FILTER })
	@doc (
			value = "Returns a list contaning only the elements that make the predicate return true")
	@test ("[1,2,3,4,5,6,7,8] where (each != 2) = [1, 3, 4, 5, 6, 7, 8] ")
	public static IList where(final IScope scope, final String eachName, final IList c, final IExpression filter) {
		return where(scope, c.iterable(scope), c.getGamlType().getContentType(), eachName, filter);
	}

	/**
	 * Where.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param contentType
	 *            the content type
	 * @param filter
	 *            the filter
	 * @return the i list
	 */
	private static IList where(final IScope scope, final Iterable c, final IType contentType, final String eachName,
			final IExpression filter) {
		final IList result = GamaListFactory.create(contentType);
		for (final Object o : c) {
			scope.setEach(eachName, o);
			if (Cast.asBool(scope, filter.value(scope))) { result.add(o); }
		}
		scope.setEach(eachName, null);
		return result;
	}

	/**
	 * Where.Optimization for a very common case (ISpecies)
	 *
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param filter
	 *            the filter
	 * @return the i list
	 */
	@operator (
			value = { "where", "select" },
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
			iterator = true,
			expected_content_type = IType.BOOL,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER, IConcept.FILTER })
	@doc (
			value = "Returns a list containing only the agents of this species that make the predicate return true")
	@no_test
	public static IList where(final IScope scope, final String eachName, final ISpecies c, final IExpression filter) {
		return where(scope, c.iterable(scope), c.getGamlType().getContentType(), eachName, filter);
	}

	/**
	 * With max of.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param filter
	 *            the filter
	 * @return the object
	 */
	@operator (
			value = { "with_max_of" },
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
			iterator = true,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER, IConcept.FILTER })
	@doc (
			value = "one of elements of the left-hand operand that maximizes the value of the right-hand operand",
			comment = "in the right-hand operand, the keyword each can be used to represent, in turn, each of the right-hand operand elements. ",
			usages = { @usage (
					value = "if the left-hand operand is nil, with_max_of returns the default value of the right-hand operand") },
			examples = { @example (
					value = "[1,2,3,4,5,6,7,8] with_max_of (each )",
					equals = "8"),
					@example (
							value = "graph g2 <- graph([]);",
							isTestOnly = true),
					@example (
							value = "g2 with_max_of (length(g2 out_edges_of each)  ) ",
							equals = "node4",
							test = false),
					@example (
							value = "(list(node) with_max_of (round(node(each).location.x))",
							equals = "node3",
							isExecutable = false),
					@example (
							value = "[1::2, 3::4, 5::6] with_max_of (each)",
							equals = "6") },
			see = { "where", "with_min_of" })
	@test ("[1,2,3,4,5,6,7,8] with_max_of (each ) = 8")
	@validator (ComparableValidator.class)
	public static Object with_max_of(final IScope scope, final String eachName, final IContainer c,
			final IExpression filter) {
		return stream(scope, c).maxBy(with(scope, eachName, filter)).orElse(null);
	}

	/**
	 * With min of.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param filter
	 *            the filter
	 * @return the object
	 */
	@operator (
			value = { "with_min_of" },
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
			iterator = true,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER, IConcept.FILTER })
	@doc (
			value = "one of elements of the left-hand operand that minimizes the value of the right-hand operand",
			comment = "in the right-hand operand, the keyword each can be used to represent, in turn, each of the right-hand operand elements. ",
			usages = { @usage (
					value = "if the left-hand operand is nil, with_max_of returns the default value of the right-hand operand") },
			examples = { @example (
					value = "[1,2,3,4,5,6,7,8] with_min_of (each )",
					equals = "1"),
					@example (
							value = "graph g2 <- graph([]);",
							isTestOnly = true),
					@example (
							value = "g2 with_min_of (length(g2 out_edges_of each)  )",
							equals = "node11",
							test = false),
					@example (
							value = "(list(node) with_min_of (round(node(each).location.x))",
							equals = "node0",
							isExecutable = false),
					@example (
							value = "[1::2, 3::4, 5::6] with_min_of (each)",
							equals = "2") },
			see = { "where", "with_max_of" })
	@test ("[1,2,3,4,5,6,7,8] with_min_of (each )  = 1")
	@validator (ComparableValidator.class)
	public static Object with_min_of(final IScope scope, final String eachName, final IContainer c,
			final IExpression filter) {
		return stream(scope, c).minBy(with(scope, eachName, filter)).orElse(null);
	}

	/**
	 * Accumulate.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param filter
	 *            the filter
	 * @return the i list
	 */
	@operator (
			value = { "accumulate" },
			content_type = ITypeProvider.THIRD_CONTENT_TYPE_OR_TYPE,
			iterator = true,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns a new flat list, in which each element is the evaluation of the right-hand operand. If this evaluation returns a list, the elements of this result are added directly to the list returned",
			comment = "accumulate is dedicated to the application of a same computation on each element of a container (and returns a list). "
					+ "In the right-hand operand, the keyword each can be used to represent, in turn, each of the left-hand operand elements. ",
			examples = { @example (
					value = "[a1,a2,a3] accumulate (each neighbors_at 10)",
					equals = "a flat list of all the neighbors of these three agents",
					isExecutable = false),
					@example (
							value = "[1,2,4] accumulate ([2,4])",
							returnType = "list<int>",
							equals = "[2,4,2,4,2,4]"),
					@example (
							value = "[1,2,4] accumulate (each * 2)",
							returnType = "list<int>",
							equals = "[2,4,8]") },
			see = { "collect" })
	@test ("[1,2,4] accumulate ([2,4]) = [2,4,2,4,2,4]")
	public static IList accumulate(final IScope scope, final String eachName, final IContainer c,
			final IExpression filter) {
		// WARNING TODO The resulting type is not computed
		final IType type = filter.getGamlType();
		IType resultingContentsType = type;
		if (resultingContentsType.isContainer()) { resultingContentsType = resultingContentsType.getContentType(); }
		return (IList) stream(scope, c).flatCollection(with(scope, eachName, filter).andThen(toLists))
				.toCollection(listOf(resultingContentsType));

	}
	
	
	@operator (
			value = { "collect" },
			content_type = ITypeProvider.TYPE_AT_INDEX + 3,
			iterator = true,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.MATRIX })
	@doc (
			value = "When applied to a field, collect returns a field of the same size, in which each element is the evaluation of the right-hand operand on the corresponding element in the left-hand operand")
	@test ("field([1,2,4],[1,3,4]) collect (x: x *2) = field([2,4,8],[2,6,8])")
	public static GamaField collect(final IScope scope, final String eachName, final GamaField f, final IExpression filter) {
		return (GamaField) collect(scope, eachName, (IMatrix)f, filter);
	}

	/**
	 * Collect.
	 *
	 * @param scope
	 *            the scope
	 * @param c
	 *            the c
	 * @param filter
	 *            the filter
	 * @return the i list
	 */
	@operator (
			value = { "collect" },
			content_type = ITypeProvider.TYPE_AT_INDEX + 3,
			iterator = true,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.MATRIX })
	@doc (
			value = "When applied to a matrix, collect returns a matrix of the same size, in which each element is the evaluation of the right-hand operand on the corresponding element in the left-hand operand")
	@test ("matrix([1,2,4],[1,3,4]) collect (x: x *2) = matrix([2,4,8],[2,6,8])")
	public static IMatrix collect(final IScope scope, final String eachName, final IMatrix c, final IExpression filter) {
		int cols = c.getCols(scope);
		int rows = c.getRows(scope);
		int type = filter.getGamlType().id();
		IMatrix result = switch (type) {
			case IType.FLOAT -> c.getGamlType() == Types.FIELD  ? GamaFieldType.buildField(scope, cols, rows) : new GamaFloatMatrix(cols, rows);
			case IType.INT -> new GamaIntMatrix(cols, rows);
			default -> new GamaObjectMatrix(cols, rows, filter.getGamlType());
		};

		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				scope.setEach(eachName, c.get(scope, x, y));
				result.set(scope, x, y, filter.value(scope));
			}
		}
		return result;
	}

	/**
	 * Collect.
	 *
	 * @param scope
	 *            the scope
	 * @param eachName
	 *            the each name
	 * @param c
	 *            the c
	 * @param filter
	 *            the filter
	 * @return the i list
	 */
	@operator (
			value = { "collect" },
			content_type = ITypeProvider.TYPE_AT_INDEX + 3,
			iterator = true,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns a new list, in which each element is the evaluation of the right-hand operand.",
			comment = "collect is similar to accumulate except that accumulate always produces flat lists if the right-hand operand returns a list."
					+ "In addition, collect can be applied to any container.",
			usages = { @usage ("if the left-hand operand is nil, collect throws an error") },
			examples = { @example (
					value = "[1,2,4] collect (each *2)",
					equals = "[2,4,8]"),
					@example (
							value = "[1,2,4] collect ([2,4])",
							equals = "[[2,4],[2,4],[2,4]]"),
					@example (
							value = "[1::2, 3::4, 5::6] collect (each + 2)",
							equals = "[4,6,8]"),
					@example (
							value = "(list(node) collect (node(each).location.x * 2)",
							equals = "the list of nodes with their x multiplied by 2",
							isExecutable = false) },
			see = { "accumulate" })
	@test ("[1,2,4] collect (each *2) = [2,4,8]")
	@test ("[1,2,4] collect ([2,4]) = [[2,4],[2,4],[2,4]]")
	@test ("[1,2,3] collect (i: i+1) = [2,3,4]")
	@test ("[1,2] collect (i: ([1,2,3] collect (j: i+j))) = [[2,3,4],[3,4,5]]")
	public static IList collect(final IScope scope, final String eachName, final IContainer c,
			final IExpression filter) {
		return (IList) stream(scope, c).map(with(scope, eachName, filter)).toCollection(listOf(filter.getGamlType()));
	}

	/**
	 * Interleave.
	 *
	 * @param scope
	 *            the scope
	 * @param cc
	 *            the cc
	 * @return the i list
	 */
	@operator (
			value = { "interleave" },
			content_type = ITypeProvider.FIRST_ELEMENT_CONTENT_TYPE,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER })
	@doc (
			value = "Returns a new list containing the interleaved elements of the containers contained in the operand",
			comment = "the operand should be a list of lists of elements. The result is a list of elements. ",
			examples = { @example (
					value = "interleave([1,2,4,3,5,7,6,8])",
					equals = "[1,2,4,3,5,7,6,8]"),
					@example (
							value = "interleave([['e11','e12','e13'],['e21','e22','e23'],['e31','e32','e33']])",
							equals = "['e11','e21','e31','e12','e22','e32','e13','e23','e33']") })
	public static IList interleave(final IScope scope, final IContainer cc) {
		final Iterable iterable = notNull(scope, cc).iterable(scope);
		IType type = cc.getGamlType().getContentType();
		if (type.isContainer()) { type = type.getContentType(); }
		final Iterator it = new InterleavingIterator(scope, Iterables.toArray(iterable, Object.class));
		return GamaListFactory.create(scope, type, it);
	}

	/**
	 * Count.
	 *
	 * @param scope
	 *            the scope
	 * @param original
	 *            the original
	 * @param filter
	 *            the filter
	 * @return the integer
	 */
	@operator (
			value = { "count" },
			iterator = true,
			expected_content_type = IType.BOOL,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns an int, equal to the number of elements of the left-hand operand that make the right-hand operand evaluate to true.",
			comment = "in the right-hand operand, the keyword each can be used to represent, in turn, each of the elements.",
			usages = { @usage ("if the left-hand operand is nil, count throws an error") },
			examples = { @example (
					value = "[1,2,3,4,5,6,7,8] count (each > 3)",
					equals = "5"),
					@example (
							value = "// Number of nodes of graph g2 without any out edge"),
					@example (
							value = "graph g2 <- graph([]);"),
					@example (
							value = "g2 count (length(g2 out_edges_of each) = 0  ) ",
							equals = "the total number of out edges",
							test = false),
					@example (
							value = "// Number of agents node with x > 32"),
					@example (
							value = "int n <- (list(node) count (round(node(each).location.x) > 32);",
							isExecutable = false),
					@example (
							value = "[1::2, 3::4, 5::6] count (each > 4)",
							equals = "1") },
			see = { "group_by" })
	public static Integer count(final IScope scope, final String eachName, final IContainer original,
			final IExpression filter) {
		return (int) notNull(scope, original).stream(scope).filter(by(scope, eachName, filter)).count();
	}

	/**
	 * One matches.
	 *
	 * @param scope
	 *            the scope
	 * @param original
	 *            the original
	 * @param filter
	 *            the filter
	 * @return the boolean
	 */
	@operator (
			value = { "one_matches", "one_verifies" },
			iterator = true,
			expected_content_type = IType.BOOL,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER })
	@doc (
			value = "Returns true if at least one of the elements of the left-hand operand make the right-hand operand evaluate to true.  Returns false if the left-hand operand is empty. 'c one_matches each.property' is strictly equivalent to '(c count each.property) > 0' but faster in most cases (as it is a shortcircuited operator) ",
			comment = "in the right-hand operand, the keyword each can be used to represent, in turn, each of the elements.",
			usages = { @usage ("if the left-hand operand is nil, one_matches throws an error") },
			examples = { @example (
					value = "[1,2,3,4,5,6,7,8] one_matches (each > 3)",
					equals = "true"),
					@example (
							value = "[1::2, 3::4, 5::6] one_matches (each > 4)",
							equals = "true") },
			see = { "none_matches", "all_match", "count" })
	public static Boolean one_matches(final IScope scope, final String eachName, final IContainer original,
			final IExpression filter) {
		return notNull(scope, original).stream(scope).anyMatch(by(scope, eachName, filter));
	}

	/**
	 * None matches.
	 *
	 * @param scope
	 *            the scope
	 * @param original
	 *            the original
	 * @param filter
	 *            the filter
	 * @return the boolean
	 */
	@operator (
			value = { "none_matches", "none_verifies" },
			iterator = true,
			expected_content_type = IType.BOOL,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER })
	@doc (
			value = "Returns true if none of the elements of the left-hand operand make the right-hand operand evaluate to true. 'c none_matches each.property' is strictly equivalent to '(c count each.property) = 0'",
			comment = "In the right-hand operand, the keyword each can be used to represent, in turn, each of the elements.",
			usages = { @usage ("If the left-hand operand is nil, none_matches throws an error."),
					@usage ("If the left-hand operand is empty, none_matches returns true.") },
			examples = { @example (
					value = "[1,2,3,4,5,6,7,8] none_matches (each > 3)",
					equals = "false"),
					@example (
							value = "[1::2, 3::4, 5::6] none_matches (each > 4)",
							equals = "false") },
			see = { "one_matches", "all_match", "count" })
	public static Boolean none_matches(final IScope scope, final String eachName, final IContainer original,
			final IExpression filter) {
		return notNull(scope, original).stream(scope).noneMatch(by(scope, eachName, filter));
	}

	/**
	 * All match.
	 *
	 * @param scope
	 *            the scope
	 * @param original
	 *            the original
	 * @param filter
	 *            the filter
	 * @return the boolean
	 */
	@operator (
			value = { "all_match", "all_verify" },
			iterator = true,
			expected_content_type = IType.BOOL,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER })
	@doc (
			value = "Returns true if all the elements of the left-hand operand make the right-hand operand evaluate to true. Returns true if the left-hand operand is empty. 'c all_match each.property' is strictly equivalent to '(c count each.property)  = length(c)' but faster in most cases (as it is a shortcircuited operator)",
			comment = "in the right-hand operand, the keyword each can be used to represent, in turn, each of the elements.",
			usages = { @usage ("if the left-hand operand is nil, all_match throws an error"),
					@usage ("if the left-hand operand is empty, all_match returns true") },
			examples = { @example (
					value = "[1,2,3,4,5,6,7,8] all_match (each > 3)",
					equals = "false"),
					@example (
							value = "[1::2, 3::4, 5::6] all_match (each > 4)",
							equals = "false") },
			see = { "none_matches", "one_matches", "count" })
	public static Boolean all_match(final IScope scope, final String eachName, final IContainer original,
			final IExpression filter) {
		return notNull(scope, original).stream(scope).allMatch(by(scope, eachName, filter));
	}

	/**
	 * Index by.
	 *
	 * @param scope
	 *            the scope
	 * @param original
	 *            the original
	 * @param keyProvider
	 *            the key provider
	 * @return the i map
	 */
	@operator (
			value = { "index_by" },
			iterator = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
			index_type = ITypeProvider.TYPE_AT_INDEX + 3,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER })
	@doc (
			value = "produces a new map from the evaluation of the right-hand operand for each element of the left-hand operand",
			usages = {
					@usage ("if the left-hand operand is nil, index_by throws an error. If the operation results in duplicate keys, only the first value corresponding to the key is kept") },
			examples = { @example (
					value = "[1,2,3,4,5,6,7,8] index_by (each - 1)",
					equals = "[0::1, 1::2, 2::3, 3::4, 4::5, 5::6, 6::7, 7::8]") },
			see = {})
	public static IMap index_by(final IScope scope, final String eachName, final IContainer original,
			final IExpression keyProvider) {

		final StreamEx s = original.stream(scope);
		final IType contentsType = original.getGamlType().getContentType();
		return (IMap) s.collect(Collectors.toMap(with(scope, eachName, keyProvider), a -> a, (a, b) -> a,
				asMapOf(keyProvider.getGamlType(), contentsType)));
	}

	/**
	 * As map.
	 *
	 * @param scope
	 *            the scope
	 * @param original
	 *            the original
	 * @param filter
	 *            the filter
	 * @return the i map
	 */
	@operator (
			value = { "as_map" },
			iterator = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 3,
			index_type = ITypeProvider.KEY_TYPE_AT_INDEX + 3,
			expected_content_type = IType.PAIR,
			category = IOperatorCategory.MAP,
			concept = { IConcept.CONTAINER, IConcept.MAP })
	@doc (
			value = "produces a new map from the evaluation of the right-hand operand for each element of the left-hand operand",
			comment = "the right-hand operand should be a pair",
			usages = { @usage ("if the left-hand operand is nil, as_map throws an error.") },
			examples = { @example (
					value = "[1,2,3,4,5,6,7,8] as_map (each::(each * 2))",
					returnType = "map<int,int>",
					equals = "[1::2, 2::4, 3::6, 4::8, 5::10, 6::12, 7::14, 8::16]"),
					@example (
							value = "[1::2,3::4,5::6] as_map (each::(each * 2))",
							returnType = "map<int,int>",
							equals = "[2::4, 4::8, 6::12] ") },
			see = {})
	public static IMap as_map(final IScope scope, final String eachName, final IContainer original,
			final IExpression filter) {
		if (!(filter instanceof BinaryOperator pair) || !"::".equals(pair.getName()))
			throw GamaRuntimeException.error("'as_map' expects a pair as second argument", scope);
		final IExpression key = pair.arg(0);
		final IExpression value = pair.arg(1);
		return (IMap) stream(scope, original).collect(Collectors.toMap(with(scope, eachName, key),
				with(scope, eachName, value), (a, b) -> a, asMapOf(key.getGamlType(), value.getGamlType())));
	}

	/**
	 * Creates the map.
	 *
	 * @param scope
	 *            the scope
	 * @param keys
	 *            the keys
	 * @param values
	 *            the values
	 * @return the i map
	 */
	@operator (
			value = { "create_map" },
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
			index_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = IOperatorCategory.MAP,
			expected_content_type = ITypeProvider.BOTH,
			concept = { IConcept.CONTAINER, IConcept.MAP })
	@doc (
			value = "returns a new map using the left operand as keys for the right operand",
			usages = { @usage ("if the left operand contains duplicates, create_map throws an error."),
					@usage ("if both operands have different lengths, choose the minimum length between the two operands"
							+ "for the size of the map") },
			examples = { @example (
					value = "create_map([0,1,2],['a','b','c'])",
					returnType = "map<int,string>",
					equals = "[0::'a',1::'b',2::'c']"),
					@example (
							value = "create_map([0,1],[0.1,0.2,0.3])",
							returnType = "map<int,float>",
							equals = "[0::0.1,1::0.2]"),
					@example (
							value = "create_map(['a','b','c','d'],[1.0,2.0,3.0])",
							returnType = "map<string,float>",
							equals = "['a'::1.0,'b'::2.0,'c'::3.0]") },
			see = {})
	public static IMap create_map(final IScope scope, final IList keys, final IList values) {
		if (keys.length(scope) != values.length(scope)) {
			GAMA.reportAndThrowIfNeeded(scope,
					GamaRuntimeException.warning("'create_map' expects two lists of the same length", scope), false);
		}
		// final HashSet newSet = new HashSet(keys);
		// if (newSet.size() < keys.length(scope))
		// throw GamaRuntimeException.error("'create_map' expects unique values in the keys list", scope);
		return GamaMapFactory.create(scope, keys.getGamlType(), values.getGamlType(), keys, values);
	}

	/**
	 * Plus.
	 *
	 * @param scope
	 *            the scope
	 * @param m1
	 *            the m 1
	 * @param m2
	 *            the m 2
	 * @return the i map
	 */
	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			type = ITypeProvider.BOTH,
			content_type = ITypeProvider.BOTH,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns a new map containing all the elements of both operands",
			examples = { @example (
					value = "['a'::1,'b'::2] + ['c'::3]",
					equals = "['a'::1,'b'::2,'c'::3]"),
					@example (
							value = "['a'::1,'b'::2] + [5::3.0]",
							equals = "['a'::1,'b'::2,5::3.0]") },
			see = { "" + IKeyword.MINUS })
	public static IMap plus(final IScope scope, final IMap m1, final IMap m2) {
		final IType type = GamaType.findCommonType(notNull(scope, m1).getGamlType(), notNull(scope, m2).getGamlType());
		final IMap res = GamaMapFactory.createWithoutCasting(type.getKeyType(), type.getContentType(), m1);
		res.putAll(m2);
		return res;
	}

	/**
	 * Plus.
	 *
	 * @param scope
	 *            the scope
	 * @param m1
	 *            the m 1
	 * @param m2
	 *            the m 2
	 * @return the i map
	 */
	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			type = ITypeProvider.TYPE_AT_INDEX + 1,
			content_type = ITypeProvider.BOTH,
			category = IOperatorCategory.CONTAINER,
			concept = { IConcept.CONTAINER })
	@doc (
			value = "returns a new map containing all the elements of both operands",
			examples = { @example (
					value = "['a'::1,'b'::2] + ('c'::3)",
					equals = "['a'::1,'b'::2,'c'::3]"),
					@example (
							value = "['a'::1,'b'::2] + ('c'::3)",
							equals = "['a'::1,'b'::2,'c'::3]") },
			see = { "" + IKeyword.MINUS })
	public static IMap plus(final IScope scope, final IMap m1, final GamaPair m2) {
		final IType type = GamaType.findCommonType(notNull(scope, m1).getGamlType(), notNull(scope, m2).getGamlType());
		final IMap res = GamaMapFactory.createWithoutCasting(type.getKeyType(), type.getContentType(), m1);
		res.put(m2.key, m2.value);
		return res;
	}

	/**
	 * Minus.
	 *
	 * @param scope
	 *            the scope
	 * @param m1
	 *            the m 1
	 * @param m2
	 *            the m 2
	 * @return the i map
	 */
	@operator (
			value = IKeyword.MINUS,
			can_be_const = true,
			type = ITypeProvider.BOTH,
			content_type = ITypeProvider.BOTH,
			category = IOperatorCategory.CONTAINER,
			concept = {})
	@doc (
			value = "returns a new map containing all the elements of the first operand not present in the second operand",
			examples = { @example (
					value = "['a'::1,'b'::2] - ['b'::2]",
					equals = "['a'::1]"),
					@example (
							value = "['a'::1,'b'::2] - ['b'::2,'c'::3]",
							equals = "['a'::1]") },
			see = { "" + IKeyword.MINUS })
	public static IMap minus(final IScope scope, final IMap m1, final IMap m2) {
		final IMap res = notNull(scope, m1).copy(scope);
		res.removeValues(scope, m2);
		return res;
	}

	/**
	 * Minus.
	 *
	 * @param scope
	 *            the scope
	 * @param m1
	 *            the m 1
	 * @param m2
	 *            the m 2
	 * @return the i map
	 */
	@operator (
			value = IKeyword.MINUS,
			can_be_const = true,
			type = ITypeProvider.TYPE_AT_INDEX + 1,
			content_type = ITypeProvider.BOTH,
			category = IOperatorCategory.CONTAINER,
			concept = {})
	@doc (
			value = "returns a new map containing all the elements of the first operand without the one of the second operand",
			examples = { @example (
					value = "['a'::1,'b'::2] - ('b'::2)",
					equals = "['a'::1]"),
					@example (
							value = "['a'::1,'b'::2] - ('c'::3)",
							equals = "['a'::1,'b'::2]") },
			see = { "" + IKeyword.MINUS })
	public static IMap minus(final IScope scope, final IMap m1, final GamaPair m2) {
		final IMap res = notNull(scope, m1).copy(scope);
		res.remove(m2.getKey());
		return res;
	}

	/**
	 * Mean.
	 *
	 * @param scope
	 *            the scope
	 * @param l
	 *            the l
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "mean",
			can_be_const = true,
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1 + ITypeProvider.FLOAT_IN_CASE_OF_INT,
			expected_content_type = { IType.INT, IType.FLOAT, IType.POINT, IType.COLOR },
			category = { IOperatorCategory.STATISTICAL, IOperatorCategory.CONTAINER, IOperatorCategory.COLOR },
			concept = { IConcept.STATISTIC, IConcept.COLOR })
	@doc (
			value = "the mean of all the elements of the operand",
			comment = "the elements of the operand are summed (see sum for more details about the sum of container elements ) and then the sum value is divided by the number of elements.",
			special_cases = {
					"if the container contains points, the result will be a point. If the container contains rgb values, the result will be a rgb color" },
			examples = { @example (
					value = "mean ([4.5, 3.5, 5.5, 7.0])",
					equals = "5.125 ") },
			see = { "sum" })
	@test ("mean ([4.5, 3.5, 5.5, 7.0]) with_precision 3 = 5.125")
	public static Object opMean(final IScope scope, final IContainer l) throws GamaRuntimeException {

		final Object s = sum(scope, l);
		int size = l.length(scope);
		if (size == 0) { size = 1; }
		if (s instanceof Number) return ((Number) s).doubleValue() / size;
		if (s instanceof GamaPoint) return Points.divide(scope, (GamaPoint) s, size);
		if (s instanceof GamaColor) return Colors.divide((GamaColor) s, size);
		return Cast.asFloat(scope, s) / size;
	}

}
