/*******************************************************************************************************
 *
 * SQLSkill.java, in gama.extension.database, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.database.gaml.skills;

import java.sql.Connection;

import gama.annotations.action;
import gama.annotations.arg;
import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.skill;
import gama.annotations.support.IConcept;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.kernel.skill.Skill;
import gama.api.runtime.scope.IScope;
import gama.api.types.dataframe.IDataFrame;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.IMap;
import gama.dev.DEBUG;
import gama.extension.database.utils.sql.SqlConnection;
import gama.extension.database.utils.sql.SqlUtils;

/**
 * The Class SQLSkill.
 */
/*
 * @Author TRUONG Minh Thai
 *
 * @Supervisors: Christophe Sibertin-BLANC Fredric AMBLARD Benoit GAUDOU
 *
 *
 * insert action as key of arg "Param" 01-Aug-2014: Add date time functions: getCurrentDateTime: get system datetime
 * getDateOffset: get (datetime + offsettime) 21-Sept-2022 : remove getCurrentDateTime, timeStamp and getDateOffset as
 * they are irrelevant to databases Last Modified: 01-Aug-2014
 */
@skill (
		name = "SQLSKILL",
		concept = { IConcept.DATABASE, IConcept.SKILL })
@SuppressWarnings ({ "rawtypes", "unchecked" })
@doc ("This skill allows agents to be provided with actions and attributes in order to connect to SQL databases")
public class SQLSkill extends Skill {

