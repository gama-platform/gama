/**
* Name: GMLTest
* Description: Tests the GML file type contributed by the gml extension. 'is_gml' only inspects
*   the extension of the path it is given, it does not read the file.
* Tags: gml, extension, test
*/

experiment GMLTest type: test autorun: true {

	test "is_gml recognises the gml extension" {
		assert is_gml("anything.gml") = true;
		assert is_gml("/some/where/else/data.gml") = true;
	}

	test "is_gml rejects other extensions" {
		assert is_gml("anything.txt") = false;
		assert is_gml("anything.shp") = false;
		assert is_gml("no_extension_at_all") = false;
	}

	test "Reading a missing gml file raises an error" {
		bool failed <- false;
		try {
			gml_file f <- gml_file("does_not_exist.gml");
			list content <- f.contents;
		} catch {
			failed <- true;
		}
		assert failed;
	}

	// '../includes/road.gml' is a copy of the sample used by the GML models of the library

	test "The features of a gml file can be read" {
		string problem <- nil;
		list<geometry> roads <- [];
		try {
			gml_file source <- gml_file("../includes/road.gml");
			roads <- source.contents;
		} catch {
			problem <- #current_error;
		}
		assert problem = nil;
		assert not empty(roads);
		assert roads all_match (each.perimeter > 0.0);
		// the attributes of the features are read too
		assert roads all_match (each get "RDLNTYPE" != nil);
	}
}
