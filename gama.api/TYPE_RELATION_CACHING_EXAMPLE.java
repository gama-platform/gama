/*******************************************************************************************************
 *
 * TypeRelationCachingExample.java - Example demonstrating the use of type relation caching
 *
 * This is a documentation/example file showing how to use the new caching features.
 * It is not part of the actual GAMA codebase but serves as a reference.
 *
 ********************************************************************************************************/
package gama.api.gaml.types.examples;

import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITypesManager;
import gama.api.gaml.types.Types;

/**
 * Example demonstrating the use of type relation caching in GAMA.
 * 
 * This class shows various scenarios where the caching improves performance
 * by avoiding redundant type hierarchy traversals.
 */
public class TypeRelationCachingExample {

	/**
	 * Example 1: Using static cache methods for built-in types
	 */
	public void exampleBuiltInTypes() {
		// Get some built-in types
		IType<?> intType = Types.INT;
		IType<?> floatType = Types.FLOAT;
		IType<?> listType = Types.LIST;
		
		// Check assignability using cached method
		// First call: computed and cached
		boolean assignable1 = Types.checkAssignability(floatType, intType);
		System.out.println("float assignable from int: " + assignable1);
		
		// Second call: cache hit - instant return
		boolean assignable2 = Types.checkAssignability(floatType, intType);
		System.out.println("float assignable from int (cached): " + assignable2);
		
		// Find common supertype
		IType<?> commonType = Types.computeCommonSupertype(intType, floatType);
		System.out.println("Common supertype of int and float: " + commonType);
		
		// Compute distance in type hierarchy
		int distance = Types.computeDistance(intType, floatType);
		System.out.println("Distance from int to float: " + distance);
		
		// Check translatability
		boolean translatable = Types.checkTranslatability(intType, floatType);
		System.out.println("int translatable into float: " + translatable);
	}

	/**
	 * Example 2: Using parametric types with caching
	 */
	public void exampleParametricTypes() {
		// Create parametric types
		IType<?> listOfInt = Types.LIST.of(Types.INT);
		IType<?> listOfFloat = Types.LIST.of(Types.FLOAT);
		IType<?> listOfString = Types.LIST.of(Types.STRING);
		
		// These will be cached separately because parametric types
		// with different content types are different instances
		boolean assignable1 = Types.checkAssignability(listOfFloat, listOfInt);
		boolean assignable2 = Types.checkAssignability(listOfFloat, listOfString);
		
		System.out.println("list<float> assignable from list<int>: " + assignable1);
		System.out.println("list<float> assignable from list<string>: " + assignable2);
		
		// Find common supertype of parametric types
		IType<?> commonListType = Types.computeCommonSupertype(listOfInt, listOfFloat);
		System.out.println("Common supertype of list<int> and list<float>: " + commonListType);
	}

	/**
	 * Example 3: Using model-specific TypesManager for species types
	 */
	public void exampleModelSpecificTypes(ITypesManager modelTypeManager) {
		// Assume we have model-specific types (species)
		IType<?> speciesA = modelTypeManager.get("A");
		IType<?> speciesB = modelTypeManager.get("B");
		
		if (speciesA != null && speciesB != null) {
			// Use the model's TypesManager for caching
			boolean assignable = modelTypeManager.checkAssignability(speciesA, speciesB);
			System.out.println("Species A assignable from Species B: " + assignable);
			
			// Find common supertype in the species hierarchy
			IType<?> commonSpecies = modelTypeManager.computeCommonSupertype(speciesA, speciesB);
			System.out.println("Common supertype of A and B: " + commonSpecies);
			
			// Compute distance in species hierarchy
			int distance = modelTypeManager.computeDistance(speciesA, speciesB);
			System.out.println("Distance from A to B: " + distance);
		}
	}

	/**
	 * Example 4: Performance scenario - repeated type checking
	 * 
	 * This simulates what happens during GAML compilation where
	 * the same type checks are performed many times.
	 */
	public void exampleRepeatedChecks() {
		IType<?> intType = Types.INT;
		IType<?> floatType = Types.FLOAT;
		
		long startTime = System.nanoTime();
		
		// Simulate repeated type checks during compilation
		for (int i = 0; i < 1000; i++) {
			// First iteration: cache miss, subsequent: cache hits
			Types.checkAssignability(floatType, intType);
			Types.computeCommonSupertype(intType, floatType);
			Types.computeDistance(intType, floatType);
			Types.checkTranslatability(intType, floatType);
		}
		
		long endTime = System.nanoTime();
		double durationMs = (endTime - startTime) / 1_000_000.0;
		
		System.out.println("1000 iterations of 4 type checks: " + durationMs + " ms");
		System.out.println("Average per operation: " + (durationMs / 4000) + " ms");
		System.out.println("Note: First iteration had cache misses, rest were cache hits");
	}

	/**
	 * Example 5: Mixed built-in and model-specific types
	 */
	public void exampleMixedTypes(ITypesManager modelTypeManager) {
		IType<?> listType = Types.LIST;
		IType<?> agentType = Types.AGENT;
		IType<?> speciesType = modelTypeManager.get("MySpecies");
		
		if (speciesType != null) {
			// Built-in types use static cache
			boolean builtInCheck = Types.checkAssignability(agentType, speciesType);
			System.out.println("agent assignable from MySpecies: " + builtInCheck);
			
			// Mixed check: species extends agent, so this should work
			IType<?> commonType = Types.computeCommonSupertype(agentType, speciesType);
			System.out.println("Common type of agent and MySpecies: " + commonType);
		}
	}

	/**
	 * Example 6: Understanding cache behavior
	 */
	public void exampleCacheBehavior() {
		System.out.println("=== Cache Behavior Example ===");
		
		// Same type comparisons are optimized and bypass cache
		boolean sameType = Types.checkAssignability(Types.INT, Types.INT);
		System.out.println("INT assignable from INT: " + sameType + " (fast path, no cache)");
		
		// Null checks are fast-pathed
		boolean nullCheck = Types.checkAssignability(null, Types.INT);
		System.out.println("null check: " + nullCheck + " (fast path, no cache)");
		
		// Different types go through cache
		boolean cached1 = Types.checkAssignability(Types.FLOAT, Types.INT);
		System.out.println("First check (cache miss): " + cached1);
		
		boolean cached2 = Types.checkAssignability(Types.FLOAT, Types.INT);
		System.out.println("Second check (cache hit): " + cached2);
		
		// Reverse order is a different cache key
		boolean cached3 = Types.checkAssignability(Types.INT, Types.FLOAT);
		System.out.println("Reverse check (different key, cache miss): " + cached3);
	}

	/**
	 * Main method for running examples
	 */
	public static void main(String[] args) {
		TypeRelationCachingExample examples = new TypeRelationCachingExample();
		
		System.out.println("=== Example 1: Built-in Types ===");
		examples.exampleBuiltInTypes();
		
		System.out.println("\n=== Example 2: Parametric Types ===");
		examples.exampleParametricTypes();
		
		System.out.println("\n=== Example 4: Performance ===");
		examples.exampleRepeatedChecks();
		
		System.out.println("\n=== Example 6: Cache Behavior ===");
		examples.exampleCacheBehavior();
		
		// Examples 3 and 5 require a model's TypesManager
		// which would be available during actual GAMA compilation
	}
}
