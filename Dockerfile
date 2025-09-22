FROM eclipse-temurin:17-jdk-alpine as builder
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew build -x test
RUN ./gradlew shadowJar
RUN ls -la /app/build/libs/  # Debug step: List files in build/libs

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/pocketmusala-api-*-all.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
