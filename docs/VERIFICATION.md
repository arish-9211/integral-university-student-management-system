# Verification report

Verified locally on **20 October 2026**. No deployment or GitHub publication was performed.

## Build and automated tests

- Clean Maven build: **BUILD SUCCESS**.
- JDK used: **25.0.1**, compiling with Java **17** release compatibility.
- Maven: **3.9.11**.
- Actual MongoDB Community server, isolated database on loopback port 27029.
- Packaged executable JAR started successfully with embedded Jetty.
- **30 JUnit tests passed**, zero failures/errors/skips:
  - 18 academic calculation tests.
  - 11 validation tests.
  - 1 BCrypt test including salt uniqueness and valid/invalid password assertions.
- **65 HTTP checks plus response assertions passed** against the final JAR. The checked-in `scripts/smoke-test.py` reproduces these checks.
- A browser-created student was edited, the application and database were restarted, and the saved record/UUID/dashboard counts were verified unchanged.
- Repeated demo seeding preserved existing records and did not add duplicates.
- Full-name search and the MongoDB-backed distinct-program endpoint passed.
- Disposable test records were removed. The database returned to the seeded counts.
- The Windows Maven bootstrap downloaded Maven, verified its SHA-512 checksum, extracted it, and returned the expected Maven/Java versions.
- The deliverable ZIP passed its archive integrity check. A fresh extraction was launched through `run.bat -NoSeed` on a separate local port; the bundled release JAR served the login page and authenticated the demo administrator without needing Maven.

## HTTP coverage

Authentication: valid login, invalid login, protected API and HTML access, current user, logout and post-logout denial.

Security: invalid CSRF token, cross-origin login denial, safe malformed/missing record errors.

Students: create, read, update, delete, name/enrollment search, combined filters, sorting, pagination, literal regex escaping, invalid page, duplicate enrollment number, invalid semester/email/phone/date.

Courses: create, update, search, filter, duplicate code, restricted deletion and successful deletion after dependency removal.

Enrollments: create, filter by course, duplicate rejection, rejection of deletion with dependent academic records.

Attendance: reject an unenrolled student, create, update, duplicate rejection, date/student filter, absent/late calculation and deletion.

Results: create, reject client-supplied fake percentage/grade by recalculating, duplicate rejection, invalid marks rejection, update, grade/search filters and deletion.

Analytics/reports: dashboard counts change after creation and return after deletion, profile attendance totals, JSON report, CSV export.

Errors: 404 page, 403/500 designed pages, missing API and malformed ID.

## Browser checks

Inspected the actual Java-served application in a Chromium-based browser:

- Polished login, demo-fill control, successful sign-in and dashboard data.
- Desktop sidebar, student table, program filtering and search.
- Detailed student profile with enrolled courses, attendance and academic performance.
- Add Student form submission, persisted creation, editing and success toasts.
- Delete confirmation dialog and cancellation.
- Course catalog and course-to-enrolled-students navigation.
- Attendance entry form and visible server validation when the student is not enrolled.
- Results, reports, and read-only administrator profile.
- Mobile layout at a 390 × 844 viewport, collapsed/open navigation, and no page-wide horizontal overflow; wide tables have their own scrolling region.
- No browser JavaScript errors were reported in the final checked views.

Screenshots in `docs/screenshots/` show fictional data from the running application.

## Verification boundaries

This does not claim an exhaustive security audit, load test, all-browser certification, or production deployment readiness. JDK 17 compatibility is enforced by the compiler target; runtime execution was verified on JDK 25. The project is designed for a single local administrator and small demo datasets. See README limitations for analytics/selectors, concurrency, and account-management scope.

Maven's shaded JAR build reports overlapping dependency metadata. The application compiled, all tests passed, and the resulting JAR was exercised successfully. JDK 25 also reports an upstream Maven/Guice deprecated-API warning; it did not cause a build failure.
