# Use a base image with Java and Maven installed
FROM maven:3.8.4-openjdk-17 AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml and download the dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code and build the application
COPY src ./src
RUN mvn package -DskipTests

# Use a lightweight base image for the runtime
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Copy the SQL script to the container
COPY schema.sql /docker-entrypoint-initdb.d/

# Expose the port on which your application runs (default is 8080)
EXPOSE 8080

# Set the entrypoint command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]