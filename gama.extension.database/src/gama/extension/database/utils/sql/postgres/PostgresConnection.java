/*******************************************************************************************************
 *
 * PostgresConnection.java, in gama.extension.database, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.extension.database.utils.sql.postgres;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.jdbc.JDBCDataStore;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import gama.core.metamodel.topology.projection.IProjection;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.dev.DEBUG;
import gama.extension.database.utils.sql.SqlConnection;
import gama.extension.database.utils.sql.SqlUtils;

/**
 * The Class PostgresConnection.
 */
/*
 * @Author TRUONG Minh Thai Fredric AMBLARD Benoit GAUDOU Christophe Sibertin-BLANC Created date: 19-Apr-2013 Modified:
 * 15-Jan-2014 Fix null error of getInsertString method
 *
 *
 * Last Modified: 15-Jan-2014
 */
public class PostgresConnection extends SqlConnection {

	/** The Constant POSTGRES. */
	public static final String POSTGRES = "postgres";

	/** The Constant POSTGIS. */
	public static final String POSTGIS = "postgis";
	
	/** The Constant WKT2GEO. */
	private static final String WKT2GEO = "ST_GeomFromText";

	//  TODO :check : keep here or move to  SqlConnection ?
	JDBCDataStore dataStore;		
	
	/**
	 * Instantiates a new postgres connection.
	 *
	 * @param venderName the vender name
	 * @param url the url
	 * @param port the port
	 * @param dbName the db name
	 * @param userName the user name
	 * @param password the password
	 * @param transformed the transformed
	 */
	public PostgresConnection(final IScope scope, final String venderName, final String url, final String port, final String dbName,
			final String userName, final String password, final Boolean transformed) {
		super(venderName, url, port, dbName, userName, password, transformed);
		
        Map<String, Object> params = new HashMap<>();
        params.put("dbtype", "postgis");
        params.put("host", url);
        params.put("port", port);
        params.put("schema", "public");
        params.put("database", dbName);
        params.put("user", userName);
        params.put("passwd", password);

        try {
			dataStore = (JDBCDataStore) DataStoreFinder.getDataStore(params);
		} catch (IOException e) {
			throw GamaRuntimeException.error("Error in  creating the PostGres/PostGIS connection.",scope);
		}
		
	}

