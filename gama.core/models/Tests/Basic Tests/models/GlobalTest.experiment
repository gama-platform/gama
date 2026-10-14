/**
* Name: GlobalTest
* Author: Antigravity
* Description: Tests global block specific syntax like torus, shape, and dimensions
* Tags: global, environment, test
*/

model GlobalTestModel

global torus: true {
	geometry shape <- square(100);
}

experiment GlobalTest type: test autorun: true {
	
	test "Global Dimensions and Torus" {
		assert world.shape.area = 10000.0;
	}
}
