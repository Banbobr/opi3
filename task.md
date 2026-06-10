# Лабораторная работа №3 (на базе Web4 / ЛР №4)

Полный отчёт о структуре проекта, автоматизации Gradle, контейнеризации Docker и тестировании.

---

## 1. Назначение проекта

Веб-приложение для проверки попадания точек в область на координатной плоскости (лабораторная №4, вариант 478265). Поверх него реализованы требования лабораторной №3:

- замена Ant на **Gradle**;
- использование **Git** вместо SVN;
- **Docker** для сборки, тестов и развёртывания;
- **JUnit 5** (модульные тесты);
- **Playwright** (15 функциональных тест-кейсов);
- вынос строк в файлы локализации + задача **native2ascii**.

**Стек:** Spring Boot 3.2, Vue 3, Vite, PostgreSQL 15, JPA + JOOQ, Gradle 8, Docker.

---

## 2. Соответствие требованиям (чеклист 100%)

| Требование | Статус | Реализация |
|------------|--------|------------|
| Gradle вместо Ant | ✅ | `build.gradle`, `gradlew` |
| Git вместо SVN | ✅ | задача `diff` |
| `build.properties` (version, mainClass, trackedClasses) | ✅ | `build.properties` |
| Переменные вынесены из скрипта | ✅ | чтение через `Properties.load()` |
| `MANIFEST.MF`: Version + Main-Class | ✅ | `jar` и `bootJar` |
| `clean` | ✅ | стандартная задача Gradle |
| `compile` | ✅ | алиас, зависит от `compileJava` |
| `build` → compile + JAR | ✅ | `bootJar` зависит от `compile` |
| `test` → после сборки | ✅ | `test.dependsOn bootJar` |
| `native2ascii` | ✅ | задача + `messages_unicode.properties` |
| Строки вынесены из кода в локализацию | ✅ | Vue + API `/api/messages` |
| `diff` + auto git commit | ✅ | задача `diff` |
| Docker multi-stage (Gradle → JRE) | ✅ | `Dockerfile` |
| Сборка в контейнере | ✅ | stage `build` |
| Тесты в контейнере | ✅ | сервис `test`, профиль `--profile test` |
| Развёртывание для functional-тестов | ✅ | сервис `app` + `BASE_URL` |
| Сборка с Docker и без | ✅ | `./gradlew build` / `docker compose up` |
| Несколько ревизий в одном окружении | ✅ | `APP_VERSION` в `.env` и тегах образов |
| JUnit 5 модульные тесты | ✅ | `PointHitCheckerTest` (8 тестов) |
| 15 функциональных тест-кейсов | ✅ | `Web4FunctionalTest` case01–case15 |
| Functional tests — фаза сборки | ✅ | `check.dependsOn functionalTest` |
| Playwright | ✅ | `com.microsoft.playwright:playwright` |

---

## 3. Файл параметров `build.properties`

```
version=1.0-SNAPSHOT
mainClass=ru.minibobr.Web4Application
trackedClasses=...PointHitChecker.java,...,messages_unicode.properties,build.gradle,build.properties
```

| Параметр | Назначение |
|----------|------------|
| `version` | Версия проекта → Gradle `version`, `MANIFEST.MF`, Docker-образы |
| `mainClass` | Точка входа Spring Boot |
| `trackedClasses` | Файлы для автокоммита задачей `diff` |

---

## 4. Gradle-задачи (`build.gradle`)

### 4.1. `clean`
Удаляет каталог `build/`.

```bash
./gradlew clean
```

### 4.2. `compile`
Компиляция Java 17. Явный алиас над `compileJava`.

```bash
./gradlew compile
```

### 4.3. `native2ascii`
Читает `src/main/resources/messages_unicode.properties` (UTF-8, кириллица), записывает `build/generated-resources/messages.properties` с escape `\uXXXX`. Результат включается в JAR через `processResources`.

```bash
./gradlew native2ascii
```

### 4.4. `buildFrontend`
Собирает Vue (`npm install && npm run build`) в `src/main/resources/static`.

