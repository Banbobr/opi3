# Как запускать проект Web4

Инструкция по запуску приложения, сборки, тестов и Docker.  
Подробный разбор файлов — в `razbor.md`, отчёт — в `task.md`.

---

## Что нужно установить

| Компонент | Версия | Зачем |
|-----------|--------|-------|
| Java | 17+ | Spring Boot backend |
| Node.js | 18+ | Vue frontend (dev и сборка) |
| Docker + Docker Compose | актуальные | PostgreSQL, app, тесты в контейнерах |
| Git | любой | задача `diff` |

Проверка:
```bash
java -version
node -version
docker --version
docker compose version
```

---

## Адреса приложения

| Страница | URL |
|----------|-----|
| Вход (логин) | http://localhost:8080/ |
| Главная (график) | http://localhost:8080/main |

При dev-режиме Vite (отдельный фронт):
| Страница | URL |
|----------|-----|
| Вход | http://localhost:5173/ |
| Главная | http://localhost:5173/main |

---

## Вариант 1: IntelliJ IDEA (самый удобный для разработки)

### Шаг 1 — PostgreSQL

**Вариант A — только БД в Docker:**
```bash
cd /Users/nikolaj/IdeaProjects/web4
docker compose up -d postgres
```

**Вариант B — PostgreSQL установлен локально**  
Параметры как в `src/main/resources/application.properties`:
- хост: `localhost`, порт: `5432`
- БД: `postgres`
- логин/пароль: `admin` / `admin`

### Шаг 2 — Backend

1. Открой `src/main/java/ru/minibobr/Web4Application.java`
2. Нажми зелёный треугольник → **Run 'Web4Application'**

Или: панель **Gradle** (справа) → **Tasks** → **application** → **bootRun** (если есть) или **bootRun** в `other`.

### Шаг 3 — Открыть в браузере

http://localhost:8080/

> Перед первым запуском с UI собери фронт (если `static` пустой):
> ```bash
> npm install && npm run build
> ```
> Или используй dev-режим (вариант 2 ниже).

### Gradle-задачи в IDEA

Панель **Gradle** → **Tasks**:

| Задача | Действие |
|--------|----------|
| `build` | Полная сборка + unit + functional тесты |
| `test` | Только unit-тесты |
| `functionalTest` | Functional (нужен запущенный сервер) |
| `bootRun` | Запуск приложения |
| `clean` | Очистка `build/` |
| `native2ascii` | Генерация `messages.properties` |
| `diff` | Git-коммит tracked-файлов |

---

## Вариант 2: Gradle + терминал (backend)

### Только PostgreSQL в Docker + приложение локально

**Терминал 1 — БД:**
```bash
cd /Users/nikolaj/IdeaProjects/web4
docker compose up -d postgres
```

Дождись готовности (5–15 сек).

**Терминал 2 — приложение:**
```bash
cd /Users/nikolaj/IdeaProjects/web4
./gradlew bootRun
```

Открыть: http://localhost:8080/

### Собрать JAR и запустить

```bash
npm install && npm run build
./gradlew bootJar
java -jar build/libs/web4-1.0-SNAPSHOT.jar
```

---

## Вариант 3: Разработка фронта отдельно (hot-reload)

Два процесса:

**Терминал 1 — backend:**
```bash
docker compose up -d postgres
./gradlew bootRun
```

**Терминал 2 — frontend:**
```bash
npm install
npm run dev
```

Открыть адрес из вывода Vite (обычно http://localhost:5173).  
Запросы `/api` проксируются на http://localhost:8080 (см. `vite.config.js`).

---

## Вариант 4: Docker (всё в контейнерах)

### Запуск приложения + БД

```bash
cd /Users/nikolaj/IdeaProjects/web4
docker compose up --build
```

- Приложение: http://localhost:8080/
- PostgreSQL снаружи: `localhost:5432`

Остановка:
```bash
docker compose down
```

С удалением данных БД:
```bash
docker compose down -v
```

### Другая версия образа

В `.env` задано `APP_VERSION=1.0-SNAPSHOT`. Другая ревизия:
```bash
APP_VERSION=2.0-dev docker compose up --build
```

---

## Запуск тестов

### Unit-тесты (JUnit) — сервер не нужен

```bash
./gradlew test
```

Отчёт: `build/reports/tests/test/index.html`

### Functional-тесты (Playwright) — сервер нужен

**Терминал 1:**
```bash
docker compose up -d postgres
./gradlew bootRun
```

**Терминал 2:**
```bash
./gradlew functionalTest
```

Или с явным URL:
```bash
./gradlew functionalTest -Dapp.base.url=http://localhost:8080
```

Отчёт: `build/reports/tests/functionalTest/index.html`

> При первом запуске Gradle скачает Chromium (`installPlaywrightBrowsers`) — может занять время.

### Полная сборка с тестами

Сервер должен быть запущен, иначе functional пропустятся (skipped):

```bash
./gradlew bootRun
```

В другом терминале:
```bash
./gradlew build
```

Порядок внутри `build`: сборка JAR → `test` → `functionalTest`.

### Тесты в Docker

Поднимает postgres, app и контейнер с прогоном тестов:

```bash
docker compose --profile test up --build
```

Внутри сервиса `test` выполняется:
```bash
./gradlew test functionalTest -Dapp.base.url=http://app:8080
```

---

## Полезные Gradle-команды

```bash
./gradlew clean              # очистка build/
./gradlew compile            # только компиляция
./gradlew native2ascii       # messages.properties из unicode
./gradlew bootJar            # собрать JAR без полного check
./gradlew test               # unit-тесты
./gradlew functionalTest     # Playwright (нужен сервер)
./gradlew build              # JAR + все тесты
./gradlew diff               # git commit tracked-файлов
./gradlew tasks              # список всех задач
```

Пропустить сборку фронта при backend-сборке:
```bash
./gradlew build -x buildFrontend
```

---

## Типичные проблемы

### Приложение не стартует — ошибка подключения к БД

PostgreSQL не запущен или неверные credentials.

```bash
docker compose up -d postgres
docker compose ps
```

Проверь `application.properties`: `admin`/`admin`, БД `postgres`, порт `5432`.

### Пустая страница / нет UI

Не собран Vue в `static/`:
```bash
npm install && npm run build
./gradlew bootRun
```

### `docker pull` падает с TLS timeout

Проблема сети до Docker Hub. Повтори позже, смени сеть/VPN, или:
```bash
docker pull postgres:15-alpine
```

Локально без полного Docker:
```bash
docker compose up -d postgres
./gradlew bootRun
```

### Functional-тесты skipped

Сервер не запущен на `:8080`. Сначала `bootRun`, потом `functionalTest`.

### Кракозябры в `messages_unicode.properties` в IDEA

**File → File Encoding** или клик по кодировке внизу справа → **UTF-8** → **Reload**.

### Порт 8080 занят

Останови другой процесс или смени в `application.properties`:
```properties
server.port=8081
```

---

## Краткая шпаргалка «что нажать»

| Цель | Действие |
|------|----------|
| Быстро посмотреть приложение | `docker compose up -d postgres` → Run `Web4Application` → http://localhost:8080 |
| Разработка UI | `bootRun` + `npm run dev` → http://localhost:5173 |
| Сдать / прод-сборка | `npm run build` → `./gradlew build` |
| Только unit-тесты | `./gradlew test` |
| Unit + functional | `bootRun` + `./gradlew build` |
| Всё в Docker | `docker compose up --build` |
| Тесты в Docker | `docker compose --profile test up --build` |

---

*Проект: Web4, лабораторная №3 на базе №4.*
