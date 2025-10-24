FROM gradle:8-jdk17 AS build

WORKDIR /home/gradle/src

COPY --chown=gradle:gradle . .

RUN gradle bootJar --no-daemon -Dorg.gradle.java.home=/opt/java/openjdk

FROM openjdk:17.0.1-jdk-slim

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

# Create files directory
RUN mkdir -p /app/files

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "app.jar"]
