package gama.extension.database.utils.sql;

import java.util.Map;

import gama.core.runtime.IScope;

public interface ISqlConnector {
	SqlConnection connection(final IScope scope, final String venderName, final String url, final String port, final String dbName,
			final String userName, final String password, final Boolean transformed);

	Map<String, Object> getConnectionParameters(final IScope scope, String host, String dbtype, String port, String database, String user, String passwd);
	
}
