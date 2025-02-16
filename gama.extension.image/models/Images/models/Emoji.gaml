/**
* Name: Emoji
* Shows how to use SVG versions of Emojis 
* Author: drogoul
* Tags: image, svg
*/


model Emojis

global {
	int shape_width <- 100;
	geometry shape <- square(shape_width);
	font text_font <- font("Arial", 10, #bold);
	file emoji_folder <- folder("../includes/emoji/"); 
	// Filter the file names that end up with "svg"
	list<svg_file> file_list <- emoji_folder select (each contains (".svg")) collect (the_file: svg_file(emoji_folder.path + "/" + the_file));
	int cell_dimension <- 10;
	int size_max <- cell_dimension - 2;
	init {
		int number <- int((shape_width / cell_dimension) ^2  / 2);
		loop the_file over: number among(file_list) {
			geometry ss <- geometry(the_file);
			create emoji with: [ratio::ss.width / ss.height,shape::(geometry(the_file)),name::replace(replace(the_file.name,"emoji_",""),".svg",""), icon:: image(the_file)];
		}
	}
	
	
	
}

species emoji {
	float ratio;
	image icon;
	rgb color <- rnd_color(255);
	the_grid my_place <- the_grid(index);
	point location <- my_place.location;
	
	aspect icon_aspect {
		draw icon size: size_max;
		draw replace(name,'_',"\n") font: text_font at: location + {0,cell_dimension/2} anchor: #top_center color: #black;
	}
	
	aspect shape_aspect {
		draw shape border: false color: color size: (ratio > 1) ? {size_max, size_max/ratio}: {size_max*ratio, size_max};
		draw replace(name,'_',"\n") font: text_font at: location + {0,cell_dimension/2} anchor: #top_center color: #black;
	}
}

grid the_grid cell_width: cell_dimension cell_height: cell_dimension*2;

experiment Emojis {
	
	output {
		layout #split consoles: false toolbars: false tabs: false background: #black controls: true;
		display "Show vector emojis" type: 3d axes: false{
			species emoji aspect: shape_aspect;
		}
		display "Show icon emojis" type: 3d axes: false {
			species emoji aspect: icon_aspect;
		}
	}
	
}

