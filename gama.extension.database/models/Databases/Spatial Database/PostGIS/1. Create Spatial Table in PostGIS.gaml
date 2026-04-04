/**
* Name: Create Spatial Table in PostGIS
* Author: Truong Minh Thai
* Description: Step 1 of the PostGIS spatial database workflow. Creates a PostgreSQL/PostGIS database and
*   a spatial table with a geometry column for storing building polygons. Includes both 2D and 3D table
*   variants. Run this model first before using 'Agents to Database in PostGIS' models. Requires a running
*   PostgreSQL server with the PostGIS extension enabled.
* Tags: database, SQL, PostGIS, PostgreSQL, spatial, geometry, create, GIS, 3d
*/
model CreateBuildingTablePostGIS

global {
	map<string, string> PARAMS <- ['host'::'localhost', 'dbtype'::'postgres', 'database'::'', 'port'::'5434', 'user'::'postgres', 'passwd'::''];
	string database_name <- "spatial_db"; // "spatial_db2d" or "spatial_db3d"

	init {
		write "This model will work only if the corresponding database is installed." color: #red;
		write "Note that for postgresql/postgis, a template database with postgis extension should be created previously.";
		write "With Postgresql 10, with pgAdmin 4: ";
		write "   - create a database named `template_postgis`,";
		write "   - open the Query tool (by right-clicking on the template_postgis database),";
		write "   - execute the code:  `CREATE EXTENSION postgis;`";
		write " pgAdmin 4 should be closed before trying to connect to the database from GAMA.";
		write "";
		create dummy;
		ask dummy {
			if (testConnection(PARAMS)) {
				do executeUpdate (params: PARAMS, updateComm: "DROP DATABASE IF EXISTS " + database_name + " ;");
				do executeUpdate (params: PARAMS, updateComm: "CREATE DATABASE "+ database_name +" with TEMPLATE = template1;");
				write "spatial_BD database has been created. ";

				// remove "database" from: PARAMS;
				put database_name key: "database" in: PARAMS;
				do executeUpdate (params: PARAMS, updateComm: "CREATE TABLE bounds" + "( " + " geom GEOMETRY " + ")");
				write "bounds table has been created.";
				do executeUpdate (params: PARAMS, updateComm: "CREATE TABLE buildings " + "( " + " name character varying(255), " + " type character varying(255), " + " geom GEOMETRY " + ")");
				write "buildings table has been created. ";
			} else {
				write "Connection to POSTGRESQL cannot be established ";
			}

		}

	}

}

species dummy skills: [SQLSKILL] {
}

experiment default_expr type: gui {
}