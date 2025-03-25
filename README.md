# MindPath
## Bachelor's Thesis Project

MindPath implements the Model-View-Controller (MVC) software design pattern.

## Tech Stack
- Java 21 and Spring Boot 3 application
- Java build tools: Maven
- Database: PostgreSQL (optional: H2 for development and testing)
- JUnit and Mockito for testing

  ### Dependencies
    - Spring Web: REST APIs
    - Spring Boot DevTools: enhanced configurations
    - Spring Data JPA: database interaction
    - Spring Security: authentication and access-control
    - Spring Validation: data validation
    - PostgreSQL Driver/H2 Database: database connectivity
    - Docker Compose: containerization
    - Lombok: reduce boilerplate code

## Authentication Flow
- Only users with ADMIN role can create teachers (/teachers).
- Any user can register as a STUDENT on the platform (/auth/register).
- Any user can log in (/auth/login).
- Users with STUDENT or TEACHER roles can change their password (/auth/change-password).

## APIs
### Users

| Method | Endpoint       | Access           | Description                  |
|--------|----------------|------------------|------------------------------|
| GET    | /users/me      | Authenticated    | Get current user profile     |
| PATCH  | /auth/change-password | Student, Teacher | Change password              |
| POST   | /auth/register | Public           | Register as a student        |
| POST   | /auth/login    | Public           | Log in and get JWT token     |
| GET    | /users/{id}    | Teacher, Admin   | Get user profile by id |

### Teachers

| Method | Endpoint  | Access | Description      |
|--------|-----------|--------|------------------|
| POST   | /teachers | Admin  | Create a teacher |

### Quizzes

| Method | Endpoint | Access | Description |
|-------|----------|--------|-------------|
| POST  | /quizzes | Teacher | Create a quiz |
| GET   | /quizzes | Authenticated | Get list of all quizzes |
| GET   | /quizzes/active | Authenticated | Get active quizzes |
| GET   | /quizzes/{id}   | Authenticated | Get quiz by id |
| PATCH | /quizzes/{id} | Teacher | Update quiz |

### Questions

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST   | /quizzes/{quizId}/questions | Teacher | Add question to a quiz |
| GET    | /quizzes/{quizId}/questions | Authenticated | Get questions for a quiz |
| PUT/PATCH | /questions/{id} | Teacher | Edit a question |
| DELETE | /questions/{id}    | Teacher | Delete a question |

### Answers

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST   | /questions/{questionId}/answers | Teacher | Add answer options to a question |
| GET    | /questions/{questionId}/answers | Authenticated | Get answers for a question |
