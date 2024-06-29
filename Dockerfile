FROM maven:3.8.5-openjdk-17 as builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src/ ./src/
RUN mvn clean install

FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY ngo-nabarun-app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]