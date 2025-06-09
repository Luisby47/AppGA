FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy the pom.xml and cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src/ /app/src/

# Build the application
RUN mvn clean package

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the database file
COPY lab2.db /app/lab2.db

# Copy the built JAR file
COPY --from=build /app/target/Lab2-1.0-SNAPSHOT.jar /app/app.jar

# Expose the application port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "/app/app.jar"]
