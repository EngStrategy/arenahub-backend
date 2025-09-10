FROM maven:3.9.9-amazoncorretto-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:resolve
COPY src ./src
RUN mvn clean package -DskipTests

FROM amazoncorretto:21
WORKDIR /app
COPY --from=builder ./app/target/*.jar ./application.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]