FROM ubuntu:latest AS build

RUN apt-get update
RUN apt-get install openjdk-17-jdk -y
RUN apt-get install libfreetype6 -y
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package

FROM amazoncorretto:17

EXPOSE 8080

COPY --from=build /target/image-editor-api-1.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]