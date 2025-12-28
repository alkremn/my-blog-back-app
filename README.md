# Блог приложение - Backend

RESTful backend для блог-приложения, построенный на Spring Boot, Spring JDBC и PostgreSQL.

## Возможности

- **Управление постами**
  - Создание, чтение, обновление и удаление постов
  - Пагинация и поиск постов
  - Поиск по названию или тегам
  - Функция лайков для постов
  - Автоматический подсчет количества комментариев

- **Система комментариев**
  - Добавление комментариев к постам
  - Обновление и удаление комментариев
  - Каскадное удаление при удалении поста

## Стек технологий

- **Фреймворк**: Spring Boot 4.0.0
- **База данных**: PostgreSQL 16 (продакшн), H2 2.4.240 (тестирование)
- **Java**: 21
- **Система сборки**: Maven
- **Тестирование**: JUnit 5, Spring Boot Test, MockMvc

## Требования

- Java 21 или выше
- Docker и Docker Compose (для PostgreSQL)
- Maven 3.x (или используйте включенный wrapper)

## Начало работы

### 1. Запуск базы данных PostgreSQL

Используя Docker Compose:

```bash
cd docker
docker compose up -d
```

Это выполнит:
- Запуск PostgreSQL 16 на порту 5432
- Создание базы данных `blogdb`
- Автоматическое выполнение скриптов инициализации схемы
- Загрузку тестовых данных для разработки

### 2. Сборка приложения

```bash
./mvnw clean package
```

### 3. Запуск тестов

```bash
./mvnw test
```

Все тесты используют встроенную базу данных H2 и не требуют запущенного PostgreSQL.

### 4. Запуск приложения

Несколько способов запуска Spring Boot приложения:

#### Способ 1: Использование Maven Plugin

```bash
./mvnw spring-boot:run
```

#### Способ 2: Запуск JAR файла

Сначала соберите приложение:
```bash
./mvnw clean package
```

Затем запустите JAR файл:
```bash
java -jar target/blog-0.0.1-SNAPSHOT.jar
```

#### Способ 3: Запуск через IDE

Запустите главный класс `com.kremnev.blog.BlogApplication` из IDE (IntelliJ IDEA, Eclipse, VS Code).

### 5. Доступ к приложению

После запуска API будет доступно по адресу:
```
http://localhost:8080/api/
```

## API эндпоинты

### Посты

- `GET /api/posts` - Получить все посты (с пагинацией и поиском)
  - Параметры запроса: `page`, `size`, `search`
  - Формат поиска: `термины заголовка #тег1 #тег2`
- `GET /api/posts/{id}` - Получить пост по ID
- `POST /api/posts` - Создать новый пост
- `PUT /api/posts/{id}` - Обновить пост
- `DELETE /api/posts/{id}` - Удалить пост
- `POST /api/posts/{id}/likes` - Добавить лайк к посту

### Комментарии

- `GET /api/posts/{postId}/comments` - Получить все комментарии к посту
- `GET /api/posts/{postId}/comments/{id}` - Получить комментарий по ID
- `POST /api/posts/{postId}/comments` - Создать новый комментарий
- `PUT /api/posts/{postId}/comments/{id}` - Обновить комментарий
- `DELETE /api/posts/{postId}/comments/{id}` - Удалить комментарий

### Изображения

- `PUT /api/posts/{postId}/image` - Загрузить/обновить изображение для поста
  - Content-Type: `multipart/form-data`
  - Параметр: `image` (файл изображения)
- `GET /api/posts/{postId}/image` - Скачать изображение поста
