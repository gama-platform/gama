# RDS serialization (version 2)

`gama.extension.rds` provides `rds_file` and `save ... format: "rds"` independently
of the statistics extension. It is included in the standard desktop and headless
distributions. No installed R runtime is needed.

## Usage

Export input from R with an explicit serialization version:

```r
saveRDS(object, "data.rds", version = 2)
```

Read and write it in GAML:

```gaml
dataframe table <- dataframe(rds_file("data.rds"));
save table to: "output.rds" format: "rds";

file vectors <- rds_file("vectors.rds", [1, 2, 3]);
save vectors;
list values <- list(rds_file("vectors.rds"));
```

Modern R reads GAMA exports with `readRDS("output.rds")`. Input may be gzip
compressed or uncompressed; output is always gzip-compressed XDR version 2.
Version 3, workspace files (`save`/`.RData`), and other compression formats are
rejected. Re-export unsupported input using the `saveRDS` command above.

## Conversion contract

| R value | GAMA value |
| --- | --- |
| Integer, numeric, logical, character vectors | Typed lists, including empty and length-one vectors |
| Unnamed lists | Lists of recursively converted values |
| Named lists with unique non-empty names | String-keyed maps |
| Two-dimensional atomic matrices without dimension names | Matrices with the same cell placement |
| Dataframes | Mutable dataframes with nullable columns |
| Factors / ordered factors | Lists of string labels; levels and ordering are not retained |
| `NA` / nested `NULL` | `nil` |

Dataframe row names are not retained. Unsupported classes, attributes, vector
types (including complex/raw), named atomic vectors, and matrix dimension names
produce errors rather than string fallbacks. Only list, map, matrix and dataframe
roots are accepted by the file/saver interface; a top-level R `NULL` is rejected.
Empty matrices must be `0 x 0`: GAMA cannot preserve `0 x n` or `n x 0` dimensions.

GAMA homogeneous scalar lists become atomic R vectors; heterogeneous/nested
lists become R lists. Scalars inside maps/lists become R length-one vectors and
therefore return as lists, not scalars. Empty untyped lists remain R lists.
Maps require non-empty string keys. Cyclic containers and unsupported objects
are rejected.

Missing logical and numeric cells remain missing, and numeric `NaN` remains
distinct from `NA`. Matrices with missing cells use GAMA object matrices because
primitive matrices cannot retain `nil`. Such matrices may lose their original
atomic type; all-missing object matrices are exported as numeric matrices.
GAML's own primitive casts may coerce `nil` before it reaches the saver; those
already-coerced values cannot be recovered. Integer `-2147483648` is exported
as numeric to avoid R's reserved integer-NA sentinel. Java `Long` values outside
the inclusive range `[-2^53, 2^53]` are rejected instead of rounded.

Each save writes one complete object immediately. `rewrite: false` permits a new
destination but rejects an existing file: appending is not supported. Conversion
and serialization complete in a temporary file before replacing a destination.
Text encoding, separators, headers and text buffering do not apply to this format.

## Agent snapshots

```gaml
save my_agents to: "agents.rds" format: "rds" attributes: ["energy", "age"];
save my_agents to: "agents.rds" format: "rds" attributes: ["double_energy"::energy * 2];
```

Agent collections become dataframes with `name`, `x`, `y`, `z` and selected
attribute columns. Without `attributes`, saveable attributes are selected using
GAMA's standard exclusions. Selected values must themselves be supported.
Empty typed agent collections are supported; mixed species, nil/non-agent
entries, unknown attributes and collisions with base column names are rejected.
Snapshots load as dataframes, not reconstructed agents.

## Navigator

RDS files use a violet R icon supplied by this plugin through an optional
workbench label decorator; headless use does not require the UI.

RDS metadata reads only the stream header: serialization version, compression
(gzip or none), stream encoding, and writer/minimum-reader R versions. The
navigator suffix displays the version and compression. No R object is
deserialized or converted, and no object type, dimensions, schema, missing-value
counts or file size are computed.

The plugin contributes its content type and metadata implementation through the
existing extension mechanism and cache, with no additional background tasks or
RDS-specific handling in shared code. Version-3 headers are reported as
unsupported; a readable version-2 header does not guarantee that its payload is
valid or contains a supported object.

## Validation

`models/Data/RDS Files` contains an executable usage example.

The `models/Tests/RDS Tests` project is discovered by GAMA's model test runner.
It includes pinned native-R-generated fixtures with provenance and license
information in `includes/NOTICE.txt`.

For standalone stream checks, from this plugin directory:

```sh
mkdir -p target/rds-tests target/interoperability
javac -cp 'libs/*' -d target/rds-tests src/gama/extension/rds/RdsIO.java tests/gama/extension/rds/RdsIOTest.java
java -cp 'target/rds-tests:libs/*' gama.extension.rds.RdsIOTest target/interoperability
```

For a native R interoperability check (R is needed only for this optional check):

```sh
Rscript tests/interoperability.R generate target/interoperability
java -cp 'target/rds-tests:libs/*' gama.extension.rds.RdsIOTest target/interoperability
Rscript tests/interoperability.R verify target/interoperability
```

`RdsConverterTest` is a standalone regression entry point using compiled
API/core/dataframe/RDS/compiler bundles and their dependencies. Run it with
`--add-modules jdk.incubator.vector`; optional arguments are paths to additional
RDS fixtures. It checks nullable schemas, matrix layout, singleton vectors,
numeric boundaries, nested values, explicit errors and destination preservation.
