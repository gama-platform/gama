/*******************************************************************************************************
 *
 * MySqlConnector.java, in gama.extension.database, is part of the source code of the GAMA modeling and simulation
 * platform (v.2026-11).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package gama.extension.database.utils.sql.mysql;

import java.util.HashMap;
import java.util.Map;

import org.geotools.data.mysql.MySQLDataStoreFactory;

import gama.api.runtime.scope.IScope;
import gama.extension.database.utils.sql.ISqlConnector;
import gama.extension.database.utils.sql.SqlConnection;

public class MySqlConnector implements ISqlConnector{

	@Override
	public SqlConnection connection(final IScope scope, String venderName, String url, String port, String dbName, String userName,
			String password, Boolean transformed) {
		return new MySqlConnection(scope, venderName, url, port, dbName, userName, password, transformed);
	}

	@Override
	public Map<String, Object> getConnectionParameters(final IScope scope, String host, String dbtype, String port, String database,
			String user, String passwd) {

		Map<String,Object> params = new HashMap<>();
		params.put(MySQLDataStoreFactory.DBTYPE.key, dbtype);
		params.put(MySQLDataStoreFactory.HOST.key, host);
		params.put(MySQLDataStoreFactory.PORT.key, Integer.valueOf(port));
		params.put(MySQLDataStoreFactory.DATABASE.key, database);
		params.put(MySQLDataStoreFactory.USER.key, user);
		params.put(MySQLDataStoreFactory.PASSWD.key, passwd);
		
		return params;
	}

}
