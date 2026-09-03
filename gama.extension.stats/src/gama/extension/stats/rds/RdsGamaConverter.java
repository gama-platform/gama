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
		if (sexp == null || sexp == Null.INSTANCE) return null;

		// 1. Check for Matrix or 2D Array attribute 'dim'
		SEXP dimSexp = sexp.getAttribute(Symbol.get("dim"));
		if (dimSexp instanceof Vector dimVec && dimVec.length() == 2) {
			int rows = dimVec.getElementAsInt(0);
			int cols = dimVec.getElementAsInt(1);
			// In R, matrices are stored in column-major order.
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

		// 2. Check for R data.frame
		if (sexp.inherits("data.frame")
				|| sexp instanceof ListVector lv && sexp.getAttribute(Symbol.get("row.names")) != null) {
			ListVector lv = (ListVector) sexp;
			IList<String> colNames = GamaListFactory.create(Types.STRING);
			IList<IList<Object>> colData = GamaListFactory.create();
			for (int i = 0; i < lv.length(); i++) {
				String name = lv.hasNames() ? lv.getName(i) : "col_" + i;
				colNames.add(name);
				SEXP colSexp = lv.getElementAsSEXP(i);
				Object convertedCol = toGamaObject(scope, colSexp);
				if (convertedCol instanceof IList colList) {
					colData.add(colList);
				} else if (convertedCol != null) {
					IList singleList = GamaListFactory.create();
					singleList.add(convertedCol);
					colData.add(singleList);
				} else {
					colData.add(GamaListFactory.create());
				}
			}
			return GamaDataFrameFactory.create(scope, colNames, colData);
		}

		// 3. Check for Factor
		if (sexp.inherits("factor") || sexp.getAttribute(Symbol.get("levels")) != null) {
			SEXP levelsSexp = sexp.getAttribute(Symbol.get("levels"));
			if (levelsSexp instanceof Vector levelsVec && sexp instanceof Vector intVec) {
				IList<String> result = GamaListFactory.create(Types.STRING);
				for (int i = 0; i < intVec.length(); i++) {
					if (intVec.isElementNA(i)) {
						result.add(null);
					} else {
						int lvlIdx = intVec.getElementAsInt(i) - 1;
						if (lvlIdx >= 0 && lvlIdx < levelsVec.length()) {
							result.add(levelsVec.getElementAsString(lvlIdx));
						} else {
							result.add(null);
						}
					}
				}
				return result;
			}
		}

		// 4. Named List / Map
		if (sexp instanceof ListVector lv && lv.hasNames()) {
			IMap<String, Object> map = GamaMapFactory.create(Types.STRING, Types.NO_TYPE);
			for (int i = 0; i < lv.length(); i++) {
				String name = lv.getName(i);
				if (name == null || name.isEmpty()) name = "item_" + i;
				map.put(name, toGamaObject(scope, lv.getElementAsSEXP(i)));
			}
			return map;
		}

		// 5. Generic ListVector
		if (sexp instanceof ListVector lv) {
			IList<Object> list = GamaListFactory.create();
			for (int i = 0; i < lv.length(); i++) {
				list.add(toGamaObject(scope, lv.getElementAsSEXP(i)));
			}
			return list;
		}

		// 6. PairList
		if (sexp instanceof PairList pl) {
			IMap<String, Object> map = GamaMapFactory.create(Types.STRING, Types.NO_TYPE);
			for (PairList.Node node : pl.nodes()) {
				String key = node.hasTag() ? node.getTag().getPrintName() : null;
				Object val = toGamaObject(scope, node.getValue());
				if (key != null) { map.put(key, val); }
			}
			if (!map.isEmpty()) return map;
		}

		// 7. Atomic Vectors (Double, Int, String, Logical)
		if (sexp instanceof Vector vec) {
			IList<Object> list = GamaListFactory.create();
			for (int i = 0; i < vec.length(); i++) { list.add(extractVectorElement(vec, i)); }
			return list;
		}

		return sexp.toString();
	}

	private static Object extractVectorElement(final Vector vec, final int i) {
		if (vec.isElementNA(i)) return null;
		if (vec instanceof DoubleArrayVector || vec.getVectorType() == Vector.Type.DOUBLE) {
			return vec.getElementAsDouble(i);
		} else if (vec instanceof IntArrayVector || vec.getVectorType() == Vector.Type.INTEGER) {
			return vec.getElementAsInt(i);
		} else if (vec instanceof StringArrayVector || vec.getVectorType() == Vector.Type.STRING) {
			return vec.getElementAsString(i);
		} else if (vec instanceof LogicalArrayVector || vec.getVectorType() == Vector.Type.LOGICAL) {
			Logical l = vec.getElementAsLogical(i);
			return l == Logical.TRUE;
		}
		return vec.getElementAsObject(i);
	}

	/**
	 * Converts a GAMA object into an SEXP structure for RDS output.
	 */
	public static SEXP toSexp(final IScope scope, final Object value) {
		if (value == null) return Null.INSTANCE;

		// 1. IDataFrame -> R data.frame
		if (value instanceof IDataFrame df) {
			ListVector.NamedBuilder builder = ListVector.newNamedBuilder();
			IList<String> cols = df.getColumns();
			for (String col : cols) {
				IList<Object> colValues = df.getColumnValues(col);
				builder.add(col, toVectorSexp(scope, colValues));
			}
			ListVector lv = builder.build();
			SEXP withClass = lv.setAttribute("class", new StringArrayVector("data.frame"));
			int rowCount = df.getRows();
			int[] rowNames = new int[rowCount];
			for (int i = 0; i < rowCount; i++) rowNames[i] = i + 1;
			SEXP withRows = withClass.setAttribute("row.names", new IntArrayVector(rowNames));
			return withRows;
		}

		// 2. IMatrix -> R Matrix with 'dim'
		if (value instanceof IMatrix matrix) {
			int cols = matrix.getCols(scope);
			int rows = matrix.getRows(scope);
			int total = rows * cols;

			// Flatten in column-major order for R
			List<Object> flattened = new ArrayList<>(total);
			for (int c = 0; c < cols; c++) {
				for (int r = 0; r < rows; r++) { flattened.add(matrix.get(scope, c, r)); }
			}
			SEXP vec = toVectorSexp(scope, flattened);
			return vec.setAttribute("dim", new IntArrayVector(rows, cols));
		}

		// 3. IMap -> R Named List
		if (value instanceof IMap map) {
			ListVector.NamedBuilder builder = ListVector.newNamedBuilder();
			for (Object entryObj : map.entrySet()) {
				java.util.Map.Entry entry = (java.util.Map.Entry) entryObj;
				String key = String.valueOf(entry.getKey());
				builder.add(key, toSexp(scope, entry.getValue()));
			}
			return builder.build();
		}

		// 4. IList of Agents -> R data.frame
		if (value instanceof IList list && !list.isEmpty() && list.get(0) instanceof IAgent) {
			IAgent firstAgent = (IAgent) list.get(0);
			ITypeDescription sd = firstAgent.getSpecies().getDescription();
			Collection<String> attrNames = new ArrayList<>(sd.getAttributeNames());
			attrNames.removeAll(SaveStatement.NON_SAVEABLE_ATTRIBUTE_NAMES);

			ListVector.NamedBuilder builder = ListVector.newNamedBuilder();
			int agentCount = list.size();

			// Add standard agent columns
			String[] names = new String[agentCount];
			double[] locX = new double[agentCount];
			double[] locY = new double[agentCount];
			double[] locZ = new double[agentCount];

			for (int i = 0; i < agentCount; i++) {
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

			// Add custom attributes
			for (String attr : attrNames) {
				List<Object> attrValues = new ArrayList<>(agentCount);
				for (int i = 0; i < agentCount; i++) {
					IAgent ag = Cast.asAgent(scope, list.get(i));
					attrValues.add(ag.getDirectVarValue(scope, attr));
				}
				builder.add(attr, toVectorSexp(scope, attrValues));
			}

			ListVector lv = builder.build();
			SEXP withClass = lv.setAttribute("class", new StringArrayVector("data.frame"));
			int[] rowNames = new int[agentCount];
			for (int i = 0; i < agentCount; i++) rowNames[i] = i + 1;
			return withClass.setAttribute("row.names", new IntArrayVector(rowNames));
		}

		// 5. Generic List
		if (value instanceof IList list) { return toVectorSexp(scope, list); }

		// 6. Primitive Scalars
		if (value instanceof Number num) {
			if (value instanceof Integer || value instanceof Long) { return new IntArrayVector(num.intValue()); }
			return new DoubleArrayVector(num.doubleValue());
		}
		if (value instanceof Boolean b) { return new LogicalArrayVector(b); }
		if (value instanceof String s) { return new StringArrayVector(s); }

		return new StringArrayVector(String.valueOf(value));
	}

	private static SEXP toVectorSexp(final IScope scope, final List<?> list) {
		if (list.isEmpty()) return DoubleArrayVector.EMPTY;

		boolean allDouble = true;
		boolean allInt = true;
		boolean allString = true;
		boolean allBool = true;

		for (Object elem : list) {
			if (elem == null) continue;
			if (!(elem instanceof Double || elem instanceof Float)) allDouble = false;
			if (!(elem instanceof Integer || elem instanceof Short || elem instanceof Byte)) allInt = false;
			if (!(elem instanceof String)) allString = false;
			if (!(elem instanceof Boolean)) allBool = false;
		}

		int size = list.size();
		if (allInt) {
			int[] arr = new int[size];
			for (int i = 0; i < size; i++) {
				Object o = list.get(i);
				arr[i] = o == null ? IntArrayVector.NA : ((Number) o).intValue();
			}
			return new IntArrayVector(arr);
		} else if (allDouble || allDouble && allInt) {
			double[] arr = new double[size];
			for (int i = 0; i < size; i++) {
				Object o = list.get(i);
				arr[i] = o == null ? DoubleArrayVector.NA : ((Number) o).doubleValue();
			}
			return new DoubleArrayVector(arr);
		} else if (allBool) {
			boolean[] arr = new boolean[size];
			for (int i = 0; i < size; i++) {
				Object o = list.get(i);
				arr[i] = o != null && (Boolean) o;
			}
			return new LogicalArrayVector(arr);
		} else if (allString) {
			String[] arr = new String[size];
			for (int i = 0; i < size; i++) {
				Object o = list.get(i);
				arr[i] = o == null ? StringVector.NA : String.valueOf(o);
			}
			return new StringArrayVector(arr);
		} else {
			// Mixed list -> ListVector in R
			ListVector.Builder builder = ListVector.newBuilder();
			for (Object elem : list) { builder.add(toSexp(scope, elem)); }
			return builder.build();
		}
	}
}
