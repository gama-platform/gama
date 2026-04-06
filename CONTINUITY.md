# Continuity Ledger

## Goal
Fix "No population of Base is accessible in the context of Simulation 0" runtime error when using micro-model (co-model) species, and fix missing population variables like `plotWeather` in micro-models.

## Constraints/Assumptions
- Micro-models are imported via `import "..." as Ant;` in GAML
- The runtime needs to see micro-model species in `GamlModelSpecies.getAllSpecies()` 
- Changes must not break regular (non-comodel) simulations
- Population variables (e.g., `list<plotWeather>`) for micro-species within micro-models must be correctly generated.

## Key Decisions
- Overrode `visitMicroSpecies` in `ModelDescription` to iterate over both regular micro-species and `microModels`. This allows `initializeMirrorsAndSubSpecies` to inject population variables for micro-species within the micro-models.
- Overrode `getMicroSpecies(String)` in `ModelDescription` to check the `microModels` map. This fixes a `NullPointerException` during dependency validation, as the `VariableDescription` lookup for the synthetic population variable needs to fetch the micro-model.

## State

### Done
- Traced full description → compilation → runtime chain
- Fixed `ModelDescription.visitChildren()` and `visitOwnChildren()` to iterate over `microModels`
- Added `v.setAlias(k)` before `model.addChildren()` in `ModelFactory.java`
- Added `addSpeciesTypeAs()` to `ITypesManager` / `TypesManager` for type registration
- Fixed `ModelDescription.visitMicroSpecies()` to include `microModels`
- Fixed NPE by overriding `ModelDescription.getMicroSpecies(String)`
- Built `gaml.compiler` and `gama.core` successfully

### Now
- Awaiting user test of the comodel attributes access.

### Next
- Remove debug logging from `GamlModelSpecies.setChildren()` and `CreateStatement.java`

## Open Questions
- None.

## Working Set
- `/Users/hqnghi/git/gama/gaml.compiler/src/gaml/compiler/descriptions/ModelDescription.java` (visitChildren, visitMicroSpecies, getMicroSpecies fixes)
- `/Users/hqnghi/git/gama/gaml.compiler/src/gaml/compiler/factories/ModelFactory.java` (alias + type registration)
- `/Users/hqnghi/git/gama/gama.api/src/gama/api/kernel/species/GamlModelSpecies.java` (debug logging)
- `/Users/hqnghi/git/gama/gama.core/src/gama/gaml/statements/CreateStatement.java` (debug logging)
