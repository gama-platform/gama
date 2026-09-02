/**
* Name: DXF File Import
* Author: Patrick Taillandier
* Description: Shows how to create agents from a DXF (Drawing Exchange Format) file. DXF is a CAD data format
*   developed by Autodesk for AutoCAD and is widely used in architecture, engineering, and construction.
*   GAMA can read DXF files and convert the geometric entities they contain (lines, polylines, polygons, etc.)
*   into agent geometries. A scale factor (here #m for meters) can be provided to correctly interpret the
*   DXF coordinate units. The model loads a house plan from a DXF file and creates one agent per geometric
*   entity, which are then displayed in a 2D view.
* Tags: dxf, load_file, cad, geometry, architecture, import, gis
*/
model DXFAgents


global
{
	dxf_file house_file <- dxf_file("../includes/house.dxf",#m);

	//compute the environment size from the dxf file envelope
	geometry shape <- envelope(house_file);
	init
	{
	//create house_element agents from the dxf file and initialized the layer attribute of the agents from the the file
		create house_element(layer:string(get("layer"))) from: house_file;
		
		//define a random color for each layer
		map<string,list<house_element>> layers <- house_element group_by each.layer;
		loop la over: layers.keys
		{
			rgb col <- rnd_color(255);
			ask layers[la]
			{
				color <- col;
			}
		}
	}
}

species house_element
{
	string layer;
	rgb color;
	aspect default
		{
		draw shape color: color;
	}
	init {
		shape <- polygon(shape.points);
	}
}

experiment DXFAgents type: gui
{   
	output
	{	layout #split;
		display map type: 3d
		{
			species house_element;
		}

		display "As_Image" type: 3d
		{
			graphics "House"
			{
				draw house_file at: {0,0} color: # brown;
			}

		}

	}

}
