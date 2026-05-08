/**
* Name: CSV File Import
* Author: Patrick Taillandier
* Description: Shows how to load a CSV file in GAMA and convert its contents into a matrix for direct data
*   manipulation. The 'csv_file' operator reads the file using the specified delimiter and parses all values.
*   The resulting file object can be cast to a matrix, making every cell of the CSV directly accessible by
*   row and column index. This approach is useful when the CSV data needs to be processed programmatically
*   rather than directly mapped to agent attributes. The model uses the Iris dataset and writes the loaded
*   matrix contents to the console.
* Tags: csv, load_file, matrix, data, import, parsing
*/


model CSVfileloading

global {
	file my_csv_file <- csv_file("../includes/iris.csv",",");
	
	init {
		//convert the file into a matrix
		matrix data <- matrix(my_csv_file);
		//loop on the matrix rows (skip the first header line)
		loop i from: 1 to: data.rows -1{
			//loop on the matrix columns
			loop j from: 0 to: data.columns -1{
				write "data rows:"+ i +" colums:" + j + " = " + data[j,i];
			}	
		}		
	}
}

experiment main type: gui;
