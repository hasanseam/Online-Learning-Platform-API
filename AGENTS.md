# Agent Guide - LearnEinBisschenGerman

Welcome, Agent! This document provides the essential context, conventions, and decision rules for contributing to the **LearnEinBisschenGerman** project.

---

## ⚠️ Environment Notes (Read First)

This project runs on **bleeding-edge versions** of Java and Spring Boot. Do not rely on assumptions from older versions.

| Component | Version | Key Implication |
|---|---|---|
| Java | 25 | Records, Pattern Matching, and modern language features are idiomatic — use them |
| Spring Boot | 4.0.1 | Uses **Jakarta EE 11** (not 10). All imports use `jakarta.*`, never `javax.*` |
| Spring Security | 6.x+ | Lambda DSL is mandatory; deprecated `WebSecurityConfigurerAdapter` does not exist |

---

## 🚀 Project Overview

**LearnEinBisschenGerman** is a backend REST API for an Online Language Learning Platform. It handles:
- Course and lesson management
- HLS video streaming via Cloudflare R2
- User progress tracking
- JWT-based authentication

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.0.1 |
| Persistence | PostgreSQL + Spring Data JPA |
| Security | Spring Security + JWT (jjwt) |
| Storage | Cloudflare R2 (S3-compatible) via AWS SDK |
| API Docs | SpringDoc OpenAPI (Swagger UI at `/swagger-ui.html`) |
| Rate Limiting | Bucket4j |
| Boilerplate | Lombok |

---

## 📂 Project Structure

All source code lives in `src/main/java/com/hasanur/learneinbisschengerman/`:

```
auth/         JWT authentication, JwtService, SecurityConfig, login/registration
config/       R2Config, app-wide configuration, AppProperties
course/       Course entity, repository, service, DTOs, controller
lesson/       Lesson entity, repository, service, DTOs, controller
video/        Video processing and HLS streaming logic
progress/     User learning progress and statistics
user/         User profiles and management
exceptions/   GlobalExceptionHandler (@RestControllerAdvice), ResourceNotFoundException
```

---

## 📐 Architecture & Patterns

### Layered Architecture

Every feature follows this strict layer order. Do not skip layers or cross-cut them.

```
Controller  →  Service  →  Repository  →  Database
    ↓               ↓
  DTOs          Entities
```

| Layer | Responsibility | Key Rules |
|---|---|---|
| **Controller** | REST endpoints, request validation, HTTP mapping | Use `@Valid` on request bodies. Return DTOs, never entities. |
| **Service** | Business logic, transactions, entity↔DTO mapping | All write operations must be `@Transactional`. Read-only queries should use `@Transactional(readOnly = true)`. |
| **Repository** | Data access | Extend `JpaRepository`. Add custom queries via JPQL (`@Query`) or derived method names. |
| **DTO** | Data transfer | Implemented as **Java Records**. One record per use case (e.g., `CourseRequest`, `CourseResponse`). |

### When to Create a New DTO

- **Always** create a new record — never expose JPA entities directly in controller responses.
- Create separate request and response records (e.g., `CreateCourseRequest`, `CourseResponse`).
- Reuse an existing response record only if the field set is identical. When in doubt, create a new one.
- Naming convention: `{Entity}{Action}Request` / `{Entity}Response` (e.g., `UpdateLessonRequest`, `LessonResponse`).

### DTO Mapping Pattern

Mapping is done **manually** inside the Service layer using a private `mapToDto` method. Do not introduce MapStruct or other mapping libraries without discussion.

```java
// Example pattern — follow this in every service
private CourseResponse mapToDto(Course course) {
    return new CourseResponse(
        course.getId(),
        course.getTitle(),
        course.getDescription(),
        course.getCreatedAt()
    );
}
```

---

## 🔐 Security Reference

### Endpoint Access Rules

| Pattern | Access |
|---|---|
| `POST /api/auth/**` | Public (no token required) |
| `GET /api/courses/**` | Public |
| `GET /api/lessons/**` | Public |
| `POST /api/courses/**` | Authenticated (any role) |
| `PUT /api/courses/**` | Authenticated (any role) |
| `DELETE /api/courses/**` | `ROLE_ADMIN` only |
| `GET /api/progress/**` | Authenticated (own data only) |
| All other `/api/**` | Authenticated |

