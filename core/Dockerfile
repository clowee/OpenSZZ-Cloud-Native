FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG JAR_FILE

RUN apk update && apk add \
git && apk add bash 


COPY target/SZZRest-0.0.1-SNAPSHOT.jar  app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
