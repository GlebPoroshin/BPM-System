plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.rut.glebporoshin.sop"
version = "0.0.1-SNAPSHOT"
description = "bpm-main-service"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // DGS Platform для GraphQL
    implementation(platform("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:8.5.6"))
    
    // API контракты
    implementation("com.rut.glebporoshin.sop:bpm-api:0.0.1-SNAPSHOT")
    
    // Events контракты для RabbitMQ
    implementation("com.rut.glebporoshin.sop:bpm-events-contracts:1.0.0-SNAPSHOT")

    // gRPC контракты
    implementation("com.rut.glebporoshin.sop:bpm-grpc-contracts:1.0.0-SNAPSHOT")

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    // RabbitMQ
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    
    // gRPC Client
    implementation("net.devh:grpc-client-spring-boot-starter:3.1.0.RELEASE")
    implementation("io.grpc:grpc-netty-shaded:1.66.0")
    
    // DGS для GraphQL
    implementation("com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter")
    implementation("com.netflix.graphql.dgs:graphql-dgs-subscriptions-websockets-autoconfigure")
    
    // Явное указание версии для разрешения конфликта
    implementation("com.graphql-java:java-dataloader:3.2.2")
    
    // Swagger UI (для отображения документации)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")

    // Observability: metrics, tracing, logging
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.zipkin.reporter2:zipkin-reporter-brave")
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
    
    // Для тестов
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

configurations.all {
    resolutionStrategy {
        force("com.graphql-java:java-dataloader:3.2.2")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
