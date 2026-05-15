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
Spring Boot Backend API (Railway)
↓
FastAPI Face Recognition Service (Railway)
↓
MySQL Database (Railway)

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
- Railway deployment

---

## Tech Stack

- Java
- Spring Boot
- Spring Security
- JWT
- Hibernate / JPA
- MySQL
- Maven
- Railway

---

## Environment Variables

```env
DATABASE_URL=jdbc:mysql://your-host:3306/your-db
DATABASE_USERNAME=your-username
DATABASE_PASSWORD=your-password

JWT_SECRET=your-secret
JWT_EXPIRATION=43200000

PYTHON_SERVICE_URL=https://your-python-service.up.railway.app

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

The backend is deployed on Railway.

### Deployment Steps

1. Push repository to GitHub
2. Connect repository to Railway
3. Configure environment variables
4. Deploy

---

## Challenges Solved

- CORS configuration
- JWT authentication
- Environment variable management
- Microservice communication
- Base64 image handling
- Railway deployment debugging
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
