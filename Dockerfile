# Multi-stage Dockerfile for building and running the Spring Boot application
# Build stage using Temurin JDK and Maven Wrapper
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src ./src
RUN chmod +x ./mvnw && ./mvnw -B -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

