# 🚀 Real-Time Kanban Backend

Backend service for a real-time collaborative Kanban board built with Spring Boot.

Designed to handle **concurrent users, real-time updates, and data consistency** using WebSockets and transactional guarantees.

---

## ⚙️ Key Features

- JWT-based authentication (stateless)
- Role-based board access (ADMIN / MEMBER)
- Real-time updates via WebSockets (STOMP)
- Task & column CRUD with position-based ordering
- Concurrency-safe drag & drop using pessimistic locking
- Board membership and collaboration system
- Activity tracking with pagination support

---

## 🧱 Architecture

- Layered architecture: Controller → Service → Repository
- DTO-based request/response handling
- Global API response wrapper
- Centralized exception handling
- Transactional service layer

---

## 🛠️ Tech Stack

- Spring Boot  
- Spring Security  
- Spring WebSocket (STOMP)  
- JPA / Hibernate  
- PostgreSQL  
- JWT Authentication  

---

## 🔌 API Overview

- `/api/auth` → Authentication (login/signup)  
- `/api/boards` → Board management  
- `/api/columns` → Column operations  
- `/api/tasks` → Task operations  
- `/api/activities` → Activity tracking  

---

## ⚡ Real-Time

- WebSocket endpoint: `/ws-kanban`
- Topics:
  - `/topic/board/{id}` → Board updates  
  - `/topic/board/{id}/activity` → Activity feed  
  - `/topic/board/{id}/presence` → Online users  
  - `/topic/board/{id}/cursor` → Live cursors  

---

## 🧪 Running Locally

```bash
git clone https://github.com/ChinthaRithwik/kanban-backend.git
cd backend
./mvnw spring-boot:run