### 4.5. `build`
Собирает исполняемый fat-JAR `web4-1.0-SNAPSHOT.jar`:
- зависит от `compile`, `native2ascii`, `buildFrontend`;
- `MANIFEST.MF`: `Version`, `Main-Class`, `Start-Class`.

```bash
./gradlew build
```

Цепочка фазы `check` (входит в `build`):
1. `bootJar` (сборка)
2. `test` (JUnit)
3. `functionalTest` (Playwright)

### 4.6. `test`
JUnit 5, запускается **после** `bootJar`.

```bash
./gradlew test
```

### 4.7. `functionalTest`
Отдельная фаза верификации, **входит в `check`** → запускается при `./gradlew build`.

```bash
./gradlew functionalTest -Dapp.base.url=http://localhost:8080
```

Если приложение недоступно — тесты пропускаются (`Assumptions`), сборка не падает.

### 4.8. `diff`
Проверяет `git status` для файлов из `trackedClasses`. При изменениях: `git add` + `git commit`.

```bash
./gradlew diff
```

---

## 5. Локализация (native2ascii + использование в приложении)

### 5.1. Исходный файл
`src/main/resources/messages_unicode.properties` — все UI-строки на русском (48 ключей).

### 5.2. Преобразование
Задача `native2ascii` → `messages.properties` (ASCII + `\uXXXX`).

### 5.3. Backend
| Файл | Роль |
|------|------|
| `MessageSourceConfig.java` | Spring `MessageSource` для `classpath:messages` |
| `MessageService.java` | Чтение всех ключей из `messages.properties` |
| `MessagesController.java` | `GET /api/messages` → JSON `{ "app.title": "..." }` |

### 5.4. Frontend
| Файл | Роль |
|------|------|
| `src/messages.js` | Загрузка `/api/messages`, функция `getMessage(key)` |
| `src/main.js` | Загрузка сообщений **до** монтирования Vue |
| `LoginPage.vue`, `MainPage.vue` | Все строки через `$m('app.xxx')` |

**Пример:** вместо захардкоженного `Войти` в шаблоне: `{{ $m('app.login.button') }}`.

### 5.5. Security
`/api/messages` добавлен в `permitAll` в `SecurityConfig.java`.

---

## 6. Docker

### 6.1. `Dockerfile` (multi-stage)

**Stage 1 `build`** (`gradle:8.10-jdk17`):
- Node.js для сборки Vue;
- `./gradlew clean build -x test -x functionalTest`;
- аргумент `APP_VERSION` для метки образа.

**Stage 2 runtime** (`eclipse-temurin:17-jre`):
- только `app.jar`;
- `java -jar app.jar`, порт 8080.

### 6.2. `docker-compose.yml`

| Сервис | Назначение |
|--------|------------|
| `postgres` | БД PostgreSQL 15 |
| `app` | Приложение, образ `web4-app:${APP_VERSION}` |
| `test` | Профиль `test`: `test` + `functionalTest` в контейнере |

### 6.3. Несколько ревизий
Файл `.env`:
```
APP_VERSION=1.0-SNAPSHOT
```

Запуск другой ревизии:
```bash
APP_VERSION=2.0-dev docker compose up --build
```

Образы: `web4-app:2.0-dev`, `web4-build:2.0-dev` — несколько версий могут сосуществовать.

### 6.4. Команды

```bash
docker compose up --build
docker compose --profile test up --build
```

---

## 7. Тестирование

### 7.1. Модульные (JUnit 5)
**Файл:** `src/test/java/ru/minibobr/point/PointHitCheckerTest.java`

8 тестов логики попадания: I/II/III четверти, R=0, IV четверть.

### 7.2. Функциональные (Playwright)
**База:** `src/functionalTest/java/ru/minibobr/functional/FunctionalTestSupport.java`

**15 кейсов:** `Web4FunctionalTest.java`

| № | Проверка |
|---|----------|
| 1 | Заголовок на странице входа |
| 2 | Часы `#clock` |
| 3–4 | Поля логина и пароля |
| 5–6 | Кнопки «Войти» и «Зарегистрироваться» |
| 7 | Сообщение об успешной регистрации |
| 8 | Редирект на `/main` после входа |
| 9 | SVG-график |
| 10–12 | Поля X, Y, R |
| 13 | Кнопка «Проверить» |
| 14 | Выход → страница входа |
| 15 | `/main` без сессии → редирект на `/` |

