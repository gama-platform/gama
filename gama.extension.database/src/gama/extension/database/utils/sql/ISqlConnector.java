/*******************************************************************************************************
 *
 * ISqlConnector.java, in gama.extension.database, is part of the source code of the GAMA modeling and simulation
 * platform (v.2026-11).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package gama.extension.database.utils.sql;

import java.util.Map;

import gama.api.runtime.scope.IScope;

public interface ISqlConnector {
	SqlConnection connection(final IScope scope, final String venderName, final String url, final String port, final String dbName,
			final String userName, final String password, final Boolean transformed);

	Map<String, Object> getConnectionParameters(final IScope scope, String host, String dbtype, String port, String database, String user, String passwd);
	
}
