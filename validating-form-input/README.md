# Validating Form Input - Spring Boot

A full-featured Spring Boot form validation application demonstrating enterprise-grade patterns.

## Features

- **Bean Validation** - `@NotNull`, `@Size`, `@Min`, `@Max`, `@Email`, `@Pattern` with custom messages
- **Custom Validators** - `@ValidPhone` annotation with regex-based `PhoneValidator`, `@PasswordMatch` class-level cross-field validator
- **In-Memory CRUD** - Thread-safe `ConcurrentHashMap`-backed service with full Create/Read/Update/Delete
- **Dashboard** - Stats cards, age distribution chart, recent registrations
- **Submissions List** - Search/filter, sortable table, inline edit/delete
- **REST Validation API** - Real-time async field validation via `GET /api/validate/{field}`
- **AJAX Feedback** - Debounced client-side validation with live status indicators
- **Password Strength Meter** - Real-time visual feedback (weak/fair/good/strong)
- **Dark Mode** - Toggle with `localStorage` persistence
- **CSV Export** - Download all registrations as CSV
- **Rate Limiting** - Sliding window rate limiter (60 POST requests/min per IP)
- **Audit Trail** - SLF4J-based request logging for POST/DELETE operations
- **Global Exception Handling** - `@ControllerAdvice` with custom 404/500 error pages
- **i18n Messages** - Externalized `ValidationMessages.properties`
- **34 Unit Tests** - Full coverage for validators and service layer

## Tech Stack

- Java 21, Spring Boot 3.3.0, Maven
- Thymeleaf, Jakarta Bean Validation (Hibernate Validator)
- Pure CSS (no frameworks), vanilla JavaScript

## Run

```bash
./mvnw spring-boot:run
```

Open http://localhost:8080

## Test

```bash
./mvnw test
```
