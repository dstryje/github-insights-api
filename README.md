# GitHub Insights API

GitHub Insights API is a Spring Boot REST service that retrieves and aggregates public GitHub developer and repository data into a simplified application-owned API contract.

The project started as a focused GitHub integration service and is being expanded into a production-oriented example of how to design, test, operate, and evolve a Java/Spring Boot application that depends on external APIs.

---

## Overview

The service accepts a GitHub username, retrieves profile information from GitHub's public API, retrieves the user's repositories, combines the results, and returns a consolidated JSON response.

Current endpoint:

```http
GET /api/v1/github/{username}
```

Example:

```http
GET http://localhost:8080/api/v1/github/octocat
```

The service currently integrates with:

```text
GET https://api.github.com/users/{username}

GET https://api.github.com/users/{username}/repos
```

No GitHub token is required for basic usage, although unauthenticated requests are subject to GitHub API rate limits.

---

## The Problem

GitHub provides extensive developer and repository information through its REST APIs, but related information is distributed across multiple endpoints and represented using GitHub-specific response models.

A consumer that wants a consolidated view of a developer must:

1. Retrieve the user's profile.
2. Retrieve the user's repositories.
3. Understand two separate external API contracts.
4. Combine the results.
5. Handle differences in field names and data formatting.
6. Handle failures and rate limits from an external dependency.

For example, a developer profile requires data from both:

```http
GET https://api.github.com/users/{username}
GET https://api.github.com/users/{username}/repos
```

GitHub Insights API provides an application layer over those APIs.

Instead of exposing GitHub's external models directly, the service retrieves the required data, transforms it into an application-owned model, and returns a single response designed for consumers of this service.

The goal is not simply to proxy GitHub.

The project is intended to demonstrate how an external API integration can be structured as a maintainable, testable, and production-oriented Spring Boot application while leaving room for additional developer and repository analytics.

---

## Current Capabilities

The application currently supports:

- GitHub user lookup by username
- GitHub profile retrieval
- GitHub repository retrieval
- Aggregation of multiple GitHub API responses
- Transformation between external GitHub DTOs and the public API model
- Date formatting
- Centralized exception handling
- Consistent error responses
- Unit and controller-level testing
- Spring Boot health and operational support through Actuator
- Spring Cache infrastructure for future caching support

---

## Example Successful Response

```json
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
```

The public response intentionally differs from GitHub's native API models.

For example:

```text
GitHub API              GitHub Insights API
------------------------------------------------
login                   user_name
name                    display_name
avatar_url              avatar
location                geo_location
created_at              created_at
```

This transformation keeps the public API independent from GitHub's response structure.

---

## Architecture

The application follows a layered architecture with clear separation between HTTP concerns, orchestration, external integrations, transformation, and error handling.

```text
                   Client
                     |
                     v
            GithubUserController
                     |
                     v
             GithubUserService
                     |
                     v
                GithubClient
                     |
          +----------+----------+
          |                     |
          v                     v
 GitHub User API       GitHub Repositories API
          |                     |
          +----------+----------+
                     |
                     v
                GithubMapper
                     |
                     v
              Response DTO
                     |
                     v
                   Client
```

At a high level:

```text
Controller
    |
    v
Service
    |
    v
Client
    |
    v
External GitHub APIs
    |
    v
Mapper
    |
    v
Application DTO
```

---

## Architecture and Design Decisions

### Controller Layer

`GithubUserController`

The controller owns the HTTP-facing contract.

Its responsibility is limited to:

- accepting requests
- validating HTTP inputs where appropriate
- delegating application work
- returning the resulting HTTP response

Business and GitHub integration logic are intentionally kept out of the controller.

### Service Layer

`GithubUserService`

The service coordinates the application workflow.

For a user lookup, it orchestrates the calls required to retrieve:

- GitHub user data
- GitHub repository data

It then delegates transformation to the mapper.

Keeping orchestration in the service prevents the controller from becoming responsible for application behavior and prevents the integration client from taking on business responsibilities.

### Client Layer

`GithubClient`

`GithubClientImpl`

The client layer encapsulates communication with GitHub.

The application uses Spring's `RestClient` for synchronous HTTP communication with the GitHub REST API.

The current workflow requires a small number of straightforward request/response calls, so a synchronous client keeps the implementation simple and readable.

For a significantly different workload, alternatives such as `WebClient`, asynchronous processing, or declarative clients could be evaluated based on actual requirements.

### RestClient Configuration

`RestClientConfig`