	/**
	 * Test connection.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 */
	/*
	 * Make a connection to BDMS
	 *
	 * @syntax: do action: connectDB { arg params value:[ "dbtype":"SQLSERVER", //MySQL/sqlserver/sqlite
	 * "url":"host address", "port":"port number", "database":"database name", "user": "user name", "passwd": "password"
	 * ]; }
	 */
	@action (
			name = "testConnection",
			args = { @arg (
					name = "params",
					type = IType.MAP,
					optional = false,
					doc = @doc ("Connection parameters")) },
			doc = @doc (
					value = "Action used to test the connection to a database",
					examples = { @example ("""
							if (!first(DB_Accessor).testConnection(PARAMS)) {\r
										write "Connection impossible";\r
										do pause;\r
									}\r
							""") }))
	public boolean testConnection(final IScope scope) {

		try (final Connection conn = SqlUtils.createConnectionObject(scope).connectDB()) {} catch (final Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Execute update QM.
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
	 * @syntax: do action: executeUpdate { arg params value:[ "dbtype":"MSSQL", "url":"host address",
	 * "port":"port number", "database":"database name", "user": "user name", "passwd": "password", ], arg updateComm
	 * value: " SQL statement string with question marks" arg values value [List of values that are used to replace
	 * question marks] }
	 */
	@action (
			name = "executeUpdate",
			args = { @arg (
					name = "params",
					type = IType.MAP,
					optional = false,
					doc = @doc ("Connection parameters")),
					@arg (
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
					value = "Action used to execute any update query (CREATE, DROP, INSERT...) to the database (query written in SQL).",
					examples = {
							@example ("do executeUpdate params: PARAMS updateComm: \"DROP TABLE IF EXISTS registration\";"),
							@example ("do executeUpdate params: PARAMS updateComm: \"INSERT INTO registration \" + \"VALUES(100, 'Zara', 'Ali', 18);\";"),
							@example ("do executeUpdate params: PARAMS updateComm: \"INSERT INTO registration \" + \"VALUES(?, ?, ?, ?);\" values: [101, 'Mr', 'Mme', 45];") }))
	public int executeUpdate_QM(final IScope scope) throws GamaRuntimeException {

		final String updateComm = (String) scope.getArg("updateComm", IType.STRING);
		final IList<Object> values = (IList<Object>) scope.getArg("values", IType.LIST);
		int row_count = -1;
		try (final SqlConnection sqlConn = SqlUtils.createConnectionObject(scope);) {
			if (values.size() > 0) {
				row_count = sqlConn.executeUpdateDB(scope, updateComm, values);
			} else {
				row_count = sqlConn.executeUpdateDB(scope, updateComm);
			}

		} catch (final Exception e) {
			e.printStackTrace();
			throw GamaRuntimeException.error("SQLSkill.executeUpdateDB: " + e.toString(), scope);
		}
		DEBUG.OUT(updateComm + " was run");

		return row_count;
		// ------------------------------------------------------------------------------------------

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
					name = "params",
					type = IType.MAP,
					optional = false,
					doc = @doc ("Connection parameters")),
					@arg (
							name = "into",
							type = IType.STRING,
							optional = false,
							doc = @doc ("Table name")),
					@arg (
							name = "data",
							type = IType.NONE,
							optional = false,
							doc = @doc ("The data to insert. A dataframe inserts all its rows in a single batch (columns = dataframe column names). A map inserts a single row (keys = columns, values = values). A list inserts a single row, one value per column in the table's declaration order.")) },
			doc = @doc (
					value = "Action used to insert data into a database table. Accepts a dataframe (several rows, batched), a map (a single named-column row) or a list (a single positional row).",
					examples = {
							@example ("do insert params: PARAMS into: \"registration\" data: dataframe_with([\"id\",\"first\",\"last\",\"age\"], [[102,'Mahnaz','Fatma',25],[103,'Zaid','Kha',30]]);"),
							@example ("do insert params: PARAMS into: \"registration\" data: [104, 'Bill', 'Clark', 40];"),
							@example ("do insert params: PARAMS into: \"registration\" data: [\"id\"::105, \"first\"::\"Zara\", \"last\"::\"Ali\"];") }))
	public int insert(final IScope scope) throws GamaRuntimeException {

		final String table_name = (String) scope.getArg("into", IType.STRING);
		final Object data = scope.getArg("data", IType.NONE);
		try (final SqlConnection sqlConn = SqlUtils.createConnectionObject(scope);) {
			return insertData(scope, sqlConn, table_name, data);
		} catch (final Exception e) {
			throw GamaRuntimeException.error("SQLSkill.insert: " + e.toString(), scope);
		}
	}

	/**
	 * Dispatches an insert depending on the runtime type of the data: dataframe (batch of rows), map (one named-column
	 * row) or list (one positional row).
	 *
	 * @param scope
	 *            the scope
	 * @param sqlConn
	 *            the connection
	 * @param table
	 *            the table name
	 * @param data
	 *            the data to insert (dataframe, map or list)
	 * @return the number of inserted rows
	 */
	static int insertData(final IScope scope, final SqlConnection sqlConn, final String table, final Object data) {
		if (data instanceof IDataFrame df) return sqlConn.insertDB(scope, table, df);
		if (data instanceof IMap map) {
			final IList<Object> cols = GamaListFactory.create();
			cols.addAll(map.getKeys());
			final IList<Object> values = GamaListFactory.create();
			values.addAll(map.getValues());
			return sqlConn.insertDB(scope, table, cols, values);
		}
		if (data instanceof IList values) return sqlConn.insertDB(scope, table, (IList<Object>) values);
		throw GamaRuntimeException.error(
				"insert: the 'data' argument must be a dataframe, a map or a list, but was " + data, scope);
	}

	/**
	 * Select QM.
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
	 * @syntax do action: select { arg params value:[ "dbtype":"SQLSERVER", "url":"host address", "port":"port number",
	 * "database":"database name", "user": "user name", "passwd": "password" ]; arg select value:
	 * "select string with question marks"; arg values value [List of values that are used to replace question marks] }
	 *
	 * @return IList<IList<Object>>
	 */
	@action (
			name = "select",
			args = { @arg (
					name = "params",
					type = IType.MAP,
					optional = false,
					doc = @doc ("Connection parameters")),
					@arg (
							name = "select",
							type = IType.STRING,
							optional = false,
							doc = @doc ("select string with question marks")),
					@arg (
							name = "values",
							type = IType.LIST,
							optional = true,
							doc = @doc ("List of values that are used to replace question marks")) },
			doc = @doc (
					value = "Action used to retrieve data from a database. The result is returned as a dataframe.",
					examples = {
							@example ("dataframe t <- select(PARAMS, \"SELECT * FROM registration\");") }))
	public IDataFrame select_QM(final IScope scope) throws GamaRuntimeException {

		final String selectComm = (String) scope.getArg("select", IType.STRING);
		final IList<Object> values = (IList<Object>) scope.getArg("values", IType.LIST);

		try (final SqlConnection sqlConn = SqlUtils.createConnectionObject(scope);) {
			return values.size() > 0 ? sqlConn.executeQueryDB(scope, selectComm, values)
					: sqlConn.selectDB(scope, selectComm);
		} catch (final Exception e) {
			throw GamaRuntimeException.error("SQLSkill.select_QM: " + e.toString(), scope);
		}
	}
}
