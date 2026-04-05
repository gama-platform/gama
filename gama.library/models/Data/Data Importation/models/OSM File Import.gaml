/**
* Name: OSM File Import
* Author: Patrick Taillandier
* Description: Shows how to import an OpenStreetMap (OSM) data file in GAMA and create agents from specific
*   feature types. OSM data files contain a wide variety of geographic features tagged with key-value attributes.
*   GAMA allows filtering which features to import using a map of tag keys and accepted values: only features
*   whose tags match the filter are imported. This model extracts roads (by highway tag) and buildings (by
*   building tag) from an OSM file, creating one road agent and one building agent per matching feature. The
*   OSM tag documentation at http://wiki.openstreetmap.org/wiki/Map_Features describes the available tags.
* Tags: load_file, osm, gis, openstreetmap, road, building, filter, import, spatial
*/
model simpleOSMLoading


global
{

//map used to filter the object to build from the OSM file according to attributes. for an exhaustive list, see: http://wiki.openstreetmap.org/wiki/Map_Features
	map filtering <- map(["highway"::["primary", "secondary", "tertiary", "motorway", "living_street", "residential", "unclassified"], "building"::["yes"]]);
	//OSM file to load
	file<geometry> osmfile;

	//compute the size of the environment from the envelope of the OSM file
	geometry shape <- envelope(osmfile);
	init
	{
	//possibility to load all of the attibutes of the OSM data: for an exhaustive list, see: http://wiki.openstreetmap.org/wiki/Map_Features
		create osm_agent(highway_str:string(read("highway")), building_str:string(read("building"))) from: osmfile;

		//from the created generic agents, creation of the selected agents
		ask osm_agent
		{
			if (length(shape.points) = 1 and highway_str != nil)
			{
				create node_agent(shape:shape, type: highway_str);
			} else
			{
				if (highway_str != nil)
				{
					create road(shape:shape, type: highway_str);
				} else if (building_str != nil)
				{
					create building(shape:shape);
				}

			}
			//do the generic agent die
			do die();
		}

	}

}

species osm_agent
{
	string highway_str;
	string building_str;
}

species road
{
	rgb color <- rnd_color(255);
	string type;
	aspect default
	{
		draw shape color: color;
	}

}

species node_agent
{
	string type;
	aspect default
	{
		draw square(3) color: # red;
	}

}

species building
{
	aspect default
	{
		draw shape color: #grey;
	}

}

experiment "Load OSM" type: gui
{
	parameter "File:" var: osmfile <- file<geometry> (osm_file("../includes/rouen.gz", filtering));
	output
	{
		display map type: 3d
		{
			species building refresh: false;
			species road refresh: false;
			species node_agent refresh: false;
		}

	}

}

experiment "Load OSM from Internet" type: gui parent: "Load OSM"
{
	parameter "File:" var: osmfile <- file<geometry> (osm_file("http://download.geofabrik.de/europe/andorra-latest.osm.pbf", filtering));
	
}
