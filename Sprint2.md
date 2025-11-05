## Sprint 2 - Architecture and Domain Implementation Summary

### Domain Model Implementation

#### Domain Entities
- **Core (Transportation) Domain**: `Reservation`, `Route`, `Station`, `TripTimeDetails` (VO)
- **Finance Subdomain**: `Invoice`, `Payment`, `OperationalCost`, `TaxRate`

##### Entities and relationships
- **Route**: Many-to-One with `Station` for `originStation` and `destinationStation`
- **Reservation**: Many-to-One with `Route`; embeds `TripTimeDetails`
- **Invoice**: Optional reference to Reservation by `reservationId`
- **Payment**: references `Invoice` by `invoiceId`

##### Value Objects
- **Embeddables**: `TripTimeDetails` (departureTime, arrivalTime)
- **Enums**: (Extensible) Status fields currently modeled as Strings, can be evolved to Enums (e.g., `ReservationStatus`, `InvoiceStatus`, `PaymentMethod`)
- **VO Custom Data Types**: Jakarta Bean Validation annotations for strong typing and constraints across entities

#### Entity Aggregates
- **Aggregate: Route**
  - **Root Entity**: `Route`
  - **Components**: `Station` (associated via references)
  - **Associations within aggregate**: origin/destination associations are Many-to-One
- **Aggregate: Reservation**
  - **Root Entity**: `Reservation`
  - **Components**: `TripTimeDetails` (embedded VO)
  - **Associations within aggregate**: Reservation → Route (Many-to-One)
- **Aggregate: Finance**
  - **Root Entities**: `Invoice`, `TaxRate`
  - **Components**: `Payment` associated to `Invoice`; `OperationalCost` standalone entries

### Business Rules Implementation

#### Business Workflow Rules
- **Business Workflow (Use-case) Services**
  - Core:
    - `RouteService` (create route from DTO, find routes)
    - `StationService` (create station, list stations)
    - `ReservationService` (create, cancel, list, find)
  - Finance:
    - `FinanceService`
      - UC1 Generate Client Invoice: `generateClientInvoice`
      - UC2 Process Payment: `processPayment` (updates invoice status → PAID)
      - UC3 Track Operational Costs: `recordOperationalCost`
      - UC4 Review Financial Reports: `getInvoicesForPeriod`, `getPaymentsForPeriod`
      - UC5 Manage Tax Rates: `upsertTaxRate`, `listTaxRates`

#### Validation Rules
- **JavaBean Validation Rules (annotations in Domain Entities)**
  - `@NotNull`, `@NotBlank`, `@Size`, `@Min` applied to entity fields (e.g., `Station.name`, `Reservation.seatCount`, `TripTimeDetails` times, finance amounts)
- **Domain Validation Services**
  - Validation currently performed via Bean Validation + service-level checks (e.g., distinct origin/destination in `RouteServiceImpl`)

#### Computation Rules
- **Domain Computation Services for Entity Fields**
  - Example: `ReservationServiceImpl` constructs `TripTimeDetails` from DTO; default reservation status set on creation
  - Finance totals currently direct from request; future enhancement could compute tax-inclusive amounts via `TaxRate`
- **Domain Computation Services for Entity Views**
  - Finance reports aggregate lists for a given period (invoices, payments)

#### Other Business Rules
- **Invoice state transition** on payment (ISSUED → PAID)
- **Route creation guard** disallowing identical origin and destination

### Deliverables

#### Entity Classes
- Core: `Route`, `Station`, `Reservation`, `TripTimeDetails`
- Finance: `Invoice`, `Payment`, `OperationalCost`, `TaxRate`

#### Entity Support Services Interfaces and Implementation Classes
- **Repositories (Spring Data JPA)**
  - Core: `RouteRepository`, `StationRepository`, `ReservationRepository`
  - Finance: `InvoiceRepository`, `PaymentRepository`, `OperationalCostRepository`, `TaxRateRepository`
- **Aggregate Entity Factories**
  - Realized within service implementations (constructors and assembly logic)

#### Business Services Classes Interfaces and Implementation Classes
- **Workflow Services**
  - Core: `RouteService`/`RouteServiceImpl`, `StationService`/`StationServiceImpl`, `ReservationService`/`ReservationServiceImpl`
  - Finance: `FinanceService`/`FinanceServiceImpl`
- **Validation Service**
  - Bean Validation via Jakarta annotations; service-level validations (e.g., route station checks)
- **Computation Services**
  - Finance reporting period filters; defaulting of statuses
- **Audit Services**
  - Not yet implemented; can be added via AOP or listeners

#### Spring App Configuration Class
- Standard Spring Boot auto-configuration; components scanned under `multitier.trans.*` packages

#### JUnit Test Classes
- **Test for Validation Service**
  - `StationControllerTest` validates entity constraints via controller endpoint
- **Test for Computation Services**
  - Implicit in service/controller tests (e.g., default statuses)
- **Test for Workflow Services**
  - `ReservationControllerTest` covers create/cancel flows via HTTP

### Layered Architecture (current modules)
- **Domain**: `multitier.trans.domain.*`
- **Application**: `multitier.trans.application.*`
- **Interfaces (API)**: `multitier.trans.interfaces.*`

### Endpoints (selected)
- Core: `/api/stations`, `/api/routes`, `/api/reservations`
- Finance: `/api/finance/invoices`, `/api/finance/payments`, `/api/finance/costs`, `/api/finance/reports`, `/api/finance/tax-rates`


