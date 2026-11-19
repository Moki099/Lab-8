# Event Registration API — Laboratory Work 8

## 1. Project Description

This project is an extension of Laboratory Work 7. It is a REST API for managing event registrations, developed using Spring Boot.

In Laboratory Work 8, the application was extended with PostgreSQL and Spring Data JPA to provide persistent data storage.

Instead of storing events in an in-memory ArrayList, the application now stores them in a PostgreSQL database.

## 2. Technologies Used

- Java 17 compatible bytecode (developed using JDK 27)
- Spring Boot 3.5.7
- Spring Web
- Spring Data JPA
- Hibernate
- PostgreSQL 18
- Gradle
- Postman / cURL

## 3. Project Structure

```text
event-registration-api/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── kz/iitu/registration/
│       │       ├── controller/
│       │       ├── model/
│       │       ├── repository/
│       │       ├── service/
│       │       └── EventRegistrationApplication.java
│       │
│       └── resources/
│           └── application.properties
│
├── gradle/
├── build.gradle
├── settings.gradle
├── gradlew
└── README.md
```

## 4. Requirements

Before running the application, make sure you have installed:

- Java Development Kit (JDK)
- PostgreSQL
- Gradle (the project includes Gradle Wrapper)
- Visual Studio Code or another Java IDE

## 5. Database Configuration

The application uses PostgreSQL as its database.

Create a database named:

```sql
CREATE DATABASE event_db;
```

Database configuration:

| Parameter | Value |
|---|---|
| Database | event_db |
| Host | localhost |
| Port | 5432 |
| Username | Your PostgreSQL username |
| Password | Your PostgreSQL password |

### Application Properties

The following configuration is used in `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/event_db
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Database credentials are provided through environment variables and are not stored in the source code.

### Set Environment Variables (Windows PowerShell)

```powershell
$env:DB_USER="postgres"
$env:DB_PASSWORD="your_password"
```

Replace `your_password` with your local PostgreSQL password.

## 6. Running the Application

Open the project directory in the terminal:

```powershell
cd "C:\Users\karam\OneDrive\Документы\web\lab8"
```

Set the database environment variables, then build the project:

```powershell
.\gradlew.bat build
```

Start the Spring Boot application:

```powershell
.\gradlew.bat bootRun
```

The application runs at:

```text
http://localhost:8080
```

## 7. REST API Endpoints

The application provides the following endpoints:

| HTTP Method | Endpoint | Description |
|---|---|---|
| GET | `/api/events` | Get all events |
| GET | `/api/events/{id}` | Get an event by ID |
| POST | `/api/events` | Create a new event |
| PUT | `/api/events/{id}` | Update an existing event |
| DELETE | `/api/events/{id}` | Delete an event |
| GET | `/api/events?title=quiz` | Search events by title |

### Example: Create an Event

**POST** `/api/events`

Request body:

```json
{
    "title": "JavaScript Quiz",
    "location": "Computer Lab 2"
}
```

Expected response:

```json
{
    "id": 1,
    "title": "JavaScript Quiz",
    "location": "Computer Lab 2"
}
```

HTTP Status: `201 Created`

### Example: Update an Event

**PUT** `/api/events/1`

```json
{
    "title": "JavaScript Quiz 2026",
    "location": "Room 301"
}
```

HTTP Status: `200 OK`

### Example: Get All Events

**GET** `/api/events`

### Example: Search Events

**GET** `/api/events?title=QUIZ`

The search supports partial matches and is case-insensitive.

## 8. HTTP Status Codes

| Status Code | Description |
|---|---|
| 200 OK | Request completed successfully |
| 201 Created | Event successfully created |
| 204 No Content | Event successfully deleted |
| 400 Bad Request | Invalid or missing event data |
| 404 Not Found | Event with the specified ID does not exist |

## 9. Database Persistence

Spring Data JPA is used to interact with PostgreSQL.

The application contains an Event entity mapped to the `events` table.

Hibernate automatically creates or updates the database schema using the entity definition.

To verify the stored events, execute:

```sql
SELECT id, title, location
FROM events;
```

### Persistence Test

To verify data persistence:

1. Create an event using POST.
2. Retrieve it using GET.
3. Stop the Spring Boot application.
4. Start the application again.
5. Retrieve the same event using its ID.

The event remains available after restarting the application because its data is stored in PostgreSQL rather than in application memory.

## 10. Main Learning Outcomes

During this laboratory work, the following concepts were implemented and studied:

- PostgreSQL database connection.
- Java Persistence API (JPA).
- Hibernate ORM.
- Entity mapping using `@Entity`, `@Id` and `@GeneratedValue`.
- Repository pattern using `JpaRepository`.
- CRUD operations with persistent storage.
- Database-generated primary keys.
- Case-insensitive title searching.
- Data validation and HTTP error handling.
- Data persistence after application restart.

## 11. Author

**Student:** Karamanov Yeskendir

**University:** International University of Information Technologies (IITU)

**Course:** Web Technologies

**Laboratory Work:** 8 — Spring Data JPA and PostgreSQL