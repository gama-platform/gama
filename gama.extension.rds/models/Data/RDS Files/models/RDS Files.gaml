/**
 * Name: RDS Files
 * Description: Reading and writing R serialization version 2, without an installed R runtime.
 * Tags: file, rds, dataframe
 */
model RdsFiles

global {
	dataframe restored_table;
	map restored_values;
	matrix restored_matrix;

	init {
		dataframe table <- dataframe_with(["number", "label"], [[10, "first"], [20, "second"], [30, "third"]]);
		save table to: "../results/table.rds" format: "rds";
		restored_table <- dataframe(rds_file("../results/table.rds"));

		map values <- ["singleton"::[42], "sequence"::[1, 2, 3], "missing"::nil];
		save values to: "../results/values.rds" format: "rds";
		restored_values <- map(rds_file("../results/values.rds"));

		matrix cells <- matrix([[1, 4], [2, 5], [3, 6]]);
		file output <- rds_file("../results/matrix.rds", cells);
		save output;
		restored_matrix <- matrix(rds_file("../results/matrix.rds"));

		write "R can read these files using readRDS().";
		write "To read an object exported from R, use saveRDS(object, path, version = 2).";
	}
}

experiment "RDS round trips" type: gui {
	output {
		monitor "Dataframe" value: restored_table;
		monitor "Named list" value: restored_values;
		monitor "Matrix" value: restored_matrix;
	}
}
