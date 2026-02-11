# Type Relation Caching in GAMA

## Overview

This document describes the caching mechanism for expensive type relation computations in GAMA's type system. The caching improves compilation performance by avoiding redundant calculations for frequently-checked type relationships.

## Cached Operations

The following type relation methods are now cached:

1. **`isAssignableFrom(IType)`** - Checks if one type can be assigned from another
2. **`findCommonSupertypeWith(IType)`** - Finds the common supertype between two types
3. **`distanceTo(IType)`** - Computes the distance between types in the hierarchy
4. **`isTranslatableInto(IType)`** - Checks if one type can be translated into another

## Architecture

### Homogeneous Type-Manager Association

Each type instance knows its owning TypesManager through a field set at instantiation time. This provides a uniform caching strategy for both built-in and model-specific types:

- **Built-in types**: Reference the global `Types.builtInTypes` manager
- **Model-specific types**: Reference their model's TypesManager
- **Parametric types**: Inherit the manager from their base type

### Key Benefits

1. **Homogeneous Design**: Both built-in and model-specific types use the same caching mechanism
2. **Automatic Scoping**: Caches are naturally scoped to the appropriate manager
3. **No Static State**: No global static caches needed - each manager has its own cache
4. **Proper Isolation**: Model-specific caches don't interfere with built-in type caches

### Cache Storage

Caches are stored in the `TypesManager` class:
- Location: `TypesManager` instance
- Scope: Per-manager (global for built-in, per-model for species)
- Lifetime: 5 minutes after last access, or until manager disposal
- Thread-safe: Yes (uses Guava Cache)

### Cache Key

The `TypePair` class serves as the cache key:
- Immutable wrapper around two `IType` instances
- Uses identity comparison (not equality) for proper parametric type handling
- Pre-computes hash code for performance
- Differentiates `list<int>` from `list<float>` correctly

## Usage

### Standard Usage

The caching is completely transparent - just use the normal type methods:

```java
// Caching is automatic - no API changes needed
boolean assignable = targetType.isAssignableFrom(sourceType);
IType<?> commonType = type1.findCommonSupertypeWith(type2);
int distance = fromType.distanceTo(toType);
boolean translatable = sourceType.isTranslatableInto(targetType);
```

### Direct Cache Access

If you have access to a TypesManager, you can use its cache methods directly:

```java
ITypesManager typeManager = model.getTypesManager();

// Explicitly use cached methods
boolean assignable = typeManager.checkAssignability(type1, type2);
IType<?> commonType = typeManager.computeCommonSupertype(type1, type2);
int distance = typeManager.computeDistance(type1, type2);
boolean translatable = typeManager.checkTranslatability(type1, type2);
```

## Implementation Details

### Type Initialization

When a type is created:

1. `TypesManager.initType()` or `addSpeciesType()` is called
2. The manager sets itself on the type via `setTypesManager(this)`
3. The type stores the reference in its `typesManager` field
4. Parametric types inherit the manager from their base type

### Cache Behavior

1. **Type method called**: e.g., `type1.isAssignableFrom(type2)`
2. **Check for manager**: If `typesManager != null`, use cache
3. **Cache lookup**: Check if result is already cached
4. **On cache miss**: Compute result using fallback logic
5. **Cache result**: Store in manager's cache for future use
6. **Return result**: From cache or computation

### Fast Paths

The following scenarios bypass the cache for optimal performance:
- Same type comparisons: `type == type` returns immediately
- Null checks: Returns appropriate default immediately
- Manager unavailable: Falls back to direct computation (rare)

### Performance Considerations

1. **Transparent Caching**: No API changes needed - caching is automatic
2. **Identity-Based Keys**: Using identity comparison for cache keys is fast and correct
3. **Guava Cache**: Automatic eviction prevents memory leaks while maintaining hot data
4. **Manager-Scoped**: Built-in types share one cache, each model has its own

### Thread Safety

All caches use concurrent data structures:
- `TypesManager` caches: Guava `Cache` (thread-safe)
- Cache keys (`TypePair`): Immutable, inherently thread-safe
- Type fields: Set once during initialization, effectively immutable

## Backward Compatibility

