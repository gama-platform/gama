# Dynamic Import System for GAMA Annotation Processing

## Overview

The GAMA annotation processing system now supports dynamic imports that allow each plugin to contribute its own packages to the generated `GamlAdditions` classes. This eliminates the need to use fully qualified class names in generated code, making it more readable and maintainable.

## Key Features

✅ **Automatic Package Discovery**: Discovers packages from annotated elements during processing  
✅ **Proper Import Syntax**: Generates correct wildcard imports (e.g., `import package.name.*;`)  
✅ **Duplicate Prevention**: Intelligent deduplication prevents duplicate imports  
✅ **Simple Name Generation**: Generated code automatically uses simple class names when packages are imported  
✅ **Manual Registration**: Processors can explicitly register additional packages  
✅ **Backward Compatibility**: Existing processors work unchanged  

## How It Works

### 1. Automatic Package Discovery

The system automatically discovers packages from annotated elements during processing:
- Packages of classes containing annotated elements
- Return types of annotated methods
- Parameter types of annotated methods
- Field types of annotated fields

### 2. Manual Package Registration

Processors can also manually register packages using utility methods:

```java
public class MyProcessor extends ElementProcessor<MyAnnotation> {
    
    @Override
    public void createElement(StringBuilder sb, Element e, MyAnnotation annotation) {
        // Register a specific package for import
        registerPackageForImport("com.example.myplugin.operators");
        
        // Register a static import
        registerStaticImport("com.example.myplugin.utils.HelperClass");
        
        // Or use the convenience method that registers and returns simple name
        String className = registerAndGetClassName("com.example.myplugin.MyClass");
        
        // Generate code using simple class names automatically
        sb.append(toClassObject("com.example.myplugin.operators.MyOperator")); 
        // → "MyOperator.class" (not "com.example.myplugin.operators.MyOperator.class")
        
        sb.append(getClassName("com.example.myplugin.types.MyType"));
        // → "MyType" (not "com.example.myplugin.types.MyType")
    }
}
```

### 3. Generated Import Structure

The generated `GamlAdditions` class will include properly formatted imports:
- Standard GAMA imports (from Constants.COLLECTIVE_IMPORTS)
- Plugin-specific packages discovered automatically
- Manually registered packages
- Static imports for utility classes
- **No duplicates**
- **Correct syntax with `.*` wildcards**

Example generated imports:
```java
import static gama.api.gaml.types.Cast.*;
import static com.example.myplugin.utils.HelperClass.*;

import java.util.*;
import gama.api.*;
import gama.api.gaml.expressions.*;
import com.example.myplugin.operators.*;
import com.example.myplugin.types.*;

public class GamlAdditions extends gama.api.additions.AbstractGamlAdditions {
    // Generated registration code with simple class names
    _operator(..., MyOperator.class, ...);  // Not com.example.myplugin.operators.MyOperator.class
}
```

## Fixed Issues (Updated)

### ✅ Issue 1: Incorrect Import Syntax
**Problem**: Imports were generated as `import package ` without proper wildcard syntax, missing semicolons.  
**Solution**: 
- Import normalization ensures proper `.*` wildcard syntax
- Fixed GamaProcessor to add semicolons to all imports
- Proper conversion from Constants format (`.`) to Java import format (`.*`)
- Fixed package extraction to use `PackageElement.getQualifiedName()` instead of `toString()`

### ✅ Issue 2: Duplicate Imports  
**Problem**: The same package could be imported multiple times.  
**Solution**: 
- Intelligent deduplication using `LinkedHashSet` 
- Proper checking against existing imports in both Constants format and dynamic format
- Enhanced `shouldAddPackage()` logic to prevent conflicts

### ✅ Issue 3: Fully Qualified Names in Generated Code
**Problem**: Generated code still used fully qualified class names even when packages were imported.  
**Solution**: 
- Enhanced `toClassObject()` method automatically uses simple names when packages are imported
- Added `getClassName()` method for direct class name usage
- Context-aware name resolution with fallback to qualified names when needed

