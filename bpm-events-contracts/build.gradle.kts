plugins {
    kotlin("jvm") version "2.0.21"
    `java-library`
    `maven-publish`
}

group = "com.rut.glebporoshin.sop"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin стандартная библиотека
    implementation(kotlin("stdlib"))
    
    // Для сериализации (опционально, но рекомендуется)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
}

kotlin {
    jvmToolchain(17)
}

// Настраиваем задачу для создания JAR
tasks {
    jar {
        enabled = true
        archiveBaseName.set("bpm-events-contracts")
    }
}

// Публикация в Maven Local
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "bpm-events-contracts"
            version = project.version.toString()
            
            from(components["java"])
        }
    }
}