> When adding a new controller, explicitly decide whether its endpoints are public or protected and configure `SecurityConfig.java` accordingly. Never leave a new endpoint in an ambiguous state.

### Roles

| Role | Description |
|---|---|
| `ROLE_USER` | Standard learner |
| `ROLE_ADMIN` | Full platform management |

### Key Files

- `auth/SecurityConfig.java` — HTTP security rules (lambda DSL)
- `auth/JwtService.java` — Token generation and validation
- `auth/JwtAuthenticationFilter.java` — Request filter chain

---

## 🪣 Storage & Media (Cloudflare R2)

### R2 Bucket Key Structure

All objects follow this path convention:

```
courses/{courseId}/lessons/{lessonId}/videos/{filename}.m3u8
courses/{courseId}/lessons/{lessonId}/videos/segments/{segment}.ts
courses/{courseId}/lessons/{lessonId}/assets/{filename}
```

When adding a new media type, follow this hierarchy and document the path pattern here.

### HLS Streaming

- Videos are segmented and stored as `.m3u8` playlists + `.ts` segment files.
- The `video/` package owns all HLS processing and streaming logic.
- Presigned URLs are used for secure, time-limited media access.

### Key Files

- `config/R2Config.java` — S3 client setup for Cloudflare R2
- `video/VideoService.java` — Upload, segment, and streaming URL generation

---

## ❌ Error Handling

### API Error Response Shape

All errors return a consistent JSON body:

```json
{
  "timestamp": "2025-01-01T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Course with id 42 not found",
  "path": "/api/courses/42"
}
```

### When to Throw What

| Situation | Exception to Throw |
|---|---|
| Entity not found by ID | `ResourceNotFoundException` |
| Invalid request state / business rule violation | `IllegalStateException` or a custom domain exception |
| Unauthorized access to a resource | `AccessDeniedException` (Spring Security handles HTTP mapping) |
| Duplicate / conflict | `DataIntegrityViolationException` (let it propagate to handler) |

All exceptions are caught and mapped to HTTP responses in `exceptions/GlobalExceptionHandler.java`. Do not return error details manually from controllers.

---

## ✅ Testing Expectations

All new features **must** include tests. Follow this baseline:

| Test Type | Scope | Location |
|---|---|---|
| Unit tests | Service layer logic and `mapToDto` methods | `src/test/java/.../service/` |
| Integration tests | Controller endpoints (use `@SpringBootTest` + `MockMvc`) | `src/test/java/.../controller/` |
| Repository tests | Custom `@Query` methods only | `src/test/java/.../repository/` |

- Mock repositories in service unit tests using Mockito (`@Mock`, `@InjectMocks`).
- Do not test trivial getters, Lombok-generated code, or Spring autowiring.

---

## 📝 Adding a New Feature — Checklist

Follow this order for every new feature:

1. **Entity** — define the JPA entity with Lombok (`@Data`, `@Builder`, etc.)
2. **Repository** — extend `JpaRepository<Entity, Long>`
3. **DTOs** — create request and response Java Records with `@Valid` constraints
4. **Service** — implement business logic with `@Transactional`, include `mapToDto`
5. **Controller** — expose endpoints, use `@Valid` on `@RequestBody`, return DTOs
6. **Security** — update `SecurityConfig.java` to define access rules for new endpoints
7. **Tests** — add unit tests for service, integration tests for controller
8. **Docs** — Swagger annotations are auto-generated via SpringDoc; verify at `/swagger-ui.html`

---

## 🚦 Getting Started

1. Ensure PostgreSQL is running locally.
2. Configure `src/main/resources/application.yml` with:
   - R2 bucket name, access key, secret key, and endpoint
   - PostgreSQL datasource URL, username, and password
   - JWT secret and expiry settings
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Access API documentation at: `http://localhost:8080/swagger-ui.html`
5. Default admin credentials (dev only) are defined in `application.yml` under `app.admin.*`.