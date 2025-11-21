plugins {
    kotlin("jvm") version "1.9.25"
    id("com.google.protobuf") version "0.9.4"
    `java-library`
    `maven-publish`
}

group = "com.rut.glebporoshin.sop"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api("io.grpc:grpc-stub:1.66.0")
    api("io.grpc:grpc-protobuf:1.66.0")
    api("com.google.protobuf:protobuf-java:3.25.3")
    api("jakarta.annotation:jakarta.annotation-api:2.1.1")
    
    implementation("io.grpc:grpc-kotlin-stub:1.4.1")
    implementation(kotlin("stdlib"))
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.3"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.66.0"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("grpc")
            }
        }
    }
}

sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/grpc")
            srcDirs("build/generated/source/proto/main/java")
        }
    }
}

tasks.jar {
    enabled = true
    archiveClassifier.set("")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "bpm-grpc-contracts"
            version = project.version.toString()
            from(components["java"])
        }
    }
}

