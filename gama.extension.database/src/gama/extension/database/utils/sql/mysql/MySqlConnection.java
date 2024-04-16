/*******************************************************************************************************
 *
 * MySqlConnection.java, in gama.extension.database, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.extension.database.utils.sql.mysql;

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
import org.geotools.data.mysql.MySQLDataStoreFactory;
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
 * The Class MySqlConnection.
 */
 /*
 * @Author TRUONG Minh Thai Fredric AMBLARD Benoit GAUDOU Christophe Sibertin-BLANC Created date: 19-Apr-2013 Modified:
 *
 * 15-Jan-2014 Fix null error of getInsertString methods Fix date/time error of getInsertString methods
 *
 * Last Modified: 15-Jan-2014
 */
public class MySqlConnection extends SqlConnection {

	/** The Constant MYSQL. */
	public static final String MYSQL = "mysql";
	
	/** The Constant WKT2GEO. */
	private static final String WKT2GEO = "GeomFromText";
	
	/** The Constant PREFIX_TIMESTAMP. */
	private static final String PREFIX_TIMESTAMP = "cast('";
	
	/** The Constant MID_TIMESTAMP. */
	private static final String MID_TIMESTAMP = "' as ";
	
	/** The Constant SUPFIX_TIMESTAMP. */
	private static final String SUPFIX_TIMESTAMP = ")";

	//  TODO :check : keep here or move to  SqlConnection ?
	JDBCDataStore dataStore;	
	
