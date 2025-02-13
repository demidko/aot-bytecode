repositories {
  mavenCentral()
}
plugins {
  `java-library`
  `maven-publish`
}
dependencies {
  testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
  testImplementation("org.hamcrest:hamcrest:2.2")
}
tasks.test {
  useJUnitPlatform()
}
publishing {
  publications {
    create<MavenPublication>("aot-bytecode") {
      from(components["java"])
    }
  }
}
