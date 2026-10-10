/**
* Name: KMLTest
* Description: Tests the 'kml' export type contributed by the kml extension: a declared kml
*   variable is usable straight away, and geometries and icons can be accumulated into it.
* Tags: kml, extension, test
*/

model KMLTestModel

global {
	kml kml_export;
}

experiment KMLTest type: test autorun: true {

	test "A declared kml variable gets a usable default value" {
		assert world.kml_export != nil;
	}

	test "Geometries can be added to a kml export manager" {
		ask world {
			kml_export <- kml_export add_geometry (circle(5), 2.0, #black, #red);
			kml_export <- kml_export add_geometry (square(3), #blue, #green);
		}
		assert world.kml_export != nil;
	}

	test "Adding an icon that does not exist raises an error" {
		bool failed <- false;
		try {
			ask world {
				kml_export <- kml_export add_icon ({0, 0}, 1.0, 0.0, "missing_icon.png");
			}
		} catch {
			failed <- true;
		}
		assert failed;
	}

	test "A kml export manager can be saved to a file" {
		string problem <- nil;
		try {
			ask world {
				kml_export <- kml_export add_geometry (circle(5) at_location {10, 10}, 2.0, #black, #red);
				save kml_export to: "kml_test_tmp.kml" format: "kml";
			}
		} catch {
			problem <- #current_error;
		}
		assert problem = nil;
		assert file_exists("kml_test_tmp.kml");
		string written <- concatenate(text_file("kml_test_tmp.kml").contents);
		assert written contains "Placemark";
		assert delete_file("kml_test_tmp.kml");
	}
}
