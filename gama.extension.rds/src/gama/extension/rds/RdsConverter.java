/*******************************************************************************************************
 *
 * RdsConverter.java, in gama.extension.rds, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.rds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.LogicalArrayVector;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Vector;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.types.matrix.GamaMatrixFactory;
import gama.api.types.matrix.IMatrix;
import gama.extension.dataframe.GamaDataFrameFactory;
import gama.extension.dataframe.IDataFrame;

/** Explicit, loss-aware mappings between R values and GAMA containers. */
public final class RdsConverter {

	private RdsConverter() {}

	public static Object fromR(final IScope scope, final SEXP value) {
		if (value == Null.INSTANCE) return null;
		if (value.inherits("data.frame") && value instanceof ListVector frame) {
			checkAttributes(scope, value, Set.of("names", "class", "row.names"));
			checkClasses(scope, value, Set.of("data.frame"));
			return dataframe(scope, frame);
		}
		if (value.inherits("factor")) {
			checkAttributes(scope, value, Set.of("class", "levels"));
			checkClasses(scope, value, Set.of("factor", "ordered"));
			return factor(scope, value);
		}
		if (value.getAttribute(Symbol.get("class")) != Null.INSTANCE)
			throw error(scope, "Unsupported R class " + value.getS3Class());
		final SEXP dimensions = value.getAttribute(Symbol.get("dim"));
		if (dimensions != Null.INSTANCE) {
			checkAttributes(scope, value, Set.of("dim", "dimnames"));
			return matrix(scope, value, dimensions);
		}
		if (value instanceof ListVector list) {
			checkAttributes(scope, value, Set.of("names"));
			if (list.getAttribute(Symbol.get("names")) != Null.INSTANCE) {
				final IMap<String, Object> result = GamaMapFactory.create(Types.STRING, Types.NO_TYPE);
				for (int i = 0; i < list.length(); i++) {
					final String name = list.getName(i);
					if (name == null || name.isEmpty() || result.containsKey(name))
						throw error(scope, "R named lists require unique, non-empty names");
					result.put(name, fromR(scope, list.getElementAsSEXP(i)));
				}
				return result;
			}
			final IList<Object> result = GamaListFactory.create();
			for (int i = 0; i < list.length(); i++) { result.add(fromR(scope, list.getElementAsSEXP(i))); }
			return result;
		}
		if (value instanceof Vector vector) {
			if (vector.getAttribute(Symbol.get("names")) != Null.INSTANCE)
				throw error(scope, "Named atomic vectors are not supported");
			checkAttributes(scope, value, Set.of());
			final IList<Object> result = GamaListFactory.create(vectorType(scope, vector));
			for (int i = 0; i < vector.length(); i++) { result.add(element(scope, vector, i)); }
			return result;
		}
		throw error(scope, "Unsupported R value: " + value.getTypeName());
	}

	private static void checkAttributes(final IScope scope, final SEXP value, final Set<String> allowed) {
		for (final Symbol attribute : value.getAttributes().names()) {
			if (!allowed.contains(attribute.getPrintName()))
				throw error(scope, "Unsupported R attribute '" + attribute.getPrintName() + "'");
		}
	}

	private static void checkClasses(final IScope scope, final SEXP value, final Set<String> allowed) {
		for (final String name : value.getS3Class()) {
			if (!allowed.contains(name)) throw error(scope, "Unsupported R class " + name);
		}
	}

	private static IType<?> vectorType(final IScope scope, final Vector vector) {
		if (vector instanceof LogicalVector) return Types.BOOL;
		if (vector instanceof IntVector) return Types.INT;
		if (vector instanceof DoubleVector) return Types.FLOAT;
		if (vector instanceof StringVector) return Types.STRING;
		throw error(scope, "Unsupported R vector: " + vector.getTypeName());
	}

	private static Object element(final IScope scope, final Vector vector, final int index) {
		vectorType(scope, vector);
		if (vector.isElementNA(index)) return null;
		if (vector instanceof LogicalVector) return vector.isElementTrue(index);
		if (vector instanceof IntVector) return vector.getElementAsInt(index);
		if (vector instanceof DoubleVector) return vector.getElementAsDouble(index);
		return vector.getElementAsString(index);
	}

