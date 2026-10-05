# Aot Bytecode

[Русская версия](README.ru.md)

Shared morphology tags, parts of speech, and character encoding for the
[Aot Russian morphological analyzer](https://github.com/demidko/aot) and its
[dictionary compiler](https://github.com/demidko/aot-compiler).

Use a published version from [JitPack](https://jitpack.io/#demidko/aot-bytecode).
The analyzer normally supplies this dependency transitively.

`MorphologyTag.fromString("С")` returns the noun tag. Parsing is exact and
case-sensitive; unknown tokens, including `null`, throw `IllegalArgumentException`.
`PartOfSpeech.partOfSpeech(tags)` returns the first matching part of speech, or
`null` when none is present.

The binary encoding is a restricted subset of Windows-1251, with a dedicated
end-of-line marker. Encoding folds `ё` into `е`; it is not a general-purpose text
codec. Enum ordinals are part of the dictionary format: reordering tags can change
how an existing dictionary is interpreted.

Run `bash gradlew test` with a compatible JDK. See
[compatibility tests and benchmarks](docs/compatibility.md) for the complete input
checks and reproducible performance measurements. Code is [MIT licensed](LICENSE).