GitHub API configuration is centralized rather than duplicated throughout the application.

This provides a single location for concerns such as:

- GitHub base URL
- required headers
- client configuration
- future timeout configuration
- authentication if a GitHub token is added later

### DTO Separation

The application maintains separate models for:

- GitHub external API responses
- GitHub Insights public API responses

This separation is intentional.

GitHub owns its API contract. GitHub Insights owns its public API contract.

Allowing GitHub DTOs to flow directly through the service would tightly couple consumers to an external provider's model.

Separating them provides a clear anti-corruption boundary between the external system and the application.

### Mapper Layer

`GithubMapper`

The mapper transforms GitHub API responses into the public API contract.

Responsibilities include:

- mapping field names
- combining profile and repository data
- converting repository objects
- formatting account creation dates

Keeping transformation logic in a dedicated mapper allows the service to remain focused on orchestration.

### Date Formatting

GitHub returns its account creation timestamp using its API representation.

GitHub Insights currently converts the value into a readable GMT representation such as:

```text
Tue, 25 Jan 2011 18:44:36 GMT
```

Transformation is performed as part of mapping rather than modifying the external GitHub DTO.

---

## Exception Handling

The application uses centralized exception handling to produce consistent API responses.

Current components include:

```text
GithubUserNotFoundException
GithubApiException
GlobalExceptionHandler
ErrorResponse
```

A missing GitHub user produces a structured response rather than exposing an internal exception.

Example:

```json
{
  "message": "User not found: invalid-user"
}
```

Centralized exception handling prevents HTTP error translation from being duplicated throughout controllers and services.

It also provides a natural location for future improvements such as:

- error codes
- correlation IDs
- timestamps
- upstream status information
- structured logging

---

## External Dependency Considerations

Because GitHub Insights depends on an external service, failures must be treated differently from failures entirely within the application.

### User Not Found

A GitHub `404` for the requested user should be treated as a known business/API condition rather than an internal server failure.

### GitHub Service Failure

Unexpected upstream failures should be translated into a controlled application error instead of exposing GitHub implementation details directly to the caller.

### Timeouts

External calls should have bounded connection and response times so unavailable dependencies cannot indefinitely consume application resources.

Timeout configuration is an area planned for further hardening.

### Retry Behavior

Retries should only be applied to transient conditions where retrying is safe.

Potential examples include:

- temporary network failures
- selected `5xx` responses
- connection timeouts

Failures such as `404` or invalid requests should not be retried.

Any retry implementation should use bounded attempts and backoff rather than retrying indefinitely.

---

## Rate Limiting

GitHub limits the number of requests that clients can make within a period of time.

Because one GitHub Insights request may require multiple GitHub API calls, rate limits are an important operational concern.

Future improvements will evaluate:

- GitHub rate-limit response headers
- remaining request quota
- reset time
- application metrics for rate-limit usage
- controlled handling of `403` / `429` responses
- authenticated GitHub requests where appropriate
- caching to reduce unnecessary repeated requests

---

## Caching Strategy

Caching is a natural fit for repeated GitHub profile lookups because public profile and repository information does not generally require transaction-level consistency.

The initial strategy will likely use the GitHub username as the cache key.

```text
Request
   |
   v
Check Cache
   |
   +--- Hit ---> Return Cached Response
   |
   +--- Miss
          |
          v
      GitHub API
          |
          v
     Build Response
          |
          v
       Cache
          |
          v
        Return
```

A short TTL can reduce:

- GitHub API traffic
- response latency
- exposure to rate limits

while limiting how long stale GitHub data can remain visible.

For a single application instance, an in-memory cache may be sufficient.

For multiple application instances, a distributed cache such as Redis could provide a shared cache across instances.

Caching should only be introduced with explicit expiration and invalidation behavior.

---

## Repository Pagination

GitHub's repository API is paginated.

The current implementation is appropriate for the original scope, but a production-oriented implementation needs an explicit strategy for repositories that span multiple pages.

Possible approaches include:

1. Retrieve all repository pages internally.
2. Expose repository pagination through GitHub Insights.
3. Limit repository retrieval based on the needs of specific insight endpoints.

The correct approach depends on API requirements, response size, performance expectations, and GitHub rate-limit impact.

Pagination support is planned as the repository-analysis features expand.

---

## API Contract

### Get GitHub User

```http
GET /api/v1/github/{username}
```

Example:

```http
GET /api/v1/github/octocat
```

The endpoint retrieves profile and repository information and returns a consolidated response.

