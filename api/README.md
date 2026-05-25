# api

This project was created using the [Ktor Project Generator](https://start.ktor.io).

Here are some useful links to get you started:

* [Ktor Documentation](https://ktor.io/docs/home.html)
* [Ktor GitHub page](https://github.com/ktorio/ktor)
* [Ktor Slack chat](https://app.slack.com/client/T09229ZC6/C0A974TJ9). [Request an invite](https://surveys.jetbrains.com/s3/kotlin-slack-sign-up).

## Features

Here's a list of features included in this project:

| Name                                                                                  | Description                                                                        |
|---------------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| [AsyncAPI](https://start.ktor.io/p/com.asyncapi/server-asyncapi)                      | Generates and serves AsyncAPI documentation                                        |
| [Caching Headers](https://start.ktor.io/p/io.ktor/server-caching-headers)             | Provides options for responding with standard cache-control headers                |
| [Compression](https://start.ktor.io/p/io.ktor/server-compression)                     | Compresses responses using encoding algorithms like GZIP                           |
| [Conditional Headers](https://start.ktor.io/p/io.ktor/server-conditional-headers)     | Skips response body, depending on ETag and LastModified headers                    |
| [Default Headers](https://start.ktor.io/p/io.ktor/server-default-headers)             | Adds a default set of headers to HTTP responses                                    |
| [Forwarded Headers](https://start.ktor.io/p/io.ktor/server-forwarded-header-support)  | Allows handling proxied headers (X-Forwarded-*)                                    |
| [HSTS](https://start.ktor.io/p/io.ktor/server-hsts)                                   | Enables HTTP Strict Transport Security (HSTS)                                      |
| [HttpsRedirect](https://start.ktor.io/p/io.ktor/server-https-redirect)                | Redirects insecure HTTP requests to the respective HTTPS endpoint                  |
| [OpenAPI](https://start.ktor.io/p/io.ktor/server-openapi)                             | Serves OpenAPI documentation                                                       |
| [Partial Content](https://start.ktor.io/p/io.ktor/server-partial-content)             | Handles requests with the Range header                                             |
| [Simple Cache](https://start.ktor.io/p/com.ucasoft/server-simple-cache)               | Provides API for cache management                                                  |
| [Simple Memory Cache](https://start.ktor.io/p/com.ucasoft/server-simple-memory-cache) | Provides memory cache for Simple Cache plugin                                      |
| [Simple Redis Cache](https://start.ktor.io/p/com.ucasoft/server-simple-redis-cache)   | Provides Redis cache for Simple Cache plugin                                       |
| [Swagger](https://start.ktor.io/p/io.ktor/server-swagger)                             | Serves Swagger UI for your project                                                 |
| [Authentication](https://start.ktor.io/p/io.ktor/server-auth)                         | Provides extension point for handling the Authorization header                     |
| [Authentication JWT](https://start.ktor.io/p/io.ktor/server-auth-jwt)                 | Handles JSON Web Token (JWT) bearer authentication scheme                          |
| [AutoHeadResponse](https://start.ktor.io/p/io.ktor/server-auto-head-response)         | Provides automatic responses for HEAD requests                                     |
| [Request Validation](https://start.ktor.io/p/io.ktor/server-request-validation)       | Adds validation for incoming requests                                              |
| [Resources](https://start.ktor.io/p/io.ktor/server-resources)                         | Provides type-safe routing                                                         |
| [Server-Sent Events (SSE)](https://start.ktor.io/p/io.ktor/server-sse)                | Support for server push events                                                     |
| [Static Content](https://start.ktor.io/p/io.ktor/server-static-content)               | Serves static files from defined locations                                         |
| [Status Pages](https://start.ktor.io/p/io.ktor/server-status-pages)                   | Provides exception handling for routes                                             |
| [Call Logging](https://start.ktor.io/p/io.ktor/server-call-logging)                   | Logs client requests                                                               |
| [Metrics](https://start.ktor.io/p/io.ktor/server-metrics)                             | Adds supports for monitoring several metrics                                       |
| [Call ID](https://start.ktor.io/p/io.ktor/server-callid)                              | Allows to identify a request/call.                                                 |
| [Content Negotiation](https://start.ktor.io/p/io.ktor/server-content-negotiation)     | Provides automatic content conversion according to Content-Type and Accept headers |
| [kotlinx.serialization](https://start.ktor.io/p/io.ktor/server-kotlinx-serialization) | Handles JSON serialization using kotlinx.serialization library                     |
| [Dependency Injection](https://start.ktor.io/p/io.ktor/server-di)                     | Enables dependency injection for your server                                       |
| [Koin](https://start.ktor.io/p/io.insert-koin/server-koin)                            | Provides dependency injection                                                      |
| [Exposed](https://start.ktor.io/p/org.jetbrains/server-exposed)                       | Adds Exposed database to your application                                          |
| [PostgreSQL](https://start.ktor.io/p/org.jetbrains/server-postgres)                   | Adds Postgres database support                                                     |
| [Rate Limiting](https://start.ktor.io/p/io.github.flaxoos/server-rate-limiting)       | Manage request rate limiting as you see fit                                        |
| [Raw Sockets](https://start.ktor.io/p/io.ktor/server-network)                         | Adds raw socket support for TCP and UDP                                            |
| [Raw Secure SSL/TLS Sockets](https://start.ktor.io/p/io.ktor/server-network-tls)      | Adds secure socket support for TCP and UDP                                         |
| [WebSockets](https://start.ktor.io/p/io.ktor/server-websockets)                       | Adds WebSocket protocol support for bidirectional client connections               |

## Building & Running

To build or run the project, use one of the following tasks:

| Task                                    | Description                                                          |
|-----------------------------------------|----------------------------------------------------------------------|
| `./gradlew test`                        | Run the tests                                                        |
| `./gradlew build`                       | Build everything                                                     |
| `./gradlew buildFatJar`                 | Build an executable JAR of the server with all dependencies included |
| `./gradlew buildImage`                  | Build the docker image to use with the fat JAR                       |
| `./gradlew publishImageToLocalRegistry` | Publish the docker image locally                                     |
| `./gradlew run`                         | Run the server                                                       |
| `./gradlew runDocker`                   | Run using the local docker image                                     |
| `./gradlew generateMigration`           | Generates database migration                                         |

If the server starts successfully, you'll see the following output:

```log
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```
