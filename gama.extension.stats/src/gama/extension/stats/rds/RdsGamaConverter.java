/*******************************************************************************************************
 *
 * RdsGamaConverter.java, in gama.extension.stats, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.stats.rds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.Logical;
import org.renjin.sexp.LogicalArrayVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Vector;

import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.types.matrix.GamaMatrixFactory;
import gama.api.types.matrix.IMatrix;
import gama.extension.dataframe.GamaDataFrameFactory;
import gama.extension.dataframe.IDataFrame;
import gama.gaml.statements.SaveStatement;

/**
 * Utility for converting between GAMA objects (matrices, dataframes, lists, maps, agents)
 * and Renjin S-Expressions (SEXP) for R RDS binary serialization.
 */
@SuppressWarnings ({ "rawtypes", "unchecked" })
public class RdsGamaConverter {

	/**
	 * Converts an SEXP from an RDS file into a corresponding GAMA object.
	 */
	public static Object toGamaObject(final IScope scope, final SEXP sexp) {
		return RdsImporter.importSexp(scope, sexp);
	}

	/**
	 * Converts a GAMA object into an SEXP structure for RDS output.
	 */
	public static SEXP toSexp(final IScope scope, final Object value) {
		return RdsExporter.exportValue(scope, value);
	}

	private static class RdsImporter {

		static Object importSexp(final IScope scope, final SEXP sexp) {
			if (sexp == null || sexp == Null.INSTANCE) return null;

			if (isMatrix(sexp)) return convertMatrix(scope, sexp);
			if (isDataFrame(sexp)) return convertDataFrame(scope, (ListVector) sexp);
			if (isFactor(sexp)) return convertFactor(sexp);

			if (sexp instanceof ListVector lv) {
				return lv.hasNames() ? convertNamedList(scope, lv) : convertGenericList(scope, lv);
			}

			if (sexp instanceof PairList pl) return convertPairList(scope, pl);
			if (sexp instanceof Vector vec) return convertAtomicVector(vec);

			return sexp.toString();
		}

		private static boolean isMatrix(final SEXP sexp) {
			SEXP dimSexp = sexp.getAttribute(Symbol.get("dim"));
			return dimSexp instanceof Vector dimVec && dimVec.length() == 2;
		}

		private static boolean isDataFrame(final SEXP sexp) {
			if (sexp.inherits("data.frame")) return true;
			return sexp instanceof ListVector && sexp.getAttribute(Symbol.get("row.names")) != null;
		}

		private static boolean isFactor(final SEXP sexp) {
			if (sexp.inherits("factor")) return true;
			return sexp.getAttribute(Symbol.get("levels")) != null;
		}

