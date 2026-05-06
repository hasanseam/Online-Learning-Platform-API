# Agent Guide - LearnEinBisschenGerman

Welcome, Agent! This document provides the essential context and guidelines for contributing to the **LearnEinBisschenGerman** project.

## 🚀 Project Overview
**LearnEinBisschenGerman** is a robust, modern backend API for an Online Language Learning Platform. It facilitates course management, lesson delivery (including HLS video streaming), user progress tracking, and secure authentication.

## 🛠 Tech Stack
- **Language:** Java 25 (Modern features like Records and Pattern Matching are heavily used)
- **Framework:** Spring Boot 4.0.1 (Bleeding edge environment)
- **Persistence:** PostgreSQL with Spring Data JPA
- **Security:** Spring Security with JWT (jjwt)
- **Storage:** Cloudflare R2 (S3-compatible) via AWS SDK
- **Documentation:** SpringDoc OpenAPI (Swagger UI)
- **Rate Limiting:** Bucket4j
- **Utilities:** Lombok for boilerplate reduction

## 📂 Project Structure
All source code is located in `src/main/java/com/hasanur/learneinbisschengerman/`:

- `auth/`: JWT authentication, security configuration, and user login/registration logic.
- `config/`: Application-wide configurations (S3/R2, Security, App properties).
- `course/`: Course entities, repositories, and management services.
- `lesson/`: Lesson content, including metadata and structure.
- `video/`: Video processing and streaming logic (HLS support).
- `progress/`: User learning progress and statistics tracking.
- `user/`: User profiles and management.
- `exceptions/`: Centralized error handling and custom exceptions (e.g., `ResourceNotFoundException`).

## 📐 Key Patterns & Standards
1. **Layered Architecture:**
   - **Controller:** REST endpoints, request validation, and mapping to DTOs.
   - **Service:** Core business logic, transaction management (`@Transactional`), and manual mapping to DTOs.
   - **Repository:** Data access layer using Spring Data JPA.
   - **DTO (Data Transfer Object):** Implemented using **Java Records**. Used for all API requests and responses to avoid exposing entities directly.

2. **Coding Style:**
   - **Lombok:** Use `@Data`, `@Getter`, `@Setter`, `@Builder`, and constructors to keep entities clean.
   - **Validation:** Use `@Valid` and Jakarta Validation constraints on DTO records.
   - **Error Handling:** Centralized via `@RestControllerAdvice`.
   - **Mapping:** Mapping from Entity to DTO is currently handled manually within the Service layer (`mapToDto` methods).

3. **Storage & Media:**
   - Videos and assets are stored in Cloudflare R2.
   - Media delivery supports HLS (HTTP Live Streaming) patterns.

## 📝 Common Tasks
- **Adding a New Feature:** Define the Entity -> Create Repository -> Implement Service (with `mapToDto`) -> Create DTO Records -> Expose via Controller.
- **Updating Security:** Check `SecurityConfig.java` and `JwtService.java` in the `auth` package.
- **Modifying Storage Logic:** See `R2Config.java` and related service classes.

## 🚦 Getting Started
1. Ensure PostgreSQL is running.
2. Configure `application.yml` with R2 credentials and DB details.
3. Run with `./mvnw spring-boot:run`.
4. Access API docs at `/swagger-ui.html`.

#DO'S
1. write always test code in separted test folder for example for user write it in src/test/java/com/hasanur/learneinbisschengerman/user/ and for course write it in src/test/java/com/hasanur/learneinbisschengerman/course/

---
*Note: This project uses futuristic versions of Java and Spring Boot. Ensure your environment is compatible with Java 25.*