---

## 8. Структура проекта

```
web4/
├── build.properties
├── build.gradle
├── .env                          # APP_VERSION для Docker
├── Dockerfile
├── docker-compose.yml
├── task.md                       # этот отчёт
├── src/
│   ├── main/
│   │   ├── java/ru/minibobr/     # Spring Boot backend
│   │   └── resources/
│   │       ├── application.properties
│   │       └── messages_unicode.properties
│   ├── test/java/                # JUnit модульные тесты
│   ├── functionalTest/java/      # Playwright тесты
│   ├── views/                    # Vue страницы
│   ├── messages.js               # загрузка локализации
│   ├── main.js
│   └── router.js
├── index.html
├── package.json
└── vite.config.js
```

---

## 9. Изменения для 100% соответствия (последняя доработка)

### Было не выполнено → исправлено

1. **Строки в коде Vue** → вынесены в `messages_unicode.properties`, UI читает через `/api/messages` и `$m()`.

2. **`functionalTest` не в фазе сборки** → добавлено `check.dependsOn functionalTest`. Команда `./gradlew build` запускает unit + functional тесты.

3. **`build` не зависел от задачи `compile`** → `bootJar` и `jar` зависят от `tasks.named('compile')`.

4. **Циклическая зависимость test↔build** → `test.dependsOn bootJar` (сборка артефакта перед тестами, без цикла).

5. **Несколько ревизий в Docker** → `APP_VERSION` в `.env`, теги образов `web4-app:${APP_VERSION}`.

### Новые файлы

- `src/main/java/ru/minibobr/config/MessageSourceConfig.java`
- `src/main/java/ru/minibobr/service/MessageService.java`
- `src/main/java/ru/minibobr/controller/MessagesController.java`
- `src/messages.js`
- `.env`

### Изменённые файлы

- `build.gradle` — `check` + `functionalTest`, зависимости `compile`
- `build.properties` — `messages_unicode.properties` в `trackedClasses`
- `messages_unicode.properties` — расширен до 48 ключей
- `LoginPage.vue`, `MainPage.vue` — локализация через `$m()`
- `main.js` — загрузка сообщений перед стартом
- `SecurityConfig.java` — `/api/messages` публичный
- `Dockerfile` — `ARG APP_VERSION`, `LABEL`
- `docker-compose.yml` — теги образов, build args

---

## 10. Запуск

### Локально (без Docker)

```bash
docker compose up -d postgres
./gradlew bootRun
```

Открыть: http://localhost:8080

### Полная сборка с тестами

```bash
./gradlew bootRun
./gradlew build
```

### Только тесты

```bash
./gradlew test
./gradlew functionalTest -Dapp.base.url=http://localhost:8080
```

### Docker

```bash
docker compose up --build
docker compose --profile test up --build
```

### IntelliJ IDEA

1. Run `Web4Application` — backend
2. Gradle → `build` — полная сборка + тесты
3. Gradle → `functionalTest` — после запуска приложения
4. Gradle → `diff` — автокоммит tracked-файлов

---

## 11. Приложение Web4 (база лабы №4)

| Компонент | Описание |
|-----------|----------|
| `PointHitChecker` | Проверка попадания в область (3 четверти) |
| `PointService` + JPA | CRUD точек |
| `PointJooqService` | Статистика hits/misses |
| `AuthService` | Регистрация/логин, BCrypt |
| Vue SPA | `/` — вход, `/main` — график и таблица |
| PostgreSQL | Таблицы `users`, `points` |

---

## 12. Примечания

- Файл `messages_unicode.properties` должен открываться в IDEA с кодировкой **UTF-8** (иначе кракозябры в редакторе).
- Functional-тесты при `./gradlew build` без запущенного сервера **пропускаются**, не падают.
- Для functional-тестов в Docker: `docker compose --profile test up --build` (нужен доступ к Docker Hub).
- Задача `diff` требует git-репозиторий и реальные изменения в tracked-файлах.

---

*Документ актуален после доработки проекта до 100% соответствия требованиям лабораторной №3.*
