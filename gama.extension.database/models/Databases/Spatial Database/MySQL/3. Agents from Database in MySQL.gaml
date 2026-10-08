/**
* Name: Agents from Database in MySQL
* Author: Benoit Gaudou
* Description: Step 3 of the MySQL spatial database workflow. Executes a spatial SQL SELECT query against
*   the MySQL spatial table and creates GAMA agents from the results. Each row becomes an agent with
*   geometry and attribute columns mapped to agent variables. Demonstrates how to load georeferenced agent
*   populations from a relational spatial database, complementing shapefile-based loading.
* Tags: database, SQL, MySQL, spatial, geometry, agents, create, GIS, select
*/

model DB2agentMySQL

global {
	map<string,string> BOUNDS <- [	//'srid'::'32648', // optional
									'host'::'localhost',
									'dbtype'::'mysql',
									'database'::'spatial_DB_GAMA',
									'port'::'8889',
									'user'::'root',
									'passwd'::'root',
								  	"select"::"SELECT geom FROM bounds;" ];
	map<string,string> PARAMS <- [	//'srid'::'32648', // optional
									'host'::'localhost',
									'dbtype'::'mysql',
									'database'::'spatial_DB_GAMA',
									'port'::'8889',
									'user'::'root',
									'passwd'::'root'];
	
	string QUERY <- "SELECT name, type, geom FROM buildings ;";
	geometry shape <- envelope(BOUNDS);		  	
	 	
	init {
		write "This model will work only if the corresponding database is installed and contains proper data." color: #red;
		write "To this purpose, the following models can run first: ";
		write "     - \"Create Spatial Table in MySQL.gaml\" to create the database,";
		write "     - \"Agents to Database in MySQL.gaml\" to insert data in the database.";
		write "";
		
		create DB_accessor {
			create buildings( type:"type", shape: "geom") from: select(PARAMS,QUERY) ;
		 }
	}
}


species DB_accessor skills: [SQLSKILL];

species buildings {
	string type;
	aspect default {
		draw shape color: #gray ;
	}	
}	

experiment DB2agentMySQL type: gui {
	output {
		display fullView {
			species buildings aspect: default;
		}
	}
}