---

## Testing Strategy

Tests are organized around application responsibilities rather than relying entirely on end-to-end tests.

### Mapper Tests

`GithubMapperTest`

Validates:

- DTO transformation
- field mapping
- repository transformation
- date formatting
- final response construction

### Service Tests

`GithubUserServiceTest`

Validates:

- orchestration behavior
- interaction with the GitHub client
- interaction with the mapper
- successful response construction

Mockito is used to isolate dependencies where appropriate.

### Controller Tests

`GithubUserControllerTest`

Uses Spring MVC testing to validate:

- endpoint behavior
- HTTP status codes
- JSON serialization
- service delegation

### Planned Integration Tests

A future integration-testing layer can use a controlled mock HTTP server such as WireMock to exercise the application from the HTTP boundary through the GitHub integration layer.

This would allow validation of:

- successful GitHub responses
- GitHub `404` responses
- GitHub `5xx` responses
- timeouts
- malformed responses
- rate-limit responses
- pagination

without depending on GitHub itself during automated tests.

---

## Observability

The application includes Spring Boot Actuator as the foundation for operational visibility.

The observability strategy will evolve to include metrics such as:

- application request count
- application request latency
- GitHub API request count
- GitHub API latency
- GitHub API failures
- cache hits and misses
- rate-limit events

Structured logging will also be introduced so application activity can be correlated across requests and external calls.

In a distributed environment, tracing could be added to follow a request from the API boundary through downstream integrations.

---

## Security

The current API uses public GitHub information and does not require application authentication.

If the service evolves into a multi-user or protected platform, security would be added at the application boundary rather than embedded throughout the business logic.

Potential future capabilities include:

- Spring Security
- token-based authentication
- authorization
- secret management
- secure GitHub token handling
- request validation
- security headers

Sensitive tokens or credentials must never be committed to source control or included in application logs.

---

## Technology Stack

Current technologies include:

- Java 17
- Spring Boot 3.5
- Spring Web
- Spring `RestClient`
- Spring Validation
- Spring Cache
- Spring Boot Actuator
- Gradle
- Lombok
- Jackson
- JUnit 5
- Mockito
- MockMvc

---

## Running the Application

### Requirements

- Java 17+
- Internet connectivity to GitHub's public API

### Run with IntelliJ IDEA

Run:

```text
GithubInsightsApiApplication
```

from IntelliJ.

### Run with Gradle

macOS/Linux:

```bash
./gradlew bootRun
```

Windows:

```powershell
.\gradlew bootRun
```

### Build the Application

macOS/Linux:

```bash
./gradlew clean build
```

Windows:

```powershell
.\gradlew clean build
```

---

## Testing the API

Once the application is running:

```http
GET http://localhost:8080/api/v1/github/octocat
```

The endpoint can be called using:

- browser
- Postman
- curl
- another HTTP client

Example using curl:

```bash
curl http://localhost:8080/api/v1/github/octocat
```

No headers or authentication are currently required when calling the local API.

The application handles the GitHub-specific outbound configuration internally.

---

## Running Tests

macOS/Linux:

```bash
./gradlew test
```

Windows:

```powershell
.\gradlew test
```

---

## Production-Oriented Design Goals

### Reliability

- bounded timeouts
- controlled retries
- upstream failure handling
- circuit-breaker evaluation
- graceful degradation where appropriate

### Performance

- response caching
- pagination strategy
- reduced downstream calls
- appropriate parallelization where justified
- performance metrics

### Maintainability

- clear package boundaries
- dependency isolation
- application-owned API contracts
- centralized configuration
- automated tests
- documented architectural decisions

### Operability

- health checks
- readiness/liveness support
- structured logging
- metrics
- tracing where appropriate
- CI/CD

### Security

- externalized secrets
- authentication when required
- authorization when required
- secure configuration
- dependency management
- safe logging practices

---

## Planned GitHub Insights Capabilities

### Developer Insights

Potential endpoint:

```http
GET /api/v1/github/{username}/insights
```

Potential response information:

- total repositories
- repository language distribution
- total stars
- total forks
- most-starred repository
- most-forked repository
- recently updated repositories
- account age

Example conceptual response:

```json
{
  "username": "octocat",
  "repository_count": 8,
  "total_stars": 245,
  "total_forks": 37,
  "languages": {
    "Java": 4,
    "JavaScript": 2,
    "Python": 2
  },
  "most_starred_repository": {
    "name": "example-repository",
    "stars": 120
  }
}
```

