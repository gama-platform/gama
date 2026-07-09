/**
* Name: Agents to Database in MySQL
* Author: Truong Minh Thai
* Description: Step 2 of the MySQL spatial database workflow. Loads building shapes from a shapefile
*   (via QGis-exported table vnm_adm2), creates building agents, then saves their geometry and attributes
*   back to the MySQL spatial table. The 'transform' parameter converts geometries from GAMA's absolute
*   coordinate system to the GIS CRS. Requires the spatial_DB_GAMA database created by model 1.
* Tags: database, SQL, MySQL, spatial, geometry, shapefile, GIS, save, agents
*/
model agent2DB_MySQL

global { 
	file buildingsShp <- file('../../includes/building.shp');
	file boundsShp <- file('../../includes/bounds.shp');
	geometry shape <- envelope(boundsShp);
	
	map<string,string> PARAMS <- ['srid'::'4326',
				                  'host'::'localhost','dbtype'::'mysql','database'::'spatial_DB_GAMA',
				                  'port'::'8889','user'::'root','passwd'::'root'];

	init {
		write "This model will work only if the corresponding database is installed" color: #red;
		write "The model \"Create Spatial Table in MySQL.gaml\" can be run previously to create the table.";
		
		create buildings(type:string(read ('NATURE'))) from: buildingsShp;
		create bounds from: boundsShp;
		
		create DB_Accessor number: 1
		{
			do executeUpdate (params: PARAMS, updateComm: "DELETE FROM buildings");
			do executeUpdate (params: PARAMS, updateComm: "DELETE FROM bounds");
		}
		write "Click on <<Step>> button to save data of agents to DB";
	}

	// All agents of each species are saved in a single batch insert, by building a dataframe
	// whose 'geom' column holds their geometries.
	reflex save_to_db when: cycle = 1 {
		ask first(DB_Accessor) {
			do insert (params: PARAMS, into: "bounds",
				data: dataframe_with(["geom"], bounds collect [each.shape]));
			do insert (params: PARAMS, into: "buildings",
				data: dataframe_with(["name", "type", "geom"], buildings collect [each.name, each.type, each.shape]));
		}
		write "" + length(bounds) + " bound(s) and " + length(buildings) + " building(s) saved to the database.";
		do pause();
	}
}

species DB_Accessor skills: [SQLSKILL] ;

species bounds ;

species buildings {
	string type;

	aspect default {
		draw shape color: #gray ;
	}
}

experiment default_expr type: gui {
	output {
		
		display GlobalView {
			species buildings aspect: default;
		}
	}
}

