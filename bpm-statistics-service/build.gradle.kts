plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.rut.glebporoshin.sop"
version = "0.0.1-SNAPSHOT"
description = "bpm-statistics-service"

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
    // Events контракты для RabbitMQ
    implementation("com.rut.glebporoshin.sop:bpm-events-contracts:1.0.0-SNAPSHOT")
    
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    
    // RabbitMQ
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    
    // Jackson для сериализации
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    
    // Для тестов
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

