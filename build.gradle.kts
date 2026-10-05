import java.util.jar.JarFile

repositories {
  mavenCentral()
}
plugins {
  `java-library`
  `maven-publish`
}
val baselineArchive = providers.gradleProperty("baselineJar").orNull?.let { path ->
  file(path).also { archive ->
    require(archive.isFile) { "baselineJar is not a file: $archive" }
    JarFile(archive).use { jar ->
      require(jar.getJarEntry("com/github/demidko/aot/morphology/MorphologyTag.class") != null) {
        "baselineJar does not contain MorphologyTag: $archive"
      }
    }
  }
}
val baselineClasspath = if (baselineArchive == null) files() else files(baselineArchive)

dependencies {
  testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
  testImplementation("org.hamcrest:hamcrest:2.2")
}
tasks.test {
  useJUnitPlatform()
  classpath = baselineClasspath + classpath
}
publishing {
  publications {
    create<MavenPublication>("aot-bytecode") {
      from(components["java"])
    }
  }
}

val jmh by sourceSets.creating
dependencies {
  add(jmh.implementationConfigurationName, sourceSets.main.get().output)
  add(jmh.implementationConfigurationName, "org.openjdk.jmh:jmh-core:1.37")
  add(jmh.annotationProcessorConfigurationName, "org.openjdk.jmh:jmh-generator-annprocess:1.37")
}
tasks.register<JavaExec>("benchmark") {
  dependsOn(tasks.named(jmh.classesTaskName))
  classpath = baselineClasspath + jmh.runtimeClasspath
  mainClass = "org.openjdk.jmh.Main"
  args(providers.gradleProperty("benchmarkArgs").orElse("").get().split(" ").filter { it.isNotEmpty() })
}
