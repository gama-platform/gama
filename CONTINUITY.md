# Ledger Snapshot

- **Goal**: Move micro-model (co-model) population management from the simulation agent level to the experiment agent level.
- **Constraints/Assumptions**: Compile-time description hierarchy remains unchanged (micro-models are still children of ModelDescription for species lookup). Only the RUNTIME population storage/lookup changes.
- **Key decisions**: 
    1. Reverted all compile-time restructuring (ModelDescription visitors, addChild, ExperimentDescription).
    2. Made only 2 targeted runtime changes:
       - `CreateStatement.findPopulation()`: stores extern micro populations on `scope.getExperiment()` instead of `executor` (simulation agent).
       - `GamlAgent.getPopulationFor(ISpecies)`: looks up extern micro populations from `scope.getExperiment()` instead of `getSimulation()`.
- **State**: 
  - **Done**: 
    - Reverted ModelDescription.addChild, visitMicroSpecies, visitChildren, visitOwnChildren, getMicroSpecies
    - Reverted ModelFactory to original model.addChildren(mm.values())
    - Reverted ExperimentDescription.visitMicroSpecies to return true
    - Changed CreateStatement.findPopulation() runtime target to experiment agent
    - Changed GamlAgent.getPopulationFor(ISpecies) runtime lookup to experiment agent
    - Fixed corrupted GamlAgent.java with clean rewrite
    - TypesManager idempotent addSpeciesType and VariableDescription null-safety still in place
    - All 3 modules build successfully (gama.api, gaml.compiler, gama.core)
  - **Now**: Awaiting user test
  - **Next**: Verify runtime behavior, clean up debug logs if any remain
- **Open questions**: None
- **Working set**:
    - `/Users/hqnghi/git/gama/gama.core/src/gama/core/agent/GamlAgent.java`
    - `/Users/hqnghi/git/gama/gama.core/src/gama/gaml/statements/CreateStatement.java`
    - `/Users/hqnghi/git/gama/gaml.compiler/src/gaml/compiler/descriptions/ModelDescription.java`
    - `/Users/hqnghi/git/gama/gaml.compiler/src/gaml/compiler/factories/ModelFactory.java`
    - `/Users/hqnghi/git/gama/gaml.compiler/src/gaml/compiler/descriptions/ExperimentDescription.java`
    - `/Users/hqnghi/git/gama/gama.api/src/gama/api/gaml/types/TypesManager.java`
    - `/Users/hqnghi/git/gama/gaml.compiler/src/gaml/compiler/descriptions/VariableDescription.java`
