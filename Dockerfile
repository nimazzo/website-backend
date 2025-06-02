# ---------- Build Stage ----------
FROM maven:3.9-eclipse-temurin-24 AS builder

# Set working directory
WORKDIR /build

# Copy Maven project files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# ---------- Runtime Stage ----------
FROM eclipse-temurin:24-jdk

# Set working directory
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /build/target/website-backend-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Label for GitHub project association
LABEL org.opencontainers.image.source="https://github.com/nimazzo/website-backend"

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]