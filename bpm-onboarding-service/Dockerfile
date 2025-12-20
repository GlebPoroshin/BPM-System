FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY bpm-grpc-contracts /app/bpm-grpc-contracts
RUN --mount=type=cache,target=/home/gradle/.gradle \
    --mount=type=cache,target=/home/gradle/.m2 \
    gradle -p /app/bpm-grpc-contracts publishToMavenLocal --no-daemon
COPY bpm-onboarding-service /app/bpm-onboarding-service
WORKDIR /app/bpm-onboarding-service
RUN --mount=type=cache,target=/home/gradle/.gradle \
    --mount=type=cache,target=/home/gradle/.m2 \
    gradle bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/bpm-onboarding-service/build/libs/*.jar app.jar
EXPOSE 8084 9091
ENV JAVA_OPTS="-Xms128m -Xmx256m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
