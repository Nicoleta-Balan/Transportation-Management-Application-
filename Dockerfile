FROM openjdk:22-jre-slim

WORKDIR /app

# Copy the pre-built JAR created by the previous CI step (mvn -B clean package)
# Note: The artifact ID is TransportationMaven and the version is 0.0.1-SNAPSHOT from your pom.xml
COPY target/TransportationMaven-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
