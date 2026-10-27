
# Build stage
FROM maven:3.9.16-eclipse-temurin-21 AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw

RUN ./mvnw -B dependency:go-offline

COPY src/ src/

RUN ./mvnw -B clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --system --uid 10001 appuser

COPY --from=build /app/target/stockpulse-0.0.1-SNAPSHOT.jar app.jar

USER 10001

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
