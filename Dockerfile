FROM openjdk:22-jdk

WORKDIR /app


COPY target/TransportationMaven-0.0.1-DEV.jar app.jar

EXPOSE 8085


ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]

