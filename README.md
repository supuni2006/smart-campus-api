# Smart Campus API

A JAX-RS RESTful API for managing campus Rooms, Sensors, and Sensor Readings, built with Jersey 3 on a Grizzly embedded HTTP server.

---

## API Overview

- **Base URL:** `http://localhost:8081/api/v1`
- **Data format:** JSON
- **Resources:** Rooms → Sensors → SensorReadings (nested hierarchy)

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/v1 | Discovery endpoint |
| GET | /api/v1/rooms | List all rooms |
| POST | /api/v1/rooms | Create a room |
| GET | /api/v1/rooms/{id} | Get a room |
| DELETE | /api/v1/rooms/{id} | Delete a room (fails if sensors assigned) |
| GET | /api/v1/sensors | List sensors (optional `?type=` filter) |
| POST | /api/v1/sensors | Register a sensor |
| GET | /api/v1/sensors/{id} | Get a sensor |
| GET | /api/v1/sensors/{id}/readings | List readings for a sensor |
| POST | /api/v1/sensors/{id}/readings | Add a reading |

---

## How to Build and Run

**Prerequisites:** Java 17+, Maven 3.8+

```bash
# 1. Clone the repository
git clone <your-repo-url>
cd smart-campus-api

# 2. Build the project
mvn clean package

# 3. Run the server
java -jar target/smart-campus-api-1.0.0.jar
```

The server starts at: `http://localhost:8081/api/v1`

To stop the server, press **Enter** in the terminal.

---

## Sample curl Commands

### 1. Discovery
```bash
curl http://localhost:8081/api/v1
```

### 2. Create a Room
```bash
curl -X POST http://localhost:8081/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"name": "Library Quiet Study", "capacity": 50}'
```

### 3. Register a Sensor (replace ROOM_ID with real id from step 2)
```bash
curl -X POST http://localhost:8081/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type": "CO2", "status": "ACTIVE", "roomId": "ROOM_ID"}'
```

### 4. Filter Sensors by Type
```bash
curl "http://localhost:8081/api/v1/sensors?type=CO2"
```

### 5. Post a Sensor Reading (replace SENSOR_ID with real id from step 3)
```bash
curl -X POST http://localhost:8081/api/v1/sensors/SENSOR_ID/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 412.5}'
```

### 6. Attempt to Delete a Room with Sensors (triggers 409)
```bash
curl -X DELETE http://localhost:8081/api/v1/rooms/ROOM_ID
```

### 7. Get All Readings for a Sensor
```bash
curl http://localhost:8081/api/v1/sensors/SENSOR_ID/readings
```

---

## Conceptual Report

### Part 1 – JAX-RS Resource Lifecycle & Discovery

**Q: Explain the default lifecycle of a JAX-RS Resource class.**

By default, JAX-RS creates a **new instance of a Resource class for every incoming HTTP request** (per-request scope). This means each request gets its own object, so instance variables are isolated and there are no shared-state issues between concurrent requests at the object level. However, because a new object is created per request, any in-memory data stored as an instance field would be lost the moment the request ends. This is why this project uses a **singleton `DataStore`** — a single shared object holding the `ConcurrentHashMap` collections — rather than storing data in the resource classes themselves. The `ConcurrentHashMap` is thread-safe, preventing race conditions when multiple requests read or write concurrently.

**Q: Why is HATEOAS considered a hallmark of advanced RESTful design?**

HATEOAS (Hypermedia as the Engine of Application State) means the API includes links in its responses that guide clients to related actions or resources — for example, returning a room object with a `"sensors"` link. This benefits client developers because they do not need to hard-code URLs or memorise documentation; they can discover navigation paths at runtime from the responses themselves. This reduces coupling between client and server: if the server changes its URL structure, clients following hypermedia links adapt automatically rather than breaking.

---

### Part 2 – Room Management

**Q: Returning only IDs vs full room objects in a list.**

Returning only IDs is bandwidth-efficient — the payload is tiny regardless of how many rooms exist. However, the client must then make N additional requests to fetch details for each room, which increases latency and server load (the N+1 problem). Returning full room objects requires one request but can be expensive on bandwidth if rooms have large data or the list is very long. The best practice is to return full objects for moderate-sized lists and use pagination, or provide a lightweight summary representation with a link to the full detail endpoint.

**Q: Is DELETE idempotent in your implementation?**

Yes. Idempotency means calling the same request multiple times produces the same server state. The first `DELETE /rooms/{id}` removes the room. Any subsequent identical request finds no room and returns **404 Not Found**. The room is still absent — the server state is the same — so the operation is idempotent. The response code differs (204 vs 404), but the resource state does not change after the first call, which satisfies the REST definition of idempotency.

---

### Part 3 – Sensor Operations

**Q: Consequences of sending the wrong Content-Type on a POST with @Consumes(APPLICATION_JSON).**

JAX-RS matches incoming requests to methods using both the URL path and the `Content-Type` header. If a client sends `text/plain` or `application/xml` when the method is annotated `@Consumes(MediaType.APPLICATION_JSON)`, the JAX-RS runtime cannot find a matching method and automatically returns **415 Unsupported Media Type**. The request body is never deserialized and the method is never invoked. This protects the API from receiving malformed data in unexpected formats.

**Q: Query parameter vs path segment for filtering.**

A path segment approach like `/sensors/type/CO2` implies `CO2` is a distinct resource that can be individually addressed, which is semantically incorrect — it is a filter on a collection, not a resource itself. Query parameters (`?type=CO2`) are specifically designed for optional refinement and filtering of a collection. They can be combined (`?type=CO2&status=ACTIVE`), omitted entirely to return everything, and do not alter the resource hierarchy. Most API design guidelines (including REST's own constraints) recommend path segments for resource identity and query parameters for filtering, searching, and pagination.

---

### Part 4 – Sub-Resources

**Q: Architectural benefits of the Sub-Resource Locator pattern.**

Without sub-resource locators, all paths would be defined in one giant resource class. As the API grows, this creates an unmaintainable "God class" with hundreds of methods. The locator pattern delegates responsibility: `SensorsResource` handles `/sensors` logic and returns a `SensorReadingsResource` instance that is solely responsible for `/sensors/{id}/readings`. Each class has a single responsibility, is independently testable, and can be modified without affecting other resources. It mirrors real domain boundaries and makes large APIs far easier to navigate and maintain.

---

### Part 5 – Error Handling

**Q: Why is HTTP 422 more semantically accurate than 404 for a missing roomId reference?**

A 404 Not Found means the requested URL/resource does not exist. In this scenario, the URL `/api/v1/sensors` is perfectly valid and exists — the problem is that the *payload* references a `roomId` that does not exist in the system. The request was syntactically correct JSON (not a 400) and the endpoint was found (not a 404), but the server cannot process the entity because of a broken reference inside it. HTTP 422 Unprocessable Entity precisely describes this: the request is well-formed but semantically invalid. Using 404 here would mislead clients into thinking they are calling the wrong URL.

**Q: Security risks of exposing Java stack traces.**

A stack trace reveals the internal architecture of the application — class names, package structure, method names, and line numbers. An attacker can use this to identify the framework in use (e.g. Jersey, Spring), discover the Java version, find specific library versions with known CVEs, understand the code path and business logic to craft targeted exploits, and identify file paths on the server. Returning a generic 500 with a safe message like `"An unexpected error occurred"` eliminates this information leakage entirely.

---
