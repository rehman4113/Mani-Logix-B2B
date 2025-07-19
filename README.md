# 🏭 ManiLogix – Enterprise Supply Chain Management System

**ManiLogix** is a scalable, microservices-based backend system designed to manage B2B supply chain operations like supplier onboarding, inventory control, order processing, and role-based access via JWT. Built entirely using **Spring Boot Microservices**, it's modular, maintainable, and ready for frontend integration or deployment on cloud-native infrastructure.

---

## ✨ Features

- 🔐 **JWT Auth & Role-Based Access**: Secure login and signup flow for Admin, Manager, and Supplier roles.
- ⚙️ **Microservice Architecture**: Each module runs independently and communicates via REST APIs with Eureka service discovery and Gateway routing.
- 📦 **Inventory Control**: Add, update, delete, and track stock.
- 📑 **Supplier Management**: Onboard new suppliers and manage details.
- 📦 **Order Workflow**: Place, update, and manage the lifecycle of product orders.
- 🔔 **Notification System**: Trigger and track user-specific system events (via Notification service).
- 🔍 **Central API Gateway**: All traffic passes through the gateway for unified routing and security.
- 🔁 **Eureka Discovery**: Automatically registers all services, enabling fault-tolerant communication.

---

## 🧱 Microservices Included

| Module            | Description                                                       |
|-------------------|-------------------------------------------------------------------|
| ✅ Auth Service     | Signup, login, and token validation with Spring Security & JWT   |
| ✅ Inventory Service| Manages items/products stock with role restrictions              |
| ✅ Supplier Service | Supplier onboarding and profile management                       |
| ✅ Order Service    | Order creation, update, and tracking                             |
| ✅ Notification Service | Sends notifications (to be extended with async messaging)   |
| ✅ API Gateway      | Centralized routing for all services                             |
| ✅ Eureka Server    | Service registry and discovery                                   |
| ✅ Shared Library   | Contains shared DTOs and utilities used across services          |

---

## 👤 User Roles and Responsibilities

### 👑 Admin
- Full access to all modules
- Can view/add/update/delete suppliers, inventory, orders
- Manage users and assign roles

### 🧑‍💼 Manager
- Access to **Inventory** and **Order** modules
- Cannot modify suppliers
- Handles day-to-day logistics operations

### 🚚 Supplier
- View and update own supplier profile
- View orders assigned to them (cannot create new orders)

> ✅ Each role has a **dedicated dashboard** when frontend is integrated

---

## 🛠️ Technologies Used

**Backend Stack:**

- **Java 17**, **Spring Boot 3.x**
- **Spring Security + JWT**
- **Spring Cloud** (Eureka, Gateway, OpenFeign)
- **REST APIs**, **DTOs**, **Lombok**, **MapStruct**
- **MySQL** (or H2 for development)
- **MongoDB**
- **Maven** for dependency management

**Architecture:**

- 📦 Microservices (modular)
- 📡 API Gateway & Eureka
- 💬 REST Communication using Feign
- 🔐 Token validation across services
- 🧩 Shared libraries for DTO reuse

---

## 🚀 Getting Started

### 🧾 Prerequisites

- Java 17+
- Maven
- MySQL (or H2 for dev)
- Postman (optional, for testing)

### 🔧 Run Order

# Start Eureka
cd eureka-server
mvn spring-boot:run

# Start API Gateway
cd api-gateway
mvn spring-boot:run

# Start Auth Service
cd auth-service
mvn spring-boot:run

# Start Inventory, Supplier, Order, Notification (one by one)
cd inventory-service
mvn spring-boot:run

All APIs are accessible via Gateway:
http://localhost:8080

🌐 API Summary:

Module	Key Routes
Auth	/auth/signup, /auth/login, /auth/validate
Inventory	/inventory/add, /inventory/all, /inventory/{id}
Supplier	/suppliers/add, /suppliers/all, /suppliers/{id}
Order	/orders/create, /orders/all, /orders/status
Notification	/notifications/send, /notifications/user/{id}

All endpoints secured via Bearer Token.

📊 Suggested Frontend (Not Included):
   You can build a frontend (React, Angular, etc.) with:
   🔐 Login + Signup Pages
   🖥️ Separate Dashboards for:
      Admin (View/Add all entities)
      Manager (Inventory/Order)
      Supplier (View Profile & Orders)

🚀 Future Enhancements:
   Swagger/OpenAPI Docs
   Docker Compose for full environment spin-up
   Kafka Integration (for notifications and event handling)
   Frontend (React or Angular)
   Email/SMS Notifications
   Centralized Config Server

💻 Folder Structure:

manilogix/
│
├── eureka-server/
├── api-gateway/
├── auth-service/
├── inventory-service/
├── supplier-service/
├── order-service/
├── notification-service/
└── shared-library/

🧪 Example Use Case
✅ A Manager logs in via Auth Service
✅ Views or modifies inventory and order records
✅ Backend validates the token via Auth’s /validate
✅ Supplier receives notifications when assigned new orders
✅ Admin can create users and assign roles during signup

🤝 Contributing
Pull requests and ideas are welcome. Please ensure all services run independently and pass the test before submitting PRs.

📄 License
This project is licensed under the MIT License.
