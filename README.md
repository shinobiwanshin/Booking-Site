# 🎟️ Event Ticket Booking Platform

A modern, full-stack event ticketing platform with secure authentication, real-time ticket validation, and comprehensive event management capabilities. Built with Spring Boot, React, and Keycloak for enterprise-grade security.

---

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Quick Start](#quick-start)
  - [Keycloak Setup](#keycloak-setup)
  - [Database Setup](#database-setup)
- [Running the Application](#-running-the-application)
  - [Backend](#backend)
  - [Frontend](#frontend)
- [API Documentation](#-api-documentation)
- [Authentication & Authorization](#-authentication--authorization)
- [Troubleshooting](#-troubleshooting)
- [Deployment](#-deployment)
- [License](#license)

---

## ✨ Features

### For Attendees

- 🎫 **Browse & Purchase Tickets**: Discover and buy tickets for published events
- 📱 **Digital Tickets**: Receive tickets with QR codes via email
- 📄 **PDF Downloads**: Download printable ticket PDFs
- 🔐 **Secure Authentication**: Login with email/password or OAuth (Google, GitHub)
- 📊 **Ticket Management**: View and manage purchased tickets

### For Organizers

- 📅 **Event Management**: Create, edit, and publish events
- 🎟️ **Ticket Types**: Configure multiple ticket types with pricing and availability
- ✅ **QR Code Validation**: Real-time ticket validation via QR scanner
- 📈 **Analytics Dashboard**: Track sales, revenue, and attendance
- 👥 **Attendee Management**: View and manage event attendees
- 📧 **Email Notifications**: Automated ticket delivery and confirmations

### Technical Features

- 🔒 **JWT-based Authentication**: Secure token-based auth with Keycloak
- 👮 **Role-Based Access Control**: ATTENDEE, ORGANIZER, and STAFF roles
- 🎨 **Modern UI/UX**: Responsive design with Tailwind CSS and shadcn/ui
- 📦 **RESTful API**: Well-structured backend API
- 🐘 **PostgreSQL Database**: Reliable data persistence
- 📮 **Email Integration**: SMTP-based email delivery
- 🔄 **Real-time Updates**: Live ticket availability tracking

---

## 🏗️ Architecture

```
┌─────────────────┐         ┌──────────────────┐         ┌─────────────────┐
│                 │         │                  │         │                 │
│  React Frontend │◄───────►│  Spring Boot API │◄───────►│   PostgreSQL    │
│   (Port 5173)   │         │   (Port 8080)    │         │   (Port 5433)   │
│                 │         │                  │         │                 │
└─────────────────┘         └──────────────────┘         └─────────────────┘
                                     │
                                     ▼
                            ┌──────────────────┐
                            │                  │
                            │    Keycloak      │
                            │   (Port 9090)    │
                            │                  │
                            └──────────────────┘
```

---

## 🛠️ Tech Stack

### Backend

- **Framework**: Spring Boot 3.4.4
- **Language**: Java 23
- **Build Tool**: Maven
- **Database**: PostgreSQL 16+
- **Authentication**: Keycloak (OAuth 2.0 / OIDC)
- **Security**: Spring Security with JWT
- **Email**: Spring Mail with SMTP
- **QR Code**: ZXing (Zebra Crossing)
- **PDF Generation**: Apache PDFBox
- **ORM**: Hibernate / JPA

### Frontend

- **Framework**: React 18
- **Language**: TypeScript
- **Build Tool**: Vite
- **Styling**: Tailwind CSS
- **UI Components**: shadcn/ui
- **Routing**: React Router
- **Auth**: react-oidc-context
- **HTTP Client**: Fetch API
- **Icons**: Lucide React

### DevOps

- **Containerization**: Docker & Docker Compose
- **Deployment**: Railway (Production)
- **Version Control**: Git / GitHub

---

## 🚀 Getting Started

### Prerequisites

Ensure you have the following installed:

- **Java 23** or higher ([Download](https://www.oracle.com/java/technologies/downloads/))
- **Node.js 18+** and npm ([Download](https://nodejs.org/))
- **PostgreSQL 16+** ([Download](https://www.postgresql.org/download/))
- **Keycloak 23+** ([Download](https://www.keycloak.org/downloads))
- **Maven 3.9+** (or use the included Maven wrapper)
- **Git** ([Download](https://git-scm.com/downloads))

### Quick Start

1. **Clone the repository**

   ```bash
   git clone https://github.com/shinobiwanshin/Booking-Site.git
   cd Booking-Site
   ```

2. **Start PostgreSQL**

   ```bash
   # Using Docker (recommended)
   docker run -d \
     --name postgres-tickets \
     -e POSTGRES_PASSWORD=changemeinprod! \
     -p 5433:5432 \
     postgres:16

   # Or start your local PostgreSQL instance on port 5433
   ```

3. **Start Keycloak**

   ```bash
   # Using Docker
   docker run -d \
     --name keycloak \
     -p 9090:8080 \
     -e KEYCLOAK_ADMIN=admin \
     -e KEYCLOAK_ADMIN_PASSWORD=admin \
     quay.io/keycloak/keycloak:23.0.0 \
     start-dev
   ```

4. **Configure Keycloak** (see [Keycloak Setup](#keycloak-setup) below)

5. **Start the Backend**

   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

6. **Start the Frontend**

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

7. **Access the Application**
   - Frontend: http://localhost:5173
   - Backend API: http://localhost:8080
   - Keycloak Admin: http://localhost:9090

### Keycloak Setup

1. **Access Keycloak Admin Console**

   - URL: http://localhost:9090
   - Username: `admin`
   - Password: `admin`

2. **Create a Realm**

   - Click "Create Realm"
   - Name: `event-ticket-platform`
   - Save

3. **Create a Client**

   - Go to Clients → Create Client
   - Client ID: `event-ticket-client`
   - Client Protocol: `openid-connect`
   - Enable "Standard Flow" and "Direct Access Grants"
   - Valid Redirect URIs: `http://localhost:5173/*`
   - Web Origins: `http://localhost:5173`
   - Save and note the Client Secret from the Credentials tab

4. **Create Realm Roles**

   - Go to Realm Roles → Create Role
   - Create three roles:
     - `ROLE_ATTENDEE`
     - `ROLE_ORGANIZER`
     - `ROLE_STAFF`

5. **Configure Identity Providers (Optional)**
   - Go to Identity Providers
   - Add Google and/or GitHub OAuth providers
   - Configure redirect URIs and client credentials

### Database Setup

The application uses Hibernate with `ddl-auto=update`, so tables will be created automatically on first run.

**Default Connection Details:**

- Host: `localhost`
- Port: `5433`
- Database: `postgres`
- Username: `postgres`
- Password: `changemeinprod!`

To use different credentials, set environment variables:

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5433/your_database
export DATABASE_USER=your_username
export DATABASE_PASSWORD=your_password
```

---

## 🎯 Running the Application

### Backend

```bash
cd backend

# Using Maven Wrapper (recommended)
./mvnw spring-boot:run

# Or with Maven installed
mvn spring-boot:run

# Or run the JAR directly
./mvnw clean package
java -jar target/tickets-0.0.1-SNAPSHOT.jar
```

**Backend will start on:** http://localhost:8080

**Health Check:** http://localhost:8080/actuator/health

### Frontend

```bash
cd frontend

# Install dependencies (first time only)
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

**Frontend will start on:** http://localhost:5173

---

## 📚 API Documentation

### Authentication Endpoints

| Method | Endpoint                    | Description               | Auth Required |
| ------ | --------------------------- | ------------------------- | ------------- |
| POST   | `/api/auth/register`        | Register new user         | No            |
| POST   | `/api/auth/login`           | Login with credentials    | No            |
| POST   | `/api/auth/forgot-password` | Request password reset    | No            |
| POST   | `/api/auth/reset-password`  | Reset password with token | No            |

### Event Endpoints

| Method | Endpoint                        | Description             | Auth Required   |
| ------ | ------------------------------- | ----------------------- | --------------- |
| GET    | `/api/v1/published-events`      | List published events   | No              |
| GET    | `/api/v1/published-events/{id}` | Get event details       | No              |
| GET    | `/api/v1/events`                | List organizer's events | Yes (ORGANIZER) |
| POST   | `/api/v1/events`                | Create new event        | Yes (ORGANIZER) |
| PUT    | `/api/v1/events/{id}`           | Update event            | Yes (ORGANIZER) |
| DELETE | `/api/v1/events/{id}`           | Delete event            | Yes (ORGANIZER) |

### Ticket Endpoints

| Method | Endpoint                                                 | Description         | Auth Required |
| ------ | -------------------------------------------------------- | ------------------- | ------------- |
| GET    | `/api/v1/tickets`                                        | List user's tickets | Yes           |
| GET    | `/api/v1/tickets/{id}`                                   | Get ticket details  | Yes           |
| GET    | `/api/v1/tickets/{id}/qr-codes`                          | Get ticket QR code  | Yes           |
| GET    | `/api/v1/tickets/{id}/pdf`                               | Download ticket PDF | Yes           |
| POST   | `/api/v1/events/{eventId}/ticket-types/{typeId}/tickets` | Purchase ticket     | Yes           |
| POST   | `/api/v1/ticket-validations`                             | Validate ticket     | Yes (STAFF)   |

### Dashboard Endpoints

| Method | Endpoint                    | Description             | Auth Required   |
| ------ | --------------------------- | ----------------------- | --------------- |
| GET    | `/api/v1/dashboard/summary` | Get organizer analytics | Yes (ORGANIZER) |

---

## 🔐 Authentication & Authorization

### Authentication Flow

1. **Standard Login (Username/Password)**

   - User submits credentials to `/api/auth/login`
   - Backend validates with Keycloak
   - Returns JWT access token and refresh token
   - Frontend stores tokens in localStorage
   - Subsequent requests include `Authorization: Bearer <token>` header

2. **OAuth Login (Google/GitHub)**
   - User clicks OAuth provider button
   - Redirected to Keycloak → Provider
   - Provider authenticates and redirects back
   - Keycloak issues tokens
   - Frontend receives and stores tokens

### User Roles

- **ATTENDEE**: Can browse events and purchase tickets
- **ORGANIZER**: Can create and manage events, view analytics
- **STAFF**: Can validate tickets at events

### Protected Routes

Frontend uses route guards to protect pages:

- `/dashboard/*` - Requires authentication
- `/dashboard/events/*` - Requires ORGANIZER role
- `/dashboard/validate` - Requires STAFF role

---

## 🔧 Troubleshooting

### Login Issues

**Problem:** "Account is not fully set up" error

**Solution:** Ensure the Keycloak user has both `firstName` and `lastName` set:

```bash
# Get admin token
curl -X POST 'http://localhost:9090/realms/master/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password&client_id=admin-cli&username=admin&password=admin'

# Update user (replace USER_ID and TOKEN)
curl -X PUT 'http://localhost:9090/admin/realms/event-ticket-platform/users/{USER_ID}' \
  -H 'Authorization: Bearer {TOKEN}' \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"John","lastName":"Doe","email":"user@example.com","emailVerified":true,"enabled":true}'
```

### Database Connection Issues

**Problem:** Cannot connect to PostgreSQL

**Solutions:**

- Verify PostgreSQL is running: `pg_isready -h localhost -p 5433`
- Check credentials in `backend/src/main/resources/application.properties`
- Ensure firewall allows port 5433
- Check Docker container: `docker ps | grep postgres`

### Keycloak Connection Issues

**Problem:** Backend cannot connect to Keycloak

**Solutions:**

- Verify Keycloak is running: `curl http://localhost:9090`
- Check Keycloak realm and client configuration
- Verify client secret matches in `application.properties`
- Check Keycloak logs: `docker logs keycloak`

### Email Issues

**Problem:** Password reset emails not sending

**Solutions:**

- Configure SMTP settings in `application.properties`:
  ```properties
  spring.mail.username=your-email@gmail.com
  spring.mail.password=your-app-password
  ```
- For Gmail, create an [App Password](https://support.google.com/accounts/answer/185833)
- Check email logs in backend console

---

## 🚢 Deployment

### Railway Deployment (Production)

This project is configured for Railway deployment.

**Backend:**

```bash
cd backend
railway login
railway link
railway up
```

**Frontend:**
Configure build settings:

- Build Command: `cd frontend && npm install && npm run build`
- Start Command: `cd frontend && npm run preview`

**Environment Variables:**
Set these in Railway dashboard:

- `DATABASE_URL`
- `KEYCLOAK_ISSUER_URI`
- `KEYCLOAK_ADMIN_URL`
- `FRONTEND_URL`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`

### Docker Deployment

```bash
# Build and run with Docker Compose
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

---

## 📝 Environment Variables

### Backend (.env or application.properties)

```properties
# Database
DATABASE_URL=jdbc:postgresql://localhost:5433/postgres
DATABASE_USER=postgres
DATABASE_PASSWORD=changemeinprod!

# Keycloak
KEYCLOAK_ISSUER_URI=http://localhost:9090/realms/event-ticket-platform
KEYCLOAK_ADMIN_URL=http://localhost:9090
KEYCLOAK_REALM=event-ticket-platform
KEYCLOAK_CLIENT_ID=event-ticket-client
KEYCLOAK_CLIENT_SECRET=your-client-secret
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=admin

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Frontend URL
FRONTEND_URL=http://localhost:5173

# Server
PORT=8080
```

### Frontend (.env)

```bash
VITE_API_URL=http://localhost:8080
VITE_KEYCLOAK_URL=http://localhost:9090
VITE_KEYCLOAK_REALM=event-ticket-platform
VITE_KEYCLOAK_CLIENT_ID=event-ticket-client
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄

License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👥 Authors

- **Amitabha Nath** - [@shinobiwanshin](https://github.com/shinobiwanshin)

---

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) - Backend framework
- [React](https://reactjs.org/) - Frontend library
- [Keycloak](https://www.keycloak.org/) - Identity and access management
- [shadcn/ui](https://ui.shadcn.com/) - UI component library
- [Tailwind CSS](https://tailwindcss.com/) - CSS framework
- [Railway](https://railway.app/) - Deployment platform

---

## 📞 Support

For support, email supersaiyan2k03@gmail.com or open an issue in the GitHub repository.

---

**⭐ Star this repository if you find it helpful!**
