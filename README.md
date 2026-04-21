# Smart Campus – Sensor & Room Management API

**Module:** 5COSC022W Client-Server Architectures
**Student:** [Your Name] | **ID:** [Your Student ID]
**Technology:** JAX-RS (Jersey 3.1.9) deployed on Apache Tomcat 10

---

## API Overview

This is a RESTful API built using JAX-RS (Jakarta RESTful Web Services) with Jersey as the implementation, deployed on Apache Tomcat 10. It manages university campus Rooms and IoT Sensors using in-memory data structures (ConcurrentHashMap). No database is used.

The API follows REST principles including resource-based URLs, correct HTTP status codes, structured JSON responses, and HATEOAS-style discovery links.

### Data Models

**Room** — represents a physical room on campus
- `id` (String) — unique identifier e.g. "LIB-301"
- `name` (String) — human-readable name
- `capacity` (int) — maximum occupancy
- `sensorIds` (List) — IDs of sensors deployed in this room

**Sensor** — represents an IoT sensor device
- `id` (String) — unique identifier e.g. "TEMP-001"
- `type` (String) — category e.g. "Temperature", "CO2", "Occupancy"
- `status` (String) — "ACTIVE", "MAINTENANCE", or "OFFLINE"
- `currentValue` (double) — most recent measurement
- `roomId` (String) — the room this sensor belongs to

**SensorReading** — represents a single recorded measurement
- `id` (String) — UUID generated automatically
- `timestamp` (long) — epoch milliseconds, set automatically
- `value` (double) — the recorded measurement

---

## Project Structure

```
src/main/java/com/smartcampus/
├── SmartCampusApplication.java          # JAX-RS Application class (@ApplicationPath /api/v1)
├── model/
│   ├── Room.java                        # Room POJO
│   ├── Sensor.java                      # Sensor POJO
│   └── SensorReading.java               # SensorReading POJO
├── store/
│   └── DataStore.java                   # Singleton in-memory store (ConcurrentHashMap)
├── resource/
│   ├── DiscoveryResource.java           # GET /api/v1
│   ├── RoomResource.java                # /api/v1/rooms
│   ├── SensorResource.java              # /api/v1/sensors
│   └── SensorReadingResource.java       # Sub-resource: /api/v1/sensors/{id}/readings
├── exception/
│   ├── ErrorResponse.java               # Structured JSON error body
│   ├── ResourceNotFoundException.java   # 404
│   ├── ResourceConflictException.java   # 409
│   ├── BadRequestException.java         # 400
│   ├── RoomNotEmptyException.java       # 409 - room has sensors
│   ├── LinkedResourceNotFoundException.java  # 422 - roomId does not exist
│   ├── SensorUnavailableException.java  # 403 - sensor in MAINTENANCE
│   ├── NotFoundExceptionMapper.java
│   ├── ConflictExceptionMapper.java
│   ├── BadRequestExceptionMapper.java
│   ├── RoomNotEmptyExceptionMapper.java
│   ├── LinkedResourceNotFoundExceptionMapper.java
│   ├── SensorUnavailableExceptionMapper.java
│   └── GenericExceptionMapper.java      # 500 catch-all
└── filter/
    └── LoggingFilter.java               # Logs all requests and responses
```

---

## How to Build and Run

### Prerequisites
- JDK 11 or higher
- Apache Tomcat 10 extracted to `C:\tomcat\apache-tomcat-10.1.54`
- Maven (bundled with NetBeans or installed separately)

### Step 1 — Build the WAR file
```bash
mvn clean package
```
This produces `target/smart-campus.war`

### Step 2 — Deploy to Tomcat
```bash
copy target\smart-campus.war C:\tomcat\apache-tomcat-10.1.54\webapps\smart-campus.war
```

### Step 3 — Start Tomcat
Double-click `C:\tomcat\start-smartcampus.bat`

Or from command line:
```bash
set JAVA_HOME=C:\Program Files\Java\jdk-23
set CATALINA_HOME=C:\tomcat\apache-tomcat-10.1.54
C:\tomcat\apache-tomcat-10.1.54\bin\startup.bat
```

### Step 4 — Access the API
```
http://localhost:8080/smart-campus/api/v1
```

---

## API Endpoints

### Discovery
| Method | Path | Description | Response |
|--------|------|-------------|----------|
| GET | `/api/v1` | API metadata and resource links | 200 OK |

### Rooms
| Method | Path | Description | Response |
|--------|------|-------------|----------|
| GET | `/api/v1/rooms` | List all rooms | 200 OK |
| POST | `/api/v1/rooms` | Create a new room | 201 Created + Location header |
| GET | `/api/v1/rooms/{roomId}` | Get room by ID | 200 OK / 404 |
| DELETE | `/api/v1/rooms/{roomId}` | Delete room (blocked if sensors exist) | 204 / 409 / 404 |

