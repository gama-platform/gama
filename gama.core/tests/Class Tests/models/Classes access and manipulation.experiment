/**
* Name: Classesaccessandmanipulation
* Based on the internal empty template.
* Author: Baptiste Lesquoy
* Tags: Class, Object, Test
*/


model Classesaccessandmanipulation

class ExampleClass {

	int notInitializedInt;
	int initializedInt <- 5;
	int initializedInInit <- 10;

	int simple_action(){
		return 5;
	}

	int action_with_parameters(int i, int j){
		return 2 * i * j;
	}

	action state_changing_action(){
		notInitializedInt <- 123;
	}
}


experiment testAccess type:test{

	test uninitializedClassIsNil {
		ExampleClass c;
		assert c = nil;
	}
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

	test sameObjectsAreNotEqual{
		ExampleClass a <- ExampleClass();
		ExampleClass b <- ExampleClass();
		assert a != b;
	}


	test objectCopy {
		ExampleClass c <- ExampleClass();
		ExampleClass b <- copy(c);
		c.notInitializedInt <- 10;
		assert b.notInitializedInt != 10;
		assert b != c;
	}


	test execSimpleAction{
		ExampleClass c <- ExampleClass();
		assert c.simple_action() = 5;
	}


	test execActionWithParams{
		ExampleClass c <- ExampleClass();
		assert c.action_with_parameters(4, 5) = 40;
	}

	test execStateAlteringAction{
		ExampleClass c <- ExampleClass();
		assert c.notInitializedInt = 0;
		c.state_changing_action();
		assert c.notInitializedInt = 123;

	}

	test objectsAreNotInAgents {
		ExampleClass obj <- ExampleClass();
		assert !(agents contains obj);
	}

	test createListObjects {
		list<ExampleClass> l <- list_with(3, ExampleClass(initializedInt:rnd(1,10)));
		assert length(l) = 3;
		assert l none_matches (each = nil);
		assert l all_match (each.initializedInt > 0);
	}


}
