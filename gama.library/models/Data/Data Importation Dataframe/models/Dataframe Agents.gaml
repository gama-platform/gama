/**
* Name: Dataframe to Agents
* Author: GAMA Team
* Description: Shows how to use a dataframe to initialize agents from tabular data.
*   This approach is an alternative to the CSV matrix-based import. Dataframes
*   provide column-based access, making it easier to map columns to agent attributes
*   by name rather than by index.
* Tags: dataframe, csv, agent, create, initialization, data, import
*/

model DataframeAgents

global {
	// Load the dataset
	dataframe iris_df <- df_load_csv("../includes/iris.csv");

	init {
		write "Loaded " + df_rows(iris_df) + " iris records";
		write "Columns: " + df_columns(iris_df);

		// Create agents from the dataframe rows
		loop i from: 0 to: df_rows(iris_df) - 1 {
			create iris_flower {
				sepal_length <- float(df_cell(iris_df, i, "sepallength"));
				sepal_width <- float(df_cell(iris_df, i, "sepalwidth"));
				petal_length <- float(df_cell(iris_df, i, "petallength"));
				petal_width <- float(df_cell(iris_df, i, "petalwidth"));
				iris_type <- string(df_cell(iris_df, i, "type"));
			}
		}

		write "Created " + length(iris_flower) + " iris agents";

		// Use dataframe filtering to count by type
		dataframe setosa <- df_filter(iris_df, "type", "Iris-setosa");
		dataframe virginica <- df_filter(iris_df, "type", "Iris-virginica");
		dataframe versicolor <- df_filter(iris_df, "type", "Iris-versicolor");

		write "Setosa: " + df_rows(setosa) + ", Virginica: " + df_rows(virginica) + ", Versicolor: " + df_rows(versicolor);
	}
}

species iris_flower {
	float sepal_length;
	float sepal_width;
	float petal_length;
	float petal_width;
	string iris_type;

	rgb color <- iris_type = "Iris-setosa" ? #blue : (iris_type = "Iris-virginica" ? #red : #yellow);

	aspect default {
		// Position based on sepal dimensions, size based on petal width
		draw circle(petal_width * 2) at: {sepal_length * 20, sepal_width * 20} color: color;
	}
}

experiment visualize type: gui {
	output {
		display "Iris Dataset" type: 2d {
			species iris_flower;
		}
		display "Iris Stats" type: 2d {
			chart "Sepal Length Distribution" type: histogram {
				data "Setosa" value: iris_flower where (each.iris_type = "Iris-setosa") collect each.sepal_length color: #blue;
				data "Virginica" value: iris_flower where (each.iris_type = "Iris-virginica") collect each.sepal_length color: #red;
				data "Versicolor" value: iris_flower where (each.iris_type = "Iris-versicolor") collect each.sepal_length color: #yellow;
			}
		}
	}
}
