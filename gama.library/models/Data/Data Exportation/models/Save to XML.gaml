/**
* Name: Save to XML
* Author: Alexis Drogoul
* Description: Shows how to save data to an XML file in GAMA, and how to read it back. The contents of an
*   'xml_file' is a map<string, unknown> representing the root element of the document. Every element is
*   represented by a map with (up to) 4 keys:
*   - "name": the tag name of the element (string, mandatory)
*   - "attributes": the attributes of the element (map<string, string>, optional)
*   - "text": the text contained in the element (string, optional)
*   - "children": the ordered list of the children elements, represented in the same way (list of maps, optional)
*   The model builds such a structure from a population of agents, saves it using the 'save' statement, and
*   then reloads the file to check its contents.
* Tags: save_file, xml, export, import, map, hierarchical, agent
*/

model SavetoXML

global {
	string file_path <- "../results/bugs.xml";
	
	init {
		create bug number: 10;
	}
	
	reflex save_and_reload when: cycle = 10 {
		// Build one element per bug, with attributes and children elements 
		list<map<string, unknown>> bug_elements <- bug collect map(
			"name"::"bug",
			"attributes"::["id"::string(each.index), "color"::string(each.color)],
			"children"::[
				map("name"::"location", "attributes"::["x"::string(each.location.x), "y"::string(each.location.y)]),
				map("name"::"size", "text"::string(each.size))
			]
		);
		// The root element contains all the bugs
		map<string, unknown> root <- map(
			"name"::"bugs",
			"attributes"::["cycle"::string(cycle), "number"::string(length(bug))],
			"children"::bug_elements
		);
		
		// Save the structure into an XML file
		save xml_file(file_path, root);
		write "Saved " + length(bug) + " bugs to " + file_path;
		
		// Reload the file and read its contents
		xml_file reloaded <- xml_file(file_path);
		map<string, unknown> reloaded_root <- reloaded.contents;
		map<string, string> root_attributes <- map(reloaded_root["attributes"]);
		write "Root element <" + reloaded_root["name"] + "> saved at cycle " + root_attributes["cycle"];
		list<map<string, unknown>> bugs <- list<map<string, unknown>>(reloaded_root["children"]);
		loop b over: bugs {
			map<string, string> atts <- map(b["attributes"]);
			list<map<string, unknown>> children <- list<map<string, unknown>>(b["children"]);
			map<string, string> loc <- map<string, string>(children[0]["attributes"]);
			write "Bug " + atts["id"] + " at {" + loc["x"] + "," + loc["y"] + "} of size " + children[1]["text"];
		}
		do pause();
	}
}

species bug skills: [moving] {
	float size <- 1.0 + rnd(4.0);
	rgb color <- rnd_color(255);
	
	reflex move {
		do wander();
	}
	
	aspect default {
		draw circle(size) color: color;
	}
}

experiment main type: gui {
	output {
		display map {
			species bug;
		}
	}
}
