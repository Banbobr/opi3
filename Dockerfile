FROM node:22-bookworm-slim AS node

FROM gradle:8.10-jdk17 AS toolchain
USER root
COPY --from=node /usr/local/ /usr/local/
WORKDIR /workspace
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY build.gradle settings.gradle build.properties gradlew ./
COPY gradle ./gradle

FROM toolchain AS build
COPY vite.config.js index.html ./
COPY src ./src
RUN chmod +x gradlew && ./gradlew bootJar -x npmCi --no-daemon

FROM toolchain AS test
ENV PLAYWRIGHT_BROWSERS_PATH=/opt/playwright
RUN ./gradlew installPlaywrightBrowsers -PplaywrightWithDeps=true --no-daemon
COPY --from=build /workspace/ /workspace/
CMD ["./gradlew", "test", "functionalTest", "-x", "npmCi", "--no-daemon", "--rerun-tasks"]

FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app
COPY --from=build /workspace/build/libs/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
