plugins {
    java
}

val camundaVersion: String by project

dependencies {
    // This module depends on camunda-workers so delegates are on the classpath
    implementation(project(":camunda-workers"))

    // Camunda Spring Boot starter — pulls in engine, REST, webapps
    implementation("org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-webapp")
    implementation("org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-rest")

    // Spring Boot starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    implementation("com.reply.camunda:camunda-workers")
    implementation("org.camunda.bpm:camunda-engine-plugin-spin:${camundaVersion}")
    implementation("org.camunda.spin:camunda-spin-dataformat-json-jackson:${camundaVersion}")

    // H2 in-memory database
    runtimeOnly("com.h2database:h2")

    // Test
    testImplementation("org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-test")
}