	private static IList<String> factor(final IScope scope, final SEXP value) {
		if (!(value instanceof IntVector codes)
				|| !(value.getAttribute(Symbol.get("levels")) instanceof StringVector levels))
			throw error(scope, "Invalid R factor");
		final IList<String> result = GamaListFactory.create(Types.STRING);
		for (int i = 0; i < codes.length(); i++) {
			if (codes.isElementNA(i)) {
				result.add(null);
			} else {
				final int code = codes.getElementAsInt(i) - 1;
				if (code < 0 || code >= levels.length()) throw error(scope, "Invalid R factor level index");
				result.add(levels.getElementAsString(code));
			}
		}
		return result;
	}

	private static IMatrix<?> matrix(final IScope scope, final SEXP value, final SEXP dimensions) {
		if (!(dimensions instanceof IntVector dim) || dim.length() != 2 || !(value instanceof Vector vector))
			throw error(scope, "Only two-dimensional R matrices are supported");
		if (value.getAttribute(Symbol.get("dimnames")) != Null.INSTANCE)
			throw error(scope, "Matrix dimension names are not supported");
		final int rows = dim.getElementAsInt(0), cols = dim.getElementAsInt(1);
		if (rows < 0 || cols < 0 || (long) rows * cols != vector.length())
			throw error(scope, "Invalid R matrix dimensions");
		if ((rows == 0) != (cols == 0))
			throw error(scope, "GAMA cannot preserve an empty matrix with a non-zero dimension");
		final IType<?> type = vectorType(scope, vector);
		// Primitive GAMA matrices cannot retain nil cells.
		final IMatrix<?> result = vector.anyNA()
				? GamaMatrixFactory.createObjectMatrix(cols, rows, Types.NO_TYPE)
				: GamaMatrixFactory.create(cols, rows, type);
		for (int c = 0; c < cols; c++) {
			for (int r = 0; r < rows; r++) { result.set(scope, c, r, element(scope, vector, c * rows + r)); }
		}
		return result;
	}

	private static IDataFrame dataframe(final IScope scope, final ListVector frame) {
		final IList<String> names = GamaListFactory.create(Types.STRING);
		final IList<IList<?>> columns = GamaListFactory.create();
		final Set<String> seen = new HashSet<>();
		int rows = -1;
		for (int c = 0; c < frame.length(); c++) {
			final String name = frame.getName(c);
			names.add(name);
			if (name == null || name.isEmpty() || !seen.add(name))
				throw error(scope, "Dataframe columns require unique, non-empty names");
			final Object converted = fromR(scope, frame.getElementAsSEXP(c));
			if (!(converted instanceof IList<?> values)) throw error(scope, "Unsupported dataframe column " + name);
			if (rows != -1 && rows != values.size()) throw error(scope, "R dataframe columns have different lengths");
			rows = values.size();
			columns.add(values);
		}
		if (frame.length() == 0) {
			final SEXP rowNames = frame.getAttribute(Symbol.get("row.names"));
			if (rowNames.length() != 0) throw error(scope, "A dataframe with rows but no columns is not supported");
			return GamaDataFrameFactory.create();
		}
		return GamaDataFrameFactory.mutable(GamaDataFrameFactory.createFromColumns(scope, names, columns));
	}

	public static SEXP toR(final IScope scope, final Object value) {
		return new Exporter(scope).convert(value);
	}

	private static final class Exporter {
		private final IScope scope;
		private final Set<Object> ancestors = Collections.newSetFromMap(new IdentityHashMap<>());

		Exporter(final IScope scope) { this.scope = scope; }

		SEXP convert(final Object value) {
			if (value == null) return Null.INSTANCE;
			if (!ancestors.add(value)) throw error(scope, "Cyclic containers cannot be exported to RDS");
			try {
				if (value instanceof IDataFrame frame) {
					final ListVector.NamedBuilder builder = new ListVector.NamedBuilder();
					for (final String name : frame.getColumns()) {
						builder.add(name, vector(frame.getColumnValues(name), frame.getColumnTypes()
								.get(frame.getColumns().indexOf(name))));
					}
					return withRows(builder.build(), frame.getRows());
				}
				if (value instanceof IMatrix<?> matrix) {
					final int rows = matrix.getRows(scope), cols = matrix.getCols(scope);
					final List<Object> cells = new ArrayList<>();
					for (int c = 0; c < cols; c++) {
						for (int r = 0; r < rows; r++) { cells.add(matrix.get(scope, c, r)); }
					}
					final IType<?> declared = matrix.getGamlType().getContentType();
					final SEXP vector = vector(cells, declared == Types.NO_TYPE && cells.stream().allMatch(v -> v == null)
							? Types.FLOAT : declared);
					if (vector instanceof ListVector) throw error(scope, "Mixed/list R matrices are not supported");
					return vector.setAttribute("dim", new IntArrayVector(rows, cols));
				}
				if (value instanceof IMap<?, ?> map) {
					final ListVector.NamedBuilder builder = new ListVector.NamedBuilder();
					for (final var entry : map.entrySet()) {
						if (!(entry.getKey() instanceof String name) || name.isEmpty())
							throw error(scope, "RDS maps require non-empty string keys");
						builder.add(name, convert(entry.getValue()));
					}
					// R has no names for a zero-length list; preserve an empty map's identity.
					return builder.build().setAttribute("names",
							new StringArrayVector(map.keySet().toArray(String[]::new)));
				}
				if (value instanceof IList<?> list) return vector(list, list.getGamlType().getContentType());
				return scalar(value);
			} finally {
				ancestors.remove(value);
			}
		}

