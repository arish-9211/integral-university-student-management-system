# Architecture and API guide

## Java responsibilities

`Application` creates a single reusable `MongoClient`, repositories, services, and servlets. It binds Jetty to loopback (`127.0.0.1`) and closes the server/client on shutdown. Static assets are packaged inside the same executable JAR.

| Package / classes | Responsibility |
| --- | --- |
| `model`: Student, Course, Enrollment, Attendance, Result, User | Immutable Java records. `StudentStatus` and `AttendanceStatus` define allowed states. Records are regular Java objects with compiler-generated private final fields, accessors and constructors. |
| `servlet`: AuthServlet, DashboardServlet, StudentServlet, CourseServlet, EnrollmentServlet, AttendanceServlet, ResultServlet | Module entry points. `CrudServlet` shares HTTP method mapping. `BaseServlet` translates safe errors to JSON; `StaticServlet` serves packaged web files. |
| `service`: AuthService, StudentService, CourseService, EnrollmentService, AttendanceService, ResultService | Authentication and module rules. `CrudService` owns CRUD orchestration. `RelatedService` validates student/course references and decorates display names. |
| `service`: AcademicRules, AnalyticsService | Central grade/attendance rules; dashboard, profile, report and CSV composition. |
| `repository`: UserRepository, StudentRepository, CourseRepository, EnrollmentRepository, AttendanceRepository, ResultRepository | Collection names and indexes. `MongoRepository<T>` owns generic persistence and translates duplicate-key failures to HTTP 409. |
| `database`: MongoDBConnection, DemoSeeder | Shared driver/client configuration and optional insert-only demo seeding. |
| `validation`: Validator | Required fields, email/phone, dates, allowed values, ranges and marks validation. |
| `filter`: AuthFilter | Protected routes, CSRF token validation, same-origin mutation checks, JSON content-type requirement and security headers. |
| `util`: Json, PasswordUtil | JSON serialization, bounded request parsing, BCrypt hashing and checks. |
| `exception`: AppException | Safe error message and HTTP status. Technical failures are logged by exception class without exposing connection strings or stack traces to users. |

Inheritance is used where behavior really repeats (CRUD services, repositories, servlets). There is no invented inheritance hierarchy among Student, Course and User. Java collections (`List`, `Map`, `ArrayList`, `TreeMap`) represent query results, grouped chart data and reports.

## HTTP contract

| Route | Methods | Meaning |
| --- | --- | --- |
| `/api/auth/login` | POST | Validate credentials, invalidate old session, create session and CSRF token |
| `/api/auth/me` | GET | Current administrator and CSRF token |
| `/api/auth/logout` | POST | Invalidate session |
| `/api/students`, `/api/courses`, `/api/enrollments`, `/api/attendance`, `/api/results` | GET, POST | Paginated query or creation |
| Same collection route + `/{id}` | GET, PUT, DELETE | Detail, full validated update, deletion |
| `/api/dashboard` | GET | Live counts, grouped charts, recent records |
| `/api/profile?studentId={id}` | GET | Student details, course attendance, results |
| `/api/programs` | GET | MongoDB distinct program values for the student filter |
| `/api/reports` | GET | Per-student attendance/marks summary |
| `/api/reports.csv` | GET | CSV download generated in Java |

Enrollment updates are deliberately rejected: remove and re-create a relationship when it has no dependent academic history. All collection lists accept `page` (1-based), `size` (1–100), whitelisted `sort` and `direction`. Student default size is 10. Query values are validated; regex search input is quoted. Multiword search matches words across the permitted fields.

Protected requests require the session cookie. Mutations require `Content-Type: application/json` and `X-CSRF-Token` (except login, which uses same-origin/JSON checks). Error responses are `{"error":"safe explanation"}`. Expected statuses: 200/201, 400 validation, 401 unauthenticated, 403 CSRF/origin, 404 missing, 405 method, 409 duplicate/dependency conflict, 413 oversized body, 415 wrong content type, 429 login throttling, 500 unexpected failure.

## Document design

Students and courses have independent lifecycles. Enrollments, attendance and results reference them with UUIDs. Embedding all attendance and results into Student would grow the document indefinitely and complicate course-wide queries. Six focused collections fit the read/write requirements without imitating relational joins everywhere.

| Collection | Fields beyond UUID/internal `_id` | Main uniqueness |
| --- | --- | --- |
| users | name, email, passwordHash | email |
| students | enrollmentNumber, firstName, lastName, gender, dateOfBirth, email, phone, program, semester, address, admissionDate, status, createdAt, updatedAt | enrollmentNumber |
| courses | courseCode, courseName, description, credits, semester, status, createdAt, updatedAt | courseCode |
| enrollments | studentId, courseId, createdAt, updatedAt | studentId + courseId |
| attendance | studentId, courseId, date, status, createdAt, updatedAt | studentId + courseId + date |
| results | studentId, courseId, examName, examDate, maximumMarks, obtainedMarks, percentage, grade, createdAt, updatedAt | studentId + courseId + examName + examDate |

Dates are validated ISO date strings; audit timestamps are UTC `Instant` strings. The UI formats them for display. This keeps the student-level driver mapping explicit and understandable. The MongoDB driver's BSON documents are confined to the repository layer.

## Security and operational choices

Passwords use BCrypt work factor 12. Successful login replaces the prior session; logout invalidates it. Cookies are HttpOnly and SameSite=Strict. Because the server deliberately uses local HTTP, `Secure` is not set; any future HTTPS deployment would need this adjustment. Failed login is throttled per remote address at 10 attempts per five-minute window, in process memory.

JSON bodies are bounded to 32 KiB. Client-supplied calculated marks/grade and IDs/timestamps are overwritten. User text is HTML-escaped before insertion into templates. CSV cells beginning with formula characters receive a leading apostrophe. CSP restricts scripts/resources to the local origin; inline styles are permitted for computed chart dimensions.

Write operations share a Java monitor in one server process, avoiding a race between relationship checks and deletes. Unique indexes independently protect duplicates. External direct database writes and multiple app instances are outside this local guarantee. There is no multi-document transaction or automatic cascade deletion.
