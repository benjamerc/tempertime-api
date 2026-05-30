# Tempertime API

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1)
![JWT](https://img.shields.io/badge/Auth-JWT-black)
![OpenAPI](https://img.shields.io/badge/OpenAPI-3.0.1-6BA539)
![Tests](https://img.shields.io/badge/Tests-531-success)
![License](https://img.shields.io/badge/License-MIT-blue)

---

## Project Overview

REST API for collaborative event and workspace management, built with Java and Spring Boot.

Tempertime allows users to create workspaces, manage group events, assign participants, and securely handle authentication and authorization using JWT-based security.

---

## Features

- JWT-based authentication with refresh token rotation
- Role-based workspace access control
- Workspace invitation system using secure invite codes
- Global and user-specific event management
- Event filtering with timezone-aware date handling
- Layered architecture using domain-oriented feature organization
- Comprehensive testing strategy with 531 automated tests
- RESTful API design with validation and global exception handling

---

## Tech Stack

### Backend
- Java 17
- Spring Boot 3.5
- Spring Security
- Spring Data JPA
- Hibernate

### Database
- MySQL 8
- H2 (testing)

### Authentication & Security
- JWT Authentication
- Spring Security
- BCrypt password hashing

### Testing
- JUnit 5
- Mockito
- MockMvc
- TestRestTemplate

### Build Tools
- Maven
- MapStruct
- Lombok

---

## Architecture

The backend follows a layered architecture with clear separation of responsibilities and domain-oriented feature organization.

```text
External Client
       ↓
Controller Layer
       ↓
Service Layer
       ↓
Repository Layer
```

The project is organized by feature domains rather than technical layers, improving modularity and maintainability.

| Domain     | Responsibility                                                 |
|------------|----------------------------------------------------------------|
| auth       | Authentication workflows and user session management           |
| users      | User profile and account management                            |
| workspaces | Workspace management, memberships and invitation codes         |
| events     | Event management, assignments and filtering                    |
| security   | Security infrastructure (JWT, Spring Security, access control) |
| common     | Shared utilities, validation and reusable components           |
| config     | Application configuration (CORS, OpenAPI setup)                |

---

## Security

- JWT-based authentication and authorization
- Refresh token rotation with automatic token revocation
- SHA-256 hashing for refresh token persistence
- BCrypt hashing for user password persistence
- Dual-storage strategy for workspace invitation codes using SHA-256 hashing for validation and AES/GCM encryption for secure owner retrieval
- Role-based access control for workspace operations
- DTO validation and business rule enforcement
- Database-level integrity constraints for data consistency and uniqueness enforcement

---

## Testing

The project includes a multi-layered testing strategy covering persistence, business logic, controllers, and end-to-end flows.

| Layer            | Tools                              | Purpose                       |
|------------------|------------------------------------|-------------------------------|
| Repository Tests | @DataJpaTest + H2                  | Database interaction testing  |
| Service Tests    | JUnit 5 + Mockito                  | Business logic isolation      |
| Controller Tests | @WebMvcTest + MockMvc              | API endpoint validation       |
| E2E Tests        | @SpringBootTest + TestRestTemplate | Full application flow testing |

### Metrics

- 518 unit and layered integration tests
- 13 end-to-end tests
- 531 total automated tests

### Running Tests

```bash
mvn test
mvn verify
```

---

## API Documentation

The API exposes 32 RESTful endpoints covering authentication, workspace management, event handling, user management, and invitation workflows.

Key capabilities:
- Authentication & authorization (JWT-based security and access control)
- Request/response schemas with examples
- Input validation and business rules
- Structured error handling with HTTP status codes and custom error codes
- Pagination, sorting, and filtering mechanisms
- Timezone-aware event querying (ISO 8601)

### Interactive Documentation (Swagger UI)

- Available at `/swagger-ui/index.html` when running locally.

### Manual Documentation (Spanish)

- API reference (Google Docs):  
  https://docs.google.com/document/d/1R9-2Gw6VUBjBQMVe5Jg1e5ZjyboWMcSxQgum1Riyetc/edit?usp=sharing

- PDF version:  
  https://drive.google.com/file/d/10X_37pL1V-T367wsqL-Zi9xBNV8k4Lup/view?usp=sharing

---

## Environment Variables

A template file is provided:

```bash
.env.example
```

| Variable                         | Description                                     |
|----------------------------------|-------------------------------------------------|
| ALLOWED_ORIGINS                  | Allowed frontend origins for CORS configuration |
| DB_URL                           | Database connection URL                         |
| DB_USERNAME                      | Database user                                   |
| DB_PASSWORD                      | Database password                               |
| JWT_SECRET_KEY                   | JWT signing key                                 |
| JWT_EXPIRATION                   | Access token expiration (ms)                    |
| JWT_REFRESH_EXPIRATION           | Refresh token expiration (ms)                   |
| WORKSPACE_INVITE_CODE_SECRET_KEY | AES key for invitation codes                    |

---

## Installation & Running

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8+

### 1. Clone repository
```bash
git clone https://github.com/benjamerc/tempertime-api.git
cd tempertime-api
```

### 2. Configure environment
Create `.env` from `.env.example`

### 3. Create database
```sql
CREATE DATABASE tempertime_db;
```

### 4. Run application
```bash
mvn spring-boot:run
```

App runs on:
```
http://localhost:8080
```

---

## Author

Developed by Benjamín Merchán

GitHub: https://github.com/benjamerc  
Email: benjaminmerchan1@gmail.com

---

## License

This project is licensed under the MIT License.

This project was developed for educational and portfolio purposes.