/**
* Name: FileReadWriteTest
* Author: Antigravity
* Description: Tests saving and reading generic text files
* Tags: file, io, test
*/

model FileReadWriteTest

experiment FileReadWriteTest type: test autorun: true {
	
	test "Save and Read Text File" {
		string text_content <- "Line1\nLine2\nLine3";
		
		// Save string to file
		save text_content to: "test_output.txt";
		
		// Read file back
		file f <- text_file("test_output.txt");
		list<string> contents <- list<string>(f);
		
		assert length(contents) = 3;
		assert contents[0] = "Line1";
		assert contents[2] = "Line3";
		
		// Check file existence
		assert file_exists("test_output.txt") = true;
		assert file_exists("non_existent_file.txt") = false;

		// do not leave the file behind
		assert delete_file("test_output.txt");
		assert file_exists("test_output.txt") = false;
	}
	
	test "Save and Read CSV File" {
		matrix<int> mat <- matrix([[1, 2], [3, 4]]);
		
		// Save matrix to CSV
		save mat to: "test_output.csv" header: false;
		
		// Read CSV back
		matrix<int> loaded_mat <- matrix<int>(csv_file("test_output.csv", ","));
		
		assert loaded_mat.rows = 2;
		assert loaded_mat.columns = 2;
		assert loaded_mat[0, 0] = 1;
		assert loaded_mat[1, 1] = 4;

		// do not leave the file behind
		assert delete_file("test_output.csv");
		assert file_exists("test_output.csv") = false;
	}
}
