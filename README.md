# Voting Challenge API

## 📌 Overview
The **Voting Challenge API** is a voting system that allows:
- Creating voting topics.
- Opening voting sessions with a custom duration.
- Registering votes with CPF validation.
- Retrieving voting results.

The project uses **Spring Boot 3**, **PostgreSQL** database, **Redis** cache, **Swagger/OpenAPI** documentation, and CPF validation simulated with **Wiremock**.

---

## 🛠️ Technologies Used
- Java 17
- Spring Boot 3.5.4
- Spring Data JPA
- PostgreSQL (Main database)
- Redis (Cache)
- Spring Cloud OpenFeign (External CPF validation service communication)
- Springdoc OpenAPI (Swagger)
- Lombok
- Flyway (Database migration)
- Docker Compose (Local infrastructure)
- Wiremock (Mocked CPF validation service)

---

## 🚀 How to Run the Application

### Prerequisites
- Java 17+
- Maven 3.9+
- Docker & Docker Compose

### Start local infrastructure
docker-compose up --build

This will start:
- PostgreSQL (port 5432)
- Redis (port 6379)
- Wiremock (port 8089)
- Application (port 8080)

### When running, the application will be available in:

http://localhost:8080/swagger-ui.html

- **Open API documentation also available at: Collection available at:**
-  doc/open-api.json

---

## 🧪 CPF Validation (Wiremock)
The Wiremock mock for CPF validation is configured with the following rules:
- **02154789681** → Valid CPF, allowed to vote.
- **11144477788** → Invalid CPF, not allowed to vote.

If you need to test other CPFs, you must add new mappings in the `wiremock/mappings` folder and corresponding example responses in `wiremock/__files`.

---

### Run the application locally without Docker
mvn spring-boot:run

---

## 📌 Endpoints

### **1️⃣ Topics (`/api/v1/topics`)**

#### Create a new topic
- **POST** `/api/v1/topics`
- **Request Body**:
- **JSON**
```json
{
  "title": "Increase in share capital",
  "description": "Proposal to increase capital for expansion"
}
```

- **Field Types**

| Field       | Type   | Required | Description                  | Example                                     |
|-------------|--------|----------|------------------------------|---------------------------------------------|
| title       | String | Yes      | The title of the topic       | "Increase in share capital"                 |
| description | String | Yes      | The description of the topic | "Proposal to increase capital for expansion"|


---
- **Response**:
- **JSON**

```json
{
  "topicId": 1,
  "title": "Increase in share capital",
  "description": "Proposal to increase capital for expansion",
  "status": "OPEN",
  "createdAt": "2025-08-12T14:30:00",
  "updatedAt": "2025-08-12T14:30:00"
}
```
- **Field Types**

| Field       | Type           | Description                                | Example                                     |
|-------------|----------------|--------------------------------------------|---------------------------------------------|
| topicId     | Long           | Unique identifier of the topic             | 1                                           |
| title       | String         | The title of the topic                     | "Increase in share capital"                 |
| description | String         | The description of the topic               | "Proposal to increase capital for expansion"|
| status      | String         | Current status (`DRAFT`, `OPEN`, etc.)     | "OPEN"                                      |
| createdAt   | LocalDateTime  | Topic creation timestamp                   | "2025-08-12T14:30:00"                        |
| updatedAt   | LocalDateTime  | Topic last update timestamp                | "2025-08-12T14:30:00"                        |

---

#### Get topic by ID
- **GET** `/api/v1/topics/{topicId}`
- **Response**:
- **JSON**
```json
{
  "topicId": 1,
  "title": "Increase in share capital",
  "description": "Proposal to increase capital for expansion",
  "status": "OPEN",
  "createdAt": "2025-08-12T14:30:00",
  "updatedAt": "2025-08-12T14:30:00"
}
```
- **Field Types**

