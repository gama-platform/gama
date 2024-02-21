package gama.extension.database.utils.sql.sqlite;

import java.util.HashMap;
import java.util.Map;

import gama.core.common.util.FileUtils;
import gama.core.runtime.IScope;
import gama.extension.database.utils.sql.ISqlConnector;
import gama.extension.database.utils.sql.SqlConnection;

public class SqliteConnector implements ISqlConnector {

	@Override
	public SqlConnection connection(final IScope scope, String venderName, String url, String port, String dbName, String userName,
			String password, Boolean transformed) {
		SqlConnection sqlConn;
		
		final String DBRelativeLocation = FileUtils.constructAbsoluteFilePath(scope, dbName, true);
		sqlConn = new SqliteConnection(venderName, DBRelativeLocation, transformed);
		
		return sqlConn;
	}

	@Override
	public Map<String, Object> getConnectionParameters(final IScope scope, String host, String dbtype, String port, String database,
			String user, String passwd) {

		Map<String,Object> params = new HashMap<>();
		
		final String DBRelativeLocation = FileUtils.constructAbsoluteFilePath(scope, database, true);
		params.put("dbtype", "sqlite");
		params.put("database", DBRelativeLocation);

		return params;
	}

}
