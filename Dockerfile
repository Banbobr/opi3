FROM node:18-alpine AS frontend-build

WORKDIR /app

COPY package.json ./
RUN npm install

COPY vite.config.js ./
COPY index.html ./
COPY src/App.vue ./src/
COPY src/main.js ./src/
COPY src/router.js ./src/
COPY src/views ./src/views

RUN npm run build

FROM gradle:8.10-jdk17 AS build

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle

COPY src ./src

COPY --from=frontend-build /app/src/main/resources/static ./src/main/resources/static

RUN gradle clean build --no-daemon -x test

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
