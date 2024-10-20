FROM openjdk:17-jdk-alpine AS builder

WORKDIR /app

COPY mvnw ./
COPY .mvn .mvn
COPY pom.xml ./

COPY src ./src

RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jdk-alpine

RUN apk add --no-cache docker-cli

WORKDIR /app

COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

COPY --from=builder /app/src ./src

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "app.jar"]
