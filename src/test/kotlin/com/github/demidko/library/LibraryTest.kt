package com.github.demidko.library

import assertk.assertThat
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class LibraryTest {

  @Test
  fun testSomeLibraryMethod() {
    val classUnderTest = Library()
    assertThat(classUnderTest.someLibraryMethod()).isTrue()
  }
}
