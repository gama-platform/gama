/**
* Name: Inheritance
* Based on the internal empty template.
* Author: Baptiste Lesquoy
* Tags: Class, Object, Test
*/


model Inheritance

class Parent {

	int notInitializedInt;
	int initializedInt <- 5;
	int initializedInInit <- 10;
	int intToOverride <- 4;

	int action_to_inherit(){
		return 5;
	}

	int action_to_override(){
		return 3;
	}

	int virtual_action();

}

class Child parent:Parent{

	int intToOverride <- 5;

	int action_to_override(){
		return super.action_to_override() * 2;
	}
	int virtual_action(){
		return 3;
	}
}

class Child2 parent:Parent{
	int virtual_action(){ return 1;}
}

experiment "Test inheritance" type:test{

	test inheritedFieldsAreStillInitialisedInParent {
		Child c <- Child();
		assert c.notInitializedInt = 0;
		assert c.initializedInt = 5;
		assert c.initializedInInit = 10;
	}

	test inheritedFieldInitializationIsOverridden{
		Child c <- Child();
		assert c.intToOverride = 5;
	}

	test constructorInitializationWorksOnChild{
		Child c <- Child(notInitializedInt: 1, initializedInt: 2, intToOverride: 3);
		assert c.notInitializedInt = 1;
		assert c.initializedInt = 2;
		assert c.intToOverride = 3;
	}

	test inheritedActionIsStillAvailable{
		Child c <- Child();
		assert c.action_to_inherit() = 5;
	}

	test virtualActionImplementationIsCalled {
		Child c <- Child();
		assert c.virtual_action() = 3;
	}

	test overridingAndCallingParentsAction{
		Child c <- Child();
		assert c.action_to_override() = 6;
	}

	test usingBaseClassToHoldChildren{
		Parent p <- Child();
		assert p.intToOverride = 5;
		assert p.virtual_action() = 3;
	}

	test usingBaseClassToHoldChild{
		Parent p <- Child();
		assert p.intToOverride = 5;
		assert p.virtual_action() = 3;
	}

	test usingBaseClassToHoldChildren{
		list<Parent> l <- [Child(), Child2()];
		assert type_of(l[0]) = Parent;
		assert actual_type_of(l[0]) = Child;
		assert l[0].virtual_action() != l[1].virtual_action();
	}
}
