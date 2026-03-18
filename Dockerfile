# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests

# Stage 2: Create the final, lightweight image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/noyblog-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8085
ENTRYPOINT ["java","-jar","/app/app.jar"]
