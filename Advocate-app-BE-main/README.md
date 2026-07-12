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
