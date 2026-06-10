FROM node:18-alpine AS frontend-build
WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm install

COPY vite.config.js ./
COPY index.html ./
COPY src ./src

RUN npm run build

FROM gradle:8.10-jdk17 AS build
WORKDIR /app

COPY build.gradle settings.gradle build.properties ./
COPY gradle ./gradle
COPY gradlew ./

COPY src ./src

COPY --from=frontend-build /app/src/main/resources/static ./src/main/resources/static

RUN chmod +x gradlew && ./gradlew clean build -x test -x functionalTest -x buildFrontend --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]