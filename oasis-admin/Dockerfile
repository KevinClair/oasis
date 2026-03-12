# ---- Build Stage ----
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline --no-transfer-progress

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests --no-transfer-progress

# ---- Runtime Stage ----
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Add a non-root user for security
RUN addgroup -S oasis && adduser -S oasis -G oasis

COPY --from=build /app/target/*.jar app.jar

RUN chown oasis:oasis app.jar

USER oasis

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
