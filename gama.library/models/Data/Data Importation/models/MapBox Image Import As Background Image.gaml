/***
* Name: MapBox Image Import As Background Image
* Author: Duc Minh Tran
* Description: Shows how to load a static map image from the Mapbox Static Images API and use it as a
*   background layer in a GAMA display. Mapbox provides high-quality satellite, street, and terrain tile images
*   through a REST API. A valid Mapbox API key is required. The model provides a user command to change the
*   map center coordinates and reload the image dynamically. This pattern can be used to provide a realistic
*   geographic context for simulations that cover a specific real-world area.
* Tags: image, background, mapbox, map, web, api, display, load_file
***/
model main

global {
	string appkey <-"KEY";
	image_file static_map_request;
	string map_center;
	point map_size;

	action load_map() {
		float s <- world.shape.height / world.shape.width;
		map_size <- {500, 500 * s};
		string request <- "https://api.mapbox.com/styles/v1/mapbox/satellite-v9/static/" + "[" + map_center + "]/" + int(map_size.x) + "x" + int(map_size.y) + "@2x?" + "access_token=" + appkey;
		write "Request : " + request;
		static_map_request <- image_file(request, "JPEG");
	}

	shape_file buildings_shape_file <- shape_file("../includes/buildings.shp");
	geometry shape <- envelope(buildings_shape_file);

	init {
		geometry loc <- (world.shape CRS_transform ("EPSG:4326"));
		map_center <- "" + loc.points[0].x + "," + loc.points[0].y + "," + loc.points[2].x + "," + loc.points[2].y;
		write loc;
		write map_center;
		
		if(appkey = "KEY") {
			map useless <- user_input_dialog("Please enter your MapBox access token as a value for the appkey variable in the code instead of \"KEY\".", []);			
		} else {
			do load_map();
		}
		
		create building from: buildings_shape_file;
	}

}

species building {
}

experiment exp {
	output {
		display main type: 3d {
			picture static_map_request;
			species building;
		}

	}

}