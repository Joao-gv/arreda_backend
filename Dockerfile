FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -Dmaven.test.skip=true package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S arreda && adduser -S arreda -G arreda \
    && mkdir -p /app/uploads && chown -R arreda:arreda /app
COPY --from=build /workspace/target/backend-0.0.1-SNAPSHOT.jar /app/app.jar
USER arreda
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
