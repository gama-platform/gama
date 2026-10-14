/**
* Name: operatortest
* Author: Truong Chi Quang
* Description: Tests the length operator
* Tags: test, length
*/

model testLength

grid cell width: 5 height: 5{
	float grid_value <-1.0;
}
experiment length type: test {
	
	test "length" {
		assert length(cell)=25; 
		assert length(cell where (each.grid_value=1.0))=25;
	}
	
}
