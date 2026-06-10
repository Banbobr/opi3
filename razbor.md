# Полный разбор выполненной лабораторной №3 (Web4)

Документ описывает **всё добавленное и изменённое** с привязкой к **каждому пункту задания**, включая **полный разбор тестирования** (unit, functional, Gradle, Docker).

Краткий отчёт: `task.md`.

---

## Содержание

1. [Требования задания](#1-требования-задания)
2. [Карта: требование → файлы](#2-карта-требование--файлы)
3. [Тестирование — полный разбор](#3-тестирование--полный-разбор)
4. [Добавленные файлы](#4-добавленные-файлы)
5. [Изменённые файлы](#5-изменённые-файлы)
6. [Генерируемые файлы](#6-генерируемые-файлы)
7. [Файлы лабы №4 без изменений](#7-файлы-лабы-4-без-изменений)
8. [Сценарии «как всё связано»](#8-сценарии-как-всё-связано)
9. [Команды для проверки](#9-команды-для-проверки)

---

## 1. Требования задания

| № | Требование | Где выполнено |
|---|------------|---------------|
| 1 | Основа — веб-лабораторная №4 | Spring Boot + Vue + PostgreSQL |
| 2 | Ant → Gradle | `build.gradle`, `gradlew` |
| 3 | SVN → Git | задача `diff` |
| 4 | `build.properties` | `build.properties` |
| 5 | Gradle-задачи | `build.gradle` |
| 6 | MANIFEST Version + Main-Class | `jar` / `bootJar` |
| 7 | Локализация + `native2ascii` | `messages_unicode.properties`, Vue, API |
| 8 | Docker multi-stage | `Dockerfile` |
| 9 | docker-compose (app, БД, тесты) | `docker-compose.yml` |
| 10 | С Docker и без | Gradle + compose |
| 11 | Unit JUnit 5 | `PointHitCheckerTest.java` |
| 12 | 15 functional Playwright | `Web4FunctionalTest.java` |
| 13 | Functional — фаза сборки | `check.dependsOn functionalTest` |
| 14 | Несколько ревизий | `.env`, теги образов |

---

## 2. Карта: требование → файлы

| Пункт | Добавлено | Изменено |
|-------|-----------|----------|
| Параметры | `build.properties`, `.env` | `build.gradle` |
| Gradle-задачи | — | `build.gradle` |
| Локализация | `messages_unicode.properties`, `MessageSourceConfig`, `MessageService`, `MessagesController`, `messages.js` | `LoginPage.vue`, `MainPage.vue`, `main.js`, `SecurityConfig` |
| MANIFEST / diff | — | `build.gradle`, `build.properties` |
| Docker | — | `Dockerfile`, `docker-compose.yml` |
| **Unit-тесты** | `PointHitCheckerTest.java` | `build.gradle` |
| **Functional-тесты** | `FunctionalTestSupport.java`, `Web4FunctionalTest.java` | `build.gradle` |
| Документация | `task.md`, `razbor.md` | — |

---

## 3. Тестирование — полный разбор

### 3.1. Требования методички к тестам

| Требование | Выполнение |
|------------|------------|
| Зависимости JUnit 5 в `build.gradle` | `spring-boot-starter-test`, `junit-jupiter` |
| Зависимости Playwright в `build.gradle` | `com.microsoft.playwright:playwright:1.41.2` |
| Шаблон unit-теста в `src/test/java` | `PointHitCheckerTest.java` |
| 15 functional test cases | `Web4FunctionalTest.java`, методы `case01`…`case15` |
| Отдельная фаза `functionalTest` | задача `functionalTest` в `build.gradle` |
| Functional tests — часть сборки | `check.dependsOn functionalTest` |
| Тесты локально | `./gradlew test`, `./gradlew functionalTest` |
| Тесты в Docker | сервис `test` в `docker-compose.yml` |

---

### 3.2. Архитектура: два уровня

```
UNIT (JUnit 5)
├── Каталог:     src/test/java/
├── Файл:        PointHitCheckerTest.java
├── Объект:      PointHitChecker (математика области)
├── Сервер:      НЕ нужен
├── Браузер:     НЕ нужен
└── Задача:      test

FUNCTIONAL (JUnit 5 + Playwright)
├── Каталог:     src/functionalTest/java/
├── Файлы:       FunctionalTestSupport.java, Web4FunctionalTest.java
├── Объект:      весь UI (Vue + Spring + сессии)
├── Сервер:      НУЖЕН (localhost:8080 или app в Docker)
├── Браузер:     Chromium (headless)
└── Задача:      functionalTest
```

**Зачем два уровня:** unit быстро ловит ошибки в формулах; functional проверяет, что пользователь реально видит кнопки, формы и редиректы.

---

### 3.3. Интеграция в Gradle (`build.gradle` — изменён)

#### Зависимости

```groovy
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'org.springframework.security:spring-security-test'

functionalTestImplementation 'com.microsoft.playwright:playwright:1.41.2'
functionalTestImplementation 'org.junit.jupiter:junit-jupiter-api:5.10.2'
functionalTestRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.10.2'
functionalTestRuntimeOnly 'org.junit.platform:junit-platform-launcher'
```

| Зависимость | Уровень | Назначение |
|-------------|---------|------------|
| `spring-boot-starter-test` | unit | JUnit 5, AssertJ, Mockito |
| `playwright` | functional | Управление Chromium |
| `junit-jupiter-*` | functional | `@Test`, `@BeforeAll`, движок |

#### Source set `functionalTest`

Отдельный от `test` каталог — по заданию functional не смешивается с unit:

```
src/functionalTest/java/ru/minibobr/functional/
```

Gradle компилирует его задачами `compileFunctionalTest`, `processFunctionalTestResources`.

#### Задачи, связанные с тестами

| Задача | Пункт задания | Что делает |
|--------|---------------|------------|
| `test` | § test после build | 8 unit-тестов; `dependsOn bootJar` |
| `installPlaywrightBrowsers` | Playwright | `java ... CLI install chromium` |
| `functionalTest` | § 15 кейсов + фаза | Playwright в браузере |
| `check` | § фаза сборки | `dependsOn functionalTest` |
| `build` | полная сборка | `assemble` + `check` = JAR + unit + functional |

#### Порядок при `./gradlew build`

```
1. bootJar          ← сборка JAR (compile, native2ascii, buildFrontend)
2. test             ← PointHitCheckerTest (8 тестов)
3. functionalTest   ← Web4FunctionalTest (15 тестов), mustRunAfter test
```

#### URL приложения для functional

| Способ | Значение |
|--------|----------|
| По умолчанию | `http://localhost:8080` |
| JVM | `-Dapp.base.url=http://localhost:8080` |
| Env (Docker) | `BASE_URL=http://app:8080` |

#### Если сервер не запущен

`FunctionalTestSupport` в `@BeforeAll` вызывает `Assumptions.assumeTrue(isAppReachable(...))`.  
Тесты **пропускаются** (skipped), сборка **не падает**.  
Для реального прогона functional нужен `bootRun` или Docker-сервис `app`.

---

### 3.4. Unit-тесты — `PointHitCheckerTest.java` (добавлен)

**Путь:** `src/test/java/ru/minibobr/point/PointHitCheckerTest.java`  
**Пункт задания:** модульные тесты JUnit 5, шаблон в `src/test/java`.  
**Тестируемый класс (лаба №4):** `src/main/java/ru/minibobr/point/PointHitChecker.java`

#### Логика `PointHitChecker` (что именно тестируем)

| Четверть | Условие | Фигура | Критерий попадания |
|----------|---------|--------|-------------------|
| I | x ≥ 0, y ≥ 0 | Прямоугольник | x ≤ R и y ≤ R |
| II | x ≤ 0, y ≥ 0 | Треугольник | y − x ≤ R/2 |
| III | x ≤ 0, y ≤ 0 | Четверть круга | x² + y² ≤ R² |
| IV | остальное | — | всегда промах |
| Особый случай | R = 0 | — | всегда промах |

#### Каждый из 8 тестов

| № | Метод | Координаты (x, y), R | Ожидание | Смысл |
|---|--------|----------------------|----------|-------|
| 1 | `hitInFirstQuadrantRectangle` | (1, 1), 2 | `assertTrue` | Точка внутри квадрата I четверти |
| 2 | `missOutsideFirstQuadrantRectangle` | (3, 3), 2 | `assertFalse` | За пределами квадрата (x,y > R) |
| 3 | `hitInSecondQuadrantTriangle` | (−1, 1), 2 | `assertTrue` | Попадание в треугольник II четверти |
| 4 | `missInSecondQuadrantTriangle` | (−2, 3), 2 | `assertFalse` | Выше гипотенузы треугольника |
| 5 | `hitInThirdQuadrantCircle` | (−1, −1), 2 | `assertTrue` | Внутри четверти круга III четверти |
| 6 | `missInThirdQuadrantCircle` | (−2, −2), 2 | `assertFalse` | Вне круга (дальше радиуса) |
| 7 | `missWhenRadiusIsZero` | (1, 1), 0 | `assertFalse` | R=0 — дегenerate case |
| 8 | `missInFourthQuadrant` | (1, −1), 2 | `assertFalse` | IV четверть не входит в область |

#### Структура тестового класса

- Хелпер `point(x, y, r)` — создаёт `Point` без Spring и БД.
- `new PointHitChecker(point)` — тестируется чистая логика.
- `@Test` + `assertTrue` / `assertFalse` — стандарт JUnit 5.

#### Связь с приложением

`PointService.savePoint()` вызывает `PointHitChecker` при каждом `POST /api/points`.  
Ошибка в unit-тестируемой логике → неверные «Попадание/Промах» на главной странице.

#### Запуск

```bash
./gradlew test
```

Отчёт: `build/reports/tests/test/index.html`

---

### 3.5. Functional — инфраструктура `FunctionalTestSupport.java` (добавлен)

**Путь:** `src/functionalTest/java/ru/minibobr/functional/FunctionalTestSupport.java`  
**Пункт задания:** шаблон/база для functional-тестов.

| Элемент | Назначение |
|---------|------------|
| `@BeforeAll setUp()` | Читает `app.base.url`, проверяет доступность сервера, запускает Playwright + Chromium headless |
| `@AfterAll tearDown()` | Закрывает browser и playwright |
| `newPage()` | Новая вкладка браузера для изоляции тестов |
| `uniqueUser()` | `user_<timestamp>` — уникальный логин, чтобы не конфликтовать в БД |
| `registerAndLogin(page, user, pass)` | Регистрация → вход → ожидание URL `/main` |
| `isAppReachable(url)` | HTTP GET, timeout 3 сек; если сервер недоступен — skip всех тестов |

**Почему headless:** тесты гоняются в CI/Docker без GUI.

---

### 3.6. Functional — 15 тест-кейсов `Web4FunctionalTest.java` (добавлен)

**Путь:** `src/functionalTest/java/ru/minibobr/functional/Web4FunctionalTest.java`  
**Пункт задания:** ровно 15 functional test cases, Playwright.

Наследует `FunctionalTestSupport`. Каждый кейс — отдельный `@Test`.

---

#### Кейс 1 — `case01_loginPageTitleVisible`

| | |
|---|---|
| **Проверяет** | Страница входа открывается, заголовок виден |
| **Шаги** | `navigate(baseUrl + "/")` |
| **Селектор** | `h1` contains `"Лабораторная работа"` |
| **Связь с приложением** | `LoginPage.vue` → `$m('app.title')` → `messages_unicode.properties` |
| **Пункт задания** | functional: главная страница (вход) загружается |

---

#### Кейс 2 — `case02_clockElementExists`

| | |
|---|---|
| **Проверяет** | Часы на странице входа |
| **Селектор** | `#clock` visible |
| **Связь** | `LoginPage.vue`, `updateClock()` в `mounted` |

---

#### Кейс 3 — `case03_usernameFieldVisible`

| | |
|---|---|
| **Проверяет** | Поле логина |
| **Селектор** | `#username` visible |
| **Связь** | `LoginPage.vue`, label `$m('app.username.label')` |

---

#### Кейс 4 — `case04_passwordFieldVisible`

| | |
|---|---|
| **Проверяет** | Поле пароля |
| **Селектор** | `#password` visible |

---

#### Кейс 5 — `case05_loginButtonVisible`

| | |
|---|---|
| **Проверяет** | Кнопка входа |
| **Селектор** | role=BUTTON, name=`"Войти"` |
| **Связь** | `$m('app.login.button')` |

---

#### Кейс 6 — `case06_registerButtonVisible`

| | |
|---|---|
| **Проверяет** | Кнопка регистрации |
| **Селектор** | role=BUTTON, name=`"Зарегистрироваться"` |
| **Связь** | `$m('app.register.button')` |

---

#### Кейс 7 — `case07_registerShowsSuccessMessage`

| | |
|---|---|
| **Проверяет** | Успешная регистрация нового пользователя |
| **Шаги** | Уникальный user → fill username/password → click «Зарегистрироваться» |
| **Селектор** | `.error-message` contains `"Регистрация успешна"` |
| **Связь с backend** | `POST /api/auth/register` → `AuthService.register()` |
| **Связь с UI** | `$m('app.register.success')` |

---

#### Кейс 8 — `case08_loginRedirectsToMain`

| | |
|---|---|
| **Проверяет** | После входа попадаем на главную |
| **Шаги** | `registerAndLogin()` |
| **Проверка** | `page.url()` contains `/main` |
| **Связь** | `AuthController.login`, `router.push('/main')` |

---

#### Кейс 9 — `case09_mainPageGraphVisible`

| | |
|---|---|
| **Проверяет** | SVG-график на главной |
| **Предусловие** | Авторизация через `registerAndLogin` |
| **Селектор** | `#svg` visible |
| **Связь** | `MainPage.vue`, область варианта 478265 |

---

#### Кейс 10 — `case10_mainPageXSelectVisible`

| | |
|---|---|
| **Проверяет** | Выпадающий список X |
| **Селектор** | `.custom-select`.first() visible |
| **Связь** | `MainPage.vue`, `xValues: [-4..4]` |

---

#### Кейс 11 — `case11_mainPageYInputVisible`

| | |
|---|---|
| **Проверяет** | Поле ввода Y |
| **Селектор** | `#y-input` visible |

---

#### Кейс 12 — `case12_mainPageRSelectVisible`

| | |
|---|---|
| **Проверяет** | Выпадающий список R |
| **Селектор** | `.custom-select`.nth(1) visible |
| **Связь** | `rValues: [1, 2, 3, 4]` |

---

#### Кейс 13 — `case13_checkButtonVisible`

| | |
|---|---|
| **Проверяет** | Кнопка «Проверить» |
| **Селектор** | role=BUTTON, name=`"Проверить"` |
| **Связь** | `checkPoint()` → `POST /api/points` |

---

#### Кейс 14 — `case14_logoutReturnsToLogin`

| | |
|---|---|
| **Проверяет** | Выход из системы |
| **Шаги** | Login → click «Выйти» → wait URL `/` |
| **Селектор** | `#username` visible (снова форма входа) |
| **Связь** | `POST /api/auth/logout`, `$m('app.logout')` |

---

#### Кейс 15 — `case15_unauthenticatedMainRedirectsToLogin`

| | |
|---|---|
| **Проверяет** | Защита маршрута `/main` |
| **Шаги** | Без логина → `navigate(/main)` → редирект на `/` |
| **Селектор** | `#username` visible |
| **Связь** | `router.js` guard → `GET /api/auth/check` → 401 → `next('/')` |

---

### 3.7. Сводная таблица 15 functional-кейсов

| № | Метод | Страница | Что проверяет |
|---|--------|----------|---------------|
| 1 | case01 | `/` | Заголовок |
| 2 | case02 | `/` | Часы |
| 3 | case03 | `/` | Поле логина |
| 4 | case04 | `/` | Поле пароля |
| 5 | case05 | `/` | Кнопка «Войти» |
| 6 | case06 | `/` | Кнопка «Зарегистрироваться» |
| 7 | case07 | `/` | Регистрация + сообщение |
| 8 | case08 | `/main` | Редирект после входа |
| 9 | case09 | `/main` | График SVG |
| 10 | case10 | `/main` | Select X |
| 11 | case11 | `/main` | Input Y |
| 12 | case12 | `/main` | Select R |
| 13 | case13 | `/main` | Кнопка «Проверить» |
| 14 | case14 | `/` | Выход |
| 15 | case15 | `/main`→`/` | Защита без сессии |

---

### 3.8. Тестирование в Docker (`docker-compose.yml` — изменён)

**Пункт задания:** тесты в контейнере + развёртывание app для functional.

#### Сервис `test` (профиль `test`)

```yaml
profiles: [test]
depends_on: postgres (healthy), app (started)
command: ./gradlew test functionalTest -Dapp.base.url=http://app:8080
environment: BASE_URL=http://app:8080, POSTGRES_*
```

| Компонент | Роль в тестировании |
|-----------|---------------------|
| `postgres` | БД для app (регистрация, точки) |
| `app` | Развёрнутое приложение — мишень для Playwright |
| `test` | Контейнер сборки (stage `build` Dockerfile), гоняет Gradle-тесты |

#### Схема прогона в Docker

```
postgres (healthy)
    ↓
app (Spring Boot :8080)
    ↓
test-контейнер:
    ./gradlew test          → PointHitCheckerTest
    ./gradlew functionalTest → Web4FunctionalTest (BASE_URL=http://app:8080)
```

**Запуск:**
```bash
docker compose --profile test up --build
```

#### Локально без Docker

```bash
docker compose up -d postgres
./gradlew bootRun
./gradlew test
./gradlew functionalTest
```

или полная сборка с тестами:
```bash
./gradlew bootRun
./gradlew build
```

---

### 3.9. Файлы тестирования — итог

| Файл | Статус | Пункт задания | Роль |
|------|--------|---------------|------|
| `PointHitCheckerTest.java` | добавлен | unit JUnit 5 | 8 тестов математики |
| `FunctionalTestSupport.java` | добавлен | infrastructure | Playwright, login helper |
| `Web4FunctionalTest.java` | добавлен | 15 cases | UI-сценарии |
| `build.gradle` | изменён | deps + tasks | test, functionalTest, check |
| `docker-compose.yml` | изменён | тесты в Docker | сервис `test` |

---

## 4. Добавленные файлы

### `build.properties`
**Пункт:** §1 параметры.  
**Роль:** `version`, `mainClass`, `trackedClasses` для Gradle, manifest, `diff`.

### `.env`
**Пункт:** §3 несколько ревизий.  
**Роль:** `APP_VERSION=1.0-SNAPSHOT` для тегов Docker-образов.

### `messages_unicode.properties`
**Пункт:** §2 native2ascii + строки из кода.  
**Роль:** 48 UI-строк UTF-8, вход для `native2ascii`.

### `MessageSourceConfig.java`
**Пункт:** §2 использование локализации.  
**Роль:** Spring `MessageSource` → `classpath:messages`.

### `MessageService.java`
**Пункт:** §2 строки реально используются.  
**Роль:** отдаёт все ключи из `messages.properties`.

### `MessagesController.java`
**Пункт:** §2 связь backend ↔ frontend.  
**Роль:** `GET /api/messages` → JSON для Vue.

### `messages.js`
**Пункт:** §2 строки вынесены из Vue.  
**Роль:** `loadMessages()`, `getMessage(key)`.

### `task.md` / `razbor.md`
**Пункт:** документация.  
**Роль:** отчёт и полный разбор.

*(Тестовые файлы — см. [раздел 3](#3-тестирование--полный-разбор))*

---

## 5. Изменённые файлы

### `build.gradle`
**Пункт:** §1–§2, §4 тесты, MANIFEST, diff.

| Добавлено | Пункт |
|-----------|-------|
| Чтение `build.properties` | §1 |
| `native2ascii`, `compile`, `diff` | §2 |
| `jar`/`bootJar` manifest | MANIFEST |
| `test.dependsOn bootJar` | test после build |
| source set `functionalTest` | §4 structure |
| `functionalTest`, `installPlaywrightBrowsers` | §4 Playwright |
| `check.dependsOn functionalTest` | §4 фаза сборки |
| `buildFrontend` | сборка Vue в JAR |

### `Dockerfile`
**Пункт:** §3 multi-stage Gradle → JRE.  
**Изменено:** 2 этапа, `ARG APP_VERSION`, сборка через `./gradlew build`.

### `docker-compose.yml`
**Пункт:** §3 app + БД + тесты.  
**Изменено:** теги `web4-app:${APP_VERSION}`, сервис `test` с профилем.

### `main.js`
**Пункт:** §2 локализация.  
**Изменено:** `loadMessages()` перед `mount`, `$m()` в Vue.

### `LoginPage.vue` / `MainPage.vue`
**Пункт:** §2 строки из кода → properties.  
**Изменено:** все UI-строки через `$m('app.xxx')`.

### `SecurityConfig.java`
**Пункт:** §2 локализация на странице входа.  
**Изменено:** `permitAll` для `/api/messages`.

---

## 6. Генерируемые файлы

### `build/generated-resources/messages.properties`
**Пункт:** §2 результат `native2ascii`.  
**Не в git.** Создаётся при `./gradlew native2ascii` или `build`.  
Кириллица → `\uXXXX`, попадает в JAR.

### `build/reports/tests/test/index.html`
Отчёт unit-тестов после `./gradlew test`.

### `build/reports/tests/functionalTest/index.html`
Отчёт functional-тестов после `./gradlew functionalTest`.

---

## 7. Файлы лабы №4 без изменений

| Файл | Роль | Связь с тестами |
|------|------|-----------------|
| `PointHitChecker.java` | Логика области | тестируется unit-тестами |
| `PointService.java` | Сохранение точек | использует PointHitChecker |
| `AuthController.java` | login/register/logout | кейсы 7, 8, 14, 15 |
| `LoginPage.vue` | UI входа | кейсы 1–7 (до локализации — хардкод) |
| `MainPage.vue` | UI главной | кейсы 9–14 |
| `router.js` | guard `/main` | кейс 15 |
| Остальной backend/frontend | лаба №4 | — |

---

## 8. Сценарии «как всё связано»

### Локализация
```
messages_unicode.properties → native2ascii → messages.properties
→ MessageService → /api/messages → messages.js → $m() в Vue
```

### Сборка и тесты
```
build.properties → build.gradle → bootJar
→ test (PointHitCheckerTest)
→ functionalTest (Web4FunctionalTest)
```

### Docker + тесты
```
.env → compose: postgres + app + test(profile)
→ functionalTest с BASE_URL=http://app:8080
```

---

## 9. Команды для проверки

| Что проверить | Команда |
|---------------|---------|
| Unit-тесты | `./gradlew test` |
| Functional (нужен bootRun) | `./gradlew bootRun` затем `./gradlew functionalTest` |
| Всё в сборке | `./gradlew bootRun` затем `./gradlew build` |
| Unit + functional в Docker | `docker compose --profile test up --build` |
| Отчёт unit | `build/reports/tests/test/index.html` |
| Отчёт functional | `build/reports/tests/functionalTest/index.html` |
| Playwright установлен | автоматически в `installPlaywrightBrowsers` |

---

## Отличие `task.md` и `razbor.md`

| `task.md` | `razbor.md` |
|-----------|-------------|
| Краткий отчёт, чеклист | Полный разбор по файлам и тестам |
| Команды | Каждый тест-кейс, Gradle, Docker |
| Для сдачи | Для понимания и защиты |

---

*Документ описывает проект Web4 в состоянии полного соответствия лабораторной №3.*
