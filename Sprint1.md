Transport Management Application - Sprint 1

1.	The chosen topic<br><br>
      Web application for transport management that allows end users to view routes, timetables, login and book tickets, and make or cancel reservations. As an administrator, you can login and create, update, and delete routes, modify timetables, and manage client bookings and reservations. Moreover, administrator accounts can also manage associated resources and revenue reports.<br><br>
2.	The team<br>
-  Product Owner proxy: Ivan Iulia<br>
-  Tech Lead: Halip Gabriella-Ionella<br>
-  QA/DevOps: Chihaia Nicoleta (Balan)<br>
3.	Repo GitLab + Board Agile<br>
      https://github.com/NicoletaChihaia/Transportation-Management-Application-<br><br>
4.	Initial backlog (epic stories+ Gerkin)<br>
      The functional requirements have been grouped in the following three functional subdomains:<br>
      •	Routes and Timetables Management,<br>
      •	Booking and Reservations Management,<br>
      •	Financial & Cost Management.<br>

    Epic: Routes and Timetables Management

        Feature: Station Management
        Scenario: Adding a new station
        Given: I am the Administrator on the station management screen.
        When: I enter the station Name, a Description, and select the Status (active/inactive).
        Then: The station is created and appears in the list

        Feature: Standard Route Management
        Scenario: Creating a standard route
        Given: I am the Administrator and the necessary stations are defined.
        When: I define the Start station name, End station name, and the stations in the middle and their order.
        Then: the standard route is saved and can be assigned a timetable.

        Feature: Custom Route Search
        Scenario: Searching for a custom route.
        Given: I am a User on the search page.
        When: I define the desired start station and end station.
        Then: the system displays a custom route, including the estimated date and time for each station.

    Epic: Booking and Reservations Management

        Feature: Reservation Creation
        Scenario: Creating a new reservation
        Given: I am a logged-in User and the transport vehicle has enough seats. The
        When: I provide the start station name, start station departing time, end station, and number of seats.
        Then: the reservation is recorded, and the seats are allocated on the vehicle. And no payment is required at this time.

        Feature: Reservation Cancellation
        Scenario: User cancels reservation
        Given: I have an active reservation.
        When: I request to delete my reservation.
        Then: the reservation is deleted only if it is up to 24h before the start of the station departing time.

        Feature: Ticket Booking (Payment)
        Scenario: Creating a booking with payment
        Given: I am a logged-in User and the number of requested seats is available.
        When: I provide the booking details (stations, time, seats) and complete the payment.
        Then: the booking is created and confirmed, and the seats are secured.

    Epic: Financial & Cost Management

        Feature: Fare Policy Management
        Scenario: Defining a new fare policy
        Given: I am the Administrator on the fare policy screen.
        When: I define the route type, vehicle class, passenger category, and pricing rule.
        Then: the new fare policy is created and applied to ticket pricing.

        Feature: Revenue Reporting
        Scenario: Viewing a daily revenue report
        Given: I am the Administrator.  
        When: I request the revenue report for the current day, filtered by time range.
        Then: the system displays the total revenue based on confirmed bookings and tickets.

        Feature: Cost Recording
        Scenario: Recording a variable cost
        Given: I am the Administrator.
        When: I record the cost of Fuel (or Maintenance, etc.) associated with a specific Route or Vehicle.
        Then: the cost is saved, and the system computes key financial indicators (e.g., profit per trip).

5.	Pipeline CI/CD (build+test)
```text
+-------------------------------------------------------------------+
| Container: Backend API (Spring Boot App)                          |
|                                                                   |
|   [Frontend Application]                                          |
|     |                                                             |
|     | HTTP Request (ex: POST /api/routes)                         |
|     v                                                             |
|   +--------------------------+                                    |
|   | Route Controller         |                                    |
|   | (RouteController.java)   |                                    |
|   | [Component]              |                                    |
|   +--------------------------+                                    |
|       |                                                           |
|       | Calls functions (ex: .save(route))                        |
|       v                                                           |
|   +--------------------------+                                    |
|   | Route Repository         |                                    |
|   | (RouteRepository.java)   |                                    |
|   | [Component]              |                                    |
|   +--------------------------+                                    |
|       |       |                                                   |
|       |       | Uses/Maps                                         |
|       |       v                                                   |
|       |     +----------------+                                    |
|       |     | Route Entity   |                                    |
|       |     | (Route.java)   |                                    |
|       |     | [Component]    |                                    |
|       |     +----------------+                                    |
|       |                                                           |
|       | Executes SQL                                              |
|       v                                                           |
|   [Database (PostgreSQL)]                                         |
|                                                                   |
+-------------------------------------------------------------------+
```
6.	ADR 001<br>
      <br>ADR 001 - Technology Stack Selection<br>
      Context: The application must be a multi-tier system for transport management and must support future scalability. Backend must use Java.<br>
      <br>Options Analyzed:<br>
      •	Java + Spring Boot + PostgreSQL + React<br>
      •	.NET Core + SQL Server + Angular<br>
      •	Node.js + MongoDB + Vue.js<br>
      <br>Decision: We choose Java + Spring Boot + PostgreSQL + React.<br>
      <br>Justification:<br>
      •	Java is mandatory for the backend.<br>
      •	The stack is known by most of the team.<br>
      •	Easy integration for automated tests and CI/CD.<br>
      •	PostgreSQL is open-source and stable.<br>
      •	React provides flexibility for a modern UI.<br>


7.	Diagrama Use Case, Diagrama UML, Diagrama SQL, Diagrama C4
      https://www.canva.com/design/DAG18YdZCG8/tD2iVf8larL_9PZJ8JawyA/edit?utm_content=DAG18YdZCG8&utm_campaign=designshare&utm_medium=link2&utm_source=sharebutton