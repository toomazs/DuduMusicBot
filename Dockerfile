# Multi-stage build for Dudu Music Bot

# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install ffmpeg for audio processing
RUN apk add --no-cache ffmpeg

# Copy the shaded jar from build stage
COPY --from=build /app/target/music-bot-1.0-shaded.jar /app/music-bot.jar

# Create a non-root user
RUN addgroup -S botuser && adduser -S botuser -G botuser
USER botuser

# Run the bot
ENTRYPOINT ["java", "-jar", "/app/music-bot.jar"]
