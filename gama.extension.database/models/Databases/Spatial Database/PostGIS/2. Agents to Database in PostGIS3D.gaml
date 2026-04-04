/**
* Name: Agents to Database in PostGIS (3D)
* Author: Truong Minh Thai, Quang Truong, Thu Tran
* Description: Variant of the PostGIS 'Agents to Database' workflow for 3D Multipolygon geometry columns.
*   Saves agent geometries (with Z coordinates) and attributes into a PostGIS table where the geometry
*   column type is Multipolygon3D. Requires the spatial database and tables created by model 1. Use this
*   variant when your spatial data has elevation or height attributes that must be stored as 3D geometry.
* Tags: database, SQL, PostGIS, PostgreSQL, spatial, geometry, 3d, GIS, save, agents
*/

model agent2DB_POSTGIS 
  
global { 
	file districtShp <- file('../../includes/district.shp') ;
	geometry shape <- envelope(districtShp);
	 
	map<string,string> PARAMS <-  ['srid'::'32846', // 32648 represents for the WGS 84 - Zone 48 Northern.
								   'host'::'localhost','dbtype'::'postgres','database'::'spatial_db3d',
								   'port'::'5434','user'::'postgres','passwd'::''];

	init {
		write "This model will work only if the corresponding database is installed" color:#red;
		write "The model \"Create Spatial Table in PostGIS.gaml\" can be run previously to create the database and tables. The model should be modified to create the database spatial_db3d.";
		
		create district from: districtShp with: (ward_name:string(read ('Ward_name')));
		write "Click on <<Step>> button to save data of agents to DB";
		
		create DB_Accessor
		{ 			
			do executeUpdate (params: PARAMS, updateComm: "DELETE FROM buildings");	
		}
	}
}   
  
species DB_Accessor skills: [SQLSKILL] ;   

species district {
	string ward_name;
	
	reflex printdata{
		write " name : " + (name) ;
	}
	reflex savetosql{  // save data into Postgres
		write "begin"+ name;
	    ask (DB_Accessor) {
	    	// Using  ST_Force3D() for converting the geometry data to 3D in the case that the geom in table is in 3D      
			do executeUpdate (params: PARAMS, updateComm: "INSERT INTO buildings(name,geom) VALUES('"+myself.ward_name+"',ST_Force3D(ST_Multi(ST_GeomFromText('" + myself.shape +"',32648))))");
		}	
	}	
	aspect default {
		draw shape color: #gray ;
	}
}   

experiment default_expr type: gui {
	output {
		
		display GlobalView {
			species district aspect: default;
		}
	}
}

