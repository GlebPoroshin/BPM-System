FROM gradle:8.5-jdk17 AS builder

WORKDIR /app
ENV GRADLE_USER_HOME=/home/gradle/.gradle-bpm-onboarding

COPY bpm-grpc-contracts /app/bpm-grpc-contracts

WORKDIR /app/bpm-grpc-contracts
RUN --mount=type=cache,target=${GRADLE_USER_HOME} gradle publishToMavenLocal --no-daemon

WORKDIR /app/bpm-onboarding-service
COPY bpm-onboarding-service/build.gradle.kts bpm-onboarding-service/settings.gradle.kts bpm-onboarding-service/gradlew ./
COPY bpm-onboarding-service/gradle gradle/

RUN --mount=type=cache,target=${GRADLE_USER_HOME} gradle dependencies --no-daemon || true

COPY bpm-onboarding-service/src src/

RUN --mount=type=cache,target=${GRADLE_USER_HOME} gradle bootJar --no-daemon

FROM eclipse-temurin:17-jre

WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

COPY --from=builder /app/bpm-onboarding-service/build/libs/*.jar app.jar

EXPOSE 8084 8084

ENV JAVA_OPTS="-Xms128m -Xmx256m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
