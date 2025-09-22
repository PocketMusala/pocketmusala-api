FROM eclipse-temurin:17-jre-alpine as builder
WORKDIR /app
COPY . .


RUN apk add --no-cache bash

# Fix permissions FIRST
RUN chmod +x gradlew

# Debug: Check project structure
RUN ls -la
RUN cat build.gradle.kts
RUN cat settings.gradle.kts

# Try different build approaches
RUN ./gradlew tasks --all || true

# Try building with different tasks
RUN ./gradlew assemble --stacktrace --console=verbose || \
    ./gradlew jar --stacktrace --console=verbose || \
    ./gradlew build --stacktrace --console=verbose

# Check what was created
RUN find . -name "build" -type d | head -5 | xargs ls -la || true
RUN find . -name "*.jar" -type f

# If no jar, try to compile manually as last resort
RUN ./gradlew classes --console=verbose && \
    find . -name "*.class" -type f | head -5