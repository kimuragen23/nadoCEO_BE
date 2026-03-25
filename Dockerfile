# Stage 1: Build
FROM eclipse-temurin:23-jdk AS builder
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY gradlew .
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Run
FROM eclipse-temurin:23-jre
WORKDIR /app

RUN groupadd -r nadoceo && useradd -r -g nadoceo nadoceo

COPY --from=builder /app/build/libs/*.jar app.jar

RUN chown -R nadoceo:nadoceo /app
USER nadoceo

EXPOSE 8080

ENTRYPOINT ["java", "--enable-preview", "-jar", "app.jar"]
