/***
* Name: Text Display
* Author: Alexis Drogoul
* Description: A reference model for drawing text strings in GAMA displays. Demonstrates the 'anchor' facet
*   which controls which point of the text bounding box is placed at the draw location (e.g., #center, #top_left,
*   #bottom_right). Also shows optional text attributes: 'depth' for extruded 3D text, 'border' for a text
*   outline, 'precision' for anti-aliasing quality, and 'wireframe' for hollow letter outlines. Multiple named
*   experiments illustrate each attribute combination.
* Tags: display, draw, string, text, anchor, depth, border, wireframe, font, visualization
***/
model TextDisplay

global {}

experiment Strings {
	
	list<font> fonts <- [font("Helvetica", 14, #plain), font("Times", 12, #plain), font("Courier", 12, #plain), font("Arial", 13, #bold), font("Times", 12, #bold+#italic), font("Geneva", 12, #bold)];
	map<string, point> anchors <- ["center"::#center, "top_left"::#top_left, "left_center"::#left_center, "bottom_left"::#bottom_left, "bottom_center"::#bottom_center, "bottom_right"::#bottom_right, "right_center"::#right_center, "top_right"::#top_right, "top_center"::#top_center];
	font current_font <- one_of(fonts) update: one_of(fonts);
	rgb current_color <- rnd_color(255) update: rnd_color(255);
	float current_depth <- rnd(16) - 8.0 update: rnd(16) - 8.0;
	int y_step <- 4;
	image_file g <- image_file("./../images/building_texture/texture1.jpg");


	output synchronized: true{
		layout #split;
		display "Strings" type: 3d  axes: false {
			graphics Strings {
				draw world.shape wireframe: true color: #black;
				int y <- 4;
				float precision <- 0.01;
			loop p over: anchors.pairs {
				write current_font.size;
					draw circle(0.5) at: {50, y} color: #red;
					draw p.key + " ABCDE...WXYZ 1234567890 precision: "+precision at: {50, y} anchor: p.value color: rnd_color(255) font: current_font with_size (current_font.size) depth: 8 precision: precision ;
					y <- y + y_step;
					precision <- precision * 2;
				}

				draw circle(0.5) at: {50, y} color: #green;
				draw "custom {0.6, 0.1} wireframe" at: {50, y} anchor: {0.6, 0.1} border: current_color font: one_of(fonts) with_size 14 wireframe: true width: 2;
				draw circle(0.5) at: {50, y + y_step} color: #red;
				draw "custom {0.2, 0.2} with border" at: {50, y + y_step} anchor: {0.2, 0.2} color: current_color depth: 3 border: rnd_color(255) width: 2;
				draw circle(0.5) at: {50, y + 2*y_step} color: #red;
				draw "custom {0.8, 0.8} with texture" at: {50, y + 2*y_step} anchor: {0.8, 0.8} font: one_of(fonts) with_size 12 depth: 8 texture: g ;
				draw circle(0.5) at: {50, y + 3*y_step} color: #red;
				draw "custom {0.8, 0.8} 3D Wireframe" at: {50, y + 3*y_step} anchor: {0.8, 0.8} font: current_font depth: 8 wireframe: true ;
				draw circle(0.5) at: {50, y + 4*y_step} color: #red;
				draw "custom {0.4, 0.1} Flat with border" at: {50, y + 4*y_step} anchor: {0.4, 0.1} font: one_of(fonts) border: #black ;
			}

		}
		
		//display "Without antialias" parent: "With antialias" antialias: false {}

	}

}




