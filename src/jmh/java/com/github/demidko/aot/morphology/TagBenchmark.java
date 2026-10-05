package com.github.demidko.aot.morphology;

import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class TagBenchmark {
  private String[] tokens;
  private int cursor;
  @Setup public void setup() {
    MorphologyTag[] tags = MorphologyTag.values();
    tokens = new String[tags.length];
    for (int i = 0; i < tags.length; i++) tokens[i] = new String(tags[i].toString());
  }
  @Benchmark public MorphologyTag parseFresh() {
    // New character content prevents reuse of String's cached hash.
    String token = new String(tokens[cursor].toCharArray());
    if (++cursor == tokens.length) cursor = 0;
    return MorphologyTag.fromString(token);
  }
  @Benchmark public MorphologyTag parse() {
    String token = tokens[cursor];
    if (++cursor == tokens.length) cursor = 0;
    return MorphologyTag.fromString(token);
  }
}
