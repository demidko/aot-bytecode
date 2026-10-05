# Token lookup characterization

Baseline: `ba2dd3e272ad181d84e56e4726af7aa03d3c65f0` (2025.02.15).
`bash gradlew test` checks every token, exact invalid-token diagnostics including
null, all 256 byte decoding values, all 65,536 UTF-16 encoding values against an
independent restricted-CP1251 expectation, ё folding, and first-match part of speech.
No enum order, tokens, encoding, dictionary, or public method signatures change.

Run `bash gradlew benchmark -PbenchmarkArgs='-prof gc -rf json -rff docs/baseline.json'`
before changing production. JMH 1.37, 3 forks, 3 x 1 s warmup, 5 x 1 s measurement,
1 GiB heap. Parsing cycles across all tokens with distinct String instances.

`morphology-tags.tsv` was extracted from the baseline Git object, not the modified
parser. It freezes all 71 enum ordinals, names, and token spellings so future
wire-format changes cannot pass just by round-tripping a changed enum.

`parseFresh` constructs a new token from a new character array on each operation,
so its hash is not cached. It includes token construction costs and models newly
parsed input. `parse` measures reused token instances. Baseline-JAR classpath
precedence (`-PbaselineJar=/absolute/path/to/original.jar`) lets both implementations
run with exactly the same updated harness; fixture expectations are not regenerated.

## Results

Temurin 21.0.12.1, Linux x86_64 VM, AMD EPYC 9V74, 5 exposed CPUs.
Means ± JMH 99.9% confidence intervals, three forks.

| Workload | Baseline ns/op | Candidate ns/op | Baseline B/op | Candidate B/op |
| --- | ---: | ---: | ---: | ---: |
| parse | 53.40 ± 2.35 | 5.67 ± 0.24 | 304.00 | 0.00 |
| parseFresh | 63.90 ± 0.97 | 27.03 ± 1.43 | 379.49 | 75.49 |

The map trades a small persistent 71-entry structure for elimination of the cloned
enum array and linear search on every call. This is a parser microbenchmark, not
an end-to-end compiler speedup: aot-compiler still pins the older 2021.10.26 API.
The baseline for fresh tokens uses a JAR built from unchanged commit
`ba2dd3e272ad181d84e56e4726af7aa03d3c65f0` with the same harness and JDK.

## Independent review

A separate read-only reviewer found no production/API regression, token/ordinal
change, or benchmark dead-code-elimination defect. Both benchmark methods return
their result to generated JMH consumption. Reused-token results benefit from
cached hashes and must not replace the fresh-token result in general parsing claims.
A compiler-wide speedup remains unmeasured. The 71-entry map is safely published by
class initialization and never mutated afterward.

The baseline override now rejects missing or unrelated archives instead of silently
loading the candidate. It also supports `test`, allowing the same four compatibility
tests to run against the original artifact. The review reruns them against both
versions. No production source changed during this follow-up review.