### Sensors
| Method | Path | Description | Response |
|--------|------|-------------|----------|
| GET | `/api/v1/sensors` | List all sensors | 200 OK |
| GET | `/api/v1/sensors?type=CO2` | Filter sensors by type | 200 OK |
| POST | `/api/v1/sensors` | Register a sensor (validates roomId) | 201 Created / 422 |
| GET | `/api/v1/sensors/{sensorId}` | Get sensor by ID | 200 OK / 404 |
| DELETE | `/api/v1/sensors/{sensorId}` | Delete sensor | 204 / 404 |

### Sensor Readings (Sub-Resource)
| Method | Path | Description | Response |
|--------|------|-------------|----------|
| GET | `/api/v1/sensors/{sensorId}/readings` | Get reading history | 200 OK |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add a reading (blocked if MAINTENANCE) | 201 Created / 403 |

---

## Error Responses

All errors return structured JSON — never a raw stack trace or HTML page:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Cannot delete room 'LIB-301': it still has 2 sensor(s) assigned."
}
```

| Status | Exception | Scenario |
|--------|-----------|----------|
| 400 | `BadRequestException` | Missing required fields |
| 403 | `SensorUnavailableException` | POST reading to MAINTENANCE sensor |
| 404 | `ResourceNotFoundException` | Resource not found |
| 409 | `RoomNotEmptyException` | Delete room that has sensors |
| 422 | `LinkedResourceNotFoundException` | Sensor references non-existent room |
| 500 | `GenericExceptionMapper` | Any unexpected error |

---

## Sample curl Commands

**1. Get API discovery info:**
```bash
curl http://localhost:8080/smart-campus/api/v1
```

**2. Get all rooms:**
```bash
curl http://localhost:8080/smart-campus/api/v1/rooms
```

**3. Create a new room:**
```bash
curl -X POST http://localhost:8080/smart-campus/api/v1/rooms -H "Content-Type: application/json" -d "{\"id\":\"HALL-01\",\"name\":\"Main Hall\",\"capacity\":200}"
```

**4. Register a sensor with valid roomId:**
```bash
curl -X POST http://localhost:8080/smart-campus/api/v1/sensors -H "Content-Type: application/json" -d "{\"id\":\"TEMP-002\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":0,\"roomId\":\"HALL-01\"}"
```

**5. Post a sensor reading:**
```bash
curl -X POST http://localhost:8080/smart-campus/api/v1/sensors/TEMP-001/readings -H "Content-Type: application/json" -d "{\"value\":23.5}"
```

**6. Filter sensors by type:**
```bash
curl http://localhost:8080/smart-campus/api/v1/sensors?type=CO2
```

**7. Attempt to delete a room that has sensors (triggers 409):**
```bash
curl -X DELETE http://localhost:8080/smart-campus/api/v1/rooms/LIB-301
```

---

## Report: Answers to Coursework Questions

---

### Part 1 – Question 1: JAX-RS Resource Lifecycle

In JAX-RS, every time a client sends an HTTP request, the framework creates a brand new instance of the resource class to handle it. This means the resource class is not shared between requests — each one gets its own fresh object. This is known as per-request scoping and it is the default behaviour defined in the JAX-RS specification.

This design has a direct impact on how data needs to be managed. Since each request creates a new resource object, you cannot store data inside the resource class itself because it would simply disappear after the request finishes. To keep data alive across multiple requests, the storage needs to exist independently of the resource class. In this project, a singleton DataStore class was created to hold all the data using ConcurrentHashMap. This map is thread-safe by design, meaning multiple requests can read and write to it at the same time without corrupting each other. For the readings list specifically, a synchronized method was used when adding new entries. This prevents a situation where two requests try to add a reading at the exact same moment and end up overwriting each other's data.

---

### Part 1 – Question 2: HATEOAS

HATEOAS stands for Hypermedia as the Engine of Application State. The idea behind it is that when a client receives a response from the API, that response should also include links telling the client what it can do next or where it can go. Instead of the client having to guess or look up URLs from a separate document, the server provides them directly inside the response.

This is much more useful than relying on static documentation because documentation can go out of date. If the server changes a URL, a client following hardcoded links would break. But if the client is following links provided in the response, it automatically adapts. It also makes the API easier to explore since a developer can start at the discovery endpoint and navigate the entire API just by following the links provided, without needing to read any external guide.

---

### Part 2 – Question 1: Returning IDs vs Full Objects in List Responses

There are two common approaches when returning a list of resources. The first is to return only the IDs of each item, and the second is to return the complete object for each item.

Returning only IDs keeps the response very small and fast to transfer, which is useful when the client does not need all the details straight away. The downside is that the client then has to make a separate request for each ID to get the full information, which can result in a large number of requests — this is commonly called the N+1 problem.

Returning full objects solves that problem by giving the client everything it needs in a single response. However, if the list is very large or the client only needs one or two fields, sending the entire object for every item wastes bandwidth. For this project, returning full objects was chosen because it makes the API easier to use for dashboard-style clients that need to display all the room details at once.

---

### Part 2 – Question 2: Is DELETE Idempotent?

Yes, the DELETE operation is idempotent in this implementation. Idempotency means that making the same request multiple times produces the same server state as making it just once.

If a client sends a DELETE request for a room that exists, the room gets removed and the server responds with 204 No Content. If the same client sends the exact same DELETE request again, the room is already gone, so the server responds with 404 Not Found. Even though the response code is different, the actual state of the server is the same both times — the room does not exist. According to the HTTP specification, idempotency is about the server state, not the response code, so this behaviour is completely correct.

The one special case is when a room still has sensors assigned to it. In that situation, the DELETE is blocked and returns 409 Conflict. The room remains untouched, which is also consistent no matter how many times the request is repeated.

---

### Part 3 – Question 1: @Consumes(APPLICATION_JSON) Mismatch

The @Consumes(MediaType.APPLICATION_JSON) annotation tells JAX-RS that a particular method only accepts requests where the Content-Type header is set to application/json. If a client sends a request with a different content type, such as text/plain or application/xml, JAX-RS will not even attempt to call the method. Instead, it automatically sends back a 415 Unsupported Media Type response before any of the method's code runs.

This is handled entirely by the JAX-RS framework as part of its content negotiation process. The benefit is that the application does not need to manually check the content type inside the method, and the client gets a clear and meaningful error code explaining exactly what went wrong.

---

### Part 3 – Question 2: @QueryParam vs Path Segment for Filtering

When filtering a collection, using a query parameter like /sensors?type=CO2 is the more appropriate design compared to embedding the filter in the path like /sensors/type/CO2.

The reason is that a URL path is meant to identify a specific resource. Writing /sensors/type/CO2 suggests that type/CO2 is itself a resource with its own identity, which is not the case here — we are simply narrowing down a list. Query parameters are designed exactly for this purpose: they are optional, they can be combined easily (for example ?type=CO2&status=ACTIVE), and leaving them out simply returns the full unfiltered collection. This matches how HTTP is intended to work and is what most API clients and caching systems expect to see.

---

### Part 4 – Question 1: Sub-Resource Locator Pattern

The sub-resource locator pattern is a way of splitting a large API into smaller, more focused classes. Instead of putting every single endpoint into one giant resource class, certain paths are delegated to separate classes that handle only their specific responsibility.

In this project, the SensorResource class handles everything related to sensors, but when a request comes in for /sensors/{id}/readings, it hands off control to a separate SensorReadingResource class. This keeps each class small and focused on one job. It also makes the code much easier to maintain because a developer working on readings does not need to understand the entire sensor management logic. Additionally, the locator method itself can act as a gatekeeper — in this project it checks whether the sensor actually exists before passing control to the readings resource, which prevents invalid requests from going any further.

---

### Part 5 – Question 1: Why 422 is More Semantically Accurate than 404

When a client tries to register a sensor and provides a roomId that does not exist in the system, returning 404 Not Found would be confusing. The 404 status code means the requested URL could not be found, but in this case the URL /api/v1/sensors is perfectly valid and the server found it without any problem.

The real issue is that the JSON body contains a reference to a room that does not exist. The request itself is well-formed and the content type is correct, but the data inside it cannot be processed because it points to something that is missing. HTTP 422 Unprocessable Entity was designed for exactly this situation — it tells the client that the server understood the request and parsed the JSON successfully, but the content of that JSON is logically invalid given the current state of the system. This gives the client much more useful information about what actually went wrong.

---

### Part 5 – Question 2: Security Risks of Exposing Stack Traces

Returning raw Java stack traces in API error responses is a serious security mistake for several reasons.

First, a stack trace reveals exactly which libraries and frameworks the application is using, including their version numbers. An attacker can use this information to search for known vulnerabilities in those specific versions and exploit them. Second, stack traces often include internal file paths and package names, which gives an attacker a map of how the server is structured. Third, method names and call sequences visible in a trace can reveal how the business logic works, making it easier to craft inputs that cause specific failures. Finally, if an attacker knows which inputs trigger exceptions, they can repeatedly send those inputs to slow down or crash the server.

To prevent this, a catch-all GenericExceptionMapper was implemented in this project. It intercepts any unhandled exception and returns a simple generic 500 response with no internal details, so the client never sees anything that could be used against the system.

---

### Part 5 – Question 3: Why Use JAX-RS Filters for Logging

Adding a Logger.info() call inside every single resource method would technically work, but it is a poor approach for several reasons.

The main problem is that it scatters logging code all over the codebase. Every time a new endpoint is added, the developer has to remember to add logging to it. If they forget, that endpoint goes unmonitored. Using a JAX-RS filter solves this completely — one class implementing ContainerRequestFilter and ContainerResponseFilter automatically covers every single request and response in the entire application, including any new endpoints added in the future.

It also keeps the resource methods clean. Each method only needs to contain the logic it is responsible for, without being cluttered with logging statements. If the logging format ever needs to change, only one file needs to be edited rather than hunting through every resource class. This follows the separation of concerns principle and makes the codebase significantly easier to maintain.
