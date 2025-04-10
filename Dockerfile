# Use image JDK 21
FROM eclipse-temurin:21-jdk-alpine

# Create work dirictory
WORKDIR /app

# Copy JAR-file in container
COPY target/LookingDev-StackOverflow-0.0.1-SNAPSHOT.jar /app.jar

# Port
EXPOSE 8082

# Run app
ENTRYPOINT ["java", "-jar", "/app.jar"]
