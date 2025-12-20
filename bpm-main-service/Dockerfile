FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY bpm-api /app/bpm-api
COPY bpm-events-contracts /app/bpm-events-contracts
COPY bpm-grpc-contracts /app/bpm-grpc-contracts
RUN --mount=type=cache,target=/home/gradle/.gradle \
    --mount=type=cache,target=/home/gradle/.m2 \
    gradle -p /app/bpm-api publishToMavenLocal --no-daemon && \
    gradle -p /app/bpm-events-contracts publishToMavenLocal --no-daemon && \
    gradle -p /app/bpm-grpc-contracts publishToMavenLocal --no-daemon
COPY bpm-main-service /app/bpm-main-service
WORKDIR /app/bpm-main-service
RUN --mount=type=cache,target=/home/gradle/.gradle \
    --mount=type=cache,target=/home/gradle/.m2 \
    gradle bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/bpm-main-service/build/libs/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
