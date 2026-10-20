# Project report and interview preparation

## 1. What was built

A complete local administrator web application for student records, courses, enrollments, attendance, results, dashboard analytics and academic reports. The deliverable includes editable source, an executable JAR, launch helpers, fictional demo data, tests, screenshots and documentation. It has not been deployed or pushed to GitHub.

## 2. Final technology stack

Java 17-compatible source, Jakarta Servlets 6, embedded Jetty 12, MongoDB with the official synchronous Java driver, Gson, BCrypt, HTML5, CSS3, vanilla JavaScript, Maven and JUnit. Charts are rendered with CSS/HTML; Chart.js was optional and is not needed. Python is only an optional integration-test runner, not part of the application runtime.

## 3. How much logic is Java?

All authoritative business logic is Java: login, sessions, validation, uniqueness, enrollment eligibility, deletion constraints, database access, server-side search/filter/pagination, attendance formulas, percentages, grades, dashboard calculations and CSV generation. JavaScript handles interaction and rendering. There is no meaningful exact percentage to assign to business logic; a line-count percentage would be misleading because UI templates and CSS do not represent business rules.

## 4–9. Architecture, collections and important classes

Browser → AuthFilter → module Servlet → Service → Repository → official MongoDB Driver → MongoDB.

Collections: `users`, `students`, `courses`, `enrollments`, `attendance`, `results`.

Servlets: `AuthServlet`, `DashboardServlet`, `StudentServlet`, `CourseServlet`, `EnrollmentServlet`, `AttendanceServlet`, `ResultServlet`; supporting `BaseServlet`, `CrudServlet`, `StaticServlet`.

Services: `AuthService`, `StudentService`, `CourseService`, `EnrollmentService`, `AttendanceService`, `ResultService`, `AnalyticsService`, `AcademicRules`; supporting `CrudService`, `RelatedService`.

Repositories: `UserRepository`, `StudentRepository`, `CourseRepository`, `EnrollmentRepository`, `AttendanceRepository`, `ResultRepository`, sharing `MongoRepository<T>`.

Other important Java classes: `Application`, `MongoDBConnection`, `DemoSeeder`, `Validator`, `PasswordUtil`, `Json`, `AppException`, the six model records and two enums. See [ARCHITECTURE.md](ARCHITECTURE.md) for responsibilities and endpoint details.

## 10–14. Folder structure, database setup, commands and login

The [README](../README.md) provides the full tree and MongoDB installation instructions. Open the extracted root in VS Code, start MongoDB, and run:

```powershell
# Terminal 1: only if MongoDB is not already running
powershell -ExecutionPolicy Bypass -File .\start-mongodb.ps1

# Terminal 2: run the bundled app
.\run.bat
```

Local URL: **http://localhost:8080**. Demo email: **admin@studentmanagement.local**. Demo password: **Admin@123**.

To rebuild after studying/editing the source:

```powershell
.\run.bat -Rebuild
```

## 15–16. Verification and limitations

See [VERIFICATION.md](VERIFICATION.md) for the final observed checks. This is a single-process local demo, not a production university system. It has one administrator role, a read-only administrator profile, no bulk imports or term-by-term historical model, and no external email or cloud services. Analytics and related-record selection favor code clarity over large-data scalability. The README describes those tradeoffs explicitly.

## 17. Recommended GitHub screenshots

Use the included dashboard, students, profile, courses, attendance and results screenshots. These show real seeded MongoDB records. Add a mobile screenshot to demonstrate responsive behavior. Avoid screenshots of real student data or MongoDB credentials.

## 18. Recommended repository description

Full-stack Student Management System built with Java Servlets, MongoDB, HTML, CSS and JavaScript, featuring student records, courses, attendance, results and dashboard analytics.

Suggested repository name: `student-management-system`.

## 19. Resume entry

**Student Management System | Java, Servlets, MongoDB, JavaScript**

