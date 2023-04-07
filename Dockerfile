FROM --platform=x86-64 amazoncorretto:17-alpine-jdk
ENV PROP prod
WORKDIR /app
COPY target/ ./target/
RUN mv /app/target/*.jar /app/target/app.jar
RUN mkdir /files

ENTRYPOINT ["java","-jar","-Dspring.profiles.active=${PROP}","/app/target/app.jar"]

EXPOSE 8080