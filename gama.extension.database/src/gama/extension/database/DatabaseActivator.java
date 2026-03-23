/*******************************************************************************************************
 *
 * DatabaseActivator.java, in gama.extension.database, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.database;

import org.osgi.framework.BundleContext;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.topology.IProjection;
import gama.api.types.list.IList;
import gama.api.types.map.IMap;
import gama.api.utils.geometry.GamaEnvelopeFactory;
import gama.api.utils.geometry.IEnvelope;
import gama.dependencies.GamaBundleActivator;
import gama.extension.database.utils.sql.SqlConnection;
import gama.extension.database.utils.sql.SqlUtils;
import gama.extension.database.utils.sql.mysql.MySqlConnection;
import gama.extension.database.utils.sql.mysql.MySqlConnector;
import gama.extension.database.utils.sql.postgres.PostgresConnection;
import gama.extension.database.utils.sql.postgres.PostgresConnector;
import gama.extension.database.utils.sql.sqlite.SqliteConnection;
import gama.extension.database.utils.sql.sqlite.SqliteConnector;

/**
 * The Class DatabaseActivator.
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class DatabaseActivator extends GamaBundleActivator {

	/**
	 * Start.
	 *
	 * @param context
	 *            the context
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public void initialize(final BundleContext context) {
		SqlUtils.externalConnectors.put(MySqlConnection.MYSQL, new MySqlConnector());
		SqlUtils.externalConnectors.put(PostgresConnection.POSTGRES, new PostgresConnector());
		SqlUtils.externalConnectors.put(PostgresConnection.POSTGIS, new PostgresConnector());
		SqlUtils.externalConnectors.put(SqliteConnection.SQLITE, new SqliteConnector());

		GamaEnvelopeFactory.addEnvelopeComputer((scope, obj) -> {

			if (!(obj instanceof IMap)) return null;
			final IMap<String, Object> params = (IMap<String, Object>) obj;

			IEnvelope env = null;
			try (SqlConnection sqlConn = SqlUtils.createConnectionObject(scope, params)) {
				// create connection
				// sqlConn = SqlUtils.createConnectionObject(scope, params);
				// get data
				final IList gamaList = sqlConn.selectDB(scope, (String) params.get("select"));
				env = SqlConnection.getBounds(gamaList);

				IProjection gis = scope.getSimulation().getProjectionFactory().fromParams(scope, params, env);
				env = gis.getProjectedEnvelope();
			} catch (Exception e) {
				throw GamaRuntimeException.error("Error in creating the world envelope from DataBase.", scope);
			}
			return env;

		});

	}

}
