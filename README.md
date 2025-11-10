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

This is the logical blueprint of how the system components interact.

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
JDK 22 (please install the version presented in the pom.xml file)

Steps:
Clone the repository
git clone [https://github.com/NicoletaChihaia/Transportation-Management-Application.git](https://github.com/NicoletaChihaia/Transportation-Management-Application.git)
cd Transportation-Management-Application

Build and start the containers:
Download Maven in order to run the docker compose command below (docker is necessary, otherwise it won't work)
docker compose up --build -d
The backend application will now be running at http://localhost:8085. The database is accessible at localhost:5438.

**Note**: The database schema is automatically initialized when the PostgreSQL container starts for the first time. The SQL scripts in the `database/` directory are executed automatically. See `database/INTEGRATION_GUIDE.md` for more details.

To load the sample data:

docker cp database/04_sample_data.sql tms_postgres_db:/tmp/04_sample_data.sql

docker compose exec db psql -U tms_user -d tms_db -f /tmp/04_sample_data.sql

B. Frontend (React)
Navigate to the frontend directory named FrontEnd:
cd FrontEnd/vite-project

Install dependencies:
npm install

Start the development server:
npm run dev

The frontend application will be accessible at http://localhost:5173.

API Endpoints (Work in Progress)
Routes (/api/routes)

GET /: Returns a list of all routes.
POST /: Creates a new route.
Body: { "originStationId": Long, "destinationStationId": Long, "vehicleCapacity": Integer }

Reservations (/api/reservations)

POST /: Creates a reservation with passenger contact details and category/class for pricing.
Body: {
  "routeId": Long,
  "passengerName": "String",
  "passengerEmail": "String",
  "passengerPhone": "String",
  "seatCount": Integer,
  "departureTime": "ISO timestamp",
  "arrivalTime": "ISO timestamp",
  "passengerCategory": "ADULT|CHILD|SENIOR|STUDENT",
  "vehicleClass": "STANDARD|COACH|MINI_BUS|DOUBLE_DECKER"
}
GET /: Returns all reservations including denormalized fare information (baseFare, vatAmount, totalFare).
PUT /{id}/cancel: Cancels a reservation and frees seats.

Route Timetables (/api/routes/{routeId}/timetables)

POST /: Creates a timetable for the route with optional entries.
GET /: Lists all timetables for the route.
POST /{timetableId}/entries: Adds one or more entries (day of week, departure/arrival times) to a timetable.

Analytics (/api/analytics)

GET /routes/{routeId}/availability: Returns current seat availability for the route.
GET /routes/{routeId}/statistics: Returns aggregated reservation and revenue statistics for the route.
GET /revenue?date=YYYY-MM-DD&endDate=YYYY-MM-DD&routeId=...&passengerCategory=...&vehicleClass=...:
    Returns revenue summaries filtered by date range, route, passenger category, and vehicle class.

## Database Integration

The application uses a comprehensive PostgreSQL database schema with:
- **Core Tables**: Stations, Routes, Fare Policies, Reservations
- **Timetables**: Route timetables and timetable entries for recurring schedules
- **Denormalized Tables**: Route Availability, Route Statistics, Revenue Summary
- **Temporality Support**: VAT rates and fare policies with historical tracking
- **Triggers**: Automatic validation and denormalization
- **Functions and Procedures**: Business logic in the database

For detailed database documentation, see:
- [Database README](database/README.md)
- [Database Integration Guide](database/INTEGRATION_GUIDE.md)
- [Database Quick Start](database/QUICK_START.md)