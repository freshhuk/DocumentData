FROM eclipse-temurin:21-jdk
EXPOSE 8000
VOLUME /tmp

COPY target/FindMe-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]