The implementation maintains full backward compatibility:
- No API breaking changes
- Existing code continues to work without modifications  
- New cached methods are purely additive (in ITypesManager interface)
- Types without a manager fall back to direct computation

## Example Scenario

Consider a GAML model with complex type checking during compilation:

```gaml
species A {
    list<int> values;
}

species B parent: A {
    list<float> other_values;
}
```

When the compiler validates assignments, conversions, and inheritance:

1. **First check**: `list<int>.isAssignableFrom(list<int>)`
   - Type checks its manager's cache
   - Cache miss - computes result
   - Stores in cache
   
2. **Second check**: `list<int>.isAssignableFrom(list<int>)`
   - Type checks its manager's cache
   - **Cache hit** - instant return
   
3. **Cross-check**: `list<float>.findCommonSupertypeWith(list<int>)`
   - Types check their manager's cache (same manager)
   - Cache miss - computes result
   - Stores in cache
   
4. **Later check**: `list<float>.findCommonSupertypeWith(list<int>)`
   - **Cache hit** - instant return

For a model with hundreds of species and attributes, this can result in thousands of cache hits per compilation.

## Architecture Diagram

```
┌─────────────────────────────────────────────┐
│ Types (Static Utility)                      │
│  • builtInTypes: TypesManager               │
│  • INT, FLOAT, STRING, LIST, etc.           │
└─────────────────┬───────────────────────────┘
                  │
                  │ references
                  ▼
┌─────────────────────────────────────────────┐
│ TypesManager (Built-in)                     │
│  • Cache<TypePair, Boolean> assignability   │
│  • Cache<TypePair, IType> commonSupertype   │
│  • Cache<TypePair, Integer> distance        │
│  • Cache<TypePair, Boolean> translatability │
└─────────────────┬───────────────────────────┘
                  │
                  │ owns & is referenced by
                  ▼
┌─────────────────────────────────────────────┐
│ GamaType (e.g., GamaIntegerType)            │
│  • typesManager: ITypesManager              │
│  • isAssignableFrom() → uses cache          │
│  • findCommonSupertypeWith() → uses cache   │
│  • distanceTo() → uses cache                │
│  • isTranslatableInto() → uses cache        │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│ Model's TypesManager                        │
│  • parent: built-in TypesManager            │
│  • Cache<TypePair, Boolean> assignability   │
│  • Cache<TypePair, IType> commonSupertype   │
│  • Cache<TypePair, Integer> distance        │
│  • Cache<TypePair, Boolean> translatability │
└─────────────────┬───────────────────────────┘
                  │
                  │ owns & is referenced by
                  ▼
┌─────────────────────────────────────────────┐
│ GamaAgentType (Species)                     │
│  • typesManager: ITypesManager (model's)    │
│  • isAssignableFrom() → uses model's cache  │
│  • etc.                                     │
└─────────────────────────────────────────────┘
```

## Debugging

To monitor cache performance, you can enable DEBUG mode in the relevant classes:

```java
// In TypesManager.java
static {
    DEBUG.ON();  // Enable debug output
}
```

## Future Enhancements

Possible improvements for future versions:

1. **Cache Statistics**: Add metrics to measure hit rates and cache effectiveness
2. **Configurable Cache Sizes**: Allow tuning cache size and expiration via preferences
3. **Smart Invalidation**: Invalidate only affected cache entries when types change
4. **Warm-up**: Pre-populate caches with common type relationships during initialization
5. **Persistent Cache**: Consider disk-based caching for frequently used models

## Files Modified

- `IType.java` - Added `getTypesManager()` and `setTypesManager()` methods
- `GamaType.java` - Added `typesManager` field and updated relation methods to use cache
- `ParametricType.java` - Added `typesManager` field and updated relation methods to use cache
- `TypesManager.java` - Added cache infrastructure and set manager on types during initialization
- `ITypesManager.java` - Added cache method signatures
- `TypePair.java` (new) - Cache key implementation

## Testing Recommendations

When testing this feature:

1. Verify types have their manager set after initialization
2. Test with parametric types (`list<int>`, `map<string,float>`, etc.)
3. Verify model-specific types (species) use their model's cache
4. Verify built-in types use the global cache
5. Test cache invalidation on model disposal
6. Verify thread safety in multi-threaded compilation scenarios
7. Measure performance improvements on large models
