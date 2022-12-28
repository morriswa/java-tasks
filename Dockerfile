FROM --platform=x86-64 amazoncorretto:17-alpine-jdk

WORKDIR /app

### BUILD IN CONTAINER
## maven resources
#COPY .mvn/ ./.mvn/
#COPY mvnw .
#
## project
#COPY src ./.src/
#COPY pom.xml .
#
## build the project
#RUN ./mvnw clean package

### PRE BUILD
COPY target/ ./target/

# define runnable jar
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app.jar

ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","/app.jar"]

EXPOSE 8080