| Field       | Type           | Description                                | Example                                     |
|-------------|----------------|--------------------------------------------|---------------------------------------------|
| topicId     | Long           | Unique identifier of the topic             | 1                                           |
| title       | String         | The title of the topic                     | "Increase in share capital"                 |
| description | String         | The description of the topic               | "Proposal to increase capital for expansion"|
| status      | String         | Current status (`DRAFT`, `OPEN`, etc.)     | "OPEN"                                      |
| createdAt   | LocalDateTime  | Topic creation timestamp                   | "2025-08-12T14:30:00"                       |
| updatedAt   | LocalDateTime  | Topic last update timestamp                | "2025-08-12T14:30:00"                       |

---

### **2️⃣ Sessions (`/api/v1/topics/{topicId}/sessions`)**

#### Open a voting session
- **POST** `/api/v1/topics/{topicId}/sessions`
- **Request Body**:
- **JSON**
  
```json
{
  "duration": 90
}
```
- **Field Types**

| Field             | Type   | Required | Description                                                     | Example |
|-------------------|--------|----------|-----------------------------------------------------------------|---------|
| duration | Long   | No       | Duration in seconds. Defaults to 60 seconds if not provided              | 90       |
---

- **Response**:
- **JSON**
```json
{
  "sessionId": 9,
  "topicId": 9,
  "opensAt": "2025-08-12T15:14:42.826Z",
  "closesAt": "2025-08-12T15:14:42.826Z"
}
```
- **Field Types**

| Field     | Type           | Description                              | Example                |
|-----------|----------------|------------------------------------------|------------------------|
| sessionId | Long           | Unique identifier of the session         | 1                      |
| topicId   | Long           | ID of the topic                          | 1                      |
| opensAt   | LocalDateTime  | Timestamp when the session started       | "2025-08-12T14:30:00"  |
| closesAt  | LocalDateTime  | Timestamp when the session will end      | "2025-08-12T14:32:00"  |

---

### **3️⃣ Votes (`/api/v1/topics/{topicId}/votes`)**

#### Register a vote
- **POST** `/api/v1/topics/{topicId}/votes`
- **Request Body**:
- **JSON**
```json
{
  "associateExternalId": "12345678901",
  "choice": "Sim"
}
```
- **Field Types**

| Field | Type   | Required | Description                                                     | Example         |
|-------|--------|----------|-----------------------------------------------------------------|-----------------|
| cpf   | String | Yes      | Voter CPF. Must pass CPF validation API                         | "02154789681"   |
| vote  | String | Yes      | Voting choice. Accepted values: `YES` or `NO`                   | "YES"           |
---


- **Response**:
- **JSON**
```json
{
  "voteId": 1,
  "topicId": 1,
  "associateId": "12345678901",
  "choice": "Sim",
  "createdAt": "2025-08-12T14:31:00"
}
```

- **Field Types**

| Field     | Type           | Description                              | Example                |
|-----------|----------------|------------------------------------------|------------------------|
| voteId    | Long           | Unique identifier of the vote            | 1                      |
| topicId   | Long           | ID of the topic                          | 1                      |
| cpf       | String         | Voter CPF                                | "02154789681"          |
| vote      | String         | Voting choice (`YES` or `NO`)            | "YES"                  |
| votedAt   | LocalDateTime  | Timestamp when the vote was registered   | "2025-08-12T14:31:00"  |
---

### **4️⃣ Results (`/api/v1/topics/{topicId}/result`)**

#### Get voting results
- **GET** `/api/v1/topics/{topicId}/result`
- **Response**:
- **JSON**
```json
{
  "topicId": 1,
  "yes": 10,
  "no": 5,
  "totalVotes": 15,
  "winner": "yes",
  "topicName": "TopicExample"
}
```
- **Field Types**

| Field    | Type  | Description                  | Example |
|----------|-------|------------------------------|---------|
| topicId  | Long  | ID of the topic              | 1       |
| yesVotes | Long  | Number of votes in favor     | 10      |
| noVotes  | Long  | Number of votes against      | 5       |
---

## 🔒 Business Rules
- A CPF can vote only once per topic.
- The voting session must be open to accept votes.
- CPF validation is done via an external service (mocked with Wiremock).
- If no session duration is provided, the default is 1 minute.

---

## 📄 Documentation
Once the application is running, access:
http://localhost:8080/swagger-ui.html

---


## 🛠️ Tests
Run:
mvn test


