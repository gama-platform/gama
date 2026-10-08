/**
* Name: Strahler Number
* Author: Patrick Taillandier
* Description: Demonstrates the computation of Strahler stream order numbers on a river network graph. The
*   Strahler number is a measure of branching complexity: leaf nodes (headwaters) have order 1; when two
*   streams of the same order meet the result has order+1; otherwise the higher order is kept. It is widely
*   used in hydrology and geomorphology to characterize drainage networks. The model loads a river graph and
*   displays each segment colored by its Strahler number.
* Tags: graph, strahler, hydrology, stream_order, river_network, geomorphology, branching
*/

model exempleStrahler

global {
	graph river_network;
	map strahler_numbers;
	file river_shapefile <- file("../includes/rivers.shp");
	geometry shape <- envelope(river_shapefile);
	map<int,rgb> color_index <- [1::#lightblue, 2::#green,3::#orange, 4::#red];
	init {
		create river from:river_shapefile ;
		river_network <- directed(as_edge_graph(river));
		strahler_numbers <- strahler(river_network);
		ask river {
			index <- strahler_numbers[self] as int;
		}
	}
}

species river {
	int index <- 1;
	aspect default {
		draw shape + index/2.0 color: color_index[index] end_arrow: 5;
		draw ""+index color: #black font: font(30);
	}
}

experiment testStrahler type: gui {
	output {
		display map type:2d {
			species river;
		}
	}
}