### ✅ Issue 4: "package" Prefix in Imports (New)
**Problem**: Some imports appeared as `import package gama.extension.image.*` with unwanted "package" prefix.  
**Solution**: 
- Fixed `extractPackageFromElement()` to use `PackageElement.getQualifiedName()` 
- Added fallback logic to strip "package " prefix from toString() results
- Proper package element detection and handling

## Implementation Details

### ProcessorContext Enhancements

- `addDynamicCollectiveImport(String packageName)`: Adds a package import with proper syntax normalization
- `addDynamicStaticImport(String className)`: Adds a static import with proper syntax normalization
- `getAllCollectiveImports()`: Returns combined standard + dynamic imports (deduplicated)
- `getAllStaticCollectiveImports()`: Returns combined standard + dynamic static imports (deduplicated)
- `isPackageImported(String packageName)`: Checks if a package is available for simple name usage
- `getClassNameForGeneration(String fullyQualifiedName)`: Returns simple name if package imported, qualified otherwise
- `discoverPluginPackages()`: Automatically discovers packages from processed elements

### ElementProcessor Utilities

- `registerPackageForImport(String packageName)`: Convenience method for processors
- `registerStaticImport(String className)`: Convenience method for static imports
- `getClassName(String fullyQualifiedName)`: Gets appropriate class name for code generation
- `registerAndGetClassName(String fullyQualifiedName)`: Combines registration with name generation
- `toClassObject(String className)`: **Now automatically uses simple names when possible**

### Processing Flow

1. **Element Processing**: All processors handle their annotated elements
2. **Package Registration**: Manual and automatic package registration occurs
3. **Package Discovery**: System automatically discovers packages from processed elements
4. **Import Generation**: Dynamic imports are combined with standard imports and deduplicated
5. **Code Generation**: Generated code automatically uses simple class names where packages are imported

## Benefits

1. **Cleaner Generated Code**: Simple class names instead of fully qualified names
2. **Correct Import Syntax**: Proper wildcard imports that actually work
3. **No Duplicates**: Clean import sections without redundant entries
4. **Better Maintainability**: Generated code is more readable and easier to debug
5. **Plugin Isolation**: Each plugin contributes only its own packages
6. **Automatic Discovery**: No manual configuration required for most cases
7. **Backward Compatibility**: Existing code continues to work unchanged

## Usage Examples

### Enhanced Operator Processor

The `OperatorProcessor` automatically registers packages and generates clean code:

```java
// In OperatorProcessor.createElement() - automatic registration
String packageName = extractPackageFromClassName(declClass);
if (packageName != null) {
    registerPackageForImport(packageName);  // Package registered
}

// Later in code generation
sb.append(toClassObject(returnType));  // Automatically uses simple name if imported!
// Result: "MyClass.class" instead of "com.myplugin.MyClass.class"
```

### Custom Processor Example

```java
public class CustomProcessor extends ElementProcessor<CustomAnnotation> {
    
    @Override
    public void createElement(StringBuilder sb, Element e, CustomAnnotation annotation) {
        // Register your plugin's packages
        registerPackageForImport("com.myplugin.core");
        registerPackageForImport("com.myplugin.utils");
        registerStaticImport("com.myplugin.Constants");
        
        // All methods now automatically use simple names
        sb.append(toClassObject("com.myplugin.core.MyClass"));     // → "MyClass.class"
        sb.append(getClassName("com.myplugin.utils.Helper"));      // → "Helper"
        sb.append("new ").append(getClassName("com.myplugin.core.Builder")); // → "new Builder"
        
        // Or use the convenience method
        String className = registerAndGetClassName("com.otherplugin.SomeClass"); // → "SomeClass"
    }
}
```

## Migration Guide

For existing processors, **no changes are required** - the system is fully backward compatible. However, to take advantage of the new functionality:

1. **Automatic Benefits**: Existing `toClassObject()` calls will automatically start using simple names when packages are discovered
2. **Enhanced Usage**: Use `registerPackageForImport()` for plugin-specific packages
3. **Code Generation**: Use `getClassName()` for direct class name usage in generated code
4. **Test Generated Output**: Verify that imports are correctly generated and code compiles

The automatic package discovery and enhanced `toClassObject()` method should provide immediate benefits without any code changes required.