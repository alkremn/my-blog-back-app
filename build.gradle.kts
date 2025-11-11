plugins {
    id("war")
}

repositories {
    mavenCentral()
}

group = "com.kremnev"
version = "1.0.0"

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.springframework:spring-webmvc:6.2.10")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.0")

    implementation("com.h2database:h2:2.2.224")
    implementation("org.springframework.data:spring-data-jdbc:3.4.1")

    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

tasks.jar {
    enabled = false
}