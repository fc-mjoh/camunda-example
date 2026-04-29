plugins {
    java
}

// Disable the boot jar for this library module; produce a plain JAR instead
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.46")
    annotationProcessor("org.projectlombok:lombok:1.18.46")

    testCompileOnly("org.projectlombok:lombok:1.18.46")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.46")
    // Camunda engine API — needed for JavaDelegate, ExecutionListener, TaskListener
    implementation("org.camunda.bpm:camunda-engine")

    // Camunda Spring Boot external task client starter
    implementation("org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-external-task-client")

    // Spring context for component scanning and annotations
    implementation("org.springframework:spring-core")
    implementation("org.springframework:spring-web")
    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-autoconfigure")

    // Jakarta dependencies for @Named / CDI-style injection if needed
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")

    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:4.0.5")
}
