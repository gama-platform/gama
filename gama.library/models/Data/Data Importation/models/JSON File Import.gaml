/**
* Name: JSON File Import
* Author: Arnaud Grignard
* Description: Shows how to load a JSON file in GAMA and use its structured contents to initialize a grid.
*   JSON (JavaScript Object Notation) is a widely used lightweight data format for hierarchical and nested data.
*   GAMA reads JSON files via the 'json_file' operator, producing a map<string, unknown> that mirrors the JSON
*   object hierarchy. Array values become lists, nested objects become nested maps. The model reads a CityIO
*   JSON payload, extracts the grid data from it, and uses the cell type information to set the color and
*   attributes of grid cells.
* Tags: load_file, grid, json, data, import, map, hierarchical
*/

model json_loading   

global {
	file JsonFile <- json_file("../includes/cityIO.json");
    map<string, unknown> c <- JsonFile.contents;

	init { 
		list<map<string, int>> cells <- c["grid"];
        loop mm over: cells {                 
            cityMatrix cell <- cityMatrix grid_at {mm["x"],mm["y"]};
            cell.type <-int(mm["type"]);
        }
	}  
} 

grid cityMatrix width:16  height:16{
	rgb color <- #black;
	int type;
   	aspect base{	
    		draw shape color:rgb(type*30) border:#black ;
    }
}

experiment Display  type: gui {
	output {
		display cityMatrixView   type: 3d axes:false{	
			species cityMatrix aspect:base;			
		}
	}
}
