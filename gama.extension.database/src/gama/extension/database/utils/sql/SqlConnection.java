/*******************************************************************************************************
 *
 * SqlConnection.java, in gama.extension.database, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.database.utils.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Geometry;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.Types;
import gama.api.kernel.topology.IProjection;
import gama.api.runtime.scope.IScope;
import gama.api.types.dataframe.GamaDataFrameFactory;
import gama.api.types.dataframe.IDataFrame;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IShape;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.utils.geometry.GamaEnvelopeFactory;
import gama.api.utils.geometry.IEnvelope;
import gama.api.utils.prefs.GamaPreferences;
import gama.core.topology.gis.Projection;
import gama.dev.DEBUG;

/**
 * The Class SqlConnection.
 */
/*
 * @Author TRUONG Minh Thai Fredric AMBLARD Benoit GAUDOU Christophe Sibertin-BLANC Created date: 19-Apr-2013 Modified:
 * 26-Apr-2013: Remove driver gama.dependencies/sqljdbc4.jar add driver gama.dependencies/jtds-1.2.6.jar Change driver
 * name for MSSQL from com.microsoft.sqlserver.jdbc.SQLServerDriver to net.sourceforge.jtds.jdbc.Driver 18-July-2013:
 * Add load extension library for SQLITE case. 15-Jan-2014: Add datetime type. Add NULL VALUE Last Modified: 15-Jan-2014
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public abstract class SqlConnection implements AutoCloseable {

	/** The Constant GEOMETRYTYPE. */
	public static final String GEOMETRYTYPE = "GEOMETRY";

	/** The Constant CHAR. */
	protected static final String CHAR = "CHAR";

	/** The Constant VARCHAR. */
	protected static final String VARCHAR = "VARCHAR";

	/** The Constant NVARCHAR. */
	protected static final String NVARCHAR = "NVARCHAR";

	/** The Constant TEXT. */
	protected static final String TEXT = "TEXT";

	/** The Constant BLOB. */
	protected static final String BLOB = "BLOB";

	/** The Constant TIMESTAMP. */
	protected static final String TIMESTAMP = "TIMESTAMP";

	/** The Constant DATETIME. */
	protected static final String DATETIME = "DATETIME"; // MSSQL,Postgres, MySQL,

	/** The Constant DATE. */
	protected static final String DATE = "DATE"; // MSSQL,Postgres, MySQL, SQlite

	/** The Constant YEAR. */
	protected static final String YEAR = "YEAR"; // Postgres, MySQL(yyyy)

	/** The Constant TIME. */
	protected static final String TIME = "TIME"; // MySQL ('00:00:00')

	/** The Constant NULLVALUE. */
	protected static final String NULLVALUE = "NULL";

	/** The Constant SQLITEDriver. */
	protected static final String SQLITEDriver = "org.sqlite.JDBC";

	/** The vender. */
	protected String vender = "";

	/** The url. */
	protected String url = "";

	/** The port. */
	protected String port = "";

	/** The db name. */
	protected String dbName = "";

	/** The user name. */
	protected String userName = "";

	/** The password. */
	protected String password = "";

	/** The transformed. */
	protected Boolean transformed = false;

	/** The extension. */
	protected String extension = null;

	// AD: Added to be sure that SQL connections use a correct projection when
	/** The gis. */
	// they load/save geometries
	private IProjection gis = null;
	// AD: Added to be sure to remember the parameters (which can contain other
	/** The params. */
	// informations about GIS data
	private Map<String, Object> params;

	/**
	 * Sets the gis.
	 *
	 * @param gis
	 *            the new gis
	 */
	public void setGis(final Projection gis) { this.gis = gis; }

	/**
	 * Gets the gis.
	 *
	 * @return the gis
	 */
	public IProjection getGis() { return this.gis; }

	/**
	 * Gets the transform.
	 *
	 * @return the transform
	 */
	public boolean getTransform() { return transformed; }

	/**
	 * Gets the saving gis projection.
	 *
	 * @param scope
	 *            the scope
	 * @return the saving gis projection
	 */
	protected IProjection getSavingGisProjection(final IScope scope) {
		final Boolean longitudeFirst =
				params.containsKey("longitudeFirst") ? (Boolean) params.get("longitudeFirst") : true;
		final String crs = (String) params.get("crs");
		if (crs != null) {
			try {
				return scope.getSimulation().getProjectionFactory().forSavingWith(scope, crs);
			} catch (final Exception e) {

				throw GamaRuntimeException.error("No factory found for decoding the EPSG " + crs
						+ " code. GAMA may be unable to save any GIS data", scope);

			}
		}
		final String srid = (String) params.get("srid");
		if (srid != null) {
			try {
				return scope.getSimulation().getProjectionFactory().forSavingWith(scope, Cast.asInt(scope, srid),
						longitudeFirst);
			} catch (final Exception e) {

				throw GamaRuntimeException.error("No factory found for decoding the EPSG " + srid
						+ " code. GAMA may be unable to save any GIS data", scope);

			}
		}
		try {
			return scope.getSimulation().getProjectionFactory().forSavingWith(scope,
					GamaPreferences.External.LIB_OUTPUT_CRS.getValue());
		} catch (final Exception e) {

			throw GamaRuntimeException.error(
					"No factory found for decoding the EPSG " + GamaPreferences.External.LIB_OUTPUT_CRS.getValue()
							+ " code. GAMA may be unable to save any GIS data",
					scope);

		}

	}

	/**
	 * Sets the params.
	 *
	 * @param params
	 *            the params
	 */
	public void setParams(final Map<String, Object> params) { this.params = params; }

	/**
	 * Instantiates a new sql connection.
	 *
	 * @param dbName
	 *            the db name
	 */
	SqlConnection(final String dbName) {
		this.dbName = dbName;
	}

	/**
	 * Instantiates a new sql connection.
	 *
	 * @param venderName
	 *            the vender name
	 * @param database
	 *            the database
	 */
	SqlConnection(final String venderName, final String database) {
		this.vender = venderName;
		this.dbName = database;
	}

	/**
	 * Instantiates a new sql connection.
	 *
	 * @param venderName
	 *            the vender name
	 * @param database
	 *            the database
	 * @param transformed
	 *            the transformed
	 */
	protected SqlConnection(final String venderName, final String database, final Boolean transformed) {
		this.vender = venderName;
		this.dbName = database;
		this.transformed = transformed;
	}

	/**
	 * Instantiates a new sql connection.
	 *
	 * @param venderName
	 *            the vender name
	 * @param url
	 *            the url
	 * @param port
	 *            the port
	 * @param dbName
	 *            the db name
	 * @param userName
	 *            the user name
	 * @param password
	 *            the password
	 */
	SqlConnection(final String venderName, final String url, final String port, final String dbName,
			final String userName, final String password) {
		this.vender = venderName;
		this.url = url;
		this.port = port;
		this.dbName = dbName;
		this.userName = userName;
		this.password = password;
	}

	/**
	 * Instantiates a new sql connection.
	 *
	 * @param venderName
	 *            the vender name
	 * @param url
	 *            the url
	 * @param port
	 *            the port
	 * @param dbName
	 *            the db name
	 * @param userName
	 *            the user name
	 * @param password
	 *            the password
	 * @param transformed
	 *            the transformed
	 */
	public SqlConnection(final String venderName, final String url, final String port, final String dbName,
			final String userName, final String password, final Boolean transformed) {
		this.vender = venderName;
		this.url = url;
		this.port = port;
		this.dbName = dbName;
		this.userName = userName;
		this.password = password;
		this.transformed = transformed;
	}

	/**
	 * Connect DB.
	 *
	 * @return the connection
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 * @throws InstantiationException
	 *             the instantiation exception
	 * @throws SQLException
	 *             the SQL exception
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 */
	/*
	 * Make a connection to BDMS
	 */
	public abstract Connection connectDB()
			throws ClassNotFoundException, InstantiationException, SQLException, IllegalAccessException;

	/**
	 * Result set 2 gama list.
	 *
	 * @param rsmd
	 *            the rsmd
	 * @param rs
	 *            the rs
	 * @return the i list
	 */
	/*
	 * @Method:resultSet2IList(ResultSetMetaData rsmd, ResultSet rs)
	 *
	 * @Description: Convert RecordSet to IList
	 *
	 * @param ResultSetMetaData,ResultSet
	 *
	 * @return IList<IList<Object>>
	 */
	protected abstract IList<IList<Object>> resultSet2GamaList(ResultSetMetaData rsmd, ResultSet rs);

	/**
	 * Returns true if column colNb is of type geometry (depends on the type of connection)
	 *
	 * @param rsmd
	 * @param colNb
	 * @return
	 * @throws SQLException
	 */
	protected abstract boolean colIsGeometryType(ResultSetMetaData rsmd, int colNb) throws SQLException;

	/**
	 * Gets the geometry columns.
	 *
	 * @param rsmd
	 *            the rsmd
	 * @return the geometry columns
	 * @throws SQLException
	 *             the SQL exception
	 */
	/*
	 * @Meththod: getGeometryColumns(ResultSetMetaData rsmd)
	 *
	 * @Description: Get columns id of field with geometry type
	 *
	 * @param ResultSetMetaData
	 *
	 * @return List<Integer>
	 *
	 * @throws SQLException
	 */
	protected List<Integer> getGeometryColumns(final ResultSetMetaData rsmd) throws SQLException {
		final int numberOfColumns = rsmd.getColumnCount();
		final List<Integer> geoColumn = new ArrayList<>();
		for (int i = 1; i <= numberOfColumns; i++) { if (colIsGeometryType(rsmd, i)) { geoColumn.add(i); } }
		return geoColumn;
	}

	/**
	 * Gets the column type name.
	 *
	 * @param rsmd
	 *            the rsmd
	 * @return the column type name
	 * @throws SQLException
	 *             the SQL exception
	 */
	/*
	 * @Method: getColumnTypeName
	 *
	 * @Description: Get columns type name
	 *
	 * @param ResultSetMetaData
	 *
	 * @return IList<String>
	 *
	 * @throws SQLException
	 */
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

	/**
	 * Returns true if a column type name is one of the possible text types (char, varchar, nvarchar and text)
	 *
	 * @param s
	 * @return
	 */
	public static boolean isTextType(final String s) {
		return CHAR.equalsIgnoreCase(s) || VARCHAR.equalsIgnoreCase(s) || NVARCHAR.equalsIgnoreCase(s)
				|| TEXT.equalsIgnoreCase(s);
	}

	/**
	 * Gets the insert string.
	 *
	 * @param scope
	 *            the scope
	 * @param conn
	 *            the conn
	 * @param table_name
	 *            the table name
	 * @param cols
	 *            the cols
	 * @param values
	 *            the values
	 * @return the insert string
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	/*
	 * Make insert command string with columns and values
	 */
	protected abstract String getInsertString(IScope scope, Connection conn, String table_name, IList<Object> cols,
			IList<Object> values) throws GamaRuntimeException;

	/**
	 * Gets the insert string.
	 *
	 * @param scope
	 *            the scope
	 * @param conn
	 *            the conn
	 * @param table_name
	 *            the table name
	 * @param values
	 *            the values
	 * @return the insert string
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	/*
	 * Make insert command string for all columns with values
	 */
	protected abstract String getInsertString(IScope scope, Connection conn, String table_name, IList<Object> values)
			throws GamaRuntimeException;

	/**
	 * Opens a connection, runs the given SELECT statement and returns the result as a dataframe.
	 *
	 * @param scope
	 *            the scope
	 * @param selectComm
	 *            the SELECT statement
	 * @return the result as a dataframe
	 */
	public IDataFrame selectDB(final IScope scope, final String selectComm) {
		try (Connection conn = connectDB();) {
			return selectDB(scope, conn, selectComm);
		} catch (final Exception e) {
			throw GamaRuntimeException.error("SQLConnection.selectDB: " + e.toString(), scope);
		}

	}

	/**
	 * Runs the given SELECT statement on an existing connection and returns the result as a dataframe.
	 *
	 * @param scope
	 *            the scope
	 * @param conn
	 *            the connection
	 * @param selectComm
	 *            the SELECT statement
	 * @return the result as a dataframe
	 */
	public IDataFrame selectDB(final IScope scope, final Connection conn, final String selectComm) {
		try (final Statement st = conn.createStatement(); final ResultSet rs = st.executeQuery(selectComm);) {
			return buildDataFrame(scope, rs);
		} catch (final SQLException e) {
			throw GamaRuntimeException.error("SQLConnection.selectDB: " + e.toString(), scope);
		}
	}

	/**
	 * Builds a dataframe from a result set: reads column names and types, converts records (geometry columns are
	 * reprojected to the world CRS when {@code transformed} is set), and wraps everything as an {@link IDataFrame} whose
	 * geometry cells are GAMA geometries.
	 *
	 * @param scope
	 *            the scope
	 * @param rs
	 *            the result set
	 * @return the dataframe
	 * @throws SQLException
	 *             the SQL exception
	 */
	private IDataFrame buildDataFrame(final IScope scope, final ResultSet rs) throws SQLException {
		final ResultSetMetaData rsmd = rs.getMetaData();
		final IList<Object> colNames = getColumnName(rsmd);
		final IList<Object> colTypes = getColumnTypeName(rsmd);
		IList<Object> records = (IList<Object>) (IList) resultSet2GamaList(rsmd, rs);
		// Reproject geometries to the world CRS if requested
		if (colTypes.contains(GEOMETRYTYPE) && transformed
				&& scope.getSimulation().getProjectionFactory().getWorld() != null) {
			final IEnvelope env = scope.getSimulation().getEnvelope();
			gis = scope.getSimulation().getProjectionFactory().fromParams(scope, params, env);
			final IList<Object> triple = GamaListFactory.create();
			triple.add(colNames);
			triple.add(colTypes);
			triple.add(records);
			records = (IList<Object>) SqlUtils.transform(scope, gis, triple, false).get(2);
		}
		return toDataFrame(scope, colNames, colTypes, records);
	}

	/**
	 * Converts the (column names, column types, records) triple produced by a query into an {@link IDataFrame}. Geometry
	 * cells (columns typed {@link #GEOMETRYTYPE}) are turned into GAMA geometries so the dataframe is directly usable in
	 * GAML and by the 'create ... from:' delegate.
	 *
	 * @param scope
	 *            the scope
	 * @param colNames
	 *            the column names
	 * @param colTypes
	 *            the column types
	 * @param records
	 *            the records (list of rows)
	 * @return the dataframe
	 */
	private IDataFrame toDataFrame(final IScope scope, final IList<Object> colNames, final IList<Object> colTypes,
			final IList<Object> records) {
		final IList<String> columns = GamaListFactory.create(Types.STRING);
		for (final Object c : colNames) { columns.add(String.valueOf(c)); }
		final int nbCol = columns.size();
		final IList<IList> rows = GamaListFactory.create(Types.LIST);
		for (final Object recObj : records) {
			final IList<Object> rec = (IList<Object>) recObj;
			final IList<Object> row = GamaListFactory.create();
			for (int j = 0; j < nbCol; j++) {
				final Object v = rec.get(j);
				if (v instanceof Geometry geom && GEOMETRYTYPE.equalsIgnoreCase(String.valueOf(colTypes.get(j)))) {
					row.add(GamaShapeFactory.createFrom(geom));
				} else {
					row.add(v);
				}
			}
			rows.add(row);
		}
		return GamaDataFrameFactory.create(scope, columns, rows);
	}

	/*
	 * Make a connection to BDMS and execute the update statement (update/insert/delete/create/drop)
	 */

	/**
	 * Execute update DB.
	 *
	 * @param scope
	 *            the scope
	 * @param updateComm
	 *            the update comm
	 * @return the int
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public int executeUpdateDB(final IScope scope, final String updateComm) throws GamaRuntimeException {

		int n = 0;
		try (Connection conn = connectDB(); final Statement st = conn.createStatement();) {

			n = st.executeUpdate(updateComm);

		} catch (final Exception e) {
			throw GamaRuntimeException.error("SQLConnection.executeUpdateDB: " + e.toString(), scope);
		}

		return n;

	}

	/**
	 * Execute update DB.
	 *
	 * @param scope
	 *            the scope
	 * @param conn
	 *            the conn
	 * @param updateComm
	 *            the update comm
	 * @return the int
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	/*
	 * execute the update statement with current connection(update/insert/delete/create/drop)
	 */
	public int executeUpdateDB(final IScope scope, final Connection conn, final String updateComm)
			throws GamaRuntimeException {
		int n = 0;
		try (final Statement st = conn.createStatement();) {
			n = st.executeUpdate(updateComm);
		} catch (

		final SQLException e) {
			throw GamaRuntimeException.error("SQLConnection.executeUpdateDB: " + e.toString(), scope);
		}

		return n;
	}

	/**
	 * Gets the column name.
	 *
	 * @param rsmd
	 *            the rsmd
	 * @return the column name
	 * @throws SQLException
	 *             the SQL exception
	 */
	/*
	 * @Method: getColumnName
	 *
	 * @Description: Get columns name
	 *
	 * @param ResultSetMetaData
	 *
	 * @return IList<String>
	 *
	 * @throws SQLException
	 */
	protected IList<Object> getColumnName(final ResultSetMetaData rsmd) throws SQLException {
		final int numberOfColumns = rsmd.getColumnCount();
		final IList<Object> columnType = GamaListFactory.create();
		for (int i = 1; i <= numberOfColumns; i++) { columnType.add(rsmd.getColumnName(i).toUpperCase()); }
		return columnType;
	}

	/**
	 * Gets the url.
	 *
	 * @return the url
	 */
	public String getURL() { return url; }

	/**
	 * Gets the vendor.
	 *
	 * @return the vendor
	 */
	public String getVendor() { return vender; }

	/**
	 * Gets the user.
	 *
	 * @return the user
	 */
	public String getUser() { return userName; }

	/**
	 * Computes the envelope enclosing every geometry contained in the first geometry column of a query-result
	 * dataframe. Returns null if the dataframe has no geometry column or no rows.
	 *
	 * @param df
	 *            the dataframe produced by a select
	 * @return the bounding envelope, or null
	 */
	public static IEnvelope getBounds(final IDataFrame df) {
		if (df == null || df.getRows() == 0) return null;
		// Locate the (first) geometry column: after conversion its cells are GAMA geometries
		String geoCol = null;
		final IList<Object> firstRow = df.getRowValues(0);
		final IList<String> cols = df.getColumns();
		for (int j = 0; j < cols.size(); j++) {
			if (firstRow.get(j) instanceof IShape) {
				geoCol = cols.get(j);
				break;
			}
		}
		if (geoCol == null) return null;
		final List<IShape> shapes = new ArrayList<>();
		for (int i = 0; i < df.getRows(); i++) {
			if (df.getCellValue(i, geoCol) instanceof IShape s) { shapes.add(s); }
		}
		return shapes.isEmpty() ? null : GamaEnvelopeFactory.of(shapes);
	}

	/**
	 * Inserts every row of a dataframe into a table, in a single JDBC batch. The dataframe column names are used as the
	 * target columns, and each row becomes one record. This supports inserting many rows in one call (a single-row
	 * insert is simply a one-row dataframe).
	 *
	 * @param scope
	 *            the scope
	 * @param conn
	 *            the connection
	 * @param table_name
	 *            the table name
	 * @param data
	 *            the dataframe whose rows are inserted
	 * @return the number of inserted rows
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public int insertDB(final IScope scope, final Connection conn, final String table_name, final IDataFrame data)
			throws GamaRuntimeException {
		final IList<Object> cols = GamaListFactory.create();
		cols.addAll(data.getColumns());
		int total = 0;
		try (final Statement st = conn.createStatement();) {
			final int nbRows = data.getRows();
			for (int i = 0; i < nbRows; i++) {
				st.addBatch(getInsertString(scope, conn, table_name, cols, data.getRowValues(i)));
			}
			for (final int count : st.executeBatch()) { total += count >= 0 ? count : 1; }
			if (DEBUG.IS_ON()) { DEBUG.OUT("SQLConnection.insertDB inserted " + total + " row(s)"); }
		} catch (final SQLException e) {
			throw GamaRuntimeException.error("SQLConnection.insertDB " + e.toString(), scope);
		}
		return total;
	}

	/**
	 * Inserts every row of a dataframe into a table, opening and closing a connection. See
	 * {@link #insertDB(IScope, Connection, String, IDataFrame)}.
	 *
	 * @param scope
	 *            the scope
	 * @param table_name
	 *            the table name
	 * @param data
	 *            the dataframe whose rows are inserted
	 * @return the number of inserted rows
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public int insertDB(final IScope scope, final String table_name, final IDataFrame data) throws GamaRuntimeException {
		try (Connection conn = connectDB();) {
			return insertDB(scope, conn, table_name, data);
		} catch (final Exception e) {
			throw GamaRuntimeException.error("SQLConnection.insertDB " + e.toString(), scope);
		}
	}

	/**
	 * Inserts a single row into the given columns of a table (column names and values are provided as parallel lists).
	 *
	 * @param scope
	 *            the scope
	 * @param conn
	 *            the connection
	 * @param table_name
	 *            the table name
	 * @param cols
	 *            the column names
	 * @param values
	 *            the values, in the same order as the columns
	 * @return the number of inserted rows
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public int insertDB(final IScope scope, final Connection conn, final String table_name, final IList<Object> cols,
			final IList<Object> values) throws GamaRuntimeException {
		if (values.size() != cols.size())
			throw GamaRuntimeException.error("Size of columns list and values list are not equal", scope);
		try (final Statement st = conn.createStatement();) {
			return st.executeUpdate(getInsertString(scope, conn, table_name, cols, values));
		} catch (final SQLException e) {
			throw GamaRuntimeException.error("SQLConnection.insertDB " + e.toString(), scope);
		}
	}

	/**
	 * Inserts a single row into the given columns of a table, opening and closing a connection. See
	 * {@link #insertDB(IScope, Connection, String, IList, IList)}.
	 *
	 * @param scope
	 *            the scope
	 * @param table_name
	 *            the table name
	 * @param cols
	 *            the column names
	 * @param values
	 *            the values
	 * @return the number of inserted rows
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public int insertDB(final IScope scope, final String table_name, final IList<Object> cols,
			final IList<Object> values) throws GamaRuntimeException {
		try (Connection conn = connectDB();) {
			return insertDB(scope, conn, table_name, cols, values);
		} catch (final Exception e) {
			throw GamaRuntimeException.error("SQLConnection.insertDB " + e.toString(), scope);
		}
	}

	/**
	 * Inserts a single row into a table, providing a value for every column in declaration order.
	 *
	 * @param scope
	 *            the scope
	 * @param conn
	 *            the connection
	 * @param table_name
	 *            the table name
	 * @param values
	 *            one value per column, in the table's column order
	 * @return the number of inserted rows
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public int insertDB(final IScope scope, final Connection conn, final String table_name, final IList<Object> values)
			throws GamaRuntimeException {
		try (final Statement st = conn.createStatement();) {
			return st.executeUpdate(getInsertString(scope, conn, table_name, values));
		} catch (final SQLException e) {
			throw GamaRuntimeException.error("SQLConnection.insertDB " + e.toString(), scope);
		}
	}

	/**
	 * Inserts a single row into a table (one value per column), opening and closing a connection. See
	 * {@link #insertDB(IScope, Connection, String, IList)}.
	 *
	 * @param scope
	 *            the scope
	 * @param table_name
	 *            the table name
	 * @param values
	 *            one value per column, in the table's column order
	 * @return the number of inserted rows
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public int insertDB(final IScope scope, final String table_name, final IList<Object> values)
			throws GamaRuntimeException {
		try (Connection conn = connectDB();) {
			return insertDB(scope, conn, table_name, values);
		} catch (final Exception e) {
			throw GamaRuntimeException.error("SQLConnection.insertDB " + e.toString(), scope);
		}
	}

	/**
	 * Execute query DB.
	 *
	 * @param scope
	 *            the scope
	 * @param conn
	 *            the conn
	 * @param queryStr
	 *            the query str
	 * @param condition_values
	 *            the condition values
	 * @return the i list
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	/*
	 * Executes a parametrized SQL query (with '?' placeholders) and returns the result as a dataframe.
	 */
	public IDataFrame executeQueryDB(final IScope scope, final Connection conn, final String queryStr,
			final IList<Object> condition_values) throws GamaRuntimeException {
		try (final PreparedStatement pstmt = conn.prepareStatement(queryStr);) {
			for (int i = 0; i < condition_values.size(); i++) { pstmt.setObject(i + 1, condition_values.get(i)); }
			try (final ResultSet rs = pstmt.executeQuery();) {
				return buildDataFrame(scope, rs);
			}
		} catch (final SQLException e) {
			throw GamaRuntimeException.error("SQLConnection.executeQueryDB: " + e.toString(), scope);
		}
	}

	/**
	 * Execute query DB.
	 *
	 * @param scope
	 *            the scope
	 * @param queryStr
	 *            the query str
	 * @param condition_values
	 *            the condition values
	 * @return the dataframe
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	/*
	 * Executes a parametrized SQL query (with '?' placeholders), opening and closing a connection, and returns the
	 * result as a dataframe.
	 */
	public IDataFrame executeQueryDB(final IScope scope, final String queryStr, final IList<Object> condition_values)
			throws GamaRuntimeException {
		try (Connection conn = connectDB();) {
			return executeQueryDB(scope, conn, queryStr, condition_values);
		} catch (final Exception e) {
			throw GamaRuntimeException.error("SQLConnection.executeQueryDB: " + e.toString(), scope);
		}
	}

	/**
	 * Execute update DB.
	 *
	 * @param scope
	 *            the scope
	 * @param conn
	 *            the conn
	 * @param queryStr
	 *            the query str
	 * @param condition_values
	 *            the condition values
	 * @return the int
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	/*
	 * @Method: executeUpdateDB(Connection conn,String queryStr, IList<Object> condition_value)
	 *
	 * @Description: Executes the SQL statement in this PreparedStatement object, which must be an SQL INSERT, UPDATE or
	 * DELETE statement; or an SQL statement that returns nothing, such as a DDL statement.
	 *
	 * @param conn: MAP of Connection parameters to RDBM
	 *
	 * @param queryStr: an SQL INSERT, UPDATE or DELETE statement with question mark (?).
	 *
	 * @param condition_values: List of values that are used to assign into conditions of queryStr.
	 *
	 * @return row_count:either (1) the row count for INSERT, UPDATE, or DELETE statements or (2) 0 for SQL statements
	 * that return nothing
	 *
	 * @throws GamaRuntimeException
	 */
	public int executeUpdateDB(final IScope scope, final Connection conn, final String queryStr,
			final IList<Object> condition_values) throws GamaRuntimeException {
		int row_count = -1;
		final int condition_count = condition_values.size();
		try (final PreparedStatement pstmt = conn.prepareStatement(queryStr);) {

			for (int i = 0; i < condition_count; i++) { pstmt.setObject(i + 1, condition_values.get(i)); }
			row_count = pstmt.executeUpdate();

		} catch (final SQLException e) {
			throw GamaRuntimeException.error("SQLConnection.selectDB: " + e.toString(), scope);
		}
		return row_count;
	}

	/**
	 * Execute update DB.
	 *
	 * @param scope
	 *            the scope
	 * @param queryStr
	 *            the query str
	 * @param condition_values
	 *            the condition values
	 * @return the int
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	/*
	 * @Method: executeUpdateDB(Connection conn,String queryStr, IList<Object> condition_value)
	 *
	 * @Description: Executes the SQL statement in this PreparedStatement object, which must be an SQL INSERT, UPDATE or
	 * DELETE statement; or an SQL statement that returns nothing, such as a DDL statement.
	 *
	 * @param queryStr: an SQL INSERT, UPDATE or DELETE statement with question mark (?).
	 *
	 * @param condition_values:
	 *
	 * @return row_count:either (1) the row count for INSERT, UPDATE, or DELETE statements or (2) 0 for SQL statements
	 * that return nothing
	 *
	 * @throws GamaRuntimeException
	 */
	public int executeUpdateDB(final IScope scope, final String queryStr, final IList<Object> condition_values)
			throws GamaRuntimeException {
		int row_count = -1;
		try (Connection conn = connectDB();) {
			row_count = executeUpdateDB(scope, conn, queryStr, condition_values);

			// set value for each condition
		} catch (

		final Exception e) {
			throw GamaRuntimeException.error("SQLConnection.executeUpdateDB: " + e.toString(), scope);
		}
		return row_count;
	}

	/**
	 * Sets the transformed.
	 *
	 * @param tranformed
	 *            the new transformed
	 */
	public void setTransformed(final boolean tranformed) { this.transformed = tranformed; }

}// end of class
