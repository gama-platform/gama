/**
* Name: ContainerOperatorsTest
* Description: Tests the operators on containers that are not covered by the other list, map and matrix tests:
* 	searches, quantifiers, set operations, ranges, grouping and indexing, statistics and matrix slicing
* Tags: list, map, matrix, container, test
*/

experiment ContainerOperatorsTest type: test autorun: true {

	list<int> numbers <- [3, 1, 4, 1, 5, 9, 2, 6];

	test "Matrix transformations" {
		matrix<int> m <- matrix([[1, 2, 3], [4, 5, 6]]);
		matrix<int> t <- transpose(m);
		assert t.columns = 3;
		assert t.rows = 2;
		assert t[2, 0] = m[0, 2];
		assert transpose(t) = m;

		assert (m + 1)[0, 0] = 2;
		assert (m * 2)[1, 2] = 12;

		matrix<int> filled <- matrix_with({2, 2}, 7);
		assert list(filled) = [7, 7, 7, 7];

		// a list fills a matrix row by row
		matrix<int> from_list <- [1, 2, 3, 4] as_matrix {2, 2};
		assert from_list row_at 0 = [1, 2];
		assert from_list row_at 1 = [3, 4];
	}
}
