/**
* Name: Agents from Database in PostGIS
* Author: Benoit Gaudou
* Description: Step 3 of the PostGIS spatial database workflow. Executes a spatial SQL SELECT query against
*   the PostGIS database and instantiates GAMA agents from each row result. Agent geometries are loaded
*   directly from the PostGIS geometry column and transformed to GAMA's coordinate system. Demonstrates how
*   to populate georeferenced agent populations from a PostGIS spatial database.
* Tags: database, SQL, PostGIS, PostgreSQL, spatial, geometry, agents, create, GIS, select
*/

model DB2agentPOSTGIS 

global {
	map<string,string> BOUNDS <- [	//'srid'::'32648', // optional
	 								'host'::'localhost',
									'dbtype'::'postgis',
									'database'::'spatial_db',
									'port'::'5434',
									'user'::'postgres',
									'passwd'::'',
								  	'select'::'SELECT ST_AsEWKB(geom) as geom FROM bounds;' ];
	map<string,string> PARAMS <- [	//'srid'::'32648', // optional
									'host'::'localhost',
									'dbtype'::'postgis',
									'database'::'spatial_db',
									'port'::'5434',
									'user'::'postgres',
									'passwd'::''];
	
	string QUERY <- "SELECT type, ST_AsEWKB(geom) as geom FROM buildings;";
	geometry shape <- envelope(BOUNDS);		  	
		  	
	init {
		write "This model will work only if the corresponding database is installed and initialized." color:#red;
		write "To this purpose, the following models can run first: ";
		write "     - \"Create Spatial Table in PostGIS.gaml\" to create the database,";		
		write "     - \"Agents to Database in PostGIS.gaml\" to insert data in the database.";
		write "";		
		
		create DB_accessor {
			create buildings from: select(PARAMS, QUERY)
							 with:( nature:"type", shape:"geom");
		 }
		 write "Buildings created: "+length(buildings) ;
	}
}

species DB_accessor skills: [SQLSKILL];

species buildings {
	string nature;
	aspect default {
		draw shape color: #gray ;
	}	
}	

experiment DB2agentPOSTGIS type: gui {
	output {
		display fullView type:3d{
			species buildings aspect: default;
		}
	}
}
