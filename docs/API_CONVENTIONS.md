# API Conventions

- Base path: `/api/<module>/...` (pluralized module names).
  - Examples: `/api/users/profile`, `/api/events/calendar`, `/api/campings/search`.
- Versioning will be handled via headers when needed; no URL versioning yet.
- Error format (emitted by `GlobalExceptionHandler`):
  ```json
  {
  	"code": "UNEXPECTED_ERROR",
  	"message": "Human readable message",
  	"status": 500
  }
  ```
- Validation errors should return HTTP 400 with a descriptive `code` (e.g., `VALIDATION_ERROR`).
- All controllers reside under their module-specific package; cross-module calls go through shared DTOs or service interfaces only.
- Use standard HTTP verbs:
  - GET for reads
  - POST for creation/commands
  - PUT/PATCH for updates
  - DELETE for removals
