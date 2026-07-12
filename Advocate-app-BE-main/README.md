# ⚖️ Advocate Management System — Backend

This is the **Spring Boot 3.5 backend** for the Advocate Management System.

> 📘 **Full project documentation is available in the parent README**  
> [`../README.md`](../README.md)

## Quick Start

```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

Or double-click `run-backend-dev.bat`.

**Prerequisites:** Java 21+, MySQL 8+

| Service | URL |
|---------|-----|
| API | http://localhost:8080 |
| Health | http://localhost:8080/api/health |

## Tech Stack

- Spring Boot 3.5.6 · Spring Data JPA · Hibernate 6
- MySQL 8+ · JJWT 0.11.5 · OpenPDF 1.3.30
- WebSocket (STOMP + SockJS) · Spring Mail · Spring AOP

## Key Features

- 24 REST controllers · 40 services · 17 entities · 19 repositories
- JWT authentication + RBAC (6 roles, 36 permissions)
- Real-time WebSocket updates
- Email (SMTP) + WhatsApp (Meta/Twilio/Mock)
- PDF report generation · Backup & restore
- AI Assistant · Global search · Audit logging

See the [parent README](../README.md) for architecture, deployment, and full feature documentation.

## Docker

### Prerequisites

- [Docker](https://docs.docker.com/get-docker/) 24+
- [Docker Compose](https://docs.docker.com/compose/install/) v2+ (for local development only)

### Build Image

```bash
docker build -t advocate-backend .
```

### Run Container

```bash
docker run -d --name advocate-backend \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:mysql://<host>:3306/advocate_db \
  -e DB_USERNAME=<user> \
  -e DB_PASSWORD=<password> \
  -e JWT_SECRET=<your-secret> \
  -e MAIL_USERNAME=<email> \
  -e MAIL_PASSWORD=<app-password> \
  -v advocate_uploads:/app/uploads \
  advocate-backend
```

### Docker Compose (Local Dev Only)

```bash
docker compose up -d --build
```

This starts both the backend and a MySQL 8 container. Not intended for production.

### Environment Variables

The table below lists all configurable environment variables. The `SPRING_DATASOURCE_*` variants are also accepted — Spring Boot maps them automatically.

| Variable | Description | Default |
|---|---|---|
| **Database** | | |
| `DB_URL` | JDBC URL | `jdbc:mysql://localhost:3306/advocate_db` |
| `DB_USERNAME` | Database user | `root` |
| `DB_PASSWORD` | Database password | `Chill.code20` |
| `DB_SHOW_SQL` | Log SQL statements | `false` |
| `DB_DDL_AUTO` | Hibernate DDL mode | `update` |
| **JWT** | | |
| `JWT_SECRET` | Signing key (generate with `openssl rand -base64 64`) | `my_super_secret_key_for_advocate_app_12345` |
| `JWT_EXPIRATION_MS` | Token expiry in milliseconds | `86400000` |
| **Mail / SMTP** | | |
| `MAIL_HOST` | SMTP host | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP port | `587` |
| `MAIL_USERNAME` | SMTP username | — |
| `MAIL_PASSWORD` | SMTP password / app password | — |
| **Email Notifications** | | |
| `NOTIFICATION_EMAIL_ENABLED` | Enable/disable email sending | `true` |
| `NOTIFICATION_SENDER_NAME` | Sender display name | `Advocate Case Management System` |
| **WhatsApp** | | |
| `WHATSAPP_PROVIDER` | Provider (`meta` / `mock` / `twilio`) | `mock` |
| `WHATSAPP_API_URL` | Meta Graph API URL | `https://graph.facebook.com/v23.0` |
| `WHATSAPP_VERIFY_TOKEN` | Webhook verify token | — |
| `WHATSAPP_PHONE_NUMBER_ID` | Meta phone number ID | — |
| `WHATSAPP_ACCESS_TOKEN` | Meta access token | — |
| `WHATSAPP_DEFAULT_LANGUAGE` | Template language | `en_US` |
| **Twilio** | | |
| `TWILIO_ACCOUNT_SID` | Twilio Account SID | — |
| `TWILIO_AUTH_TOKEN` | Twilio Auth Token | — |
| `TWILIO_WHATSAPP_FROM` | Twilio WhatsApp sender | — |
| **Document Upload** | | |
| `DOCUMENT_UPLOAD_DIR` | Upload directory path | `uploads` |
| **CORS** | | |
| `CORS_ORIGINS` | Comma-separated allowed origins | — |
| **Frontend** | | |
| `FRONTEND_URL` | Frontend base URL (for email links) | `http://localhost:5173` |
| **OTP** | | |
| `OTP_SALT` | OTP hashing salt | — |
| `OTP_EXPIRY_MINUTES` | OTP validity in minutes | `10` |
| `OTP_RATE_LIMIT` | Max OTP requests per hour | `5` |
| **Crypto** | | |
| `CRYPTO_SECRET_KEY` | 32-byte Base64 key for communication encryption | — |

### Production Deployment (Koyeb)

1. **Push the image** to a container registry (Docker Hub, GitHub Container Registry, etc.):

   ```bash
   docker tag advocate-backend ghcr.io/<your-org>/advocate-backend:latest
   docker push ghcr.io/<your-org>/advocate-backend:latest
   ```

2. **Create a Koyeb App** via the dashboard or CLI:

   - **Source:** Deploy from container registry
   - **Image:** `ghcr.io/<your-org>/advocate-backend:latest`
   - **Port:** `8080`
   - **Command:** *(leave blank — uses Dockerfile ENTRYPOINT)*
   - **Environment variables:** Set all required variables from the table above (especially `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`)

   Or deploy directly from the Git repository:

   - **Source:** Git repository
   - **Build Context:** `Advocate-app-BE-main`
   - **Dockerfile:** `Advocate-app-BE-main/Dockerfile`
   - **Port:** `8080`
   - **Environment variables:** Same as above
