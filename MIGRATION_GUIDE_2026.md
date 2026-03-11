# Guide de Migration : GAMA Version 2026 (Eclipse 2025-12 et JDK 25)

Ce document décrit les changements architecturaux et d'API majeurs intervenus entre la version précédente de GAMA (sur la branche `main`) et la version 2026 (sur la branche `Upgrade-to-Eclipse-2025-12-and-JDK-25`).

L'objectif de cette mise à jour massive (plus de 10 000 fichiers modifiés) est de moderniser GAMA, d'améliorer sa compatibilité avec les standards récents (JDK 25, Eclipse 2025-12) et de séparer les détails d'implémentation de l'API publique via la création d'un tout nouveau module `gama.api`.

---

## 1. Changements de Modules et Importations (Création de `gama.api`)

Le changement le plus impactant pour les développeurs d'extensions GAMA est la création du module **`gama.api`**.
Auparavant, la plupart des interfaces publiques et des utilitaires étaient mélangés avec le code d'implémentation dans `gama.core` ou `gama.annotations`.

**Action requise :** La majorité de vos imports pointant vers `gama.core.*` ou `gama.annotations.*` devront être mis à jour vers `gama.api.*`.

### Correspondances des packages principaux :
*   **Types de base :**
    *   `gama.core.common.interfaces.ITyped` $\rightarrow$ `gama.api.gaml.types.ITyped`
    *   `gama.gaml.types.IType` $\rightarrow$ `gama.api.gaml.types.IType`
    *   `gama.gaml.types.ITypesManager` $\rightarrow$ `gama.api.gaml.types.ITypesManager`
*   **Collections :**
    *   `gama.core.util.IList` $\rightarrow$ `gama.api.types.list.IList`
    *   `gama.core.util.graph.IGraph` $\rightarrow$ `gama.api.types.graph.IGraph`
*   **Géométrie et Topologie :**
    *   La classe concrète `GamaPoint` est remplacée par l'interface `IPoint` dans les signatures de l'API (`gama.api.types.geometry.IPoint`).
    *   `gama.core.metamodel.topology.ITopology` $\rightarrow$ `gama.api.types.topology.ITopology`
*   **AST et Expressions :**
    *   `gama.gaml.expressions.IExpression` $\rightarrow$ `gama.api.gaml.expressions.IExpression`
    *   `gama.gaml.statements.IStatement` $\rightarrow$ `gama.api.gaml.statements.IStatement`

---

## 2. Refonte de l'interface `IAgent`

L'interface racine des agents, `IAgent`, a subi de profondes modifications structurelles.

1.  **Changement de Package :**
    *   *Ancien :* `gama.core.metamodel.agent.IAgent`
    *   *Nouveau :* `gama.api.kernel.agent.IAgent`
2.  **Héritage et Géométrie :**
    *   `IAgent` n'hérite plus directement de `IShape` et `IAttributed`.
    *   Il hérite désormais de `IDelegatingShape`, ce qui permet une meilleure séparation des responsabilités concernant la géométrie de l'agent.
3.  **Utilisation des Interfaces Géométriques :**
    *   Les méthodes comme `getLocation()` et `setLocation(...)` retournent et prennent désormais une interface `IPoint` au lieu de la classe concrète `GamaPoint`.
4.  **Gestion des Attributs et Conteneurs :**
    *   `IAgent` n'étend plus `IContainer.Addressable<String, Object>`.
    *   Il étend désormais `IContainer.ToGet<String, Object>`. Les modifications directes d'attributs via l'interface conteneur nécessitent de passer par les mécanismes de variables du GAML (`IVariable`) ou de `IVarAndActionSupport`.
5.  **Propriété `peers` :**
    *   La liste des agents pairs (`getPeers()`) est désormais considérée conceptuellement comme étant en **lecture seule**.
    *   L'implémentation par défaut de la méthode `setPeers(IList<IAgent> peers)` dans `IAgent` est vide. Ne comptez plus sur cette méthode pour modifier le voisinage d'un agent.

---

## 3. Refonte des Populations (`IPopulation` et `AbstractPopulation`)

La gestion interne des populations d'agents a été refactorisée pour réduire la duplication de code et clarifier l'API.

1.  **Introduction de `AbstractPopulation` :**
    *   La logique commune (conteneur d'agents interne, gestion des miroirs, de la topologie, `init` et `step`) a été extraite dans une nouvelle classe abstraite de base : `AbstractPopulation`.
    *   `GamaPopulation` a été refactorisée pour étendre cette classe abstraite.
2.  **Changements dans l'API `IPopulation` :**
    *   **Suppression** : Le prédicat imbriqué `IsLiving` a été retiré.
    *   **Renommage** : La méthode `createAgentAt(...)` est renommée en `createAgentAtIndex(...)`. *Tous les appels existants doivent être mis à jour.*
    *   **Ajout** : Une méthode par défaut `createOneAgent(...)` a été ajoutée.
    *   **Changement de comportement** : La méthode `fireAgentsAdded` n'est plus une méthode par défaut dans l'interface, son implémentation est déléguée aux classes concrètes.

---

## 4. Compilateur et Parseur GAML (`gaml.compiler`)

De très gros changements ont eu lieu dans le compilateur Xtext GAML.

1.  **AST Strictement Typé :**
    *   Création de nombreux éléments syntaxiques formels : `SyntacticAttributeElement`, `SyntacticSpeciesElement`, `SyntacticModelElement`, etc.
2.  **Descriptions :**
    *   Les fabriques (`DescriptionFactory`) et les objets de descriptions (comme `SpeciesDescription`, `ModelDescription`, `StatementDescription`) ont été remaniés et déplacés.
3.  **Ressources et Shaders GLSL :**
    *   Les shaders GLSL (pour le plugin `gama.ui.display.opengl4`) ne sont plus stockés sous forme de chaînes de caractères (String literals) dans le code Java.
    *   Ils doivent obligatoirement être placés dans des fichiers `.vert` et `.frag` dédiés dans un sous-dossier `glsl` du plugin et chargés dynamiquement avec `getClass().getResourceAsStream()`.

---

## 5. Utilitaires et I/O (`gama.api.utils.*`)

La gestion des fichiers et les bibliothèques utilitaires ont été rationalisées et isolées dans `gama.api.utils`.

*   **Refactorisation de `FileUtils` :** Les méthodes d'I/O ont été centralisées. L'utilisation d'anciens utilitaires dispersés doit être remplacée par l'API centralisée dans `gama.api.utils.files.FileUtils`.
*   **Outils Généraux :** De nombreux utilitaires (`GamaPreferences`, `StringUtils`, manipulations `CSV` et `JSON`, générateurs aléatoires `GamaRNG`) ont été déplacés dans `gama.api.utils.*`.
*   **Pattern Matching :** L'utilisation du *pattern matching* Java pour `instanceof` est désormais la norme (introduite avec la migration JDK), ce qui simplifie grandement les tests de types dans les implémentations (`if (obj instanceof MyClass myVar) { ... }`).

---

**Note pour les développeurs CI/CD :**
Veillez à ce que vos environnements de compilation locaux (Maven Tycho) et CI utilisent bien le **JDK 25** (ou supérieur) pour pouvoir compiler cette nouvelle version de GAMA.
