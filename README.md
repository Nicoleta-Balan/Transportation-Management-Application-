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

Use Case Diagram, UML Diagram, SQL Diagram, C4 Diagram
https://www.canva.com/design/DAG18YdZCG8/tD2iVf8larL_9PZJ8JawyA/edit?utm_content=DAG18YdZCG8&utm_campaign=designshare&utm_medium=link2&utm_source=sharebutton

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

This is the logical blueprint of how the system components interact.
```
+-------------------------------------------------------------+
| User (Dispatcher) [Actor]                                   |
|                                                             |
|   | Opens in browser                                        |
|   | via Port 5173                                           |
|   v                                                         |
| +---------------------------------------------------------+ |
| |   +-------------------------+                           | |
| |   | Frontend Application    |                           | |
| |   | (React)                 |                           | |
| |   | [Container]             |                           | |
| |   +-------------------------+                           | |
| |               |                                         | |
| |               | API Calls (HTTP/JSON)                   | |
| |               | to Port 8085                            | |
| |               v                                         | |
| |   +-------------------------+                           | |
| |   | Backend API             |                           | |
| |   | (Spring Boot App)       |                           | |
| |   | [Container]             |                           | |
| |   +-------------------------+                           | |
| |               |                                         | |
| |               | Reads/Writes data (using JDBC)          | |
| |               v                                         | |
| |   +-------------------------+                           | |
| |   | Database                |                           | |
| |   | (PostgreSQL)            |                           | |
| |   | [Container]             |                           | |
| |   +-------------------------+                           | |
| |                                                         | |
| +---------------------------------------------------------+ |
+-------------------------------------------------------------+
```

Deliverables

A REST-ful Spring Boot API for managing core operations (CRUD for Routes).
A persistent PostgreSQL database.
A containerized runtime environment using docker-compose.yml to run the entire stack (backend + db).
A Continuous Integration and Continuous Delivery (CI/CD) pipeline using GitHub Actions to automatically build and run the application on the dev server.
A frontend application (React) for user interaction.

Setup
Download Docker Desktop
Install:
Maven 
Node
Npm
JDK 22 (please install the version presented in the pom.xml file)

Steps:
Clone the repository
git clone [https://github.com/NicoletaChihaia/Transportation-Management-Application.git](https://github.com/NicoletaChihaia/Transportation-Management-Application.git)
cd Transportation-Management-Application

Build and start the containers:
Download Maven in order to run the docker compose command below (docker is necessary, otherwise it won't work)
docker compose up --build -d
The backend application will now be running at http://localhost:8085. The database is accessible at localhost:5438.

B. Frontend (React)
Navigate to the frontend directory named FrontEnd:
cd FrontEnd/vite-project

Install dependencies:
npm install

Start the development server:
npm run dev

The frontend application will be accessible at http://localhost:3000.

Open API: http://localhost:8085/api-docs<br>
Swagger: http://localhost:8085/swagger-ui/index.html