/**
* Name: Agents to Database in PostGIS (2D)
* Author: Truong Minh Thai
* Description: Variant of the PostGIS 'Agents to Database' workflow for 2D Multipolygon geometry columns.
*   Saves agent geometries and attributes into a PostGIS table where the geometry column type is
*   Multipolygon (2D). Requires the spatial database and tables created by model 1. Use this variant
*   when your PostGIS table stores 2D geometries; use the 3D variant for 3D geometry columns.
* Tags: database, SQL, PostGIS, PostgreSQL, spatial, geometry, 2d, GIS, save, agents
*/

model agent2DB_POSTGIS 
  
global { 
	file buildingsShp <- file('../../includes/building.shp') ;
	geometry shape <- envelope(buildingsShp);
	 
	map<string,string> PARAMS <-  ['srid'::'4326', // optinal postgis
								   'host'::'localhost','dbtype'::'postgres','database'::'spatial_db2d',
								   'port'::'5434','user'::'postgres','passwd'::''];

	init {
		write "This model will work only if the corresponding database is installed" color: #red;
		write "The model \"Create Spatial Table in PostGIS.gaml\" can be run previously to create the database and tables. The model should be modified to create the database spatial_db2d.";
		
		create buildings(type:string(read ('NATURE'))) from: buildingsShp;
		write "Click on <<Step>> button to save data of agents to DB";
		
		create DB_Accessor
		{ 			
			do executeUpdate (params: PARAMS, updateComm: "DELETE FROM buildings");	
		} 
	}
}   
  
species DB_Accessor skills: [SQLSKILL] ;   

species buildings {
	string type;
	
	reflex printdata{
		write " name : " + (name) + "; type: " + (type) + "shape:" + shape;
	}
//	
	reflex savetosql{  // save data into Postgres
		write "begin"+ name;
	    ask (DB_Accessor) {
			do executeUpdate (params: PARAMS, updateComm: "INSERT INTO buildings(type,geom) VALUES('" + myself.type + "',ST_Multi(ST_GeomFromText('" + myself.shape +"',4326)))");
		}	
	}	
	
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

