FROM openjdk:17-jdk-slim
VOLUME /tmp
EXPOSE 8080
COPY target/identity-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]