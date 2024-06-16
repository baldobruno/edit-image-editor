FROM ubuntu:latest AS build

RUN apt-get update
RUN apt-get install openjdk-17-jdk -y
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package

FROM openjdk:17-jdk-slim

EXPOSE 8080

COPY --from=build /target/image-editor-api-1.jar app.jar

ENTRYPOINT ["java", "-cp", "app.jar", "Main"]