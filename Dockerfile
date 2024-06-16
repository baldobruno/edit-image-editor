FROM ubuntu:latest AS build

RUN apt-get update
RUN apt-get install openjdk-17-jdk -y
COPY . .

RUN ./mvnw clean install

FROM openjdk:17-jdk-slim

EXPOSE 8080

COPY --from=build /build/libs/image-editor-1.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]