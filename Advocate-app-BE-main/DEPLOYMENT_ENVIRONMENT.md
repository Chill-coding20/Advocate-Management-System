# Deployment Environment Guide

## Prerequisites

- Java 17+
- MySQL 8+
- Maven (`.\mvnw`)

## Environment Variables

Copy `.env.example` to `.env` and set all values. The application reads from environment
variables at startup. If a variable is not set, the `application.properties` default is used.

### Required (application fails to start without these)

| Variable     | Description                                  |
|--------------|----------------------------------------------|
| `DB_URL`     | JDBC URL, e.g. `jdbc:mysql://localhost:3306/advocate_db` |
| `JWT_SECRET` | Secret key used to sign JSON Web Tokens. Generate with `openssl rand -base64 64`. |

### Strongly Recommended

| Variable             | Description                                                        |
|----------------------|--------------------------------------------------------------------|
| `DB_USERNAME`        | Database user                                                      |
| `DB_PASSWORD`        | Database password                                                  |
| `MAIL_USERNAME`      | SMTP username / sender email                                       |
| `MAIL_PASSWORD`      | SMTP app password (not your regular email password)                |
| `CRYPTO_SECRET_KEY`  | 32-byte Base64-encoded AES-256 key. Generate: `openssl rand -base64 32` |
| `OTP_SALT`           | Salt for OTP hashing. Generate a random string.                    |
| `CORS_ORIGINS`       | Comma-separated allowed origins (default: `http://localhost:5173`) |

### Optional

| Variable                  | Default                              | Description                        |
|---------------------------|--------------------------------------|------------------------------------|
| `DB_SHOW_SQL`             | `false`                              | Hibernate SQL logging              |
| `DB_DDL_AUTO`             | `update`                             | Hibernate DDL mode                 |
| `JWT_EXPIRATION_MS`       | `86400000` (24h)                     | JWT token validity                 |
| `NOTIFICATION_EMAIL_ENABLED` | `true`                            | Enable/disable email               |
| `WHATSAPP_PROVIDER`       | `mock`                               | `mock`, `meta`, or `twilio`        |
| `WHATSAPP_API_URL`        | `https://graph.facebook.com/v23.0`   | Meta Cloud API base URL            |
| `WHATSAPP_VERIFY_TOKEN`   | `AdvocateApp2026SecureToken`         | Webhook verify token               |
| `WHATSAPP_PHONE_NUMBER_ID` | (none)                              | Meta phone number ID               |
| `WHATSAPP_ACCESS_TOKEN`   | (none)                               | Meta access token                  |
| `DOCUMENT_UPLOAD_DIR`     | `uploads`                            | File upload directory              |
| `OTP_EXPIRY_MINUTES`      | `10`                                 | OTP validity                       |
| `OTP_RATE_LIMIT`          | `5`                                  | Max OTP requests per hour per email|

## Production Checklist

1. Generate a strong `JWT_SECRET` (`openssl rand -base64 64`).
2. Generate a strong `CRYPTO_SECRET_KEY` (`openssl rand -base64 32`).
3. Set strong DB credentials.
4. Set `DB_SHOW_SQL=false`.
5. Use a real SMTP provider (Gmail, SendGrid, etc.).
6. If using WhatsApp, set provider to `meta` and configure credentials.
7. Set `CORS_ORIGINS` to your frontend domain(s).
8. Verify `.env` is in `.gitignore` — **never commit secrets**.

## How It Works

- The backend reads from `application.properties` using `${VAR_NAME:default}` syntax.
- If the environment variable is set, Spring uses it; otherwise the default is used.
- At startup, `AdvocateAppApplication.environmentCheck()` validates that `JWT_SECRET` and `DB_URL` are present, and warns if defaults are detected.

## Building & Running

```bash
# Build (without running tests)
.\mvnw clean package -DskipTests

# Run
java -jar target/advocate-app-0.0.1-SNAPSHOT.jar
```
