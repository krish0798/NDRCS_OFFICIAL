# National Disaster Response Control System (NDRCS) 🚑🌐

![NDRCS Status](https://img.shields.io/badge/Status-Live_Production-success) ![Architecture](https://img.shields.io/badge/Architecture-Spring_Boot_%7C_Vanilla_JS-blue)

NDRCS is a comprehensive, three-tier disaster management architecture designed to streamline emergency responses, dispatch rescue teams, and collect real-time citizen incident reports.

### 🔴 **[Live Application Portal](https://ndrcs-krish-official.netlify.app)**

---

## 🎯 Core Features & Architecture

The system perfectly mirrors physical emergency dispatch mechanics.

1. **Citizen Portal (Public)**
   - Secure Registration & Email Verification (via Auth & Brevo API).
   - Real-time incident reporting with HTML5 Geolocation scraping.
   
2. **Control Room Dashboard (Command Center)**
   - Unified overview of all active, resolved, and assigned incidents.
   - Dispatch capabilities to assign specific incidents to available rescue units.
   - Live bidirectional messaging pipeline with field operatives.

3. **Rescue Team GPS Portal (Field Operatives)**
   - Continuous GPS telemetry polling and live tracking on operational maps.
   - Immediate receipt of Control Room directives.
   - One-click incident resolution and SOS status updates.

---

## 👨‍💻 About The Project & Academic Context

*“Vibe Coded” and Built for Impact.*

This project was developed by **Krish Jaiswar**, a 2nd-year student at **Mumbai University**. 
It was architected specifically for the **Idea Lab** semester subject, which focuses on solving United Nations Sustainable Development Goals (UN SDGs) and tackling real-world problem statements through innovative tech prototypes.

- **Architecture & Design**: Database schema, system data flow, and architecture ideology designed and tested entirely by myself.
- **Backend Coding**: Code generation pairing with ChatGPT (Spring Boot, MySQL).
- **Frontend Coding**: Code generation pairing with Antigravity (Vanilla JS, Netlify, APIs).
- **Deployment & DevOps**: Cloud hosting configuration, SQL linkage, Netlify pushes, and Railway operations manually executed and tested by me.

> [!WARNING]
> **Important Note regarding Live Links:** Because the backend is hosted on Railway's free tier, the live SQL database and APIs may go offline approximately **one month later** after exhausting free usage. If the live site is down, please refer to the attached architecture documentation! A master report file will also be added to this GitHub repo shortly.

---

## 🏗️ Technical Stack

This repository holds the mono-repo structure containing both the API servers and the User Interface.

### The Backend (`/BACKEND/ndrcs-backend/`)
- **Framework**: Java Spring Boot (v3.x)
- **Database**: MySQL / Spring Data JPA
- **Authentication**: Custom three-tier localized session handling.
- **External APIs**: Brevo REST API (for transactional emails/OTPs).
- **Hosting**: Railway (`ndrcs-backend-production.up.railway.app`)

### The Frontend (`/FRONTEND/`)
- **Structure**: Vanilla HTML5, CSS3, and JavaScript.
- **Mapping**: Leaflet.js / OpenStreetMap API.
- **Networking**: Abstracted DRY `fetch()` wrapper via `api.js`.
- **Hosting**: Netlify (`ndrcs-krish-official.netlify.app`)

---

## 🔒 Security Configuration (Important for Contributors)

This repository strictly enforces `.gitignore` rules to prevent credential leakage. 
If you are cloning this repository to work locally:

1. You must manually create `BACKEND/ndrcs-backend/application-local.properties`.
2. Add your local MySQL strings:
   ```ini
   DB_URL=jdbc:mysql://localhost:3306/ndrcs_official
   DB_USERNAME=root
   DB_PASSWORD=your_local_password
   ```

*Note: Production variables (Brevo Keys, Railway MySQL strings) are stored as cloud reference variables and not exposed to the repository.*

---

## 🚀 Realistic Future Upgrades & Known Technical Debts

Since this project was rapidly prototyped for an academic deployment, there are several "technical debts" intentionally left in the codebase that should be resolved before scaling to thousands of concurrent users. These are excellent targets for future open-source contributors or version 2.0:

### 1. The Database N+1 Memory Flaw (JPA Optimization)
Currently, several Service layer methods (e.g., `IncidentService`, `AuthService`) fetch user profiles by utilizing `.findAll().stream().filter(...)`. 
* **The Risk:** At 1,000+ users, this will download the entire SQL table into RAM and cause an Out-Of-Memory (OOM) crash.
* **The Fix:** Refactor the JPA Repositories to use localized queries like `findByAuthUserId(Long id)` to push the filtering logic directly to the MySQL engine level.

### 2. Transition from State-less Credential Passing to JWTs 
Currently, the frontend continuously passes raw `password` strings over HTTPS for sensitive POST requests. 
* **The Risk:** Computationally heavy. The servers must compute `passwordEncoder.matches()` (BCrypt hashing) on every single API ping (e.g., live GPS updates).
* **The Fix:** Implement standard Spring Security JSON Web Tokens (JWT). The frontend should exchange credentials for a token at `/api/auth/login`, and pass an `Authorization: Bearer <token>` header to bypass BCrypt checking on frequent pings.

### 3. Orphaned Data & `@Transactional` Missing Constraints
In `IncidentService.java`, the system saves an `Incident` to the database, and sequentially saves a `CitizenReport` to bind it to the citizen. 
* **The Risk:** It currently lacks the `@Transactional` wrapper. If the connection drops between those two lines of code, an "orphaned" incident will be spawned with no citizen attached causing dashboard anomalies.
* **The Fix:** Simply annotate the mutating service methods with `@Transactional` for clean rollbacks.

### 4. Unified Control Room Inbox (Intentional Design vs Scalability)
Currently, in the `control-dashboard.js`, the messaging inbox fetches messages specifically for `myProfileId = 1`. 
* **The Design Choice:** This was done intentionally! In a high-stakes dispatch environment, a unified shared inbox prevents "internal tension" or siloed information. If one admin logs off, the next admin sees the exact same critical messages.
* **The Future Upgrade:** Instead of hardcoding `1` on the client-side to achieve this, the 2.0 backend should introduce a `Shared Group Inbox` REST logic so any admin can query the group feed securely.

---
## 🌟 Final Thoughts

*"Technology is best when it brings people together to protect one another."*

Thank you for exploring the NDRCS Prototype! This project stands as a testament to the fact that when driven by real-world problem statements—like the UN Sustainable Development Goals—students and developers can architect vital, life-saving systems. 

To the incredible future contributors, professors, and reviewers evaluating this architecture: The framework is built, the deployment is live, and the foundation is solid. Let’s keep innovating for a safer tomorrow. 

**Happy Coding! - Krish Jaiswar**

*Architecture flowcharts and project scopes are attached sequentially in the `PROJECT_DOCS` folder.* 
