# CCHC Clinic Enterprise System (ITP4511)

An enterprise web application for the **Community Care Health Clinic (CCHC)** network, developed for HKIIT module **ITP4511 – Enterprise System Development**. The system manages clinics, appointments, queues, patient accounts, staff, and operational reporting.

## Modules

- **Patient portal** — registration, login, OTP verification, password reset, appointment booking, queue status, notifications, and profile management.
- **Staff / Doctor** — check-in, doctor schedule, queue board, appointment management, incident reporting.
- **Admin** — clinic management, service management, user management, batch import, audit trail, patient-behaviour analytics, and overall dashboards / reports.
- **Realtime** — Server-Sent Events (SSE) endpoints for live queue slot and notification updates.

## Architecture

Two front-ends share the same Jakarta EE backend (servlets + JSP for the classic webapp; the React SPA consumes the JSON API servlets).

```
ITP4511-Enterprise-system/
├── jakarta-webapp/           # Jakarta EE 10 backend + JSP views
│   ├── database/
│   │   └── cchc_clinic_db.sql
│   ├── src/main/java/hk/edu/hkiit/jakarta/webapp/
│   │   ├── bean/             # Domain beans / DTOs
│   │   ├── controller/       # Servlets (UI + JSON API + SSE)
│   │   ├── dao/              # Data access objects
│   │   ├── db/               # Connection / pool helpers
│   │   ├── filter/           # Auth / security filters
│   │   ├── resources/        # i18n + config resources
│   │   ├── tag/              # Custom JSP tags
│   │   └── util/             # Utilities (bcrypt, mail, csv, etc.)
│   ├── src/main/webapp/      # JSP views, CSS, JS, WEB-INF
│   └── pom.xml
├── react-frontend/           # React 19 + Vite SPA
│   └── src/{pages,components,services,context,hooks,utils}
├── docs/
└── reimport-db.bat           # Convenience script to reload MySQL schema
```

## Tech Stack

**Backend** — Jakarta EE 10 (Servlets, JSP, JSTL), Java 11, Maven (WAR packaging).

**Libraries** — MySQL Connector/J 8, Oracle JDBC (ojdbc5), jBCrypt (password hashing), Apache Commons CSV, Angus Mail (SMTP), Jackson (JSON).

**Frontend (SPA)** — React 19, Vite 7, React Router 7, Axios, Zustand (state), date-fns, react-hot-toast.

**Database** — MySQL 8 (schema: `cchc_clinic_db`).

**Server** — Designed to run on a Jakarta EE 10 container such as Apache Tomcat 10+ / GlassFish 7 / Payara 6.

## Getting Started

### Prerequisites

- JDK 11+
- Maven 3.8+
- MySQL 8 (e.g. via XAMPP — the included `reimport-db.bat` assumes `C:\xampp\mysql`)
- A Jakarta EE 10 container (Tomcat 10+, Payara 6, or GlassFish 7)
- Node.js 20+ (for the React frontend)

### 1. Initialise the database

On Windows with XAMPP MySQL running:

```bat
reimport-db.bat
```

Or manually:

```bash
mysql -u root < jakarta-webapp/database/cchc_clinic_db.sql
```

This drops and recreates the `cchc_clinic_db` schema and seeds clinic data.

### 2. Build & deploy the Jakarta webapp

```bash
cd jakarta-webapp
mvn clean package
```

Deploy the produced `target/jakarta-webapp-1.0.war` to your servlet container. The application will be available at `http://localhost:8080/jakarta-webapp/`.

Configure database credentials and SMTP settings via the resources under `src/main/java/hk/edu/hkiit/jakarta/webapp/resources/` (or the container's JNDI configuration) before deployment.

### 3. Run the React frontend (optional)

```bash
cd react-frontend
npm install
npm run dev
```

The Vite dev server serves the SPA, which calls the JSON API servlets (`*ApiServlet`, `*SseServlet`) exposed by the Jakarta backend.

## Key Endpoints (servlets)

- `LoginServlet`, `RegisterServlet`, `OtpServlet`, `ForgotPasswordServlet`, `ResetPasswordServlet`
- `AppointmentApiServlet`, `QueueApiServlet`, `ClinicApiServlet`, `NotificationApiServlet`, `PatientAccountApiServlet`
- `NotificationSseServlet`, `SlotSseServlet` (Server-Sent Events)
- Admin: `ClinicManagementServlet`, `UserManagementServlet`, `ServiceManagementServlet`, `BatchImportServlet`, `AuditTrailServlet`, `ReportServlet`, `OverallDashboardServlet`, `PatientBehaviorServlet`, `IncidentManagementServlet`

## Project

HKIIT — ITP4511 Enterprise System Development assignment (Maven artifact: `hk.edu.hkiit:jakarta-webapp:1.0`, project name *CCHC-ITP4511_Assignment*).