	/**
	 * Instantiates a new my sql connection.
	 *
	 * @param venderName the vender name
	 * @param url the url
	 * @param port the port
	 * @param dbName the db name
	 * @param userName the user name
	 * @param password the password
	 * @param transformed the transformed
	 */
	public MySqlConnection(final IScope scope, final String venderName, final String url, final String port, final String dbName,
			final String userName, final String password, final Boolean transformed) {
		super(venderName, url, port, dbName, userName, password, transformed);
		
		Map<String,String> params = new HashMap<>();
		params.put(MySQLDataStoreFactory.DBTYPE.key, vender);
		params.put(MySQLDataStoreFactory.HOST.key, url);
		params.put(MySQLDataStoreFactory.PORT.key, port);
		params.put(MySQLDataStoreFactory.DATABASE.key, dbName);
		params.put(MySQLDataStoreFactory.USER.key, userName);
		params.put(MySQLDataStoreFactory.PASSWD.key, password);

		try {
			dataStore = (JDBCDataStore) DataStoreFinder.getDataStore(params);
		} catch (IOException e) {
			throw GamaRuntimeException.error("Error in  creating the MySQL connection.",scope);
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
						rowList.add(SqlUtils.inputStream2Geometry(rs.getBinaryStream(j)));
					} else {
						rowList.add(rs.getObject(j));
					}
				}
				repRequest.add(rowList);
				// i++;
			}
		} catch (final Exception e) {

		}
		return repRequest;

	}

	@Override
	protected boolean colIsGeometryType(final ResultSetMetaData rsmd, int colNb) throws SQLException {
		return MYSQL.equalsIgnoreCase(vender) && (rsmd.getColumnType(colNb) == -2 || rsmd.getColumnType(colNb) == -4);
	}
	

	@Override
	protected IList<Object> getColumnTypeName(final ResultSetMetaData rsmd) throws SQLException {
		final int numberOfColumns = rsmd.getColumnCount();
		final IList<Object> columnType = GamaListFactory.create();
		for (int i = 1; i <= numberOfColumns; i++) {
			if (colIsGeometryType(rsmd, i)) {
				columnType.add(GEOMETRYTYPE);
			} else {
				columnType.add(rsmd.getColumnTypeName(i).toUpperCase());
			}
		}
		return columnType;

	}

	@Override
	protected String getInsertString(final IScope scope, final Connection conn, final String table_name,
			final IList<Object> cols, final IList<Object> values) throws GamaRuntimeException {
		final int col_no = cols.size();
		String insertStr = "INSERT INTO ";
		String selectStr = "SELECT ";
		String colStr = "";
		String valueStr = "";
		// Check size of parameters
		if (values.size() != col_no) {
			throw new IndexOutOfBoundsException("Size of columns list and values list are not equal");
		}
		// Get column name
		for (int i = 0; i < col_no; i++) {
			if (i == col_no - 1) {
				colStr = colStr + (String) cols.get(i);
			} else {
				colStr = colStr + (String) cols.get(i) + ",";
			}
		}
		// create SELECT statement string
		selectStr = selectStr + colStr + " FROM " + table_name + " LIMIT 1 ;";

		if (DEBUG.IS_ON()) {
			DEBUG.OUT("MySqlConnection.getInsertString.select command:" + selectStr);
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
			valueStr = "";
			final IProjection saveGis = getSavingGisProjection(scope);
			for (int i = 0; i < col_no; i++) {
				// Value list begin-------------------------------------------
				if (values.get(i) == null) {
					valueStr = valueStr + NULLVALUE;
				} else if (GEOMETRYTYPE.equalsIgnoreCase((String) col_Types.get(i))) {

					// 23/Jul/2013 - Transform GAMA GIS TO NORMAL
					final WKTReader wkt = new WKTReader();
					Geometry geo = wkt.read(values.get(i).toString());
					// DEBUG.LOG(geo.toString());
					if (transformed) {
						geo = saveGis.inverseTransform(geo);
					}
					valueStr = valueStr + WKT2GEO + "('" + geo.toString() + "')";

				} else if (isTextType((String)col_Types.get(i))) { // for
																					// String
																					// type
					// Correct error string
					String temp = values.get(i).toString();
					temp = temp.replaceAll("'", "''");
					// Add to value:
					valueStr = valueStr + "'" + temp + "'";
				} else if (TIMESTAMP.equalsIgnoreCase((String) col_Types.get(i))) {
					valueStr = valueStr + "TIMESTAMP('" + values.get(i).toString() + "')";
				} else if (YEAR.equalsIgnoreCase((String) col_Types.get(i))) {
					valueStr = valueStr + "YEAR('" + values.get(i).toString() + "')";
				} else if (DATETIME.equalsIgnoreCase((String) col_Types.get(i))) {
					valueStr = valueStr + PREFIX_TIMESTAMP + values.get(i).toString() + MID_TIMESTAMP + DATETIME
							+ SUPFIX_TIMESTAMP;
				} else if (DATE.equalsIgnoreCase((String) col_Types.get(i))) {
					valueStr = valueStr + PREFIX_TIMESTAMP + values.get(i).toString() + MID_TIMESTAMP + DATE
							+ SUPFIX_TIMESTAMP;
				} else if (TIME.equalsIgnoreCase((String) col_Types.get(i))) {
					valueStr = valueStr + PREFIX_TIMESTAMP + values.get(i).toString() + MID_TIMESTAMP + TIME
							+ SUPFIX_TIMESTAMP;
				} else { // For other type
					valueStr = valueStr + values.get(i).toString();
				}
				if (i != col_no - 1) { // Add delimiter of each value
					valueStr = valueStr + ",";
				}
				// Value list
				// end--------------------------------------------------------

			}
			insertStr = insertStr + table_name + "(" + colStr + ") " + "VALUES(" + valueStr + ")";

		} catch (final SQLException e) {
			e.printStackTrace();
			throw GamaRuntimeException.error("MySqlConection.getInsertString:" + e.toString(), scope);
		} catch (final ParseException e) {
			e.printStackTrace();
			throw GamaRuntimeException.error("MySqlConection.getInsertString:" + e.toString(), scope);
		}

		return insertStr;
	}

	@Override
	protected String getInsertString(final IScope scope, final Connection conn, final String table_name,
			final IList<Object> values) throws GamaRuntimeException {
		String insertStr = "INSERT INTO ";
		String selectStr = "SELECT ";
		String colStr = "";
		String valueStr = "";

		// Get column name
		// create SELECT statement string
		selectStr = selectStr + " * " + " FROM " + table_name + " LIMIT 1 ;";

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
			colStr = "";
			valueStr = "";
			for (int i = 0; i < col_no; i++) {
				// Value list begin-------------------------------------------
				if (values.get(i) == null) {
					valueStr = valueStr + NULLVALUE;
				} else if (GEOMETRYTYPE.equalsIgnoreCase((String) col_Types.get(i))) { // for
					// 23/Jul/2013 - Transform GAMA GIS TO NORMAL
					final WKTReader wkt = new WKTReader();
					Geometry geo = wkt.read(values.get(i).toString());
					if (transformed) {
						geo = getSavingGisProjection(scope).inverseTransform(geo);
					}
					valueStr = valueStr + WKT2GEO + "('" + geo.toString() + "')";

				} else if (CHAR.equalsIgnoreCase((String) col_Types.get(i))
						|| VARCHAR.equalsIgnoreCase((String) col_Types.get(i))
						|| NVARCHAR.equalsIgnoreCase((String) col_Types.get(i))
						|| TEXT.equalsIgnoreCase((String) col_Types.get(i))) {

					String temp = values.get(i).toString();
					temp = temp.replaceAll("'", "''");
					// Add to value:
					valueStr = valueStr + "'" + temp + "'";
				} else if (TIMESTAMP.equalsIgnoreCase((String) col_Types.get(i))) {
					valueStr = valueStr + "TIMESTAMP('" + values.get(i).toString() + "')";
				} else if (YEAR.equalsIgnoreCase((String) col_Types.get(i))) {
					valueStr = valueStr + "YEAR('" + values.get(i).toString() + "')";
				} else if (DATETIME.equalsIgnoreCase((String) col_Types.get(i))) {
					valueStr = valueStr + PREFIX_TIMESTAMP + values.get(i).toString() + MID_TIMESTAMP + DATETIME
							+ SUPFIX_TIMESTAMP;
				} else if (((String) col_Types.get(i)).equalsIgnoreCase(DATE)) {
					valueStr = valueStr + PREFIX_TIMESTAMP + values.get(i).toString() + MID_TIMESTAMP + DATE
							+ SUPFIX_TIMESTAMP;
				} else if (((String) col_Types.get(i)).equalsIgnoreCase(TIME)) {
					valueStr = valueStr + PREFIX_TIMESTAMP + values.get(i).toString() + MID_TIMESTAMP + TIME
							+ SUPFIX_TIMESTAMP;
				} else { // For other type
					valueStr = valueStr + values.get(i).toString();
				}
				// Value list
				// end--------------------------------------------------------
				// column list
				colStr = colStr + col_Names.get(i).toString();

				if (i != col_no - 1) { // Add delimiter of each value
					colStr = colStr + ",";
					valueStr = valueStr + ",";
				}
			}

			insertStr = insertStr + table_name + "(" + colStr + ") " + "VALUES(" + valueStr + ")";

		} catch (final SQLException e) {
			e.printStackTrace();
			throw GamaRuntimeException.error("MySqlConection.getInsertString:" + e.toString(), scope);
		} catch (final ParseException e) {
			e.printStackTrace();
			throw GamaRuntimeException.error("MySqlConection.getInsertString:" + e.toString(), scope);
		}

		return insertStr;
	}

	@Override
	public void close() throws Exception {
		dataStore.dispose();
	}
}
