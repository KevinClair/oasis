# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY oasis-admin/pom.xml oasis-admin/pom.xml
COPY oasis-scheduler-spring-boot-starter/pom.xml oasis-scheduler-spring-boot-starter/pom.xml
# Download dependencies first (layer caching)
RUN mvn -f oasis-admin/pom.xml -DskipTests dependency:go-offline 2>/dev/null || true
COPY oasis-admin/src oasis-admin/src
COPY oasis-scheduler-spring-boot-starter/src oasis-scheduler-spring-boot-starter/src
RUN mvn -f oasis-admin/pom.xml -DskipTests package -pl oasis-admin -am

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/oasis-admin/target/*.jar app.jar
EXPOSE 8080
ENV OASIS_DB_URL="jdbc:mysql://mysql:3306/oasis?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
ENV OASIS_DB_USERNAME="root"
ENV OASIS_DB_PASSWORD="123456"
ENV OASIS_JWT_SECRET="change-me-to-a-strong-base64-key"
ENTRYPOINT ["java", "-jar", "app.jar"]