		private SEXP scalar(final Object value) {
			if (value instanceof Boolean b) return new LogicalArrayVector(b);
			if (value instanceof String s) return new StringArrayVector(s);
			if (value instanceof Byte || value instanceof Short || value instanceof Integer) {
				final int n = ((Number) value).intValue();
				return n == Integer.MIN_VALUE ? new DoubleArrayVector((double) n) : new IntArrayVector(n);
			}
			if (value instanceof Long n) {
				if (n < -9007199254740992L || n > 9007199254740992L)
					throw error(scope, "Integer outside the exact R numeric range: " + n);
				return new DoubleArrayVector(n.doubleValue());
			}
			if (value instanceof Float || value instanceof Double) {
				final double n = ((Number) value).doubleValue();
				return new DoubleArrayVector(Double.isNaN(n) ? Double.NaN : n);
			}
			throw error(scope, "Unsupported value for RDS export: " + value.getClass().getSimpleName());
		}

		SEXP vector(final List<?> values, final IType<?> declared) {
			int kind = declared.id();
			boolean found = false;
			for (final Object value : values) {
				if (value == null) continue;
				final int next = scalarKind(value);
				if (!found) { kind = next; found = true; }
				else if (kind != next) {
					kind = (kind == IType.INT || kind == IType.FLOAT)
							&& (next == IType.INT || next == IType.FLOAT) ? IType.FLOAT : IType.LIST;
				}
			}
			if (kind == IType.INT) {
				final int[] data = new int[values.size()];
				for (int i = 0; i < data.length; i++) {
					data[i] = values.get(i) == null ? IntVector.NA : ((Number) values.get(i)).intValue();
				}
				return new IntArrayVector(data);
			}
			if (kind == IType.FLOAT) {
				final double[] data = new double[values.size()];
				for (int i = 0; i < data.length; i++) {
					if (values.get(i) == null) data[i] = DoubleVector.NA;
					else data[i] = ((DoubleVector) numeric(values.get(i))).getElementAsDouble(0);
				}
				return new DoubleArrayVector(data);
			}
			if (kind == IType.BOOL) {
				final int[] data = new int[values.size()];
				for (int i = 0; i < data.length; i++) {
					data[i] = values.get(i) == null ? LogicalVector.NA : Boolean.TRUE.equals(values.get(i)) ? 1 : 0;
				}
				return new LogicalArrayVector(data);
			}
			if (kind == IType.STRING) return new StringArrayVector(values.toArray(String[]::new));
			final ListVector.Builder builder = new ListVector.Builder();
			for (final Object value : values) { builder.add(convert(value)); }
			return builder.build();
		}

		private SEXP numeric(final Object value) {
			final SEXP result = scalar(value);
			return result instanceof IntVector integers ? new DoubleArrayVector(integers.getElementAsDouble(0)) : result;
		}

		private int scalarKind(final Object value) {
			if (value instanceof Boolean) return IType.BOOL;
			if (value instanceof String) return IType.STRING;
			if (value instanceof Byte || value instanceof Short || value instanceof Integer)
				return ((Number) value).intValue() == Integer.MIN_VALUE ? IType.FLOAT : IType.INT;
			if (value instanceof Long || value instanceof Float || value instanceof Double) return IType.FLOAT;
			return IType.LIST;
		}
	}

	static SEXP withRows(final ListVector frame, final int count) {
		final int[] rows = new int[count];
		for (int i = 0; i < count; i++) { rows[i] = i + 1; }
		return frame.setAttribute("class", new StringArrayVector("data.frame"))
				.setAttribute("row.names", new IntArrayVector(rows));
	}

	static GamaRuntimeException error(final IScope scope, final String message) {
		return GamaRuntimeException.error("RDS: " + message, scope);
	}
}
