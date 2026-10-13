# oquo-scale-conversion-creator

Java utility to include scale conversions in [OQUO](https://github.com/tecnomod-um/oquo)-based modules from TSV files.

Ontology quality metrics produce raw values on very different scales (ratios, counts, percentages...). OQUO harmonises
them by converting each raw value to the five-point OQuaRE scale (`oquo:OquareFiveCategoriesScale`):

| Category | Meaning |
|---|---|
| 1 | Not acceptable |
| 2 | Insufficient |
| 3 | Minimally acceptable |
| 4 | Acceptable |
| 5 | Exceeds the requirements |

These conversions are modelled in the ontology as `oquo:ScaleConversion` instances with an input scale, an output scale
and a set of point mappings. Writing them by hand is tedious and error-prone, so this tool reads them from a TSV file and
adds them to an OQUO module, such as [oquo-huron](https://purl.org/oquo-huron).

## Requirements

- Java 17 or later
- Maven 3

## Build

```bash
mvn package
```

This runs the tests and produces `target/oquo-scale-conversion-creator-0.0.1-jar-with-dependencies.jar`.

## Usage

```bash
java -jar target/oquo-scale-conversion-creator-0.0.1-jar-with-dependencies.jar \
    -t scales.tsv \
    -i https://purl.org/oquo-huron \
    -o oquo-huron.owl \
    --overwrite-scales
```

| Option | Description |
|---|---|
| `-t`, `--tsv-file` | TSV file with the conversions (see [TSV format](#tsv-format)). Required. |
| `-i`, `--ontology` | IRI of the ontology to add the conversions to, e.g. `https://purl.org/oquo-huron` or `file:///path/to/module.owl`. A local file must be given as a `file://` IRI, not as a plain path. Required. |
| `-o`, `--output` | File where the modified ontology is saved, in the same format as the input. Required. |
| `--overwrite-scales` | Remove the existing conversions between the input scale and the output scale before adding the new one. Without this option, the new conversion is added next to any existing one. |
| `--create-metric` | Create the metric if it does not exist, as a subclass of `qm:DerivedMeasure`. |
| `--create-input-scale` | Create the input scale if it does not exist, as an `oquo:RawScale` and `om:Ratio_scale` individual. |
| `-h`, `--help` | Show the help and exit. |
| `-V`, `--version` | Show the version and exit. |

The ontology's imports are loaded too, so metrics and scales defined in imported modules are found. The output scale,
`oquo:OquareFiveCategoriesScale`, is defined in `oquo-core`, so the ontology must import it, directly or indirectly.

## TSV format

The file is tab-separated. Values may be quoted. The first line is a header and is ignored. Each of the other lines
describes one conversion with these columns, in this order:

| Column | Content |
|---|---|
| `metric` | IRI of the metric. |
| `input_scale` | IRI of the scale of the raw metric values. |
| `output_scale` | Always `https://purl.org/oquo#OquareFiveCategoriesScale`. |
| `1` ... `5` | Raw values assigned to each OQuaRE category (see below). |

Extra columns after the eighth, such as notes, are ignored.

Example (one row per conversion; in the file the columns are separated by tabs):

| metric | input_scale | output_scale | 1 | 2 | 3 | 4 | 5 | Notes |
|---|---|---|---|---|---|---|---|---|
| `https://purl.org/oquo#NamesPerPropertyMetric` | `https://purl.org/oquo#NamesPerPropertyMetricScale` | `https://purl.org/oquo#OquareFiveCategoriesScale` | `[0, 0.1)` | `[0.1, 0.97)` | `[0.97, 1.03)` | `[1.03, 1.5)` | `[1.5, Inf)` | |
| `https://purl.org/oquo#SynonymsPerPropertyMetric` | `https://purl.org/oquo#SynonymsPerPropertyMetricScale` | `https://purl.org/oquo#OquareFiveCategoriesScale` | `[0, 0.1)` | `[0.1, 0.6)` | `[0.6, 1.1)` | `[1.1, 2)` | `[2, Inf)` | |

### Ratio input scales

When the input scale is an `om:Ratio_scale`, columns `1` to `5` contain intervals of raw values:

- `[` and `]` are closed bounds; `(` and `)` are open bounds.
- `Inf` and `-Inf` (case insensitive) are positive and negative infinity.
- `-` means that the category has no interval.

For example, `[0.97, 1.03)` maps every value `v` with `0.97 <= v < 1.03` to that category.

The tool creates an `oquo:CardinalToOrdinalScaleConversion` with one `oquo:CardinalToOrdinalScalePointMapping` per
category. Each mapping links an `oquo:Interval` (`oquo:hasInputCardinalInterval`) to the OQuaRE category
(`oquo:hasOutputScaleCategory`).

### Ordinal input scales

When the input scale is an `om:Ordinal_scale`, columns `1` to `5` contain the IRI of the input scale item mapped to each
OQuaRE category. Empty columns are ignored.

The tool creates an `oquo:OrdinalToOrdinalScaleConversion` with one `oquo:OrdinalToOrdinalScalePointMapping` per
non-empty column. Each mapping links the input item (`oquo:hasInputScaleCategory`) to the OQuaRE category
(`oquo:hasOutputScaleCategory`). The item IRIs are not checked, so they must be full IRIs of existing items.

## Validation

Each line is checked before the ontology is modified. If a check fails, the tool prints a message, skips the line and
goes on with the next one. A skipped line creates no entities and removes no conversions, even with
`--overwrite-scales`.

A line is skipped when:

- it has fewer than eight columns;
- the metric or the input scale does not exist and the corresponding `--create-*` option was not given;
- the output scale is not `oquo:OquareFiveCategoriesScale`, or it is not found in the ontology;
- the input scale is neither a ratio scale nor an ordinal scale;
- for ratio scales, any of the five intervals is empty or invalid, e.g. `[0)`, `[a, 1)` or `[2, 1)`.

## Tests

```bash
mvn test
```

The tests run offline against a small OQUO-like ontology in `src/test/resources/test-module.ttl`.

## License

This project is licensed under the GNU General Public License v3.0. See [LICENSE](LICENSE).
