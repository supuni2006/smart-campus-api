# 🏫 Smart Campus API

> **5COSC022W – Client-Server Architectures Coursework 2025/26**  
> A JAX-RS RESTful API for managing campus Rooms, Sensors, and Sensor Readings.  
> Built with **Jersey 3.1** on a **Grizzly** embedded HTTP server.

---

## 📋 Table of Contents

- [API Overview](#api-overview)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [How to Build and Run](#how-to-build-and-run)
- [Sample curl Commands](#sample-curl-commands)
- [Error Handling Summary](#error-handling-summary)
- [Conceptual Report](#conceptual-report)

---

## API Overview

| Detail | Value |
|--------|-------|
| **Base URL** | `http://localhost:8081/api/v1` |
| **Data Format** | JSON |
| **Server** | Grizzly (embedded – no external servlet container needed) |
| **Contact** | admin@smartcampus.ac.uk |

### Resource Hierarchy

```
/api/v1
├── /rooms
│   ├── GET    → list all rooms
│   ├── POST   → create a room
│   └── /{roomId}
│       ├── GET    → get a specific room
│       └── DELETE → delete room (blocked if sensors are assigned)
└── /sensors
    ├── GET    → list sensors (optional ?type= filter)
    ├── POST   → register a sensor (validates roomId exists)
    └── /{sensorId}
        ├── GET → get a specific sensor
        └── /readings
            ├── GET  → list all readings for sensor
            └── POST → add a reading (blocked if sensor is MAINTENANCE)
```

### Full Endpoint Table

| Method | Path | Description | Success Code |
|--------|------|-------------|-------------|
| `GET` | `/api/v1` | Discovery – API metadata & links | 200 |
| `GET` | `/api/v1/rooms` | List all rooms | 200 |
| `POST` | `/api/v1/rooms` | Create a room | 201 |
| `GET` | `/api/v1/rooms/{roomId}` | Get a room by ID | 200 |
| `DELETE` | `/api/v1/rooms/{roomId}` | Delete a room | 204 |
| `GET` | `/api/v1/sensors` | List sensors (optional `?type=`) | 200 |
| `POST` | `/api/v1/sensors` | Register a new sensor | 201 |
| `GET` | `/api/v1/sensors/{sensorId}` | Get a sensor by ID | 200 |
| `GET` | `/api/v1/sensors/{sensorId}/readings` | List readings for a sensor | 200 |
| `POST` | `/api/v1/sensors/{sensorId}/readings` | Add a new reading | 201 |

---

## Project Structure

```
smart-campus-api/
├── pom.xml
└── src/main/java/com/smartcampus/
    ├── Main.java                          # Entry point – starts Grizzly server
    ├── SmartCampusApplication.java        # JAX-RS app config (@ApplicationPath)
    ├── model/
    │   ├── Room.java
    │   ├── Sensor.java
    │   └── SensorReading.java
    ├── store/
    │   └── DataStore.java                 # Singleton in-memory store (ConcurrentHashMap)
    ├── resource/
    │   ├── RootResource.java              # GET /api/v1  (discovery)
    │   ├── RoomsResource.java             # /api/v1/rooms
    │   ├── SensorsResource.java           # /api/v1/sensors
    │   └── SensorReadingsResource.java    # /api/v1/sensors/{id}/readings (sub-resource)
    ├── exception/
    │   ├── RoomNotEmptyException.java
    │   ├── LinkedResourceNotFoundException.java
    │   └── SensorUnavailableException.java
    ├── mapper/
    │   ├── RoomNotEmptyMapper.java        # 409 Conflict
    │   ├── LinkedResourceNotFoundMapper.java # 422 Unprocessable Entity
    │   ├── SensorUnavailableMapper.java   # 403 Forbidden
    │   └── GenericErrorMapper.java        # 500 Internal Server Error (catch-all)
    └── dto/
        └── ApiError.java                  # Consistent JSON error response body
```

---

## Prerequisites

- **Java 17** or later
- **Maven 3.8** or later

Verify your setup:

```bash
java -version
mvn -version
```

---

## How to Build and Run

### Step 1 – Clone the repository

```bash
git clone https://github.com/<your-username>/smart-campus-api.git
cd smart-campus-api
```

### Step 2 – Build the fat JAR

```bash
mvn clean package
```

This compiles the project and packages everything (including Jersey and Grizzly) into a single executable JAR at `target/smart-campus-api-1.0.0.jar`.

### Step 3 – Start the server

```bash
java -jar target/smart-campus-api-1.0.0.jar
```

You should see:

```
Smart Campus API running at:
http://localhost:8081/api/v1
```

### Step 4 – Verify it is running

```bash
curl http://localhost:8081/api/v1
```

Expected response:

```json
{
  "name": "Smart Campus API",
  "version": "v1",
  "contact": "admin@smartcampus.ac.uk",
  "links": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

### Step 5 – Stop the server

Press **Enter** in the terminal where the server is running.

---

## Sample curl Commands

> **Tip:** Run these commands in order — later commands depend on IDs returned by earlier ones. Copy the `id` fields from JSON responses and substitute them where you see `<ROOM_ID>` or `<SENSOR_ID>`.

---

### 1. Discovery Endpoint

```bash
curl -s http://localhost:8081/api/v1
```

---

### 2. Create a Room

```bash
curl -s -X POST http://localhost:8081/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"name": "Library Quiet Study", "capacity": 50}'
```

**Response (201):**
```json
{
  "id": "a1b2c3d4-...",
  "name": "Library Quiet Study",
  "capacity": 50,
  "sensorIds": []
}
```

---

### 3. List All Rooms

```bash
curl -s http://localhost:8081/api/v1/rooms
```

---

### 4. Get a Specific Room

```bash
curl -s http://localhost:8081/api/v1/rooms/<ROOM_ID>
```

---

### 5. Register a Sensor (link it to the room)

```bash
curl -s -X POST http://localhost:8081/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type": "CO2", "status": "ACTIVE", "roomId": "<ROOM_ID>"}'
```

**Response (201):**
```json
{
  "id": "e5f6g7h8-...",
  "type": "CO2",
  "status": "ACTIVE",
  "currentValue": 0.0,
  "roomId": "<ROOM_ID>"
}
```

---

### 6. Filter Sensors by Type

```bash
curl -s "http://localhost:8081/api/v1/sensors?type=CO2"
```

---

### 7. Post a Sensor Reading

```bash
curl -s -X POST http://localhost:8081/api/v1/sensors/<SENSOR_ID>/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 412.5}'
```

**Response (201):**
```json
{
  "id": "r9s0t1u2-...",
  "timestamp": 1713430000000,
  "value": 412.5
}
```

---

### 8. Get All Readings for a Sensor

```bash
curl -s http://localhost:8081/api/v1/sensors/<SENSOR_ID>/readings
```

---

### 9. Attempt to Delete a Room That Has Sensors (triggers 409)

```bash
curl -s -X DELETE http://localhost:8081/api/v1/rooms/<ROOM_ID>
```

**Response (409 Conflict):**
```json
{
  "error": "ROOM_NOT_EMPTY",
  "message": "Room '<ROOM_ID>' cannot be deleted because it still has 1 sensor(s) assigned to it."
}
```

---

### 10. Attempt to Register a Sensor With a Non-Existent Room (triggers 422)

```bash
curl -s -X POST http://localhost:8081/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type": "Temperature", "status": "ACTIVE", "roomId": "does-not-exist"}'
```

**Response (422 Unprocessable Entity):**
```json
{
  "error": "LINKED_RESOURCE_NOT_FOUND",
  "message": "Room with id 'does-not-exist' does not exist. Cannot register sensor."
}
```

---

### 11. Attempt to Post a Reading to a MAINTENANCE Sensor (triggers 403)

First, register a sensor with MAINTENANCE status:
```bash
curl -s -X POST http://localhost:8081/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type": "Occupancy", "status": "MAINTENANCE", "roomId": "<ROOM_ID>"}'
```

Then try to add a reading:
```bash
curl -s -X POST http://localhost:8081/api/v1/sensors/<MAINTENANCE_SENSOR_ID>/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 10.0}'
```

**Response (403 Forbidden):**
```json
{
  "error": "SENSOR_UNAVAILABLE",
  "message": "Sensor '<SENSOR_ID>' is currently under MAINTENANCE and cannot accept new readings."
}
```

---

## Error Handling Summary

| Scenario | Exception | HTTP Status |
|----------|-----------|-------------|
| Delete room with sensors assigned | `RoomNotEmptyException` | **409** Conflict |
| Register sensor with non-existent roomId | `LinkedResourceNotFoundException` | **422** Unprocessable Entity |
| Post reading to a MAINTENANCE sensor | `SensorUnavailableException` | **403** Forbidden |
| Any other unexpected runtime error | `Throwable` (catch-all) | **500** Internal Server Error |
| Resource not found | JAX-RS `NotFoundException` | **404** Not Found |
| Wrong Content-Type sent | JAX-RS built-in | **415** Unsupported Media Type |

All error responses follow this consistent JSON structure:

```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable explanation of what went wrong."
}
```

---

## Conceptual Report

### Part 1 – Service Architecture & Setup

---

**Q1.1: Explain the default lifecycle of a JAX-RS Resource class. How does this impact in-memory data management?**

By default, JAX-RS creates a **new instance of a Resource class for every incoming HTTP request** (per-request scope). This means each request gets its own object, so instance variables are completely isolated between concurrent requests. However, because a new instance is created and discarded per request, any data stored directly on the resource object would be permanently lost the moment the request finishes.

This design decision directly shapes how in-memory state must be managed. Storing data in instance fields of the resource class would cause data loss between requests. The solution used in this project is a **singleton `DataStore`** — a single, application-scoped object that lives for the entire lifetime of the server and holds all `Room`, `Sensor`, and `SensorReading` data in `ConcurrentHashMap` collections. `ConcurrentHashMap` is thread-safe by design, preventing race conditions when multiple simultaneous requests attempt to read or write data at the same time. Each resource class obtains a reference to this singleton via `DataStore.get()`, ensuring all requests share the same data consistently.

---

**Q1.2: Why is HATEOAS considered a hallmark of advanced RESTful design? How does it benefit client developers?**

HATEOAS — Hypermedia as the Engine of Application State — is the principle that API responses should include links guiding the client to related resources and available actions, rather than requiring the client to construct URLs from external documentation. For example, a response for a newly created sensor might include a `"readings"` link pointing directly to that sensor's readings endpoint.

The key benefit is **reduced coupling between client and server**. A client following hypermedia links does not need to hard-code any URL patterns. If the server restructures its URL hierarchy in a future version, clients that navigate via links continue to work without modification. It also makes the API self-documenting at runtime — a developer can explore the entire API surface simply by following links in responses, which is far more reliable than keeping static documentation in sync with a changing codebase. This is why HATEOAS is considered the highest maturity level in the Richardson Maturity Model for REST APIs.

---

### Part 2 – Room Management

---

**Q2.1: What are the implications of returning only IDs versus full room objects in a list response?**

Returning **only IDs** minimises bandwidth — the response payload is tiny regardless of the number of rooms. However, it forces the client to make one additional HTTP request per room to retrieve its details, creating the classic **N+1 request problem**. For a campus with hundreds of rooms, this means hundreds of round-trips, dramatically increasing total latency and placing unnecessary load on the server.

Returning **full room objects** in a single list request solves the N+1 problem but can be costly if the dataset is large or if each object carries substantial nested data. The industry-standard approach is to return a **lightweight summary representation** (key fields like id, name, capacity) in the list, and reserve the full representation for the individual `GET /{roomId}` endpoint. For very large collections, **pagination** using query parameters such as `?page=1&size=20` is also essential to prevent unbounded response sizes.

---

**Q2.2: Is the DELETE operation idempotent in your implementation? Justify your answer.**

Yes, the DELETE operation is idempotent. REST defines idempotency as: calling the same request N times produces the same **server state** as calling it once. The first `DELETE /rooms/{id}` removes the room from the data store and returns **204 No Content**. Any subsequent identical `DELETE` for the same ID finds no room and returns **404 Not Found**. Crucially, the server state after both calls is identical — the room does not exist. The *response code* differs (204 vs 404), but idempotency is a property of state, not of the response. The room remains absent regardless of how many times the request is repeated, confirming idempotency.

---

### Part 3 – Sensor Operations & Filtering

---

**Q3.1: What are the technical consequences if a client sends a POST request with the wrong Content-Type?**

The `@Consumes(MediaType.APPLICATION_JSON)` annotation instructs the JAX-RS runtime to only match this method when the incoming request carries a `Content-Type: application/json` header. If a client sends `text/plain`, `application/xml`, or any other media type, the runtime performs content negotiation and finds no matching resource method for that combination of path and content type. It automatically returns **415 Unsupported Media Type** without ever deserializing the body or invoking the method.

This is a deliberate protection mechanism. It ensures the message body is never passed to Jackson for deserialization unless the format is confirmed to be JSON, preventing malformed or unexpected data from reaching application logic. The method itself never executes, and no partial or corrupt state can be written to the data store.

---

**Q3.2: Why is the `@QueryParam` approach for filtering generally considered superior to embedding the filter in the URL path?**

A path segment such as `/sensors/type/CO2` implies that `CO2` is a **named resource** with its own identity that can be directly addressed, fetched, or modified. This is semantically misleading — `CO2` is a *filter criterion*, not a resource. Treating it as a path segment also makes composing multiple filters awkward (e.g., `/sensors/type/CO2/status/ACTIVE` becomes a deeply nested, unreadable path).

Query parameters (`/sensors?type=CO2`) are the correct semantic tool for optional collection refinement. They are naturally composable (`?type=CO2&status=ACTIVE`), can be omitted entirely to return the full collection, and do not affect the resource identity or the URL hierarchy. They also align with how search engines, caching layers, and API gateways interpret URLs — query strings signal "variation of the same resource" rather than "a different resource entirely." This makes the API more intuitive, more flexible, and easier to extend in the future.

---

### Part 4 – Sub-Resources

---

**Q4.1: Discuss the architectural benefits of the Sub-Resource Locator pattern.**

Without the sub-resource locator pattern, every nested path — `/sensors/{id}/readings`, `/sensors/{id}/readings/{rid}`, and any future additions — would have to be defined as methods inside `SensorsResource`. In a large API, this produces a **"God class"**: a single file with dozens or hundreds of methods that violates the Single Responsibility Principle and becomes extremely difficult to navigate, test, or maintain.

The sub-resource locator solves this by using a method (annotated with `@Path` but no HTTP verb) that simply instantiates and returns a new resource class, passing contextual state (in this case, `sensorId`) via the constructor. `SensorReadingsResource` is then entirely responsible for the `/readings` sub-path. Each class has one clear responsibility, can be unit-tested in isolation, and can be evolved independently. As the API grows — adding authentication, caching, or new sub-paths — the separation of concerns makes changes safe and localised, which mirrors how real-world production APIs are structured.

---

### Part 5 – Advanced Error Handling & Exception Mapping

---

**Q5.1: Why is HTTP 422 Unprocessable Entity more semantically accurate than 404 Not Found when a referenced roomId does not exist?**

HTTP **404 Not Found** is a routing error — it means the URL the client requested does not exist on the server. In this scenario, the URL `POST /api/v1/sensors` is entirely valid and the endpoint was found successfully. The problem lies not in the URL but inside the **request body**, which references a `roomId` that has no corresponding record in the data store.

HTTP **422 Unprocessable Entity** precisely describes this situation: the request was syntactically valid JSON (not a 400 Bad Request), the endpoint existed (not a 404), but the server cannot process the entity because it contains a **semantically broken reference**. Using 422 gives the client accurate diagnostic information — it knows the URL is correct and the JSON syntax is fine, but that a specific field value in its payload is referencing something that does not exist. A 404 response would mislead the client into believing it called the wrong endpoint, making debugging unnecessarily difficult.

---

**Q5.2: From a cybersecurity standpoint, what are the risks of exposing Java stack traces to external API consumers?**

A raw Java stack trace is an information disclosure vulnerability. It exposes:

- **Framework and library identification** — class names like `org.glassfish.jersey` or `com.fasterxml.jackson` reveal the exact frameworks and versions in use, allowing an attacker to look up known CVEs for those specific versions.
- **Internal package and class structure** — full class names and method signatures reveal the application's architecture, making it easier to craft targeted injection or bypass payloads.
- **Server file paths** — stack frames often include absolute file paths, disclosing directory structure and potentially the operating system.
- **Business logic details** — the sequence of method calls can reveal how the application processes requests, exposing logic that could be exploited.
- **Java version** — identifying the runtime version enables targeting of JVM-level vulnerabilities.

The `GenericErrorMapper` in this project intercepts all unexpected `Throwable` errors before they reach the HTTP response and returns only a generic `{"error": "INTERNAL_ERROR", "message": "An unexpected error occurred."}` body. This eliminates the information leakage entirely while still signalling to the client that something went wrong on the server side.

---

*University of Westminster – 5COSC022W Client-Server Architectures – 2025/26*
