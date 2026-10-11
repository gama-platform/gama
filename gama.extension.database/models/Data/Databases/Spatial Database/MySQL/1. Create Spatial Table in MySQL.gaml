/**
* Name: Create Spatial Table in MySQL
* Author: Truong Minh Thai
* Description: Step 1 of the MySQL spatial database workflow. Creates a MySQL database and a spatial table
*   (building) with a geometry column. The table is structured to store polygon geometries corresponding to
*   GIS building features. Run this model first before using the 'Agents to Database' and 'Agents from
*   Database' MySQL models.
* Tags: database, SQL, MySQL, spatial, geometry, create, GIS
*/
model CreateBuildingTableMySQL


global
{
	map<string, string> PARAMS <- ['host'::'localhost', 'dbtype'::'mysql', 'database'::'', 'port'::'8889', 'user'::'root', 'passwd'::'root'];
	init
	{
		write "This model will work only if the corresponding database is installed" color: #red;

		create test_species number: 1;
		ask test_species {
			if (testConnection (PARAMS)) {
				do executeUpdate (params: PARAMS, updateComm: "DROP DATABASE IF EXISTS spatial_DB_GAMA");						
				do executeUpdate (params: PARAMS, updateComm: "CREATE DATABASE spatial_DB_GAMA");
				write "spatial_BD_GAMA database was created ";
					
				PARAMS["database"] <- "spatial_DB_GAMA";

				do executeUpdate (params: PARAMS, updateComm: "CREATE TABLE bounds" + "( " + " geom GEOMETRY " + ")");
				write "bounds table was created ";
				
				do executeUpdate (params: PARAMS, updateComm: "CREATE TABLE buildings " + "( " + " name VARCHAR(255), " + " type VARCHAR(255), " + " geom GEOMETRY " + ")");
				write "buildings table was created ";
			} else {
				write "Connection to MySQL can not be established ";
			}
		}
	}
}

species test_species skills: [SQLSKILL]  { }

experiment default_expr type: gui { }