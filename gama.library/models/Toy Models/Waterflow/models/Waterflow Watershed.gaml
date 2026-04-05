/***
* Name: Waterflow Watershed
* Author: Benoit Gaudou
* Description: A watershed hydrology model inspired by the SWAT (Soil and Water Assessment Tool) framework.
*   The watershed is subdivided into sub-watershed polygons loaded from a shapefile. Water accumulates in
*   each sub-watershed from rainfall and flows downstream to adjacent sub-watersheds according to their
*   topographic order. To better visualize the flow dynamics, rainfall is uniform and occurs only every
*   20 steps; the remaining steps show pure drainage. This model demonstrates how shapefile-based polygonal
*   spatial units can represent hydrological catchments and their upstream-downstream connectivity.
* Tags: shapefile, gis, gui, hydrology, water_flow, watershed, SWAT, catchment, rainfall
***/


model Waterflowwatershed

global {	
	file watershed_shape_file <- shape_file("../includes/ZH2.shp");
	geometry shape <- envelope(watershed_shape_file);

	float rain <- rnd(10.0) update: every(20#cycle) ? rnd(10.0) : 0.0;
	
	init {
		create watershed from: watershed_shape_file with: (id_watershed:int(read("ID_ZH")), id_watershed_outlet:int(read("ID_ND_EXUT")),order:int(read("order")));
		
		ask watershed {
			do init_watershed;
			write "" + int(self) + " " + length(shape.points) + " points";
		}
	}
	
	reflex water_floaw {
		ask reverse(watershed sort_by(each.order)) {
			do model_hydro;
		}
	}
}

species watershed schedules: [] {
	int id_watershed;
	int id_watershed_outlet;
	int order;
	
	list<watershed> watershed_upstream;

	float volume_watershed ;

	action init_watershed() {
		// Find ZH in the upstream 
		watershed_upstream <- watershed where(each.id_watershed_outlet = id_watershed);
	}
		
	action model_hydro() {	
		volume_watershed <- 0.7 * rain * self.shape.area  + (watershed_upstream sum_of(each.volume_watershed));	
	}	

	aspect blueFlow {
		draw shape border: #white color:rgb(0,0,255*volume_watershed/100000000);
	}
}

experiment waterFlow type: gui {
	output {
	 	display "My display Abs" type:2d{ 
			species watershed aspect: blueFlow;
		}
	}
}