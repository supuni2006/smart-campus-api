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
git clone https://github.com/supuni2006/smart-campus-api.git
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


# 📘 Smart Campus API – Report Q&A

## 🧩 Part 1 – Service Architecture & Setup

### Q1.1: In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

By default, JAX-RS uses a per-request lifecycle — it creates a new Resource class instance for every incoming HTTP request and discards it once the response is sent. This means any data stored in instance variables would be lost after each request, making them unsuitable for persisting state. To solve this, the project uses a singleton DataStore shared across all requests, backed by ConcurrentHashMap which is thread-safe and prevents race conditions when multiple requests read or write concurrently.

---

### Q1.2: Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

HATEOAS (Hypermedia as the Engine of Application State) means the API embeds navigational links in its responses so clients can discover endpoints at runtime without hard-coding URLs. This decouples the client from the server — if the server changes its URL structure, clients following links adapt automatically without code changes. It makes the API self-documenting and represents Level 3 (the highest level) of the Richardson Maturity Model for REST APIs.

---

## 🏢 Part 2 – Room Management

### Q2.1: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.

Returning only IDs reduces bandwidth but forces the client to make one additional HTTP request per room to fetch its details — the N+1 request problem — increasing latency and server load. Returning full objects requires just one request but increases payload size. Best practice is to return a lightweight summary (id, name, capacity) in the list, reserve the full object for GET /rooms/{id}, and use pagination for large collections.

---

### Q2.2: Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

Yes, the DELETE operation is idempotent. REST defines idempotency as: calling the same request N times produces the same server state as calling it once. The first DELETE /rooms/{id} removes the room from the data store and returns 204 No Content. Any subsequent identical DELETE for the same ID finds no room and returns 404 Not Found. Crucially, the server state after both calls is identical — the room does not exist. The response code differs (204 vs 404), but idempotency is a property of state, not of the response. The room remains absent regardless of how many times the request is repeated, confirming idempotency.

---

## 🌡️ Part 3 – Sensor Operations & Filtering

### Q3.1: We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

The JAX-RS runtime performs content negotiation and cannot match the method if the Content-Type is not application/json. It automatically returns 415 Unsupported Media Type without deserializing the body or invoking the method. This protects the API from receiving malformed data — if a client sends text/plain or application/xml, the request is rejected before any application logic executes.

---

### Q3.2: You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

A path segment such as /sensors/type/CO2 implies that CO2 is a named resource with its own identity that can be directly addressed, fetched, or modified. This is semantically misleading — CO2 is a filter criterion, not a resource. Treating it as a path segment also makes composing multiple filters awkward (e.g., /sensors/type/CO2/status/ACTIVE becomes a deeply nested, unreadable path).

Query parameters (/sensors?type=CO2) are the correct semantic tool for optional collection refinement. They are naturally composable (?type=CO2&status=ACTIVE), can be omitted entirely to return the full collection, and do not affect the resource identity or the URL hierarchy. They also align with how search engines, caching layers, and API gateways interpret URLs — query strings signal "variation of the same resource" rather than "a different resource entirely." This makes the API more intuitive, more flexible, and easier to extend in the future.

---

## 🧱 Part 4 – Sub-Resources

### Q4.1: Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

Without sub-resource locators, every nested path would be defined inside one class, creating a "God class" with hundreds of methods that violates the Single Responsibility Principle and becomes impossible to maintain. The locator pattern delegates /readings logic to a dedicated SensorReadingsResource class. Each class has one clear responsibility, is independently testable, and can be modified without affecting other resources — keeping the codebase clean and scalable as the API grows.

---

## ⚠️ Part 5 – Advanced Error Handling & Exception Mapping

### Q5.1: Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

HTTP 404 Not Found is a routing error — it means the URL the client requested does not exist on the server. In this scenario, the URL POST /api/v1/sensors is entirely valid and the endpoint was found successfully. The problem lies not in the URL but inside the request body, which references a roomId that has no corresponding record in the data store.

HTTP 422 Unprocessable Entity precisely describes this situation: the request was syntactically valid JSON (not a 400 Bad Request), the endpoint existed (not a 404), but the server cannot process the entity because it contains a semantically broken reference. Using 422 gives the client accurate diagnostic information — it knows the URL is correct and the JSON syntax is fine, but that a specific field value in its payload is referencing something that does not exist. A 404 response would mislead the client into believing it called the wrong endpoint, making debugging unnecessarily difficult.

---

### Q5.2: From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

A raw Java stack trace is an information disclosure vulnerability. It exposes:

- Framework and library identification — class names like org.glassfish.jersey or com.fasterxml.jackson reveal the exact frameworks and versions in use, allowing an attacker to look up known CVEs for those specific versions.
- Internal package and class structure — full class names and method signatures reveal the application's architecture, making it easier to craft targeted injection or bypass payloads.
- Server file paths — stack frames often include absolute file paths, disclosing directory structure and potentially the operating system.
- Business logic details — the sequence of method calls can reveal how the application processes requests, exposing logic that could be exploited.
- Java version — identifying the runtime version enables targeting of JVM-level vulnerabilities.

The GenericErrorMapper in this project intercepts all unexpected Throwable errors before they reach the HTTP response and returns only a generic {"error": "INTERNAL_ERROR", "message": "An unexpected error occurred."} body. This eliminates the information leakage entirely while still signalling to the client that something went wrong on the server side.

*University of Westminster – 5COSC022W Client-Server Architectures – 2025/26*
