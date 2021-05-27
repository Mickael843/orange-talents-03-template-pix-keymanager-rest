FROM openjdk:11.0.11-jre
COPY build/libs/keymanager-rest-0.1-all.jar /app/keymanager-rest.jar
WORKDIR /app
EXPOSE 50051
CMD ["java", "-jar", "keymanager-rest.jar"]