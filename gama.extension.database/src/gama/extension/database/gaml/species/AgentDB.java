/*******************************************************************************************************
 *
 * AgentDB.java, in gama.extension.database, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.database.gaml.species;

import java.sql.Connection;
import java.sql.SQLException;

import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.species;
import gama.core.metamodel.agent.GamlAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IList;
import gama.dev.DEBUG;
import gama.extension.database.utils.sql.SqlConnection;
import gama.extension.database.utils.sql.SqlUtils;
import gama.gaml.types.IType;

/**
 * The Class AgentDB.
 */
/*
 * @Author TRUONG Minh Thai
 *
 * @Supervisors: Christophe Sibertin-BLANC Fredric AMBLARD Benoit GAUDOU
 *
 * species: The AgentDB is defined in this class. AgentDB supports the action - isConnected: returns true/false -
 * testConnection: tests the connection - close: closes the current connection - connect: makes a connection to DBMS. -
 * select: executeQuery to select data from DBMS via current connection. - executeUpdate: runs executeUpdate to
 * update/insert/delete/drop/create data on DBMS via current connection.
 *
 */
@species (
		name = "AgentDB",
		doc = @doc ("An abstract species that can be extended to provide agents with capabilities to access databases"))
@doc ("AgentDB is an abstract species that can be extended to provide agents with capabilities to access databases")
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class AgentDB extends GamlAgent {

	/** The conn. */
	private Connection conn = null;

	/** The sql conn. */
	private SqlConnection sqlConn = null;

	/** The is connection. */
	private boolean isConnection = false;

	/** The params. */
	private java.util.Map<String, String> params = null;

	/**
	 * Instantiates a new agent DB.
	 *
	 * @param s
	 *            the s
	 * @param index
	 *            the index
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public AgentDB(final IPopulation s, final int index) throws GamaRuntimeException {
		super(s, index);
	}

	/**
	 * Checks if is connected.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if is connected
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "isConnected",
			doc = @doc (
					value = "To check if connection to the server was successfully established or not.",
					returns = "Returns true if connection to the server was successfully established, otherwise, it returns false."))
	public boolean isConnected(final IScope scope) throws GamaRuntimeException {
		return isConnection;
	}

	/**
	 * Close.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "close",
			doc = @doc (
					value = "Close the established database connection.",
					returns = "Returns null if the connection was successfully closed, otherwise, it returns an error."))
	public Object close(final IScope scope) throws GamaRuntimeException {
		try {
			conn.close();
			isConnection = false;
		} catch (final SQLException e) {
			throw GamaRuntimeException.error("AgentDB.close error:" + e.toString(), scope);
		} catch (final NullPointerException npe) {
			if (conn == null) throw GamaRuntimeException
					.error("AgentDB.close error: cannot close a database connection that does not exist.", scope);
		}
		return null;

	}

	/**
	 * Connect DB.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	/*
	 * Make a connection to BDMS
	 *
	 * @syntax: do action: connectDB { arg params value:[ "dbtype":"SQLSERVER", "url":"host address", "port":
	 * "port number", "database":"database name", "user": "user name", "passwd": "password" ]; }
	 */
	@action (
			name = "connect",
			args = { @arg (
					name = "params",
					type = IType.MAP,
					optional = false,
					doc = @doc ("Connection parameters")) },
			doc = @doc (
					value = "Establish a database connection.",
					returns = "Returns null if connection to the server was successfully established, otherwise, it returns an error."))
	public Object connectDB(final IScope scope) throws GamaRuntimeException {

		params = (java.util.Map<String, String>) scope.getArg("params", IType.MAP);

		if (isConnection)
			throw GamaRuntimeException.error("AgentDB.connection error: a connection is already opened", scope);
		try {
			sqlConn = SqlUtils.createConnectionObject(scope);
			conn = sqlConn.connectDB();
			isConnection = true;
		} catch (final Exception e) {
			throw GamaRuntimeException.error("AgentDB.connect:" + e.toString(), scope);
		}
		return null;
		// ----------------------------------------------------------------------------------------------------------
	}

	/**
	 * Test connection.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	/*
	 * Test a connection to DBMS
	 *
	 * @syntax: testConnection { arg params value:[ "dbtype":"SQLSERVER", "url":"host address", "port": "port number",
	 * "database":"database name", "user": "user name", "passwd": "password", ]; }
	 */
	@action (
			name = "testConnection",
			args = { @arg (
					name = "params",
					type = IType.MAP,
					optional = false,
					doc = @doc ("Connection parameters")) },
			doc = @doc (
					value = "To test a database connection .",
					returns = "Returns true if connection to the server was successfully established, otherwise, it returns false."))
	public boolean testConnection(final IScope scope) throws GamaRuntimeException {
		try (final Connection conn = SqlUtils.createConnectionObject(scope).connectDB()) {} catch (final Exception e) {
			return false;
		}
		return true;
		// ---------------------------------------------------------------------------------------
	}

	/**
	 * Select.
	 *
	 * @param scope
	 *            the scope
	 * @return the i list
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	/*
	 * Make a connection to BDMS and execute the select statement
	 *
	 * @syntax do action: select { arg select value: "select string with question marks"; arg values value [List of
	 * values that are used to replace question marks] }
	 *
	 * @return IList<IList<Object>>
	 */
	@action (
			name = "select",
			args = { @arg (
					name = "select",
					type = IType.STRING,
					optional = false,
					doc = @doc ("select string")),
					@arg (
							name = "values",
							type = IType.LIST,
							optional = true,
							doc = @doc ("List of values that are used to replace question marks")) },
			doc = @doc (
					value = "Make a connection to DBMS and execute the select statement.",
					returns = "Returns the obtained result from executing the select statement."))
	public IList select(final IScope scope) throws GamaRuntimeException {

		if (!isConnection) throw GamaRuntimeException.error("AgentDB.select: Connection was not established ", scope);
		final String selectComm = (String) scope.getArg("select", IType.STRING);
		final IList<Object> values = (IList<Object>) scope.getArg("values", IType.LIST);
		IList<? super IList<? super IList>> repRequest;
		// get data
		try {
			if (values.size() > 0) {
				repRequest = sqlConn.executeQueryDB(scope, conn, selectComm, values);
			} else {
				repRequest = sqlConn.selectDB(scope, conn, selectComm);
			}
			return repRequest;
		} catch (final Exception e) {
			e.printStackTrace();
			throw GamaRuntimeException.error("AgentDB.select: " + e.toString(), scope);
		}
		// --------------------------------------------------------------------------------------------------

	}

	/**
	 * Execute update.
	 *
	 * @param scope
	 *            the scope
	 * @return the int
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	/*
	 * - Make a connection to BDMS - Executes the SQL statement in this PreparedStatement object, which must be an SQL
	 * INSERT, UPDATE or DELETE statement; or an SQL statement that returns nothing, such as a DDL statement.
	 *
	 * @syntax: do action: executeUpdate { arg updateComm value: " SQL statement string with question marks" arg values
	 * value [List of values that are used to replace question marks] }
	 */
	@action (
			name = "executeUpdate",
			args = { @arg (
					name = "updateComm",
					type = IType.STRING,
					optional = false,
					doc = @doc ("SQL commands such as Create, Update, Delete, Drop with question mark")),
					@arg (
							name = "values",
							type = IType.LIST,
							optional = true,
							doc = @doc ("List of values that are used to replace question mark")) },
			doc = @doc (
					value = "- Make a connection to DBMS - Executes the SQL statement in this PreparedStatement object, which must be an SQL\n"
							+ "	 INSERT, UPDATE or DELETE statement; or an SQL statement that returns nothing, such as a DDL statement.",
					returns = "Returns the number of updated rows. "))
	public int executeUpdate(final IScope scope) throws GamaRuntimeException {

		if (!isConnection) throw GamaRuntimeException.error("AgentDB.select: Connection was not established ", scope);
		final String updateComm = (String) scope.getArg("updateComm", IType.STRING);
		final IList<Object> values = (IList<Object>) scope.getArg("values", IType.LIST);

		int row_count = -1;
		// get data
		try {
			if (values.size() > 0) {
				row_count = sqlConn.executeUpdateDB(scope, conn, updateComm, values);
			} else {
				row_count = sqlConn.executeUpdateDB(scope, conn, updateComm);
			}

		} catch (final Exception e) {
			e.printStackTrace();
			throw GamaRuntimeException.error("AgentDB.executeUpdate: " + e.toString(), scope);
		}
		if (DEBUG.IS_ON()) { DEBUG.OUT(updateComm + " was run"); }

		return row_count;
		// ----------------------------------------------------------------------------------------------------
	}

	/**
	 * Gets the paramater.
	 *
	 * @param scope
	 *            the scope
	 * @return the paramater
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "getParameter",
			args = {},
			doc = @doc (
					value = "Returns the list used parameters to make a connection to DBMS (dbtype, url, port, database, user and passwd).",
					returns = "Returns the list of used parameters to make a connection to DBMS. "))
	public Object getParamater(final IScope scope) throws GamaRuntimeException {
		return params;
	}

	/**
	 * Sets the parameter.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "setParameter",
			args = { @arg (
					name = "params",
					type = IType.MAP,
					optional = false,
					doc = @doc ("Connection parameters")) },
			doc = @doc (
					value = "Sets the parameters to use in order to make a connection to the DBMS (dbtype, url, port, database, user and passwd).",
					returns = "null. "))
	public Object setParameter(final IScope scope) throws GamaRuntimeException {
		params = (java.util.Map<String, String>) scope.getArg("params", IType.MAP);

		if (isConnection) {
			try {
				conn.close();
				isConnection = false;
			} catch (final SQLException e) {
				// e.printStackTrace();
				throw GamaRuntimeException.error("AgentDB.close error:" + e.toString(), scope);
			}
		}
		return null;
	}

	/**
	 * Insert.
	 *
	 * @param scope
	 *            the scope
	 * @return the int
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	/*
	 * Make a connection to BDMS and execute the insert statement
	 *
	 * @syntax do insert with: [into:: table_name, columns:column_list, values:value_list];
	 *
	 * @return an integer
	 */
	@action (
			name = "insert",
			args = { @arg (
					name = "into",
					type = IType.STRING,
					optional = false,
					doc = @doc ("Table name")),
					@arg (
							name = "columns",
							type = IType.LIST,
							optional = true,
							doc = @doc ("List of column name of table")),
					@arg (
							name = "values",
							type = IType.LIST,
							optional = false,
							doc = @doc ("List of values that are used to insert into table. Columns and values must have same size")) },
			doc = @doc (
					value = "- Make a connection to DBMS - Executes the insert statement.",
					returns = "Returns the number of updated rows. "))
	public int insert(final IScope scope) throws GamaRuntimeException {

		if (!isConnection) throw GamaRuntimeException.error("AgentDB.select: Connection was not established ", scope);
		final String table_name = (String) scope.getArg("into", IType.STRING);
		final IList<Object> cols = (IList<Object>) scope.getArg("columns", IType.LIST);
		final IList<Object> values = (IList<Object>) scope.getArg("values", IType.LIST);
		int rec_no = -1;

		try {
			if (cols.size() > 0) {
				rec_no = sqlConn.insertDB(scope, conn, table_name, cols, values);
			} else {
				rec_no = sqlConn.insertDB(scope, conn, table_name, values);
			}
		} catch (final Exception e) {
			e.printStackTrace();
			throw GamaRuntimeException.error("AgentDB.insert: " + e.toString(), scope);
		}
		if (DEBUG.IS_ON()) { DEBUG.OUT("Insert into " + " was run"); }

		return rec_no;
	}
	// -----------------------------------------------------------------------------------------------------
}
