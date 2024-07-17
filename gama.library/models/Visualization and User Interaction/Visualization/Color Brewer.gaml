/**
* Name: Color defined by choosing a Color Brewer
* Author:  Arnaud Grignard & Patrick Taillandier
* Description: A model to show how to use color brewer. In this model, two experiments are presents : one just to show the different colors present 
* 	in some selected brewer, and a second one to show in a grid the different colors of the brewer having at least a minimal number of colors passed 
* 	in parameter.
* Tags: color
*/


model ColorBrewer


global {

	//number of colors
	int nb_classes<-14 min:1 max: 15;
	
	
	//list of palettes that have at least nb_classes
	list<string> palettes <- brewer_palettes(nb_classes);
	
	//the current sequential palette from the list of all available sequential Palettes
	string sequentialPalette <- "YlOrRd" among:["YlOrRd","Grays","PuBu","BuPu","YlOrBr","Greens","BuGn","GnBu","PuRd","Purples","Blues","Oranges","OrRd","Reds","YlGn","YlGnBu"] on_change:update_colors;
	
	//the current diverging palette from the list of all available diverging Palettes
	string divergingPalette <- "BrBG" among:["PRGn","PuOr","RdGy","Spectral","RdYlGn","RdBu","RdYlBu","PiYG","BrBG"] on_change:update_colors;
	
	//the current qualitative palette from the list of all available qualitative Palettes
	string qualitativePalette <- "Pastel1" among:["Accents","Paired","Set3","Set2","Set1","Dark2","Pastel2","Pastel1"] on_change:update_colors;
	
	//build the lists of colors from the palettes
	list<rgb> SequentialColors <- brewer_colors(sequentialPalette);
	list<rgb> DivergingColors <- brewer_colors(divergingPalette);
	list<rgb> QualitativeColors <- brewer_colors(qualitativePalette);
	
	action update_colors{
		SequentialColors <-  brewer_colors(sequentialPalette);
		DivergingColors <- brewer_colors(divergingPalette);
		QualitativeColors <- brewer_colors(qualitativePalette);
		ask experiment{
			do update_outputs(true);
		}
	}
	
	init {
		//if the palettes is not empty
		if (not empty(palettes)) {
			//for each palette
			loop i from: 0 to: length(palettes) - 1 {
				//define a  list of nb_classes colors from the current palette
				list<rgb> colors <- brewer_colors(palettes[i],nb_classes);
				
				//define the colors of the corresponding cells
				ask cell where (each.grid_y = i){
					palette <- palettes[i];
					color <- colors[grid_x,i];	
				}
			}
		}
	}
}

grid cell width:nb_classes height: max([1,length(palettes)]) {
	string palette;
}


//in this experiment, we do not use the cell agents, but we directlty draw the different palettes of colors
experiment BrewerPalette type: gui {
	
	font base_font <- font("helvetica", 12, #bold);
	float legend_offset <- 11.0;
	float square_size;
	
	
	
	parameter "Sequential Palettes" var:sequentialPalette category:"Brewer";
	parameter "Diverging Palettes" var:divergingPalette category:"Brewer";
	parameter "Qualitatives Palettes" var:qualitativePalette category:"Brewer";
	
	init {
		square_size <- (world.shape.width - legend_offset) / nb_classes;		
	} 
	
	
	output {
		display View1 type:3d antialias:false axes:false{
			graphics "brewer" position:{0, 20} {
				//Sequential
				draw "Sequential" at:{0,square_size/2} color:#black perspective:true font:base_font;
				loop i from:0 to:length(SequentialColors)-1{
					draw square(square_size) color:SequentialColors[i] at: {legend_offset +  square_size*(0.5 + i), square_size/2, 0};
				}
				//Diverging
				draw "Diverging" at:{0,1.5*square_size} color:#black perspective:true  font:base_font;
				loop i from:0 to:length(DivergingColors)-1{
					draw square(square_size) color:DivergingColors[i] at: {legend_offset + square_size*(0.5 + i), 1.5*square_size, 0};
				}
				//Qualitative		
				draw "Qualitative" at:{0,2.5*square_size} color:#black perspective:true  font:base_font;
				loop i from:0 to:length(QualitativeColors)-1{
					draw square(square_size) color:QualitativeColors[i] at: {legend_offset + square_size*(0.5 + i), 2.5*square_size, 0};
				}
		    }
		}	
	}
}

//in this experiment, we display the cell agents with the  different aspects
experiment BrewerColoredAgent type: gui {
	parameter "Number of data classes" var:nb_classes category:"Brewer";
	output {
		display View1 antialias:false axes:false{
			grid cell border: #black ;
			graphics "Names" {
				loop i from:0 to:length(palettes)-1 {
					draw string(i)+":"+cell[0,i].palette at:cell[0,i].location-{9,-1,0} color:#black;
				}
			}
		}	
	}
}