	@Override
	public Connection connectDB()
			throws ClassNotFoundException, InstantiationException, SQLException, IllegalAccessException {
		Connection conn = null;
		try {
			conn = dataStore.getConnection(Transaction.AUTO_COMMIT);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		return conn;
	}

	@Override
	protected IList<IList<Object>> resultSet2GamaList(final ResultSetMetaData rsmd, final ResultSet rs) {
		// convert Geometry in SQL to Geometry type in GeoTool

		final IList<IList<Object>> repRequest = GamaListFactory.create(gama.gaml.types.Types.LIST);
		try {
			final List<Integer> geoColumn = getGeometryColumns(rsmd);
			final int nbCol = rsmd.getColumnCount();
			while (rs.next()) {
				final IList<Object> rowList = GamaListFactory.create();
				for (int j = 1; j <= nbCol; j++) {
					if (geoColumn.contains(j)) {
						rowList.add(SqlUtils.read(rs.getBytes(j)));
					} else {
						rowList.add(rs.getObject(j));
					}
				}
				repRequest.add(rowList);
			}
		} catch (final Exception e) {

		}
		return repRequest;

	}
	

	@Override
	protected boolean colIsGeometryType(ResultSetMetaData rsmd, int colNb) throws SQLException {
		return 		(POSTGRES.equalsIgnoreCase(vender) || POSTGIS.equalsIgnoreCase(vender)) 
				&& 	(rsmd.getColumnType(colNb) == 1111 || rsmd.getColumnType(colNb) == -2);
	}


	@Override
	protected String getInsertString(final IScope scope, final Connection conn, final String table_name,
			final IList<Object> cols, final IList<Object> values) throws GamaRuntimeException {
		final int col_no = cols.size();
		String insertStr = "INSERT INTO ";
		String selectStr = "SELECT ";
		StringBuilder colStr = new StringBuilder();
		// Check size of parameters
		if (values.size() != col_no) {
			throw new IndexOutOfBoundsException("Size of columns list and values list are not equal");
		}
		// Get column name
		for (int i = 0; i < col_no; i++) {
			colStr.append(cols.get(i));
			if (i < col_no - 1) {
				colStr.append(",");
			}
		}
		// create SELECT statement string
		selectStr = selectStr + colStr + " FROM " + table_name + " LIMIT 1 ;";

		if (DEBUG.IS_ON()) {
			DEBUG.OUT("PostgresConnection.getInsertString.select command:" + selectStr);
		}

		try {
			// get column type;
			final Statement st = conn.createStatement();
			final ResultSet rs = st.executeQuery(selectStr);
			final ResultSetMetaData rsmd = rs.getMetaData();
			final IList<Object> col_Names = getColumnName(rsmd);
			final IList<Object> col_Types = getColumnTypeName(rsmd);

			if (DEBUG.IS_ON()) {
				DEBUG.OUT("list of column Name:" + col_Names);
				DEBUG.OUT("list of column type:" + col_Types);
			}
			// Insert command
			// set parameter value
			StringBuilder valueStr = new StringBuilder(0);
			final IProjection saveProj = getSavingGisProjection(scope);
			for (int i = 0; i < col_no; i++) {
				// Value list begin-------------------------------------------
				if (values.get(i) == null) {
					valueStr.append(NULLVALUE);
				} else if (GEOMETRYTYPE.equalsIgnoreCase((String) col_Types.get(i))) {

					// 23/Jul/2013 - Transform GAMA GIS TO NORMAL
					final WKTReader wkt = new WKTReader();
					Geometry geo = wkt.read(values.get(i).toString());
					// DEBUG.LOG(geo.toString());
					if (transformed) {
						geo = saveProj.inverseTransform(geo); // have problem
																// here
					}
					valueStr.append(WKT2GEO);
					valueStr.append("('");
					valueStr.append(geo);
					valueStr.append("')");

				} else if (isTextType((String) col_Types.get(i))) { // for
																					// String
																					// type
					// Correct error string
					String temp = values.get(i).toString();
					temp = temp.replaceAll("'", "''");
					// Add to value:
					valueStr.append("'");
					valueStr.append(temp);
					valueStr.append("'");
				} else { // For other type
					valueStr.append(values.get(i));
				}
				if (i != col_no - 1) { // Add delimiter of each value
					valueStr.append(",");
				}
				// Value list
				// end--------------------------------------------------------

			}
			insertStr = insertStr + table_name + "(" + colStr + ") " + "VALUES(" + valueStr + ")";

			if (DEBUG.IS_ON()) {
				DEBUG.OUT("PostgresConection.getInsertString:" + insertStr);
			}

		} catch (final SQLException e) {
			e.printStackTrace();
			throw GamaRuntimeException.error("PostgresConnection.getInsertString:" + e.toString(), scope);
		} catch (final ParseException e) {
			e.printStackTrace();
			throw GamaRuntimeException.error("PostgresConnection.getInsertString:" + e.toString(), scope);
		}

		return insertStr;
	}

	@Override
	protected String getInsertString(final IScope scope, final Connection conn, final String table_name,
			final IList<Object> values) throws GamaRuntimeException {
		String insertStr = "INSERT INTO ";
		String selectStr = "SELECT ";

		// Get column name
		// create SELECT statement string
		selectStr = selectStr + " * " + " FROM " + table_name + " LIMIT 1 ;";

		if (DEBUG.IS_ON()) {
			DEBUG.OUT("PostgresConnection.getInsertString.select command:" + selectStr);
		}

		try {
			// get column type;
			final Statement st = conn.createStatement();
			final ResultSet rs = st.executeQuery(selectStr);
			final ResultSetMetaData rsmd = rs.getMetaData();
			final IList<Object> col_Names = getColumnName(rsmd);
			final IList<Object> col_Types = getColumnTypeName(rsmd);
			final int col_no = col_Names.size();
			// Check size of parameters
			if (values.size() != col_Names.size()) {
				throw new IndexOutOfBoundsException("Size of columns list and values list are not equal");
			}

			// Insert command
			// set parameter value
			StringBuilder colStr = new StringBuilder();
			StringBuilder valueStr = new StringBuilder();
			for (int i = 0; i < col_no; i++) {
				// Value list begin-------------------------------------------
				if (values.get(i) == null) {
					valueStr.append(NULLVALUE);
				} else if (GEOMETRYTYPE.equalsIgnoreCase((String) col_Types.get(i))) {
					// 23/Jul/2013 - Transform GAMA GIS TO NORMAL
					final WKTReader wkt = new WKTReader();
					Geometry geo = wkt.read(values.get(i).toString());
					// DEBUG.LOG(geo.toString());
					if (transformed) {
						geo = getSavingGisProjection(scope).inverseTransform(geo);
					}
					// DEBUG.LOG(geo.toString());
					valueStr.append(WKT2GEO);
					valueStr.append("('");
					valueStr.append(geo);
					valueStr.append("')");

				} else if (isTextType((String) col_Types.get(i))) {
					String temp = values.get(i).toString();
					temp = temp.replaceAll("'", "''");
					// Add to value:
					valueStr.append("'");
					valueStr.append(temp);
					valueStr.append("'");
				} else { // For other type
					valueStr.append(values.get(i));
				}
				// Value list
				// end--------------------------------------------------------
				// column list
				colStr.append(col_Names.get(i));

				if (i != col_no - 1) { // Add delimiter of each value
					colStr.append(",");
					valueStr.append(",");
				}
			}

			insertStr = insertStr + table_name + "(" + colStr + ") " + "VALUES(" + valueStr + ")";

		} catch (final SQLException e) {
			e.printStackTrace();
			throw GamaRuntimeException.error("PostgresConnection.getInsertString:" + e.toString(), scope);
		} catch (final ParseException e) {
			e.printStackTrace();
			throw GamaRuntimeException.error("PostgresConnection.getInsertString:" + e.toString(), scope);
		}

		return insertStr;
	}
	
	@Override
	public void close() throws Exception {
		dataStore.dispose();
	}
	
}
