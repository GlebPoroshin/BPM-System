FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

COPY bpm-api /app/bpm-api
COPY bpm-events-contracts /app/bpm-events-contracts
COPY bpm-grpc-contracts /app/bpm-grpc-contracts

WORKDIR /app/bpm-api
RUN gradle publishToMavenLocal --no-daemon

WORKDIR /app/bpm-events-contracts
RUN gradle publishToMavenLocal --no-daemon

WORKDIR /app/bpm-grpc-contracts
RUN gradle publishToMavenLocal --no-daemon

WORKDIR /app/bpm-main-service
COPY bpm-main-service/build.gradle.kts bpm-main-service/settings.gradle.kts bpm-main-service/gradlew ./
COPY bpm-main-service/gradle gradle/

RUN gradle dependencies --no-daemon || true

COPY bpm-main-service/src src/

RUN gradle clean bootJar --no-daemon

FROM eclipse-temurin:17-jre

WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

COPY --from=builder /app/bpm-main-service/build/libs/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

