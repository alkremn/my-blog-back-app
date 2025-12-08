plugins {
    java
    war
}

group = "com.kremnev"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring MVC (brings spring-context, spring-beans, etc. transitively)
    implementation("org.springframework:spring-webmvc:6.2.14")

    // Jackson (Spring MVC will use these for @ResponseBody / JSON)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.0")

    // Spring JDBC for JdbcTemplate
    implementation("org.springframework:spring-jdbc:6.2.14")

    // PostgreSQL JDBC driver (used at runtime to talk to Docker Postgres)
    runtimeOnly("org.postgresql:postgresql:42.7.3")

    // HikariCP connection pooling
    implementation("com.zaxxer:HikariCP:5.1.0")

    implementation("org.springframework.data:spring-data-commons:3.3.2")

    // Provided by Tomcat / servlet container
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")
    testImplementation("jakarta.servlet:jakarta.servlet-api:6.0.0")

    // JUnit Jupiter from your version catalog
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Mockito for mocking dependencies in unit tests
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")

    // Spring Test for MockMultipartFile and testing utilities
    testImplementation("org.springframework:spring-test:6.2.14")

    // H2 in-memory database for integration tests
    testImplementation("com.h2database:h2:2.3.232")

    // Hamcrest matchers for assertions
    testImplementation("org.hamcrest:hamcrest:2.2")

    // JSON Path for jsonPath() matchers in MockMvc tests
    testImplementation("com.jayway.jsonpath:json-path:2.9.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    // We only care about WAR (for Tomcat), disabling plain JAR.
    enabled = false
}