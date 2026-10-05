package com.github.demidko.aot.morphology;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static com.github.demidko.aot.bytecode.BytecodeUtils.*;
import java.util.*;

class MorphologyCompatibilityTest {
  @Test void wireOrdinalsAndTokensStayFrozen() throws Exception {
    try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(
        getClass().getResourceAsStream("/morphology-tags.tsv"), java.nio.charset.StandardCharsets.UTF_8))) {
      String line;
      int count = 0;
      while ((line = reader.readLine()) != null) {
        String[] fields = line.split("\t");
        MorphologyTag tag = MorphologyTag.values()[Integer.parseInt(fields[0])];
        assertEquals(fields[1], tag.name());
        assertEquals(fields[2], tag.toString());
        count++;
      }
      assertEquals(MorphologyTag.values().length, count);
    }
  }
  @Test void allTokensRoundTripAndUnknownsKeepDiagnostics() {
    Set<String> tokens = new HashSet<>();
    for (MorphologyTag tag : MorphologyTag.values()) {
      assertTrue(tokens.add(tag.toString()), "duplicate token");
      assertSame(tag, MorphologyTag.fromString(tag.toString()));
      assertSame(tag, MorphologyTag.fromString(new String(tag.toString())));
    }
    for (String invalid : Arrays.asList(null, "", "noun", " С", "С ", "с", "😀")) {
      IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> MorphologyTag.fromString(invalid));
      assertEquals("Invalid token: " + invalid, error.getMessage());
    }
  }
  @Test void byteEncodingIsRestrictedCp1251WithYoFolding() {
    for (int i = -128; i < 128; i++) {
      byte b = (byte)i;
      char expected = (i >= -64 && i < 0) ? (char)(0x410 + i + 64) : 0;
      if (i == 45 || (i >= 48 && i <= 57)) expected = (char)i;
      if (i == -106) expected = '–';
      if (i == -105) expected = '—';
      if (i == -88) expected = 'Ё';
      if (i == -72) expected = 'ё';
      assertEquals(expected, byteToChar(b));
      if (expected == 0) assertThrows(IllegalArgumentException.class, () -> safeByteToChar(b));
      else assertEquals(expected, safeByteToChar(b));
      assertEquals(i == 100, isEndOfLine(b));
      assertEquals(i != 100, isContent(b));
    }
    for (int i = 0; i <= Character.MAX_VALUE; i++) {
      char c = (char)i;
      byte expected = (i >= 0x410 && i <= 0x44f) ? (byte)(i - 0x410 + 0xc0) : 0;
      if (c == '-' || (c >= '0' && c <= '9')) expected = (byte)c;
      if (c == '–') expected = (byte)0x96;
      if (c == '—') expected = (byte)0x97;
      if (c == 'Ё') expected = (byte)0xc5;
      if (c == 'ё') expected = (byte)0xe5;
      assertEquals(expected, charToByte(c));
    }
    assertEquals(safeCharToByte('е'), safeCharToByte('ё'));
    assertThrows(IllegalArgumentException.class, () -> safeCharToByte('x'));
  }
  @Test void partOfSpeechUsesFirstMatchingTag() {
    assertNull(PartOfSpeech.partOfSpeech(Collections.emptyList()));
    assertSame(PartOfSpeech.Verb, PartOfSpeech.partOfSpeech(Arrays.asList(MorphologyTag.Genitive, MorphologyTag.Verb, MorphologyTag.Noun)));
    assertSame(PartOfSpeech.Noun, PartOfSpeech.partOfSpeech(Arrays.asList(MorphologyTag.Noun, MorphologyTag.Verb)));
    assertThrows(NullPointerException.class, () -> PartOfSpeech.partOfSpeech((MorphologyTag)null));
  }
}
