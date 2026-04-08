/**
* Name: Procedural City
* Author: Arnaud Grignard
* Description: Generates a random 3D city procedurally: buildings with random heights and footprints are placed
*   on a regular grid. Two experiments are provided: the first renders buildings with 3D box shapes and texture
*   images applied to each face; the second uses untextured colored buildings where the color depends on the
*   angle from a rotating point light, creating dynamic shadow effects. This model is used as the city
*   environment in the City Boids comodel.
* Tags: 3d, texture, light, procedural, city, generation, visualization, building, display
*/

model procedural_city

global {
	int number_of_building min: 1 <- 300;
	int width_and_height_of_environment min: 10 <- 500;
	
	geometry shape <- square(width_and_height_of_environment);
		
	file roof_texture <- file('../images/building_texture/roof_top.jpg');		
	list textures <- [file('../images/building_texture/texture1.jpg'), file('../images/building_texture/texture2.jpg'), file('../images/building_texture/texture3.jpg'), file('../images/building_texture/texture4.jpg'), file('../images/building_texture/texture5.jpg'), file('../images/building_texture/texture6.jpg'), file('../images/building_texture/texture7.jpg'), file('../images/building_texture/texture8.jpg'), file('../images/building_texture/texture9.jpg'), file('../images/building_texture/texture10.jpg')];

	init {
		create Building number:number_of_building{
			width <- (rnd(100) / 100) * (rnd(100) / 100) * (rnd(100) / 100) * 50 + 10;
			 depth <-	(rnd(100) / 100) * (rnd(100) / 100) * (rnd(100) / 100 * width) * 10 + 10;
			shape <- box(width, width, depth) rotated_by rnd(360);
			texture <- textures[rnd(9)];
		}
	}
}

species Building{
	float width;
	float height;
	float depth;
	int angle;			
	file texture;

	reflex shuffle{
		width <- (rnd(100) / 100) * (rnd(100) / 100) * (rnd(100) / 100) * 50 + 10;
		 depth <-	(rnd(100) / 100) * (rnd(100) / 100) * (rnd(100) / 100 * width) * 10 + 10;
		shape <- box(width, width, depth) rotated_by rnd(360);
	}

	aspect base {
		draw shape color:#white;
	}

	aspect textured {
		draw shape texture:[roof_texture.path, texture.path] color: rnd_color(255);
	}
}


experiment ProceduralCity  type: gui{
	float minimum_cycle_duration <- 0.05;
	parameter 'Number of Agents' var:number_of_building  category: 'Initialization';
	parameter 'Dimensions' var:width_and_height_of_environment category: 'Initialization';
	init {
		gama.pref_texture_orientation <- true;
	}

	output {
		display City type:3d background:#white axes:false{
	  	camera 'default' location: {178.9256,868.4599,470.2417} target: {274.5961,228.3136,0.0};
			species Building aspect:textured;							
		}
	}
}
