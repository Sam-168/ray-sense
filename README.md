# RaySense Backend API

A Spring Boot backend powering the RaySense smart attendance platform.

## Overview

RaySense Backend is the central API service responsible for:

- User authentication
- JWT authorization
- Attendance management
- Session management
- Student and lecturer workflows
- Database operations
- Communication with the face recognition microservice

The backend exposes RESTful APIs consumed by the Vue.js frontend.

---

## Architecture

Vue.js Frontend (Vercel)
↓
Spring Boot Backend API (Render)
↓
FastAPI Face Recognition Service (Render)
↓
MySQL Database (Aiven)

---

## Features

- JWT authentication
- Role-based access control
- Student registration
- Lecturer session management
- Attendance tracking
- Face recognition integration
- RESTful APIs
- MySQL integration
- Render deployment

---

## Tech Stack

- Java
- Spring Boot
- Spring Security
- JWT
- Hibernate / JPA
- MySQL
- Maven
- Render
- Aiven for MySQL

---

## Environment Variables

```env
DATABASE_URL=jdbc:mysql://your-aiven-host:your-port/defaultdb?sslmode=require
DATABASE_USERNAME=your-username
DATABASE_PASSWORD=your-password

JWT_SECRET=your-secret
JWT_EXPIRATION=43200000

PYTHON_SERVICE_URL=https://your-face-service.onrender.com

CORS_ALLOWED_ORIGINS=https://your-frontend.vercel.app
```

---

## Application Configuration

Example `application-prod.properties`

```properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}

jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:43200000}

face.recognition.service.url=${PYTHON_SERVICE_URL}
```

---

## Installation

Clone the repository:

```bash
git clone https://github.com/yourusername/raysense-backend.git
```

Navigate into the project:

```bash
cd raysense-backend
```

Run the application:

```bash
./mvnw spring-boot:run
```

---

## Security

The backend uses:

- JWT authentication
- Stateless session management
- Spring Security
- Role-based authorization

### Roles

- STUDENT
- LECTURER
- ADMIN

---

## API Endpoints

### Authentication

```text
POST /api/auth/login
POST /api/students/register-with-photo
```

### Attendance

```text
POST /api/attendance/sessions/{id}/mark-by-face
GET /api/attendance/my-attendance
```

### Face enrolment (admin only)

```text
POST /upload/{studentId}
POST /upload/{studentId}?replace=true
```

The endpoint accepts a `photo` multipart field containing a JPEG or PNG image up
to 5 MB. Replacing an existing face encoding must be requested explicitly.

### Lecturer

```text
POST /api/lecturer/sections/{id}/sessions/start
POST /api/lecturer/sessions/{id}/end
```

---

## Face Recognition Integration

The backend communicates with the FastAPI microservice using REST APIs.

Example:

```java
ResponseEntity<FaceEncodingResponse> response =
    restTemplate.postForEntity(
        pythonServiceUrl + "/register-face",
        request,
        FaceEncodingResponse.class
    );
```

---

## Deployment

The root `render.yaml` defines the backend as a free Render web service.

### Deployment Steps

1. Deploy the face-recognition service and copy its public Render URL.
2. Create an Aiven for MySQL service and copy its connection values.
3. In Render, create a Blueprint from this repository.
4. Enter the requested database credentials and face-service URL.
5. Deploy, then verify `/api/health`.

Use this format for `DATABASE_URL`:

```text
jdbc:mysql://HOST:PORT/defaultdb?sslmode=require
```

The Blueprint generates `JWT_SECRET` automatically. Database credentials remain dashboard-managed secrets and are not committed to source control. Production deployments do not create the development sample accounts.

---

## Challenges Solved

- CORS configuration
- JWT authentication
- Environment variable management
- Microservice communication
- Base64 image handling
- Cloud deployment debugging
- Production database connectivity

---

## Future Improvements

- Refresh token support
- WebSocket support
- API rate limiting
- CI/CD pipelines
- Dockerized deployment
- Centralized logging

---

## License

This project is for educational and portfolio purposes.
