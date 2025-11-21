FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

COPY bpm-events-contracts /app/bpm-events-contracts

WORKDIR /app/bpm-events-contracts
RUN gradle publishToMavenLocal --no-daemon

WORKDIR /app/bpm-statistics-service
COPY bpm-statistics-service/build.gradle.kts bpm-statistics-service/settings.gradle.kts bpm-statistics-service/gradlew ./
COPY bpm-statistics-service/gradle gradle/

RUN gradle dependencies --no-daemon || true

COPY bpm-statistics-service/src src/

RUN gradle clean bootJar --no-daemon

FROM eclipse-temurin:17-jre

WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

COPY --from=builder /app/bpm-statistics-service/build/libs/*.jar app.jar

EXPOSE 8083

ENV JAVA_OPTS="-Xms128m -Xmx256m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

