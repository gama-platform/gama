/*******************************************************************************************************
 *
 * Activator.java, in gama.extension.database, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.extension.database;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import gama.core.common.geometry.Envelope3D;
import gama.core.common.geometry.GeometryUtils;
import gama.core.metamodel.topology.projection.IProjection;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.extension.database.utils.sql.SqlConnection;
import gama.extension.database.utils.sql.SqlUtils;
import gama.extension.database.utils.sql.mysql.MySqlConnection;
import gama.extension.database.utils.sql.mysql.MySqlConnector;
import gama.extension.database.utils.sql.postgres.PostgresConnection;
import gama.extension.database.utils.sql.postgres.PostgresConnector;
import gama.extension.database.utils.sql.sqlite.SqliteConnection;
import gama.extension.database.utils.sql.sqlite.SqliteConnector;

/**
 * The Class Activator.
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class Activator implements BundleActivator {

	@Override
	public void start(final BundleContext context) throws Exception {
		SqlUtils.externalConnectors.put(MySqlConnection.MYSQL,new MySqlConnector());		
		SqlUtils.externalConnectors.put(PostgresConnection.POSTGRES,new PostgresConnector());		
		SqlUtils.externalConnectors.put(PostgresConnection.POSTGIS,new PostgresConnector());				
		SqlUtils.externalConnectors.put(SqliteConnection.SQLITE,new SqliteConnector());		

		
		GeometryUtils.addEnvelopeComputer((scope, obj) -> {

			if (!(obj instanceof IMap)) { return null; }
			final IMap<String, Object> params = (IMap<String, Object>) obj;
			
			Envelope3D env = null;
			try(SqlConnection sqlConn = SqlUtils.createConnectionObject(scope, params)) {
			 // create connection
			 // sqlConn = SqlUtils.createConnectionObject(scope, params);
			 // get data
				final IList gamaList = sqlConn.selectDB(scope, (String) params.get("select"));
				env = SqlConnection.getBounds(gamaList);

				IProjection gis;
				gis = scope.getSimulation().getProjectionFactory().fromParams(scope, params, env);
				env = gis.getProjectedEnvelope();
			} catch(Exception e ) {
				throw GamaRuntimeException.error("Error in creating the world envelope from DataBase.",scope);				
			}
			return env;
			// ----------------------------------------------------------------------------------------------------

		});

	}

	@Override
	public void stop(final BundleContext context) throws Exception {}

}
