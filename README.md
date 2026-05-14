Transportation-Management-Application

Overview
The Transportation Management System (TMS) is a web-based platform designed to simplify and automate passenger transport operations.
The application enables users to search and book available routes, manage their reservations, and view trip details.
Administrators can create and manage routes, vehicles, and drivers while tracking ticket sales, revenue, and route performance.

Technical Stack
Backend: Java 22 (Spring Boot 3)
Frontend: React (Vite)
Database: PostgreSQL 15
Deployment: Docker (Docker Compose)
CI/CD: GitHub Actions

Objectives
Improve efficiency in route and booking management
Provide real-time access to transport data
Support economic decision-making through revenue reports
Implement a layered architecture (Presentation, Business Logic, Data Access)

System Architecture
The application follows a three-tier architecture:
Frontend (Presentation Layer) – React.js interface for passengers and admins (Port 5173)
Backend (Business Logic Layer) – Java Spring Boot REST API (Port 8085)
Database (Data Layer) – PostgreSQL for persistent data storage (Port 5438)

C4 Diagram (Level 2: Containers)

Deliverables

A REST-ful Spring Boot API for managing core operations (CRUD for Routes).
A persistent PostgreSQL database.
A containerized runtime environment using docker-compose.yml to run the entire stack (backend + db).
A Continuous Integration and Continuous Delivery (CI/CD) pipeline using GitHub Actions to automatically build and run the application on the dev server.
A frontend application (React) for user interaction.

Setup
There are two ways to run the project:
Method 1: Run with Docker (Recommended)
This is the simplest and fastest method. It will automatically start the backend and the database together.

Requirements:
Docker and Docker Compose installed.

Steps:
Clone the repository
git clone [https://github.com/NicoletaChihaia/Transportation-Management-Application.git](https://github.com/NicoletaChihaia/Transportation-Management-Application.git)
cd Transportation-Management-Application


Build and start the containers:
docker compose up --build -d
The backend application will now be running at http://localhost:8085. The database is accessible at localhost:5438.

Method 2: Manual Run (Local Development)
A. Backend (Spring Boot)
Open the project in an IDE (e.g., IntelliJ IDEA).
Ensure you have a PostgreSQL instance running locally and configure src/main/resources/application.properties with your connection details.
Run the application using your IDE or via Maven:
mvn spring-boot:run


B. Frontend (React)
Navigate to the frontend directory named FrontEnd:
cd FrontEnd

Install dependencies:
npm install

Start the development server:
npm run dev

The frontend application will be accessible at http://localhost:5173.

API Endpoints (Work in Progress)
Routes (/api/routes)

GET /: Returns a list of all routes.
POST /: Creates a new route.
Body: { "originCity": "String", "destinationCity": "String", "vehicleCapacity": "Integer" }