- Built a full-stack student administration application using Java Servlets and MongoDB, with CRUD workflows for students, courses, enrollments, attendance and assessment results.
- Implemented BCrypt authentication, session-protected APIs, server-side validation and MongoDB unique indexes, with centralized Java logic for attendance percentages, grades and dashboard analytics.
- Created a responsive vanilla JavaScript interface with search, filters, server-side pagination, student profiles and CSV reports; validated the application with 30 JUnit tests and 65 HTTP integration checks.

These describe the delivered functionality. Study and adapt the code before presenting the implementation as work you can independently explain; do not claim production usage, scale or measured performance improvements that have not been demonstrated.

## 20. Five interview questions and answers

**1. Why did you choose Servlets instead of Spring Boot?**

Servlets let me practice HTTP request/response handling, filters, sessions and explicit dependency wiring directly. Jetty supplies the web container, while my services contain the rules. This matches my current Java/OOP learning goals. Spring Boot could simplify configuration later, but would not change the underlying HTTP and layered design concepts.

**2. What happens when a duplicate enrollment is submitted?**

The service first validates that the student and course exist and are active. The database has a unique compound index on `studentId` and `courseId`. If insertion violates it, the repository translates MongoDB's duplicate-key exception into a safe conflict response. The UI shows that message. The index protects uniqueness even when two requests race.

**3. How are authentication and sessions secured?**

Passwords are hashed with salted BCrypt. Login verifies the hash, replaces any old session, and stores only safe administrator details plus a random CSRF token. A filter checks protected requests. Mutations require the token and JSON content type, and cross-origin requests are rejected. Cookies are HttpOnly/SameSite, sessions expire after 30 minutes of inactivity, and logout invalidates the session. HTTPS/Secure cookies would be part of future deployment work.

**4. Why use references rather than embedding all academic records in a student?**

Attendance and results can grow continuously and are queried both by student and course. Separate collections with references prevent an unbounded student document and avoid copying course metadata into every record. Services validate relationships. This is a design choice based on access patterns, not an attempt to reproduce SQL tables automatically.

**5. How do you know your academic calculations are correct?**

`AcademicRules` centralizes the calculations and boundary rules. JUnit checks marks at grade thresholds, fractional percentages, impossible inputs, attendance at 75% and 90%, and no-record cases. Integration tests submit real records to the HTTP API and verify results/profile statistics. The frontend displays the server's calculation rather than computing a second version.

## 21. Complete Add Student flow

1. The administrator opens **Students → Add student**. A dialog collects personal and academic details.
2. Vanilla JavaScript sends JSON with `POST /api/students`, including the session cookie and CSRF header.
3. `AuthFilter` verifies the authenticated session, content type, origin and security token.
4. `StudentServlet`, through `CrudServlet`, parses the bounded JSON request and calls `StudentService.save`.
5. `StudentService` calls `Validator.student`. Java checks required fields, email, phone, semester, status, dates, and enrollment-number format. It normalizes the email and enrollment number.
6. `CrudService` assigns a server-generated UUID and UTC timestamps and constructs an immutable `Student` model. Client values cannot choose these metadata fields.
7. `StudentRepository`, through `MongoRepository`, converts the model to BSON and inserts it through the shared official MongoDB client.
8. MongoDB enforces the unique enrollment-number index. Duplicate input becomes HTTP 409; invalid input becomes HTTP 400. A valid insert returns HTTP 201 with the saved record.
9. JavaScript closes the dialog, displays a success toast, and fetches the current paginated student list. The data remains in MongoDB across browser refreshes and server restarts.

## Suggested learning order

Read `Student`, then `Validator`, `AcademicRules`, `StudentRepository`, `StudentService`, `CrudServlet`, `AuthFilter`, and finally `Application`. Follow one request in the browser network panel, set a breakpoint in `StudentService`, and change a small validation rule with a corresponding JUnit test. This makes the abstraction layers concrete.
