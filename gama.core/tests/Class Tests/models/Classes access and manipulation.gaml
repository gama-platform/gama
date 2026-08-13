/**
* Name: Classesaccessandmanipulation
* Based on the internal empty template. 
* Author: Baptiste Lesquoy
* Tags: 
*/


model Classesaccessandmanipulation

class ExampleClass {
	
	int notInitializedInt;
	int initializedInt <- 5;
	int initializedInInit;
	
	init {
		initializedInInit <- 10;	
	}
}


experiment testAccess type:test{
	
	test defaultIntFieldValue {
		ExampleClass c <- ExampleClass();
		assert c.notInitializedInt = 0;
	}
	
	test declarationLevelInitialization{
		ExampleClass c <- ExampleClass();
		assert c.initializedInt = 5;
	}
	
	test initLevelInitialization{
		ExampleClass c <- ExampleClass();
		assert c.initializedInInit = 10;
	}
	
	test creationLevelInitialization {
		ExampleClass c <- ExampleClass(notInitializedInt:1, initializedInt:2, initializedInInit:3);
		assert c.notInitializedInt = 1;
		assert c.initializedInt = 2;
		assert c.initializedInInit = 3;
	}
	
	test valueAssignment {
		ExampleClass c <- ExampleClass();
		c.initializedInt <- 123;
		assert c.initializedInt = 123;
	}
	
	test objectIsAReference {
		ExampleClass c <- ExampleClass();
		ExampleClass b <- c;
		c.notInitializedInt <- 10;
		assert b.notInitializedInt = 10;
	}
	
	test referencesEquality {
		ExampleClass c <- ExampleClass();
		ExampleClass b <- c;
		assert c = b;
	}
	
	test objectCopyt {
		ExampleClass c <- ExampleClass();
		ExampleClass b <- copy(c);
		c.notInitializedInt <- 10;
		assert b.notInitializedInt != 10;
		assert b != c;
	}
	
	
}