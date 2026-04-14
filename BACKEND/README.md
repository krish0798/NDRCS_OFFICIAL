# NDRCS - Backend API Server ⚙️

This folder contains the core Spring Boot application that manages database persistence, dispatch logic, and session security for the NDRCS ecosystem.

## 🛠️ Tech Stack
- **Framework**: Java 21 / Spring Boot 3.2.x
- **Persistence**: Spring Data JPA / Hibernate
- **Database**: MySQL 8.0+
- **Security**: BCrypt Password Hashing
- **Email/OTPs**: Brevo REST API (Deployed) / Spring Mail (Local)

## 📂 Core Modules
- **Controllers**: Exposes REST endpoints for Auth, Citizens, Incidents, Rescue Teams, and Messaging.
- **Services**: Contains physical dispatch business logic (preventing double assignments, GPS zeroing, transaction state).
- **DTOs**: Data Transfer Objects controlling strict payload rules between the Java engine and frontend JS.
- **Seeder**: Automatically initializes 4 Control Room Admins and 4 Rescue Teams upon boot for immediate deployment testing.

## 🚀 Environment Setup & Running Locally

1. **Database Configuration**
   You must have local MySQL installed. Create a database named `ndrcs_official`.

2. **Properties File**
   Create a file inside `src/main/resources/` named `application-local.properties` (this is ignored by Git in the root folder):
   ```ini
   DB_URL=jdbc:mysql://localhost:3306/ndrcs_official
   DB_USERNAME=root
   DB_PASSWORD=your_password_here
   ```

3. **Execution**
   You can run this project using Maven from this directory:
   ```bash
   # Windows
   mvnw.cmd spring-boot:run
   
   # Mac/Linux
   ./mvnw spring-boot:run
   ```

*Note: For cloud deployments (like Railway), refer to the root folder README for mapping environment variables like `BREVO_API_KEY`.*
