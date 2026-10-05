## Summary

Replace the per-call enum-array clone and linear scan in `MorphologyTag.fromString()` with a private immutable token map. Preserve exact matching, first-match behavior, null handling, and error messages.

## Changes

- Build the token index once without changing enum ordinals or token spellings.
- Add frozen ordinal/token fixtures and comprehensive encoding/parser tests.
- Add reproducible JMH measurements for reused tokens and newly constructed tokens.
- Provide English and Russian README files.

## Validation and independent review

The source baseline is `ba2dd3e272ad181d84e56e4726af7aa03d3c65f0`. Baseline tests and measurements were committed before production changes.

All four compatibility tests pass against both the original artifact and candidate. They cover all 71 enum ordinals and tokens, invalid/null diagnostics, all 256 byte values, all 65,536 UTF-16 encoding inputs, ё folding, and first-match part-of-speech selection.

A separate reviewer found no production/API regression or benchmark dead-code-elimination issue. Generated JMH loops consume the results. Missing or unrelated baseline archives now fail explicitly rather than silently loading candidate classes. No dictionary, encoding rule, enum ordinal, or public method signature changes.

## Performance

JMH 1.37, three forks, three 1-second warmups and five 1-second measurements per fork, fixed 1 GiB heap. Both implementations use the same harness and JDK. Means `±` 99.9% confidence intervals:

| Workload | Baseline ns/op | Candidate ns/op | Baseline B/op | Candidate B/op |
| --- | ---: | ---: | ---: | ---: |
| Reused token | 53.40 ± 2.35 | 5.67 ± 0.24 | 304.00 | 0.00 |
| Newly constructed token | 63.90 ± 0.97 | 27.03 ± 1.43 | 379.49 | 75.49 |

Raw results and reproduction instructions are in [docs/compatibility.md](https://github.com/fluffy-manul/aot-bytecode/blob/characterize-tag-lookup/docs/compatibility.md).

## Limitations

The map adds a small persistent 71-entry structure. Reused-token results benefit from cached hashes; the fresh-token workload includes token construction and measures uncached input. Neither is an end-to-end compiler speedup. `aot-compiler` still pins the older `2021.10.26` API and does not automatically receive this change.
