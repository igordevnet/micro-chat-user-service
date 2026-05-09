<div align="center">

# 👤 Micro-Chat: User Service

**The secure entry point for the Micro-Chat ecosystem, handling identity, authentication, and account lifecycle management.**

<br/>

[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)](#)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-brightgreen?style=for-the-badge&logo=spring-boot)](#)
[![Spring Security](https://img.shields.io/badge/Spring_Security-Auth-6DB33F?style=for-the-badge&logo=spring-security)](#)
[![JWT](https://img.shields.io/badge/JWT-Tokens-black?style=for-the-badge&logo=json-web-tokens)](#)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-336791?style=for-the-badge&logo=postgresql)](#)

</div>

<br/>

## 🏗️ Architecture & Features

This service acts as the Identity Provider (IdP) for the entire distributed system. It is built strictly using **Clean Architecture** and **Domain-Driven Design (DDD)**, completely isolating the core business logic (Use Cases) from external frameworks like Spring Security and Jakarta Mail.

### ✨ Key Capabilities
- **Stateless Authentication:** Implements secure JWT generation (Access Tokens) and HttpOnly Cookie-based Refresh Tokens.
- **Account Lifecycle:** Full registration flow with automated SMTP email verification.
- **Credential Recovery:** Secure, token-based password reset architecture.
- **Centralized Security:** Acts as the source of truth for the API Gateway and downstream microservices.

<br/>

## 🛠️ Environment Variables

To run this service locally or in a Kubernetes cluster, configure the following environment variables. If omitted, the application will safely default to the fallback values.

### 🗄️ Database (PostgreSQL)

| Variable | Description | Default Fallback |
| :--- | :--- | :--- |
| `HOST` | PostgreSQL database host | `localhost` |
| `PORT` | PostgreSQL database port | `5432` |
| `DB_NAME` | Database schema name | `db_micro_chat` |

### 🔒 Security & JWT

| Variable | Description | Default Fallback |
| :--- | :--- | :--- |
| `SECRET_KEY` | Base64 encoded secret for signing Access & Refresh Tokens | `404E63...` |
| `JWT_EXPIRATION` | Access token lifespan in milliseconds (Default: 24 mins) | `1440000` |

### 📧 SMTP Mail Server (Verification & Recovery)

| Variable | Description | Default Fallback |
| :--- | :--- | :--- |
| `MAIL_HOST` | SMTP server address | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP server port | `587` |
| `MAIL_USERNAME` | Email address used to send outgoing mail | `your-email@gmail.com` |
| `MAIL_PASSWORD` | App-specific password for the SMTP account | `our-app-password` |

### 🌐 Frontend Configuration

| Variable | Description | Default Fallback |
| :--- | :--- | :--- |
| `APP_URL` | Base URL of the Angular client (used for password reset links) | `http://localhost:4200` |

<br/>

## 🚀 Running the Application

### 1️⃣ Local Development
If you are running the infrastructure (Postgres) locally via Docker Compose:

```bash
# Build the application
./mvnw clean package -DskipTests

# Run the Spring Boot app on port 8081
java -jar target/micro-chat-user-service-0.0.1-SNAPSHOT.jar
```

### 2️⃣ Kubernetes Deployment (Minikube / Production)

This service is orchestrated via Kubernetes. Build the local image and apply the manifests found in the central ecosystem repository:
```Bash

# Build the Docker image locally
docker build -t user-service:latest .

# Apply the Kubernetes manifest
kubectl apply -f k8s/03-user-service.yaml
```
### 📚 API Documentation (Swagger)

This service utilizes springdoc-openapi to expose its REST contracts.

When running within the full ecosystem, the Swagger UI for this microservice is centrally aggregated and accessible via the API Gateway:

    🔗 Gateway Swagger UI: http://api.microchat.local/swagger-ui.html

    (Select "User Service" from the top-right dropdown menu).

If running in isolation, the raw OpenAPI JSON spec can be accessed at:

http://localhost:8081/v3/api-docs