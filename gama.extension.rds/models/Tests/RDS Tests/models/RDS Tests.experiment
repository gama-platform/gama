model RdsTests

species snapshot_agent {
	int amount <- 7;
}

species other_snapshot_agent {}

experiment "RDS Tests" type: test autorun: true {

	test "Native R version-2 matrix and version-3 rejection" {
		matrix m <- matrix(rds_file("../includes/test_matrix__xdr__version_2.rds"));
		assert m.columns = 3 and m.rows = 2;
		assert m[0, 0] = 1 and m[1, 0] = 2 and m[2, 0] = 3;
		assert m[0, 1] = 4 and m[1, 1] = 5 and m[2, 1] = 6;
		bool failed <- false;
		try {
			matrix unsupported <- matrix(rds_file("../includes/test_matrix__xdr__version_3.rds"));
		} catch {
			failed <- true;
		}
		assert failed;
	}

	test "Native R dataframe and factors" {
		dataframe df <- dataframe(rds_file("../includes/test_dataframe__xdr__version_2.rds"));
		assert df.rows = 3 and df.columns = 2;
		assert df.keys = ["class", "value"];
		assert df["class"] = ["a", "b", "b"];
		assert df["value"] = [1, 2, 3];
	}

	test "Unsupported complex columns fail explicitly" {
		bool failed <- false;
		try {
			dataframe unsupported <- dataframe(rds_file("../includes/test_dataframe_dtypes_with_na__xdr__version_2.rds"));
		} catch {
			failed <- true;
		}
		assert failed;
	}

	test "Rectangular matrix export and import" {
		matrix m <- matrix([[1, 4], [2, 5], [3, 6]]);
		save m to: "../results/matrix.rds" format: "rds";
		matrix restored <- matrix(rds_file("../results/matrix.rds"));
		assert restored = m;
		assert delete_file("../results/matrix.rds");
	}

	test "Rectangular dataframe export and import" {
		dataframe df <- dataframe_with(["number", "label"], [[10, "a"], [20, "b"], [30, "c"]]);
		save df to: "../results/frame.rds" format: "rds";
		dataframe restored <- dataframe(rds_file("../results/frame.rds"));
		assert restored.rows = 3 and restored.columns = 2;
		assert restored["number"] = [10, 20, 30];
		assert restored["label"] = ["a", "b", "c"];
		assert delete_file("../results/frame.rds");
	}

	test "Singleton vectors and file-object saves" {
		file f <- rds_file("../results/singleton.rds", [42]);
		save f;
		assert list(rds_file("../results/singleton.rds")) = [42];
		assert delete_file("../results/singleton.rds");
	}

	test "Inferred-format map saves and nested NULL" {
		map values <- ["a"::1, "b"::[2, 3], "nothing"::nil];
		save values to: "../results/map.rds";
		map restored <- map(rds_file("../results/map.rds"));
		assert restored["a"] = [1];
		assert restored["b"] = [2, 3];
		assert restored["nothing"] = nil;
		assert delete_file("../results/map.rds");
	}

	test "Appending is rejected without damaging the file" {
		save [1, 2] to: "../results/append.rds" format: "rds";
		bool failed <- false;
		try {
			save [3, 4] to: "../results/append.rds" format: "rds" rewrite: false;
		} catch {
			failed <- true;
		}
		assert failed;
		assert list(rds_file("../results/append.rds")) = [1, 2];
		assert delete_file("../results/append.rds");
	}

	test "Agent snapshots and attribute selection" {
		create snapshot_agent number: 2 returns: agents_to_save;
		save agents_to_save to: "../results/agents.rds" format: "rds";
		dataframe defaults <- dataframe(rds_file("../results/agents.rds"));
		assert defaults["amount"] = [7, 7];
		save agents_to_save to: "../results/agents.rds" format: "rds" attributes: ["amount"];
		dataframe restored <- dataframe(rds_file("../results/agents.rds"));
		assert restored.rows = 2;
		assert restored.keys = ["name", "x", "y", "z", "amount"];
		assert restored["amount"] = [7, 7];
		save agents_to_save to: "../results/agents.rds" format: "rds" attributes: ["doubled"::amount * 2];
		dataframe computed <- dataframe(rds_file("../results/agents.rds"));
		assert computed["doubled"] = [14, 14];
		save agents_to_save to: "../results/agents.rds" attributes: ["amount"];
		dataframe inferred <- dataframe(rds_file("../results/agents.rds"));
		assert inferred["amount"] = [7, 7];
		assert delete_file("../results/agents.rds");
		ask agents_to_save { do die(); }
	}

	test "Empty typed agent populations export empty dataframes" {
		list<snapshot_agent> nobody <- [];
		save nobody to: "../results/empty_agents.rds" format: "rds" attributes: ["amount"];
		dataframe restored <- dataframe(rds_file("../results/empty_agents.rds"));
		assert restored.rows = 0;
		assert restored.keys = ["name", "x", "y", "z", "amount"];
		assert delete_file("../results/empty_agents.rds");
	}

	test "Logical missing values survive a round trip" {
		list values <- list(rds_file("../includes/test_nullable_logical__xdr__version_2.rds"));
		assert values[0] = true and values[1] = false and values[2] = nil;
		save values to: "../results/logical.rds" format: "rds";
		list restored <- list(rds_file("../results/logical.rds"));
		assert restored[0] = true and restored[1] = false and restored[2] = nil;
		assert delete_file("../results/logical.rds");
	}

	test "Failed conversion preserves an existing destination" {
		save [1, 2] to: "../results/preserved.rds" format: "rds";
		bool failed <- false;
		try {
			save [{1, 2}] to: "../results/preserved.rds" format: "rds";
		} catch {
			failed <- true;
		}
		assert failed;
		assert list(rds_file("../results/preserved.rds")) = [1, 2];
		assert delete_file("../results/preserved.rds");
	}

	test "Mixed species are rejected" {
		create snapshot_agent number: 1 returns: first_species;
		create other_snapshot_agent number: 1 returns: second_species;
		list<agent> mixed <- first_species + second_species;
		bool failed <- false;
		try {
			save mixed to: "../results/mixed.rds" format: "rds";
		} catch {
			failed <- true;
		}
		assert failed;
		assert not file_exists("../results/mixed.rds");
		ask mixed { do die(); }
	}

	test "Unknown attributes are rejected even for empty populations" {
		list<snapshot_agent> nobody <- [];
		list<string> selected <- ["missing_attribute"];
		bool failed <- false;
		try {
			save nobody to: "../results/invalid.rds" format: "rds" attributes: selected;
		} catch {
			failed <- true;
		}
		assert failed;
		assert not file_exists("../results/invalid.rds");
	}
}
