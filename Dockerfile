FROM amazoncorretto:8-alpine-jdk

RUN apk add --no-cache tzdata musl-locales musl-locales-lang \
    && echo "export LANG=ru_RU.UTF-8" >> /etc/profile

ENV LANG=ru_RU.UTF-8
ENV LC_ALL=ru_RU.UTF-8

WORKDIR /app
COPY target/TestApp-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
