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

## Production

### Требования

- VPS с Ubuntu/Debian и root-доступом.
- Домен (или поддомен), указывающий на IP сервера:
  - `your-domain.com` — фронтенд
  - `console.your-domain.com` — консоль MinIO
- Открытые порты 80 и 443.

### 1. Подготовка сервера

```bash
apt update && apt upgrade -y

# Установка Docker
curl -fsSL https://get.docker.com | sh
usermod -aG docker $USER

# Переподключиться по SSH, чтобы группа применилась
```

### 2. Настройка firewall

```bash
ufw allow OpenSSH
ufw allow 80
ufw allow 443
ufw enable
ufw status verbose
```

> **Важно:** `ufw allow OpenSSH` должен быть выполнен **до** `ufw enable`, иначе доступ к серверу будет потерян.

### 3. Клонирование репозитория

```bash
git clone <repo-url> ~/Cloud-file-storage
cd ~/Cloud-file-storage
```

### 4. Создание `.env.prod`
```bash
nano .env.prod
```

Пример содержимого:

```env
MINIO_ACCESS_KEY=
MINIO_SECRET_KEY=
MINIO_BROWSER_REDIRECT_URL=https://minio.your-domain.com

DB_PORT=5432
DB_NAME=cloud
DB_USERNAME=
DB_PASSWORD=

REDIS_PORT=6379
REDIS_PASSWORD=
```
### 5. Создание `Caddyfile`

В той же папке, что и `docker-compose-prod.yaml`:

```bash
nano Caddyfile
```

```caddy
your-domain.com {
    reverse_proxy frontend:80
}

console.your-domain.com {
    reverse_proxy minio:9001
}
```

Caddy автоматически получит TLS-сертификаты от Let's Encrypt при первом запуске.

### 6. Запуск

```bash
docker compose --env-file .env.prod -f docker-compose-prod.yaml up -d
```

### 7. Проверка
Открыть в браузере:
- `https://your-domain.com` — frontend
- `https://console.your-domain.com` — console MinIO

## Особенности production-конфигурации

- **Caddy в Docker, а не на хосте.** Вся инфраструктура описывается одним `docker-compose-prod.yaml`. Нет необходимости ставить Caddy на хост и управлять им через systemd.
- **Caddy — единственная входная точка.** Наружу торчат только порты 80 и 443. MinIO, Postgres, Redis доступны только внутри Docker-сети.
- **MinIO не публикует порты.** Консоль доступна только через Caddy по `console.твой-домен.com`. Переменная `MINIO_BROWSER_REDIRECT_URL` обязательна, иначе после логина MinIO редиректит на внутренний адрес и вход ломается.
- **Том `caddy_data` нельзя удалять.** Том содержит сертификаты Let's Encrypt. При удалении Caddy запросит новые, а у Let's Encrypt есть лимиты (5 дублей в неделю на домен).
