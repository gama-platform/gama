/**
* Name: OSMTest
* Description: Tests the OSM file type contributed by the osm extension. 'is_osm' only inspects
*   the extension of the path it is given, it does not read the file.
* Tags: osm, extension, test
*/

experiment OSMTest type: test autorun: true {

	test "is_osm recognises the osm extensions" {
		assert is_osm("city.osm") = true;
		assert is_osm("/data/city.pbf") = true;
	}

	test "is_osm rejects other extensions" {
		assert is_osm("city.shp") = false;
		assert is_osm("city.txt") = false;
		assert is_osm("no_extension_at_all") = false;
	}

	test "Reading a missing osm file raises an error" {
		bool failed <- false;
		try {
			osm_file f <- osm_file("does_not_exist.osm");
			list content <- f.contents;
		} catch {
			failed <- true;
		}
		assert failed;
	}

	// '../includes/tiny.osm' is a handwritten file: a residential street made of 3 nodes, and a café

	test "Nodes with tags and ways are read as geometries" {
		osm_file source <- osm_file("../includes/tiny.osm");
		assert source.exists;
		assert source.name = "tiny.osm";
		list<geometry> features <- source.contents;
		// the nodes that only describe the way are not features by themselves
		assert length(features) = 2;

		list<geometry> streets <- features where (each get "highway" != nil);
		assert length(streets) = 1;
		geometry street <- streets[0];
		assert street get "highway" = "residential";
		assert street get "name" = "Rue du Test";
		assert string(street get "osm_id") = "10";
		// a way is a line going through its nodes
		assert length(street.points) = 3;
		assert street.perimeter > 0.0;
		assert street.area = 0.0;
	}

	test "A tagged node is read as a point" {
		osm_file source <- osm_file("../includes/tiny.osm");
		list<geometry> features <- source.contents;
		list<geometry> cafes <- features where (each get "amenity" = "cafe");
		assert length(cafes) = 1;
		assert cafes[0] get "name" = "Le Test";
		assert length(cafes[0].points) = 1;
		assert cafes[0].perimeter = 0.0;
	}

	test "A filter keeps the features that have the given tags" {
		osm_file streets_only <- osm_file("../includes/tiny.osm", ["highway"::["residential"]]);
		list<geometry> kept <- streets_only.contents;
		assert length(kept) = 1;
		assert kept[0] get "highway" = "residential";

		osm_file nothing <- osm_file("../includes/tiny.osm", ["highway"::["motorway"]]);
		list<geometry> none_kept <- nothing.contents;
		assert empty(none_kept);
	}

	test "The envelope of the file surrounds its features" {
		osm_file source <- osm_file("../includes/tiny.osm");
		geometry bounds <- envelope(source);
		list<geometry> features <- source.contents;
		assert bounds.area > 0.0;
		assert features all_match (bounds covers each);
	}
}
