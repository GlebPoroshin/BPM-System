FROM gradle:8.5-jdk17 AS builder

WORKDIR /app
ENV GRADLE_USER_HOME=/home/gradle/.gradle-bpm-compliance

COPY bpm-events-contracts /app/bpm-events-contracts

WORKDIR /app/bpm-events-contracts
RUN --mount=type=cache,target=${GRADLE_USER_HOME} gradle publishToMavenLocal --no-daemon

WORKDIR /app/bpm-compliance-service
COPY bpm-compliance-service/build.gradle.kts bpm-compliance-service/settings.gradle.kts bpm-compliance-service/gradlew ./
COPY bpm-compliance-service/gradle gradle/

RUN --mount=type=cache,target=${GRADLE_USER_HOME} gradle dependencies --no-daemon || true

COPY bpm-compliance-service/src src/

RUN --mount=type=cache,target=${GRADLE_USER_HOME} gradle bootJar --no-daemon

FROM eclipse-temurin:17-jre

WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

COPY --from=builder /app/bpm-compliance-service/build/libs/*.jar app.jar

EXPOSE 8085

ENV JAVA_OPTS="-Xms128m -Xmx256m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
