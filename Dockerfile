# Stage 1: Build the application with Maven
FROM maven:3.8.4-openjdk-11-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the final image with only the JAR file
FROM adoptopenjdk:11-jre-hotspot-bionic

WORKDIR /app

COPY --from=build /app/target/HomeFinder-2.0.0-SNAPSHOT.jar ./app.jar

ARG MONGO_CONN_STR
ARG MONGO_DATABASE
ARG TG_TOKEN
ARG KAKFA_BOOTSTRAP_SERVERS

CMD ["java", "-jar", "./app.jar"]