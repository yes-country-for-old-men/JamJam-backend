FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY settings.gradle build.gradle ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies || true

COPY src src
RUN ./gradlew --no-daemon clean bootJar

FROM eclipse-temurin:21-jre-alpine AS runtime

COPY --from=build /app/build/libs/*.jar /app.jar

EXPOSE 8080
ENTRYPOINT ["sh","-c","java -jar /app.jar"]