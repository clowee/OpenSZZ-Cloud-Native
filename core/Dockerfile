FROM maven:3.6.3-jdk-11-slim AS build
WORKDIR /src/app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src/ ./src
RUN mvn install -X

FROM adoptopenjdk/openjdk11:jre-11.0.9.1_1-alpine
VOLUME /tmp
ARG JAR_FILE

RUN apk update && apk add \
git && apk add bash 


COPY --from=build /src/app/target/SZZRest-0.0.1-SNAPSHOT.jar  app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
