# Vahan — Vehicle Marketplace

Production-ready marketplace to buy & sell cars, bikes, scooters and bicycles in India.

- Backend: Spring Boot 3.5 · Java 21 · PostgreSQL · Spring Security + JWT · Google OAuth2
- Frontend: React 18 · Vite 5 · Bootstrap 5 · React Query · React Hook Form
- Storage: Local filesystem (backend/uploads/)
- Docs: OpenAPI / Swagger UI
- Theme: Y Combinator inspired

## Prerequisites
Java 21+, Maven 3.9+, Node 18+, npm 9+, PostgreSQL 13+

## Setup
### 1. Database
    psql -U postgres
    CREATE DATABASE my_database;
    \q

### 2. Backend
    cd backend
    mvn clean install
    mvn spring-boot:run
API: http://localhost:8080
Swagger: http://localhost:8080/swagger-ui.html

Default admin: admin@ecom.local / Admin@12345 (change immediately)

### 3. Frontend
    cd frontend
    npm install
    npm run dev
App: http://localhost:5173

### 4. Google OAuth (optional)
1. https://console.cloud.google.com/apis/credentials
2. Create OAuth 2.0 Client ID (Web application)
3. Redirect URI: http://localhost:8080/login/oauth2/code/google
4. Paste into backend application.properties
5. Restart backend

### 5. Google Maps (optional)
Put key in frontend/.env as VITE_GOOGLE_MAPS_KEY=...

## Features
- Email + Google sign-in · JWT access/refresh
- Search by keyword, category, brand, location · sort · pagination
- Listings with 3–5 images · private Q&A · make-offer · wishlist · reviews · reports
- Admin dashboard: users, vehicles, reports, audit logs
- Fully responsive

## Security notes
- Forgot-password does NOT verify ownership (per spec). Add email/OTP before public use.
- Replace app.jwt.secret with a long random string.
