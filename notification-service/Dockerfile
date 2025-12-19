FROM gradle:8.5-jdk17 AS builder

WORKDIR /app
ENV GRADLE_USER_HOME=/home/gradle/.gradle-notification

COPY bpm-events-contracts /app/bpm-events-contracts
WORKDIR /app/bpm-events-contracts
RUN --mount=type=cache,target=${GRADLE_USER_HOME} gradle publishToMavenLocal --no-daemon

WORKDIR /app/notification-service
COPY notification-service/build.gradle.kts notification-service/settings.gradle.kts ./

RUN --mount=type=cache,target=${GRADLE_USER_HOME} gradle dependencies --no-daemon || true

COPY notification-service/src src/

RUN --mount=type=cache,target=${GRADLE_USER_HOME} gradle bootJar --no-daemon

FROM eclipse-temurin:17-jre

WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

COPY --from=builder /app/notification-service/build/libs/*.jar app.jar

EXPOSE 8086

ENV JAVA_OPTS="-Xms128m -Xmx256m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
