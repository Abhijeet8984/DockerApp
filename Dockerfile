FROM eclipse-temurin:25-jre

WORKDIR /opt/app
COPY target/DockerApp-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# docker run -d -v name-vol:/app -p 8080:8080 myapp