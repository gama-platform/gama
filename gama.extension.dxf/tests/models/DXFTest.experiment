/**
* Name: DXFTest
* Description: Tests the DXF file type contributed by the dxf extension.
*   NOTE: gama.extension.dxf is currently built but listed in no feature, so it is absent from
*   the packaged product and this test is never picked up by the test runner.
* Tags: dxf, extension, test
*/

experiment DXFTest type: test autorun: true {

	test "Reading a missing dxf file raises an error" {
		bool failed <- false;
		try {
			dxf_file f <- dxf_file("does_not_exist.dxf");
			list content <- f.contents;
		} catch {
			failed <- true;
		}
		assert failed;
	}
}