The exact contract will be defined when the feature is implemented.

### Repository Details

Potential endpoint:

```http
GET /api/v1/github/{username}/repos/{repository}
```

Possible future data includes:

- language
- stars
- forks
- open issues
- creation date
- last update date
- default branch

---

## Future Architecture

```text
                       React / TypeScript
                              |
                              v
                    REST / GraphQL API
                              |
                              v
                     Application Services
                              |
              +---------------+---------------+
              |                               |
              v                               v
       GitHub Integration                Insight Engine
              |                               |
              v                               v
          GitHub API                    Aggregation Logic
              |
              v
          Cache Layer
              |
              v
       Metrics / Logging
```

Features will only be added when they solve a clear application or operational problem.

The goal is not to add technologies simply to make the project larger.

---

## Deployment Roadmap

Planned infrastructure and delivery improvements include:

- Docker image
- GitHub Actions CI pipeline
- automated build and test
- integration test execution
- container health checks
- externalized configuration
- environment-specific configuration
- Kubernetes deployment configuration
- readiness and liveness probes

Cloud-specific infrastructure will be introduced only when there is a clear need for it.

---

## API Evolution

Potential future interfaces include:

```text
REST
GraphQL
```

GraphQL may be useful once consumers need flexible access to a larger set of GitHub profile, repository, and insight data.

It will not replace REST simply for the sake of introducing another framework.

---

## Frontend Roadmap

A lightweight React and TypeScript interface may be added to demonstrate end-to-end application development.

Potential functionality:

- search for a GitHub username
- display developer profile information
- list repositories
- display language distribution
- show repository statistics
- display insight summaries

The backend will remain independently usable as an API.

---

## Engineering Principles

### Keep Responsibilities Clear

Controllers handle HTTP.

Services coordinate application behavior.

Clients communicate with external systems.

Mappers transform models.

Infrastructure concerns remain separate from business logic.

### Do Not Leak External Contracts

GitHub's API model should not become the application's public contract.

### Build for Failure

External systems can be:

- slow
- unavailable
- rate limited
- inconsistent

The application should make those failure modes explicit.

### Add Complexity Only When It Has a Purpose

A simple synchronous call is preferable to a distributed workflow when the requirements do not justify additional complexity.

Caching, messaging, asynchronous processing, databases, GraphQL, Kubernetes, and other technologies should be introduced because they solve a real problem.

### Make the System Observable

Production problems should be diagnosable without attaching a debugger to a running application.

### Make Changes Safe

Automated tests and CI/CD should make it possible to change the application confidently.

---

## Roadmap

### Phase 1 — Foundation

- [x] Spring Boot application
- [x] GitHub user integration
- [x] GitHub repository integration
- [x] REST endpoint
- [x] DTO separation
- [x] Mapping layer
- [x] Centralized exception handling
- [x] Unit tests
- [x] Controller tests
- [x] Actuator dependency
- [x] Cache dependency
- [ ] Complete project rename to GitHub Insights API

### Phase 2 — Integration Hardening

- [ ] HTTP timeout configuration
- [ ] GitHub rate-limit handling
- [ ] Retry strategy for transient failures
- [ ] Repository pagination
- [ ] Response caching
- [ ] Cache TTL configuration
- [ ] Integration tests with a mock GitHub server
- [ ] Structured logging
- [ ] Application metrics

### Phase 3 — Developer Insights

- [ ] Developer insight service
- [ ] Repository statistics
- [ ] Language aggregation
- [ ] Star aggregation
- [ ] Fork aggregation
- [ ] Most-starred repository
- [ ] Recently updated repository analysis

### Phase 4 — Delivery and Operations

- [ ] Docker
- [ ] GitHub Actions CI
- [ ] Automated test pipeline
- [ ] Container health checks
- [ ] Readiness/liveness configuration
- [ ] Kubernetes deployment configuration

### Phase 5 — API Expansion

- [ ] GraphQL endpoint
- [ ] Expanded repository APIs
- [ ] API documentation / OpenAPI

### Phase 6 — User Interface

- [ ] React
- [ ] TypeScript
- [ ] Developer search
- [ ] Profile display
- [ ] Repository display
- [ ] Insight visualization
- [ ] End-to-end testing

---

## Author

Developed by Daniel Stryjewski.

GitHub Insights API is an evolving engineering project focused on building a clean, reliable, and maintainable Spring Boot integration service and then expanding it incrementally into a broader developer-insights platform.
