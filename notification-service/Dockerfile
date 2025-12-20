FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY bpm-events-contracts /app/bpm-events-contracts
RUN --mount=type=cache,target=/home/gradle/.gradle \
    --mount=type=cache,target=/home/gradle/.m2 \
    gradle -p /app/bpm-events-contracts publishToMavenLocal --no-daemon
COPY notification-service /app/notification-service
WORKDIR /app/notification-service
RUN --mount=type=cache,target=/home/gradle/.gradle \
    --mount=type=cache,target=/home/gradle/.m2 \
    gradle bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/notification-service/build/libs/*.jar app.jar
EXPOSE 8086
ENV JAVA_OPTS="-Xms128m -Xmx256m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
