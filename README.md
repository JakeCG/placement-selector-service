# Placement Selector Service
A Spring Boot application that filters video placements based on ad-tech criteria and exports results as CSV.

It simulates a lightweight ad-tech selection pipeline using in-memory datasets for videos and channels.

---

## Design Overview

The application follows a simple layered architecture:

Controller → Service → Repository → Exporter

- Filtering is implemented using Java Streams with modular predicate methods for clarity and testability
- CSV export is delegated to a dedicated component to enforce separation of concerns
- In-memory storage simulates a lightweight ad-serving pipeline without external dependencies

---

## Tech Stack
- Java 21
- Spring Boot 3.5
- Gradle
- Spring Web
- Spring Validation
- JUnit 5

---

## Build & Run
### Build the application
```bash
./gradlew build
```

### Run the application
```bash
./gradlew bootRun
```

---

## Testing
### Run tests
```bash
./gradlew test
```

---

## API

### Filter videos
**POST** `/api/placements/filter`  
**Content-Type:** `application/json`

### Example Request
```JSON
{
  "minViewCount": 10000,
  "minSubscriberCount": 5000,
  "languages": ["en"],
  "excludeMadeForKids": true,
  "requireEmbeddable": true,
  "publishedAfter": "2024-01-01",
  "minDurationSeconds": 60,
  "maxDurationSeconds": 1800
}
```

### Export CSV
**GET** `/api/placements/export?format=csv`  

Returns:
- CSV file of last filtered result

### Reset state
**DELETE** `/api/placements/filter`

Clears in-memory filtered results.

---

## How Filtering Works

1. All videos are loaded from in-memory JSON data at startup
2. Each video is evaluated against a set of independent filter predicates
3. Only videos passing all conditions are retained
4. Results are sorted by view count in descending order
5. The final result is stored for CSV export

---

## Data Source
The application loads static JSON files at startup:

- `videos.json`
- `channels.json`

These are stored in src/main/resources.

No database is used — this is intentional for simplicity and portability.

---

## Assumptions
1. In-memory dataset
   - All video and channel data is loaded at startup
   - No persistence layer is used
2. The service maintains in-memory state (`lastFilterResult`) and assumes a single active user/session.
   - Results are stored in memory for CSV export
   - Only one active session is supported
     Boolean fields (`isEmbeddable`, `isMadeForKids`) may be null in the dataset and are treated as false during filtering to ensure deterministic behaviour.
4. Channel integrity
   - Videos may reference missing channels
   - Missing channel is displayed as "Unknown"

---

## Trade-offs
1. No persistence
    - Simplifies setup by avoiding external infrastructure
    - Data is ephemeral and resets on application restart
   - Simplifies setup by avoiding external infrastructure, but makes the system unsuitable for production workloads


2. In-memory filtering
    - Very fast for small datasets
    - Not scalable for large video catalogs
    - No indexing or query optimization


3. Stateful service (lastFilter)
    - Enables quick CSV export
    - Not thread-safe in multi-user systems
    - Breaks horizontal scaling assumptions


4. Basic CSV generation
    - No external dependencies
    - No streaming support for large exports
    - No compression or pagination

---

## What I would change in production
### 1. Persistent storage 

Replace JSON files with:
- PostgreSQL / MySQL for structured data
- or Elasticsearch for search-heavy workloads

### 2. Pagination & streaming
Instead of loading everything:
- Add page, size, cursor
- Stream results using Spring WebFlux or JDBC cursors

### 3. Stateless API design
Remove lastFilterResult state:
- Store filter request client-side or in cache (Redis)
- Make export re-run filter or use job IDs
 
### 4. Async export system
For large datasets:
- Trigger export as background job
- Store result in S3 / blob storage
- Return download URL

Example:
- **POST** `/export → returns jobId`
- **GET** `/export/{jobId} → status`
- **GET** `/download/{jobId}`

### 5. YouTube API integration (real-world upgrade)
Instead of static JSON:
- Replace dataset with YouTube Data API

Handle:
- quota limits
- rate limiting
- retry/backoff strategies
- caching (Redis)

### 6. Better filtering architecture

Replace chained `.filter()` calls with:
- Specification pattern
- or dynamic query builder (JPA Criteria API)

### 7. Observability
Add:
- structured logging
- metrics (filter duration, export size)
- tracing (Spring Sleuth / OpenTelemetry)

### 8. Validation layer
Move validation out of service into:
- `@Valid` DTO constraints
- custom validators for cross-field rules