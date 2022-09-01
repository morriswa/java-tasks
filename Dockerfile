FROM --platform=x86-64 amazoncorretto:18-alpine-jdk

WORKDIR /app
COPY target .
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app.jar

ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","/app.jar"]

EXPOSE 8080