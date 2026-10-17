# Cloud File Storage

Учебное приложение для хранения файлов: Spring Boot + React + MinIO + Postgres + Redis.

## Локальный запуск
1. Создать `.env.standalone` на основе `.env.standalone.example`.

2. Поднять инфраструктуру (Postgres, Redis, MinIO, Nginx + static):
   ```bash
   docker compose -f docker-compose-standalone.yml --env-file .env.standalone up -d
   ```
3. Собрать backend:
   ```bash
   ./gradlew clean build
   ```
4. Запустить собранный JAR с активным profile standalone.
   ```bash
   java -jar build/libs/*.jar --spring.profiles.active=standalone
   ```
5. Приступить к работе с функционалом приложения:
   - Frontend: `http://localhost` (port 80)
   - API: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui/index.html`
   - Console MinIO: `http://localhost:${MINIO_CONSOLE_PORT}`