		private static IMatrix convertMatrix(final IScope scope, final SEXP sexp) {
			Vector dimVec = (Vector) sexp.getAttribute(Symbol.get("dim"));
			int rows = dimVec.getElementAsInt(0);
			int cols = dimVec.getElementAsInt(1);

			IList list = GamaListFactory.create();
			Vector dataVec = (Vector) sexp;

			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					int idx = c * rows + r;
					list.add(extractVectorElement(dataVec, idx));
				}
			}
			return GamaMatrixFactory.create(scope, list, cols, rows, Types.NO_TYPE);
		}

		private static IDataFrame convertDataFrame(final IScope scope, final ListVector lv) {
			IList<String> colNames = GamaListFactory.create(Types.STRING);
			IList colData = GamaListFactory.create();

			for (int i = 0; i < lv.length(); i++) {
				String name = lv.hasNames() ? lv.getName(i) : "col_" + i;
				colNames.add(name);
				SEXP colSexp = lv.getElementAsSEXP(i);
				Object convertedCol = importSexp(scope, colSexp);
				colData.add(toColumnList(convertedCol));
			}
			return GamaDataFrameFactory.create(scope, colNames, colData);
		}

		private static IList<Object> toColumnList(final Object convertedCol) {
			if (convertedCol instanceof IList colList) return colList;
			IList singleList = GamaListFactory.create();
			if (convertedCol != null) singleList.add(convertedCol);
			return singleList;
		}

		private static IList<String> convertFactor(final SEXP sexp) {
			SEXP levelsSexp = sexp.getAttribute(Symbol.get("levels"));
			if (!(levelsSexp instanceof Vector levelsVec) || !(sexp instanceof Vector intVec)) {
				return GamaListFactory.create(Types.STRING);
			}

			IList<String> result = GamaListFactory.create(Types.STRING);
			for (int i = 0; i < intVec.length(); i++) {
				if (intVec.isElementNA(i)) {
					result.add(null);
				} else {
					int lvlIdx = intVec.getElementAsInt(i) - 1;
					boolean valid = lvlIdx >= 0 && lvlIdx < levelsVec.length();
					result.add(valid ? levelsVec.getElementAsString(lvlIdx) : null);
				}
			}
			return result;
		}

		private static IMap<String, Object> convertNamedList(final IScope scope, final ListVector lv) {
			IMap<String, Object> map = GamaMapFactory.create(Types.STRING, Types.NO_TYPE);
			for (int i = 0; i < lv.length(); i++) {
				String name = lv.getName(i);
				String key = (name != null && !name.isEmpty()) ? name : "item_" + i;
				map.put(key, importSexp(scope, lv.getElementAsSEXP(i)));
			}
			return map;
		}

		private static IList<Object> convertGenericList(final IScope scope, final ListVector lv) {
			IList<Object> list = GamaListFactory.create();
			for (int i = 0; i < lv.length(); i++) {
				list.add(importSexp(scope, lv.getElementAsSEXP(i)));
			}
			return list;
		}

		private static IMap<String, Object> convertPairList(final IScope scope, final PairList pl) {
			IMap<String, Object> map = GamaMapFactory.create(Types.STRING, Types.NO_TYPE);
			for (PairList.Node node : pl.nodes()) {
				if (node.hasTag()) {
					String key = node.getTag().getPrintName();
					map.put(key, importSexp(scope, node.getValue()));
				}
			}
			return map;
		}

		private static IList<Object> convertAtomicVector(final Vector vec) {
			IList<Object> list = GamaListFactory.create();
			for (int i = 0; i < vec.length(); i++) {
				list.add(extractVectorElement(vec, i));
			}
			return list;
		}

		private static Object extractVectorElement(final Vector vec, final int i) {
			if (vec.isElementNA(i)) return null;

			if (vec instanceof DoubleArrayVector || vec.getVectorType() == Vector.Type.DOUBLE) {
				return vec.getElementAsDouble(i);
			}
			if (vec instanceof IntArrayVector || vec.getVectorType() == Vector.Type.INTEGER) {
				return vec.getElementAsInt(i);
			}
			if (vec instanceof StringArrayVector || vec.getVectorType() == Vector.Type.STRING) {
				return vec.getElementAsString(i);
			}
			if (vec instanceof LogicalArrayVector || vec.getVectorType() == Vector.Type.LOGICAL) {
				Logical l = vec.getElementAsLogical(i);
				return l == Logical.TRUE;
			}
			return vec.getElementAsObject(i);
		}
	}

	private static class RdsExporter {

		static SEXP exportValue(final IScope scope, final Object value) {
			if (value == null) return Null.INSTANCE;

			if (value instanceof IDataFrame df) return dataFrameToSexp(scope, df);
			if (value instanceof IMatrix matrix) return matrixToSexp(scope, matrix);
			if (value instanceof IMap map) return mapToSexp(scope, map);
			if (isAgentList(value)) return new AgentExporter(scope, (IList) value).export();
			if (value instanceof IList list) return toVectorSexp(scope, list);

			return scalarToSexp(value);
		}

		private static SEXP dataFrameToSexp(final IScope scope, final IDataFrame df) {
			ListVector.NamedBuilder builder = ListVector.newNamedBuilder();
			IList<String> cols = df.getColumns();

			for (String col : cols) {
				IList<Object> colValues = df.getColumnValues(col);
				builder.add(col, toVectorSexp(scope, colValues));
			}

			ListVector lv = builder.build();
			SEXP withClass = lv.setAttribute("class", new StringArrayVector("data.frame"));
			return withClass.setAttribute("row.names", createRowNames(df.getRows()));
		}

		private static SEXP matrixToSexp(final IScope scope, final IMatrix matrix) {
			int cols = matrix.getCols(scope);
			int rows = matrix.getRows(scope);
			int total = rows * cols;

			List<Object> flattened = new ArrayList<>(total);
			for (int c = 0; c < cols; c++) {
				for (int r = 0; r < rows; r++) {
					flattened.add(matrix.get(scope, c, r));
				}
			}

			SEXP vec = toVectorSexp(scope, flattened);
			return vec.setAttribute("dim", new IntArrayVector(rows, cols));
		}

		private static SEXP mapToSexp(final IScope scope, final IMap map) {
			ListVector.NamedBuilder builder = ListVector.newNamedBuilder();
			for (Object entryObj : map.entrySet()) {
				Map.Entry entry = (Map.Entry) entryObj;
				String key = String.valueOf(entry.getKey());
				builder.add(key, exportValue(scope, entry.getValue()));
			}
			return builder.build();
		}

		private static boolean isAgentList(final Object value) {
			return value instanceof IList list && !list.isEmpty() && list.get(0) instanceof IAgent;
		}

		private static SEXP scalarToSexp(final Object value) {
			if (value instanceof Integer || value instanceof Long) {
				return new IntArrayVector(((Number) value).intValue());
			}
			if (value instanceof Number num) {
				return new DoubleArrayVector(num.doubleValue());
			}
			if (value instanceof Boolean b) {
				return new LogicalArrayVector(b);
			}
			return new StringArrayVector(String.valueOf(value));
		}

		private static SEXP toVectorSexp(final IScope scope, final List<?> list) {
			if (list.isEmpty()) return DoubleArrayVector.EMPTY;

			ListTypeKind typeKind = determineListType(list);
			int size = list.size();

			return switch (typeKind) {
				case INT -> buildIntVector(list, size);
				case DOUBLE -> buildDoubleVector(list, size);
				case BOOL -> buildLogicalVector(list, size);
				case STRING -> buildStringVector(list, size);
				case MIXED -> buildMixedListVector(scope, list);
			};
		}

		private enum ListTypeKind { INT, DOUBLE, BOOL, STRING, MIXED }

		private static ListTypeKind determineListType(final List<?> list) {
			ListTypeKind kind = null;
			for (Object elem : list) {
				if (elem == null) continue;
				kind = combineKinds(kind, getElementKind(elem));
				if (kind == ListTypeKind.MIXED) break;
			}
			return kind == null ? ListTypeKind.DOUBLE : kind;
		}

		private static ListTypeKind combineKinds(final ListTypeKind current, final ListTypeKind next) {
			if (current == null || current == next) return next;
			if (current == ListTypeKind.INT && next == ListTypeKind.DOUBLE) return ListTypeKind.DOUBLE;
			if (current == ListTypeKind.DOUBLE && next == ListTypeKind.INT) return ListTypeKind.DOUBLE;
			return ListTypeKind.MIXED;
		}

		private static ListTypeKind getElementKind(final Object elem) {
			if (elem instanceof Integer || elem instanceof Short || elem instanceof Byte) return ListTypeKind.INT;
			if (elem instanceof Double || elem instanceof Float || elem instanceof Long) return ListTypeKind.DOUBLE;
			if (elem instanceof Boolean) return ListTypeKind.BOOL;
			if (elem instanceof String) return ListTypeKind.STRING;
			return ListTypeKind.MIXED;
		}

		private static IntArrayVector buildIntVector(final List<?> list, final int size) {
			int[] arr = new int[size];
			for (int i = 0; i < size; i++) {
				Object o = list.get(i);
				arr[i] = (o == null) ? IntArrayVector.NA : ((Number) o).intValue();
			}
			return new IntArrayVector(arr);
		}

		private static DoubleArrayVector buildDoubleVector(final List<?> list, final int size) {
			double[] arr = new double[size];
			for (int i = 0; i < size; i++) {
				Object o = list.get(i);
				arr[i] = (o == null) ? DoubleArrayVector.NA : ((Number) o).doubleValue();
			}
			return new DoubleArrayVector(arr);
		}

		private static LogicalArrayVector buildLogicalVector(final List<?> list, final int size) {
			boolean[] arr = new boolean[size];
			for (int i = 0; i < size; i++) {
				Object o = list.get(i);
				arr[i] = (o != null && (Boolean) o);
			}
			return new LogicalArrayVector(arr);
		}

		private static StringArrayVector buildStringVector(final List<?> list, final int size) {
			String[] arr = new String[size];
			for (int i = 0; i < size; i++) {
				Object o = list.get(i);
				arr[i] = (o == null) ? StringVector.NA : String.valueOf(o);
			}
			return new StringArrayVector(arr);
		}

		private static ListVector buildMixedListVector(final IScope scope, final List<?> list) {
			ListVector.Builder builder = ListVector.newBuilder();
			for (Object elem : list) {
				builder.add(exportValue(scope, elem));
			}
			return builder.build();
		}
	}

	private static class AgentExporter {
		private final IScope scope;
		private final IList list;
		private final int count;
		private final ListVector.NamedBuilder builder = ListVector.newNamedBuilder();

		AgentExporter(final IScope scope, final IList list) {
			this.scope = scope;
			this.list = list;
			this.count = list.size();
		}

		SEXP export() {
			addBaseColumns();
			addCustomAttributes();
			ListVector lv = builder.build();
			SEXP withClass = lv.setAttribute("class", new StringArrayVector("data.frame"));
			return withClass.setAttribute("row.names", createRowNames(count));
		}

		private void addBaseColumns() {
			String[] names = new String[count];
			double[] locX = new double[count];
			double[] locY = new double[count];
			double[] locZ = new double[count];

			for (int i = 0; i < count; i++) {
				IAgent ag = Cast.asAgent(scope, list.get(i));
				names[i] = ag.getName();
				locX[i] = ag.getLocation().getX();
				locY[i] = ag.getLocation().getY();
				locZ[i] = ag.getLocation().getZ();
			}

			builder.add("name", new StringArrayVector(names));
			builder.add("x", new DoubleArrayVector(locX));
			builder.add("y", new DoubleArrayVector(locY));
			builder.add("z", new DoubleArrayVector(locZ));
		}

		private void addCustomAttributes() {
			IAgent firstAgent = (IAgent) list.get(0);
			ITypeDescription sd = firstAgent.getSpecies().getDescription();
			Collection<String> attrNames = new ArrayList<>(sd.getAttributeNames());
			attrNames.removeAll(SaveStatement.NON_SAVEABLE_ATTRIBUTE_NAMES);

			for (String attr : attrNames) {
				List<Object> attrValues = new ArrayList<>(count);
				for (int i = 0; i < count; i++) {
					IAgent ag = Cast.asAgent(scope, list.get(i));
					attrValues.add(ag.getDirectVarValue(scope, attr));
				}
				builder.add(attr, RdsExporter.exportValue(scope, attrValues));
			}
		}
	}

	private static IntArrayVector createRowNames(final int count) {
		int[] rowNames = new int[count];
		for (int i = 0; i < count; i++) rowNames[i] = i + 1;
		return new IntArrayVector(rowNames);
	}
}
