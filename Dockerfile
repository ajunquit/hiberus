FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml ./
COPY openapi openapi

RUN mvn -q -DskipTests dependency:go-offline

COPY src src

RUN mvn -q -DskipTests package \
    && cp "$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*.original' | head -n 1)" app.jar

FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

COPY --from=build /workspace/app.jar app.jar

EXPOSE 8075

ENV JAVA_OPTS=""

HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
    CMD wget -qO- http://127.0.0.1:8075/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
