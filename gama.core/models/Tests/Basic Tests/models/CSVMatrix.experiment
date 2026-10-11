/**
* Name: CSVMatrix
* Description: Unit tests for loading a CSV file into a matrix of int, float and
*   string (the three branches of GamaCSVFile.createMatrixFrom). Guards against
*   regressions on CSV -> matrix reading.
*   The matrices are rectangular (4 columns x 3 rows) so that a column/row swap is
*   caught by the dimension checks. Reminder: GAMA matrices are indexed [column, row] despite all common sense.
* Author: baptiste lesquoy
* Tags: csv, matrix, file, load_file
*/


model test_csv_matrix


experiment "CSV to matrix tests" type: test {

	// ============================= INT =============================

	test load_int_matrix {
		matrix<int> m <- matrix(csv_file("../includes/matrix_int.csv", ",", int));
		assert m.columns = 4;
		assert m.rows = 3;
		// [column, row]
		assert m[0, 0] = 11;
		assert m[3, 0] = 14;
		assert m[0, 1] = 21;
		assert m[0, 2] = 31;
		assert m[1, 2] = 32;
		assert m[3, 2] = 34;
	}

	// ============================ FLOAT ============================

	test load_float_matrix {
		matrix<float> m <- matrix(csv_file("../includes/matrix_float.csv", ",", float));
		assert m.columns = 4;
		assert m.rows = 3;
		assert m[0, 0] = 1.5;
		assert m[3, 0] = 4.5;
		assert m[0, 1] = 5.5;
		assert m[0, 2] = 9.5;
		assert m[1, 2] = 10.5;
		assert m[3, 2] = 12.5;
	}

	// =========================== STRING ===========================
	// matrix_string.csv has NO trailing newline on purpose (cf. #1127).

	test load_string_matrix {
		matrix<string> m <- matrix(csv_file("../includes/matrix_string.csv", ",", string));
		assert m.columns = 4;
		assert m.rows = 3;
		assert m[0, 0] = "a";
		assert m[3, 0] = "d";
		assert m[0, 1] = "e";
		assert m[0, 2] = "i";
		assert m[1, 2] = "j";
		assert m[3, 2] = "l";
	}

	// ===================== Type auto-detection =====================
	// Without an explicit type, the contents type must be inferred from the file.

	test load_int_matrix_autodetect {
		matrix m <- matrix(csv_file("../includes/matrix_int.csv"));
		assert m.columns = 4;
		assert m.rows = 3;
		assert m[0, 0] = 11;
		assert m[3, 2] = 34;
	}
}
