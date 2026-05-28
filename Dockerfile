# syntax=docker/dockerfile:1.7

# ---- Build stage ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# Copy the Maven wrapper and POM first so dependency layers cache well
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw \
 && ./mvnw -B -ntp -q dependency:go-offline || true

# Now copy sources and build the jar (skip tests — tests need Docker-in-Docker)
COPY src src
RUN ./mvnw -B -ntp -Dmaven.test.skip=true package \
 && cp target/*.jar app.jar

# ---- Runtime stage ----
FROM eclipse-temurin:25-jre
WORKDIR /app

# Non-root runtime user
RUN groupadd --system app && useradd --system --gid app --home /app app \
 && mkdir -p /app/uploads \
 && chown -R app:app /app
USER app

COPY --from=build --chown=app:app /workspace/app.jar /app/app.jar

EXPOSE 8080
ENV JAVA_OPTS="" \
    SPRING_PROFILES_ACTIVE=""

ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
