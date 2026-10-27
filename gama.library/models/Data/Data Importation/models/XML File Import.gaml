/**
* Name: XML File Import
* Author: Alexis Drogoul
* Description: Shows how to read an XML file in GAMA and use its contents to initialize a model.
*   GAMA reads XML files via the 'xml_file' operator. Its contents is a map<string, unknown> representing
*   the root element of the document. Every element is represented by a map with 4 keys:
*   - "name": the tag name of the element (string)
*   - "attributes": the attributes of the element (map<string, string>)
*   - "text": the text directly contained in the element, trimmed (string)
*   - "children": the ordered list of the children elements, each represented in the same way (list of maps)
*   The model first prints the whole tree in the console using a recursive action, then looks for specific
*   elements to define the size of the environment and to create agents with the characteristics found in the file.
* Tags: load_file, xml, data, import, map, hierarchical
*/

model xml_loading

global {
	xml_file config_file <- xml_file("../includes/ecosystem.xml");
	// The root element of the document
	map<string, unknown> root <- config_file.contents;
	
	// The environment is defined using the values of the 'width' and 'height' elements of the 'environment' element 
	map<string, unknown> environment_element <- first_child(root, "environment");
	geometry shape <- rectangle(float(text_of(environment_element, "width")), float(text_of(environment_element, "height")));
	
	init {
		// Attributes of the root element
		map<string, string> root_attributes <- map(root["attributes"]);
		write "Ecosystem '" + root_attributes["name"] + "' (version " + root_attributes["version"] + ")";
		write text_of(root, "description");
		write "";
		write "Structure of the file:";
		do print_element(root, "");
		
		// Creation of the agents described by the 'species' elements
		loop species_element over: children_named(root, "species") {
			map<string, string> atts <- map(species_element["attributes"]);
			create animal number: int(atts["number"]) {
				kind <- atts["name"];
				color <- rgb(atts["color"]);
				speed <- float(world.text_of(species_element, "speed"));
				size <- float(world.text_of(species_element, "size"));
			}
		}
	}
	
	// Returns the list of the children of an element that have the given tag name
	list<map<string, unknown>> children_named(map<string, unknown> element, string tag) {
		list<map<string, unknown>> children <- list(element["children"]);
		return children where (each["name"] = tag);
	}
	
	// Returns the first child of an element with the given tag name (or nil if there is none)
	map<string, unknown> first_child(map<string, unknown> element, string tag) {
		return first(children_named(element, tag));
	}
	
	// Returns the text of the first child of an element with the given tag name
	string text_of(map<string, unknown> element, string tag) {
		map<string, unknown> child <- first_child(element, tag);
		return child = nil ? "" : string(child["text"]);
	}
	
	// Recursively prints an element, its attributes, its text and its children
	action print_element(map<string, unknown> element, string indent) {
		map<string, string> atts <- map(element["attributes"]);
		string text <- string(element["text"]);
		write indent + "<" + element["name"] + ">" + (empty(atts) ? "" : " " + atts) + (empty(text) ? "" : " : " + text);
		list<map<string, unknown>> children <- list(element["children"]);
		loop child over: children {
			do print_element(child, indent + "    ");
		}
	}
}

species animal skills: [moving] {
	string kind;
	rgb color;
	float size;
	
	reflex move {
		do wander();
	}
	
	aspect default {
		draw circle(size) color: color border: #black;
	}
}

experiment "Load XML" type: gui {
	output {
		display "Ecosystem" {
			species animal;
		}
	}
}
