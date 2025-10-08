FROM amazoncorretto:8-alpine-jdk
WORKDIR /app
COPY target/TestApp-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

