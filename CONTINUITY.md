# Continuity Ledger

## Goal
Fix "No population of Base is accessible in the context of Simulation 0" runtime error when using micro-model (co-model) species.

## Constraints/Assumptions
- Micro-models are imported via `import "..." as Ant;` in GAML
- The runtime needs to see micro-model species in `GamlModelSpecies.getAllSpecies()` 
- Changes must not break regular (non-comodel) simulations

## Key Decisions
- Root cause identified: `ModelDescription.visitChildren()` never iterated over `microModels` map
- The `addChild()` chain for a `ModelDescription` child drops it silently because `isSpecies()` returns false and the ultimate `IDescription.addChild()` default is a no-op
- Micro-models were stored ONLY in `microModels` map, but never compiled into runtime species
- Fix: Add micro-model iteration to `visitChildren()` and `visitOwnChildren()` in `ModelDescription.java`

## State

### Done
- Traced full description → compilation → runtime chain
- Identified that `IDescription.addChild()` default is empty — micro-models were silently dropped
- Fixed `ModelDescription.visitChildren()` and `visitOwnChildren()` to iterate over `microModels`
- Added `v.setAlias(k)` before `model.addChildren()` in `ModelFactory.java`
- Added `addSpeciesTypeAs()` to `ITypesManager` / `TypesManager` for type registration
- Built `gaml.compiler` and `gama.core` successfully

### Now
- Awaiting user test of the runtime behavior

### Next
- Remove debug logging from `GamlModelSpecies.setChildren()` and `CreateStatement.java`
- Verify micro-model species appears in `getAllSpecies()` map at runtime

## Open Questions
- Will the compiled micro-model `GamlModelSpecies` correctly match `case ISpecies` in `GamlSpecies.setChildren()` and populate `microSpecies` map?

## Working Set
- `/Users/hqnghi/git/gama/gaml.compiler/src/gaml/compiler/descriptions/ModelDescription.java` (visitChildren fix)
- `/Users/hqnghi/git/gama/gaml.compiler/src/gaml/compiler/factories/ModelFactory.java` (alias + type registration)
- `/Users/hqnghi/git/gama/gama.api/src/gama/api/kernel/species/GamlModelSpecies.java` (debug logging)
- `/Users/hqnghi/git/gama/gama.core/src/gama/gaml/statements/CreateStatement.java` (debug logging)
