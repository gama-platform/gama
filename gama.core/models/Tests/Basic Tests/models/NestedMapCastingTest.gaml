/**
* Name: NestedMapCastingTest
* Description: Declared unknown content types must reach nested maps (#751).
* Tags: map, list, casting, test
*/

model NestedMapCastingTestModel

experiment NestedMapCastingTest type: test autorun: true {
	test "Nested maps accept mixed values after homogeneous initialization" {
		map<string, unknown> flat <- ["a"::1.0];
		flat["x"] <- "X";
		assert flat["x"] = "X";
		map<string, map<string, unknown>> mixed <- ["a"::["b"::1.0, "x"::"X"]];
		assert mixed["a"]["x"] = "X";
		map<string, map<string, unknown>> nested <- ["a"::["b"::1.0]];
		nested["a"]["x"] <- "X";
		assert nested["a"]["x"] = "X";
		assert nested["a"]["b"] = 1.0;
		map<string, map<string, unknown>> distinct <- ["a"::["b"::1.0], "c"::["d"::"D"]];
		distinct["a"]["x"] <- "X";
		assert distinct["a"]["x"] = "X";
	}

	test "Declared types propagate through deeper nesting" {
		map<string, map<string, map<string, unknown>>> nested <- ["a"::["b"::["c"::1.0]]];
		nested["a"]["b"]["x"] <- "X";
		assert nested["a"]["b"]["x"] = "X";
		assert nested["a"]["b"]["c"] = 1.0;
		map<string, map<string, map<string, unknown>>> distinct <-
			["a"::["b"::["c"::1.0]], "d"::["e"::["f"::"F"]]];
		distinct["a"]["b"]["x"] <- "X";
		assert distinct["a"]["b"]["x"] = "X";
	}

	test "Declared map types propagate through lists" {
		list<map<string, unknown>> maps <- [["b"::1.0]];
		maps[0]["x"] <- "X";
		assert maps[0]["x"] = "X";
		list<map<string, unknown>> distinct <- [["b"::1.0], ["c"::"C"]];
		distinct[0]["x"] <- "X";
		assert distinct[0]["x"] = "X";
		map<string, list<map<string, unknown>>> nested <- ["a"::[["b"::1.0]]];
		nested["a"][0]["x"] <- "X";
		assert nested["a"][0]["x"] = "X";
	}

	test "Explicitly typed maps still cast inserted values" {
		map<string, map<string, float>> nested <- ["a"::["b"::1.0]];
		nested["a"]["x"] <- "2.5";
		assert nested["a"]["x"] = 2.5;
	}

	test "Nested nil remains nil" {
		map<string, map<string, unknown>> nested <- ["a"::nil];
		assert nested["a"] = nil;
	}
}
