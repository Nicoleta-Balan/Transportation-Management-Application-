FROM openjdk:25-jdk-slim as builder

WORKDIR /app

COPY mvnw .
COPY .mvn .

COPY pom.xml .
COPY src ./src

RUN ./mvnw clean package -DskipTests

FROM openjdk:25-jre-slim

WORKDIR /app

COPY --from=builder /app/target/TransportationMaven-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
