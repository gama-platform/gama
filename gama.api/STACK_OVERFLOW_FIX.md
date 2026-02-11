# Stack Overflow Fix - Type Relation Caching

## Problem Identified

The initial implementation caused a **stack overflow** due to infinite recursion between:
- `GamaType.isAssignableFrom()` → `TypesManager.checkAssignability()` → `type.isAssignableFrom()` → ...

```
Stack trace:
at gama.api.gaml.types.GamaType.isAssignableFrom(GamaType.java:310)
at gama.api.gaml.types.TypesManager.checkAssignability(TypesManager.java:342)
(repeating infinitely)
```

## Root Cause

The TypesManager cache methods were calling the **public cached methods** on types:
```java
// TypesManager.checkAssignability - WRONG!
final boolean result = from.isAssignableFrom(to);  // ← Calls back to cached method
```

This created circular delegation:
1. Type's public method delegates to manager's cache method
2. Manager's cache method calls type's public method
3. Loop infinitely

## Solution Implemented

### 1. Added Internal Uncached Methods

Added **protected internal computation methods** to break the recursion cycle:

#### In GamaType:
- `computeIsAssignableFrom(IType)` - Uncached assignability check
- `computeIsTranslatableInto(IType)` - Uncached translatability check
- `computeDistanceTo(IType)` - Uncached distance computation
- `computeFindCommonSupertypeWith(IType)` - Uncached common supertype finding

#### In ParametricType:
- Same four internal methods adapted for parametric types

#### In GamaNoType:
- Overridden `computeIsTranslatableInto()` - Specialized behavior
- Overridden `computeFindCommonSupertypeWith()` - Specialized behavior

### 2. Updated TypesManager

Modified cache methods to call **internal uncached methods**:

```java
// TypesManager.checkAssignability - CORRECT!
public boolean checkAssignability(final IType<?> from, final IType<?> to) {
    // ... fast paths ...
    
    // Call INTERNAL method to avoid recursion
    boolean result;
    if (from instanceof ParametricType) {
        result = ((ParametricType) from).computeIsAssignableFrom(to);
    } else if (from instanceof GamaType) {
        result = ((GamaType<?>) from).computeIsAssignableFrom(to);
    } else {
        result = from == to;  // Fallback
    }
    assignabilityCache.put(key, result);
    return result;
}
```

### 3. Helper Methods for Type Hierarchy Navigation

Added static helper methods in GamaType to handle types that might not be GamaType or ParametricType:

- `checkAssignabilityDirect(IType, IType)` - Direct assignability without cache
- `checkTranslatabilityDirect(IType, IType)` - Direct translatability without cache
- `computeDistanceToHelper(IType)` - Helper for distance computation
- `computeCommonSupertypeHelper(IType, IType)` - Helper for common supertype

These helpers properly dispatch to:
- `computeXxx()` methods if the type is GamaType or ParametricType
- Public methods otherwise (as fallback)

## Call Flow After Fix

### Successful Flow:
```
User code: floatType.isAssignableFrom(intType)
    ↓
GamaType.isAssignableFrom(intType)
    ↓
if (typesManager != null) → YES
    ↓
typesManager.checkAssignability(this, intType)
    ↓
Check cache → MISS
    ↓
((GamaType) floatType).computeIsAssignableFrom(intType)  ← INTERNAL METHOD
    ↓
return this == intType || isSuperTypeOf(intType)  ← Direct computation
    ↓
Store in cache and return
```

### Subsequent Call (Cache Hit):
```
User code: floatType.isAssignableFrom(intType)
    ↓
GamaType.isAssignableFrom(intType)
    ↓
typesManager.checkAssignability(this, intType)
    ↓
Check cache → HIT → return instantly ✓
```

## Verification of Specialized Methods

### GamaNoType Overrides

✅ **Verified and preserved:**
- `findCommonSupertypeWith()` - Always returns `this` (NO_TYPE is supertype of all)
- `isTranslatableInto()` - Cannot translate to BOOL, INT, or FLOAT

Both now have:
- `computeXxx()` methods with the specialized logic
- Public methods that delegate to cache when available

### ParametricType Overrides

✅ **Verified and preserved:**
- `isAssignableFrom()` - Checks base type, content type, and key type
- `isTranslatableInto()` - Similar multi-part check
- `distanceTo()` - Sum of distances for base, content, and key types
- `findCommonSupertypeWith()` - Complex logic for container types

All methods now have:
- `computeXxx()` internal methods with the original logic
- Proper handling of recursive calls to avoid stack overflow

## Files Modified

1. **GamaType.java**
   - Added 4 protected `computeXxx()` methods
   - Added 4 static helper methods
   - Updated public methods to delegate to cache

2. **ParametricType.java**
   - Added 4 public `computeXxx()` methods
   - Handles both GamaType and non-GamaType component types
   - Updated public methods to delegate to cache

3. **GamaNoType.java**
   - Overridden `computeIsTranslatableInto()`
   - Overridden `computeFindCommonSupertypeWith()`
   - Updated public methods to delegate to cache

4. **TypesManager.java**
   - Updated 4 cache methods to call internal `computeXxx()` methods
   - Added type checking (instanceof) to dispatch correctly
   - Added fallbacks for non-GamaType implementations

## Testing Recommendations

1. ✅ Verify no stack overflow on basic type checks
2. ✅ Test GamaNoType specialized behavior is preserved
3. ✅ Test ParametricType complex logic works correctly
4. ✅ Verify cache hits occur on repeated checks
5. ✅ Test with deeply nested type hierarchies
6. ✅ Test with custom type implementations (non-GamaType)

## Summary

The fix successfully breaks the recursion cycle by:
1. Separating **public cached methods** from **internal uncached computation**
2. Having TypesManager call the **internal methods** directly
3. Preserving all specialized behavior in subclasses
4. Providing proper fallbacks for non-standard type implementations

The architecture is now:
- **Public methods**: Entry points that use cache via TypesManager
- **Internal methods**: Pure computation logic without cache lookups
- **TypesManager**: Orchestrates caching and calls internal methods

**Result:** No stack overflow, proper caching, and all specialized behaviors preserved! ✓
