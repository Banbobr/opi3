# Web4 - Лабораторная работа №3

Приложение для проверки попадания точек в заданную область на координатной плоскости.

## Технологии

- **Backend**: Spring Boot 3.2.0, Spring Data JPA, JOOQ, PostgreSQL
- **Frontend**: Vue.js 3, Vite
- **База данных**: PostgreSQL 15
- **Контейнеризация**: Docker, Docker Compose

## Требования

- Java 17+
- Node.js 18+
- Docker и Docker Compose

## Запуск через Docker Compose

```bash
docker-compose up --build
```

Приложение будет доступно по адресу: http://localhost:8080

## Локальная разработка

### Backend

```bash
./gradlew bootRun
```

### Frontend

```bash
npm install
npm run dev
```

### Сборка

```bash
# Сборка frontend
npm run build

# Сборка backend
./gradlew build
```

## Структура проекта

- `src/main/java` - Java код (Spring Boot приложение)
- `src/main/resources` - Конфигурационные файлы
- `src/` - Vue.js исходники
- `index.html` - Точка входа для Vue.js

## Особенности

- Аутентификация с хешированием паролей (BCrypt)
- Responsive дизайн (Desktop >= 1147px, Tablet >= 688px, Mobile < 688px)
- REST API для взаимодействия frontend и backend
- Использование JOOQ и Spring Data JPA для работы с БД


