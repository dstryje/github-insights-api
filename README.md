# GitHub Insights API

A Spring Boot REST API that retrieves public GitHub user profile information and associated repositories and returns a consolidated, structured response.

This project demonstrates practical enterprise Java development with an emphasis on clean architecture, separation of concerns, external API integration, automated testing, and maintainability.

---

## Overview

The service accepts a GitHub username, retrieves profile information from the GitHub public API, retrieves the user's repositories, and returns the information through a simplified API contract.

Example endpoint:

GET /api/v1/github/{username}

Example:

http://localhost:8080/api/v1/github/octocat

---

## Architecture

The application uses a layered architecture to keep responsibilities separated and make the code easier to maintain and test.

Controller → Service → Client → Mapper → DTO

Cross-cutting concerns such as configuration and exception handling are kept separate from the primary application flow.

### Controller Layer

Handles incoming HTTP requests and delegates business operations to the service layer.

GithubUserController

The controller is intentionally kept thin and does not contain external API or transformation logic.

### Service Layer

Coordinates the application workflow, including retrieving GitHub profile and repository information and delegating response transformation.

GithubUserService

### Client Layer

Provides an abstraction around communication with the external GitHub API.

GithubClient  
GithubClientImpl

The implementation uses Spring's RestClient to communicate with GitHub.

External API communication is isolated behind the client interface so that the rest of the application does not depend directly on GitHub's implementation details.

### Configuration

RestClient configuration is centralized in:

RestClientConfig

This keeps external service configuration separate from business logic and provides a single location for configuring GitHub API communication.

### Mapper Layer

Transforms external GitHub DTOs into the application's public API response model.

GithubMapper

Keeping transformation logic separate prevents the service and controller layers from becoming responsible for response formatting.

### DTO Separation

Separate DTOs are maintained for:

- GitHub external API responses
- Internal API response contracts

This prevents the public API from becoming tightly coupled to GitHub's response structure and allows either side to evolve independently.

### Exception Handling

Centralized exception handling provides consistent API error responses.

Current exception components include:

GithubUserNotFoundException  
GithubApiException  
GlobalExceptionHandler  
ErrorResponse

Example:

{
"message": "User not found: invalid-user"
}

---

## Technology Stack

- Java 17
- Spring Boot 3
- Spring Web
- Spring RestClient
- Spring Validation
- Spring Actuator
- Spring Cache
- Gradle
- JUnit 5
- Mockito
- Lombok
- Jackson

---

## Running the Application

### IntelliJ IDEA

Run the Spring Boot application using:

GithubInsightsApiApplication.java

### Gradle

Windows:

./gradlew.bat clean build

macOS/Linux:

./gradlew clean build

Then start the application using IntelliJ or Gradle.

---

## Using the API

Once the application is running locally:

GET /api/v1/github/{username}

Example:

GET /api/v1/github/octocat

The API can be called using Postman, curl, a browser, or another HTTP client.

No authentication is currently required when calling the local API.

The application handles the required GitHub API headers when communicating with GitHub.

---

## Example Successful Response

{
"user_name": "octocat",
"display_name": "The Octocat",
"avatar": "https://avatars.githubusercontent.com/u/583231?v=4",
"geo_location": "San Francisco",
"email": null,
"url": "https://api.github.com/users/octocat",
"created_at": "Tue, 25 Jan 2011 18:44:36 GMT",
"repos": [
{
"name": "Hello-World",
"url": "https://api.github.com/repos/octocat/Hello-World"
}
]
}

---

## Example Error Response

If the requested GitHub user does not exist:

{
"message": "User not found: invalid-user"
}

---

## Testing Strategy

The application contains tests across the primary application layers.

### Mapper Tests

Validate DTO transformation and response formatting.

GithubMapperTest

### Service Tests

Validate service orchestration and dependency interactions using Mockito.

GithubUserServiceTest

### Controller Tests

Validate:

- Endpoint behavior
- HTTP status codes
- JSON serialization
- Service delegation

using Spring MockMvc.

GithubUserControllerTest

The testing strategy will continue to expand as additional production capabilities are introduced.

---

## Engineering Goals

The project is being developed as a production-style Spring Boot service rather than simply an API integration example.

The primary engineering goals are:

- Clear separation of responsibilities
- Maintainable application architecture
- Testable components
- Resilient external API communication
- Consistent error handling
- Production observability
- Automated build and testing
- Containerized deployment
- Clear architectural documentation

---

## Planned Enhancements

The next phases of the project will introduce additional production-oriented capabilities, including:

- Response caching
- GitHub API rate-limit handling
- Retry and resilience strategies for transient failures
- Improved integration testing
- Docker containerization
- CI/CD pipeline
- Enhanced health checks and observability
- Structured logging
- API documentation
- Security considerations
- Architecture documentation

Additional capabilities will be introduced where they provide a practical architectural benefit rather than simply adding technologies to the project.

---

## Author

Developed by Daniel Stryjewski.

This project demonstrates my approach to designing and building maintainable enterprise Java applications, including API design, integration architecture, testing, resilience, and production-readiness.