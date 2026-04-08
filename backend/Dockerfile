FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
ARG SERVICE
RUN chmod +x gradlew && ./gradlew :services:${SERVICE}:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jdk
WORKDIR /app
ARG SERVICE
COPY --from=build /app/services/${SERVICE}/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
